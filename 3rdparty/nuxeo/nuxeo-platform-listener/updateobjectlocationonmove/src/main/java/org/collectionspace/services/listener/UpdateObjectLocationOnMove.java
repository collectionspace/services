package org.collectionspace.services.listener;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

public class UpdateObjectLocationOnMove extends AbstractUpdateObjectLocationValues {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    private final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);

    @Override
    protected DocumentModel updateCollectionObjectValuesFromMovement(DocumentModel collectionObjectDocModel,
            DocumentModel movementDocModel) throws ClientException {

        collectionObjectDocModel = updateComputedCurrentLocationValue(collectionObjectDocModel, movementDocModel);
        // This method can be overridden and extended by adding or removing method
        // calls here, to update a custom set of values in the CollectionObject
        // record by pulling in values from the related Movement record.
        return collectionObjectDocModel;
    }

    protected DocumentModel updateComputedCurrentLocationValue(DocumentModel collectionObjectDocModel,
            DocumentModel movementDocModel)
            throws ClientException {

        // Get the current location value from the Movement (the "new" value)
        String currentLocationRefName =
                (String) movementDocModel.getProperty(MOVEMENTS_COMMON_SCHEMA, CURRENT_LOCATION_PROPERTY);

        // Check that the value returned, which is expected to be a
        // reference (refName) to an authority term (such as a storage
        // location or organization term):
        //
        // * If it is not blank ...
        // * Is then capable of being successfully parsed by an authority item parser.
        if ((Tools.notBlank(currentLocationRefName)
                && (RefNameUtils.parseAuthorityTermInfo(currentLocationRefName) == null))) {
            logger.warn("Could not parse current location refName '" + currentLocationRefName + "'");
            return collectionObjectDocModel;
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("current location refName passes basic validation tests.");
                logger.trace("currentLocation refName=" + currentLocationRefName);
            }
        }
        
        // Get the computed current location value of the CollectionObject
        // (the "existing" value)
        String existingComputedCurrentLocationRefName =
                (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA,
                COMPUTED_CURRENT_LOCATION_PROPERTY);
        if (logger.isTraceEnabled()) {
            logger.trace("Existing computedCurrentLocation refName=" + existingComputedCurrentLocationRefName);
        }

        // If the new value is blank, the existing value should always be
        // overwritten ('nulled out') with a blank value.
        if (Tools.isBlank(currentLocationRefName)) {
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA,
                    COMPUTED_CURRENT_LOCATION_PROPERTY, (Serializable) null);
            // Otherwise, if the existing value is blank, or if the new value
            // is different than the existing value ...
        } else if (Tools.isBlank(existingComputedCurrentLocationRefName)
                || (!currentLocationRefName.equals(existingComputedCurrentLocationRefName))) {
            if (logger.isTraceEnabled()) {
                logger.trace("computedCurrentLocation refName requires updating.");
            }
            // ... update the existing value in the CollectionObject with the
            // new value from the Movement.
            collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA,
                    COMPUTED_CURRENT_LOCATION_PROPERTY, currentLocationRefName);

        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("computedCurrentLocation refName does NOT require updating.");
            }
        }
        return collectionObjectDocModel;
    }
}