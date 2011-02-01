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
package org.collectionspace.services.nuxeo.client.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.common.ReflectionMapper;
import org.collectionspace.services.common.Tools;
import org.collectionspace.services.common.service.ListResultField;
import org.collectionspace.services.common.service.DocHandlerParams;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is generified by the marker types T and TL,
 * however, T is expected to map to something like BlobCommon, MediaCommon, ObjectexitCommon, etc.,
 * whereas TL is expected to map to AbstractCommonList,
 * since, for example, BlobCommonList and ObjectexitCommonList descend from AbstractCommonList,
 * and so on for every JAXB-generated schema class.
 *
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 *
 */
public abstract class DocHandlerBase<T, TL> extends RemoteDocumentModelHandlerImpl<T, TL> {

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

    /** Subclass DocHandlers may override this method to control exact creation of the common list.
     *  This class instantiates an AbstractCommonList from the classname returned by getDocHandlerParams().AbstractCommonListClassname.
     * @return
     * @throws Exception
     */
    public AbstractCommonList createAbstractCommonListImpl() throws Exception {
        //  String classname = this.commonList.getClass().getName();
        String classname = getDocHandlerParams().getAbstractCommonListClassname();
        return (AbstractCommonList)(ReflectionMapper.instantiate(classname));
    }


    /** DocHandlerBase calls this method with the CSID as id */
    public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
        return createItemForCommonList(getDocHandlerParams().getCommonListItemClassname(),
        		docModel, label, id, true);
    }

    /*
    public static class CommonListReflection {
        public String SchemaName;		// May be Repository schema, or a pseudo schema we manage
        public String DublinCoreTitle;            // TODO: for CollectionObjectDocumentModelHandler, NUXEO_DC_TITLE = "CollectionSpace-CollectionObject"
        public String SummaryFields;
        public String AbstractCommonListClassname;
        public String CommonListItemClassname;
        public String ListResultsItemMethodName;
        public List<ListResultField> ListItemsArray;  //setter, xpath
    }
     */

    public abstract DocHandlerParams.Params getDocHandlerParams();

    public String getSummaryFields(AbstractCommonList commonList){
        return getDocHandlerParams().getSummaryFields();
    }

    public List<ListResultField> getListItemsArray(){
        return getDocHandlerParams().getListResultsFields().getListResultField();
    }

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
        extractPagingInfo(((TL)commonList), wrapDoc);
        commonList.setFieldsReturned(getSummaryFields(commonList));
        List list = createItemsList(commonList);
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            String id = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            Object item = createItemForCommonList(docModel, label, id);
            list.add(item);
        }
        return (TL)commonList;
    }

    @Override
    public String getQProperty(String prop) {
        return getDocHandlerParams().getSchemaName() + ":" + prop;
    }

    //============= dublin core handling =======================================

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        fillDublinCoreObject(wrapDoc);
    }

    protected void fillDublinCoreObject(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        String title = getDocHandlerParams().getDublinCoreTitle();
        if (Tools.isEmpty(title)){
            return;
        }
        DocumentModel docModel = wrapDoc.getWrappedObject();
        docModel.setPropertyValue("dublincore:title", title);
    }

    //================== UTILITY METHODS ================================================

    public static ReflectionMapper.STATUS callPropertySetterWithXPathValue(DocumentModel docModel,
                           Object listItem,
                           String setterName,
                           String schema,
                           String xpath)
                           throws Exception {
        //Object prop = docModel.getProperty(label, elementName);
        String value = getXPathStringValue(docModel, schema, xpath);
        return ReflectionMapper.callSetter(listItem, setterName, value);
    }

    public static ReflectionMapper.STATUS callSimplePropertySetter(Object target, String name, Object arg) {
        return ReflectionMapper.callSetter(target, name, arg);
    }

    /**   @param commonListClassname is a package-qualified java classname, including inner class $ notation, such as
     *                             "org.collectionspace.services.objectexit.ObjectexitCommonList$ObjectexitListItem".
     *    @param includeStdFields set to true to have the method set Uri and Csid automatically, based on id param.
     */
    public Object createItemForCommonList(String commonListClassname, DocumentModel docModel, 
    		String schema, String id, boolean includeStdFields) throws Exception {
        //createItemForCommonList(docModel, label, id);
        Object item = ReflectionMapper.instantiate(commonListClassname);
        List<ListResultField> resultsFields = getListItemsArray();
        for (ListResultField field : resultsFields ){
        	callPropertySetterWithXPathValue(docModel, item, 
        			field.getSetter(), schema, field.getXpath());
        }
        if (includeStdFields){
        	callSimplePropertySetter(item, "setCsid", id);
        	callSimplePropertySetter(item, "setUri", getServiceContextPath() + id);
        }
        return item;
    }

    /** Subclasses should override this method if they don't want to automatically
     *  call List createItemsList(AbstractCommonList commonList, String listItemMethodName)
     *  which will use introspection to create a summary list, and will find the primary
     *  field for you if specified.
     */
    public List createItemsList(AbstractCommonList commonList) throws Exception {
        return createItemsList(commonList, getDocHandlerParams().getListResultsItemMethodName());
    }

    /** e.g. createItemsList(commonList, "getObjectexitListItem" */
    public List createItemsList(AbstractCommonList commonList, String listItemMethodName) throws Exception {
        Class commonListClass = commonList.getClass();
        Class[] types = new Class[] {};
        try {
            Method m = commonListClass.getMethod(listItemMethodName, types);
            return (List)(ReflectionMapper.fireGetMethod(m, commonList));
        } catch (NoSuchMethodException nsm){
            return new ArrayList();
        }
    }



}

