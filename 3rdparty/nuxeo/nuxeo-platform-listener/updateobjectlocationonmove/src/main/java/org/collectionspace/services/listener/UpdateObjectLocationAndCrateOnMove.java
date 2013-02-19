package org.collectionspace.services.listener;

import java.io.Serializable;
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
        
        // Get the current crate value from the Movement (the "new" value)
        String crateRefName =
                (String) movementDocModel.getProperty(MOVEMENTS_ANTHROPOLOGY_SCHEMA, CRATE_PROPERTY);

        // Check that the value returned, which is expected to be a
        // reference (refName) to an authority term:
        //
        // * If it is not blank ...
        // * Is then capable of being successfully parsed by an authority item parser.
        if (Tools.notBlank(crateRefName)
                && RefNameUtils.parseAuthorityTermInfo(crateRefName) == null) {
            logger.warn("Could not parse crate refName '" + crateRefName + "'");
            return collectionObjectDocModel;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("crate refName passes basic validation tests.");
                logger.trace("crate refName=" + crateRefName);
            }
        }
        // Get the computed crate value of the CollectionObject
        // (the "existing" value)
        String existingCrateRefName =
                (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA,
                COMPUTED_CRATE_PROPERTY);
        if (logger.isTraceEnabled()) {
            logger.trace("Existing crate refName=" + existingCrateRefName);
        }

        // If the new value is blank, any non-blank existing value should always
        // be overwritten ('nulled out') with a blank value.
        if (Tools.isBlank(crateRefName) && Tools.notBlank(existingCrateRefName)) {
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_ANTHROPOLOGY_SCHEMA,
                    COMPUTED_CRATE_PROPERTY, (Serializable) null);
            // Otherwise, if the new value is not blank, and
            // * the existing value is blank, or
            // * the new value is different than the existing value ...
        } else if (Tools.notBlank(crateRefName) &&
                    (Tools.isBlank(existingCrateRefName)
                    || !crateRefName.equals(existingCrateRefName))) {
            if (logger.isTraceEnabled()) {
                logger.trace("crate refName requires updating.");
            }
            // ... update the existing value in the CollectionObject with the
            // new value from the Movement.
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