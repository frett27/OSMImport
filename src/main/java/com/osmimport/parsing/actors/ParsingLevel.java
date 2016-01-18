package com.osmimport.parsing.actors;

public enum ParsingLevel {
	PARSING_LEVEL_POINT(0), PARSING_LEVEL_LINE(1), PARSING_LEVEL_POLYGON(2);

	private int value;

	ParsingLevel(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ParsingLevel fromInt(int level) {
		if (level == 2) {
			return PARSING_LEVEL_POLYGON;
		} else if (level == 1) {
			return PARSING_LEVEL_LINE;
		} else if (level == 0) {
			return PARSING_LEVEL_POINT;
		} else {
			throw new RuntimeException(
					"Bad parsing level , only 0,1,2 accepted");
		}
	}
}
