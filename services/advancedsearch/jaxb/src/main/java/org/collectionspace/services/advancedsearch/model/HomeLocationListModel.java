package org.collectionspace.services.advancedsearch.model;

import java.util.List;
import java.util.stream.Collectors;

import org.collectionspace.services.advancedsearch.HomeLocations;
import org.collectionspace.services.advancedsearch.ObjectFactory;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.HomeLocationGroup;

public class HomeLocationListModel {

	public static HomeLocations homeLocationList(CollectionobjectsCommon collectionObject) {
		HomeLocations homeLocations = null;
		final ObjectFactory objectFactory = new ObjectFactory();

		if (collectionObject != null && collectionObject.getHomeLocationGroupList() != null) {
			List<String> locations = collectionObject.getHomeLocationGroupList().getHomeLocationGroup().stream()
					.filter(group -> group != null && group.getHomeLocation() != null
							&& !group.getHomeLocation().isEmpty())
					.map(HomeLocationGroup::getHomeLocation)
					.collect(Collectors.toList());

			if (!locations.isEmpty()) {
				homeLocations = objectFactory.createHomeLocations();
				homeLocations.getHomeLocation().addAll(locations);
			}
		}

		return homeLocations;
	}
}
