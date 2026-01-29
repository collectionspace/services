package org.collectionspace.services.advancedsearch.model;

import org.collectionspace.services.advancedsearch.NagpraCategories;
import org.collectionspace.services.advancedsearch.ObjectFactory;
import org.collectionspace.services.collectionobject.domain.nagpra.CollectionObjectsNAGPRA;

public class NAGPRACategoryModel {

	public static NagpraCategories napgraCategories(CollectionObjectsNAGPRA nagpra) {
		final ObjectFactory asObjectFactory = new ObjectFactory();
		final CollectionObjectsNAGPRA.NagpraCategories objectNAGPRACategories = nagpra.getNagpraCategories();

		NagpraCategories searchCategories = null;
		if (objectNAGPRACategories != null && !objectNAGPRACategories.getNagpraCategory().isEmpty()) {
			searchCategories = asObjectFactory.createNagpraCategories();
			searchCategories.getNagpraCategory().addAll(objectNAGPRACategories.getNagpraCategory());
		}

		return searchCategories;
	}

}
