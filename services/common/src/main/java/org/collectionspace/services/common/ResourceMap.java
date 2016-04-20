package org.collectionspace.services.common;

import java.util.Map;

/*
 * Maps service names to Resource instances. Use the Service Client Class to get the service name. 
 */
public interface ResourceMap<IT, OT> extends Map<String, CollectionSpaceResource<IT, OT>> {

}
