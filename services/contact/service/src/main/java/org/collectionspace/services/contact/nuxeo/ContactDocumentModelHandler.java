/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 Regents of the University of California

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.contact.nuxeo;

import java.util.Map;

import javax.ws.rs.core.UriInfo;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.contact.ContactsCommon;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ContactDocumentModelHandler.
 */
public class ContactDocumentModelHandler
        extends NuxeoDocumentModelHandler<ContactsCommon> {

    private final Logger logger = LoggerFactory.getLogger(ContactDocumentModelHandler.class);
    private static final String COMMON_PART_LABEL = "contacts_common";
    private String inAuthority;
    private String inItem;

    /**
     * Gets the in authority.
     *
     * @return the in authority
     */
    public String getInAuthority() {
        return inAuthority;
    }

    /**
     * Sets the in authority.
     *
     * @param inAuthority the new in item
     */
    public void setInAuthority(String inAuthority) {
        this.inAuthority = inAuthority;
    }

    /**
     * Gets the in item.
     *
     * @return the in item
     */
    public String getInItem() {
        return inItem;
    }

    /**
     * Sets the in item.
     *
     * @param inItem the new in item
     */
    public void setInItem(String inItem) {
        this.inItem = inItem;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // first fill all the parts of the document
        super.handleCreate(wrapDoc);
        handleInAuthority(wrapDoc.getWrappedObject());
        handleDisplayNames(wrapDoc.getWrappedObject());
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        super.handleUpdate(wrapDoc);
        handleDisplayNames(wrapDoc.getWrappedObject());
    }

    /**
     * Check the logic around the parent pointer. Note that we only need do this on
     * create, since we have logic to make this read-only on update. 
     * 
     * @param docModel
     * 
     * @throws Exception the exception
     */
    private void handleInAuthority(DocumentModel docModel) throws Exception {
        String commonPartLabel = getServiceContext().getCommonPartLabel("contacts");
        docModel.setProperty(commonPartLabel,
                ContactJAXBSchema.IN_AUTHORITY, inAuthority);
        docModel.setProperty(commonPartLabel,
                ContactJAXBSchema.IN_ITEM, inItem);
    }

    private void handleDisplayNames(DocumentModel docModel) throws Exception {
        String commonPartLabel = getServiceContext().getCommonPartLabel("contacts");
        String email = getStringValueInPrimaryRepeatingComplexProperty(
                docModel, commonPartLabel, ContactJAXBSchema.EMAIL_GROUP_LIST,
                ContactJAXBSchema.EMAIL);
        String telephoneNumber = getStringValueInPrimaryRepeatingComplexProperty(
                docModel, commonPartLabel, ContactJAXBSchema.TELEPHONE_NUMBER_GROUP_LIST,
                ContactJAXBSchema.TELEPHONE_NUMBER);
        String addressPlace1 = getStringValueInPrimaryRepeatingComplexProperty(
                docModel, commonPartLabel, ContactJAXBSchema.ADDRESS_GROUP_LIST,
                ContactJAXBSchema.ADDRESS_PLACE_1);
        String displayName = prepareDefaultDisplayName(email, telephoneNumber, addressPlace1);
        docModel.setProperty(commonPartLabel, ContactJAXBSchema.DISPLAY_NAME,
                displayName);
    }

    /**
     * Produces a default displayName from the basic name and foundingPlace fields.
     * @see OrgAuthorityClientUtils.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param shortName
     * @param foundingPlace
     * @return
     * @throws Exception
     */
    private static String prepareDefaultDisplayName(String email,
            String telephoneNumber, String addressPlace1) throws Exception {

        final int MAX_DISPLAY_NAME_LENGTH = 30;

        StringBuilder newStr = new StringBuilder("");
        final String sep = " ";
        boolean firstAdded = false;

        if (!(email == null || email.isEmpty())) {
            newStr.append(email);
            firstAdded = true;
        }

        if (!(telephoneNumber == null || telephoneNumber.isEmpty())) {
            if (newStr.length() <= MAX_DISPLAY_NAME_LENGTH) {
                if (firstAdded) {
                    newStr.append(sep);
                } else {
                    firstAdded = true;
                }
                newStr.append(telephoneNumber);
            }
        }

        if (!(addressPlace1 == null || addressPlace1.isEmpty())) {
            if (newStr.length() <= MAX_DISPLAY_NAME_LENGTH) {
                if (firstAdded) {
                    newStr.append(sep);
                }
                newStr.append(addressPlace1);
            }
        }

        String displayName = newStr.toString();

        if (displayName.length() > MAX_DISPLAY_NAME_LENGTH) {
            return displayName.substring(0, MAX_DISPLAY_NAME_LENGTH) + "...";
        } else {
            return displayName;
        }
    }

    @Override
    public String getUri(DocumentModel docModel) {
        String uri = "";
        UriInfo ui = getServiceContext().getUriInfo();
        if (ui != null && ui.getPath() != null) {
            uri = '/' + getAuthorityPathComponent(ui) + '/' + inAuthority
                    + '/' + AuthorityClient.ITEMS + '/' + inItem
                    + getServiceContextPath() + getCsid(docModel);
            // uri = "/" + ui.getPath() + "/" + getCsid(docModel);
        } else {
            uri = super.getUri(docModel);
        }
        return uri;
    }

    // Assumes the initial path component in the URI, following the base URI,
    // identifies the relevant authority resource
    private String getAuthorityPathComponent(UriInfo ui) {
        return ui.getPathSegments().get(0).toString();
    }

    /**
     * Filters out ContactJAXBSchema.IN_AUTHORITY, and IN_ITEM, to ensure that
     * the parent links remains untouched.
     * Also remove the display name, as this is always computed.
     * @param objectProps the properties parsed from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
            Map<String, Object> objectProps, ObjectPartType partMeta) {
        super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
        objectProps.remove(ContactJAXBSchema.IN_AUTHORITY);
        objectProps.remove(ContactJAXBSchema.IN_ITEM);
        objectProps.remove(ContactJAXBSchema.URI);
        objectProps.remove(ContactJAXBSchema.DISPLAY_NAME);
    }
}
