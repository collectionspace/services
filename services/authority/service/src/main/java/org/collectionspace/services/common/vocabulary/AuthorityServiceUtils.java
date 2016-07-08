package org.collectionspace.services.common.vocabulary;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityIdentifierUtils;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.RemoteClientConfig;
import org.collectionspace.services.config.tenant.RemoteClientConfigurations;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.common.document.DocumentException;

//import org.dom4j.DocumentException;
import org.eclipse.jetty.http.HttpStatus;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class AuthorityServiceUtils {
    private static final Logger logger = LoggerFactory.getLogger(AuthorityIdentifierUtils.class);
    //
    // Used to keep track if an authority item's is deprecated
    public static final String DEFAULT_REMOTECLIENT_CONFIG_NAME = "default";
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

    /*
     * Try to find a named remote client configuration in the current tenant bindings.  If the value of the incoming param 'remoteClientConfigName' is
     * blank or null, we'll try to find a name in the authority service's bindings.  If we can't find a name there, we'll try using the default name.
     * 
     * If the incoming param 'remoteClientConfigName' is not null, we'll look through all the named remote client configurations in the tenant's binding
     * to find the configuration.  If we can't find the named configuration, we'll throw an exception.
     * 
     * If there are no remote client configurations in the tenant's bindings, we'll throw an exception.
     */
	public static final RemoteClientConfig getRemoteClientConfig(ServiceContext ctx, String remoteClientConfigName) throws Exception {
    	RemoteClientConfig result = null;
    	
    	TenantBindingType tenantBinding = ServiceMain.getInstance().getTenantBindingConfigReader().getTenantBinding(ctx.getTenantId());
    	RemoteClientConfigurations remoteClientConfigurations = tenantBinding.getRemoteClientConfigurations();
    	if (remoteClientConfigurations != null) {
    		if (Tools.isEmpty(remoteClientConfigName) == true) {
    			// Since the authority instance didn't specify a remote client config name, let's see if the authority type's service bindings specifies one
    			ServiceBindingType serviceBindingType =
    					ServiceMain.getInstance().getTenantBindingConfigReader().getServiceBinding(ctx.getTenantId(), ctx.getServiceName());
    			remoteClientConfigName = serviceBindingType.getRemoteClientConfigName();
    		}
    		//
    		// If we still don't have a remote client config name, let's use the default value.
    		//
    		if (Tools.isEmpty(remoteClientConfigName) == true) {
    			remoteClientConfigName = DEFAULT_REMOTECLIENT_CONFIG_NAME;
    		}
    		
    		List<RemoteClientConfig> remoteClientConfigList = remoteClientConfigurations.getRemoteClientConfig();
    		for (RemoteClientConfig config : remoteClientConfigList) {
    			if (config.getName().equalsIgnoreCase(remoteClientConfigName)) {
    				result = config;
    				break;
    			}
    		}
    	} else {
    		String errMsg = String.format("No remote client configurations could be found in the tenant bindings for tenant named '%s'.",
    				ctx.getTenantName());
    		logger.error(errMsg);
    		throw new Exception(errMsg);
    	}
    	
    	if (result == null) {
    		String errMsg = String.format("Could not find a remote client configuration named '%s' in the tenant bindings for tenant named '%s'",
    				remoteClientConfigName, ctx.getTenantName());
    		logger.error(errMsg);
    		throw new Exception(errMsg);
    	}
    	
    	return result;
    }
    
    /**
     * Make a request to the SAS Server for an authority payload.
     * 
     * @param ctx
     * @param specifier
     * @param responseType
     * @return
     * @throws Exception
     */
    static public PoxPayloadIn requestPayloadInFromRemoteServer(ServiceContext ctx, String remoteClientConfigName, Specifier specifier, Class responseType) throws Exception {
    	PoxPayloadIn result = null;
    	
    	RemoteClientConfig remoteClientConfig = getRemoteClientConfig(ctx, remoteClientConfigName);
        AuthorityClient client = (AuthorityClient) ctx.getClient(remoteClientConfig);
        
        Response res = client.read(specifier.getURNValue());
        try {
	        int statusCode = res.getStatus();
	        if (statusCode == HttpStatus.OK_200) {
	            result = new PoxPayloadIn((String)res.readEntity(responseType)); // Get the entire response!	        	        	
	        } else {
	        	String errMsg = String.format("Could not retrieve authority information for '%s' on remote server '%s'.  Server returned status code %d",
	        			specifier.getURNValue(), remoteClientConfig.getUrl(), statusCode);
		        if (logger.isDebugEnabled()) {
		            logger.debug(errMsg);
		        }
		        throw new DocumentException(statusCode, errMsg);
	        }
        } finally {
        	res.close();
        }
    	
    	return result;
    }
    
    //
    // Makes a call to the remote SAS server for a authority item payload
    //    
    static public PoxPayloadIn requestPayloadInFromRemoteServer(
    		AuthorityItemSpecifier specifier, 
    		String remoteClientConfigName, 
    		String serviceName, 
    		Class responseType, 
    		boolean syncHierarchicalRelationships) throws Exception {
    	PoxPayloadIn result = null;
    	
    	ServiceContext authorityCtx = new MultipartServiceContextImpl(serviceName);
    	RemoteClientConfig remoteClientConfig = getRemoteClientConfig(authorityCtx, remoteClientConfigName);
        AuthorityClient client = (AuthorityClient) authorityCtx.getClient(remoteClientConfig);
        Response res = client.readItem(specifier.getParentSpecifier().getURNValue(), specifier.getItemSpecifier().getURNValue(),
    			AuthorityClient.INCLUDE_DELETED_ITEMS, syncHierarchicalRelationships);
        
        try {
	        int statusCode = res.getStatus();
	        if (statusCode == HttpStatus.OK_200) {
	            result = new PoxPayloadIn((String)res.readEntity(responseType)); // Get the entire response.
	        } else {
	        	String errMsg = String.format("Could not retrieve authority item information for '%s:%s' on remote server '%s'.  Server returned status code %d",
	        			specifier.getParentSpecifier().getURNValue(), specifier.getItemSpecifier().getURNValue(), remoteClientConfig.getUrl(), statusCode);
		        if (logger.isDebugEnabled()) {
		            logger.debug(errMsg);
		        }
		        throw new DocumentException(statusCode, errMsg);
	        }	        
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
			PoxPayloadIn payload) throws org.dom4j.DocumentException {
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