OSMImport
=========

Tools for importing OSM files in FileGeodatabases, **this is a playground** for understanding actor system, and big data principles. The idea is to provide a simple command line to integrate OSM data in the final wished model, in a efficient manner.

In this experiment, i tried to use actor system to distribute the process among large number of machines, using RAM to make joins. No random access on the datas, only streams, that lead to disruptive performances on OSM data parsing and transforms. Performances are quite impressive, with a 50000 inserted rows / s, and more than 1 000 000 entities processed per minute on a laptop (1.7 Ghz INTEL i5, 2 physical cores, 8Gb RAM), processing France with a couple of entities in about 1 + 1/2 hours).

Using the actor systems, also permit to use a simple "command line" process to do the job.

NOTA : still an ongoing project, dev skills highly required

# TODO

- <strike>handling relationships</strike>
- <strike>handling polygons</strike>
- <strike>tables support</strike>
- error logs, and feedbacks
- clean logs
- handling all kind of fields (only integer, string, long)

