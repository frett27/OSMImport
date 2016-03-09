#OSM Import documentation

## Introduction

This documentation explain how to use the OSMImport project to create tables / featureclass from OSM PBF/XML files.

## 5 Mins Startup

download the install bundle and put it in a directory of your choice.


###Version _0.7.10-SNAPSHOT_  

**Windows 64 bits** - 
[osmimport-0.7.10-SNAPSHOT.zip](https://s3-eu-west-1.amazonaws.com/osmimport-cli/osmimport-0.7.10-SNAPSHOT.zip) - Nota: if you use the FGDB output file format, you will need to install redistribuable VC++ 2012 from microsoft, if not already installed.

**Linux 64 bits** - ** Coming **



###From the Git Repo

> you must have a JDK java 7 (or up) installed, for compiling

	git clone https://github.com/frett27/osmimport.git

	gradlew fatJar

> the resulting standalone jar will be located in `build/libs`


##Download PBF or OSM located datas

if you dont have PBF or OSM file, you can download them at : [http://www.geofabrik.de/data/download.html](http://www.geofabrik.de/data/download.html) . geofabrik provide daily extraction of OSM datas in PBF or OSM (xml) files.

##Choose an already existed script

Existing scripts are located in the **[scripts](../scripts)** folder

* [streets.groovy](../scripts/streets.groovy) - Sample file to extract streets
* [buildings.groovy](../scripts/buildings.groovy) - Sample file to extract buildings in FileGeodatabase



##Run the commandline

Place the OSM (PBF/XML) datafile and the script file, in a folder and run :

	java -Xmx6g -jar [pathto]/osmtoolsreader-all-0.7.10-SNAPSHOT.jar import -i ./rhone-alpes-latest.osm.pbf -s ./scripts/buildings.groovy

the output files are placed in the location defined in the stream file (the -s option).


##Command line parameters

	C:\projets\OSMImport>java -Xmx6g -jar build\libs\osmtoolsreader-all-0.7.10-SNAPSHOT.jar help import

		OSM Import
		usage: osmimport  import
		 -e <eventbuffer>         Buffer of events to maintain
		 -i,--input <input>       [REQUIRED] input PBF or OSM file, this can be
		                          .pbf or .osm files
		 -l <logfolder>           activate the entity log report, and specify the
		                          folder in which the entities report are created
		 -m <maxways>             Number of ways to handle by pass for each worker
		 -p <parsinglevel>        level of parsing, 0 -> only points, 1 -> points
		                          and lines, 2 -> points, lines and polygons. By
		                          defaut, the level 2 is taken
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
