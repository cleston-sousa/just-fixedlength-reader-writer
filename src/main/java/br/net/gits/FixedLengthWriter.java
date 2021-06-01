package br.net.gits;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import br.net.gits.annotation.Line;
import br.net.gits.exception.ReadException;
import br.net.gits.exception.WriteException;
import br.net.gits.transform.Field;
import br.net.gits.transform.PadPosition;

public class FixedLengthWriter {

	private int lineCount = 0;

	private StringBuffer sb = new StringBuffer();

	/**
	 * Get instance for iteration
	 * 
	 * @return
	 */
	public static FixedLengthWriter init() {
		return new FixedLengthWriter();
	}

	/**
	 * return string from mapped object
	 * 
	 * @param mappedObject must have @Line annotation
	 * @return
	 */
	public static String getLine(Object mappedObject) {

		if (!mappedObject.getClass().isAnnotationPresent(Line.class)) {
			throw new ReadException("Invalid Mapper, annotation @Line missing.");
		}

		var lineLength = mappedObject.getClass().getAnnotation(Line.class).length();

		var fieldAnnotation = br.net.gits.annotation.Field.class;
		var fieldClass = FieldUtils.getFieldsListWithAnnotation(mappedObject.getClass(), fieldAnnotation);

		var fields = fieldClass.stream().map(item -> {
			var setted = item.getAnnotation(fieldAnnotation);
			return new Field(item.getName(), setted.min(), setted.max(), setted.trim(), setted.onlyNumbers(),
					setted.decimal(), setted.padPostion(), setted.padCharacter());
		}).collect(Collectors.toList());

		int maxRange = calculateMaxRange(fields);
		if (lineLength == 0)
			lineLength = maxRange;

		if (maxRange > lineLength)
			throw new WriteException(
					String.format("One or more fields has the end at position %d and the line lenght is set to %d",
							maxRange, lineLength));

		fields.sort(new Comparator<Field>() {
			@Override
			public int compare(Field o1, Field o2) {
				return o1.getMin() > o2.getMin() ? 1 : -1;
			}
		});

		StringBuffer line = new StringBuffer(padRight(" ", lineLength, ' '));
		Map<String, String> errors = new HashMap<>();
		for (Field field : fields) {
			try {

				Object value = BeanUtils.getSimpleProperty(mappedObject, field.getName());

				var s = value.toString();
				if (field.isOnlyNumbers())
					s = onlyNumbers(s);

				if (field.getPadPostion().equals(PadPosition.RIGHT)) {
					s = padRight(s, field.getMax() - field.getMin() + 1, field.getPadCharacter());
				} else {
					s = padLeft(s, field.getMax() - field.getMin() + 1, field.getPadCharacter());
				}

				line.insert(field.getMin() - 1, s);

			} catch (Exception e) {
				errors.put(field.getName(), String.format("could not write value at line position %d to %d",
						field.getMin(), field.getMax()));
			}

		}

		if (!errors.isEmpty()) {
			throw new ReadException(String.format("Found errors at line when writing line"), errors);
		}

		return padRight(line.toString(), lineLength, ' ');
	}

	/**
	 * add object values to string buffer
	 * 
	 * 
	 * @param mappedObject must have @Line annotation
	 * @return
	 */
	public FixedLengthWriter add(Object mappedObject) {

		lineCount++;

		try {
			this.sb.append(getLine(mappedObject) + "\n");
		} catch (Exception e) {
			throw new ReadException(String.format("Found errors at line %d ", lineCount));
		}

		return this;
	}

	/**
	 * output string buffered
	 * 
	 * @return
	 */
	public String content() {
		return sb.toString();
	}

	/**
	 * get max range from fields
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
	 * make a string filling or slicing the right side if necessary
	 * 
	 * @param input
	 * @param length
	 * @param padChar char to fill if string size is less than length
	 * @return
	 */
	public static String padRight(String input, int length, char padChar) {
		StringBuilder output = new StringBuilder(input);
		if (output.length() < length) {
			while (output.length() < length) {
				output.append(padChar);
			}
		} else if (output.length() > length) {
			return output.substring(0, length);
		}
		return output.toString();
	}

	/**
	 * make a string filling or slicing the left side if necessary
	 * 
	 * @param input
	 * @param length
	 * @param padChar char to fill if string size is less than length
	 * @return
	 */
	public static String padLeft(String input, int length, char padChar) {
		StringBuilder output = new StringBuilder(input);

		if (output.length() < length) {
			while (output.length() < length) {
				output.insert(0, padChar);
			}
		} else if (output.length() > length) {
			return output.substring(0, length);
		}
		return output.toString();
	}

	/**
	 * Remove all chars in the string but numbers
	 * 
	 * @param value
	 * @return
	 */
	public static String onlyNumbers(String value) {
		return value.toString().replaceAll("[^0-9]*", "");
	}

}
