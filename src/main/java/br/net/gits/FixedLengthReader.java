package br.net.gits;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import br.net.gits.annotation.Line;
import br.net.gits.exception.ReadException;
import br.net.gits.transform.Field;
import br.net.gits.transform.LineMapper;

public class FixedLengthReader {

	private int lineCount = 0;

	private LineIterator it = null;

	private InputStream input;

	private String charset = StandardCharsets.UTF_8.name();

	Map<String, LineMapper> lineMappers = new HashMap<>();

	LineMapper lineMapperDefault;

	private List<String> sorted = new ArrayList<>();

	/**
	 * Get instance for iteration
	 * 
	 * @return
	 */
	public static FixedLengthReader init() {
		return new FixedLengthReader();
	}

	/**
	 * Set input stream and chaset
	 * 
	 * @param input
	 * @param charset
	 * @return
	 */
	public FixedLengthReader input(InputStream input, String charset) {
		this.charset = charset;
		return input(input);
	}

	/**
	 * Set input stream
	 * 
	 * @param input
	 * @return
	 */
	public FixedLengthReader input(InputStream input) {
		this.lineCount = 0;
		this.input = input;
		return this;
	}

	/**
	 * Open itarator
	 * 
	 * @return
	 */
	public FixedLengthReader open() {
		try {
			this.it = IOUtils.lineIterator(this.input, this.charset);
		} catch (Exception e) {
			throw new ReadException("Could not open input stream", e);
		}
		return this;
	}

	/**
	 * Add mapper to iteration
	 * 
	 * @param clazz must be annotated with <code>@Line</code>
	 * @return
	 */
	public FixedLengthReader mapper(Class<?> clazz) {
		if (!clazz.isAnnotationPresent(Line.class)) {
			throw new ReadException("Invalid Mapper, annotation @Line missing.");
		}
		var fieldAnnotation = br.net.gits.annotation.Field.class;

		LineMapper mapper = new LineMapper();
		mapper.length = clazz.getAnnotation(Line.class).length();
		mapper.pattern = clazz.getAnnotation(Line.class).pattern();
		mapper.type = clazz;
		var fieldClass = FieldUtils.getFieldsListWithAnnotation(clazz, fieldAnnotation);
		var fields = fieldClass.stream().map(item -> {
			var setted = item.getAnnotation(fieldAnnotation);
			return new Field(item.getName(), setted.min(), setted.max(), setted.trim(), setted.onlyNumbers(),
					setted.decimal(), setted.padPostion(), setted.padCharacter());
		}).collect(Collectors.toList());

		for (Field field : fields) {
			mapper.fields.put(field.getName(), field);
		}

		lineMapperDefault = mapper;
		lineMappers.put(mapper.pattern, mapper);
		sorted.add(mapper.pattern);
		return this;
	}

	/**
	 * Iterator has next function
	 * 
	 * @return
	 */
	public boolean hasNext() {
		return it.hasNext();
	}

	/**
	 * 
	 * @param line
	 * @param clazz must be annotated with <code>@Line</code>
	 * @return
	 */
	public static Object getLine(String line, Class<?> clazz) {
		return getLine(line, clazz, 1);
	}

	/**
	 * Get object from supplied line
	 * 
	 * @param line
	 * @param clazz     must be annotated with <code>@Line</code>
	 * @param lineCount
	 * @return
	 */
	public static Object getLine(String line, Class<?> clazz, int lineCount) {

		if (!clazz.isAnnotationPresent(Line.class)) {
			throw new ReadException("Invalid Mapper, annotation @Line missing.");
		}

		var fieldAnnotation = br.net.gits.annotation.Field.class;

		LineMapper mapper = new LineMapper();
		mapper.length = clazz.getAnnotation(Line.class).length();
		mapper.type = clazz;
		var fieldClass = FieldUtils.getFieldsListWithAnnotation(clazz, fieldAnnotation);
		var fields = fieldClass.stream().map(item -> {
			var setted = item.getAnnotation(fieldAnnotation);
			return new Field(item.getName(), setted.min(), setted.max(), setted.trim(), setted.onlyNumbers(),
					setted.decimal(), setted.padPostion(), setted.padCharacter());
		}).collect(Collectors.toList());
		for (Field field : fields) {
			mapper.fields.put(field.getName(), field);
		}

		if (StringUtils.isBlank(line))
			return null;

		return extract(line, mapper, lineCount);
	}

