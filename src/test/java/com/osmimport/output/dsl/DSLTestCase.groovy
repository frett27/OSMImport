package com.osmimport.output.dsl

import org.fgdbapi.thindriver.xml.EsriGeometryType;

import com.osmimport.output.dsl.TBuilder;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.output.Filter;
import com.osmimport.output.Stream
import com.osmimport.output.Transform;

class T extends Filter {
	@Override
	public boolean filter(OSMAttributedEntity e) {
		return false;
	}
}

class DSLTestCase extends GroovyTestCase {

	/**
	 * Test de définition de la sémantique de flux de l'outil
	 */
	void testFilter() {
				// définition du flux initial des objets osm
				def mystream = new Stream()
		
				def sb = new TBuilder()
				
				
				// construction de la chaine
				def s = sb.build(mystream) {
		
					// défini un flux de sortie, et description de la structure
					sortie = gdb(path : "monchemin.gdb") {
						featureclass("matable", EsriGeometryType.ESRI_GEOMETRY_POINT,"WGS84") {
							_text("mon texte", size : 40)
							_integer("mon champ entier")
							_double("autre champ")
						}
					}
		
					for (i in [1,5]) {
						t=stream(mystream) {
							// use Equals to statically type the filter
							filter ( new T() )
							
							transform = { e-> return e } as Transform
							
						}
					}
					
					// dummy filter
					f = filter { e -> return true }
		
					// a stream
					t = stream(mystream) {
		
						filter { e ->
							f(e) // reference à un filtre au dessus, 
								 // this function return the result
						}
		
					}
		
					// transformation d'un flux
					stream(t){
						transform  = { e -> return null; }
					}
		
					
				
					
					// flux de sortie
					out(streams : t, gdb : sortie, tablename:"matable")
		
		
				}
		
				System.out.println(s);
				
		
		
	}
}


