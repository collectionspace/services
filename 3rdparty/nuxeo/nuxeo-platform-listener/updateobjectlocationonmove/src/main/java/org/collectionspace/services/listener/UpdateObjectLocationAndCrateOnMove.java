package org.collectionspace.services.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

public class UpdateObjectLocationAndCrateOnMove extends UpdateObjectLocationOnMove {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    private final Log logger = LogFactory.getLog(UpdateObjectLocationAndCrateOnMove.class);
    // FIXME: Get values below from external constants
    private final static String COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA = "collectionobjects_anthropology";
    private final static String MOVEMENTS_ANTHROPOLOGY_SCHEMA = "movements_anthropology";
    private final static String CRATE_PROPERTY = "crate";
    // FIXME: Verify this value against the final schema name for this field, created via PAHMA-647 
    private final static String COMPUTED_CRATE_PROPERTY = "computedCrate";

    @Override
    protected DocumentModel updateCollectionObjectValuesFromMovement(DocumentModel collectionObjectDocModel,
            DocumentModel movementDocModel) throws ClientException {
        collectionObjectDocModel = updateComputedCurrentLocationValue(collectionObjectDocModel, movementDocModel);
        collectionObjectDocModel = updateComputedCrateValue(collectionObjectDocModel, movementDocModel);
        return collectionObjectDocModel;
    }

    protected DocumentModel updateComputedCrateValue(DocumentModel collectionObjectDocModel,
            DocumentModel movementDocModel)
            throws ClientException {
        
        // Get the current crate value from the Movement.
        String crateRefName =
                (String) movementDocModel.getProperty(MOVEMENTS_ANTHROPOLOGY_SCHEMA, CRATE_PROPERTY);

        // Check that the value returned, which is expected to be a
        // reference (refName) to an authority term is, at a minimum:
        //
        // * Non-null and non-blank.
        //   (Note: we need to verify this assumption; can a CollectionObject's
        //   computed current crate value ever meaningfully be 'un-set'
        //   by returning it to a null value?)
        //
        // * Capable of being successfully parsed by an authority item parser;
        //   that is, returning a non-null parse result.
        if ((Tools.isBlank(crateRefName)
                || (RefNameUtils.parseAuthorityTermInfo(crateRefName) == null))) {
            logger.warn("Could not parse crate refName '" + crateRefName + "'");
            return collectionObjectDocModel;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("crate refName passes basic validation tests.");
                logger.trace("crate refName=" + crateRefName);
            }
        }
        // If the value returned from the function passes validation,
        // compare it to the value in the crate field of the CollectionObject.
        String existingCrateRefName =
                (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA,
                COMPUTED_CRATE_PROPERTY);
        if (logger.isTraceEnabled()) {
            logger.trace("Existing crate refName=" + existingCrateRefName);
        }

        // If the CollectionObject lacks a crate value, or if the new value
        // differs from its existing value ...
        if (Tools.isBlank(existingCrateRefName)
                || (!crateRefName.equals(existingCrateRefName))) {
            if (logger.isTraceEnabled()) {
                logger.trace("crate refName requires updating.");
            }
            // ... update that value.
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA,
                    COMPUTED_CRATE_PROPERTY, crateRefName);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("crate refName does NOT require updating.");
            }
        }
        
        return collectionObjectDocModel;
    }
}