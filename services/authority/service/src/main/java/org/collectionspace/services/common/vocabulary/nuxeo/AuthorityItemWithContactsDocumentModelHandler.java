package org.collectionspace.services.common.vocabulary.nuxeo;

import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.AuthorityResourceWithContacts;
import org.nuxeo.ecm.core.api.DocumentModel;

public abstract class AuthorityItemWithContactsDocumentModelHandler<AICommon> extends AuthorityItemDocumentModelHandler<AICommon> {

    public AuthorityItemWithContactsDocumentModelHandler(String authorityCommonSchemaName,
            String authorityItemCommonSchemaName) {
        super(authorityCommonSchemaName, authorityItemCommonSchemaName);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void completeCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.completeCreate(wrapDoc);
        handleContactPayload(wrapDoc);
    }

    private void handleContactPayload(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        ContactClient contactClient = new ContactClient();
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        PoxPayloadIn input = ctx.getInput();
        PayloadInputPart contactPart = input.getPart(contactClient.getCommonPartName());
        if (contactPart != null) {
            DocumentModel docModel = wrapDoc.getWrappedObject();
            String authorityCsid = this.getInAuthorityCsid();
            String itemCsid = this.getCsid(docModel);
            createContact(authorityCsid, itemCsid, contactPart);
        }
    }

    private void createContact(String authorityCsid, String itemCsid, PayloadInputPart contactPart) throws Exception {
        ContactClient contactClient = new ContactClient();
        String payloadTemplate = "<?xml version='1.0' encoding='UTF-8'?><document>%s</document>";
        String xmlPayload = String.format(payloadTemplate, contactPart.asXML());
        PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
                
        AuthorityResourceWithContacts contactResource = (AuthorityResourceWithContacts) getServiceContext().getResource();
        
        contactResource.createContact(getServiceContext(), authorityCsid, itemCsid, input, null);
    }
}
