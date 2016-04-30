package org.collectionspace.services.common;

import java.util.HashMap;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

public class ResourceMapImpl extends HashMap<String, CollectionSpaceResource<PoxPayloadIn, PoxPayloadOut>> implements ResourceMap<PoxPayloadIn, PoxPayloadOut> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
