package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;

public class TitleGroupListModel {
	public static String titleGroupListToDisplayString(TitleGroupList tlList) {
		String returnString = "";
		List<TitleGroup> titleGroups = tlList.getTitleGroup();
		if(null != titleGroups) {
			if(!titleGroups.isEmpty()) {
				TitleGroup titleGroup = titleGroups.get(0);
				if(null != titleGroup.getTitle()) {
					returnString = titleGroup.getTitle();
				}
			}
		}

		return returnString;
	}
}
