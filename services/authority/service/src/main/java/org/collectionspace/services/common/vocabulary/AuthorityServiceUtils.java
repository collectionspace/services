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
    //
    // Revision property statics
    //
    public static final String SHOULD_UPDATE_REV_PROPERTY = "SHOULD_UPDATE_REV_PROPERTY";
    public static final boolean DONT_UPDATE_REV = false;
    public static final boolean UPDATE_REV = true;

    //
    // Makes a call to the SAS server for a authority payload
    //
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
}