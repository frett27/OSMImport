OSMImport
=========

Yet An Other OSM Import / Formatting OSM data tool

This project aim to provide a command line for filtering / structuring OSM PBF or XML files directly in tables or featureclasses inside a FileGeodatabases (read to use). 

Since October 2014, lots of feedbacks have been implemented in the lastest version. This project **doesn't need a PostGIS database**, **you don't need extensive software stack**. This tools works on Windows directly. Except Java, **No additional software needed**. 

This project use internal actors system and big data principles (streams, no sequencial read) to do the job, proposing a disruptive performance experience. This tool benefit directly from a Muticore machine, 

#Features

- Windows / Linux friendly (tested, but in practice should be run on all java compatible desktop / server machines)
- PBF / XML / OSM input file support
- FileGeodatabase Output, that can be natively read by ArcGIS or QGIS
- Point / Line / Polygon / Relation entities handling
	- Way reconstruction, Polygon reconstruction, to have a proper geometry, handled by the ESRI geometry library
- Automatic RAM memory management (for stream processing efficiency)
- Simple declarative groovy script for transformations and filtering
	- All Groovy and Java third party are directly usable in the script, for filtering and transformations.
	- Possibility to merge a set of script to have different outputed database from a single run.
	- Easy to share (a single human readable .script file)


#Usage context

- At minimum of 5go of RAM is necessary for a first load
	- Nota : RAM is used for processing complete ways and polygons, if RAM is not available, subsequent input file read are going to be done and will lead to decrease performances.

A typical 32 Gb or RAM permit to handle France territory in a nice timeframe. (let us know your benchmarks)

- The tool only support WGS84 coordinate system for the moment as the osm datas use this coordinate system and it is quite easy to reproject thoses in an other coordinate system afterward.


#Benchmarks

- **More than 1 000 000 final entities processed per minute** on a standard laptop (1.7 Ghz INTEL i5, 2 physical cores, 8Gb RAM)), processing a french region takes about 10mins with 8Gb of RAM. Processing France with a 32gb of RAM takes about 1,5 Hour.


#Version 1.0 roadmap plan

- <strike>handling relationships</strike>
- <strike>handling polygons</strike>
- <strike>tables support</strike>
- Work on simplify the writing of scripts
- error logs, and feedbacks
- clean logs
- extend fields type to Date (supported fields: integer, string, long, floats)

