package org.collectionspace.services.client;

import java.util.List;
import javax.ws.rs.core.MediaType;

import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.contact.AddressGroup;
import org.collectionspace.services.contact.AddressGroupList;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.EmailGroup;
import org.collectionspace.services.contact.EmailGroupList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactClientUtils {

    private static final Logger logger =
        LoggerFactory.getLogger(ContactClientUtils.class);

    public static PoxPayloadOut createContactInstance(String identifier, String headerLabel) {
        final String inAuthority = "";
        final String inItem = "";
        return createContactInstance(
            inAuthority,
            inItem,
            "email-" + identifier,
            "addressType-" + identifier,
            "addressPlace-" + identifier,
            headerLabel);
    }

    public static PoxPayloadOut createContactInstance(
        String inAuthority, String inItem, String identifier, String headerLabel) {
        return createContactInstance(
            inAuthority,
            inItem,
            "email-" + identifier,
            "addressType-" + identifier,
            "addressPlace-" + identifier,
            headerLabel);
    }

    public static PoxPayloadOut createContactInstance(
        String inAuthority, String inItem, String email,
        String addressType, String addressPlace, String headerLabel) {
        ContactsCommon contact = new ContactsCommon();
        
        contact.setInAuthority(inAuthority);
        contact.setInItem(inItem);
        
        EmailGroupList emailGroupList = new EmailGroupList();
        List<EmailGroup> emailGroups = emailGroupList.getEmailGroup();
        EmailGroup emailGroup = new EmailGroup();
        emailGroup.setEmail(email);
        emailGroups.add(emailGroup);
        contact.setEmailGroupList(emailGroupList);
        
        AddressGroupList addressGroupList = new AddressGroupList();
        List<AddressGroup> addressGroups = addressGroupList.getAddressGroup();
        AddressGroup addressGroup = new AddressGroup();
        addressGroup.setAddressType(addressType);
        addressGroup.setAddressPlace1(addressPlace);
        addressGroups.add(addressGroup);
        contact.setAddressGroupList(addressGroupList);
        
        PoxPayloadOut multipart = new PoxPayloadOut(ContactClient.SERVICE_PAYLOAD_NAME);
        @SuppressWarnings("deprecation")
		PayloadOutputPart commonPart =
            multipart.addPart(contact, MediaType.APPLICATION_XML_TYPE);
//        ContactClient client = new ContactClient();
        commonPart.setLabel(headerLabel);

        return multipart;
    }

}
