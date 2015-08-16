package com.osmimport.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * several static tools functions
 * @author pfreydiere
 *
 */
public class Tools {

	/**
	 * Deep Clone an object
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public static Serializable clone(Serializable s) throws Exception {
		if (s == null)
			return null;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(s);
		oos.close();

		ByteArrayInputStream is = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(is);
		return (Serializable)ois.readObject();

	}

	/**
	 * construct a safe actor name, replacing unsupported special characters to _
	 * @param s the string
	 * @return
	 */
	public static String toActorName(String s) {
		assert s != null;
		return s.replaceAll("[\\ \\\\:.]", "_");
	}
	
}
