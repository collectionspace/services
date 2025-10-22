package org.collectionspace.services.advancedsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.collectionspace.collectionspace_core.CollectionSpaceCore;
import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.advancedsearch.model.BriefDescriptionListModel;
import org.collectionspace.services.advancedsearch.model.ContentConceptListModel;
import org.collectionspace.services.advancedsearch.model.ObjectNameListModel;
import org.collectionspace.services.advancedsearch.model.ResponsibleDepartmentsListModel;
import org.collectionspace.services.advancedsearch.model.TitleGroupListModel;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.relation.RelationResource;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.media.MediaResource;
import org.collectionspace.services.nuxeo.client.handler.CSDocumentModelList;
import org.collectionspace.services.nuxeo.client.handler.CSDocumentModelList.CSDocumentModelResponse;
import org.collectionspace.services.nuxeo.client.handler.UnfilteredDocumentModelHandler;
import org.collectionspace.services.relation.RelationsCommonList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class defines the advanced search endpoints.
 */
@Path(AdvancedSearchConstants.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch
		extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem, AdvancedsearchListItem> {
	private static final String FIELDS_RETURNED = "uri|csid|refName|blobCsid|updatedAt|objectId|objectNumber|objectName|title|computedCurrentLocation|responsibleDepartments|responsibleDepartment|contentConcepts|briefDescription";
	// FIXME: it's not great to hardcode this here
	private static final String COMMON_PART_NAME = CollectionObjectClient.SERVICE_NAME
	                                               + CollectionSpaceClient.PART_LABEL_SEPARATOR
	                                               + CollectionSpaceClient.PART_COMMON_LABEL;

	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);
	private final CollectionObjectResource cor = new CollectionObjectResource();
	private final MediaResource mr = new MediaResource();
	private final RelationResource relations = new RelationResource();

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
		final String markRelated = queryParams.getFirst(IQueryManager.MARK_RELATED_TO_CSID_AS_SUBJECT);

		cor.setDocumentHandlerClass(UnfilteredDocumentModelHandler.class);
		ObjectFactory objectFactory = new ObjectFactory();
		// the list to return
		AdvancedsearchCommonList resultsList = objectFactory.createAdvancedsearchCommonList();
		// FIXME: this shouldn't be necessary?
		resultsList.advancedsearchListItem = new ArrayList<>();

		AbstractCommonList abstractCommonList = cor.getList(uriInfo);
		if (!(abstractCommonList instanceof CSDocumentModelList)) {
			return resultsList;
		}

		Unmarshaller unmarshaller;
		CSDocumentModelList collectionObjectList = (CSDocumentModelList) abstractCommonList;
		try {
			unmarshaller = AdvancedSearchJAXBContext.getJaxbContext().createUnmarshaller();
		} catch (JAXBException e) {
			// this should result in a 500, need to verify from bigReThrow to see what exception it should be
			throw new RuntimeException("Unable to create unmarshaller for AdvancedSearch", e);
		}

		for (CSDocumentModelResponse response : collectionObjectList.getResponseList()) {
			PoxPayloadOut outputPayload = response.getPayload();
			PayloadOutputPart corePart = outputPayload.getPart(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA);
			PayloadOutputPart commonPart = outputPayload.getPart(COMMON_PART_NAME);
			CollectionSpaceCore core;
			CollectionobjectsCommon collectionObject;

			try {
				core = (CollectionSpaceCore) unmarshaller.unmarshal((Document) corePart.getBody());
				collectionObject = (CollectionobjectsCommon) unmarshaller.unmarshal((Document) commonPart.getBody());
			} catch (JAXBException e) {
				throw new RuntimeException(e);
			}

			String csid = response.getCsid();
			UriInfoWrapper wrappedUriInfo = new UriInfoWrapper(uriInfo);
			List<String> blobCsids = findBlobCsids(csid, wrappedUriInfo);

			AdvancedsearchListItem listItem = objectFactory.createAdvancedsearchCommonListAdvancedsearchListItem();
			if (core != null) {
				listItem.setCsid(csid);
				listItem.setRefName(core.getRefName());
				listItem.setUri(core.getUri());
				listItem.setUpdatedAt(core.getUpdatedAt());
			} else {
				logger.warn("advancedsearch: could not find collectionspace_core associated with csid {}", csid);
			}

			if (collectionObject != null) {
				listItem.setObjectNumber(collectionObject.getObjectNumber());
				listItem.setBriefDescription(BriefDescriptionListModel
					.briefDescriptionListToDisplayString(collectionObject.getBriefDescriptions()));
				listItem.setComputedCurrentLocation(collectionObject.getComputedCurrentLocation());
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
				// add the populated item to the results
				resultsList.getAdvancedsearchListItem().add(listItem);
			} else {
				logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}", csid);
			}

			if (markRelated != null) {
				RelationsCommonList relationsList = relations.getRelationForSubject(markRelated, csid, uriInfo);
				listItem.setRelated(!relationsList.getRelationListItem().isEmpty());
			}
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
		if (associatedMedia == null || associatedMedia.getListItem() == null) {
			return Collections.emptyList();
		}

		return associatedMedia.getListItem().stream()
			.filter(item -> item != null && item.getAny() != null)
			.flatMap(li -> li.getAny().stream())
			.filter(element -> MediaJAXBSchema.blobCsid.equals(element.getTagName()))
			.map(Element::getTextContent)
			.filter(blobCsid -> blobCsid != null && !blobCsid.isEmpty())
			.collect(Collectors.toList());
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
		return AdvancedSearchConstants.SERVICE_NAME;
	}

	@Override
	protected String getVersionString() {
		return "0.01";
	}
}