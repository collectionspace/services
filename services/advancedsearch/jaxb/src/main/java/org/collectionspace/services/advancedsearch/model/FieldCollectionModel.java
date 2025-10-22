package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;

public class FieldCollectionModel {

	public static String fieldCollectionPlace(CollectionobjectsCommon common) {
		String fieldCollectionPlace = null;
		if (common != null && common.getFieldCollectionPlaces() != null) {
			List<String> places = common.getFieldCollectionPlaces().getFieldCollectionPlace();
			if (!places.isEmpty()) {
				fieldCollectionPlace = places.get(0);
			}
		}
		return fieldCollectionPlace;
	}

	public static String fieldCollectionSite(CollectionobjectsCommon common) {
		String fieldCollectionSite = null;
		if (common != null && common.getFieldCollectionSites() != null) {
			List<String> sites = common.getFieldCollectionSites().getFieldCollectionSite();
			if (!sites.isEmpty()) {
				fieldCollectionSite = sites.get(0);
			}
		}
		return fieldCollectionSite;
	}

	public static String fieldCollectionAgent(CollectionobjectsCommon common) {
		throw new UnsupportedOperationException("tbd");
	}
}
