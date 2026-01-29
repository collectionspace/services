package org.collectionspace.services.advancedsearch;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.collectionspace.collectionspace_core.CollectionSpaceCore;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.nagpra.CollectionObjectsNAGPRA;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.CollectionobjectsNaturalhistory;

/**
 * Singleton for the {@link JAXBContext} which the AdvancedSearch will use
 * Eventually this should just be a bean which can be injected
 * @since 8.3.0
 */
public final class AdvancedSearchJAXBContext {
	private static final AdvancedSearchJAXBContext INSTANCE = new AdvancedSearchJAXBContext();

	private final JAXBContext jaxbContext;

	private AdvancedSearchJAXBContext() {
		try {
			jaxbContext = JAXBContext.newInstance(CollectionSpaceCore.class, CollectionobjectsCommon.class,
			                                      CollectionobjectsNaturalhistory.class, CollectionObjectsNAGPRA.class);
		} catch (JAXBException e) {
			throw new RuntimeException("Unable to create JAXBContext for AdvancedSearch");
		}
	}

	public static JAXBContext getJaxbContext() {
		return INSTANCE.jaxbContext;
	}
}
