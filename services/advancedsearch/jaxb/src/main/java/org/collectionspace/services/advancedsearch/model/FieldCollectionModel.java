package org.collectionspace.services.advancedsearch.model;

import java.util.List;
import java.util.Optional;

import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.StructuredDateGroup;

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

	public static String fieldCollectionDate(CollectionobjectsCommon common) {
		String fieldCollectionDate = null;
		if (common != null && common.getFieldCollectionDateGroup() != null) {
			StructuredDateGroup fieldCollectionDateGroup = common.getFieldCollectionDateGroup();
			fieldCollectionDate = fieldCollectionDateGroup.getDateDisplayDate();
		}
		return fieldCollectionDate;
	}


	public static Optional<String> fieldCollector(CollectionobjectsCommon common) {
		String fieldCollector = null;
		if (common != null && common.getFieldCollectors() != null) {
			final List<String> fieldCollectors = common.getFieldCollectors().getFieldCollector();
			if (!fieldCollectors.isEmpty()) {
				fieldCollector = fieldCollectors.get(0);
			}
		}

		return Optional.ofNullable(fieldCollector);
	}
}
