package com.poc.osm.output.model;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import com.esrifrance.fgdbapi.xml.ArrayOfField;
import com.esrifrance.fgdbapi.xml.ArrayOfIndex;
import com.esrifrance.fgdbapi.xml.ArrayOfPropertySetProperty;
import com.esrifrance.fgdbapi.xml.DEFeatureClass;
import com.esrifrance.fgdbapi.xml.DETable;
import com.esrifrance.fgdbapi.xml.DataElement;
import com.esrifrance.fgdbapi.xml.EsriDatasetType;
import com.esrifrance.fgdbapi.xml.EsriFeatureType;
import com.esrifrance.fgdbapi.xml.EsriFieldType;
import com.esrifrance.fgdbapi.xml.EsriGeometryType;
import com.esrifrance.fgdbapi.xml.Field;
import com.esrifrance.fgdbapi.xml.Fields;
import com.esrifrance.fgdbapi.xml.GeographicCoordinateSystem;
import com.esrifrance.fgdbapi.xml.GeometryDef;
import com.esrifrance.fgdbapi.xml.Index;
import com.esrifrance.fgdbapi.xml.Indexes;
import com.esrifrance.fgdbapi.xml.ObjectFactory;
import com.esrifrance.fgdbapi.xml.PropertySet;
import com.esrifrance.fgdbapi.xml.SpatialReference;
import com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper;

/**
 * Table / Featureclass Description helper, for simplify the structure creation
 * 
 * @author pfreydiere
 * 
 */
public class TableHelper {

	private DETable dataElement;

	private String name;

	public static TableHelper newTable(String name) {

		TableHelper th = new TableHelper(name);

		Field o = new Field();
		o.setName("OBJECTID");
		o.setType(EsriFieldType.ESRI_FIELD_TYPE_OID);
		o.setLength(4);
		o.setRequired(true);
		o.setIsNullable(false);
		o.setEditable(false);
		o.setAliasName(o.getName());
		o.setModelName(o.getName());

		Fields flds = new Fields();

		flds.setFieldArray(new ArrayOfField());
		flds.getFieldArray().getField().add(o); // adding objectid

//		Index oid = new Index();
//		oid.setName("OIDINDEX");
//
//		Fields sfields = flds;
//		ArrayOfField indexsarrayOfField = new ArrayOfField();
//		sfields.setFieldArray(indexsarrayOfField);
//		indexsarrayOfField.getField().add(o);
//
//		oid.setFields(sfields);
//		oid.setIsUnique(true);
//		oid.setIsAscending(true);

		ArrayOfIndex arrayOfIndex = new ArrayOfIndex();
//		arrayOfIndex.getIndex().add(oid);

		th.dataElement = new DETable();

		Indexes indices = new Indexes();
		indices.setIndexArray(arrayOfIndex);
		th.dataElement.setIndexes(indices);

		th.dataElement.setCatalogPath("/" + name);
		th.dataElement.setName(name);
		th.dataElement.setChildrenExpanded(false);
		th.dataElement.setOIDFieldName("OBJECTID");
		th.dataElement.setHasOID(true);
		th.dataElement.setDatasetType(EsriDatasetType.ESRI_DT_TABLE);
		th.dataElement.setVersioned(false);

		// to be adjusted
		th.dataElement.setCLSID("{52353152-891A-11D0-BEC6-00805F7C4268}");

		th.dataElement.setModelName(name);
		th.dataElement.setAliasName(name);

		th.dataElement.setFields(flds);

		return th;
	}

