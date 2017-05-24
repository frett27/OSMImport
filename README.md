OSMImport
=========

_Patrice Freydiere - 2015 - 2016_

---


![](https://travis-ci.org/frett27/OSMImport.svg?branch=master)


Yet An Other OSM Import / Formatting OSM data tool - [Change Log](ChangeLog.md)

This project aim to provide a simple **optimized command line for filtering / GIS structuring OSM PBF or OSM XML files**. The result are tables or featureclasses inside one or multiple output FileGeodatabase (ready to use in GIS software). CSV output files are also supported since 0.6 version, allowing to have a smooth BigData shift for OSM data

Since October 2014, lots of feedbacks have been implemented in the lastest version. This project **doesn't need PostGIS database**, **or extensive software stack**. This tools has been tested on Windows (x64) and Linux (x64). 

This project use dirsuptive technology, an internal actors system using big data principles (streams, no sequencial read), proposing a disruptive performance experience. This tool benefit directly from a Muticore machine. for more information about architecture see [architecture](doc/architecture/architecture.md)

#Features

- **Windows** / **Linux** friendly (tested, but in practice should be run on all java compatible desktop / server machines)
- **PBF / XML / OSM / CSV Big Data Folder Input** input file support
- **FileGeodatabase Output**, that can be natively read by ArcGIS Desktop or QGIS
- **Create CSV text files in a folder** for Hadoop/Big stacks
- **Simple declarative groovy transformation script** 
	- All **Groovy** and **Java third party are directly usable** in the script, for filtering and transformations.
	- Simplified merge on a set of script to have different outputed database from a single run.
	- **Scripts are Easy to share and maintain** (a single human readable .script file)
- **Multiple destinations output on one run** (ability to merge import scripts)
- **Way reconstruction**, **Polygon reconstruction**, to have a proper GIS geometry, handled by the ESRI geometry library
- Automatic **RAM memory management** (manage the RAM consumption)


#Usage context - known usage

- A minimum of 5go of RAM is necessary for a first load
	- Nota : RAM is used for processing complete ways and polygons, if RAM is not available, subsequent input file read are going to be done and will lead to decrease performances.


- A typical 32 Gb or RAM permit to handle France territory in a very nice timeframe. (please give us your benchmarks feedbacks and configurations). Special tuning could be done for an optimized process. (message regularisation limites, depending on the available memory).

- The tool currenlty **only support WGS84 coordinate system** as the osm datas use this coordinate system and it is quite easy to reproject thoses in an other coordinate system afterward.

- for huge integration (more than 1gb of PBF), or less that 2h country integration, consider using the `osm-flink-tools` to preprocess the input file, this is MUCH FASTER.


#Benchmarks

for simple scenarios (light write pressure):

- **More than 1 000 000 final entities processed per minute** for simple transformations on a standard laptop (1.7 Ghz INTEL i5, 2 physical cores, 8Gb RAM, 5400 rpm Hard Drive)), processing a french department takes about 10mins with 8Gb of RAM. Processing France with a 32gb of RAM takes about 1,5 Hour.

- As a REX, on heaviest writing pressure and full stack, using ramimport.groovy script and exporting in gdb. The admin level 1 export, take about 40 mins, with the same hardware as above.

- as FileGeodatabase need a write synchronization per featureclass, writes are synchronized for this output format. This is not the case for CSV output

#PBF / OSM - Data Files

Openstreet france propose OSM extracts at this location : http://download.openstreetmap.fr/extracts/

#Download and install - Next actions

Take a tour at [5 mins setup guide](doc/QuickStart.md) to launch your first command line.

[How to write Scripts](doc/WritingAScript.md) explain how to customize or configure your scripts for your usage.

[Existing Scripts folder](scripts) contains a bunch of existing scripts for already configured OSM transformation and filtering

other documentations can be found in the [doc](doc) folder

don't hesitate to send pull requests or return of experience.

#Ready to use samples scripts :

- [Buildings](scripts/buildings.groovy) - extract buildings
- [Streets](scripts/streets.groovy) - extract streets


#Contributions

Contributions are welcome on :

- documentation
- scripts
- code
- enhancements
- ...


#Version 1.0 roadmap plan

- <strike>handling relationships</strike>
- <strike>handling polygons</strike>
- <strike>tables support</strike>
- <strike>csv text files support</strike>
- <strike>Work on simplify the writing of scripts</strike>
- <strike>fix erroneous polygon reconstruction</strike>
- propose a sample script gallery for common operations
- error logs, and feedbacks
- clean logs
- extend fields type to Date (supported fields: integer, string, long, floats)

