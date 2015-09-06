import com.esri.core.geometry.Geometry
import com.osmimport.model.OSMEntity
import com.osmimport.model.OSMEntityGeometry


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = csv(path : var_gdb) {
		featureclass("streets", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_integer('id')
			_text("type")
			_text("name")
			_text("oneway")
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
			String oneway = e.getFields()?.get("oneway")
			e.getFields()?.clear()
			e.setValue("id",e.id)
			e.setValue("name", name)
			e.setValue("type", t)
			e.setValue("oneway",oneway)
			return e;
		}

	}

	
	// flux de sortie
	out(streams : [b], sink : sortie, tablename:"streets")

	
}

