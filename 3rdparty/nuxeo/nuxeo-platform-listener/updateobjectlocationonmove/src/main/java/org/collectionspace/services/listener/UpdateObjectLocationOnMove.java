package org.collectionspace.services.listener;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public class UpdateObjectLocationOnMove extends AbstractUpdateObjectLocationValues {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    private final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);
    
    @Override
    protected void updateAllLocationValues(CoreSession coreSession, String collectionObjectCsid)
            throws ClientException {
        updateCurrentLocationValue(coreSession, collectionObjectCsid);
    }

    private void updateCurrentLocationValue(CoreSession coreSession, String collectionObjectCsid)
            throws ClientException {
        DocumentModel collectionObjectDocModel;
        String computedCurrentLocationRefName;
        collectionObjectDocModel = getDocModelFromCsid(coreSession, collectionObjectCsid);
        if (collectionObjectDocModel == null) {
            return;
        }
        // Verify that the CollectionObject record is active.
        if (!isActiveDocument(collectionObjectDocModel)) {
            return;
        }
        // Obtain the computed current location of that CollectionObject.
        computedCurrentLocationRefName = computeCurrentLocation(coreSession, collectionObjectCsid);
        if (logger.isTraceEnabled()) {
            logger.trace("computedCurrentLocation refName=" + computedCurrentLocationRefName);
        }

        // Check that the value returned, which is expected to be a
        // reference (refName) to a storage location authority term,
        // is, at a minimum:
        // * Non-null and non-blank. (We need to verify this assumption; can a
        //   CollectionObject's computed current location value ever meaningfully
        //   be 'un-set' by returning it to a null value?)
        // * Capable of being successfully parsed by an authority item parser;
        //   that is, returning a non-null parse result.
        if ((Tools.isBlank(computedCurrentLocationRefName)
                || (RefNameUtils.parseAuthorityTermInfo(computedCurrentLocationRefName) == null))) {
            logger.warn("Could not parse computed current location refName '" + computedCurrentLocationRefName + "'");
            return;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("computed current location refName passes basic validation tests.");
            }
        }

        // If the value returned from the function passes validation,
        // compare it to the value in the computedCurrentLocation
        // field of that CollectionObject.
        String existingComputedCurrentLocationRefName =
                (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
        if (logger.isTraceEnabled()) {
            logger.trace("Existing computedCurrentLocation refName=" + existingComputedCurrentLocationRefName);
        }
        // If the CollectionObject lacks a computed current location value,
        // or if the new computed value differs from its existing value ...
        if (Tools.isBlank(existingComputedCurrentLocationRefName)
                || (!computedCurrentLocationRefName.equals(existingComputedCurrentLocationRefName))) {
            if (logger.isTraceEnabled()) {
                logger.trace("computedCurrentLocation refName requires updating.");
            }
            // ... update that value and then save the updated CollectionObject.
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY, computedCurrentLocationRefName);
            coreSession.saveDocument(collectionObjectDocModel);
            if (logger.isTraceEnabled()) {
                String afterUpdateComputedCurrentLocationRefName =
                        (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
                logger.trace("Following update, new computedCurrentLocation refName value=" + afterUpdateComputedCurrentLocationRefName);

            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("computedCurrentLocation refName does NOT require updating.");
            }
        }
    }
}