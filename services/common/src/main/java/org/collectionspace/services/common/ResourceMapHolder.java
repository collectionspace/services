package org.collectionspace.services.common;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

public interface ResourceMapHolder {
	public ResourceMap<PoxPayloadIn, PoxPayloadOut> getResourceMap();
}
