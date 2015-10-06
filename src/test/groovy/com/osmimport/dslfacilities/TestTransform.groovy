package com.osmimport.dslfacilities

import static org.junit.Assert.*

import org.junit.Test
import org.junit.runner.RunWith

import com.osmimport.model.OSMAttributedEntity

class TestTransform {
	
	@Test
	void test() {	
		def o = new OSMAttributedEntity(0, ["toto":"titi", "tutu":"tete"]);
		println Transform.on(o).map("toto").into("test").end().dump();
		assertEquals o.fields["test"], "titi"
		assertEquals o.fields.size() , 1
	}
	
	@Test
	void testNullFields() {
		def o = new OSMAttributedEntity(0, null);
		println Transform.on(o).map("toto").into("test").end().dump();
	}
	
	@Test
	void testNoFieldsRetained() {
		def o = new OSMAttributedEntity(0, ["titi":"tutu"]);
		println Transform.on(o).map("tete").into("tete").end().dump();
		assertNotNull o.fields
		assertNull o.fields["tete"]
	}
	
	
	@Test
	void testSettingNoValues() {
		def o = new OSMAttributedEntity(0, null);
		println Transform.on(o).end().dump();
		assertNull o.fields
	}
	
	@Test
	void testSettingValues() {
		def o = new OSMAttributedEntity(0, null);
		println Transform.on(o).newValue("toto", 32).end().dump();
	}
	
	@Test
	void testKeepField() {
		def o = new OSMAttributedEntity(0, ["toto":"titi"]);
		println Transform.on(o).keep("toto").end().dump();
		assertTrue o.fields.containsKey("toto") && o.fields["toto"] == "titi"
	}
	
	@Test
	void testKeepFieldNull() {
		def o = new OSMAttributedEntity(0, null);
		println Transform.on(o).keep("toto").end().dump();
		assertTrue o.fields.containsKey("toto")
	}
	
}
