
import com.poc.osm.model.OSMAttributedEntity;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.model.OSMRelatedObject;
import com.poc.osm.model.OSMRelation;


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : "c:\\temp\\t2.gdb") {
		featureclass("pts", ESRI_GEOMETRY_POINT,"WGS84") {
			_long('id')
		}
		featureclass("lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_long('id')
		}
		featureclass("polygon", ESRI_GEOMETRY_POLYGON,"WGS84") {
			_long('id')
		}
		table("rels") {
			_long('id')
			_long('rid')
			_text('role')
			_text('type')
		}
		
		
	}

	// dummy filter
	// f = filter { e -> return true }

	// a stream
	
	rels = stream(osmstream, label:"relations") {
		filter {
			e ->
			 (e instanceof OSMRelation) && e.getFields() != null
	   }
		
		transform { OSMRelation e ->
			e.getFields()?.clear();
			e.setValue("id",e.getId());
				
			e?.relations.collect { 
				
				OSMAttributedEntity ro = new OSMAttributedEntity(e.id, e.fields);
				ro.setValue("id", e.id);
				ro.setValue("rid", it.relatedId);
				ro.setValue('role', it.relation);
				ro.setValue('type', it.type);
				
				ro;
				
			}
				
			
		}
	}
	
	
	t = stream(osmstream, label:"Points with informations") {

		filter {
			 e ->
			  (e instanceof OSMEntity) && e.geometryType == Geometry.Type.Point && e.fields != null
		}
		
		transform { OSMEntity e ->
			e.getFields()?.clear();
			e.setValue("id",e.id);
			return e;
		}

	}
	// a stream
	l = stream(osmstream, label:"polylines") {

		filter {
			 e ->
			   e instanceof OSMEntity && e.geometryType == Geometry.Type.Polyline && e.getFields() != null
		}
		
		transform { OSMEntity e ->
			e.getFields()?.clear();
			e.setValue("id",e.id);
			return e;
		}

	}
	polys = stream(osmstream, label:"polygones") {
		
				filter {
					 e ->
					(e instanceof OSMEntity) &&   e.geometryType == Geometry.Type.Polygon && e.getFields() != null
				}
				
				transform { OSMEntity e ->
					e.getFields()?.clear();
					e.setValue("id",e.id);
					return e;
				}
		
			}
		
	
	// flux de sortie
	out(streams : t, gdb : sortie, tablename:"pts")
	out(streams : l, gdb : sortie, tablename:"lines")
	out(streams : rels, gdb : sortie, tablename:"rels")
	out(streams : polys, gdb : sortie, tablename:"polygon")
	


}

