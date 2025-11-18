package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.BriefDescriptionList;

public class BriefDescriptionListModel {
	private static final int DESCRIPTION_LENGTH = 255;

	public static String briefDescriptionListToDisplayString(BriefDescriptionList bdList) {
		List<String> bds = bdList.getBriefDescription();
		StringBuilder buf = new StringBuilder();
		if (null != bds) {
			if (!bds.isEmpty()) {
				// get the 1st 255 characters of the 1st defined brief description if there is one
				String description = bds.get(0);
				if (null != description) {
					int length = description.length();
					buf.append(description);
					if (length > DESCRIPTION_LENGTH) {
						buf.replace(DESCRIPTION_LENGTH, buf.length(), "...");
					}
				}

			}
		}
		return buf.toString();
	}
}
