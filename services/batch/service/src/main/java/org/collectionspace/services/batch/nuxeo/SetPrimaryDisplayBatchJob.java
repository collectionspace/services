// Author: RJ Li

package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.collectionspace.services.client.MediaClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;

public class SetPrimaryDisplayBatchJob extends AbstractBatchJob {
	final Logger log = LoggerFactory.getLogger(SetPrimaryDisplayBatchJob.class);
	
	public SetPrimaryDisplayBatchJob() {
		setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE));
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			if (requestIsForInvocationModeSingle()) {
				String csid = getInvocationContext().getSingleCSID();
				
				if (csid == null) {
					throw new Exception("no singleCSID was supplied");
				}
	
				String docType = getInvocationContext().getDocType();
				
				if (StringUtils.isEmpty(docType)) {
					throw new Exception("no docType was supplied");
				}
	
				log.debug("set primary display for " + docType + " record with csid: " + csid);
				if (logger.isTraceEnabled()) {
		            logger.trace("trace on");
		        }
				log.debug("trigger off");
				setPrimary(docType, csid);
				
			}
		} catch(Exception e) {
			setErrorResult(e.getMessage());
		}	
	}
	/**
	 * Makes the current media record the primary display by setting the schema to true
	 * And makes the primary display of the rest of the media records related by a collectionobject to be false
	 * @param docType The type of record
	 * @param csid Identifier
	 */
	private void setPrimary(String docType, String csid) {
		/*First create the two payloads. One sets the current image as primary display.
		 * The other one sets all other related media objects to be false
		 */
        String thisMediaUpdatePayload =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<document name=\"media\">"
                + "  <ns2:media_pahma "
                + "      xmlns:ns2=\"http://collectionspace.org/services/media/local/pahma\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "    <primaryDisplay>" + true + "</primaryDisplay>"
                + "  </ns2:media_pahma>"
                + "</document>";
        
        String otherMediaUpdatePayload =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<document name=\"media\">"
                + "  <ns2:media_pahma "
                + "      xmlns:ns2=\"http://collectionspace.org/services/media/local/pahma\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "    <primaryDisplay>" + false + "</primaryDisplay>"
                + "  </ns2:media_pahma>"
                + "</document>";
        
        /*
         * Gets the mediaresource that will be used as object for updating the payload
         */
        log.debug("call before getResourceMap");
        ResourceMap resourcemap = getResourceMap();

        log.debug("ResouceMap: " + resourcemap + " and call before get Media client resource base");
        ResourceBase mediaResource = resourcemap.get(MediaClient.SERVICE_NAME);
      
        /*
         * Updates current media with the new payload
         */
        log.debug("MediaResource: " + mediaResource + " and Before update");
        try {
        	byte[] response = mediaResource.update(resourcemap, createUriInfo(), csid,
        					thisMediaUpdatePayload);
        } catch (Exception e) {
        	log.error("this update error", e);
        }
        
        /* 
         * Gets all related media objects first by getting the collectionobject and media objects related to that 
         */
        log.debug("Primary display for CollectionObject " + csid
                + " was set to " + true);
        List<String> relatedCollectionObjects = null;
        List<String> otherMediaRecords = new ArrayList<String>();
        
		try {
			relatedCollectionObjects = findRelatedCollectionObjects(csid);
			log.debug("relatedCollectionObjects is initialized to: " + relatedCollectionObjects);
			for (String collectionObjectcsid: relatedCollectionObjects) {
				otherMediaRecords.addAll(findRelatedObjects(collectionObjectcsid, CollectionObjectConstants.NUXEO_DOCTYPE, "affects", null, "Media"));
			}
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			log.error("finding relation caused an error", e);
		}
		
		/*
		 * Updates the other media records with other payload
		 */
		log.debug("otherMediaRecords: " + otherMediaRecords);
        for (String otherMediaCsid: otherMediaRecords) {
        	 try {
        		if (!otherMediaCsid.equals(csid)) {
					byte[] response2 = mediaResource.update(resourcemap, createUriInfo(), otherMediaCsid,
							otherMediaUpdatePayload);
        		}
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				log.error("Others failed to update", e);
			}	
        }
        
        log.debug("update success?");
    }
}
