package com.osmimport.parsing.csv;

import java.util.Map;

import com.osmimport.model.OSMRelatedObject;

import junit.framework.TestCase;

public class TestMapStringToolsParsing extends TestCase {

	public void testParsing() throws Exception {

		Map<String, String> hrels = MapStringTools.fromString("relid=26659011|role=outer|type=way");

		String rrelsid = hrels.get("relid");
		String rrole = hrels.get("role");
		String rtype = hrels.get("type");

		OSMRelatedObject osmrel = new OSMRelatedObject(Long.parseLong(rrelsid), rrole, rtype);

	}
	
}
