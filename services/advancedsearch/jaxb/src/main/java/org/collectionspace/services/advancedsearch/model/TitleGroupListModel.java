package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.TitleGroup;
import org.collectionspace.services.collectionobject.TitleGroupList;

public class TitleGroupListModel {

	/**
	 * @param tlList The TitleGroupList
	 * @return the value of the title field for the first TitleGroup
	 */
	public static String titleGroupListToDisplayString(TitleGroupList tlList) {
		if (tlList == null) {
			return null;
		}

		String title = null;
		List<TitleGroup> titleGroups = tlList.getTitleGroup();

		if (!titleGroups.isEmpty() && titleGroups.get(0) != null) {
			TitleGroup titleGroup = titleGroups.get(0);
			title = titleGroup.getTitle();
		}

		return title;
	}
}
