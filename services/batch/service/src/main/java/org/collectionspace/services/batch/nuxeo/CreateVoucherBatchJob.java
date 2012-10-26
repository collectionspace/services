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
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.LoanoutClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.datetime.GregorianCalendarDateTimeUtils;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.loanout.nuxeo.LoanoutConstants;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.RelationsCommonList;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateVoucherBatchJob implements BatchInvocable {
	private static List<String> invocationModes = Arrays.asList(INVOCATION_MODE_SINGLE);

	protected final int CREATED_STATUS = Response.Status.CREATED.getStatusCode();
	protected final int BAD_REQUEST_STATUS = Response.Status.BAD_REQUEST.getStatusCode();
	protected final int INT_ERROR_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

	private ResourceMap resourceMap;
	private InvocationContext context;
	private int completionStatus;
	private InvocationResults results;	
	private InvocationError errorInfo;	

	final Logger logger = LoggerFactory.getLogger(CreateVoucherBatchJob.class);

	public CreateVoucherBatchJob() {
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

			String csid = context.getSingleCSID();

			if (StringUtils.isEmpty(csid)) {
				throw new Exception("Missing context csid");
			}

			String docType = context.getDocType();

			if (docType.equals(CollectionObjectConstants.NUXEO_DOCTYPE)) {
				results = createVoucherFromCataloging(csid);	
			}
			else if (docType.equals(MovementConstants.NUXEO_DOCTYPE)) {
				results = createVoucherFromCurrentLocation(csid);
			}
			else {
				throw new Exception("Unsupported docType: " + docType);
			}

			completionStatus = STATUS_COMPLETE;
		}
		catch(Exception e) {
			completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS, e.getMessage());
			results.setUserNote(e.getLocalizedMessage());
		}
	}

	public InvocationResults createVoucherFromCataloging(String collectionObjectCsid) throws ResourceException {
		InvocationResults results = new InvocationResults();

		String voucherCsid = createVoucher();
		logger.debug("voucher created: voucherCsid=" + voucherCsid);
		
		String forwardRelationCsid = createRelation(voucherCsid, LoanoutConstants.NUXEO_DOCTYPE, collectionObjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE, RelationConstants.AFFECTS_TYPE);
		String backwardRelationCsid = createRelation(collectionObjectCsid, CollectionObjectConstants.NUXEO_DOCTYPE, voucherCsid, LoanoutConstants.NUXEO_DOCTYPE, RelationConstants.AFFECTS_TYPE);
		logger.debug("relations created: forwardRelationCsid=" + forwardRelationCsid + " backwardRelationCsid=" + backwardRelationCsid);
		
		results.setNumAffected(1);
		results.setPrimaryURICreated("loanout.html?csid=" + voucherCsid);
		results.setUserNote("Voucher created");

		return results;
	}

	public InvocationResults createVoucherFromCurrentLocation(String movementCsid) throws ResourceException, URISyntaxException {
		long numAffected = 0;
		String primaryUriCreated = null;
		
		List<String> collectionObjectCsids = findRelatedCollectionObjects(movementCsid);
		
		// There should only be one, but just in case...
			
		for (String collectionObjectCsid : collectionObjectCsids) {
			InvocationResults innerResults = createVoucherFromCataloging(collectionObjectCsid);
				
			numAffected = numAffected + innerResults.getNumAffected();
				
			if (primaryUriCreated == null) {
				primaryUriCreated = innerResults.getPrimaryURICreated();
			}
		}
		
		InvocationResults results = new InvocationResults();
		results.setNumAffected(numAffected);
		results.setPrimaryURICreated(primaryUriCreated);
		
		if (collectionObjectCsids.size() == 0) {
			results.setUserNote("No related cataloging record was found");
		}
		else {
			results.setUserNote("Voucher created for " + numAffected + " cataloging " + (numAffected == 1 ? "record" : "records"));
		}
		
		return results;
	}

	private String createVoucher() throws ResourceException {
		String voucherCsid = null;
		String voucherNumber = GregorianCalendarDateTimeUtils.timestampUTC();

		String createVoucherPayload = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"loansout\">" +
				"<ns2:loansout_botgarden xmlns:ns2=\"http://collectionspace.org/services/loanout/local/botgarden\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					"<collectorList>" +
						"<collector/>" +
					"</collectorList>" +
				"</ns2:loansout_botgarden>" +
				"<ns2:loansout_common xmlns:ns2=\"http://collectionspace.org/services/loanout\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
					"<loanOutNumber>" + voucherNumber + "</loanOutNumber>" +
				"</ns2:loansout_common>" +
			"</document>";

		ResourceBase resource = resourceMap.get(LoanoutClient.SERVICE_NAME);
		Response response = resource.create(resourceMap, null, createVoucherPayload);

		if (response.getStatus() == CREATED_STATUS) {
			voucherCsid = CollectionSpaceClientUtils.extractId(response);
		}
		else {
			throw new ResourceException(response, "Error creating voucher");
		}

		return voucherCsid;
	}

	private String createRelation(String subjectCsid, String subjectDocType, String objectCsid, String objectDocType, String relationshipType) throws ResourceException {
		String relationCsid = null;

		String createRelationPayload =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"<document name=\"relations\">" +
				"<ns2:relations_common xmlns:ns2=\"http://collectionspace.org/services/relation\" xmlns:ns3=\"http://collectionspace.org/services/jaxb\">" +
					"<subjectCsid>" + subjectCsid + "</subjectCsid>" +
					"<subjectDocumentType>" + subjectDocType + "</subjectDocumentType>" +
					"<objectCsid>" + objectCsid + "</objectCsid>" +
					"<objectDocumentType>" + objectDocType + "</objectDocumentType>" +
					"<relationshipType>" + relationshipType + "</relationshipType>" +
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
	
	private class ResourceException extends Exception {
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
