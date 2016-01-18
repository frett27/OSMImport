import com.esri.core.geometry.Geometry
import com.esri.core.geometry.MultiPath
import com.esri.core.geometry.Polygon
import com.osmimport.model.OSMEntity
import com.osmimport.model.OSMEntityGeometry


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
			 e -> 
			 isPolygon(e) && has(e,"building") 
		}
		
		transform {  e ->
			on(e).newValue("id",e.id).map("building").into("type").end()
			String t = e.getFields().get("building")
			return e;
		}

	}

	l = stream(osmstream, label:"building lines") {
		
		filter {
			 e -> isPolyline(e) && has(e,"building")
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
	out(streams : [b,l], sink : sortie, tablename:"buildings")
	// out(streams : l, sink : sortie, tablename:"buildings_line")
	
}

