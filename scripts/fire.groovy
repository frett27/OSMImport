/*
    This script, extract the osm "fire" entities  

*/


	// construction de la chaine
	builder.build(osmstream) {
	
		// défini un flux de sortie, et description de la structure
		sortie = gdb(path : var_gdb) {
				featureclass("firestations", ESRI_GEOMETRY_POLYLINE,"WGS84") {
				_integer('id')
				_text("type")
				_text("name")
			}
		}
	
		// a stream
		b = stream(osmstream, label:"streets") {
	
			filter {
				 e -> isPoint(e) && has(e,"amenity","fire_station")
			}
			
			transform {  e ->
				
				on(e).map("name").into("name").newValue("id", e.id).end()
				
			}
	
		}
	
		
		// flux de sortie
		out(streams : [b], sink : sortie, tablename:"firestations")
	
	
}

