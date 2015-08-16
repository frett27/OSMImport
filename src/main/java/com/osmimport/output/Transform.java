package com.osmimport.output;

import java.util.List;

import com.osmimport.model.OSMAttributedEntity;

public abstract class Transform {

	public abstract List<OSMAttributedEntity> transform(OSMAttributedEntity e);

}
