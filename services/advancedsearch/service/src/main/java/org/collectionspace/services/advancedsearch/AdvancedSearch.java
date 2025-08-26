package org.collectionspace.services.advancedsearch;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.advancedsearch.model.BriefDescriptionListModel;
import org.collectionspace.services.advancedsearch.model.ContentConceptListModel;
import org.collectionspace.services.advancedsearch.model.ObjectNameListModel;
import org.collectionspace.services.advancedsearch.model.ResponsibleDepartmentsListModel;
import org.collectionspace.services.advancedsearch.model.TitleGroupListModel;
import org.collectionspace.services.client.AdvancedSearchClient;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.media.MediaResource;
import org.dom4j.DocumentException;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

// FIXME: The *Client pattern should be deprecated; it's only really used in unit tests, and trying to use it in services creates authorization challenges. It's repeated here only to maintain parity with existing services.
/**
 * This class defines the advanced search endpoints.
 */
@Path(AdvancedSearchClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch
		extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem, AdvancedsearchListItem> {
	private static final String FIELDS_RETURNED = "uri|csid|refName|blobCsid|updatedAt|objectId|objectNumber|objectName|title|computedCurrentLocation|responsibleDepartments|responsibleDepartment|contentConcepts|briefDescription";
	private static final String COMMON_PART_NAME = CollectionObjectClient.SERVICE_NAME + CollectionSpaceClient.PART_LABEL_SEPARATOR + CollectionSpaceClient.PART_COMMON_LABEL; // FIXME: it's not great to hardcode this here

	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);
	private final CollectionObjectResource cor = new CollectionObjectResource();
	private final MediaResource mr = new MediaResource();

	public AdvancedSearch() {
		super();
	}

	/**
	 * Primary advanced search API endpoint.
	 * 
	 * @param request The incoming request. Injected. 
	 * @param uriInfo The URI info of the incoming request, including query parameters and other search control parameters. Injected.
	 * @return A possibly-empty AbstractCommonList of the advanced search results corresponding to the query
	 */
	@GET
	public AbstractCommonList getList(@Context Request request, @Context UriInfo uriInfo) {
		logger.info("advancedsearch called with path: {}", uriInfo.getPath());
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters(true);
		logger.info("advancedsearch called with query params: {}", queryParams);

		// the list to return
		ObjectFactory objectFactory = new ObjectFactory();
		AdvancedsearchCommonList resultsList = objectFactory.createAdvancedsearchCommonList();
		// FIXME: this shouldn't be necessary?
		resultsList.advancedsearchListItem = new ArrayList<AdvancedsearchListItem>();

		// the logic here is to use CollectionObjectResource to perform the search, then
		// loop over the results retrieving corresponding CollectionobjectsCommon objects, 
		// which have more fields
		AbstractCommonList collectionObjectList = cor.getList(uriInfo);
		List<ListItem> collectionObjectListItems = collectionObjectList.getListItem();

		HashMap<String, String> collectionObjectValuesMap = new HashMap<String, String>();
		for (ListItem item : collectionObjectListItems) {
			// FIXME: is there no better way to do this? We should at least abstract this logic out of here
			List<Element> els = item.getAny();
			for (Element el : els) {
				String elementName = el.getTagName();
				String elementText = el.getTextContent();
				collectionObjectValuesMap.put(elementName, elementText);
			}
			String csid = collectionObjectValuesMap.get("csid");
			UriInfoWrapper wrappedUriInfo = new UriInfoWrapper(uriInfo);
			List<String> blobCsids = findBlobCsids(csid, wrappedUriInfo);

			/*
			 * NOTE: code below is partly based on
			 * CollectionObjectServiceTest.readCollectionObjectCommonPart and
			 * AbstractPoxServiceTestImpl
			 */
			ResourceMap resourceMap = ResteasyProviderFactory.getContextData(ResourceMap.class);
			Response res = cor.get(request, resourceMap, uriInfo, csid);
			int statusCode = res.getStatus();
			logger.warn("advancedsearch: call to CollectionObjectResource for csid {} returned status {}", csid, statusCode);
			CollectionobjectsCommon collectionObject = null;
			// FIXME: is there no better way to do this? We should at least abstract this logic out of here
			PoxPayloadIn input = null;
			try {
                String responseXml = new String((byte[]) res.getEntity(),StandardCharsets.UTF_8);
                input = new PoxPayloadIn(responseXml);
			} catch (DocumentException e) {
				// TODO: need better error handling
				logger.error("advancedsearch: could not create PoxPayloadIn", e);
				continue;
			}
			if (null != input) {
				PayloadInputPart payloadInputPart = input.getPart(COMMON_PART_NAME);
				if (null != payloadInputPart) {
					collectionObject = (CollectionobjectsCommon) payloadInputPart.getBody();
				}
			}
			// build up a listitem for the result list using the additional fields in CollectionObjectsCommon
			if (null != collectionObject) {
				AdvancedsearchListItem listItem = objectFactory.createAdvancedsearchCommonListAdvancedsearchListItem();
				listItem.setBriefDescription(BriefDescriptionListModel
						.briefDescriptionListToDisplayString(collectionObject.getBriefDescriptions()));
				// TODO: collectionObject.getComputedCurrentLocation() is (can be?) a refname.
				// code below extracts display name. there's probably something in RefName or
				// similar to do this kind of thing see also
				// ContentConceptListModel.displayNameFromRefName
				String currLoc = collectionObject.getComputedCurrentLocation();
				String currLocDisplayName = currLoc;
				if (null != currLoc && currLoc.indexOf("'") < currLoc.lastIndexOf("'")) {
					currLocDisplayName = currLoc.substring(currLoc.indexOf("'") + 1, currLoc.lastIndexOf("'"));
				}
				listItem.setComputedCurrentLocation(currLocDisplayName); // "Computed Current
																			// Location: Display
																			// full string" from
																			// https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
				listItem.setObjectName(
						ObjectNameListModel.objectNameListToDisplayString(collectionObject.getObjectNameList()));
				listItem.setTitle(
						TitleGroupListModel.titleGroupListToDisplayString(collectionObject.getTitleGroupList()));
				ResponsibleDepartmentsList rdl = ResponsibleDepartmentsListModel
						.responsibleDepartmentListToResponsibleDepartmentsList(
								collectionObject.getResponsibleDepartments());
				listItem.setResponsibleDepartments(rdl);
				listItem.setResponsibleDepartment(
						ResponsibleDepartmentsListModel.responsibleDepartmentsListDisplayString(rdl));

				listItem.setContentConcepts(
						ContentConceptListModel.contentConceptListDisplayString(collectionObject.getContentConcepts()));

				// from media resource
				if (blobCsids.size() > 0) {
					listItem.setBlobCsid(blobCsids.get(0));
				}

				// from collectionobject itself
				listItem.setCsid(collectionObjectValuesMap.get("csid"));
				listItem.setObjectId(collectionObjectValuesMap.get("objectId")); // "Identification Number: Display full
																					// string" from
																					// https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
				listItem.setObjectNumber(collectionObjectValuesMap.get("objectNumber"));
				listItem.setRefName(collectionObjectValuesMap.get("refName"));
				listItem.setUri(collectionObjectValuesMap.get("uri"));
				try {
					XMLGregorianCalendar date = DatatypeFactory.newInstance()
							.newXMLGregorianCalendar(collectionObjectValuesMap.get("updatedAt"));
					listItem.setUpdatedAt(date); // "Last Updated Date: Display Date, if updated same day can we display
													// x number of hours ago" from
													// https://docs.google.com/spreadsheets/d/103jyxa2oCtt8U0IQ25xsOyIxqwKvPNXlcCtcjGlT5tQ/edit?gid=0#gid=0
				} catch (DatatypeConfigurationException e) {
					// FIXME need better error handling
					logger.error("advancedsearch: could not create XMLGregorianCalendar for updatedAt ", e);
					logger.error("advancedsearch: updatedAt: {}", collectionObjectValuesMap.get("updatedAt"));
				}

				// add the populated item to the results
				resultsList.getAdvancedsearchListItem().add(listItem);
			} else {
				logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}", csid);
			}
			res.close();
		}

		// NOTE: I think this is necessary for the front end to know what to do with
		// what's returned (?)
		AbstractCommonList abstractList = (AbstractCommonList) resultsList;
		abstractList.setItemsInPage(collectionObjectList.getItemsInPage());
		abstractList.setPageNum(collectionObjectList.getPageNum());
		abstractList.setPageSize(collectionObjectList.getPageSize());
		abstractList.setTotalItems(collectionObjectList.getTotalItems());
		// FIXME: is there a way to generate this rather than hardcode it?
		abstractList.setFieldsReturned(FIELDS_RETURNED);

		return resultsList;
	}

	/** 
	 * Retrieves the blob CSIDs associated with a given object's CSID
	 * 
	 * @param csid The CSID of an object whose associated blobs (thumbnails) is desired
	 * @param wrappedUriInfo The wrapped (mutable) UriInfo of the incoming query that ultimately triggered this call
	 * @return A possibly-empty list of strings of the blob CSIDs associated with CSID
	 */
	private List<String> findBlobCsids(String csid, UriInfoWrapper wrappedUriInfo) {
		MultivaluedMap<String, String> wrappedQueryParams = wrappedUriInfo.getQueryParameters();
		wrappedQueryParams.clear();
		wrappedQueryParams.add(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT, csid);
		wrappedQueryParams.add("pgSz", "1");
		wrappedQueryParams.add("pgNum", "0");
		wrappedQueryParams.add("sortBy", "media_common:title");
		AbstractCommonList associatedMedia = mr.getList(wrappedUriInfo);
		HashMap<String, String> mediaResourceValuesMap = new HashMap<String, String>();
		ArrayList<String> blobCsids = new ArrayList<String>();
		for (ListItem item : associatedMedia.getListItem()) {
			// FIXME: is there no better way to do this? we should at least abstract out this logic
			List<Element> els = item.getAny();
			for (Element el : els) {
				String elementName = el.getTagName();
				String elementText = el.getTextContent();
				mediaResourceValuesMap.put(elementName, elementText);
			}
			String blobCsid = mediaResourceValuesMap.get("blobCsid");
			if (null != blobCsid) {
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
		return (ServiceContextFactory<AdvancedsearchListItem, AdvancedsearchListItem>) RemoteServiceContextFactory
				.get();
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