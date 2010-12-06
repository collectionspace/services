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
package org.collectionspace.services.media.nuxeo;

import java.util.List;

import org.collectionspace.services.MediaJAXBSchema;
import org.collectionspace.services.common.DocHandlerBase;
import org.collectionspace.services.media.MediaCommon;
import org.collectionspace.services.media.MediaCommonList;
import org.collectionspace.services.media.MediaCommonList.MediaListItem;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * The Class MediaDocumentModelHandler.
 */
public class MediaDocumentModelHandler
        extends DocHandlerBase<MediaCommon, AbstractCommonList> {

    public final String getNuxeoSchemaName(){
        return "media";
    }

    public String getSummaryFields(AbstractCommonList commonList){
        return "title|source|filename|identificationNumber|uri|csid";
    }

    public AbstractCommonList createAbstractCommonListImpl(){
        return new MediaCommonList();
    }

    public List createItemsList(AbstractCommonList commonList){
        List list = ((MediaCommonList)commonList).getMediaListItem();
        return list;
    }

    public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
        MediaListItem item = new MediaListItem();
        item.setTitle((String) docModel.getProperty(label, MediaJAXBSchema.title));
        item.setSource((String) docModel.getProperty(label, MediaJAXBSchema.source));
        item.setFilename((String) docModel.getProperty(label, MediaJAXBSchema.filename));
        item.setIdentificationNumber((String) docModel.getProperty(label, MediaJAXBSchema.identificationNumber));
        item.setUri(getServiceContextPath() + id);
        item.setCsid(id);
        return item;
    }
}

