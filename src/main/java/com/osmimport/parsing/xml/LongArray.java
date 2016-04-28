package com.osmimport.parsing.xml;

import java.util.Arrays;

public class LongArray {

	private long[] array;
	private int nbelements = 0;
	private int capacity = 100;

	public LongArray(int capacity) {
		assert capacity > 0;
		this.capacity = capacity;
		array = new long[capacity];
	}

	public void add(long l) {
		if (nbelements == array.length) {
			// copy on write
			array = Arrays.copyOf(array, array.length + capacity);
		}
		array[nbelements++] = l;
	}

	public long[] getArray() {
		return Arrays.copyOf(array, nbelements);
	}

}
