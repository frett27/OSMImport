import com.esri.core.geometry.Geometry
import com.esri.core.geometry.MultiPath
import com.esri.core.geometry.Polygon
import com.osmimport.model.OSMEntity
import com.osmimport.model.OSMEntityGeometry


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_gdb) {
		featureclass("amenity", ESRI_GEOMETRY_POINT, "WGS84") {
			_integer('id')
			_text("amenity")
			_text("name")
			_text("religion")
			_text("parking")
		}
	}

	// a stream
	b = stream(osmstream, label:"amenity") {
		filter {
			e -> 
			 isPoint(e) && has(e,"amenity") 
		}
		transform {  e ->
			on(e).newValue("id",e.id).
				map("amenity").into("amenity").
				map("name").into("name").
				map("religion").into("religion").
				map("parking").into("parking").
				end()
			return e;
		}
	}
	
	// flux de sortie
	out(streams : [b], sink : sortie, tablename:"amenity")
	
}

