#OSM Import documentation

## Introduction

This documentation explain how to use the OSMImport project to create tables / featureclass from OSM PBF/XML files.

## 5 Mins Startup

download the install bundle and put it in a directory of your choice.


###Version _0.5-SNAPSHOT_  

**Windows 64 bits** - 
[osmimport-0.5-SNAPSHOT.zip](https://s3-eu-west-1.amazonaws.com/osmimport-cli/osmimport-0.5-SNAPSHOT.zip) - Nota: if you use the FGDB output file format, you will need to install redistribuable VC++ 2012 from microsoft, if not already installed.

**Linux 64 bits** - @@TBD


##Download PBF or OSM located datas

if you dont have PBF or OSM file, you can download them at : [http://www.geofabrik.de/data/download.html](http://www.geofabrik.de/data/download.html) . geofabrik provide daily extraction of OSM datas in PBF or OSM (xml) files.

##Choose an already existed script

Existing scripts are located in the **[scripts](../scripts)** folder

* [streets.groovy](../scripts/streets.groovy) - Sample file to extract streets
* [buildings.groovy](../scripts/buildings.groovy) - Sample file to extract buildings inFile geodatabase



##Run the commandline

Place the OSM (PBF/XML) datafile and the script file, in a folder and run :

	osmimport import -i ./rhone-alpes-latest.osm.pbf -s ./scripts/buildings.groovy

the output files are placed in the location defined in the stream file (the -s option).


##Command line parameters

	>osmimport
	OSM Import
	   Available Commands :
	    help : get help on commands
	    import : import osm file with a transformation script
	    copycsv : copy a csv file into a file geodatabase	


	>osmimport import
	
	OSM Import
	23:05:31.514 [main] DEBUG com.osmimport.MCLI - launching command :import
	 error in arguments :Missing required options: i, s
	usage: osmimport
	 -i,--input <input>       [REQUIRED] input PBF or OSM file, this can be
	                          .pbf or .osm files
	 -s,--streams <streams>   [REQUIRED] script file describing the filtering
	                          and transformations, (.groovy files)
	 -v <var>                 [OPTIONAL] additional variables definition that
	                          are mapped into var[name] in the script



some scripts may have variable defined (for output gdb path, or other refinements), look at the script for mandatory variables.

this article ([UsingVariable.md](UsingVariable.md)) explain in details how to use variable from command line.



##Sample script , takes points and lines geometries with keys and values attached.


	import com.esri.core.geometry.Geometry;
	import com.osmimport.model.OSMEntity;
	
	
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
		out(streams : t, sink: sortie, tablename:"pts")
		out(streams : l, sink: sortie, tablename:"lines")
	
	
	}
	

This file is a groovy file, with additional keywords to define the integration of the entities. All Third party library could be used for processing the result.
