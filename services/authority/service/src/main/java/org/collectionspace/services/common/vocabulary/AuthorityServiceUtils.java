package org.collectionspace.services.common.vocabulary;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityIdentifierUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorityServiceUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthorityIdentifierUtils.class);

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
        Response res = client.read(specifier.value);
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
    
    static public PoxPayloadIn requestPayloadIn(AuthorityItemSpecifier specifier, String serviceName, Class responseType) throws Exception {
    	PoxPayloadIn result = null;
    	
    	ServiceContext parentCtx = new MultipartServiceContextImpl(serviceName);
        AuthorityClient client = (AuthorityClient) parentCtx.getClient();
        Response res = client.readItem(specifier.getParentSpecifier().value, specifier.getItemSpecifier().value);
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
}