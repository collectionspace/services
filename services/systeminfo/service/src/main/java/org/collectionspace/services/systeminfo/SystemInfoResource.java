package org.collectionspace.services.systeminfo;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.HttpMethod;

import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceAction;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.security.UnauthorizedException;

@Path(SystemInfoClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class SystemInfoResource extends AbstractCollectionSpaceResourceImpl<SystemInfoCommon, SystemInfoCommon> {

	@Override
	public Class<?> getCommonPartClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServiceName() {
		return SystemInfoClient.SERVICE_NAME;
	}

	@Override
	protected String getVersionString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	//
	// API Endpoints
	//
	
    @GET
    public SystemInfoCommon get(@Context UriInfo ui) {
    	SystemInfoCommon result = null;

    	try {
    		result = new SystemInfoCommon();
    		result.setInstanceId("_default");
    		result.setDisplayName("CollectionSpace Services v5.1-1@UCB");
    		Version ver = new Version();
    		ver.setMajor("5");
    		ver.setMinor("1");
    		ver.setPatch("0");
    		ver.setBuild("1");
    		result.setVersion(ver);
    		
    		result.setHostTimezone(TimeZone.getDefault().getID());
    		result.setHostLocale(Locale.getDefault().toLanguageTag());
    		result.setHostCharset(Charset.defaultCharset().name());
    		//
    		// To get the full set of the system information, we required the user be authenticated *and* have "DELETE" privs on the "systeminfo" resource
    		//
    		try {
    			ServiceContext<SystemInfoCommon, SystemInfoCommon> ctx = createServiceContext(getServiceName(), ui);
    			CSpaceResource res = new URIResourceImpl(ctx.getTenantId(), SystemInfoClient.SERVICE_NAME, HttpMethod.DELETE);
    			if (AuthZ.get().isAccessAllowed(res)) {
    	    		result.setNuxeoVersionString("7.10-HF17");
    	    		result.setHost(String.format("Architecture:%s Name:%s Version:%s",
    	    				System.getProperty("os.arch"), System.getProperty("os.name"), System.getProperty("os.version")));
    	    		result.setJavaVersionString(System.getProperty("java.version"));
    	    		result.setPostgresVersionString("9.5.7");
    			}
    		} catch (UnauthorizedException e) {
    			logger.trace(e.getMessage(), e);
    		}

    	} catch(Exception e) {
    		Response response = Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).type("text/plain").build();
            throw new CSWebApplicationException(response);
    	}

    	return result;
    }
    
	@Override
	public ServiceContextFactory<SystemInfoCommon, SystemInfoCommon> getServiceContextFactory() {
        return (ServiceContextFactory<SystemInfoCommon, SystemInfoCommon>) RemoteServiceContextFactory.get();
	}
}
