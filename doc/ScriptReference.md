
#OSMImport - Scripts DSL reference


##Groovy

Groovy is used to define the script, this permit to inline programmatic transformations and benefit from a concise and compile phase to check the correctness of the definition. 

Third java party can also directly be used from the script, this permit also to use external libraries to integrate custom transformations or operations. In order to use a java library, put it in the classpath, and use it in the script.

When the script is executed , a bunch of properties, functions and objects are predefined :

_predefined objects table :_

<table>
<tr>
<th>
	name
</th>
<th>
	kind of object
</th>
<th>
	Description / use
</th>

</tr>


<tr>
<td><b>osmstream</b></td><td>object</td>
<td>this object is the main osmstream read from the file (further details will come next).
</td>
</tr>


<tr>
<td><b>builder</b></td><td>object</td>
<td>this object is the builder for the transformation graph, it has only one method : build(<i>rootstream</i>). This method take in parameter, the main osm entity stream and call the definition of the graph creation using the following braces.<br/>

<pre>

builder.build(osmstream) {

	// define output stream
	sortie = gdb(path : var_gdb) {
		// transformation graph directives
	}

</pre>

</td>
</tr>


</table>



##gdb / csv directive

Thoses 2 directives define the structure of the output (in a geodatabase or in a folder containing CSV files) [CSV output format](CSVOutputFormat.md), **gdb or csv** is defined with a "**path**" attribute locating the destination of the result.

	csv(path:"..." ) { // create a directory containing the output csv files
		....
	}

	or  gdb(path:"...") { // create a file geodatabase containing the output tables and featureclasses

	}



Inside thoses directive, you can place multiple **featureclass** or **table** elements defining the structures to create. 

The **featureclass** directive take 3 parameters :

  - the name of the featureclass (String)
  - the geometry type : ( ESRI_GEOMETRY_POINT, ESRI_GEOMETRY_POLYLINE, ESRI_GEOMETRY_POLYGON)
  - The coordinate system to use (for the moment : **only WGS84**)

**table** directive only take 1 parameter : the name of the table

inside **featureclass** or **table** directive, other directives are placed for defining the fields :

		gdb(path : var_outputgdb) { 
			featureclass("pts", ESRI_GEOMETRY_POINT,"WGS84") {
				_long('id')
			}
			featureclass("lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
				_long('id')
			}
			featureclass("polygon", ESRI_GEOMETRY_POLYGON,"WGS84") {
				_long('id')
			}
			table("rels") {
				_long('id')
				_long('rid')
				_text('role', size:20)
				_text('type', size:30)
			}	
		}


- **\_text** : define a text field , the first parameter is the field name. Additional named parameters can be defined for :


<table>
			<tr>
			<th>
				Associated Attributes
</th>
<th>Description</th>
</tr>

<tr>
<td>size</td><td>specify the size of the field (by default, 255)</td>			

</tr>	
</table>
       

- **\_integer** : define an integer field.



<table>
			<tr>
			<th>
				Associated Attributes
</th>
<th>Description</th>
</tr>

<tr>
<td><i>none</i></td><td></td>			

</tr>	
</table>

- **\_long** : define long field.



<table>
			<tr>
			<th>
				Associated Attributes
</th>
<th>Description</th>
</tr>

<tr>
<td><i>none</i></td><td></td>			

</tr>	
</table>



- **\_double** : define an double floating point field.


<table>
			<tr>
			<th>
				Associated Attributes
</th>
<th>Description</th>
</tr>

<tr>
<td><i>none</i></td><td></td>			

</tr>	
</table>


Use example :


	// this example below define an output stream, and the output tables and featureclasses
	
	//
	// Create a file geodatabase with 3 featureclasses, using the WGS84 output format
	//
	//   pts : having an id field of integer.
	//   line, polygon : also having an integer id field
	//
	//    a table : "rels", containing 4 fields (id, rid, role, type)
	output = gdb(path : var_outputgdb) { //
		featureclass("pts", ESRI_GEOMETRY_POINT,"WGS84") {
			_long('id')
		}
		featureclass("lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_long('id')
		}
		featureclass("polygon", ESRI_GEOMETRY_POLYGON,"WGS84") {
			_long('id')
		}
		table("rels") {
			_long('id')
			_long('rid')
			_text('role', size:20)
			_text('type', size:30)
		}	
	}



	// this example below define a CSV folder (same definition as above)
	output = csv(path : var_outputcsvfolder) {
		featureclass("pts", ESRI_GEOMETRY_POINT,"WGS84") {
			_long('id')
		}
		featureclass("lines", ESRI_GEOMETRY_POLYLINE,"WGS84") {
			_long('id')
		}
		featureclass("polygon", ESRI_GEOMETRY_POLYGON,"WGS84") {
			_long('id')
		}
		table("rels") {
			_long('id')
			_long('rid')
			_text('role', size:20)
			_text('type', size:30)
		}	
	}



