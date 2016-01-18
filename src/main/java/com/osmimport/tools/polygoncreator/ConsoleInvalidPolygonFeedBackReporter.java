package com.osmimport.tools.polygoncreator;

import java.util.List;

public class ConsoleInvalidPolygonFeedBackReporter implements
		IInvalidPolygonConstructionFeedBack {

	@Override
	public synchronized void polygonCreationFeedBackReport(List<MultiPathAndRole> elements,
			String reason) {
		System.out.println("Invalid polygon feedback report :" + reason);
	}
	
}
