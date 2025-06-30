package org.collectionspace.services.advancedsearch; 

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.AdvancedSearchClient;
//import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.jaxb.AbstractCommonList;


@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch extends AbstractCollectionSpaceResourceImpl<Integer,String> {
	private CollectionObjectResource cor = new CollectionObjectResource();
	
	@GET
	public AbstractCommonList getList(@Context UriInfo uriInfo) {
		AbstractCommonList results = new AbstractCommonList();
		AbstractCommonList foo = cor.getList(uriInfo);
		return results;
	}

	@Override
	public Class<?> getCommonPartClass() {
		return AdvancedsearchCommon.class;
	}

	@Override
	public ServiceContextFactory<Integer, String> getServiceContextFactory() {
		return (ServiceContextFactory<Integer, String>) RemoteServiceContextFactory.get();
	}

	@Override
	public String getServiceName() {
		return AdvancedSearchClient.SERVICE_NAME;
	}

	@Override
	protected String getVersionString() {
		return "0.01";
	}
	
}