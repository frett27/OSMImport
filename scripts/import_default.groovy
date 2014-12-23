import com.esri.core.geometry.Geometry;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.output.Filter;
import com.poc.osm.output.Transform;

import groovy.util.XmlSlurper;


class FilterKeyExist extends Filter {
	
	 Geometry.Type geomType;
	 String name;
	
	boolean filter(OSMEntity e){
		return ((e.geometryType == geomType) && e.fields != null && e.fields[name] != null) as boolean
	}
	
}

// construction de la chaine

builder.build(osmstream) {
	
	def featureClasses = [:]

 	def x = new XmlSlurper().parse(
		 new File("C:\\projets\\OsmSemanticNetwork\\osm_semantic_network.skos.rdf"))
	     .declareNamespace(osnp:"http://spatial.ucd.ie/lod/osn/property/", 
			 rdf: "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
 
		 x.'rdf:Description'.each { n ->
			 if (n."@rdf:about".text() ==~ /.*\/k:[A-Za-z0-9:_]+/)
			 {
				 url = n."@rdf:about".text()
				 k = (url =~ /.*\/k:(.*)$/)[0][1] 
				 // println k
				 
				 kv = "k:" + k;
				 values = n."osnp:link".findAll { it."@rdf:resource".text().indexOf(kv+"/v:") != -1 }.collect{ (it."@rdf:resource".text() =~ /.*\/v:(.*)/)[0][1]}
				 
				 if (values) {
					 featureClasses[k] = values
				 }
			 }
			 
		 }
		 
		 
	println featureClasses;

	

	wished = ['landuse','highway','historic','building']
	// wished = ['highway']
	
	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : "c:\\temp\\t.gdb") {
		
		for (l in wished) {
			featureclass(l + "_pts", ESRI_GEOMETRY_POINT,"WGS84") {
				/* _text("k", size : 40)
				_integer("mon champ entier")
				_double("autre champ") */
				_integer('id')
				_text("type", size:2000)
			}
			featureclass(l + "_lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
				/* _text("k", size : 40)
				_integer("mon champ entier")
				_double("autre champ") */
				_integer('id')
				_text("type", size:2000)
			}
			
		}

	
	}

	// dummy filter
	// f = filter { e -> return true }

	pts = [:]
	lines = [:]
	
	
	def lastStream = osmstream;
	
	
	
	for (l in wished) {
	
		// a stream
		pts[l] = builder.stream(lastStream) {
			
	
			filter(new FilterKeyExist(geomType:Geometry.Type.Point,name:l))
			
			transform { OSMEntity e ->
				String t = e.fields[k];
				e.getFields()?.clear();
				e.setValue("id",e.id);
				e.setValue("type",t);
				return e;
			} 
	
		}
		
		lastStream = pts[l].other
		
		// a stream
		lines[l] = builder.stream(pts[l].other) {
			
			def k = "" + l;
	
			filter(new FilterKeyExist(geomType:Geometry.Type.Polyline,name:k))
			
			transform  { OSMEntity e ->
				String t = e.fields[k];
				e.getFields()?.clear();
				e.setValue("id",e.id);
				e.setValue("type",t);
				return e;
			} 
	
		}
		
		
	}

	

	// flux de sortie
	
	for (l in wished) {
		out(streams : pts[l], gdb : sortie, tablename:l+"_pts")
		out(streams : lines[l], gdb : sortie, tablename:l+"_lines")
	}
	
}


