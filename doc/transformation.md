#Documentation import OSM

## introduction

Cette documentation définie le fichier de transformation, permettant l'import des données OSM dans une filegeodatabase, permettant d'effectuer quelques adaptations durant l'import, dans les adaptations possibles, on note :

- Le mapping de certaines propriétés dans des champs de FGDB, en utilisant :
	- du mapping entre les valeurs
	- une sélection d'un sous ensemble des valeures


##Fichier de configuration

plusieures sections sont définies dans le ficheir de configuration, ce fichier définit les section suivantes :

- la description des différents flux à partir du fichier OSM
- les transformations à effectuer sur les flux
- leur insertion dans une ou plusieures tables/couches de la FGDB


###Section stream

chaque flux peut être filtré et transformé pour construire un nouveau flux

	arbres = stream(in) {
		
		filter { 
			e ->

				if (!e.getGeometry().getType() == EsriGeometryType.Point)
					return false;

				return e.hasField('champOrigine') && (e.v("champOrigine") in ["3","4"])
		}

		transformation {

			e -> 
				n = e.fieldMapping(["champOrigine"],	["champDestination"])
			
				n = n.valueMapping(["champDestination"], 
					[ "3" : "Toto", "4" : "autrevaleur" ])

				return n
		}
	}

in : flux d'entree des entités OSM

Les propriétés filter ou transformation sont optionnelles. Ceci permet de pouvoir réutiliser un même flux, pour plusieures sorties


###Section output  
 

Cette section le stockage dans une couche de la FGDB

on indique alors la liste des flux de sortie avec les différents paramètres de sortie.

	outputStream(arbres).in("filegdbfilepath").into("tableorfeatureClassName").withStructure(tableStructure)

