
OSMImport
=========

_Patrice Freydiere - 2015 - 2016 - 2017_

---


![](https://travis-ci.org/frett27/OSMImport.svg?branch=master)


Yet An Other OSM Import Tools for OSM data - [Change Log](ChangeLog.md)

This project aim to provide a simple **command line**  for ingesting OSM Data in a GIS.

The output result are tables or featureclasses inside one or multiple output FileGeodatabase (ready to use in GIS software). CSV output files are also supported since 0.6 version, allowing to have a smooth BigData shift.

This tools is currently used on Windows (x64) and Linux (x64). 

For performances or filtering capability, see [architecture](doc/architecture/architecture.md) and [doc](doc) folder

# Features

- **PBF / XML / OSM / AVRO / CSV Big Data Folder Input** input file support
- **FileGeodatabase Output**, that can be natively read by ArcGIS Desktop or QGIS
- **Create CSV text files in a folder** for Hadoop/Big stacks
- **Simple declarative groovy transformation script** 
- **Way reconstruction**, **Polygon reconstruction** for PBF / XML input file
- **Scripts are Easy to share and maintain** (a single human readable .script file), see [scripts](scripts) folder
- You can use any third party using **Groovy** or **Java ** language in scripts, for filtering and transformations.
- **Multiple destinations output on one run** (ability to merge import scripts)
- Automatic **RAM memory management** (manage the RAM consumption)




**Windows** / **Linux** friendly 



# Usage context - known usage

- A minimum of 5go of RAM is necessary for a first load
- Nota : RAM is used for processing complete ways and polygons, if RAM is not available, subsequent input file read are going to be done and will lead to decrease performances.


- A typical 32 Gb or RAM permit to handle France territory in a very nice timeframe. (please give us your benchmarks feedbacks and configurations). Special tuning could be done for an optimized process. (message regularisation limites, depending on the available memory).

- The tool currenlty **only support WGS84 coordinate system** as the osm datas use this coordinate system and it is quite easy to reproject thoses in an other coordinate system afterward.

- for huge integration (more than 1gb of PBF), or less that 2h country integration, consider using the `osm-flink-tools` to preprocess the input file, this is MUCH FASTER. This is done for avro prepared files

  ​


# Benchmarks

test it :-), we got lots of fast feedback for planet extract / transforms.



# PBF / OSM - Data Files

Openstreet france provide OSM extracts at this location : http://download.openstreetmap.fr/extracts/



# AVRO - Data Files

Prepared avro data files are available at this url : https://s3-eu-west-1.amazonaws.com/avroosm/index.html



# Download and install - Next actions

Take a tour at [5 mins setup guide](doc/QuickStart.md) to launch your first command line.

[Existing Scripts folder](scripts) contains a bunch of existing scripts for already configured OSM transformation and filtering

If you want to go further and customize the scripts, see [How to write Scripts](doc/WritingAScript.md). It explain how to customize or configure your scripts for your usage.

other documentations can be found in the [doc](doc) folder

don't hesitate to send pull requests or comments.

# Sample scripts :

- [Buildings](scripts/buildings.groovy) - extract buildings
- [Streets](scripts/streets.groovy) - extract streets


# Contributions

Contributions are welcome on :

- documentation
- scripts
- code
- enhancements
- ...


# Version 1.0 roadmap plan

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


