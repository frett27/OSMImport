import com.esri.core.geometry.Geometry
import com.esri.core.geometry.MultiPath
import com.esri.core.geometry.Polygon
import com.poc.osm.model.OSMEntity
import com.poc.osm.model.OSMEntityGeometry


// construction de la chaine
builder.build(osmstream) {

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_gdb) {
		featureclass("buildings", ESRI_GEOMETRY_POLYGON,"WGS84") {
			_integer('id')
			_text("type")
		}
	}

	// a stream
	b = stream(osmstream, label:"buildings") {

		filter {
			 e -> e instanceof OSMEntity &&
			   e.geometryType == Geometry.Type.Polygon  && e.getFields() && e.getFields().containsKey("building")
		}
		
		transform {  e ->
			String t = e.getFields().get("building")
			e.getFields()?.clear()
			e.setValue("id",e.id)
			e.setValue("type", t)
			return e;
		}

	}

	l = stream(osmstream, label:"building lines") {
		
		filter {
			 e -> e instanceof OSMEntity &&
			   e.geometryType == Geometry.Type.Polyline && e.getFields() && e.getFields().containsKey("building")
		}
		
		transform {
			 e ->
			
			Geometry g = e.getGeometry()
			if (g == null || g.isEmpty())
			{
				return null;
			}
			
			Polygon p = new Polygon()
			p.add((MultiPath)g,false)
			
			Map<String,String> newFields = new HashMap<String,String>()
			newFields.put("type",e.getFields().get("building") )
			newFields.put("id", e.getId())
			
			f = new OSMEntityGeometry(e.getId(), p ,newFields);
			
			
			return f;
		}
		
	}
	
	
	// flux de sortie
	out(streams : [b,l], gdb : sortie, tablename:"buildings")
	// out(streams : l, gdb : sortie, tablename:"buildings_line")
	
}

