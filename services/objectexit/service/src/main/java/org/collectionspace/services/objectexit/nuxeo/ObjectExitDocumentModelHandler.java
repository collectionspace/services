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
package org.collectionspace.services.objectexit.nuxeo;

import java.util.List;

import org.collectionspace.services.ObjectexitJAXBSchema;
import org.collectionspace.services.common.DocHandlerBase;
import org.collectionspace.services.objectexit.ObjectexitCommon;
import org.collectionspace.services.objectexit.ObjectexitCommonList;
import org.collectionspace.services.objectexit.ObjectexitCommonList.ObjectexitListItem;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * The Class ObjectExitDocumentModelHandler.
 */
public class ObjectExitDocumentModelHandler
        extends DocHandlerBase<ObjectexitCommon, AbstractCommonList> {

    public final String getNuxeoSchemaName(){
        return "objectexit";
    }

    @Override
    public String getSummaryFields(AbstractCommonList commonList){
        return "exitNumber|currentOwner|uri|csid";
    }

    public AbstractCommonList createAbstractCommonListImpl(){
        return new ObjectexitCommonList();
    }

    public List createItemsList(AbstractCommonList commonList){
        //actually means getObjectexitListItems(), plural -- it's a list, but element is named singular, so JAXB generates like so.
        List list = ((ObjectexitCommonList)commonList).getObjectexitListItem(); //List<ObjectexitCommonList.ObjectexitListItem> list = oeList.getObjectexitListItem();
        return list;
    }

    public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
        ObjectexitListItem item = new ObjectexitListItem();
        item.setExitNumber((String) docModel.getProperty(label, ObjectexitJAXBSchema.OBJECT_EXIT_NUMBER));
        item.setCurrentOwner((String) docModel.getProperty(label, ObjectexitJAXBSchema.OBJECT_EXIT_CURRENT_OWNER));
        item.setUri(getServiceContextPath() + id);
        item.setCsid(id);
        return item;
    }
}

