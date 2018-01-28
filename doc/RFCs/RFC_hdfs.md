# RFC Hdfs


Problem we want to solve :

- speedup the availability of OSM Data into an HDFS cluster
- lean the Hadoop processing of OSM Datas

# Status

Partially implements

# Requirements

it seems that several formats are used in hadoop cluster, 
ESRI provide a JSON oriented Text File
OpenSource community use

personal experience drive me to use text delimited files, with a base 64 Shape encoded geometry.

CSV delimiter is really simple to use and can be used in non geographic framework or tools, geometry encoding is then available as string, and decoded when needed. the WKT is also used for geometry encoding make it human readable (nice for files exchange).

for points, X and Y storage save a lots of disk space and there are variety of datas that are referenced as point, this is an important design point.

# Target / Specs

1 - 
Choose the hdfs target / table / CSV file

encode the X/Y in double

encode polylines/polygon in shape base64 encoded

2 - open several formats for the output

