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
package org.collectionspace.services.common;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public abstract class DocHandlerBase<T, TL>
        extends RemoteDocumentModelHandlerImpl<T, TL> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AbstractCommonList commonList;

    @Override
    public TL getCommonPartList() {
        return (TL)commonList;
    }

    public void setCommonPartList(AbstractCommonList aCommonList) {
        this.commonList = aCommonList;
    }

    private T commonPart;

    @Override
    public T getCommonPart() {
        return (T)commonPart;
    }

    public void setCommonPart(T commonPart) {
        this.commonPart = commonPart;
    }

    public abstract String getNuxeoSchemaName();

    public abstract String getSummaryFields(AbstractCommonList commonList);

    public abstract AbstractCommonList createAbstractCommonListImpl();

    public abstract List createItemsList(AbstractCommonList commonList );
    
    /** DocHandlerBase calls this method with the CSID as id */
    public abstract Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception;

    @Override
    public T extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    public void fillCommonPart(T objectexitObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public TL extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        String label = getServiceContext().getCommonPartLabel();

        AbstractCommonList commonList = createAbstractCommonListImpl();
        //ObjectexitCommonList oeList = (ObjectexitCommonList)extractPagingInfo(commonList, wrapDoc);
        extractPagingInfo(((TL)commonList), wrapDoc);
        commonList.setFieldsReturned(getSummaryFields(commonList));
        List list = createItemsList(commonList);
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            String id = NuxeoUtils.extractId(docModel.getPathAsString());
            Object item = createItemForCommonList(docModel, label, id);
            list.add(item);
        }
        return (TL)commonList;
    }

    @Override
    public String getQProperty(String prop) {
        return getNuxeoSchemaName() + ":" + prop;
    }



}

