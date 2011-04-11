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
package org.collectionspace.services.movement.nuxeo;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.MovementJAXBSchema;
import org.collectionspace.services.common.datetime.DateTimeFormatUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.movement.MovementsCommon;
import org.collectionspace.services.movement.MovementsCommonList;
import org.collectionspace.services.movement.MovementsCommonList.MovementListItem;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class MovementDocumentModelHandler.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
public class MovementDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<MovementsCommon, MovementsCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(MovementDocumentModelHandler.class);
    
    /** The Movement. */
    private MovementsCommon Movement;
    
    /** The Movement list. */
    private MovementsCommonList MovementList;

    /**
     * Gets the common part.
     *
     * @return the common part
     */
    @Override
    public MovementsCommon getCommonPart() {
        return Movement;
    }

    /**
     * Sets the common part.
     *
     * @param Movement the new common part
     */
    @Override
    public void setCommonPart(MovementsCommon Movement) {
        this.Movement = Movement;
    }

    /**
     * Gets the common part list.
     *
     * @return the common part list
     */
    @Override
    public MovementsCommonList getCommonPartList() {
        return MovementList;
    }

    /**
     * Sets the common part list.
     *
     * @param MovementList the new common part list
     */
    @Override
    public void setCommonPartList(MovementsCommonList MovementList) {
        this.MovementList = MovementList;
    }

    /**
     * Extract common part.
     *
     * @param wrapDoc the wrap doc
     * @return the Movements common
     * @throws Exception the exception
     */
    @Override
    public MovementsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Fill common part.
     *
     * @param MovementObject the Movement object
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    @Override
    public void fillCommonPart(MovementsCommon MovementObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /**
     * Extract common part list.
     *
     * @param wrapDoc the wrap doc
     * @return the Movements common list
     * @throws Exception the exception
     */
    @Override
    public MovementsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        MovementsCommonList coList = extractPagingInfo(new MovementsCommonList(), wrapDoc);
        AbstractCommonList commonList = (AbstractCommonList) coList;
        commonList.setFieldsReturned("movementReferenceNumber|currentLocation|locationDate|uri|csid");
        List<MovementsCommonList.MovementListItem> list = coList.getMovementListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        String label = getServiceContext().getCommonPartLabel();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            MovementListItem ilistItem = new MovementListItem();
            ilistItem.setMovementReferenceNumber((String) docModel.getProperty(label,
                    MovementJAXBSchema.MOVEMENT_REFERENCE_NUMBER));
            ilistItem.setCurrentLocation((String) docModel.getProperty(label,
                    MovementJAXBSchema.CURRENT_LOCATION));
            GregorianCalendar gcal = (GregorianCalendar) docModel.getProperty(label,
                    MovementJAXBSchema.LOCATION_DATE);
            ilistItem.setLocationDate(DateTimeFormatUtils.formatAsISO8601Timestamp(gcal));
            String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            ilistItem.setUri(getServiceContextPath() + id);
            ilistItem.setCsid(id);
            list.add(ilistItem);
        }

        return coList;
    }

    /**
     * Gets the q property.
     *
     * @param prop the prop
     * @return the q property
     */
    @Override
    public String getQProperty(String prop) {
        return MovementConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }

}

