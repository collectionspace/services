package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.ObjectNameGroup;
import org.collectionspace.services.collectionobject.ObjectNameList;

public class ObjectNameListModel {
	public static String objectNameListToDisplayString(ObjectNameList onList) {
		List<ObjectNameGroup> objectNameGroups = onList.getObjectNameGroup();
		String returnString = "";
		if(null != objectNameGroups ) {
			if(!objectNameGroups.isEmpty()) {
				ObjectNameGroup objectNameGroup = objectNameGroups.get(0);
				if(null != objectNameGroup.getObjectName()) {
					returnString = objectNameGroup.getObjectName();
				}
			}
		}
		
		return returnString;
	}
}
