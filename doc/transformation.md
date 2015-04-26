#OSM Import documentation

## Introduction

This documentation explain how to use the OSMImport project to import OSM PBF files in a custom modeled FileGeodatabase.

## 5 Mins Startup

download the install bundle in a directory.
Place the transformation file (aka Stream file), in a directory of your choice and run :


	java -Xmx6g -ea -server -jar osmtoolsreader-all-0.3.jar -i ./rhone-alpes-latest.osm.pbf -s ./scripts/buildings.groovy

the output files are placed in the location defined in the stream file (the -s option).


##Command line arguments

	
	usage: osmimport
	 -i,--inputpbf <inputpbf>   input PBF OSM file
	 -s,--streams <streams>     script file describing the streams
	 -v <var>                   variable



#Constructing a stream file

The stream file define the process to do with the OSM datas. It contains :

- the definition of the destination model structure
- the process for transformation on the OSM entities


**First sample stream File**

	import com.esri.core.geometry.Geometry;
	import com.poc.osm.model.OSMEntity;
	
	
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
		}
	
		// dummy filter
		// f = filter { e -> return true }
	
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
		l = stream(osmstream, label:"modification 2") {
	
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


All directives are placed in a builder.build section, 
	
	builder.build(osmstream) {
	
		// declarative part are placed here
	
	}

the output model is defined using gdb directive, there can be multiple gdb output if it is necessary to generate multiple file geodatabases.

		// 
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
		}

in this example, the definition of the structure of the gdb is put in the variable "**sortie**". this variable is then used in the bottom of the file to define the mapping of the streams to the tables and featureclasses.

###gdb directive

This directive define the structure of the geodatabase, **gdb** is defined with a "**path**" attribute locating the destination of the result.

Inside this directive, you can place multiple **featureclass** or **table** elements defining the featureclasses to create in the file geodatabase. 

The **featureclass** directive take 3 parameters :

  - the name of the featureclass (String)
  - the geometry type : ( ESRI_GEOMETRY_POINT, ESRI_GEOMETRY_POLYLINE, ESRI_GEOMETRY_POLYGON)
  - The coordinate system to use (for the moment : **only WGS84**)

**Table** directive only take 1 parameter : the name of the table

inside **featureclass** or **table** directive, other directives are placed for defining the fields :

   - **\_text** : define a text field , the first parameter is the field name. Additional named parameters can be defined for :
	   - size : specify the size of the field (by default, 255)

   - **\_integer** : define an integer field.
   - **\_double** : define an double floating point field.



###stream directive

the stream directive specify a stream of entities, streams are caracterized by 2 elements :

- the filter (to adjust which entity will be processed)
- the transform (to modify or adjust the entities before raising them again).

in the following example, the stream define a filter and an associate transform.

		l = stream(osmstream, label:"modification 2") {
	
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


**the first parameter** of the stream is the source stream (the root is defined in the main section ie:

	builder.build(osmstream) {
	
		// declarative part are placed here
	
	}

osmstream is a builtin stream providing the entities from the osm file.

an other attribute "**label**" permit to have comprehensive logs for the transforms.

the filter directive specify a function returning a boolean, that define of the entity can be processed be the stream.

the transform directive define a function that take an OSMEntity and return a possibly modified OSMEntity.

OSM entities passed have the following properties :

![](OSMEntity.PNG)


- **id** : the id of the OSM entity
- **geometry** : the constructed geometry, belonging to the ESRI geometry API
- **fields** : a hash containing the fields values



###out directive

the last part of the stream file, is the "**out**" directive. This directive permit to define the mapping between the "fgdb model" and streams.

		// flux de sortie
		out(streams : t, gdb : sortie, tablename:"pts")
		out(streams : l, gdb : sortie, tablename:"lines")
	
in the following example, the variable "t" containing a stream will be sinked to table/featureclass "pts" of the "sortie" gdb.

when outputing fields in the file geodatabase, values are automatically converted in the output format (if possible), otherwise, the value is rejected and a log of the error is available.

 