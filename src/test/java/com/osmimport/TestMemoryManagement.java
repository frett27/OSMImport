package com.osmimport;

import junit.framework.TestCase;

public class TestMemoryManagement extends TestCase {

	public void testMemory() {
		int nbofworkers = 4;
		long v = (long) ((Runtime.getRuntime().maxMemory() * 1.0 - 3_000_000_000.0) / (1_000_000_000.0) * 180_000 / nbofworkers);
		System.out.println(v);
	}
}
