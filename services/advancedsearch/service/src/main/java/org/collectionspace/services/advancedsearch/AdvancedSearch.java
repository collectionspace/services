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
import org.collectionspace.services.advancedsearch.model.AgentModel;
import org.collectionspace.services.advancedsearch.model.BriefDescriptionListModel;
import org.collectionspace.services.advancedsearch.model.ContentConceptListModel;
import org.collectionspace.services.advancedsearch.model.FieldCollectionModel;
import org.collectionspace.services.advancedsearch.model.NAGPRACategoryModel;
import org.collectionspace.services.advancedsearch.model.ObjectNameListModel;
import org.collectionspace.services.advancedsearch.model.ObjectProductionModel;
import org.collectionspace.services.advancedsearch.model.ResponsibleDepartmentsListModel;
import org.collectionspace.services.advancedsearch.model.TaxonModel;
import org.collectionspace.services.advancedsearch.model.TitleGroupListModel;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.nagpra.CollectionObjectsNAGPRA;
import org.collectionspace.services.collectionobject.domain.naturalhistory.CollectionobjectsNaturalhistory;
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
	// FIXME: it's not great to hardcode either of these
	private static final String FIELDS_RETURNED = "uri|csid|refName|blobCsid|updatedAt|objectId|objectNumber|objectName|title|computedCurrentLocation|responsibleDepartments|responsibleDepartment|contentConcepts|briefDescription";
	private static final String COMMON_PART_NAME = CollectionObjectClient.SERVICE_NAME
		+ CollectionSpaceClient.PART_LABEL_SEPARATOR
		+ CollectionSpaceClient.PART_COMMON_LABEL;

	private static final String NATHIST_PART_NAME = CollectionObjectClient.SERVICE_NAME
		+ CollectionSpaceClient.PART_LABEL_SEPARATOR
		+ CollectionSpaceClient.NATURALHISTORY_EXT_EXTENSION_NAME;

	private static final String NAGPRA_PART_NAME = CollectionObjectClient.SERVICE_NAME
		+ CollectionSpaceClient.PART_LABEL_SEPARATOR
		+ CollectionSpaceClient.NAGPRA_EXTENSION_NAME;

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
			CollectionObjectsNAGPRA objectsNAGPRA = null;
			CollectionobjectsNaturalhistory naturalHistory = null;

			try {
				core = (CollectionSpaceCore) unmarshaller.unmarshal((Document) corePart.getBody());
				collectionObject = (CollectionobjectsCommon) unmarshaller.unmarshal((Document) commonPart.getBody());

				PayloadOutputPart nagpraPart = outputPayload.getPart(NAGPRA_PART_NAME);
				if (nagpraPart != null) {
					objectsNAGPRA = (CollectionObjectsNAGPRA) unmarshaller.unmarshal((Document) nagpraPart.getBody());
				}

				PayloadOutputPart natHistPart = outputPayload.getPart(NATHIST_PART_NAME);
				if (natHistPart != null) {
					naturalHistory = (CollectionobjectsNaturalhistory) unmarshaller.unmarshal(
						(Document) natHistPart.getBody());
				}
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

				listItem.setTitle(TitleGroupListModel.titleGroupListToDisplayString(
					collectionObject.getTitleGroupList()));
				listItem.setResponsibleDepartment(ResponsibleDepartmentsListModel.responsibleDepartmentString(
					collectionObject));

				listItem.setObjectName(ObjectNameListModel.objectName(collectionObject));
				listItem.setObjectNameControlled(ObjectNameListModel.objectNameControlled(collectionObject));

				listItem.setContentConcepts(ContentConceptListModel.contentConceptList(collectionObject));

				// Field collection items (place, site, date, collector, role)
				listItem.setFieldCollectionPlace(FieldCollectionModel.fieldCollectionPlace(collectionObject));
				listItem.setFieldCollectionSite(FieldCollectionModel.fieldCollectionSite(collectionObject));
				listItem.setFieldCollectionDate(FieldCollectionModel.fieldCollectionDate(collectionObject));
				FieldCollectionModel.fieldCollector(collectionObject).ifPresent(collector -> {
					listItem.setFieldCollector(collector);
					listItem.setFieldCollectorRole("field collector"); // todo: how would we i18n this?
				});


				// Object Production Information (place, date, agent, agent role)
				listItem.setObjectProductionDate(ObjectProductionModel.objectProductionDate(collectionObject));
				listItem.setObjectProductionPlace(ObjectProductionModel.objectProductionPlace(collectionObject));

				AgentModel.agent(collectionObject).ifPresent(agent -> {
					listItem.setAgent(agent.getAgent());
					listItem.setAgentRole(agent.getRole());
				});

				listItem.setForm(TaxonModel.preservationForm(collectionObject));

				// from media resource
				if (blobCsids.size() > 0) {
					listItem.setBlobCsid(blobCsids.get(0));
				}
				// add the populated item to the results
				resultsList.getAdvancedsearchListItem().add(listItem);
			} else {
				logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}", csid);
			}

			if (naturalHistory != null) {
				listItem.setTaxon(TaxonModel.taxon(naturalHistory));
			}

			if (objectsNAGPRA != null) {
				listItem.setNagpraCategories(NAGPRACategoryModel.napgraCategories(objectsNAGPRA));
			}

			if (markRelated != null) {
				RelationsCommonList relationsList = relations.getRelationForSubject(markRelated, csid, uriInfo);
				listItem.setRelated(!relationsList.getRelationListItem().isEmpty());
			}
		}

		resultsList.setItemsInPage(collectionObjectList.getItemsInPage());
		resultsList.setPageNum(collectionObjectList.getPageNum());
		resultsList.setPageSize(collectionObjectList.getPageSize());
		resultsList.setTotalItems(collectionObjectList.getTotalItems());
		resultsList.setFieldsReturned(FIELDS_RETURNED);

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