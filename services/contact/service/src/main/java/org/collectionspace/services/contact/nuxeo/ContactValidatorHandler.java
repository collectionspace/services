package org.collectionspace.services.contact.nuxeo;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.document.ValidatorHandlerImpl;
import org.collectionspace.services.contact.ContactsCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactValidatorHandler extends ValidatorHandlerImpl<PoxPayloadIn, PoxPayloadOut> {

    private static final Logger logger = LoggerFactory.getLogger(ContactValidatorHandler.class);

    // Error strings
    private static final String VALIDATION_ERROR =
        "The contact record payload was invalid. See log file for more details.";

    @Override
    protected Class<?> getCommonPartClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void handleCreate() throws InvalidDocumentException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleGet() throws InvalidDocumentException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleGetAll() throws InvalidDocumentException {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleUpdate() throws InvalidDocumentException {
        try {
            ContactsCommon contactsCommon = (ContactsCommon) getCommonPart();
        } catch (AssertionError e) {
            logger.error("Exception validating ContactsCommon", e);
            throw new InvalidDocumentException(VALIDATION_ERROR, e);
        }
    }

    @Override
    protected void handleDelete() throws InvalidDocumentException {
        // TODO Auto-generated method stub
    }

}
