package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.collectionspace.services.client.ContactClient;
import org.collectionspace.services.contact.ContactsCommon;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContactClientUtils {

    private static final Logger logger =
        LoggerFactory.getLogger(ContactClientUtils.class);

    public static MultipartOutput createContactInstance(String identifier, String headerLabel) {
        String inAuthority = "";
        String inItem = "";
        return createContactInstance(
            inAuthority,
            inItem,
            "addressText1-" + identifier,
            "postcode-" + identifier,
            "addressType1-" + identifier,
            headerLabel);
    }

    public static MultipartOutput createContactInstance(
        String inAuthority, String inItem, String identifier, String headerLabel) {
        return createContactInstance(
            inAuthority,
            inItem,
            "addressText1-" + identifier,
            "postcode-" + identifier,
            "addressType1-" + identifier,
            headerLabel);
    }

    public static MultipartOutput createContactInstance(
        String inAuthority, String inItem, String addressText,
        String postcode, String addressType, String headerLabel) {
        ContactsCommon contact = new ContactsCommon();
        contact.setInAuthority(inAuthority);
        contact.setInItem(inItem);
        contact.setAddressText1(addressText);
        contact.setPostcode1(postcode);
        contact.setAddressType1(addressType);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(contact, MediaType.APPLICATION_XML_TYPE);
        ContactClient client = new ContactClient();
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, contact common");
            // logger.debug(objectAsXmlString(contact, ContactsCommon.class));
        }

        return multipart;
    }


}
