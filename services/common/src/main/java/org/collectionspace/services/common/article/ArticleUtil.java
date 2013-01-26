package org.collectionspace.services.common.article;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.article.ArticlesCommon;
import org.collectionspace.services.blob.BlobsCommon;
import org.collectionspace.services.client.ArticleClient;
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
	private static ArticlesCommon setArticlesCommonMetadata(
			ArticlesCommon articlesCommon,
			UriInfo uriInfo,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx) {
		ArticlesCommon result = articlesCommon;
		
		if (result == null) {
			result = new ArticlesCommon(); // If they passed in null, we'll create a new instance
		}
		
		String articleSource = result.getArticleSource();
		if (articleSource == null || articleSource.trim().isEmpty()) {
	    	String publishingService = parentCtx.getServiceName(); // Overrides any existing value
	    	result.setArticleSource(publishingService);
		}
    	
    	String publicUrl = String.format("%s%s/%s/%s/%s",	// e.g., http://{base url}/articles/{csid}/{tenant ID}/content
    			uriInfo.getBaseUri().toString(), 			// the base part of the URL
    			ArticleClient.SERVICE_NAME,					// the base service path to the Article service
    			ArticleClient.CSID_PATH_PARAM_VAR,			// the {csid} param part that will be filled in later in ArticleDocumentModelHandler.fillAllParts() method
    			parentCtx.getTenantId(),					// the tenant ID part
    			ArticleClient.PUBLICITEMS_CONTENT_SUFFIX);	// the final "content" suffix
    	result.setArticleContentUrl(publicUrl);
    	
		return result;
	}
	
	/*
	 * Publishes a PoxPayloadOut instance for public access
	 */
	public static Response publishToRepository(
			ArticlesCommon articlesCommon,
			ResourceMap resourceMap,
    		UriInfo uriInfo,
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
			PoxPayloadOut poxPayloadOut) {
		Response result = null;
		
    	articlesCommon = setArticlesCommonMetadata(articlesCommon, uriInfo, parentCtx);
    	PoxPayloadIn input = new PoxPayloadIn(ArticleClient.SERVICE_PAYLOAD_NAME, articlesCommon, 
    			ArticleClient.SERVICE_COMMON_PART_NAME);
    	    	
    	ArticleResource articleResource = new ArticleResource();
    	result = articleResource.create(parentCtx, resourceMap, uriInfo, input.getXmlPayload());
    	
		return result;
	}
	
	/*
	 * Publishes a a byte stream for public access
	 */
	public static Response publishToRepository(
			ArticlesCommon articlesCommon,
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
    	articlesCommon.setArticleContentRepositoryId(blobsCommon.getRepositoryId());
    	articlesCommon.setArticleContentName(streamName);
    	
    	PoxPayloadOut poxPayloadOut = new PoxPayloadOut(ArticleClient.SERVICE_PAYLOAD_NAME);
    	poxPayloadOut.addPart(ArticleClient.SERVICE_COMMON_PART_NAME, articlesCommon);
    	
    	ArticleResource articleResource = new ArticleResource();
    	result = articleResource.create(parentCtx, resourceMap, uriInfo, poxPayloadOut.toXML());
    	
		return result;
	}

}
