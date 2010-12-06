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
package org.collectionspace.services.blob.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.BlobJAXBSchema;
import org.collectionspace.services.common.DocHandlerBase;
import org.collectionspace.services.blob.BlobCommon;
import org.collectionspace.services.blob.BlobCommonList;
import org.collectionspace.services.blob.BlobCommonList.BlobListItem;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.nuxeo.ecm.core.api.DocumentModel;

public class BlobDocumentModelHandler
        extends DocHandlerBase<BlobCommon, AbstractCommonList> {

    public final String getNuxeoSchemaName(){
        return "blob";
    }

    public String getSummaryFields(AbstractCommonList commonList){
        return "name|mimeType|encoding|length|uri|csid";
    }

    public AbstractCommonList createAbstractCommonListImpl(){
        return new BlobCommonList();
    }

    public List createItemsList(AbstractCommonList commonList){
        List list = ((BlobCommonList)commonList).getBlobListItem();
        return list;
    }

    public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
        BlobListItem item = new BlobListItem();
        item.setEncoding((String) docModel.getProperty(label, BlobJAXBSchema.encoding));
        item.setMimeType((String) docModel.getProperty(label, BlobJAXBSchema.mimeType));
        //String theData = (String) docModel.getProperty(label, BlobJAXBSchema.data);
        item.setName((String) docModel.getProperty(label, BlobJAXBSchema.name));
        item.setLength((String) docModel.getProperty(label, BlobJAXBSchema.length));
        item.setUri(getServiceContextPath() + id);
        item.setCsid(id);
        return item;
    }
}

