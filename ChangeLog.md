
Version 0.4:
-------

- Table support for integrate the result.

- Transform can now accept List of entities in result, permitting to fire more than One entity. This permit to create new entities from relations or integrate Entities in more that One FeatureClass.

- Relations can now be integrated in Tables, using a transform that lineage the OSMRelation with the related entities.

- When a polygon relation contains related ways with outer and inner roles, this will try to create an associated polygon.