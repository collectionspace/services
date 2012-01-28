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
public abstract class AbstractCommonListPoxServiceClientImpl<P extends CollectionSpaceCommonListPoxProxy>
	extends	AbstractPoxServiceClientImpl<AbstractCommonList, P> {
	//
	// All clients returning AbstractCommonList types should extend this class.
	//
}
