package com.poc.osm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Random;

import junit.framework.TestCase;

import com.esri.core.geometry.Point;
import com.poc.osm.model.OSMEntity;
import com.poc.osm.model.OSMEntityGeometry;

public class TestOSMEntitySerialization extends TestCase {

	private String randomString() {
		Random r = new Random();

		if (r.nextDouble() < 0.01)
			return "";

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			sb.append((char) ('a' + r.nextInt(26)));
		}
		return sb.toString();

	}

	private OSMEntity randomEntity() {
		Random r = new Random();
		Point p = new Point(r.nextDouble() * 10000, r.nextDouble() * 10000);

		HashMap<String, Object> h = null;
		int nb = (int) (r.nextDouble() * 10);
		for (int i = 0; i < nb; i++) {
			if (h == null) {
				h = new HashMap<String, Object>();
			}

			h.put(randomString(), randomString());

		}

		OSMEntity o = new OSMEntityGeometry(r.nextLong(), p, h);

		return o;
	}

	public void testSerialize() throws Exception {

		for (int i = 0; i < 10000; i++) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(baos);

			OSMEntity e = randomEntity();
			e.writeExternal(os);
			os.flush();
			os.close();

			byte[] b = baos.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(b);
			System.out.println("size of serialization :" + b.length);
			ObjectInputStream ois = new ObjectInputStream(bais);

			OSMEntity r = new OSMEntityGeometry(0,null,null);
			try {

				long start = System.nanoTime();
				for (int j = 0; j < 10000; j++) {
					r.readExternal(ois);
					long s = System.nanoTime();
					ois = new ObjectInputStream(new ByteArrayInputStream(b));
					long delta = System.nanoTime() - s;
					start += delta;
				}
				System.out.println(" time (ms) for 10000 deser: "
						+ (System.nanoTime() - start) / 1000000);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

	}

}
