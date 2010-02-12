/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

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
package org.collectionspace.services.person.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.PersonAuthorityJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.person.PersonauthoritiesCommon;
import org.collectionspace.services.person.PersonauthoritiesCommonList;
import org.collectionspace.services.person.PersonauthoritiesCommonList.PersonauthorityListItem;

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PersonAuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class PersonAuthorityDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<PersonauthoritiesCommon, PersonauthoritiesCommonList> {

    private final Logger logger = LoggerFactory.getLogger(PersonAuthorityDocumentModelHandler.class);
    /**
     * personAuthority is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private PersonauthoritiesCommon personAuthority;
    /**
     * personAuthorityList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private PersonauthoritiesCommonList personAuthorityList;


    /**
     * getCommonPart get associated personAuthority
     * @return
     */
    @Override
    public PersonauthoritiesCommon getCommonPart() {
        return personAuthority;
    }

    /**
     * setCommonPart set associated personAuthority
     * @param personAuthority
     */
    @Override
    public void setCommonPart(PersonauthoritiesCommon personAuthority) {
        this.personAuthority = personAuthority;
    }

    /**
     * getCommonPartList get associated personAuthority (for index/GET_ALL)
     * @return
     */
    @Override
    public PersonauthoritiesCommonList getCommonPartList() {
        return personAuthorityList;
    }

    @Override
    public void setCommonPartList(PersonauthoritiesCommonList personAuthorityList) {
        this.personAuthorityList = personAuthorityList;
    }

    @Override
    public PersonauthoritiesCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(PersonauthoritiesCommon personAuthorityObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public PersonauthoritiesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        DocumentModelList docList = wrapDoc.getWrappedObject();

        PersonauthoritiesCommonList coList = new PersonauthoritiesCommonList();
        List<PersonauthoritiesCommonList.PersonauthorityListItem> list = coList.getPersonauthorityListItem();

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        Iterator<DocumentModel> iter = docList.iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            PersonauthorityListItem ilistItem = new PersonauthorityListItem();
            ilistItem.setDisplayName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    PersonAuthorityJAXBSchema.DISPLAY_NAME));
            ilistItem.setRefName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    PersonAuthorityJAXBSchema.REF_NAME));
            ilistItem.setVocabType((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    PersonAuthorityJAXBSchema.VOCAB_TYPE));
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri(getServiceContextPath() + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
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
        return PersonAuthorityConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