	/**
	 * create a new FeatureClass description
	 * 
	 * @param name
	 * @param geomType
	 * @param gcs
	 * @return
	 */
	public static TableHelper newFeatureClass(String name,
			EsriGeometryType geomType, SpatialReference gcs) {

		TableHelper th = newTable(name);

		DEFeatureClass fc = new DEFeatureClass();
		fc.setFields(th.dataElement.getFields()); // retrieve the fields
		fc.setIndexes(th.dataElement.getIndexes());

		th.dataElement = fc; // replace the dataElement

		Field s = new Field();
		s.setName("SHAPE");
		s.setType(EsriFieldType.ESRI_FIELD_TYPE_GEOMETRY);
		s.setRequired(true);
		s.setIsNullable(true);
		s.setAliasName("SHAPE");
		s.setModelName("SHAPE");
		s.setEditable(true);

		GeometryDef geom = new GeometryDef();
		geom.setGeometryType(geomType);
		geom.setSpatialReference(gcs);
		geom.setGridSize0((double) 0);

		geom.setHasM(false);
		geom.setHasZ(false);

		s.setGeometryDef(geom);

		Fields flds = th.dataElement.getFields();
		flds.getFieldArray().getField().add(s);

//		Index sindex = new Index();
//		sindex.setName("FDO_SHAPE");
//		Fields shapeIndexFields = flds;
//		ArrayOfField shapeIndexArrayOfFields = new ArrayOfField();
//		shapeIndexArrayOfFields.getField().add(s);
//		shapeIndexFields.setFieldArray(shapeIndexArrayOfFields);
//		sindex.setFields(shapeIndexFields);
//		sindex.setIsAscending(true);
//		sindex.setIsUnique(false);
//
//		fc.getIndexes().getIndexArray().getIndex().add(sindex);

		fc.setCatalogPath("/" + name);
		fc.setName(name);
		fc.setFeatureType(EsriFeatureType.ESRI_FT_SIMPLE);
		fc.setShapeType(geomType);
		fc.setChildrenExpanded(false);
		fc.setShapeFieldName("SHAPE");
		fc.setHasM(false);
		fc.setHasZ(false);
		fc.setOIDFieldName("OBJECTID");
		fc.setHasOID(true);
		fc.setDatasetType(EsriDatasetType.ESRI_DT_FEATURE_CLASS);
		fc.setVersioned(false);
		fc.setCLSID("{52353152-891A-11D0-BEC6-00805F7C4268}");

		fc.setModelName(name);
		fc.setAliasName(name);

		fc.setSpatialReference(gcs);

		return th;

	}

	public DETable build() {

		PropertySet ps = new PropertySet();
		dataElement.setExtensionProperties(ps);
		ps.setPropertyArray(new ArrayOfPropertySetProperty());
		return dataElement;
	}

	public String buildAsString() throws Exception {
		return serializeElement(build());
	}
	
	public String getName()  {
		return this.name;
	}

	// ////////////////////////////////
	protected static String serializeElement(DETable de) throws Exception {
		class NP extends NamespacePrefixMapper {
			@Override
			public String getPreferredPrefix(String arg0, String arg1,
					boolean arg2) {
				if (arg0.equals("http://www.esri.com/schemas/ArcGIS/10.1"))
					return "esri";
				if (arg0.equals("http://www.w3.org/2001/XMLSchema-instance"))
					return "xsi";
				return null;
			}
		}

		JAXBContext isn = JAXBContext.newInstance(ObjectFactory.class
				.getPackage().getName());
		Marshaller m = isn.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
				new NP());

		StringWriter sw = new StringWriter();
		m.marshal(new JAXBElement<DataElement>(new QName(
				"http://www.esri.com/schemas/ArcGIS/10.1", "DataElement"),
				DataElement.class, de), sw);

		return sw.getBuffer().toString();
	}

	TableHelper(String name) {
		this.name = name;
	}

	public TableHelper addField(Field f) {
		dataElement.getFields().getFieldArray().getField().add(f);
		return this;
	}

	/**
	 * add a simple integer field
	 * 
	 * @param name
	 * @return
	 */
	public TableHelper addIntegerField(String name) {

		Field f = new Field();
		f.setName(name);
		f.setType(EsriFieldType.ESRI_FIELD_TYPE_INTEGER);
		f.setLength(4);
		f.setEditable(true);
		f.setIsNullable(true);

		return addField(f);

	}

	public TableHelper addStringField(String name, int length) {

		Field f = new Field();
		f.setName(name);
		f.setType(EsriFieldType.ESRI_FIELD_TYPE_STRING);
		f.setLength(length);
		f.setEditable(true);
		f.setIsNullable(true);

		return addField(f);

	}

	public TableHelper addDoubleField(String name) {
		Field f = new Field();
		f.setName(name);
		f.setType(EsriFieldType.ESRI_FIELD_TYPE_STRING);
		f.setLength(8);
		f.setEditable(true);
		f.setIsNullable(true);
		return addField(f);
	}

	//
	// public TableHelper addLongField(String name) {
	//
	// }
	//

	public static GeographicCoordinateSystem constructW84SpatialReference() {

		GeographicCoordinateSystem gcs = new GeographicCoordinateSystem();
		gcs.setWKID(4326);
		gcs.setWKT("GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0],UNIT[\"Degree\",0.017453292519943295]]");
		gcs.setHighPrecision(true);
		gcs.setXYScale(10000.0);
		gcs.setXOrigin(-200.0);
		gcs.setYOrigin(-100.0);
		gcs.setZOrigin(-1000.0);
		gcs.setMOrigin(0.0);
		gcs.setZScale(100.0);
		gcs.setMScale(100.0);
		gcs.setXYTolerance(0.0001);
		gcs.setLeftLongitude(-180.0);
		return gcs;
	}

}
