package org.collectionspace.services.advancedsearch; 

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.AdvancedSearchClient;
//import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.jaxb.AbstractCommonList;


@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch {
	private CollectionObjectResource cor = new CollectionObjectResource();
	
	@GET
	public AbstractCommonList getList(@Context UriInfo uriInfo) {
		AbstractCommonList results = null;
		
		AbstractCommonList foo = cor.getList(uriInfo);
		
		return results;
	}
	
}