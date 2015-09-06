#Using Variables in scripts


for creating generic script, it often need to parametrize entries from command line and pass it to the script

rational :

On the command line the '-v' option permit to define some variables that will be defined in the scrippt prefixed by "var_" to avoid naming collision :

    osmimport -i [.pbf or .osm file] -s [script path] -v "variable1=content" -v "variable2=othervalue" -v .... -v ...

multiple variables can be passed in repeating the -v option. All variable definition must be enclosed by quotes and defined using a "=" sign.


usage example :

    C:\projets\OSMImport\build\distributions\osmimport-0.5-SNAPSHOT>osmimport \ 
			-i "C:\projets\OSMImport\rhone-alpes-latest.osm.pbf" \
			-s "C:\projets\OSMImport\scripts\rawimport.groovy" \
			-v "toto=titi" \
			-v "tutu=vuvu"

in the example, we define 2 variables in the script : var_toto and var_tutu

the defined variables in scripts will be strings, containing the left part of the "=" sign, if other types are needed in the script, the script can convert the type using Double.parseDouble or Integer.parseInteger functions.
