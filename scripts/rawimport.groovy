
import com.osmimport.model.OSMAttributedEntity
import com.osmimport.model.OSMEntity
import com.osmimport.model.OSMRelatedObject
import com.osmimport.model.OSMRelation

/**
 * this script import the osm data, exploding lines, polygons and points in separate featureclasses
 * a table is created for all the relations, attributes are also exploded in a separate table
 */

// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_outputgdb) {
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
		table("attributes") {
			_long('id')
			_text('key')
			_text('value')
		}
	}

	attributed = stream(osmstream, label:"Attribute collecting") {
		transform { e->
            if (e.fields) {
                // there are entities   
                return e.fields.collect { k,v ->
                    def oa = new OSMAttributedEntity(-1)
                    oa.setValue("key",k)
                    oa.setValue("value",v)
                }
            }
			
			return []
		}
	}
	
	
	// relations stream
	
	rels = stream(osmstream, label:"Relations") {
		filter {
			e ->
			 (e instanceof OSMRelation) && e.getFields() != null
	   }
		
		transform { OSMRelation e ->
			
			e.getFields()?.clear();
			
			e.setValue("id",e.getId());
			
			// r = []  don't do this, this lead to concurrent issues
			
			List r = new ArrayList<OSMAttributedEntity>(); 
			
			for (int i = 0 ; i < e.relations.size() ; i ++ )
			{
				OSMRelatedObject related = e.relations.get(i)
				OSMAttributedEntity ro = new OSMAttributedEntity(e.id, e.fields);
				ro.setValue("id", e.id);
				ro.setValue("rid", related.relatedId);
				ro.setValue('role', related.role);
				ro.setValue('type', related.type);
				
				r.add(ro)
			}
			
			
			return r;
		}
	}
	
	
	t = stream(osmstream, label:"Points") {

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
	l = stream(osmstream, label:"Polylines") {

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
					(e instanceof OSMEntity) && e.geometryType == Geometry.Type.Polygon && e.getFields() != null
				}
				
				transform { OSMEntity e ->
					e.getFields()?.clear();
					e.setValue("id",e.id);
					return e;
				}
		
			}
		
	
	// flux de sortie
	out(streams : t, sink : sortie, tablename:"pts")
	out(streams : l, sink : sortie, tablename:"lines")
	out(streams : rels, sink : sortie, tablename:"rels")
	out(streams : polys, sink : sortie, tablename:"polygon")
	out(streams : attributed, sink : sortie, tablename:"attributes")
	


}

