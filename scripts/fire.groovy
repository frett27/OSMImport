/*
    This script, extract the osm "fire" entities  

*/


	// construction de la chaine
	builder.build(osmstream) {
	
		// défini un flux de sortie, et description de la structure
		sortie = gdb(path : var_gdb) {
				featureclass("firestations", ESRI_GEOMETRY_POINT,"WGS84") {
				_long('id')
				_text("type")
				_text("name")
			}
            featureclass("hydrant", ESRI_GEOMETRY_POINT, "WGS84") {

				_long('id')
				_text("type")
				_text("diameter")
				_text("pressure")
				_text("position")
                _integer("count")
            }

           


		}
	
		// a firestation stream
		firestations = stream(osmstream, label:"firestation") {
	
			filter {
				 e -> isPoint(e) && has(e,"amenity","fire_station")
			}
			
			transform {  e ->
				
				on(e).map("name").into("name").newValue("id", e.id).end()
				
			}
	
		}

        hydrant = stream(osmstream, label:"hydrant") {
            filter {
                e -> isPoint(e) && has(e, "emergency", "fire_hydrant")
            }

            transform { e ->
                on(e).
                map("fire_hydrant:type").into("type").
                map("fire_hydrant:diameter").into("diameter").
                map("fire_hydrant:pressure").into("pressure").
                map("fire_hydrant:position").into("position").
                map("fire_hydrant:count").into("count").
                newValue("id", e.id).
                end()

            }
            
        }
    
	
		
		// flux de sortie
		out(streams : [firestations], sink : sortie, tablename:"firestations")
		out(streams : [hydrant], sink : sortie, tablename:"hydrant")
	
	
}

