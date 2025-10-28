package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.ObjectProductionDateGroupList;
import org.collectionspace.services.collectionobject.ObjectProductionPlaceGroup;

public class ObjectProductionModel {

	public static String objectProductionDate(CollectionobjectsCommon collectionObject) {
		String date = null;
		if (collectionObject != null && collectionObject.getObjectProductionDateGroupList() != null) {
			ObjectProductionDateGroupList dateGroup = collectionObject.getObjectProductionDateGroupList();
			if (!dateGroup.getObjectProductionDateGroup().isEmpty()) {
				date = dateGroup.getObjectProductionDateGroup().get(0).getDateDisplayDate();
			}
		}
		return date;
	}

	public static String objectProductionPlace(CollectionobjectsCommon collectionObject) {
		String place = null;
		if (collectionObject != null && collectionObject.getObjectProductionPlaceGroupList() != null) {
			List<ObjectProductionPlaceGroup> placeGroup = collectionObject.getObjectProductionPlaceGroupList()
				.getObjectProductionPlaceGroup();

			if (!placeGroup.isEmpty()) {
				place = placeGroup.get(0).getObjectProductionPlace();
			}
		}

		return place;
	}
}
