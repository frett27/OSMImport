
#ChangeLog :

- 25/04/2016 : add support for double, single fields, reading for CSV files in directory
- 17/01/2016 : documentation enhancements, full transforming DSL documentation, in the reference documentation file. adding polygon creation failing reports.
- 10/10/2015 : add export command to export featureclasses and tables into geojson geometry format for csv export
- 27/09/2015 : add multiple actions on command line
- 06/09/2015 : version 0.6, add csv output, using base64 shape encoded geometries
- 16/08/2015 : version 0.5,add XML/OSM file support, bugs fixes
- 28/01/2015 : Polygon support, packaged in a simple command line, ajustment with JRE, and RAM consumpsion


Version 0.7.12
--------------

Add support for several other primitive types (Short, Float), add reading of CSV files located in a directory, with rels, points, ways and polygons as input. This permit to use the flink tools to generate the constructed OSM elements and process them using this commandline.

OSM Flink tools can now be chained with osmimport.


Version 0.7.8
-------------

Adding the reporting about failure in polygon creation, on import command line. This permit to log all the errors associated to polygon creation.

add -l option for import , permit to log in separate files all failures in polygon construct.

add -p option for limiting the polyline, polygon creation (save time in polygons, or lignes are useless)

Version 0.7.5
-------------

- add new command : export , this permit to export in geojson fgdb tables, for small datasets, it's interesting to have a geojson, for displaying in a browser.

- new commands for simplifing the script writing, for example old stream definition :

<pre>
	b = stream(osmstream, label:"streets") {

		filter {
			 e -> e instanceof OSMEntity &&
			   e.geometryType == Geometry.Type.Polyline  && e.getFields() && e.getFields().containsKey("highway")
		}
		
		transform {  e ->
			String t = e.getFields().get("highway")
			String name = e.getFields()?.get("name")
			e.getFields()?.clear()
			e.setValue("id",e.id)
			e.setValue("name", name)
			e.setValue("type", t)
			return e;
		}

	}

</pre>

can be written with the new DSL as :

<pre>

	l = stream(osmstream, label:"streets") {
		filter {
			 e -> isPolyline(e) && has("highway") // ust be a polyline and has a highway key
		}
		transform { OSMEntity e ->
			on(e).newValue("id", e.id). // define a new attribute, from an expression
			keep("highway"). // keep the osm field highway, with the same name in the output
			end() // generate the entity and return it
		}
	}


</pre>



Version 0.7
-----------

- refactor on cli invoke, now the first parameter on the command line is the action, currently , import and copycsv

breaking change :

  now the launch of the import is done with the following command line "osmimport **import** -s ... " the import keyword has been added to permit to have other additionals commands.

before :

	osmimport  -s ... -i .... -v ....
 
now :

	osmimport import -s ... -i .... -v ....

options are unchanged

Adding in this version of a copy command, to permit to convert a geometric CSV file to a file geodatabase, this permit to load fgdb from csv files.


Version 0.6 (2015 september):
------------
- add output in csv for table/feature class, permit to use this tool with big data stacks

incompatible changes :
  
  as the output is not only a GDB, out "gdb" property is renamed "sink" 

before :

	out(streams : [b], gdb : sortie, tablename:"streets")

since 0.6 version:

	out(streams : [b], sink : sortie, tablename:"streets")

see [CSV output format](CSVOutputFormat.md) for details about CSV export.

this rename permit to use other output format , ie : csv. And is necessary for future use.


Version 0.5 (2015 june):
------------

- add XML support for input file (not as performant as PBF, for the moment, simple implementation for testing scripts with small datasets)

breaking change : 

- the com.poc.osm package has been renamed to com.osmimport


Version 0.4 (2014 december):
------------

- Table support, relations support.

- Transform can now accept List of entities in result, permitting to fire more than One entity. This permit to create new entities from relations or integrate Entities in more that One FeatureClass.

- Relations can now be integrated in Tables, using a transform that lineage the OSMRelation with the related entities.

- When a polygon relation contains related ways with outer and inner roles, this will try to create an associated polygon.

- Add _long type for fields, this is equivalent to 

	usage : `_long("fieldname")`

	`_integer("fieldname", size : 8)`
