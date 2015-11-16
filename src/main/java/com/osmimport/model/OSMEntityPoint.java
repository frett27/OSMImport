package com.osmimport.model;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.Geometry.Type;
import com.esri.core.geometry.Point;

public class OSMEntityPoint extends OSMEntity {

	private double x;
	private double y;
	
	protected OSMEntityPoint()
	{
		super();
	}
	
	public OSMEntityPoint(long id, double x, double y, Map<String,Object> fields)
	{
		super(id, fields);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public Geometry getGeometry() {
		return new Point(x, y);
	}
	
	@Override
	public Type getGeometryType() {
		return Geometry.Type.Point;
	}
	
	public void readExternalMapDB(DataInput in) throws IOException {
		this.id = in.readLong(); // 1

		int geometryType = in.readInt(); // 2

		int blength = in.readInt(); // 3 length
		byte[] b = new byte[blength];
		in.readFully(b); // 4 geom content

		Geometry.Type t = Geometry.Type.Unknown;

		if (geometryType == Geometry.Type.Point.value()) {
			t = Geometry.Type.Point;
		} else if (geometryType == Geometry.Type.MultiPoint.value()) {
			t = Geometry.Type.MultiPoint;
		} else if (geometryType == Geometry.Type.Polyline.value()) {
			t = Geometry.Type.Polyline;
		} else if (geometryType == Geometry.Type.Polygon.value()) {
			t = Geometry.Type.Polygon;
		} else if (geometryType == Geometry.Type.Line.value()) {
			t = Geometry.Type.Line;
		} else if (geometryType == Geometry.Type.Envelope.value()) {
			t = Geometry.Type.Envelope;
		}

		this.x = in.readDouble();
		this.y = in.readDouble();

		int nbf = in.readInt(); // 5 nbf
		if (nbf == 0) {
			fields = null;
		} else {
			HashMap<String, Object> f = new HashMap<String, Object>();
			for (int i = 0; i < nbf; i++) {
				f.put(in.readUTF(), in.readUTF()); // 6 couples
			}
			fields = f;
		}

	}

	public void writeExternalMapDB(DataOutput out) throws IOException {

		out.writeLong(id); // 1

		out.writeInt(getGeometryType().value()); // 2 Geom type

		out.writeDouble(x);
		out.writeDouble(y);
		
		if (fields == null) {
			out.writeInt(0); // 5 nbf
		} else {
			out.writeInt(fields.size()); // 5 nbf
			for (Entry<String, Object> s : fields.entrySet()) {
				String k = s.getKey();
				if (k == null)
					k = "";
				out.writeUTF(k);

				Object v = s.getValue();
				if (v == null)
					v = "";
				out.writeUTF(v.toString());
			}
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {

		readExternalMapDB(in);

	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		writeExternalMapDB(out);
	}

	
	@Override
	public OSMAttributedEntity copy() {


		Map<String, Object> f = this.fields;
		if (f != null) {
			f = new HashMap<String, Object>(f);
		}

		return new OSMEntityPoint(this.id,this.x, this.y, f);
		
	}
	
}