	/**
	 * Iterate over the InputStream, identify a mapper for the line extract object
	 * informed in <b>LineMapper</b> <code>type</code> identified
	 * 
	 * @return
	 */
	public Object read() {
		lineCount++;
		String line = it.nextLine();
		if (StringUtils.isBlank(line))
			return null;

		var mapper = this.lineMapperDefault;

		if (this.lineMappers.size() > 1)
			mapper = match(line);

		return extract(line, mapper, this.lineCount);
	}

	/**
	 * Extract object supplied in <b>LineMapper</b> <code>type</code> from String
	 * line
	 * 
	 * @param line
	 * @param mapper
	 * @param lineCount
	 * @return Object instance evaluated
	 */
	private static Object extract(String line, LineMapper mapper, int lineCount) {

		var mapped = doTokenize(line, mapper, lineCount);
		Object o = null;
		try {
			o = mapper.type.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			var message = String.format("The class {%s} hasn't 'no args constructor'", mapper.type.toString());
			throw new ReadException(message);
		}

		Map<String, String> errors = new HashMap<>();
		for (Entry<String, String> entryMapped : mapped.entrySet()) {
			try {

				Class<?> type = PropertyUtils.getPropertyType(o, entryMapped.getKey());

				if (type == int.class || type == Integer.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), Integer.parseInt(entryMapped.getValue()));

				if (type == long.class || type == Long.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), Long.parseLong(entryMapped.getValue()));

				if (type == float.class || type == Float.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), Float.parseFloat(entryMapped.getValue()));

