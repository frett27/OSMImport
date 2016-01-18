package com.osmimport.tools.polygoncreator;

import java.util.List;

import com.osmimport.tools.IReport;

public interface IInvalidPolygonConstructionFeedBack extends IReport {

	/**
	 * report we have a problem constructing a polygon
	 */
	public void polygonCreationFeedBackReport(List<MultiPathAndRole> elements,
			String reason) throws Exception;

}
