
OSMImport
=========

_Patrice Freydiere - 2015 - 2016_ - 2017

---


![](https://travis-ci.org/frett27/OSMImport.svg?branch=master)


Yet An Other OSM Import / Formatting OSM data tool - [Change Log](ChangeLog.md)

This project aim to provide a simple **command line for filtering / GIS structuring OSM PBF or OSM XML files**. The result are tables or featureclasses inside one or multiple output FileGeodatabase (ready to use in GIS software). CSV output files are also supported since 0.6 version, allowing to have a smooth BigData shift for OSM data 

This project use an internal actors system architecture, using big data principles (streams, no sequencial read). This tool benefit directly from a Muticore machine. for more information about architecture see [architecture](doc/architecture/architecture.md)



#Objectives

- Simple to use, performant
- Contributing or extracting information thanks to shared scripts

#Current Features

- **Windows** / **Linux** 

- **PBF / XML / OSM / CSV Big Data Folder Input** input file support

- **FileGeodatabase Output**, that can be natively read by ArcGIS Desktop or QGIS

- **CSV text files output in a folder** for Hadoop/Big stacks, can be pushed to hdfs store

- **Simple declarative groovy transformation script** 
-  It use a **Groovy** DSL, opening the field to java third party libraries, used for filtering and transformations.
-  If multiple class or objects must be extracted in different files, this can be done in one script.
-  **Scripts are humany readable **

- **Inprocess Way and Polygon construction**, to ease the use in a GIS.

- **Performances** : extracting some objets in files can typically be done in half the usual time. 

  ​


#Usage context - known usage

- A minimum of 5go of RAM is necessary for a first simple load 


- The tool currenlty **only support WGS84 coordinate system** as the osm datas use this coordinate system and it is quite easy to reproject thoses in an other coordinate system afterward.

- for huge integration (more than 1gb of PBF),  consider using the `osm-flink-tools` to preprocess the input file, this is MUCH FASTER.

  - [See Large integration Article]()

    ​


#Benchmarks

for simple scenarios (light write pressure):

- **Thanks to LMAX, more than 1 000 000 final entities can be processed in a minute** for simple transformations on a standard laptop. See benchmark article for metrics.

- As a REX, on heaviest writing pressure and full stack, using ramimport.groovy script and exporting in gdb. The admin level 1 export, take about 40 mins, with the same hardware as above, with preprocessing.

- as FileGeodatabase Java API need synchronization per featureclass, writes are synchronized for this output format. There is a performance tradoff. This is not the case for CSV output



# One line startup

```
java -Xmx6g -jar [pathto]/osmimport.jar import -i ./rhone-alpes-latest.osm.pbf -s ./scripts/buildings.groovy
```

Some extract/transform **scripts** are available here : [scripts](scripts)



#Tutorial / Informations

Take a tour at [5 mins setup guide](doc/QuickStart.md) to launch your first command line.

[How to write Scripts](doc/ScriptReference.md) explain how to create, customize or configure your scripts.

[Scripts folder](scripts) Scripts samples

other documentations can be found in the [doc](doc) folder



# PBF / OSM - Data Files

OpenStreetMap France offert OSM extracts mirror at this location : http://download.openstreetmap.fr/extracts/



#Ready to use samples scripts :

- [Buildings](scripts/buildings.groovy) - extract buildings
- [Streets](scripts/streets.groovy) - extract streets




# How To Build the project

You are a developer, you want to compile and use if from source :

Java 7 or 8 is mandatory

```
git clone https://github.com/frett27/osmimport.git
cd osmimport
./gradlew fatJar
```

use or contribute.

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

