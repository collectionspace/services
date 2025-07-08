package org.collectionspace.services.advancedsearch.model;

import org.collectionspace.services.collectionobject.ObjectNameList;

public class ObjectNameListModel {
	public static String objectNameListToDisplayString(ObjectNameList onList) {
		// TODO: implement
		return onList.getObjectNameGroup().get(0).getObjectName();
	}
}
