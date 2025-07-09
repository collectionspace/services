package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.BriefDescriptionList;

public class BriefDescriptionListModel {
	private static int DESCRIPTION_LENGTH = 55;
	public static String briefDescriptionListToDisplayString(BriefDescriptionList bdList) {
		List<String> bds = bdList.getBriefDescription();
		String returnString = "";
		if(null != bds) {
			if(!bds.isEmpty()) {
				// get the 1st 55 characters of the 1st defined brief description if there is one
				if(null != bds.get(0)) {
					int length = bds.get(0).length();
					returnString = bds.get(0).substring(0, (length >= DESCRIPTION_LENGTH) ? DESCRIPTION_LENGTH : length);
				}
				
			}
		}
		return returnString;
	}
}
