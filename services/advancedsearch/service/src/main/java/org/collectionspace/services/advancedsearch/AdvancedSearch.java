package org.collectionspace.services.advancedsearch; 

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.client.AdvancedSearchClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem,AdvancedsearchListItem> {
	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);
	
	/*
	 * // Here's a pattern for retrieving objects from another service:
	 * private CollectionObjectResource cor = new CollectionObjectResource();
	 * AbstractCommonList collectionObjectsList = cor.getList(uriInfo);
	 */
	
	@GET
	public AbstractCommonList getList(@Context UriInfo uriInfo) {
		logger.info("advancedsearch called with uriInfo: {}", uriInfo);
		
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
		/*
		 *  extract whatever else is needed from query params to fulfill request
		 *  in many cases we'll probably just call another service and pass uriInfo to
		 *  it so we may not need to do stuff like this; it's here for now to logging/testing
		 *  purposes
		 */
		logger.info("advancedsearch called with keywords: {}", keywords);
		
		// Build a mock list to return. We will eventually populate this with real
		// data retrieved from other services and/or the database
		AdvancedsearchCommonList resultsList = new AdvancedsearchCommonList();
		List<AdvancedsearchListItem> results = resultsList.advancedsearchListItem;
		AdvancedsearchListItem mockItem = new AdvancedsearchListItem();
		mockItem.setBriefDescription("This is a mock brief description from advanced search");
		mockItem.setComputedCurrentLocation("Mock location");
		mockItem.setCsid("mockcsid");
		mockItem.setObjectId("mock Object Id");
		mockItem.setObjectName("mock Object Name");
		mockItem.setObjectTitle("mock Object Title");
		mockItem.setUri("http://mock.uri/uri");
		results.add(mockItem);
		
		// NOTE: I think this is necessary for the front end to know what to do with what's returned (?)
		AbstractCommonList abstractList = (AbstractCommonList)results;
		abstractList.setItemsInPage(1);
		abstractList.setPageNum(0);
		abstractList.setPageSize(100);
		abstractList.setTotalItems(1);
		abstractList.setFieldsReturned("uri|csid|objectId|objectName|objectTitle|computedCurrentLocation|responsibleDepartments|briefDescription");
		
		return resultsList;
	}

	@Override
	public Class<?> getCommonPartClass() {
		return null;
	}

	@Override
	public ServiceContextFactory<AdvancedsearchListItem, AdvancedsearchListItem> getServiceContextFactory() {
		// TODO not used?
		return (ServiceContextFactory<AdvancedsearchListItem, AdvancedsearchListItem>) RemoteServiceContextFactory.get();
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