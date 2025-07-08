package org.collectionspace.services.advancedsearch.model;

import org.collectionspace.services.collectionobject.TitleGroupList;

public class TitleGroupListModel {
	public static String titleGroupListToDisplayString(TitleGroupList tlList) {
		// TODO: implement
		return tlList.getTitleGroup().get(0).getTitle();
	}
}
