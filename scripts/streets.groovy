
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
		filter { e ->
			isPolyline(e) && has(e,"highway")
		}
		transform {  e ->
			on(e).keep("name").keep("highway").newValue("id",e.id).end()
		}
	}

	// flux de sortie
	out(streams : [b], sink : sortie, tablename:"streets")

}

