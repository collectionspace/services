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
import java.util.Map;

import org.collectionspace.services.contact.ContactJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.contact.ContactsCommon;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.contact.ContactsCommonList.ContactListItem;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class ContactDocumentModelHandler.
 */
public class ContactDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<ContactsCommon, ContactsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ContactDocumentModelHandler.class);
    
    /** The contact. */
    private ContactsCommon contact;
    
    /** The contact list. */
    private ContactsCommonList contactList;

    /** The in authority. */
    private String inAuthority;

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
     * @param inAuthority the new in authority
     */
    public void setInAuthority(String inAuthority) {
        this.inAuthority = inAuthority;
    }

    /** The in item. */
    private String inItem;

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

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getCommonPart()
     */
    @Override
    public ContactsCommon getCommonPart() {
        return contact;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPart(java.lang.Object)
     */
    @Override
    public void setCommonPart(ContactsCommon contact) {
        this.contact = contact;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getCommonPartList()
     */
    @Override
    public ContactsCommonList getCommonPartList() {
        return contactList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(ContactsCommonList contactList) {
        this.contactList = contactList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public ContactsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(ContactsCommon contactObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public ContactsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        ContactsCommonList coList = extractPagingInfo(new ContactsCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("addressPlace|uri|csid");
        List<ContactsCommonList.ContactListItem> list = coList.getContactListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            ContactListItem clistItem = new ContactListItem();
            // TODO Revisit which information unit(s) should be returned
            // in each entry, in a list of contact information.
            // See CSPACE-1018
            clistItem.setAddressPlace((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    ContactJAXBSchema.ADDRESS_PLACE));
            String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            clistItem.setUri(getServiceContextPath() + id);
            clistItem.setCsid(id);
            list.add(clistItem);
        }

        return coList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(String prop) {
        return ContactConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
    
    /**
     * Filters out ContactJAXBSchema.IN_AUTHORITY, and IN_ITEM, to ensure that
     * the parent links remains untouched.
     * @param objectProps the properties parsed from the update payload
     * @param partMeta metadata for the object to fill
     */
    @Override
    public void filterReadOnlyPropertiesForPart(
    		Map<String, Object> objectProps, ObjectPartType partMeta) {
    	super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
    	objectProps.remove(ContactJAXBSchema.IN_AUTHORITY);
    	objectProps.remove(ContactJAXBSchema.IN_ITEM);
    }

}

