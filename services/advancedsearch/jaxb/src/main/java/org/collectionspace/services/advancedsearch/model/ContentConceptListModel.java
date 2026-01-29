package org.collectionspace.services.advancedsearch.model;

import java.util.List;

import org.collectionspace.services.advancedsearch.ContentConcepts;
import org.collectionspace.services.advancedsearch.ObjectFactory;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;

public class ContentConceptListModel {

	public static ContentConcepts contentConceptList(CollectionobjectsCommon collectionObject) {
		ContentConcepts concepts = null;
		final ObjectFactory objectFactory = new ObjectFactory();

		if (collectionObject != null && collectionObject.getContentConcepts() != null) {
			List<String> objectConcepts = collectionObject.getContentConcepts().getContentConcept();
			if (!objectConcepts.isEmpty()) {
				concepts = objectFactory.createContentConcepts();
				concepts.getContentConcept().addAll(objectConcepts);
			}
		}

		return concepts;
	}
}
