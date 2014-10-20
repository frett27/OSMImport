package com.poc.osm.output.dsl

import com.esrifrance.fgdbapi.xml.EsriGeometryType;
import com.poc.osm.output.Stream;
import com.poc.osm.output.dsl.TBuilder;

import groovy.util.GroovyTestCase

class DSLTestCase extends GroovyTestCase {

	/**
	 * Test de définition de la sémantique de flux de l'outil
	 */
	void testFilter() {

		// définition du flux initial des objets osm
		def mystream = new Stream()

		def sb = new TBuilder()

		// construction de la chaine
		def s = sb.build {

			// défini un flux de sortie, et description de la structure
			sortie = gdb(path : "monchemin.gdb") {
				featureclass("matable", EsriGeometryType.ESRI_GEOMETRY_POINT,"WGS84") {
					_text("mon texte", size : 40)
					_integer("mon champ entier")
					_double("autre champ")
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
				transform { e -> return null; }
			}

			// flux de sortie
			out(stream : t, gdb : sortie, tablename:"matable")


		}

		System.out.println(s);
	}
}


