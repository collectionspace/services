package org.collectionspace.services.advancedsearch;

import static org.collectionspace.services.client.CollectionSpaceClient.PART_COMMON_LABEL;
import static org.collectionspace.services.client.CollectionSpaceClient.PART_LABEL_SEPARATOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.advancedsearch.mapper.CollectionObjectMapper;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.api.RefNameUtils;
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
import org.w3c.dom.NodeList;

/**
 * This class defines the advanced search endpoints.
 */
@Path(AdvancedSearchConstants.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AdvancedSearch
		extends AbstractCollectionSpaceResourceImpl<AdvancedsearchListItem, AdvancedsearchListItem> {
	// FIXME: it's not great to hardcode either of these
	private static final String FIELDS_RETURNED = "uri|csid|refName|blobCsid|updatedAt|objectId|objectNumber|objectName|title|computedCurrentLocation|homeLocations|responsibleDepartments|responsibleDepartment|contentConcepts|briefDescription";

	private final Logger logger = LoggerFactory.getLogger(AdvancedSearch.class);
	private final CollectionObjectResource cor = new CollectionObjectResource();
	private final MediaResource mr = new MediaResource();
	private final RelationResource relations = new RelationResource();

	private static final String PAGE_SIZE = "1";
	private static final String PAGE_NUM = "0";
	private static final String MEDIA_SORT_BY = "media_common:title";

	private static final String COLLECTIONOBJECT_COMMON_PART_NAME =
			CollectionObjectClient.SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
	private static final String MEDIA_PRIORITY_FIELD = "mediaPriority";

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
		final UriInfoWrapper relUriInfo = relationUriInfo(uriInfo);
		for (CSDocumentModelResponse response : collectionObjectList.getResponseList()) {
			String csid = response.getCsid();
			Map<String, String> blobInfo = findPriorityBlobInfo(uriInfo, response);

			if (blobInfo.isEmpty()) {
				UriInfoWrapper blobUriInfo = new UriInfoWrapper(uriInfo);
				blobInfo = findBlobInfo(csid, blobUriInfo);
			}

			AdvancedsearchListItem listItem = responseMapper.asListItem(response, blobInfo);
			if (listItem != null) {
				if (markRelated != null) {
					RelationsCommonList relationsList = relations.getRelationForSubject(markRelated, csid, relUriInfo);
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

	private UriInfoWrapper relationUriInfo(final UriInfo uriInfo) {
		final UriInfoWrapper wrapper = new UriInfoWrapper(uriInfo);
		final MultivaluedMap<String, String> queryParameters = wrapper.getQueryParameters();
		queryParameters.clear();
		queryParameters.add(IClientQueryParams.PAGE_SIZE_PARAM, PAGE_SIZE);
		queryParameters.add(IClientQueryParams.START_PAGE_PARAM, PAGE_NUM);
		return wrapper;
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
		wrappedQueryParams.add(IClientQueryParams.PAGE_SIZE_PARAM, PAGE_SIZE);
		wrappedQueryParams.add(IClientQueryParams.START_PAGE_PARAM, PAGE_NUM);
		wrappedQueryParams.add(IClientQueryParams.ORDER_BY_PARAM, MEDIA_SORT_BY);
		return blobInfoFromList(mr.getList(wrappedUriInfo));
	}

	/**
	 * Extracts the blob info (blob CSID and alt text) from the first item of a media list.
	 *
	 * @param mediaList The media list to extract from
	 * @return A possibly-empty map of the blob info of the first media list item
	 */
	private Map<String, String> blobInfoFromList(AbstractCommonList mediaList) {
		if (mediaList == null || mediaList.getListItem() == null) {
			return Collections.emptyMap();
		}

		Predicate<Element> tagFilter = (element -> MediaJAXBSchema.blobCsid.equals(element.getTagName()) ||
												   MediaJAXBSchema.altText.equals(element.getTagName()));
		Predicate<Element> tagNotEmpty = (element -> element.getTextContent()  != null &&
													 !element.getTextContent().isEmpty());

        return mediaList.getListItem().stream()
			.filter(item -> item != null && item.getAny() != null)
			.flatMap(li -> li.getAny().stream())
			.filter(tagFilter.and(tagNotEmpty))
			.collect(Collectors.toMap(Element::getTagName, Element::getTextContent));
	}

	/**
	 * Retrieves the blob info (blob CSID and alt text) for the top entry of an object's media priority list,
	 *
	 * @param uriInfo The URI info of the incoming request that triggered the search
	 * @param response The response from the CollectionObjectResource for a single object
	 * @return A possibly-empty map of the top media priority record's blob info.
	 */
	private Map<String, String> findPriorityBlobInfo(UriInfo uriInfo, CSDocumentModelResponse response) {
		PoxPayloadOut payload = response.getPayload();

		if (payload == null) {
			return Collections.emptyMap();
		}

		PayloadOutputPart commonPart = payload.getPart(COLLECTIONOBJECT_COMMON_PART_NAME);
		List<String> refNames = elementTextValues(commonPart, MEDIA_PRIORITY_FIELD);

		if (refNames.isEmpty()) {
			return Collections.emptyMap();
		}

		String mediaCsid = RefNameUtils.parseAuthorityInfo(refNames.get(0)).csid;

		return findMediaBlobInfo(mediaCsid, new UriInfoWrapper(uriInfo));
	}

	/**
	 * Retrieves the blob info (blob CSID and alt text) of a single media record.
	 *
	 * @param mediaCsid The CSID of the media record
	 * @param wrappedUriInfo The wrapped (mutable) UriInfo of the incoming query that ultimately triggered this call
	 * @return A possibly-empty map of the media record's blob info.
	 */
	private Map<String, String> findMediaBlobInfo(String mediaCsid, UriInfoWrapper wrappedUriInfo) {
		MultivaluedMap<String, String> wrappedQueryParams = wrappedUriInfo.getQueryParameters();
		wrappedQueryParams.clear();
		wrappedQueryParams.add(IQueryManager.SEARCH_TYPE_KEYWORDS_AS, "ecm:name=\"" + mediaCsid + "\"");
		wrappedQueryParams.add(WorkflowClient.WORKFLOW_QUERY_DELETED_QP, Boolean.FALSE.toString());
		wrappedQueryParams.add(IClientQueryParams.PAGE_SIZE_PARAM, PAGE_SIZE);
		wrappedQueryParams.add(IClientQueryParams.START_PAGE_PARAM, PAGE_NUM);

		return blobInfoFromList(mr.getList(wrappedUriInfo));
	}

	/**
	 * Retrieves the non-empty text values of all elements with a given local name within a payload part.
	 *
	 * @param part The payload part to search, which may be null
	 * @param localName The local name of the elements to find
	 * @return A possibly-empty list of the non-empty text values, in document order
	 */
	private List<String> elementTextValues(PayloadOutputPart part, String localName) {
		List<String> values = new ArrayList<>();

		if (part == null) {
			return values;
		}

		Document doc = (Document) part.getBody();
		NodeList nodes = doc.getDocumentElement().getElementsByTagName(localName);

		for (int i = 0; i < nodes.getLength(); i++) {
			String text = nodes.item(i).getTextContent();

			if (text != null && !text.isEmpty()) {
				values.add(text);
			}
		}

		return values;
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