				if (type == double.class || type == Double.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), Double.parseDouble(entryMapped.getValue()));

				if (type == byte.class || type == Byte.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), Byte.parseByte(entryMapped.getValue()));

				if (type == short.class || type == Short.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), Short.parseShort(entryMapped.getValue()));

				if (type == BigDecimal.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), new BigDecimal(entryMapped.getValue()));

				if (type == String.class)
					PropertyUtils.setProperty(o, entryMapped.getKey(), entryMapped.getValue());

			} catch (Exception e) {
				var fieldInfo = mapper.fields.get(entryMapped.getKey());
				errors.put(entryMapped.getKey(), String.format("line position %d to %d , value: [%s] ",
						fieldInfo.getMin(), fieldInfo.getMax(), entryMapped.getValue()));
			}
		}
		if (!errors.isEmpty()) {
			throw new ReadException(String.format("Found errors at line %d ", lineCount), errors);
		}

		return o;
	}

	/**
	 * Find first <b>LineMapper</b> using <code>pattern</code>
	 * 
	 * @param line
	 * @return
	 */
	private LineMapper match(String line) {
		LineMapper value = null;
		for (String key : sorted) {
			if (match(key, line)) {
				value = this.lineMappers.get(key);
				break;
			}
		}
		if (value == null)
			throw new ReadException(String.format("Could not find a mapper for line %d, value: [%s]", lineCount, line));

		return value;
	}

	/**
	 * Yields the tokens resulting from the splitting pair name value of the
	 * supplied <code>line</code>.
	 * 
	 * @param line the line to be tokenized (can be <code>null</code>)
	 * 
	 * @return the resulting tokens (empty if the line is null)
	 * @throws IncorrectLineLengthException if line length is greater than or less
	 *                                      than the max range set.
	 */
	/**
	 * Yields the tokens resulting from the splitting of the supplied
	 * <code>line</code>.
	 * 
	 * @param line
	 * @param mapper
	 * @param lineCount
	 * 
	 * @return Map of field names and values
	 */
	private static Map<String, String> doTokenize(String line, LineMapper mapper, int lineCount) {
		Map<String, String> tokens = new HashMap<>();
		StringBuffer token;
		var maxRange = calculateMaxRange(mapper.fields.values());
		var lineLength = line.length();
		if (lineLength < maxRange) {
			throw new ReadException(String.format("Line %d is shorter than max range, line size %d, range %d ",
					lineCount, lineLength, maxRange));
		}
		for (Field f : mapper.fields.values()) {
			token = new StringBuffer();
			var startPos = f.getMin() - 1;
			var endPos = f.getMax();
			var chunck = line.substring(startPos, endPos);
			token.append(f.isTrim() ? chunck.trim() : chunck);
			if (f.isOnlyNumbers() && f.getDecimal() > 0) {
				token.insert(token.length() - f.getDecimal(), ".");
			}
			tokens.put(f.getName(), token.toString());
		}
		return tokens;
	}

	/**
	 * Return maximun range of fields collection
	 * 
	 * @param collection
	 * @return
	 */
	private static int calculateMaxRange(Collection<Field> collection) {
		int maxRange = 0;
		for (Field field : collection) {
			if (field.getMax() > maxRange) {
				maxRange = field.getMax();
			}
		}
		return maxRange;
	}

	/**
	 * Lifted from AntPathMatcher in Spring Core. Tests whether or not a string
	 * matches against a pattern. The pattern may contain two special
	 * characters:<br>
	 * '*' means zero or more characters<br>
	 * '?' means one and only one character
	 * 
	 * @param pattern pattern to match against. Must not be <code>null</code>.
	 * @param str     string which must be matched against the pattern. Must not be
	 *                <code>null</code>.
	 * @return <code>true</code> if the string matches against the pattern, or
	 *         <code>false</code> otherwise.
	 */
	private static boolean match(String pattern, String str) {
		int patIdxStart = 0;
		int patIdxEnd = pattern.length() - 1;
		int strIdxStart = 0;
		int strIdxEnd = str.length() - 1;
		char ch;

		boolean containsStar = pattern.contains("*");

		if (!containsStar) {
			// No '*'s, so we make a shortcut
			if (patIdxEnd != strIdxEnd) {
				return false; // Pattern and string do not have the same size
			}
			for (int i = 0; i <= patIdxEnd; i++) {
				ch = pattern.charAt(i);
				if (ch != '?') {
					if (ch != str.charAt(i)) {
						return false;// Character mismatch
					}
				}
			}
			return true; // String matches against pattern
		}

		if (patIdxEnd == 0) {
			return true; // Pattern contains only '*', which matches anything
		}

		// Process characters before first star
		while ((ch = pattern.charAt(patIdxStart)) != '*' && strIdxStart <= strIdxEnd) {
			if (ch != '?') {
				if (ch != str.charAt(strIdxStart)) {
					return false;// Character mismatch
				}
			}
			patIdxStart++;
			strIdxStart++;
		}
		if (strIdxStart > strIdxEnd) {
			// All characters in the string are used. Check if only '*'s are
			// left in the pattern. If so, we succeeded. Otherwise failure.
			for (int i = patIdxStart; i <= patIdxEnd; i++) {
				if (pattern.charAt(i) != '*') {
					return false;
				}
			}
			return true;
		}

		// Process characters after last star
		while ((ch = pattern.charAt(patIdxEnd)) != '*' && strIdxStart <= strIdxEnd) {
			if (ch != '?') {
				if (ch != str.charAt(strIdxEnd)) {
					return false;// Character mismatch
				}
			}
			patIdxEnd--;
			strIdxEnd--;
		}
		if (strIdxStart > strIdxEnd) {
			// All characters in the string are used. Check if only '*'s are
			// left in the pattern. If so, we succeeded. Otherwise failure.
			for (int i = patIdxStart; i <= patIdxEnd; i++) {
				if (pattern.charAt(i) != '*') {
					return false;
				}
			}
			return true;
		}

		// process pattern between stars. padIdxStart and patIdxEnd point
		// always to a '*'.
		while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
			int patIdxTmp = -1;
			for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
				if (pattern.charAt(i) == '*') {
					patIdxTmp = i;
					break;
				}
			}
			if (patIdxTmp == patIdxStart + 1) {
				// Two stars next to each other, skip the first one.
				patIdxStart++;
				continue;
			}
			// Find the pattern between padIdxStart & padIdxTmp in str between
			// strIdxStart & strIdxEnd
			int patLength = (patIdxTmp - patIdxStart - 1);
			int strLength = (strIdxEnd - strIdxStart + 1);
			int foundIdx = -1;
			strLoop: for (int i = 0; i <= strLength - patLength; i++) {
				for (int j = 0; j < patLength; j++) {
					ch = pattern.charAt(patIdxStart + j + 1);
					if (ch != '?') {
						if (ch != str.charAt(strIdxStart + i + j)) {
							continue strLoop;
						}
					}
				}

				foundIdx = strIdxStart + i;
				break;
			}

			if (foundIdx == -1) {
				return false;
			}

			patIdxStart = patIdxTmp;
			strIdxStart = foundIdx + patLength;
		}

		// All characters in the string are used. Check if only '*'s are left
		// in the pattern. If so, we succeeded. Otherwise failure.
		for (int i = patIdxStart; i <= patIdxEnd; i++) {
			if (pattern.charAt(i) != '*') {
				return false;
			}
		}

		return true;
	}
}
