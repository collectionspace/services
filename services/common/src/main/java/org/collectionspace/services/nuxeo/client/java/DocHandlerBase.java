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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.common.ReflectionMapper;
import org.collectionspace.services.common.Tools;
import org.collectionspace.services.common.service.ListResultField;
import org.collectionspace.services.common.service.DocHandlerParams;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is generified by the marker type T,
 * where T is expected to map to something like BlobCommon, MediaCommon, ObjectexitCommon, etc.,
 * and so on for every JAXB-generated schema class.
 *
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 *
 */
public abstract class DocHandlerBase<T> extends RemoteDocumentModelHandlerImpl<T, AbstractCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private AbstractCommonList commonList;

    @Override
    public AbstractCommonList getCommonPartList() {
        return commonList;
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

    public DocHandlerParams.Params getDocHandlerParams(){
    	MultipartServiceContext sc = (MultipartServiceContext) getServiceContext();
    	ServiceBindingType sb = sc.getServiceBinding();
    	DocHandlerParams dhb = sb.getDocHandlerParams();
    	if(dhb!=null&&dhb.getParams()!=null) {
    		return dhb.getParams();
    	}
        throw new RuntimeException("No DocHandlerParams configured for: "+sb.getName());
    }

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
    public AbstractCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        //String label = getServiceContext().getCommonPartLabel();
        
        /*
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
        */
        /* Rewrite
         * Create the CommonList
         * List<ListResultField> resultsFields = getListItemsArray();
         * Construct array of strings of resultsFields
         *   add csid and uri
         * Set the fieldNames for CommonList
         * For each doc in list:
         *   Create HashMap of values
         *   get csid, set csid hashmap value
         *   get uri, set uri hashmap value
         *   for (ListResultField field : resultsFields ){
         *	   get String value from Xpath
         *	   set hashMap value
         *   AddItem to CommonList
         * 
         */
    	String commonSchema = getServiceContext().getCommonPartLabel();
    	CommonList commonList = new CommonList();
        extractPagingInfo(commonList, wrapDoc);
        List<ListResultField> resultsFields = getListItemsArray(); 
        int nFields = resultsFields.size()+2;
        String fields[] = new String[nFields];
        fields[0] = "csid";
        fields[1] = "uri";
        for(int i=2;i<nFields;i++) {
        	ListResultField field = resultsFields.get(i-2); 
        	fields[i]=field.getElement();
        }
        commonList.setFieldsReturned(fields);
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
		HashMap<String,String> item = new HashMap<String,String>();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            String id = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            item.put(fields[0], id);
            String uri = getServiceContextPath() + id;
            item.put(fields[1], uri);
            for (ListResultField field : resultsFields ){
            	String schema = field.getSchema();
            	if(schema==null || schema.trim().isEmpty())
            		schema = commonSchema;
                String value = 
                	getXPathStringValue(docModel, schema, field.getXpath());
                if(value!=null && !value.trim().isEmpty()) {
                	item.put(field.getElement(), value);
                }
            }
            commonList.addItem(item);
            item.clear();
        }

        return commonList;
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
        return createItemsList(commonList, 
        		getDocHandlerParams().getListResultsItemMethodName());
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

