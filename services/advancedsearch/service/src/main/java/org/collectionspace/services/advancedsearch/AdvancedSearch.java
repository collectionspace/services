package org.collectionspace.services.advancedsearch; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.collectionspace.services.client.CollectionObjectProxy;
import org.collectionspace.services.client.CollectionSpaceClient;
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
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.RemoteClientConfig;
import org.collectionspace.services.config.tenant.RemoteClientConfigurations;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;


@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem,AdvancedsearchListItem> {
	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);

	@GET
	public AbstractCommonList getList(@Context UriInfo uriInfo) {
		logger.info("advancedsearch called with path: {}", uriInfo.getPath());
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters(true);
		logger.info("advancedsearch called with query params: {}", queryParams);
		
		// the list to return
		ObjectFactory objectFactory = new ObjectFactory();
		AdvancedsearchCommonList resultsList = objectFactory.createAdvancedsearchCommonList();
		// FIXME: this shouldn't be necessary?
		resultsList.advancedsearchListItem = new ArrayList<AdvancedsearchListItem>();
		
		// the logic here is to use CollectionObjectResource to perform the search, then use
		// CollectionObjectClient to retrieve corresponding CollectionobjectsCommon objects, which have more fields
		CollectionObjectResource cor = new CollectionObjectResource();
		AbstractCommonList collectionObjectList = cor.getList(uriInfo);
		List<ListItem> collectionObjectListItems = collectionObjectList.getListItem();
		CollectionObjectClient client = null;
		try {		
			client = new CollectionObjectClient();
		} catch (Exception e) {
			// FIXME need better error handling
			logger.error("advancedsearch: could not create CollectionObjectClient",e);
			return resultsList;
		}
		HashMap<String, String> collectionObjectValuesMap = new HashMap<String,String>();
		for(ListItem item: collectionObjectListItems) {
			List<Element> els = item.getAny();
			for(Element el: els) {
		        String elementName = el.getTagName();
		        String elementText = el.getTextContent();
		        collectionObjectValuesMap.put(elementName, elementText);
			}
			String csid = collectionObjectValuesMap.get("csid");
			/*
			 * NOTE code below is derived from CollectionObjectServiceTest.readCollectionObjectCommonPart and AbstractPoxServiceTestImpl
			 */
	        Response res = client.read(csid);
	        CollectionobjectsCommon collectionObject = null;
			PoxPayloadIn input = null;
			try {
				String responseXml = res.readEntity(String.class);
				input = new PoxPayloadIn(responseXml);
			} catch (DocumentException e) {
				// FIXME need better error handling
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
				// FIXME: virtually everything below could blow up!
				// FIXME: implement the correct logic that Jessi wrote up here: https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
				AdvancedsearchListItem listItem = objectFactory.createAdvancedsearchCommonListAdvancedsearchListItem();
				listItem.setBriefDescription(collectionObject.getBriefDescriptions().getBriefDescription().get(0));
				listItem.setComputedCurrentLocation(collectionObject.getComputedCurrentLocation());
				listItem.setCsid(collectionObjectValuesMap.get("csid"));
				listItem.setObjectId(collectionObjectValuesMap.get("objectId"));
				listItem.setObjectName(collectionObject.getObjectNameList().getObjectNameGroup().get(0).getObjectName());
				listItem.setObjectTitle(collectionObject.getTitleGroupList().getTitleGroup().get(0).getTitle());
				listItem.setUri(collectionObjectValuesMap.get("uri"));
				
				// populate responsibleDepartments list
				ResponsibleDepartmentsList responsibleDepartmentList = objectFactory.createResponsibleDepartmentsList();
				// FIXME: where are the other fields?
				List<String> responsibleDepartmentNames = collectionObject.getResponsibleDepartments().getResponsibleDepartment();
				for(String responsibleDepartmentName : responsibleDepartmentNames) {
					ResponsibleDepartment responsibleDepartment = objectFactory.createResponsibleDepartment();
					responsibleDepartment.setName(responsibleDepartmentName);
					responsibleDepartmentList.getResponsibleDepartment().add(responsibleDepartment);
				}
				listItem.setResponsibleDepartments(responsibleDepartmentList);
				
				// add the populated item to the results
				resultsList.advancedsearchListItem.add(listItem);
			}
			else {
				logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}",csid);
			}
			res.close();
		}
		
		// NOTE: I think this is necessary for the front end to know what to do with what's returned (?)
		// FIXME: need better values for all these hardcoded numbers and fields
		// FIXME: we've not implemented anything that'd allow paging
		AbstractCommonList abstractList = (AbstractCommonList)resultsList;
		abstractList.setItemsInPage(collectionObjectListItems.size());
		abstractList.setPageNum(0);
		abstractList.setPageSize(collectionObjectListItems.size());
		abstractList.setTotalItems(collectionObjectListItems.size());
		abstractList.setFieldsReturned("uri|csid|objectId|objectName|objectTitle|computedCurrentLocation|responsibleDepartments|briefDescription");
		
		return resultsList;
	}

	@Override
	public Class<?> getCommonPartClass() {
		return null;
	}

	@Override
	public ServiceContextFactory<AdvancedsearchListItem, AdvancedsearchListItem> getServiceContextFactory() {
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