
#Script DSL reference


##Groovy

We use groovy to define the script, this permit to inline transformations and benefit from a concise and compile phase to check the correctness of the definition. As Groovy is used, this permit also to use external libraries to integrate custom transformations or operations. 

The groovy script used to specify the transformations, this script is executed with a bunch of properties, functions and object predefined :

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

	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_gdb) {
		// transformation graph directives
	}

</pre>

</td>
</tr>


</table>



##gdb directive

This directive define the structure of the geodatabase, **gdb** is defined with a "**path**" attribute locating the destination of the result.


Inside this directive, you can place multiple **featureclass** or **table** elements defining the featureclasses to create in the file geodatabase. 

The **featureclass** directive take 3 parameters :

  - the name of the featureclass (String)
  - the geometry type : ( ESRI_GEOMETRY_POINT, ESRI_GEOMETRY_POLYLINE, ESRI_GEOMETRY_POLYGON)
  - The coordinate system to use (for the moment : **only WGS84**)

**table** directive only take 1 parameter : the name of the table

inside **featureclass** or **table** directive, other directives are placed for defining the fields :

   - **\_text** : define a text field , the first parameter is the field name. Additional named parameters can be defined for :
	   - size : specify the size of the field (by default, 255)

   - **\_integer** : define an integer field.
   - **\_long** : define long field.
   - **\_double** : define an double floating point field.


Use example :


	// défini un flux de sortie, et description de la structure
	sortie = gdb(path : var_outputgdb) {
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
			_text('role')
			_text('type')
		}	
	}


