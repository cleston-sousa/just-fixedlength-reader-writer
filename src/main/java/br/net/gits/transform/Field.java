package br.net.gits.transform;

import lombok.ToString;

@ToString
public class Field {

	final private int min;

	final private int max;

	final private boolean trim;

	final private int decimal;

	final private boolean onlyNumbers;

	final private char padCharacter;

	final private PadPosition padPostion;

	public boolean isTrim() {
		return trim;
	}

	final private String name;

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public String getName() {
		return name;
	}

	public int getDecimal() {
		return decimal;
	}

	public boolean isOnlyNumbers() {
		return onlyNumbers;
	}

	public Field(String name, int min, int max, boolean trim, boolean onlyNumbers, int decimal, PadPosition padPostion,
			char padCharacter) {
		checkMinMaxValues(min, max);
		this.trim = trim;
		this.decimal = decimal;
		this.onlyNumbers = onlyNumbers;
		this.padCharacter = padCharacter;
		this.padPostion = padPostion;
		this.name = name;
		this.min = min;
		this.max = max;
	}

	private void checkMinMaxValues(int min, int max) {
		if (!(min > 0)) {
			throw new IllegalArgumentException("Min value must be higher than zero");
		}
		if (!(min <= max)) {
			throw new IllegalArgumentException("Min value should be lower or equal to max value");
		}

	}

	public char getPadCharacter() {
		return padCharacter;
	}

	public PadPosition getPadPostion() {
		return padPostion;
	}

}
