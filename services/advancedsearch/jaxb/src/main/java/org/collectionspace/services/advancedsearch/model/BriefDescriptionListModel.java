package org.collectionspace.services.advancedsearch.model;

import org.collectionspace.services.collectionobject.BriefDescriptionList;

public class BriefDescriptionListModel {
	public static String briefDescriptionListToDisplayString(BriefDescriptionList bdList) {
		// TODO: implement
		return bdList.getBriefDescription().get(0);
	}
}
