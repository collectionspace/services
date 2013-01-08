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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.batch.AbstractBatchInvocable;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionSpaceClientUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.jboss.resteasy.specimpl.UriInfoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectLocationBatchJob extends AbstractBatchInvocable {

    // FIXME; Get from existing constants and replace these local declarations
    final static String COLLECTIONOBJECTS_COMMON_SCHEMA_NAME = "collectionobjects_common";
    final static String OBJECT_NUMBER_FIELD_NAME = "objectNumber";
    private InvocationResults results = new InvocationResults();
    final String CLASSNAME = this.getClass().getSimpleName();
    final Logger logger = LoggerFactory.getLogger(UpdateObjectLocationBatchJob.class);

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

    /**
     * Get a field value from a PoxPayloadOut, given a part name and namespace-qualified xpath
     * expression.
     */
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
    
    private InvocationResults updateComputedCurrentLocations(List<String> csids) {
        
        ResourceMap resourcemap = getResourceMap();
        ResourceBase collectionObjectResource = resourcemap.get(CollectionObjectClient.SERVICE_NAME);
        PoxPayloadOut collectionObjectPayload;
        String objectNumber;
        String computedCurrentLocation;
        int numAffected = 0;
        // FIXME: Temporary during testing/development
        final String COMPUTED_CURRENT_LOCATION = "FOO_COMPUTED_CURRENT_LOCATION";
        
        try {

            // For each CollectionObject record:
            for (String csid : csids) {
                // Get the movement records related to this record
                // Get the latest movement record from among those
                // Extract its current location value
                // FIXME: Temporary during testing/development
                computedCurrentLocation = COMPUTED_CURRENT_LOCATION;
                // Update the computed current location value in the CollectionObject record
                collectionObjectPayload = findByCsid(collectionObjectResource, csid);
                if (Tools.notBlank(collectionObjectPayload.toXML())) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Payload: " + "\n" + collectionObjectPayload);
                    }
                    // Silenly fails at various places in dom4j calls (selectSingleNode, selectNode,
                    // createXpath) in any of the methods tried above, without throwing an Exception
                    /*
                    objectNumber = getFieldValue(collectionObjectPayload,
                            COLLECTIONOBJECTS_COMMON_SCHEMA_NAME,
                            "ns2", "http://collectionspace.org/services/collectionobject",
                            OBJECT_NUMBER_FIELD_NAME);
                    if (logger.isInfoEnabled()) {
                        logger.info("Object number: " + objectNumber);
                    }
                    */
                    objectNumber = "BAR"; // FIXME
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
                        /*
                         * if (response.getStatus() != OK_STATUS) {
                         String errMsg = "Error encountered in " + CLASSNAME + ": " + "Updating CollectionObject failed.";
                         setErrorResult(errMsg);
                         } else {
                         results.setUserNote("Computed current location value for CollectionObject " + csid + " set to " + computedCurrentLocation);
                         numAffected++;
                         }
                         */
                    }
                    
                }
            }
        } catch (Exception e) {
            String errMsg = "Error encountered in " + CLASSNAME + ": " + e.getLocalizedMessage();
            setErrorResult(errMsg);
            getResults().setNumAffected(numAffected);
            return getResults();
        }
        getResults().setNumAffected(numAffected);
        return getResults();
    }
}
