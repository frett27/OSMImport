
// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_gdb) {
		featureclass("streets", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_integer('id')
			_text("highway")
			_text("oneway")
			_text("name")
			_text("tunnel")
			_text("access")
			_text("bicycle")
			_text("minspeed")
			_text("maxspeed")
			
		}
		featureclass("traffic", ESRI_GEOMETRY_POINT,"WGS84") {
			_integer('id')
			_text("highway")
			_text("barrier")
			_text("traffic_calming")
			_text("direction")
			_text("crossing")
			
			
		}
		
	}

	// a stream
	b = stream(osmstream, label:"streets") {
		filter { e ->
			def cond = isPolyline(e) && has(e,"highway") && 
				(e.fields["highway"] in ["tertiary","tertiary_link","motorway",
					"motorway_link", "primary","primary_link","secondary","secondary_link",
					"yes","trunk","trunk_link","road","unclassified","residential" ])
			cond
		}
		transform {  e ->
			on(e).keep("name").keep("highway").
			keep("oneway").keep("tunnel").keep("access").
			keep("bicycle").keep("minspeed").
			keep("maxspeed").
			newValue("id",e.id).
			end()
		}
	}
	
	ts = stream(osmstream, label:"traffic_signal") {
		filter { e-> 
			isPoint(e) && has(e,"highway")
			
		}
		transform { e->
			def v = on(e).keep("highway").keep("barrier").
			map("traffic_signals:direction").into("direction").
			keep("traffic_calming").
			keep("crossing").
			end()
			
			
			
		}
		
	}

	// flux de sortie
	out(streams : [b], sink : sortie, tablename:"streets")
	out(streams : [ts], sink : sortie, tablename:"traffic")
	

}

