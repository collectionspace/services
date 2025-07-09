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
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.advancedsearch.model.BriefDescriptionListModel;
import org.collectionspace.services.advancedsearch.model.ObjectNameListModel;
import org.collectionspace.services.advancedsearch.model.ResponsibleDepartmentsListModel;
import org.collectionspace.services.advancedsearch.model.TitleGroupListModel;
import org.collectionspace.services.client.AdvancedSearchClient;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
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
				listItem.setBriefDescription(BriefDescriptionListModel.briefDescriptionListToDisplayString(collectionObject.getBriefDescriptions()));
				listItem.setComputedCurrentLocation(collectionObject.getComputedCurrentLocation());
				listItem.setObjectName(ObjectNameListModel.objectNameListToDisplayString(collectionObject.getObjectNameList()));
				listItem.setObjectTitle(TitleGroupListModel.titleGroupListToDisplayString(collectionObject.getTitleGroupList()));
				listItem.setResponsibleDepartments(ResponsibleDepartmentsListModel.responsibleDepartmentListToResponsibleDepartmentsList(collectionObject.getResponsibleDepartments()));
				
				// from collectionobject
				listItem.setCsid(collectionObjectValuesMap.get("csid"));
				listItem.setObjectId(collectionObjectValuesMap.get("objectId"));
				listItem.setUri(collectionObjectValuesMap.get("uri"));
				try {
					XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(collectionObjectValuesMap.get("updatedAt"));
					listItem.setUpdatedAt(date);
				} catch (DatatypeConfigurationException e) {
					// FIXME need better error handling
					logger.error("advancedsearch: could not create XMLGregorianCalendar for updatedAt ",e);
					logger.error("advancedsearch: updatedAt = ",collectionObjectValuesMap.get("updatedAt"));
				}
				
				// add the populated item to the results
				resultsList.getAdvancedsearchListItem().add(listItem);
			}
			else {
				logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}",csid);
			}
			res.close();
		}
		
		// NOTE: I think this is necessary for the front end to know what to do with what's returned (?)
		// FIXME: need better values for all these hardcoded numbers and fields
		// FIXME: we've not implemented anything that'd allow paging
		AbstractCommonList abstractList = (AbstractCommonList) resultsList;
		abstractList.setItemsInPage(collectionObjectListItems.size());
		abstractList.setPageNum(0);
		abstractList.setPageSize(collectionObjectListItems.size());
		abstractList.setTotalItems(collectionObjectListItems.size());
		abstractList.setFieldsReturned("uri|csid|updatedAt|objectId|objectName|objectTitle|computedCurrentLocation|responsibleDepartments|briefDescription");
		
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