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
	
	public static Response publishToRepository(ResourceMap resourceMap,
    		UriInfo uriInfo,
    		ServiceContext parentCtx,
			PoxPayloadOut poxPayloadOut) {
		Response result = null;
		
    	ArticlesCommon articlesCommon = new ArticlesCommon();
    	articlesCommon.setArticlePublisher("Hello, this is a test.");
    	PoxPayloadIn input = new PoxPayloadIn(ArticleClient.SERVICE_PAYLOAD_NAME, articlesCommon, 
    			ArticleClient.SERVICE_COMMON_PART_NAME);
    	    	
    	ArticleResource articleResource = new ArticleResource();
    	result = articleResource.create(parentCtx, resourceMap, uriInfo, input.getXmlPayload());
    	
		return result;
	}
	
	public static Response publishToRepository(ResourceMap resourceMap,
    		UriInfo uriInfo,
    		RepositoryClient repositoryClient,
    		ServiceContext parentCtx,
			InputStream inputStream,
			String streamName) throws TransactionException {
		Response result = null;
		
    	BlobsCommon blobsCommon = NuxeoBlobUtils.createBlobInRepository(parentCtx, repositoryClient, inputStream, streamName);
		
    	ArticlesCommon articlesCommon = new ArticlesCommon();
    	articlesCommon.setArticlePublisher(parentCtx.getUserId());
    	articlesCommon.setArticleContentCsid(blobsCommon.getRepositoryId());
    	PoxPayloadOut poxPayloadOut = new PoxPayloadOut(ArticleClient.SERVICE_PAYLOAD_NAME);
    	poxPayloadOut.addPart(ArticleClient.SERVICE_COMMON_PART_NAME, articlesCommon);
    	
    	ArticleResource articleResource = new ArticleResource();
    	result = articleResource.create(parentCtx, resourceMap, uriInfo, poxPayloadOut.toXML());
    	
		return result;
	}

}
