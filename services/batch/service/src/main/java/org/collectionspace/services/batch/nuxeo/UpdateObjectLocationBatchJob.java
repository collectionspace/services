package org.collectionspace.services.batch.nuxeo;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.MovementClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;

import org.dom4j.DocumentException;


/*
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.XPath;
 */

import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.jdom.Namespace;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob extends AbstractBatchInvocable {

    // FIXME: Where appropriate, get from existing constants rather than local declarations
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA_NAME = "collectionobjects_common";
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common";
    private final static String MOVEMENTS_COMMON_SCHEMA = MovementConstants.NUXEO_SCHEMA_NAME;
    private final static String COMPUTED_CURRENT_LOCATION_ELEMENT_NAME = "computedCurrentLocation";
    private final static String CURRENT_LOCATION_ELEMENT_NAME = "currentLocation";
    private final static String LOCATION_DATE_ELEMENT_NAME = "locationDate";
    private final static String OBJECT_NUMBER_ELEMENT_NAME = "objectNumber";
    private final static Namespace COLLECTIONOBJECTS_COMMON_NAMESPACE =
            Namespace.getNamespace("ns2", "http://collectionspace.org/services/collectionobject");
    private InvocationResults results = new InvocationResults();
    private final String CLASSNAME = this.getClass().getSimpleName();
    private final Logger logger = LoggerFactory.getLogger(UpdateObjectLocationBatchJob.class);

    // Initialization tasks
    public UpdateObjectLocationBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
    }

    /**
     * The main work logic of the batch job. Will be called after setContext.
     */
    @Override
    public void run() {

        setCompletionStatus(STATUS_MIN_PROGRESS);

        try {
            // FIXME: Placeholder during early development
            if (logger.isInfoEnabled()) {
                logger.info("Invoking " + CLASSNAME + " ...");
                logger.info("Invocation context is: " + getInvocationContext().getMode());
            }

            if (!requestedInvocationModeIsSupported()) {
                setInvocationModeNotSupportedResult();
            }

            List<String> csids = new ArrayList<String>();
            if (requestIsForInvocationModeSingle()) {
                String singleCsid = getInvocationContext().getSingleCSID();
                if (Tools.notBlank(singleCsid)) {
                    csids.add(singleCsid);
                }
            } else if (requestIsForInvocationModeList()) {
                List<String> listCsids = (getInvocationContext().getListCSIDs().getCsid());
                if (listCsids == null || listCsids.isEmpty()) {
                    throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT_MESSAGE);
                }
                csids.addAll(listCsids);
            } else if (requestIsForInvocationModeGroup()) {
                String groupCsid = getInvocationContext().getGroupCSID();
                // FIXME: Get individual CSIDs from the group
                // and add them to the list
            }

            if (csids.isEmpty()) {
                throw new Exception(CSID_VALUES_NOT_PROVIDED_IN_INVOCATION_CONTEXT_MESSAGE);
            }

            // Update the current computed location field for each CollectionObject
            setResults(updateComputedCurrentLocations(csids));
            setCompletionStatus(STATUS_COMPLETE);

        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
        }

    }

    // Ray's convenience methods from his AbstractBatchJob class for the UC Berkeley Botanical Garden v2.4 implementation.
    protected PoxPayloadOut findByCsid(String serviceName, String csid) throws URISyntaxException, DocumentException {
        ResourceBase resource = getResourceMap().get(serviceName);
        return findByCsid(resource, csid);
    }

    protected PoxPayloadOut findByCsid(ResourceBase resource, String csid) throws URISyntaxException, DocumentException {
        byte[] response = resource.get(null, createUriInfo(), csid);
        PoxPayloadOut payload = new PoxPayloadOut(response);
        return payload;
    }

    protected UriInfo createUriInfo() throws URISyntaxException {
        return createUriInfo("");
    }

    protected UriInfo createUriInfo(String queryString) throws URISyntaxException {
        URI absolutePath = new URI("");
        URI baseUri = new URI("");
        return new UriInfoImpl(absolutePath, baseUri, "", queryString, Collections.<PathSegment>emptyList());
    }

    protected UriInfo createRelationSearchUriInfo(String subjectCsid, String objType) throws URISyntaxException {
        String queryString = "sbj=" + subjectCsid + "&objType=" + objType;
        URI uri = new URI(null, null, null, queryString, null);
        return createUriInfo(uri.getRawQuery());
    }

    protected String getFieldValue(PoxPayloadOut payload, String partLabel, Namespace partNamespace, String fieldPath) {
        String value = null;
        SAXBuilder builder = new SAXBuilder();
        try {
            Document document = builder.build(new StringReader(payload.toXML()));
            Element root = document.getRootElement();
            Element part = root.getChild(partLabel, partNamespace);
            Element field = part.getChild(fieldPath, partNamespace);
            value = field.getText();
        } catch (Exception e) {
            logger.error("Error getting value from field path " + fieldPath
                    + " in schema part " + partLabel);
            return null;
        }
        return value;
    }

    /**
     * Get a field value from a PoxPayloadOut, given a part name and xpath
     * expression.
     */
    /*
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
     */
    /**
     * Get a field value from a PoxPayloadOut, given a part name and
     * namespace-qualified xpath expression.
     */
    /*
     protected String getFieldValue(PoxPayloadOut payload, String partLabel, String namespacePrefix, String namespace, String fieldPath) {
     String value = null;
     PayloadOutputPart part = payload.getPart(partLabel);

     if (part != null) {
     Element element = part.asElement();
     logger.info(partLabel + " part element =" + element.asXML());

     Map<String, String> namespaceUris = new HashMap<String, String>();
     namespaceUris.put(namespacePrefix, namespace);

     XPath xPath = DocumentHelper.createXPath(fieldPath);
     xPath.setNamespaceURIs(namespaceUris);

     Node node = xPath.selectSingleNode(element);
     // Node node = element.selectSingleNode(fieldPath);

     if (node != null) {
     value = node.getText();
     }
     }

     return value;
     }
     */
    private InvocationResults updateComputedCurrentLocations(List<String> csids) {

        ResourceMap resourcemap = getResourceMap();
        ResourceBase collectionObjectResource = resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        ResourceBase movementResource = resourcemap.get(MovementClient.SERVICE_NAME);
        PoxPayloadOut collectionObjectPayload;
        String objectNumber;
        String computedCurrentLocation;
        int numAffected = 0;

        try {

            // For each CollectionObject record:
            for (String csid : csids) {

                // Get the movement records related to this record

                // FIXME: Create a convenience method for constructing queries like the following
                String queryString = "rtObj=" + csid; // FIXME: Get from constant
                URI uri = new URI(null, null, null, queryString, null);
                UriInfo uriInfo = createUriInfo(uri.getRawQuery());

                AbstractCommonList relatedMovements = movementResource.getList(uriInfo);
                if (logger.isInfoEnabled()) {
                    logger.info("Identified " + relatedMovements.getTotalItems()
                            + " Movement records related to the object CollectionObject record " + csid);
                }

                // Get relation records in the reverse direction as well,
                // merge with records obtained above, and remove duplicates
                queryString = "rtSbj=" + csid; // FIXME: Get from constant
                uri = new URI(null, null, null, queryString, null);
                uriInfo = createUriInfo(uri.getRawQuery());

                AbstractCommonList reverseRelatedMovements = movementResource.getList(uriInfo);
                if (logger.isInfoEnabled()) {
                    logger.info("Identified " + reverseRelatedMovements.getTotalItems()
                            + " Movement records related to the subject CollectionObject record " + csid);
                }

                if ((relatedMovements.getTotalItems() == 0) && reverseRelatedMovements.getTotalItems() == 0) {
                    continue;
                }

                // Merge the two lists
                relatedMovements.getListItem().addAll(reverseRelatedMovements.getListItem());

                // Get the latest movement record from among those, and extract
                // its current location value
                computedCurrentLocation = "";
                String currentLocation;
                String locationDate;
                String mostRecentLocationDate = "";
                for (AbstractCommonList.ListItem movementRecord : relatedMovements.getListItem()) {

                    // FIXME: Add 'de-duping' code here to avoid processing any
                    // related Movement record more than once

                    locationDate = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, LOCATION_DATE_ELEMENT_NAME);
                    if (Tools.isBlank(locationDate)) {
                        continue;
                    }
                    currentLocation = AbstractCommonListUtils.ListItemGetElementValue(movementRecord, CURRENT_LOCATION_ELEMENT_NAME);
                    if (Tools.isBlank(currentLocation)) {
                        continue;
                    }
                    if (logger.isInfoEnabled()) {
                        logger.info("Location date value = " + locationDate);
                        logger.info("Current location value = " + currentLocation);
                    }
                    // Assumes that all values for this element/field will be consistent ISO 8601
                    // date/time representations, each of which can be ordered via string comparison.
                    if (locationDate.compareTo(mostRecentLocationDate) > 0) {
                        mostRecentLocationDate = locationDate;
                        // FIXME: Add validation here that the currentLocation value parses successfully as an item refName
                        computedCurrentLocation = currentLocation;
                    }

                }

                // Update the computed current location value in the CollectionObject record
                collectionObjectPayload = findByCsid(collectionObjectResource, csid);
                if (Tools.notBlank(collectionObjectPayload.toXML())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Payload: " + "\n" + collectionObjectPayload);
                    }
                    // Silently fails at various places in dom4j calls (selectSingleNode, selectNode,
                    // createXpath) in any of the methods tried, without throwing an Exception.
                    //
                    // Those methods are now commented out, in favor of a replacement, however temporary,
                    // using JDOM.
                    //
                    // FIXME: Get namespace from constant; verify whether prefix or URI is required
                    objectNumber = getFieldValue(collectionObjectPayload,
                            COLLECTIONOBJECTS_COMMON_SCHEMA_NAME, COLLECTIONOBJECTS_COMMON_NAMESPACE,
                            OBJECT_NUMBER_ELEMENT_NAME);
                    if (logger.isInfoEnabled()) {
                        logger.info("Object number: " + objectNumber);
                    }
                    if (Tools.notBlank(objectNumber)) {
                        String collectionObjectUpdatePayload =
                                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                                + "<document name=\"collectionobject\">"
                                + "<ns2:collectionobjects_common xmlns:ns2=\"http://collectionspace.org/services/collectionobject\">"
                                + "<objectNumber>" + objectNumber + "</objectNumber>"
                                + "<computedCurrentLocation>" + computedCurrentLocation + "</computedCurrentLocation>"
                                + "</ns2:collectionobjects_common></document>";
                        if (logger.isInfoEnabled()) {
                            logger.info("Update payload: " + "\n" + collectionObjectUpdatePayload);
                        }
                        byte[] response = collectionObjectResource.update(resourcemap, null, csid, collectionObjectUpdatePayload);
                        numAffected++;
                        if (logger.isTraceEnabled()) {
                            logger.trace("Computed current location value for CollectionObject " + csid + " set to " + computedCurrentLocation);

                        }
                    }

                }
            }
        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage() + " ";
            errMsg = errMsg + "Successfully updated " + numAffected + " CollectionObject record(s) prior to error.";
            logger.error(errMsg);
            setErrorResult(errMsg);
            getResults().setNumAffected(numAffected);
            return getResults();
        }

        getResults().setNumAffected(numAffected);
        return getResults();
    }
}
