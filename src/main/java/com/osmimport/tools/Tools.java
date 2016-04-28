package com.osmimport.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

import org.fgdbapi.thindriver.TableHelper;

import com.osmimport.structures.model.FeatureClass;
import com.osmimport.structures.model.Field;
import com.osmimport.structures.model.FieldType;
import com.osmimport.structures.model.Table;

/**
 * several static tools functions
 * 
 * @author pfreydiere
 * 
 */
public class Tools {

	/**
	 * Deep Clone an object
	 * 
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
		return (Serializable) ois.readObject();

	}

	/**
	 * construct a safe actor name, replacing unsupported special characters to
	 * _
	 * 
	 * @param s
	 *            the string
	 * @return
	 */
	public static String toActorName(String s) {
		assert s != null;
		return s.replaceAll("[\\ \\\\:.]", "_");
	}

	/**
	 * convert an internal Table to TableHelper
	 * 
	 * @param t
	 * @return
	 * @throws Exception
	 */
	public static TableHelper convertTable(Table t) throws Exception {
		assert t != null;
		TableHelper th = null;
		if (t instanceof FeatureClass) {

			FeatureClass fct = (FeatureClass) t;
			if (!"WGS84".equals(fct.getSrs()))
				throw new InstantiationException(
						"only WGS84 SRS is supported for the moment");

			th = TableHelper.newFeatureClass(fct.getName(), fct.getGeomType(),
					TableHelper.constructW84SpatialReference());

		} else {
			th = TableHelper.newTable(t.getName());
		}

		List<Field> allFields = t.getFields();
		assert allFields != null;
		for (Field f : allFields) {
			if (f.getType() == FieldType.GEOMETRY) {
				// skipped because it is always added
			} else if (f.getType() == FieldType.INTEGER) {
				th.addIntegerField(f.getName());
			} else if (f.getType() == FieldType.LONG) {
				th.addLongField(f.getName());
			} else if (f.getType() == FieldType.STRING) {
				th.addStringField(f.getName(), f.getLength());
			} else if (f.getType() == FieldType.DOUBLE) {
				th.addDoubleField(f.getName());
			} else if (f.getType() == FieldType.SINGLE) {
				th.addDoubleField(f.getName());
			} else if (f.getType() == FieldType.SHORT) {
				th.addIntegerField(f.getName());
			} else {
				throw new Exception("field type " + f.getName()
						+ " unsupported");
			}
		}

		return th;
	}

	/**
	 * add spaces in the string builder
	 * @param sb
	 * @param nb
	 */
	public static void space(StringBuilder sb,int nb)
	{
		assert sb != null;
		
		if (nb >=0)
		{
			for (int i = 0 ; i < nb;i++)
			{
				sb.append(' ');
			}
		}
	}
	
}
