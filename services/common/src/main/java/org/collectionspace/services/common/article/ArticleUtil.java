package org.collectionspace.services.common.article;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.article.PublicitemsCommon;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.client.PublicItemClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.TransactionException;
import org.collectionspace.services.common.imaging.nuxeo.NuxeoBlobUtils;
import org.collectionspace.services.common.repository.RepositoryClient;

public class ArticleUtil {
	
	/*
	 * Sets common fields for an ArticlesCommon instance
	 */
	private static PublicitemsCommon setArticlesCommonMetadata(
			PublicitemsCommon articlesCommon,
			UriInfo uriInfo,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx) {
		PublicitemsCommon result = articlesCommon;
		
		if (result == null) {
			result = new PublicitemsCommon(); // If they passed in null, we'll create a new instance
		}
		
		String itemSource = result.getItemSource();
		if (itemSource == null || itemSource.trim().isEmpty()) {
	    	String publishingService = parentCtx.getServiceName(); // Overrides any existing value
	    	result.setItemSource(publishingService);
		}
    	
    	String publicUri = String.format("%s/%s/%s/%s",			// e.g., publicitems/{csid}/{tenant ID}/content
//    			uriInfo.getBaseUri().toString(), 				// the base part of the URL
    			PublicItemClient.SERVICE_NAME,					// the base service path to the Article service
    			PublicItemClient.CSID_PATH_PARAM_VAR,			// the {csid} param part that will be filled in later in ArticleDocumentModelHandler.fillAllParts() method
    			parentCtx.getTenantId(),						// the tenant ID part
    			PublicItemClient.PUBLICITEMS_CONTENT_SUFFIX);	// the final "content" suffix
    	result.setItemContentUri(publicUri);
    	
		return result;
	}
	
	/*
	 * Publishes a PoxPayloadOut instance for public access
	 */
	public static Response publishToRepository(
			PublicitemsCommon articlesCommon,
			ResourceMap resourceMap,
    		UriInfo uriInfo,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
			PoxPayloadOut poxPayloadOut) {
		Response result = null;
		
    	articlesCommon = setArticlesCommonMetadata(articlesCommon, uriInfo, parentCtx);
    	PoxPayloadIn input = new PoxPayloadIn(PublicItemClient.SERVICE_PAYLOAD_NAME, articlesCommon, 
    			PublicItemClient.SERVICE_COMMON_PART_NAME);
    	    	
    	ArticleResource articleResource = new ArticleResource();
    	result = articleResource.create(parentCtx, resourceMap, uriInfo, input.getXmlPayload());
    	
		return result;
	}
	
	/*
	 * Publishes a a byte stream for public access
	 */
	public static Response publishToRepository(
			PublicitemsCommon articlesCommon,
			ResourceMap resourceMap,
    		UriInfo uriInfo,
    		RepositoryClient<PoxPayloadIn, PoxPayloadOut> repositoryClient,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
			InputStream inputStream,
			String streamName) throws TransactionException {
		Response result = null;
		
    	BlobsCommon blobsCommon = NuxeoBlobUtils.createBlobInRepository(parentCtx, repositoryClient,
    			inputStream, streamName, false);
		
    	articlesCommon = setArticlesCommonMetadata(articlesCommon, uriInfo, parentCtx);
    	articlesCommon.setItemContentId(blobsCommon.getRepositoryId());
    	articlesCommon.setItemContentName(streamName);
    	
    	PoxPayloadOut poxPayloadOut = new PoxPayloadOut(PublicItemClient.SERVICE_PAYLOAD_NAME);
    	poxPayloadOut.addPart(PublicItemClient.SERVICE_COMMON_PART_NAME, articlesCommon);
    	
    	ArticleResource articleResource = new ArticleResource();
    	result = articleResource.create(parentCtx, resourceMap, uriInfo, poxPayloadOut.toXML());
    	
		return result;
	}

}
