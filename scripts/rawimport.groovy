
import com.poc.osm.model.OSMAttributedEntity;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.model.OSMRelatedObject;
import com.poc.osm.model.OSMRelation;


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : "c:\\temp\\t.gdb") {
		featureclass("pts", ESRI_GEOMETRY_POINT,"WGS84") {
			/* _text("k", size : 40)
			_integer("mon champ entier")
			_double("autre champ") */
			_integer('id')
		}
		featureclass("lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			/* _text("k", size : 40)
			_integer("mon champ entier")
			_double("autre champ") */
			_integer('id')
		}
		featureclass("polygon", ESRI_GEOMETRY_POLYGON,"WGS84") {
			/* _text("k", size : 40)
			_integer("mon champ entier")
			_double("autre champ") */
			_integer('id')
		}
		table("rels", "WGS84") {
			_integer('id')
			_integer('rid')
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
			
			f = { entity -> 
				def listr = new ArrayList<OSMRelatedObject>();
				
				entity?.relations.each { OSMRelatedObject r ->
					
					OSMAttributedEntity ro = new OSMAttributedEntity(entity.id, entity.fields);
					ro.setValue("id", entity.id);
					ro.setValue("rid", r.relatedId);
					ro.setValue('role', r.relation);
					ro.setValue('type', r.type);
					
					listr << ro;
					
				}
				
				listr
			}
			
			return (f(e) as List);
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

