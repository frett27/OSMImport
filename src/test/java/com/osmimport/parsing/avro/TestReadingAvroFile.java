package com.osmimport.parsing.avro;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Test;

import com.osmimport.input.csv.ParserCallBack;
import com.osmimport.model.OSMAttributedEntity;

public class TestReadingAvroFile {

  @Test
  public void testReadAvro() throws Exception {

    File f = new File("C:\\france-avro\\1.avro");
    OSMAvroParser p =
        new OSMAvroParser(
            new ParserCallBack() {

              @Override
              public void lineParsed(long lineNumber, OSMAttributedEntity entity) throws Exception {
                System.out.println("read : " + entity);
              }

              @Override
              public void invalidLine(long lineNumber, String line) {
                // TODO Auto-generated method stub

              }
            });
    FileInputStream fis = new FileInputStream(f);
    try {
      p.parse(fis);
    } finally {
      fis.close();
    }
  }
}
