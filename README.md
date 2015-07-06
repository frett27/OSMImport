OSMImport
=========

Yet An Other OSM Import.

This project aim to provide a Tools for importing OSM PBF files directly in FileGeodatabases. This project is currently **a playground** for understanding actor system, and big data principles. The idea is to provide a simple command line to integrate OSM data in the final wished model, in a efficient manner.

In this experiment, actor system are used to distribute the process among large number of machines and cores, using RAM to make joins as streams (hooked relation construction). No random access on the datas, only streams. This technique lead to disruptive performances on OSM data parsing and transforms. Performances are in a first manner quite impressive, with a 50000 inserted rows / s, and more than 1 000 000 entities processed per minute on a laptop (1.7 Ghz INTEL i5, 2 physical cores, 8Gb RAM), processing France with a couple of entities in about 1 + 1/2 hours).

A lot of work is also to be done to have a proper enduser usage. We currently work on France scope, and probably planet level in the next weeks.

NOTA : still an ongoing project, dev skills highly required, please contact us for suggestions.

# TODO

- <strike>handling relationships</strike>
- <strike>handling polygons</strike>
- <strike>tables support</strike>
- Work on simplify the writing of scripts
- error logs, and feedbacks
- clean logs
- extend kind of fields (only integer, string, long)

