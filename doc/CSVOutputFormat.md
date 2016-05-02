#CSV output format - Hadoop pivot

**Since 0.6**, it is possible to export osm datas into a folder containing csv files.
Each table and featureclass output will be generated in a proper file.

Every records are generated in a text file, using the current local charset encoding. With a `'\\n'` endline character.
**Geometries are encoded in base64**, containing a the **ShapeFile Format encoded** geometry description. Esri geometry java project on github is able to directly load the geometry in Java Object, as JTS, or other java geometry toolkits.

Strings are encoded with a leading and trailing `"` character


Example :


	// script generating streets lines in csv files
	builder.build(osmstream) {
	
		// défini un flux de sortie, et description de la structure
		sortie = csv(path : var_gdb) {
			featureclass("streets", ESRI_GEOMETRY_POLYLINE,"WGS84") {
				_integer('id')
				_text("type")
				_text("name")
				_text("oneway")
			}
		}
	
		// a stream
		b = stream(osmstream, label:"streets") {
	
			filter {
				 e -> e instanceof OSMEntity &&
				   e.geometryType == Geometry.Type.Polyline  && e.getFields() && e.getFields().containsKey("highway")
			}
			
			transform {  e ->
				String t = e.getFields().get("highway")
				String name = e.getFields()?.get("name")
				String oneway = e.getFields()?.get("oneway")
				e.getFields()?.clear()
				e.setValue("id",e.id)
				e.setValue("name", name)
				e.setValue("type", t)
				e.setValue("oneway",oneway)
				return e;
			}
	
		}
	
		
		// flux de sortie
		out(streams : [b], sink : sortie, tablename:"streets")
	
		
	}

This script generate a streets.csv file with the following extracted content.


Generated output :


	AwAAAHAwitRqFhdAgpWeRN+YRkBGxUmraxYXQBw6h3zhmEZAAQAAAAIAAAAAAAAAcDCK1GoWF0AcOod84ZhGQEbFSatrFhdAgpWeRN+YRkA=,64802844,"unclassified","Rue des Maths",
	AwAAAMyrhDgg8xZA7hY3JKSYRkArQTUULfcWQIx+2eK/mEZAAQAAAAMAAAAAAAAAzKuEOCDzFkCMftniv5hGQDsdyHpq9RZAlF7lGrCYRkArQTUULfcWQO4WNySkmEZA,8015988,"unclassified","Rue du Souvenir",
	AwAAAFUEk3b+CBdAYyGEkaiZRkA55/HFKAkXQBSScTGrmUZAAQAAAAIAAAAAAAAAVQSTdv4IF0AUknExq5lGQDnn8cUoCRdAYyGEkaiZRkA=,8016119,"primary",,"yes"
	AwAAANronJ/iIBdA14uhnGiYRkC9VGzM6yAXQDeyhuaVmEZAAQAAAAIAAAAAAAAA2uicn+IgF0DXi6GcaJhGQL1UbMzrIBdAN7KG5pWYRkA=,25612323,"trunk","Rocade Sud","yes"
	AwAAAOmXLf5b5hZAw52waUqURkCZXgdzYOYWQGuEfqZelEZAAQAAAAIAAAAAAAAAmV4Hc2DmFkDDnbBpSpRGQOmXLf5b5hZAa4R+pl6URkA=,8016395,"service",,"no"
	AwAAACN2XENfgBZAg4ExbN6iRkCWwnI6f4EWQCDRqdDlokZAAQAAAAIAAAAAAAAAlsJyOn+BFkAg0anQ5aJGQCN2XENfgBZAg4ExbN6iRkA=,5757931,"primary","Route de l'Isère",
	AwAAALdqMj4nyBZArrmj/+WURkC3ajI+J8gWQOmPH+D/lEZAAQAAAAIAAAAAAAAAt2oyPifIFkDpjx/g/5RGQLdqMj4nyBZArrmj/+WURkA=,31755896,"unclassified","Rue de la Source",
	AwAAAHrwyXm1shZAfD4UFiaZRkDW5Cmr6bIWQEVnmUUomUZAAQAAAAQAAAAAAAAAevDJebWyFkCldQqoJplGQMoQEUjTshZAfD4UFiaZRkCuuaP/5bIWQEa1iCgmmUZA1uQpq+myFkBFZ5lFKJlGQA==,25549465,"service",,"yes"

 