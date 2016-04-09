package org.collectionspace.services.common.vocabulary.nuxeo;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorityServiceUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthorityIdentifierUtils.class);

    static public PoxPayloadIn getPayloadIn(AuthorityItemSpecifier specifier, String serviceName, Class responseType) throws Exception {
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