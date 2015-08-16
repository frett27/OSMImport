#OSM Import documentation

## Introduction

This documentation explain how to use the OSMImport project to create tables / featureclass from OSM PBF/XML files.

## 5 Mins Startup

download the install bundle in a directory.

Place the OSM (PBF/XML) datafile and the script file, in a folder and run :


	java -Xmx6g -ea -server -jar osmtoolsreader-all-0.3.jar -i ./rhone-alpes-latest.osm.pbf -s ./scripts/buildings.groovy

the output files are placed in the location defined in the stream file (the -s option).


##Command line arguments

	
	usage: osmimport
	 -i,--inputpbf <inputpbf>   input PBF OSM file
	 -s,--streams <streams>     script file describing the streams
	 -v <var>                   variable definition



##Example of a script , that take only the geometries of points and lines that have keys and values.


	import com.esri.core.geometry.Geometry;
	import com.poc.osm.model.OSMEntity;
	
	
	// construct the processing graph
	builder.build(osmstream) {
	
		// défini un flux de sortie, et description de la structure
		sortie = gdb(path : "c:\\temp\\t.gdb") {
			featureclass("pts", ESRI_GEOMETRY_POINT,"WGS84") {
				_integer('id')
			}
			featureclass("lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
				_integer('id')
			}
		}
	
	
		// a stream
		t = stream(osmstream, label:"Points with informations") {
	
			filter {
				OSMEntity e ->
				   e.geometryType == Geometry.Type.Point && e.getFields() != null
			}
			
			transform { OSMEntity e ->
				e.getFields()?.clear();
				e.setValue("id",e.id);
				return e;
			}
	
		}

		// a stream
		l = stream(osmstream, label:"only lines and fields") {
	
			filter {
				OSMEntity e ->
				   e.geometryType == Geometry.Type.Polyline && e.getFields() != null
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
	
	
	}
	

This file is a groovy file, with additional keywords to define the integration of the entities. All Third party library could be used for processing the result.
