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
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.common.ReflectionMapper;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.common.service.ListResultField;
import org.collectionspace.services.common.service.DocHandlerParams;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.datetime.DateTimeFormatUtils;
import org.collectionspace.services.common.document.DocumentException;
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
    
    protected static final int NUM_STANDARD_LIST_RESULT_FIELDS = 3;

    @Override
    public AbstractCommonList getCommonPartList() {
        return commonList;
    }

    @Override
	public void setCommonPartList(AbstractCommonList aCommonList) {
        this.commonList = aCommonList;
    }

    private T commonPart;

    @Override
    public T getCommonPart() {
        return (T)commonPart;
    }

    @Override
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
        if (classname == null){
            throw new Exception("in createAbstractCommonListImpl. getDocHandlerParams().getAbstractCommonListClassname() is null");
        }
        classname = classname.trim();
        return (AbstractCommonList)(ReflectionMapper.instantiate(classname));
    }


    /** DocHandlerBase calls this method with the CSID as id */
    public Object createItemForCommonList(DocumentModel docModel, String label, String id) throws Exception {
        return createItemForCommonList(getDocHandlerParams().getCommonListItemClassname(),
        		docModel, label, id, true);
    }

	public DocHandlerParams.Params getDocHandlerParams() throws DocumentException {
		MultipartServiceContext sc = (MultipartServiceContext) getServiceContext();
		ServiceBindingType sb = sc.getServiceBinding();
		DocHandlerParams dhb = sb.getDocHandlerParams();
		if (dhb != null && dhb.getParams() != null) {
			return dhb.getParams();
		}
		throw new DocumentException("No DocHandlerParams configured for: "
				+ sb.getName());
	}

    public String getSummaryFields(AbstractCommonList theCommonList) throws DocumentException {
        return getDocHandlerParams().getSummaryFields();
    }

    public List<ListResultField> getListItemsArray() throws DocumentException {
        return getDocHandlerParams().getListResultsFields().getListResultField();
    }

    @Override
    public T extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

	@Override
	public void fillCommonPart(T objectexitObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    // This is an old hack restored by Laramie in confusion about how the 
	// replacement model works. Will be removed ASAP.
    public AbstractCommonList extractCommonPartListLaramieHACK(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        String label = getServiceContext().getCommonPartLabel();
        AbstractCommonList commonList = createAbstractCommonListImpl();
        //LC extractPagingInfo((commonList), wrapDoc);
        commonList.setFieldsReturned(getSummaryFields(commonList));
        List list = createItemsList(commonList);
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            String id = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            Object item = createItemForCommonList(docModel, label, id);
            list.add(item);
        }
        extractPagingInfo((commonList), wrapDoc); //LC
        return commonList;
    }

	public static String getUpdatedAtAsString(DocumentModel docModel) throws Exception {
			GregorianCalendar cal = (GregorianCalendar)
								docModel.getProperty(COLLECTIONSPACE_CORE_SCHEMA,
											COLLECTIONSPACE_CORE_UPDATED_AT);
			String updatedAt = DateTimeFormatUtils.formatAsISO8601Timestamp(cal);
			return updatedAt;
	}

    @Override
    public AbstractCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        String classname = getDocHandlerParams().getAbstractCommonListClassname();
        if (!Tools.isBlank(classname)){
             return extractCommonPartListLaramieHACK(wrapDoc);
        }
        
    	String commonSchema = getServiceContext().getCommonPartLabel();
    	CommonList commonList = new CommonList();
        extractPagingInfo(commonList, wrapDoc);
        List<ListResultField> resultsFields = getListItemsArray();
        int nFields = resultsFields.size()+NUM_STANDARD_LIST_RESULT_FIELDS;
        String fields[] = new String[nFields];
        fields[0] = "csid";
        fields[1] = "uri";
        fields[2] = "updatedAt";
        for(int i=NUM_STANDARD_LIST_RESULT_FIELDS;i<nFields;i++) {
        	ListResultField field = resultsFields.get(i-NUM_STANDARD_LIST_RESULT_FIELDS); 
        	fields[i]=field.getElement();
        }
        commonList.setFieldsReturned(fields);
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
		HashMap<String,String> item = new HashMap<String,String>();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            String id = NuxeoUtils.getCsid(docModel);
            item.put(fields[0], id);
            String uri = getUri(docModel);
            item.put(fields[1], uri);
            item.put(fields[2], getUpdatedAtAsString(docModel));

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

    // TODO - get rid of this if we can - appears to be unused.
    @Override
    public String getQProperty(String prop) throws DocumentException {
        return getDocHandlerParams().getSchemaName() + ":" + prop;
    }

    //============= dublin core handling =======================================

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
        super.fillAllParts(wrapDoc, action);
        fillDublinCoreObject(wrapDoc);
    }

    /**
     * Fill dublin core object, but only if there are document handler parameters in the service
     * bindings.
     *
     * @param wrapDoc the wrap doc
     * @throws Exception the exception
     */
    // TODO - Remove this? 
    // This look like it is never used in a sensible way. It just stuffs a static
    // String that matches the service name into a bogus field.
    protected void fillDublinCoreObject(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	DocHandlerParams.Params docHandlerParams = null;
    	try {
    		docHandlerParams = getDocHandlerParams();
    	} catch (Exception e) {
    		logger.warn(e.getMessage());
    	}
    	
    	if (docHandlerParams != null) {
	        String title = getDocHandlerParams().getDublinCoreTitle();
	        if (Tools.isEmpty(title) == false){
		        DocumentModel docModel = wrapDoc.getWrappedObject();
		        docModel.setPropertyValue("dublincore:title", title);
	        }
    	}
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

