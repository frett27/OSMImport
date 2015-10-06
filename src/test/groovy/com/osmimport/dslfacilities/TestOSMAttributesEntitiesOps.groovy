package com.osmimport.dslfacilities;

import static org.junit.Assert.*

import org.junit.Test

import com.osmimport.model.OSMEntityPoint
import com.osmimport.output.Stream
import com.osmimport.output.dsl.TBuilder

class TestOSMAttributesEntitiesOps {

	@Test
	public void testIsPoint() {

		use(OSMAttributedEntitiesOps) {
			OSMEntityPoint p = new OSMEntityPoint(0, 1, 1, null);
			assertTrue(p.isPoint());
		}
	}

	@Test
	public void testIsPointInBuilder() {
		use(OSMAttributedEntitiesOps) {
			def osmstream = new Stream()
			def b = new TBuilder().build(osmstream) {

				stream(osmstream) {

					filter { e ->
						e.isPolyline()
					}
					
					transform {
						e -> 
						
						e
					}
				}


				OSMEntityPoint p = new OSMEntityPoint(0, 1, 1, null);
				assertTrue(p.isPoint());
			}
			
			print b
			
			print b.streams[0].filter.filter(new OSMEntityPoint(0,1,2,null))
			
		} // use
	}
}
