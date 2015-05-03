
import com.poc.osm.model.OSMAttributedEntity
import com.poc.osm.model.OSMEntity
import com.poc.osm.model.OSMRelatedObject
import com.poc.osm.model.OSMRelation


def createRelations(OSMRelation e) {

	e.getFields()?.clear();

	e.setValue("id",e.getId());

	ArrayList<OSMAttributedEntity> r = new ArrayList<OSMAttributedEntity>();

	for (int i = 0 ; i < e.relations.size() ; i ++ ) {
		OSMRelatedObject related = e.relations.get(i)
		OSMAttributedEntity ro = new OSMAttributedEntity(e.id, e.fields);
		ro.setValue("id", e.id);
		ro.setValue("rid", related.relatedId);
		ro.setValue('role', related.relation);
		ro.setValue('type', related.type);

		r.add(ro)
	}

	return r;
}


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : "c:\\temp\\trels.gdb") {

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
		filter { e ->
			(e instanceof OSMRelation) && e.getFields() != null
		}

		transform { e -> createRelations(e) }
	}


	// flux de sortie

	out(streams : rels, gdb : sortie, tablename:"rels")

}

