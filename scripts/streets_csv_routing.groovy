import com.esri.core.geometry.Geometry
import com.osmimport.model.OSMEntity
import com.osmimport.model.OSMEntityGeometry


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = csv(path : var_ouputcsv) {
		featureclass("streets", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_integer('id')
			_text("type")
			_text("name")
			_text("oneway")
		}
	}

	// a stream
	b = stream(osmstream, label:"streets") {

		filter { e ->
			isPolyline(e) && has(e,"highway")
		}

		transform {  e ->
			on(e).map("highway").into("type").
					keep("name").
					keep("oneway").
					newValue("id", e.id).end()
			return e;
		}

	}

	// flux de sortie
	out(streams : [b], sink : sortie, tablename:"streets")

}


