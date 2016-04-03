package org.collectionspace.services.client;

import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * 
 * @author remillet
 *
 * All clients returning AbstractCommonList types should extend this class.
 * 
 * @param <P>
 */
public abstract class AbstractCommonListPoxServiceClientImpl<P extends CollectionSpaceCommonListPoxProxy, CPT>
	extends	AbstractPoxServiceClientImpl<AbstractCommonList, P, CPT> {
	//
	// All clients returning AbstractCommonList types should extend this class.
	//
}
