
builder.build(osmstream) {
	sortie = csv(path : var_gdb) {
		featureclass('highway_pt',ESRI_GEOMETRY_POLYLINE, 'WGS84') {
			_text('osmid',size:50)    _text('width',size:50)
			_text('sac_scale',size:50)
			_text('access',size:50)
			_text('surface',size:50)
			_text('name',size:50)
			_text('direction',size:50)
			_text('cycleway',size:50)
			_text('ref',size:50)
			_text('lit',size:50)
			_text('oneway',size:50)
			_text('destination',size:50)
			_text('motorroad',size:50)
			_text('lanes',size:50)
			_text('step_count',size:50)
			_text('reference',size:50)
			_text('abutters',size:50)
			_text('area',size:50)
			_text('sidewalk',size:50)
			_text('operator',size:50)
			_text('service',size:50)
			_text('tracktype',size:50)
			_text('footway',size:50)
			_text('highway',size:50)
		}
		featureclass('highway_l',ESRI_GEOMETRY_POINT, 'WGS84') {
			_text('osmid',size:50)    _text('width',size:50)
			_text('sac_scale',size:50)
			_text('access',size:50)
			_text('surface',size:50)
			_text('name',size:50)
			_text('direction',size:50)
			_text('cycleway',size:50)
			_text('ref',size:50)
			_text('lit',size:50)
			_text('oneway',size:50)
			_text('destination',size:50)
			_text('motorroad',size:50)
			_text('lanes',size:50)
			_text('step_count',size:50)
			_text('reference',size:50)
			_text('abutters',size:50)
			_text('area',size:50)
			_text('sidewalk',size:50)
			_text('operator',size:50)
			_text('service',size:50)
			_text('tracktype',size:50)
			_text('footway',size:50)
			_text('highway',size:50)
		}
		featureclass('highway_p',ESRI_GEOMETRY_POLYGON, 'WGS84') {
			_text('osmid',size:50)    _text('width',size:50)
			_text('sac_scale',size:50)
			_text('access',size:50)
			_text('surface',size:50)
			_text('name',size:50)
			_text('direction',size:50)
			_text('cycleway',size:50)
			_text('ref',size:50)
			_text('lit',size:50)
			_text('oneway',size:50)
			_text('destination',size:50)
			_text('motorroad',size:50)
			_text('lanes',size:50)
			_text('step_count',size:50)
			_text('reference',size:50)
			_text('abutters',size:50)
			_text('area',size:50)
			_text('sidewalk',size:50)
			_text('operator',size:50)
			_text('service',size:50)
			_text('tracktype',size:50)
			_text('footway',size:50)
			_text('highway',size:50)
		}
	}
	highway_pt=stream(osmstream, label: 'highway_pt') {
		filter {  e-> isPoint(e) && has(e,"highway") }
		transform {  e-> on(e).keep([
				"width",
				"sac_scale",
				"access",
				"surface",
				"name",
				"direction",
				"cycleway",
				"ref",
				"lit",
				"oneway",
				"destination",
				"motorroad",
				"lanes",
				"step_count",
				"reference",
				"abutters",
				"area",
				"sidewalk",
				"operator",
				"service",
				"tracktype",
				"footway",
				"highway"
			]).newValue('osmid', e.id).end()  }
	}
	highway_l=stream(osmstream, label: 'highway_l') {
		filter {  e-> isPolyline(e) && has(e,"highway") }
		transform {  e-> on(e).keep([
				"width",
				"sac_scale",
				"access",
				"surface",
				"name",
				"direction",
				"cycleway",
				"ref",
				"lit",
				"oneway",
				"destination",
				"motorroad",
				"lanes",
				"step_count",
				"reference",
				"abutters",
				"area",
				"sidewalk",
				"operator",
				"service",
				"tracktype",
				"footway",
				"highway"
			]).newValue('osmid', e.id).end()  }
	}
	highway_p=stream(osmstream, label: 'highway_p') {
		filter {  e-> isPolygon(e) && has(e,"highway") }
		transform {  e-> on(e).keep([
				"width",
				"sac_scale",
				"access",
				"surface",
				"name",
				"direction",
				"cycleway",
				"ref",
				"lit",
				"oneway",
				"destination",
				"motorroad",
				"lanes",
				"step_count",
				"reference",
				"abutters",
				"area",
				"sidewalk",
				"operator",
				"service",
				"tracktype",
				"footway",
				"highway"
			]).newValue('osmid', e.id).end()  }
	}
	out(streams : [highway_pt ], sink:sortie, tablename: 'highway_pt')
	out(streams : [highway_l ], sink:sortie, tablename: 'highway_l')
	out(streams : [highway_p ], sink:sortie, tablename: 'highway_p')
} // end
