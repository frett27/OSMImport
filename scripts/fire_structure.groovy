/*
    This script, extract the osm "fire" entities  

*/


	// construction de la structure
builder.structure() {
	
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

