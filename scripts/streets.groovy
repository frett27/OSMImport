import com.esri.core.geometry.Geometry
import com.osmimport.model.OSMEntity
import com.osmimport.model.OSMEntityGeometry


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_gdb) {
		featureclass("streets", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_integer('id')
			_text("type")
			_text("name")
		}
	}

	// a stream
	b = stream(osmstream, label:"streets") {

		filter {
			 e -> e instanceof OSMEntity &&
			   e.geometryType == Geometry.Type.Polyline  && e.getFields() && e.getFields().containsKey("highway")
		}
		
		transform {  e ->
			String t = e.getFields().get("highway")
			String name = e.getFields()?.get("name")
			e.getFields()?.clear()
			e.setValue("id",e.id)
			e.setValue("name", name)
			e.setValue("type", t)
			return e;
		}

	}

	
	// flux de sortie
	out(streams : [b], gdb : sortie, tablename:"streets")

	
}

