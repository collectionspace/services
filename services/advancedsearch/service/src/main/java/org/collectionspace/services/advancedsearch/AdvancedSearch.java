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
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.media.MediaResource;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem,AdvancedsearchListItem> {
	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);
	private final CollectionObjectResource cor = new CollectionObjectResource();
	private CollectionObjectClient collectionObjectClient = null;
	private final MediaResource mr = new MediaResource();
		
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
		// TODO the resource and client are both singletons, are they not? If so we should create them once rather than at each call to getList
		AbstractCommonList collectionObjectList = cor.getList(uriInfo);
		List<ListItem> collectionObjectListItems = collectionObjectList.getListItem();
		try {		
			collectionObjectClient = new CollectionObjectClient();
			collectionObjectClient.setupHttpClient4();
		} catch (Exception e) {
			// FIXME need better error handling
			logger.error("advancedsearch: could not create CollectionObjectClient",e);
			return resultsList;
		}
		
		// FIXME: is there no better way to do this?
		HashMap<String, String> collectionObjectValuesMap = new HashMap<String,String>();
		for(ListItem item: collectionObjectListItems) {
			List<Element> els = item.getAny();
			for(Element el: els) {
		        String elementName = el.getTagName();
		        String elementText = el.getTextContent();
		        collectionObjectValuesMap.put(elementName, elementText);
			}
			String csid = collectionObjectValuesMap.get("csid");
			UriInfoWrapper wrappedUriInfo = new UriInfoWrapper(uriInfo);
			List<String> blobCsids = findBlobCsids(csid, wrappedUriInfo);
			
			/*
			 * NOTE code below is derived from CollectionObjectServiceTest.readCollectionObjectCommonPart and AbstractPoxServiceTestImpl
			 */
	        Response res = collectionObjectClient.read(csid);
	        int statusCode = res.getStatus();
	        logger.warn("advancedsearch: call to CollectionObjectClient returned status = ",statusCode);
	        CollectionobjectsCommon collectionObject = null;
			PoxPayloadIn input = null;
			try {
				String responseXml = res.readEntity(String.class);
				input = new PoxPayloadIn(responseXml);
				logger.warn("advancedsearch: call to CollectionObjectClient returned XML = ",responseXml);
			} catch (DocumentException e) {
				// FIXME need better error handling
				logger.error("advancedsearch: could not create PoxPayloadIn",e);
				continue;
			}
			if(null != input) {
	            PayloadInputPart payloadInputPart = input.getPart(collectionObjectClient.getCommonPartName());
				if (null != payloadInputPart) {
					collectionObject = (CollectionobjectsCommon) payloadInputPart.getBody();
				}
			}
			if(null != collectionObject) {
				AdvancedsearchListItem listItem = objectFactory.createAdvancedsearchCommonListAdvancedsearchListItem();
				listItem.setBriefDescription(BriefDescriptionListModel.briefDescriptionListToDisplayString(collectionObject.getBriefDescriptions()));
				listItem.setComputedCurrentLocation(collectionObject.getComputedCurrentLocation()); // "Computed Current Location: Display full string" from https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
				listItem.setObjectName(ObjectNameListModel.objectNameListToDisplayString(collectionObject.getObjectNameList()));
				listItem.setTitle(TitleGroupListModel.titleGroupListToDisplayString(collectionObject.getTitleGroupList()));
				ResponsibleDepartmentsList rdl = ResponsibleDepartmentsListModel.responsibleDepartmentListToResponsibleDepartmentsList(collectionObject.getResponsibleDepartments());
				listItem.setResponsibleDepartments(rdl);
				if(null != rdl && null != rdl.responsibleDepartment && rdl.responsibleDepartment.size() > 0) {
					ResponsibleDepartment rd = rdl.responsibleDepartment.get(0);
					listItem.setResponsibleDepartment(rd.name);
				}
				
				// from media resource
				if(blobCsids.size() > 0) {
					listItem.setBlobCsid(blobCsids.get(0));
				}
				
				// from collectionobject
				listItem.setCsid(collectionObjectValuesMap.get("csid"));
				listItem.setObjectId(collectionObjectValuesMap.get("objectId")); // "Identification Number: Display full string" from https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
				listItem.setUri(collectionObjectValuesMap.get("uri"));
				try {
					XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(collectionObjectValuesMap.get("updatedAt"));
					listItem.setUpdatedAt(date); // "Last Updated Date: Display Date, if updated same day can we display x number of hours ago" from https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
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
		AbstractCommonList abstractList = (AbstractCommonList) resultsList;
		abstractList.setItemsInPage(collectionObjectList.getItemsInPage());
		abstractList.setPageNum(collectionObjectList.getPageNum());
		abstractList.setPageSize(collectionObjectList.getPageSize());
		abstractList.setTotalItems(collectionObjectList.getTotalItems());
		// FIXME: is there a way to generate this rather than hardcode it?
		abstractList.setFieldsReturned("uri|csid|blobCsid|updatedAt|objectId|objectName|title|computedCurrentLocation|responsibleDepartments|responsibleDepartment|briefDescription");
		
		return resultsList;
	}

	private List<String> findBlobCsids(String csid, UriInfoWrapper wrappedUriInfo) {
		// FIXME: is there no better way to do this?
		MultivaluedMap<String, String> wrappedQueryParams = wrappedUriInfo.getQueryParameters();
		wrappedQueryParams.clear();
		wrappedQueryParams.add(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT, csid);
		wrappedQueryParams.add("pgSz", "1");
		wrappedQueryParams.add("pgNum", "0");
		wrappedQueryParams.add("sortBy", "title");
		AbstractCommonList associatedMedia = mr.getList(wrappedUriInfo);
		HashMap<String,String> mediaResourceValuesMap = new HashMap<String,String>();
		ArrayList<String> blobCsids = new ArrayList<String>();
		for(ListItem item : associatedMedia.getListItem()) {
			List<Element> els = item.getAny();
			for(Element el: els) {
				String elementName = el.getTagName();
				String elementText = el.getTextContent();
				mediaResourceValuesMap.put(elementName, elementText);
			}
			String blobCsid = mediaResourceValuesMap.get("blobCsid");
			if(null != blobCsid) {
				blobCsids.add(blobCsid);
			}
		}
		return blobCsids;
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