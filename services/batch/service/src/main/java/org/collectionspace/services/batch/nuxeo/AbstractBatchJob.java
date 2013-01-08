package org.collectionspace.services.batch.nuxeo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PlaceAuthorityClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommonList;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBatchJob implements BatchInvocable {
	public final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	public final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	public final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

	private List<String> invocationModes = Collections.emptyList();
	private ResourceMap resourceMap;
	private InvocationContext context;
	private int completionStatus;
	private InvocationResults results;	
	private InvocationError errorInfo;	

	final Logger logger = LoggerFactory.getLogger(AbstractBatchJob.class);

	public AbstractBatchJob() {
		this.completionStatus = STATUS_UNSTARTED;
		this.results = new InvocationResults();
	}

	public List<String> getSupportedInvocationModes() {
		return invocationModes;
	}
	
	protected void setSupportedInvocationModes(List<String> invocationModes) {
		this.invocationModes = invocationModes;
	}

	public ResourceMap getResourceMap() {
		return resourceMap;
	}

	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}

	public InvocationContext getInvocationContext() {
		return context;
	}

	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}

	public int getCompletionStatus() {
		return completionStatus;
	}
	
	protected void setCompletionStatus(int completionStatus) {
		this.completionStatus = completionStatus;
	}

	public InvocationResults getResults() {
		return (STATUS_COMPLETE == completionStatus) ? results : null;
	}
	
	protected void setResults(InvocationResults results) {
		this.results = results;
	}

	public InvocationError getErrorInfo() {
		return errorInfo;
	}
	
	protected void setErrorInfo(InvocationError errorInfo) {
		this.errorInfo = errorInfo;
	}

	public abstract void run();
	
	protected String getFieldXml(Map<String, String> fields, String fieldName) {
		return getFieldXml(fieldName, fields.get(fieldName));
	}

	protected String getFieldXml(String fieldName, String fieldValue) {
		String xml = "<" + fieldName + ">" + (fieldValue == null ? "" : StringEscapeUtils.escapeXml(fieldValue)) + "</" + fieldName + ">";
		
		return xml;
	}
	
	protected String createRelation(String subjectCsid, String subjectDocType, String objectCsid, String objectDocType, String relationshipType) throws ResourceException {
		String relationCsid = null;

		String createRelationPayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"relations\">" +
				"<ns2:relations_common xmlns:ns2=\"http://collectionspace.org/services/relation\" xmlns:ns3=\"http://collectionspace.org/services/jaxb\">" +
					"<subjectCsid>" + StringEscapeUtils.escapeXml(subjectCsid) + "</subjectCsid>" +
					"<subjectDocumentType>" + StringEscapeUtils.escapeXml(subjectDocType) + "</subjectDocumentType>" +
					"<objectCsid>" + StringEscapeUtils.escapeXml(objectCsid) + "</objectCsid>" +
					"<objectDocumentType>" + StringEscapeUtils.escapeXml(objectDocType) + "</objectDocumentType>" +
					"<relationshipType>" + StringEscapeUtils.escapeXml(relationshipType) + "</relationshipType>" +
				"</ns2:relations_common>" +
			"</document>";

		ResourceBase resource = resourceMap.get(RelationClient.SERVICE_NAME);
		Response response = resource.create(resourceMap, null, createRelationPayload);

		if (response.getStatus() == CREATED_STATUS) {
			relationCsid = CollectionSpaceClientUtils.extractId(response);
		}
		else {
			throw new ResourceException(response, "Error creating relation");
		}

		return relationCsid;
	}

	/**
	 * Return the csids of records that are related to a given subject record, and have a given document type.
	 * Soft-deleted relations are filtered from the list, but soft-deleted object records are not.
	 * 
	 * @param subjectCsid		The csid of the subject record
	 * @param objectDocType		The document type of the object records to find
	 * @return
	 * @throws URISyntaxException
	 */
	protected List<String> findRelated(String subjectCsid, String objectDocType) throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		RelationResource relationResource = (RelationResource) resourceMap.get(RelationClient.SERVICE_NAME);
		RelationsCommonList relationList = relationResource.getList(createRelationSearchUriInfo(subjectCsid, objectDocType));

		for (RelationsCommonList.RelationListItem item : relationList.getRelationListItem()) {
			csids.add(item.getObjectCsid());
		}

		return csids;
	}

	protected List<String> findRelatedCollectionObjects(String subjectCsid) throws URISyntaxException {
		return findRelated(subjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE);
	}

	protected List<String> findRelatedMovements(String subjectCsid) throws URISyntaxException {
		return findRelated(subjectCsid, MovementConstants.NUXEO_DOCTYPE);
	}

	/**
	 * Returns the movement record related to the specified record, if there is only one.
	 * Returns null if there are zero or more than one related movement records.
	 * 
	 * @param subjectCsid	The csid of the record
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	protected String findSingleRelatedMovement(String subjectCsid) throws URISyntaxException, DocumentException {
		String foundMovementCsid = null;
		List<String> movementCsids = findRelatedMovements(subjectCsid);
		
		for (String movementCsid : movementCsids) {
			PoxPayloadOut movementPayload = findMovementByCsid(movementCsid);
			String movementWorkflowState = getFieldValue(movementPayload, MovementConstants.WORKFLOW_STATE_SCHEMA_NAME, MovementConstants.WORKFLOW_STATE_FIELD_NAME);
		
			if (!movementWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
				if (foundMovementCsid != null) {
					return null;
				}
				
				foundMovementCsid = movementCsid;
			}
		}

		return foundMovementCsid;
	}

	protected PoxPayloadOut findByCsid(String serviceName, String csid) throws URISyntaxException, DocumentException {
		ResourceBase resource = resourceMap.get(serviceName);
		byte[] response = resource.get(createUriInfo(), csid);

		PoxPayloadOut payload = new PoxPayloadOut(response);

		return payload;
	}

	protected PoxPayloadOut findCollectionObjectByCsid(String csid) throws URISyntaxException, DocumentException {
		return findByCsid(CollectionObjectClient.SERVICE_NAME, csid);
	}

	protected PoxPayloadOut findMovementByCsid(String csid) throws URISyntaxException, DocumentException {
		return findByCsid(MovementClient.SERVICE_NAME, csid);
	}
	
	protected List<String> findAll(String serviceName, int pageSize, int pageNum) throws URISyntaxException, DocumentException {
		ResourceBase resource = resourceMap.get(serviceName);	
		AbstractCommonList list = resource.getList(this.createPagedListUriInfo(pageNum, pageSize));
		List<String> csids = new ArrayList<String>();
		
		for (AbstractCommonList.ListItem item : list.getListItem()) {
			for (org.w3c.dom.Element element : item.getAny()) {
				if (element.getTagName().equals("csid")) {
					csids.add(element.getTextContent());
					break;
				}
			}
		}
		
		return csids;
	}
	
	protected List<String> findAllCollectionObjects(int pageSize, int pageNum) throws URISyntaxException, DocumentException {
		return findAll(CollectionObjectClient.SERVICE_NAME, pageSize, pageNum);
	}
	
	protected List<String> getVocabularyCsids(String serviceName) throws URISyntaxException {
		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) resourceMap.get(serviceName);
		AbstractCommonList vocabularyList = resource.getAuthorityList(createDeleteFilterUriInfo());
		List<String> csids = new ArrayList<String>();
		
		for (AbstractCommonList.ListItem item : vocabularyList.getListItem()) {
			for (org.w3c.dom.Element element : item.getAny()) {
				if (element.getTagName().equals("csid")) {
					csids.add(element.getTextContent());
					break;
				}
			}
		}

		return csids;
	}
	
	protected PoxPayloadOut findAuthorityItemByCsid(String serviceName, String csid) throws URISyntaxException, DocumentException {
		List<String> vocabularyCsids = getVocabularyCsids(serviceName);
		PoxPayloadOut itemPayload = null;
		
		for (String vocabularyCsid : vocabularyCsids) {
			logger.debug("vocabularyCsid=" + vocabularyCsid);
			
			itemPayload = findAuthorityItemByCsid(serviceName, vocabularyCsid, csid);
			
			if (itemPayload != null) {
				break;
			}
		}
		
		return itemPayload;
	}
	
	protected PoxPayloadOut findAuthorityItemByCsid(String serviceName, String vocabularyCsid, String csid) throws URISyntaxException, DocumentException {
		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) resourceMap.get(serviceName);
		byte[] response = resource.getAuthorityItem(null, createDeleteFilterUriInfo(), vocabularyCsid, csid);
		 
		PoxPayloadOut payload = new PoxPayloadOut(response);

		return payload;
	}
	
	protected PoxPayloadOut findTaxonByCsid(String csid) throws URISyntaxException, DocumentException {
		return findAuthorityItemByCsid(TaxonomyAuthorityClient.SERVICE_NAME, csid);
	}
	
	protected PoxPayloadOut findAuthorityItemByShortId(String serviceName, String vocabularyShortId, String itemShortId) throws URISyntaxException, DocumentException {
		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) resourceMap.get(serviceName);
		byte[] response = resource.getAuthorityItem(null, createDeleteFilterUriInfo(), "urn:cspace:name(" + vocabularyShortId + ")", "urn:cspace:name(" + itemShortId + ")");
 
		PoxPayloadOut payload = new PoxPayloadOut(response);

		return payload;
	}
	
	protected PoxPayloadOut findAuthorityItemByRefName(String serviceName, String refName) throws URISyntaxException, DocumentException {
		RefName.AuthorityItem item = RefName.AuthorityItem.parse(refName);
		
		String vocabularyShortId = item.getParentShortIdentifier();
		String itemShortId = item.getShortIdentifier();
		
		return findAuthorityItemByShortId(serviceName, vocabularyShortId, itemShortId);
	}

	protected PoxPayloadOut findPlaceByRefName(String refName) throws URISyntaxException, DocumentException {
		return findAuthorityItemByRefName(PlaceAuthorityClient.SERVICE_NAME, refName);
	}
	
	protected PoxPayloadOut findTaxonByRefName(String refName) throws URISyntaxException, DocumentException {
		return findAuthorityItemByRefName(TaxonomyAuthorityClient.SERVICE_NAME, refName);
	}
	
	protected List<String> findReferencingObjects(String serviceName, String parentCsid, String csid, String type, String sourceField) throws URISyntaxException {
		logger.debug("findReferencingObjects serviceName=" + serviceName + " parentCsid=" + parentCsid + " csid=" + csid + " type=" + type + " sourceField=" + sourceField);

		AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) resourceMap.get(serviceName);
		AuthorityRefDocList refDocList = resource.getReferencingObjects(parentCsid, csid, createRefSearchFilterUriInfo(type));
		List<String> csids = new ArrayList<String>();
		
		for (AuthorityRefDocList.AuthorityRefDocItem item : refDocList.getAuthorityRefDocItem()) {
			if (!item.getWorkflowState().equals(WorkflowClient.WORKFLOWSTATE_DELETED) && (sourceField == null || item.getSourceField().equals(sourceField))) {
				csids.add(item.getDocId());
			}
		}

		return csids;
	}

	protected List<String> findReferencingObjects(String serviceName, String csid, String type, String sourceField) throws URISyntaxException, DocumentException {
		logger.debug("findReferencingObjects serviceName=" + serviceName + " csid=" + csid + " type=" + type + " sourceField=" + sourceField);

		List<String> vocabularyCsids = getVocabularyCsids(serviceName);
		String parentCsid = null;
		
		if (vocabularyCsids.size() == 1) {
			parentCsid = vocabularyCsids.get(0);
		}
		else {
			for (String vocabularyCsid : vocabularyCsids) {
				PoxPayloadOut itemPayload = findAuthorityItemByCsid(serviceName, vocabularyCsid, csid);
				
				if (itemPayload != null) {
					parentCsid = vocabularyCsid;
					break;
				}
			}
		}
		
		return findReferencingObjects(serviceName, parentCsid, csid, type, sourceField);
	}

	protected List<String> findReferencingCollectionObjects(String serviceName, String csid, String sourceField) throws URISyntaxException, DocumentException {
		return findReferencingObjects(serviceName, csid, ServiceBindingUtils.SERVICE_TYPE_OBJECT, sourceField);
	}

	protected List<String> findReferencingCollectionObjects(String serviceName, String vocabularyShortId, String csid, String sourceField) throws URISyntaxException, DocumentException {
		return findReferencingObjects(serviceName, "urn:cspace:name(" + vocabularyShortId + ")", csid, ServiceBindingUtils.SERVICE_TYPE_OBJECT, sourceField);
	}
	
	/**
	 * Create a stub UriInfo
	 * 
	 * @throws URISyntaxException 
	 */
	protected UriInfo createUriInfo() throws URISyntaxException {
		return createUriInfo("");
	}

	protected UriInfo createUriInfo(String queryString) throws URISyntaxException {
		queryString = escapeQueryString(queryString);
		
		URI	absolutePath = new URI("");
		URI	baseUri = new URI("");

		return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment> emptyList());
	}
	
	protected UriInfo createDeleteFilterUriInfo() throws URISyntaxException {
		return createUriInfo("wf_deleted=false");
	}

	protected UriInfo createKeywordSearchUriInfo(String schemaName, String fieldName, String value) throws URISyntaxException {
		return createUriInfo("kw=&as=( (" +schemaName + ":" + fieldName + " ILIKE \"" + value + "\") )&wf_deleted=false");
	}

	protected UriInfo createRelationSearchUriInfo(String subjectCsid, String objType) throws URISyntaxException {
		return createUriInfo("sbj=" + subjectCsid + "&objType=" + objType + "&wf_deleted=false");
	}
	
	protected UriInfo createRefSearchFilterUriInfo(String type) throws URISyntaxException {
		return createUriInfo("type=" + type + "&wf_deleted=false");
	}

	protected UriInfo createPagedListUriInfo(int pageNum, int pageSize) throws URISyntaxException {
		return createUriInfo("pgSz=" + pageSize + "&pgNum=" + pageNum + "&wf_deleted=false");
	}

	protected String escapeQueryString(String queryString) throws URISyntaxException {
		URI uri =  new URI(null, null, null, queryString, null);

		return uri.getRawQuery();
	}
	
	/**
	 * Get a field value from a PoxPayloadOut, given a part name and xpath expression.
	 */
	protected String getFieldValue(PoxPayloadOut payload, String partLabel, String fieldPath) {
		String value = null;
		PayloadOutputPart part = payload.getPart(partLabel);

		if (part != null) {
			Element element = part.asElement();
			Node node = element.selectSingleNode(fieldPath);

			if (node != null) {
				value = node.getText();
			}
		}

		return value;
	}

	protected boolean getBooleanFieldValue(PoxPayloadOut payload, String partLabel, String fieldPath) {
		String value = getFieldValue(payload, partLabel, fieldPath);
		
		return (value != null && value.equals("true"));
	}
	
	protected List<String> getFieldValues(PoxPayloadOut payload, String partLabel, String fieldPath) {
		List<String> values = new ArrayList<String>();
		PayloadOutputPart part = payload.getPart(partLabel);

		if (part != null) {
			Element element = part.asElement();
			List<Node> nodes = element.selectNodes(fieldPath);

			if (nodes != null) {
				for (Node node : nodes) {
					values.add(node.getText());
				}
			}
		}

		return values;
	}
	
	protected String getDisplayNameFromRefName(String refName) {
		RefName.AuthorityItem item = RefName.AuthorityItem.parse(refName);

		return (item == null ? refName : item.displayName);
	}
	
	protected class ResourceException extends Exception {
		private static final long serialVersionUID = 1L;

		private Response response;
		
		public ResourceException(Response response, String message) {
			super(message);
			this.setResponse(response);
		}

		public Response getResponse() {
			return response;
		}

		public void setResponse(Response response) {
			this.response = response;
		}
	}
}
