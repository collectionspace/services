package org.collectionspace.services.batch.nuxeo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.batch.BatchInvocable;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommonList;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateDeadFlagBatchJob implements BatchInvocable {
	private static List<String> invocationModes = Arrays.asList(INVOCATION_MODE_SINGLE);

	protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

	private ResourceMap resourceMap;
	private InvocationContext context;
	private int completionStatus;
	private InvocationResults results;	
	private InvocationError errorInfo;	

	final Logger logger = LoggerFactory.getLogger(UpdateDeadFlagBatchJob.class);

	public UpdateDeadFlagBatchJob() {
		this.completionStatus = STATUS_UNSTARTED;
		this.results = new InvocationResults();
	}

	public List<String> getSupportedInvocationModes() {
		return invocationModes;
	}

	public void setResourceMap(ResourceMap resourceMap) {
		this.resourceMap = resourceMap;
	}

	public void setInvocationContext(InvocationContext context) {
		this.context = context;
	}

	public int getCompletionStatus() {
		return completionStatus;
	}

	public InvocationResults getResults() {
		return (STATUS_COMPLETE == completionStatus) ? results : null;
	}

	public InvocationError getErrorInfo() {
		return errorInfo;
	}
	
	public void run() {
		completionStatus = STATUS_MIN_PROGRESS;
		
		try {
			String mode = context.getMode();
			
			if (!mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
				throw new Exception("Unsupported invocation mode: " + mode);
			}
			
			String movementCsid = context.getSingleCSID();
			
			if (StringUtils.isEmpty(movementCsid)) {
				throw new Exception("Missing context csid");
			}
			
			results = updateRelatedDeadFlags(movementCsid);
			completionStatus = STATUS_COMPLETE;
		}
		catch(Exception e) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS, e.getMessage());
			results.setUserNote(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Update the dead flag for all collectionobjects related to the given movement record,
	 * based on the assumption that the action code of the specified movement record has just changed.
	 * 
	 * @param movementCsid	the csid of the movement that was updated
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateRelatedDeadFlags(String movementCsid) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		long numAffected = 0;
		List<String> userNotes = new ArrayList<String>();
		
		PoxPayloadOut payload = findMovementByCsid(movementCsid);

		String actionCode = getFieldValue(payload, MovementConstants.ACTION_CODE_SCHEMA_NAME, MovementConstants.ACTION_CODE_FIELD_NAME);
		logger.debug("actionCode=" + actionCode);
		
		if (actionCode.equals(MovementConstants.DEAD_ACTION_CODE) || actionCode.equals(MovementConstants.REVIVED_ACTION_CODE)) {
			String actionDate = getFieldValue(payload, MovementConstants.ACTION_DATE_SCHEMA_NAME, MovementConstants.ACTION_DATE_FIELD_NAME);
			logger.debug("actionDate=" + actionDate);
			
			List<String> collectionObjectCsids = findRelatedCollectionObjects(movementCsid);
			
			for (String collectionObjectCsid : collectionObjectCsids) {
				logger.debug("found related collectionobject: " + collectionObjectCsid);

				InvocationResults collectionObjectResults = updateDeadFlag(collectionObjectCsid, movementCsid, actionCode, actionDate);

				if (collectionObjectResults.getNumAffected() > 0) {
					numAffected = numAffected + collectionObjectResults.getNumAffected();
					userNotes.add(collectionObjectResults.getUserNote());
				}
			}
		}
		
		if (numAffected > 0) {
			results.setNumAffected(numAffected);
			results.setUserNote(StringUtils.join(userNotes, ", "));
		}
		
		return results;
	}

	/**
	 * Update the dead flag for the given collectionobject, based on the assumption that the action code
	 * of the specified movement record has just changed, and that the movement record is related to
	 * the collectionobject.
	 * 
	 * @param collectionObjectCsid	the csid of the collectionobject to update
	 * @param updatedMovementCsid	the csid of the related movement that was updated
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	public InvocationResults updateDeadFlag(String collectionObjectCsid, String updatedMovementCsid) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		PoxPayloadOut payload = findMovementByCsid(updatedMovementCsid);

		String actionCode = getFieldValue(payload, MovementConstants.ACTION_CODE_SCHEMA_NAME, MovementConstants.ACTION_CODE_FIELD_NAME);
		logger.debug("actionCode=" + actionCode);
		
		if (actionCode.equals(MovementConstants.DEAD_ACTION_CODE) || actionCode.equals(MovementConstants.REVIVED_ACTION_CODE)) {
			String actionDate = getFieldValue(payload, MovementConstants.ACTION_DATE_SCHEMA_NAME, MovementConstants.ACTION_DATE_FIELD_NAME);
			logger.debug("actionDate=" + actionDate);

			results = updateDeadFlag(collectionObjectCsid, updatedMovementCsid, actionCode, actionDate);
		}
		
		return results;
	}
	
	/**
	 * Update the dead flag for the given collectionobject, based on the assumption that the action code
	 * of the specified movement record has just changed, and that the movement record is related to
	 * the collectionobject.
	 * 
	 * @param collectionObjectCsid	the csid of the collectionobject to update
	 * @param updatedMovementCsid	the csid of the related movement that was updated
	 * @param actionCode			the action code of the movement
	 * @param actionDate			the action date of the movement
	 * @return
	 * @throws URISyntaxException
	 * @throws DocumentException
	 */
	private InvocationResults updateDeadFlag(String collectionObjectCsid, String updatedMovementCsid, String actionCode, String actionDate) throws URISyntaxException, DocumentException {
		InvocationResults results = new InvocationResults();
		PoxPayloadOut payload = findCollectionObjectByCsid(collectionObjectCsid);

		String workflowState = getFieldValue(payload, CollectionObjectConstants.WORKFLOW_STATE_SCHEMA_NAME, CollectionObjectConstants.WORKFLOW_STATE_FIELD_NAME);
		
		if (workflowState.equals(CollectionObjectConstants.DELETED_STATE)) {
			logger.debug("skipping deleted collectionobject: " + collectionObjectCsid);
		}
		else {			
			String deadFlag = getFieldValue(payload, CollectionObjectConstants.DEAD_FLAG_SCHEMA_NAME, CollectionObjectConstants.DEAD_FLAG_FIELD_NAME);
			boolean isDead = (deadFlag != null) && (deadFlag.equalsIgnoreCase("true"));

			logger.debug("updating dead flag: collectionObjectCsid=" + collectionObjectCsid + " actionCode=" + actionCode + " isDead=" + isDead);

			if (actionCode.equals(MovementConstants.REVIVED_ACTION_CODE)) {
				if (isDead) {
					/*
					 * The object is dead, but a location was revived. Unset the dead flag and date on the object.
					 */
					setDeadFlag(collectionObjectCsid, false, null);
					
					results.setNumAffected(1);
					results.setUserNote(collectionObjectCsid + " set to alive");
				}
			}
			else if (actionCode.equals(MovementConstants.DEAD_ACTION_CODE)) {
				if (!isDead) {
					/*
					 * The object is not dead, but a location was marked dead. If there are no remaining live locations,
					 * set the dead flag and date on the object. Any movement record that is not deleted represents
					 * a live location, with one exception: the movement record that was just marked dead may not have
					 * been deleted yet, but it should not count as a live location.
					 */
					List<String> movementCsids = findRelatedMovements(collectionObjectCsid);
					boolean liveLocationExists = false;
					
					for (String movementCsid : movementCsids) {
						logger.debug("found related movement: movementCsid=" + movementCsid);
						
						if (!movementCsid.equals(updatedMovementCsid)) {
							PoxPayloadOut movementPayload = findMovementByCsid(movementCsid);
							String movementWorkflowState = getFieldValue(movementPayload, MovementConstants.WORKFLOW_STATE_SCHEMA_NAME, MovementConstants.WORKFLOW_STATE_FIELD_NAME);
						
							if (!movementWorkflowState.equals(MovementConstants.DELETED_STATE)) {
								logger.debug("found live location: movementCsid=" + movementCsid);
								
								liveLocationExists = true;
								break;
							}
						}
					}
					
					if (!liveLocationExists) {
						setDeadFlag(collectionObjectCsid, true, actionDate);

						results.setNumAffected(1);
						results.setUserNote(collectionObjectCsid + " set to dead");
					}
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Update the dead flag and dead date of the specified collectionobject.
	 * 
	 * @param collectionObjectCsid	the csid of the collectionobject to update
	 * @param deadFlag				the new value of the dead flag field
	 * @param deadDate				the new value of the dead date field
	 */
	private void setDeadFlag(String collectionObjectCsid, boolean deadFlag, String deadDate) {
		String updatePayload = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"collectionobjects\">" +
				"<ns2:collectionobjects_botgarden xmlns:ns2=\"http://collectionspace.org/services/collectionobject/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					"<deadFlag>" + (deadFlag ? "true" : "false") + "</deadFlag>" +
					"<deadDate>" + (deadDate == null ? "" : deadDate) + "</deadDate>" +
				"</ns2:collectionobjects_botgarden>" +
				"<ns2:collectionobjects_common xmlns:ns2=\"http://collectionspace.org/services/collectionobject\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
				"</ns2:collectionobjects_common>" +					
			"</document>";
		
		logger.debug(updatePayload);
		
		ResourceBase resource = resourceMap.get(CollectionObjectClient.SERVICE_NAME);
		resource.update(resourceMap, collectionObjectCsid, updatePayload);
	}
			
	/**
	 * Return a list of csids that are related to the subjectCsid, and have doctype objectDocType.
	 * Deleted objects are not filtered from the list.
	 * 
	 * @param subjectCsid
	 * @param objectDocType
	 * @return
	 * @throws URISyntaxException
	 */
	private List<String> findRelated(String subjectCsid, String objectDocType) throws URISyntaxException {
		List<String> csids = new ArrayList<String>();
		RelationResource relationResource = (RelationResource) resourceMap.get(RelationClient.SERVICE_NAME);
		RelationsCommonList relationList = relationResource.getList(createRelationSearchUriInfo(subjectCsid, objectDocType));

		for (RelationsCommonList.RelationListItem item : relationList.getRelationListItem()) {
			csids.add(item.getObjectCsid());
		}

		return csids;
	}
	
	private List<String> findRelatedCollectionObjects(String subjectCsid) throws URISyntaxException {
		return findRelated(subjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE);
	}
	
	private List<String> findRelatedMovements(String subjectCsid) throws URISyntaxException {
		return findRelated(subjectCsid, MovementConstants.NUXEO_DOCTYPE);
	}
	
	private PoxPayloadOut findByCsid(String serviceName, String csid) throws URISyntaxException, DocumentException {
		ResourceBase resource = resourceMap.get(serviceName);
		byte[] response = resource.get(createUriInfo(), csid);

		PoxPayloadOut payload = new PoxPayloadOut(response);
		
		return payload;
	}

	private PoxPayloadOut findCollectionObjectByCsid(String csid) throws URISyntaxException, DocumentException {
		return findByCsid(CollectionObjectClient.SERVICE_NAME, csid);
	}

	private PoxPayloadOut findMovementByCsid(String csid) throws URISyntaxException, DocumentException {
		return findByCsid(MovementClient.SERVICE_NAME, csid);
	}

	/**
	 * Create a stub UriInfo
	 * 
	 * @throws URISyntaxException 
	 */
	private UriInfo createUriInfo() throws URISyntaxException {
		return createUriInfo("");
	}

	private UriInfo createUriInfo(String queryString) throws URISyntaxException {
		URI	absolutePath = new URI("");
		URI	baseUri = new URI("");

		return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment> emptyList());
	}
	
	private UriInfo createRelationSearchUriInfo(String subjectCsid, String objType) throws URISyntaxException {
		String queryString = "sbj=" + subjectCsid + "&objType=" + objType;
		URI uri =  new URI(null, null, null, queryString, null);

		return createUriInfo(uri.getRawQuery());		
	}
	
	/**
	 * Get a field value from a PoxPayloadOut, given a part name and xpath expression.
	 */
	private String getFieldValue(PoxPayloadOut payload, String partLabel, String fieldPath) {
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
}
