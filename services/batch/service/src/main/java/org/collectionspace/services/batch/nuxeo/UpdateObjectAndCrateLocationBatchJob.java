package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.dom4j.DocumentException;
import org.jdom.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateObjectAndCrateLocationBatchJob extends UpdateObjectLocationBatchJob {

    // FIXME: Where appropriate, get from existing constants rather than local declarations
    private final static String COMPUTED_CURRENT_LOCATION_ELEMENT_NAME = "computedCurrentLocation";
    private final static String CRATE_ELEMENT_NAME = "crate";
    private final static String COMPUTED_CRATE_ELEMENT_NAME = "computedCrate";
    private final static String CURRENT_LOCATION_ELEMENT_NAME = "currentLocation";
    private final static String COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA_NAME = "collectionobjects_anthropology";
    private final static String COLLECTIONOBJECTS_ANTHROPOLOGY_NAMESPACE_PREFIX = "ns2";
    private final static String COLLECTIONOBJECTS_ANTHROPOLOGY_NAMESPACE_URI =
            "http://collectionspace.org/services/collectionobject/domain/anthropology";
    private final static Namespace COLLECTIONOBJECTS_ANTHROPOLOGY_NAMESPACE =
            Namespace.getNamespace(
            COLLECTIONOBJECTS_ANTHROPOLOGY_NAMESPACE_PREFIX,
            COLLECTIONOBJECTS_ANTHROPOLOGY_NAMESPACE_URI);
    private final static String COLLECTIONOBJECTS_COMMON_SCHEMA_NAME = "collectionobjects_common";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX = "ns2";
    private final static String COLLECTIONOBJECTS_COMMON_NAMESPACE_URI =
            "http://collectionspace.org/services/collectionobject";
    private final static Namespace COLLECTIONOBJECTS_COMMON_NAMESPACE =
            Namespace.getNamespace(
            COLLECTIONOBJECTS_COMMON_NAMESPACE_PREFIX,
            COLLECTIONOBJECTS_COMMON_NAMESPACE_URI);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Initialization tasks
    public UpdateObjectAndCrateLocationBatchJob() {
        super();
    }

    // This method can be overridden and extended to update a custom set of
    // values in the CollectionObject record by pulling in values from its
    // most recent related Movement record.
    //
    // Note: any such values must first be exposed in Movement list items,
    // in turn via configuration in Services tenant bindings ("listResultsField").
    @Override
    protected int updateCollectionObjectValues(ResourceBase collectionObjectResource,
            String collectionObjectCsid, AbstractCommonList.ListItem mostRecentMovement,
            ResourceMap resourcemap, int numUpdated)
            throws DocumentException, URISyntaxException {
        PoxPayloadOut collectionObjectPayload;
        String computedCrate;
        String computedCurrentLocation;
        String previousComputedCrate;
        String previousComputedCurrentLocation;

        collectionObjectPayload = findByCsid(collectionObjectResource, collectionObjectCsid);
        if (Tools.isBlank(collectionObjectPayload.toXML())) {
            return numUpdated;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Payload: " + "\n" + collectionObjectPayload);
            }
        }
        // Perform the update only if either the computed current location value
        // or the computed crate value will change as a result of the update
        computedCurrentLocation =
                AbstractCommonListUtils.ListItemGetElementValue(mostRecentMovement, CURRENT_LOCATION_ELEMENT_NAME);
        previousComputedCurrentLocation = getFieldElementValue(collectionObjectPayload,
                COLLECTIONOBJECTS_COMMON_SCHEMA_NAME, COLLECTIONOBJECTS_COMMON_NAMESPACE,
                COMPUTED_CURRENT_LOCATION_ELEMENT_NAME);
        if (logger.isTraceEnabled()) {
            logger.trace("computedCurrentLocation=" + computedCurrentLocation);
            logger.trace("previousComputedCurrentLocation=" + previousComputedCurrentLocation);
        }

        // As noted above, the value of the 'crate' field in the 'movements_anthropology' schema
        // must first be configured, via Services tenant bindings, to be returned in items in
        // list results for Movement records, for the following statement to work.
        computedCrate =
                AbstractCommonListUtils.ListItemGetElementValue(mostRecentMovement, CRATE_ELEMENT_NAME);
        previousComputedCrate = getFieldElementValue(collectionObjectPayload,
                COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA_NAME, COLLECTIONOBJECTS_ANTHROPOLOGY_NAMESPACE,
                COMPUTED_CRATE_ELEMENT_NAME);
        if (logger.isTraceEnabled()) {
            logger.trace("computedCrate=" + computedCrate);
            logger.trace("previousComputedCrate=" + previousComputedCrate);
        }

        if ((Tools.notBlank(previousComputedCurrentLocation)
                && computedCurrentLocation.equals(previousComputedCurrentLocation))
                && (Tools.notBlank(previousComputedCrate) && computedCrate.equals(previousComputedCrate))) {
            return numUpdated;
        }

        String collectionObjectUpdatePayload =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<document name=\"collectionobject\">"
                + "  <ns2:collectionobjects_common "
                + "      xmlns:ns2=\"http://collectionspace.org/services/collectionobject\">"
                + "    <ns2:computedCurrentLocation>" + computedCurrentLocation + "</ns2:computedCurrentLocation>"
                + "  </ns2:collectionobjects_common>"
                + "  <ns2:collectionobjects_anthropology "
                + "      xmlns:ns2=\"http://collectionspace.org/services/collectionobject/domain/anthropology\">"
                + "    <computedCrate>" + computedCrate + "</computedCrate>"
                + "  </ns2:collectionobjects_anthropology>"
                + "</document>";

        if (logger.isTraceEnabled()) {
            logger.trace("Update payload: " + "\n" + collectionObjectUpdatePayload);
        }
        byte[] response = collectionObjectResource.update(resourcemap, null, collectionObjectCsid,
                collectionObjectUpdatePayload);
        numUpdated++;
        if (logger.isTraceEnabled()) {
            logger.trace("Computed current location value for CollectionObject " + collectionObjectCsid
                    + " was set to " + computedCurrentLocation);
            logger.trace("Computed crate value for CollectionObject " + collectionObjectCsid
                    + " was set to " + computedCrate);
        }
        return numUpdated;
    }
}
