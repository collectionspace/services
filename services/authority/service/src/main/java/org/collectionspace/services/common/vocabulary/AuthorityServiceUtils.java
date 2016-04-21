package org.collectionspace.services.common.vocabulary;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityIdentifierUtils;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorityServiceUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthorityIdentifierUtils.class);
    //
    // Used to keep track if an authority item's is deprecated
    public static final String IS_DEPRECATED_PROPERTY = "IS_DEPRECATED_PROPERTY";
    public static final Boolean DEPRECATED = true;
    public static final Boolean NOT_DEPRECATED = !DEPRECATED;
    
    // Used to keep track if an authority item's rev number should be updated
    public static final String SHOULD_UPDATE_REV_PROPERTY = "SHOULD_UPDATE_REV_PROPERTY";
    public static final boolean UPDATE_REV = true;
    public static final boolean DONT_UPDATE_REV = !UPDATE_REV;

    // Used to keep track if an authority item is a locally proposed member of a SAS authority
    public static final String IS_PROPOSED_PROPERTY = "IS_PROPOSED";
    public static final Boolean PROPOSED = true;
    public static final Boolean NOT_PROPOSED = !PROPOSED;
    public static final Boolean NO_CHANGE = null;

    static public PoxPayloadIn requestPayloadIn(ServiceContext ctx, Specifier specifier, Class responseType) throws Exception {
    	PoxPayloadIn result = null;
    	
        AuthorityClient client = (AuthorityClient) ctx.getClient();
        Response res = client.read(specifier.getURNValue());
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(client.getClass().getCanonicalName() + ": status = " + statusCode);
	        }
	        
            result = new PoxPayloadIn((String)res.readEntity(responseType)); // Get the entire response!	        
        } finally {
        	res.close();
        }
    	
    	return result;
    }
    
    //
    // Makes a call to the SAS server for a authority item payload
    //    
    static public PoxPayloadIn requestPayloadIn(AuthorityItemSpecifier specifier, String serviceName, Class responseType) throws Exception {
    	PoxPayloadIn result = null;
    	
    	ServiceContext parentCtx = new MultipartServiceContextImpl(serviceName);
        AuthorityClient client = (AuthorityClient) parentCtx.getClient();
        Response res = client.readItem(specifier.getParentSpecifier().getURNValue(), specifier.getItemSpecifier().getURNValue());
        try {
	        int statusCode = res.getStatus();
	
	        // Check the status code of the response: does it match
	        // the expected response(s)?
	        if (logger.isDebugEnabled()) {
	            logger.debug(client.getClass().getCanonicalName() + ": status = " + statusCode);
	        }
	        
            result = new PoxPayloadIn((String)res.readEntity(responseType)); // Get the entire response!	        
        } finally {
        	res.close();
        }
    	
    	return result;
    }
    
    static public boolean setAuthorityItemDeprecated(DocumentModel docModel, String authorityItemCommonSchemaName, Boolean flag) throws Exception {
    	boolean result = false;
    	
    	docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.DEPRECATED,
    			new Boolean(flag));
    	CoreSessionInterface session = (CoreSessionInterface) docModel.getCoreSession();
    	session.saveDocument(docModel);
    	result = true;
    	
    	return result;
    }
    
    /**
     * Mark the authority item as deprecated.
     * 
     * @param ctx
     * @param itemInfo
     * @throws Exception
     */
    static public boolean markAuthorityItemAsDeprecated(ServiceContext ctx, String authorityItemCommonSchemaName, String itemCsid) throws Exception {
    	boolean result = false;
    	
    	try {
	    	DocumentModel docModel = NuxeoUtils.getDocFromCsid(ctx, (CoreSessionInterface)ctx.getCurrentRepositorySession(), itemCsid);
	    	result = setAuthorityItemDeprecated(docModel, authorityItemCommonSchemaName, AuthorityServiceUtils.DEPRECATED);
    	} catch (Exception e) {
    		logger.warn(String.format("Could not mark item '%s' as deprecated.", itemCsid), e);
    		throw e;
    	}
    	
    	return result;
    }
}