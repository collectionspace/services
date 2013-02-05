package org.collectionspace.services.common.publicitem;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.publicitem.PublicitemsCommon;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.client.PublicItemClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoBlobUtils;
import org.collectionspace.services.common.publicitem.PublicItemResource;
import org.collectionspace.services.common.repository.RepositoryClient;

public class PublicItemUtil {
	
	/*
	 * Sets common fields for an PublicitemsCommon instance
	 */
	private static PublicitemsCommon setCommonMetadata(
			PublicitemsCommon publicitemsCommon,
			UriInfo uriInfo,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx) {
		PublicitemsCommon result = publicitemsCommon;
		
		if (result == null) {
			result = new PublicitemsCommon(); // If they passed in null, we'll create a new instance
		}
		
		String itemSource = result.getContentSource();
		if (itemSource == null || itemSource.trim().isEmpty()) {
	    	String publishingService = parentCtx.getServiceName(); // Overrides any existing value
	    	result.setContentSource(publishingService);
		}
    	
		//
		// Store just a partial URL -just the CollectionSpace specific URI without the base URL.
		//
    	String publicUri = String.format("%s/%s/%s/%s",			// e.g., publicitems/{tenant ID}/{csid}/content
    			PublicItemClient.SERVICE_NAME,					// the base service path to the PublicItem service
    			PublicItemClient.CSID_PATH_PARAM_VAR,			// the {csid} param part that will be filled in later in PublicItemDocumentModelHandler.fillAllParts() method
    			parentCtx.getTenantId(),						// the tenant ID part
    			PublicItemClient.PUBLICITEMS_CONTENT_SUFFIX);	// the final "content" suffix
    	result.setContentUri(publicUri);
    	
		return result;
	}
	
	/*
	 * Publishes a PoxPayloadOut instance for public access
	 */
	public static Response publishToRepository(
			PublicitemsCommon publicitemsCommon,
			ResourceMap resourceMap,
    		UriInfo uriInfo,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
			PoxPayloadOut poxPayloadOut) {
		Response result = null;
		
    	publicitemsCommon = setCommonMetadata(publicitemsCommon, uriInfo, parentCtx);
    	PoxPayloadIn input = new PoxPayloadIn(PublicItemClient.SERVICE_PAYLOAD_NAME, publicitemsCommon, 
    			PublicItemClient.SERVICE_COMMON_PART_NAME);
    	    	
    	PublicItemResource publicItemResource = new PublicItemResource();
    	result = publicItemResource.create(parentCtx, resourceMap, uriInfo, input.getXmlPayload());
    	
		return result;
	}
	
	/*
	 * Publishes a a byte stream for public access
	 */
	public static Response publishToRepository(
			PublicitemsCommon publicitemsCommon,
			ResourceMap resourceMap,
    		UriInfo uriInfo,
    		RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
			InputStream inputStream,
			String streamName) throws TransactionException {
		Response result = null;
		
    	BlobsCommon blobsCommon = NuxeoBlobUtils.createBlobInRepository(parentCtx, repositoryClient,
    			inputStream, streamName, false);
		
    	publicitemsCommon = setCommonMetadata(publicitemsCommon, uriInfo, parentCtx);
    	publicitemsCommon.setContentId(blobsCommon.getRepositoryId());
    	publicitemsCommon.setContentName(streamName);
    	
    	PoxPayloadOut poxPayloadOut = new PoxPayloadOut(PublicItemClient.SERVICE_PAYLOAD_NAME);
    	poxPayloadOut.addPart(PublicItemClient.SERVICE_COMMON_PART_NAME, publicitemsCommon);
    	
    	PublicItemResource publicItemResource = new PublicItemResource();
    	result = publicItemResource.create(parentCtx, resourceMap, uriInfo, poxPayloadOut.toXML());
    	
		return result;
	}

}
