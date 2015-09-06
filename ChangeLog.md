
#ChangeLog :

- 06/09/2015 : version 0.6, add csv output, using base64 shape encoded geometries
- 16/08/2015 : version 0.5,add XML/OSM file support, bugs fixes
- 28/01/2015 : Polygon support, packaged in a simple command line, ajustment with JRE, and RAM consumpsion


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
