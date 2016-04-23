package org.collectionspace.services.common.vocabulary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityIdentifierUtils;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.DocumentException;
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
    public static final Boolean SAS_ITEM = true;
    public static final Boolean NOT_SAS_ITEM = !SAS_ITEM;

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
    
    static public boolean setAuthorityItemDeprecated(ServiceContext ctx,
    		DocumentModel docModel, String authorityItemCommonSchemaName, Boolean flag) throws Exception {
    	boolean result = false;
    	
    	docModel.setProperty(authorityItemCommonSchemaName, AuthorityItemJAXBSchema.DEPRECATED,
    			new Boolean(flag));
    	CoreSessionInterface repoSession = (CoreSessionInterface) ctx.getCurrentRepositorySession();
    	repoSession.saveDocument(docModel);
    	result = true;
    	
    	return result;
    }
    
    /*
     * The domain name part of refnames on SAS may not match that of local refnames, so we need to update all the payload's
     * refnames with the correct domain name
     */
	static public PoxPayloadIn filterRefnameDomains(ServiceContext ctx,
			PoxPayloadIn payload) throws DocumentException {
		PoxPayloadIn result = null;

		
		String payloadStr = payload.getXmlPayload();
		Pattern p = Pattern.compile("(urn:cspace:)(([a-z]{1,}\\.?)*)"); // matches the domain name part of a RefName.  For example, matches "core.collectionspace.org" of RefName urn:cspace:core.collectionspace.org:personauthorities:name(person):item:name(BigBird1461101206103)'Big Bird'
		Matcher m = p.matcher(payloadStr);

		StringBuffer filteredPayloadStr = new StringBuffer();
		while (m.find() == true) {
			if (logger.isDebugEnabled()) {
				logger.debug("Replacing: " + m.group(2));
			}
			m.appendReplacement(filteredPayloadStr, m.group(1) + ctx.getTenantName());
		}
		m.appendTail(filteredPayloadStr);
		result = new PoxPayloadIn(filteredPayloadStr.toString());

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("", filteredPayloadStr));
		}

		return result;
	}
    
    /**
     * Mark the authority item as deprecated.
     * 
     * @param ctx
     * @param itemInfo
     * @throws Exception
     */
    static public boolean markAuthorityItemAsDeprecated(ServiceContext ctx, String authorityItemCommonSchemaName, AuthorityItemSpecifier authorityItemSpecifier) throws Exception {
    	boolean result = false;
    	
    	try {
	    	DocumentModel docModel = NuxeoUtils.getDocFromSpecifier(ctx, (CoreSessionInterface)ctx.getCurrentRepositorySession(),
	    			authorityItemCommonSchemaName, authorityItemSpecifier);
	    	result = setAuthorityItemDeprecated(ctx, docModel, authorityItemCommonSchemaName, AuthorityServiceUtils.DEPRECATED);
    	} catch (Exception e) {
    		logger.warn(String.format("Could not mark item '%s' as deprecated.", authorityItemSpecifier.getItemSpecifier().getURNValue()), e);
    		throw e;
    	}
    	
    	return result;
    }
}