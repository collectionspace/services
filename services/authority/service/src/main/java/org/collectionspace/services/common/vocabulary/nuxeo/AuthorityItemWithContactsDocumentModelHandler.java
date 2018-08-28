package org.collectionspace.services.common.vocabulary.nuxeo;

import java.util.List;

import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.AuthorityResourceWithContacts;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
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
        handleContactCreate(wrapDoc);
    }
    
    @Override
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.completeUpdate(wrapDoc);
        handleContactUpdate(wrapDoc);
    }

    private void handleContactCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String authorityCsid = this.getInAuthorityCsid();
        String itemCsid = this.getCsid(docModel);
        PoxPayloadIn input = ctx.getInput();
        ContactClient contactClient = new ContactClient();
        //
        // There may be multiple contact payloads
        //
        List<PayloadInputPart> contactPartList = input.getParts(contactClient.getCommonPartName());
        for (PayloadInputPart contactPart: contactPartList) {
            createContact(authorityCsid, itemCsid, contactPart);                    
        }
    }

    private void createContact(String authorityCsid, String itemCsid, PayloadInputPart contactPart) throws Exception {
        String payloadTemplate = "<?xml version='1.0' encoding='UTF-8'?><document>%s</document>";
        String xmlPayload = String.format(payloadTemplate, contactPart.asXML());
        PoxPayloadIn input = new PoxPayloadIn(xmlPayload);
                
        AuthorityResourceWithContacts contactResource = (AuthorityResourceWithContacts) getServiceContext().getResource();
        contactResource.createContact(getServiceContext(), authorityCsid, itemCsid, input, null);
    }
    
    private boolean csidInList(String csid, AbstractCommonList existingContactsList) {
        boolean result = false;
        
        List<ListItem> itemList = existingContactsList.getListItem();
        for (ListItem item: itemList) {
            if (getCsid(item).equalsIgnoreCase(csid)) {
                result = true;
                break;
            }
        }
        
        return result;
    }
    
    /*
     * Updates the contact list for an authority item.  *** warning *** Will not page through all existing contacts, so if the authority
     * items has more than a page of contacts (page size is 40 by default), this behavior of this function is
     * undefined.
     */
    private void handleContactUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
        DocumentModel docModel = wrapDoc.getWrappedObject();
        String authorityCsid = this.getInAuthorityCsid();
        String itemCsid = this.getCsid(docModel);
        PoxPayloadIn input = ctx.getInput();
        ContactClient contactClient = new ContactClient();
        
        AuthorityResourceWithContacts contactResource = (AuthorityResourceWithContacts) ctx.getResource();
        AbstractCommonList existingContactList = contactResource.getContactList(ctx, authorityCsid, itemCsid, null);
        List<PayloadInputPart> contactPartList = input.getParts(contactClient.getCommonPartName());

        //
        // If there are no update contact payloads, then return
        //
        if (contactPartList.isEmpty()) {
            return;
        }
        
        //
        // If there are no existing contacts and we're given contact payloads then we'll
        // assume the requester wants us to create new contact records
        //
        if (existingContactList.getTotalItems() == 0 && !contactPartList.isEmpty()) {
            String payloadTemplate = "<?xml version='1.0' encoding='UTF-8'?><document>%s</document>";
            for (PayloadInputPart contactPart: contactPartList) {
                String xmlPayload = String.format(payloadTemplate, contactPart.asXML());
                PoxPayloadIn contactInput = new PoxPayloadIn(xmlPayload);
                contactResource.createContact(getServiceContext(), authorityCsid, itemCsid, contactInput, null);                
            }
            
            return;
        }
        
        //
        // If there are more update payloads than existing contacts then fail with an exception.
        //
        // TODO: We could try to find the existing ones and perform updates and assume additional
        // contact payloads need to be used to create new contacts.
        //
        if (existingContactList.getTotalItems() < contactPartList.size()) {
            throw new DocumentException("There are more update payloads than existing contacts.");
        }
        
        //
        // If there is only one existing contact and only 1 update payload being sent then
        // update the existing contact -assuming the CSID (if any) in the update payload matches
        // the CSID of the existing contact.  If the incoming payload has no CSID specified then
        // we'll use the payload to update the existing contact record.
        //
        if (existingContactList.getTotalItems() == 1 && contactPartList.size() == 1) {
            String existingContactCsid = this.getCsid(existingContactList.getListItem().get(0));
            PayloadInputPart contactPart = contactPartList.get(0);
            ContactsCommon contactUpdate = (ContactsCommon) contactPart.getBody();
            
            if (Tools.isEmpty(contactUpdate.getCsid()) == true) {
                //
                // Assume the incoming update payload refers to the one (and only) existing contact
                //
                contactResource.updateContact(ctx, authorityCsid, itemCsid, existingContactCsid, contactPart);
                return;
            } else {
                if (contactUpdate.getCsid().equalsIgnoreCase(existingContactCsid)) {
                    contactResource.updateContact(ctx, authorityCsid, itemCsid, existingContactCsid, contactPart);
                    return;
                } else {
                    throw new DocumentException("The CSID's of the contact being requested an update does not match the existing contact CSID.");
                }
            }
        }
        
        //
        // If we've made it this far, it better mean that the number of update payloads is the same as the
        // number of existing contacts.  If so, all the payloads need to have CSIDs that match a CSID of an existing
        // contact
        //
        if (existingContactList.getTotalItems() == contactPartList.size()) {
            for (PayloadInputPart contactPart: contactPartList) {
                ContactsCommon contact = (ContactsCommon) contactPart.getBody();
                if (Tools.isEmpty(contact.getCsid()) == false) {
                    if (csidInList(contact.getCsid(), existingContactList)) {
                        contactResource.updateContact(ctx, authorityCsid, itemCsid, contact.getCsid(), contactPart);
                    }
                } else {
                    throw new DocumentException("Contact payload for update did not contain a CSID.");
                }
            }
        } else {
            throw new DocumentException("The number of update payloads does not match the number of existing contacts.");
        }
        

    }    
}
