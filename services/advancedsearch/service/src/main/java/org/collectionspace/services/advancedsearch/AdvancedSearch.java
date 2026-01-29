package org.collectionspace.services.advancedsearch;

import java.util.Collections;
import java.util.Map;
import java.util.function.Predicate;
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

import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.advancedsearch.mapper.CollectionObjectMapper;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
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
		AdvancedsearchCommonList resultsList = objectFactory.createAdvancedsearchCommonList();

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

		final CollectionObjectMapper responseMapper = new CollectionObjectMapper(unmarshaller);
		for (CSDocumentModelResponse response : collectionObjectList.getResponseList()) {
			String csid = response.getCsid();
			UriInfoWrapper wrappedUriInfo = new UriInfoWrapper(uriInfo);
			Map<String, String> blobInfo = findBlobInfo(csid, wrappedUriInfo);

			AdvancedsearchListItem listItem = responseMapper.asListItem(response, blobInfo);
			if (listItem != null) {
				if (markRelated != null) {
					RelationsCommonList relationsList = relations.getRelationForSubject(markRelated, csid, uriInfo);
					listItem.setRelated(!relationsList.getRelationListItem().isEmpty());
				}
				resultsList.getAdvancedsearchListItem().add(listItem);
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
	private Map<String, String> findBlobInfo(String csid, UriInfoWrapper wrappedUriInfo) {
		MultivaluedMap<String, String> wrappedQueryParams = wrappedUriInfo.getQueryParameters();
		wrappedQueryParams.clear();
		wrappedQueryParams.add(IQueryManager.SEARCH_RELATED_TO_CSID_AS_SUBJECT, csid);
		wrappedQueryParams.add("pgSz", "1");
		wrappedQueryParams.add("pgNum", "0");
		wrappedQueryParams.add("sortBy", "media_common:title");
		AbstractCommonList associatedMedia = mr.getList(wrappedUriInfo);
		if (associatedMedia == null || associatedMedia.getListItem() == null) {
			return Collections.emptyMap();
		}

		Predicate<Element> tagFilter = (element -> MediaJAXBSchema.blobCsid.equals(element.getTagName()) ||
												   MediaJAXBSchema.altText.equals(element.getTagName()));
		Predicate<Element> tagNotEmpty = (element -> element.getTextContent()  != null &&
													 !element.getTextContent().isEmpty());

        return associatedMedia.getListItem().stream()
			.filter(item -> item != null && item.getAny() != null)
			.flatMap(li -> li.getAny().stream())
			.filter(tagFilter.and(tagNotEmpty))
			.collect(Collectors.toMap(Element::getTagName, Element::getTextContent));
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