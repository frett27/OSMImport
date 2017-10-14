package com.osmimport.parsing.avro;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Map;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.specific.SpecificDatumReader;
import org.osm.avro.AComplex;
import org.osm.avro.ANode;
import org.osm.avro.ARelated;
import org.osm.avro.ARelation;
import org.osm.avro.OSMEntity;
import org.osm.avro.OSMType;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.osmimport.input.csv.ParserCallBack;
import com.osmimport.model.OSMAttributedEntity;
import com.osmimport.model.OSMEntityGeometry;
import com.osmimport.model.OSMEntityPoint;
import com.osmimport.model.OSMRelatedObject;
import com.osmimport.model.OSMRelation;

public class OSMAvroParser {

  private ParserCallBack output;

  public OSMAvroParser(ParserCallBack output) {
    assert output != null;
    this.output = output;
  }

  /**
   * parse the avro file
   *
   * @param avroFile
   * @throws Exception
   */
  public void parse(InputStream avroFile) throws Exception {

    assert avroFile != null;

    InputStream inputStream = new BufferedInputStream(avroFile);
    try {
      DataFileStream<OSMEntity> userDatumReader =
          new DataFileStream<OSMEntity>(inputStream, new SpecificDatumReader(OSMEntity.class));

      while (userDatumReader.hasNext()) {

        OSMEntity entity = userDatumReader.next();

        OSMAttributedEntity exportedEntity = null;

        if (entity != null) {
          if (entity.getOsmtype() == OSMType.WAY) {
            AComplex way = entity.getWay();
            ByteBuffer geometry = way.getGeometry();
            Geometry g =
                GeometryEngine.geometryFromEsriShape(geometry.array(), Geometry.Type.Polyline);
            exportedEntity = new OSMEntityGeometry(way.getId(), g, (Map) way.getFields());
          } else if (entity.getOsmtype() == OSMType.POLYGON) {
            AComplex poly = entity.getPolygon();
            ByteBuffer geometry = poly.getGeometry();
            Geometry g =
                GeometryEngine.geometryFromEsriShape(geometry.array(), Geometry.Type.Polygon);
            exportedEntity = new OSMEntityGeometry(poly.getId(), g, (Map) poly.getFields());
          } else if (entity.getOsmtype() == OSMType.RELATION) {
            ARelation rel = entity.getRel();

            // convert type
            ArrayList<OSMRelatedObject> l = new ArrayList<OSMRelatedObject>();

            if (rel.getRelated() != null) {
              for (ARelated r : rel.getRelated()) {
                l.add(new OSMRelatedObject(r.getRelatedId(), r.getRole(), r.getType()));
              }
            }

            exportedEntity = new OSMRelation(rel.getId(), (Map) rel.getFields(), l);

          } else if (entity.getOsmtype() == OSMType.NODE) {
            ANode n = entity.getNode();
            exportedEntity = new OSMEntityPoint(n.getId(), n.getX(), n.getY(), (Map) n.getFields());

          } else {
            // unknown entity
          }
        }

        if (exportedEntity != null) {
          output.lineParsed(0, exportedEntity);
        }
      }

    } finally {
      inputStream.close();
    }
  }
}
