package org.collectionspace.services.common;

import java.util.Map;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;

/*
 * Maps service names to Resource instances. Use the Service Client Class to get the service name. 
 */
public interface ResourceMap extends Map<String, CollectionSpaceResource<PoxPayloadIn, PoxPayloadOut>> {

}
