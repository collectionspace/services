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
package org.collectionspace.services.location.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.LocationAuthorityJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.location.LocationauthoritiesCommon;
import org.collectionspace.services.location.LocationauthoritiesCommonList;
import org.collectionspace.services.location.LocationauthoritiesCommonList.LocationauthorityListItem;

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LocationAuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class LocationAuthorityDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<LocationauthoritiesCommon, LocationauthoritiesCommonList> {

    private final Logger logger = LoggerFactory.getLogger(LocationAuthorityDocumentModelHandler.class);
    /**
     * locationAuthority is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private LocationauthoritiesCommon locationAuthority;
    /**
     * locationAuthorityList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private LocationauthoritiesCommonList locationAuthorityList;


    /**
     * getCommonPart get associated locationAuthority
     * @return
     */
    @Override
    public LocationauthoritiesCommon getCommonPart() {
        return locationAuthority;
    }

    /**
     * setCommonPart set associated locationAuthority
     * @param locationAuthority
     */
    @Override
    public void setCommonPart(LocationauthoritiesCommon locationAuthority) {
        this.locationAuthority = locationAuthority;
    }

    /**
     * getCommonPartList get associated locationAuthority (for index/GET_ALL)
     * @return
     */
    @Override
    public LocationauthoritiesCommonList getCommonPartList() {
        return locationAuthorityList;
    }

    @Override
    public void setCommonPartList(LocationauthoritiesCommonList locationAuthorityList) {
        this.locationAuthorityList = locationAuthorityList;
    }

    @Override
    public LocationauthoritiesCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(LocationauthoritiesCommon locationAuthorityObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public LocationauthoritiesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        LocationauthoritiesCommonList coList = extractPagingInfo(new LocationauthoritiesCommonList(),
        		wrapDoc);

        //FIXME: iterating over a long list of documents is not a long term
        //strategy...need to change to more efficient iterating in future
        List<LocationauthoritiesCommonList.LocationauthorityListItem> list = coList.getLocationauthorityListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            LocationauthorityListItem ilistItem = new LocationauthorityListItem();
            ilistItem.setDisplayName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    LocationAuthorityJAXBSchema.DISPLAY_NAME));
            ilistItem.setRefName((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    LocationAuthorityJAXBSchema.REF_NAME));
            ilistItem.setShortIdentifier((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
            		LocationAuthorityJAXBSchema.SHORT_IDENTIFIER));
            ilistItem.setVocabType((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    LocationAuthorityJAXBSchema.VOCAB_TYPE));
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
        return LocationAuthorityConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

