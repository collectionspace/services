package org.collectionspace.services.advancedsearch; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.client.AdvancedSearchClient;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectDocumentModelHandler;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.dom4j.DocumentException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem,AdvancedsearchListItem> {
	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);

	@GET
	public AbstractCommonList getList(@Context UriInfo uriInfo) {
		logger.info("advancedsearch called with path: {}", uriInfo.getPath());
		logger.info("advancedsearch called with query params: {}", uriInfo.getQueryParameters(true));
		
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
		AdvancedsearchListItem mockItem = new AdvancedsearchListItem();
		mockItem.setBriefDescription("This is a mock brief description from advanced search");
		mockItem.setComputedCurrentLocation("Mock location");
		mockItem.setCsid("mockcsid");
		mockItem.setObjectId("mock Object Id");
		mockItem.setObjectName("mock Object Name");
		mockItem.setObjectTitle("mock Object Title");
		mockItem.setUri("http://mock.uri/uri");
		
		// mock responsible department
		ResponsibleDepartmentsList mockResponsibleDepartmentsList = new ResponsibleDepartmentsList();
		ResponsibleDepartment mockResponsibleDepartment = new ResponsibleDepartment();
		mockResponsibleDepartment.setCsid("mock responsible department csid");
		mockResponsibleDepartment.setName("mock responsible department name");
		mockResponsibleDepartment.setRefName("mock responsible department refname");
		mockResponsibleDepartment.setUri("http://mock.url/responsibleDepartment");
		mockResponsibleDepartmentsList.getResponsibleDepartment().add(mockResponsibleDepartment);
		
		ResponsibleDepartment mockResponsibleDepartment2 = new ResponsibleDepartment();
		mockResponsibleDepartment2.setCsid("mock responsible department2 csid");
		mockResponsibleDepartment2.setName("mock responsible department2 name");
		mockResponsibleDepartment2.setRefName("mock responsible department2 refname");
		mockResponsibleDepartment2.setUri("http://mock.url/responsibleDepartment2");
		mockResponsibleDepartmentsList.getResponsibleDepartment().add(mockResponsibleDepartment2);
		
		mockItem.setResponsibleDepartments(mockResponsibleDepartmentsList);
		resultsList.getAdvancedsearchListItem().add(mockItem);
		
		// NOTE: I think this is necessary for the front end to know what to do with what's returned (?)
		AbstractCommonList abstractList = (AbstractCommonList)resultsList;
		abstractList.setItemsInPage(1);
		abstractList.setPageNum(0);
		abstractList.setPageSize(100);
		abstractList.setTotalItems(1);
		abstractList.setFieldsReturned("uri|csid|objectId|objectName|objectTitle|computedCurrentLocation|responsibleDepartments|briefDescription");
		
		// an experiment: this works fine; you can send e.g. ?kw=note and it'll return the correct collectionobjects
		// fields=csid|uri|refName|updatedAt|workflowState|objectNumber|objectName|title|responsibleDepartment
		CollectionObjectResource cor = new CollectionObjectResource();
		AbstractCommonList collectionObjectsList = cor.getList(uriInfo);
		List<ListItem> listItems = collectionObjectsList.getListItem();
		CollectionObjectClient client = null;
		try {
			client = new CollectionObjectClient();
		} catch (Exception e) {
			// FIXME need better handling
			logger.error("advancedsearch: could not create CollectionObjectClient",e);
			return resultsList;
		}
		for(ListItem item: listItems) {
			List<Element> els = item.getAny();
			String csid = "";
			for(Element el: els) {
				String elementName = el.getTagName();
				String elementText = el.getTextContent();
				if(elementName.equals("csid")) {
					// FIXME need better logic
					csid = elementText;
					break;
				}
			}
			/*
			 * NOTE code below is derived from CollectionObjectServiceTest.readCollectionObjectCommonPart and AbstractPoxServiceTestImpl
			 */
	        Response res = client.read(csid);
	        CollectionobjectsCommon collectionObject = null;
			PoxPayloadIn input = null;
			try {
				input = new PoxPayloadIn((String)res.readEntity(String.class));
			} catch (DocumentException e) {
				// FIXME need better handling
				logger.error("advancedsearch: could not create PoxPayloadIn",e);
				continue;
			}
			if(null != input) {
	            PayloadInputPart payloadInputPart = input.getPart(client.getCommonPartName());
				if (null != payloadInputPart) {
					collectionObject = (CollectionobjectsCommon) payloadInputPart.getBody();
				}				
			}
			if(null != collectionObject) {
				logger.info("advancedsearch: found CollectionobjectsCommon associated with csid {}",csid);
				logger.info("advancedsearch: computed current location:",collectionObject.getComputedCurrentLocation());
			}
			else {
				logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}",csid);
			}
		}

		
		/*
		 * Class<CollectionobjectsCommon> commonPartClass = cor.getCommonPartClass();
		 * ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx; try { ctx =
		 * cor.getServiceContextFactory().createServiceContext(cor.getServiceName(),
		 * uriInfo); } catch (Exception e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } RepositoryClient<PoxPayloadIn, PoxPayloadOut>
		 * repoClient = cor.getRepositoryClient(ctx);
		 * CollectionObjectDocumentModelHandler codmh =
		 * (CollectionObjectDocumentModelHandler) cor.createDocumentHandler(ctx);
		 * codmh.setCommonPartList(collectionObjectsList);
		 */

		
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