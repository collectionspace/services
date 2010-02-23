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

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactsCommonList.ContactListItem;

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContactDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ContactDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<ContactsCommon, ContactsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(ContactDocumentModelHandler.class);
    /**
     * contact is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private ContactsCommon contact;
    /**
     * contactList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private ContactsCommonList contactList;

    private String inAuthority;

    public String getInAuthority() {
        return inAuthority;
    }

    public void setInAuthority(String inAuthority) {
        this.inAuthority = inAuthority;
    }

    private String inItem;

    public String getInItem() {
        return inItem;
    }

    public void setInItem(String inItem) {
        this.inItem = inItem;
    }

    /**
     * getCommonPart get associated contact
     * @return
     */
    @Override
    public ContactsCommon getCommonPart() {
        return contact;
    }

    /**
     * setCommonPart set associated contact
     * @param contact
     */
    @Override
    public void setCommonPart(ContactsCommon contact) {
        this.contact = contact;
    }

    /**
     * getCommonPartList get associated contact (for index/GET_ALL)
     * @return
     */
    @Override
    public ContactsCommonList getCommonPartList() {
        return contactList;
    }

    @Override
    public void setCommonPartList(ContactsCommonList contactList) {
        this.contactList = contactList;
    }

    @Override
    public ContactsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(ContactsCommon contactObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public ContactsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        DocumentModelList docList = wrapDoc.getWrappedObject();

        ContactsCommonList coList = new ContactsCommonList();
        List<ContactsCommonList.ContactListItem> list = coList.getContactListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            ContactListItem clistItem = new ContactListItem();
            // TODO Revisit which information unit(s) should be returned
            // in each entry, in a list of contact information.
            // See CSPACE-1018
            clistItem.setAddressPlace((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    ContactJAXBSchema.ADDRESS_PLACE));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            clistItem.setUri(getServiceContextPath() + id);
            clistItem.setCsid(id);
            list.add(clistItem);
        }

        return coList;
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(String prop) {
        return ContactConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

