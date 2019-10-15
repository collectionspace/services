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
package org.collectionspace.services.common.document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.query.QueryContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractDocumentHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 * @param <T> 
 * @param <TL> 
 * @param <WT> 
 * @param <WTL> 
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractDocumentHandlerImpl<T, TL, WT, WTL>
        implements DocumentHandler<T, TL, WT, WTL> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(AbstractDocumentHandlerImpl.class);
    
    /** The properties. */
    private Map<String, Object> properties = new HashMap<String, Object>();
    
    /** The doc filter. */
    private DocumentFilter docFilter = null;
    
    /** The service context. */
    private ServiceContext serviceContext;

    /**
     * Instantiates a new abstract document handler impl.
     */
    public AbstractDocumentHandlerImpl() {
    	// Empty constructor
    }

    abstract protected String getRefnameDisplayName(DocumentWrapper<WT> docWrapper);
        
    /*
     * Should return a reference name for the wrapper object
     */
    abstract protected RefName.RefNameInterface getRefName(DocumentWrapper<WT> docWrapper, String tenantName, String serviceName);
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getServiceContext()
     */
	@Override
    public ServiceContext getServiceContext() {
        return serviceContext;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#setServiceContext(org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public void setServiceContext(ServiceContext ctx) {
        serviceContext = ctx;
    }

    /**
     * @return the properties
     */
    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * @param properties the properties to set
     */
    @Override
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

//    public void initializeDocumentFilter(ServiceContext ctx) {
//    	DocumentFilter docFilter = this.createDocumentFilter(ctx);
//    	this.setDocumentFilter(docFilter);
//    }
    /* (non-Javadoc)
 * @see org.collectionspace.services.common.document.DocumentHandler#createDocumentFilter()
 */
@Override
    public abstract DocumentFilter createDocumentFilter();

    /**
     * @return the DocumentFilter
     */
    @Override
    public DocumentFilter getDocumentFilter() {
        return docFilter;
    }

    /**
     * @param properties the DocumentFilter to set
     */
    @Override
    public void setDocumentFilter(DocumentFilter docFilter) {
        this.docFilter = docFilter;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepare(org.collectionspace.services.common.document.DocumentHandler.Action)
     */
    @Override
    final public void prepare(Action action) throws Exception {
        switch (action) {
            case CREATE:
                validate(action);
                prepareCreate();
                break;

            case UPDATE:
                validate(action);
                prepareUpdate();
                break;

            case GET:
                prepareGet();
                break;

            case GET_ALL:
                prepareGetAll();
                break;

            case DELETE:
                validate(action);
                prepareDelete();
                break;
                
            case SYNC:
                prepareSync();
                break;
                
			case WORKFLOW:
				logger.error("Should never get to this code path.  If you did, there is a bug in the code.");
				Thread.dumpStack();
				break;
				
			default:
				logger.error("Should never get to this code path.  If you did, there is a bug in the code.");
				Thread.dumpStack();
				break;
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepareCreate()
     */
    @Override
    public void prepareCreate() throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepareUpdate()
     */
    @Override
    public void prepareUpdate() throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepareGet()
     */
    @Override
    public void prepareGet() throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepareGetAll()
     */
    @Override
    public void prepareGetAll() throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepareDelete()
     */
    @Override
    public void prepareDelete() throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#prepareDelete()
     */
    @Override
    public void prepareSync() throws Exception {
    	// Do nothing. Subclasses can override if they want/need to.
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handle(org.collectionspace.services.common.document.DocumentHandler.Action, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    final public boolean handle(Action action, DocumentWrapper<?> wrapDoc) throws Exception {
    	boolean result = true;
    	
        switch (action) {
            case CREATE:
                handleCreate((DocumentWrapper<WT>) wrapDoc);
                break;

            case UPDATE:
                handleUpdate((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET:
                handleGet((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET_ALL:
                handleGetAll((DocumentWrapper<WTL>) wrapDoc);
                break;

            case DELETE:
                result = handleDelete((DocumentWrapper<WT>) wrapDoc);
                break;
                
            case SYNC:
                result = handleSync((DocumentWrapper<Object>) wrapDoc);
                break;                
                
			case WORKFLOW:
				logger.error("Should never get to this code path.  If you did, there is a bug in the code.");
				Thread.dumpStack();
				break;
				
			default:
				logger.error("Should never get to this code path.  If you did, there is a bug in the code.");
				Thread.dumpStack();
				break;
        }
        
        return result;
    }
    
    @Override
	public void sanitize(DocumentWrapper<WT> wrapDoc) {
    	//
    	// By default, do nothing.  Sub-classes can override if they want to.
    	//
    }


    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handleCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract void handleCreate(DocumentWrapper<WT> wrapDoc) throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handleUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract void handleUpdate(DocumentWrapper<WT> wrapDoc) throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handleGet(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract void handleGet(DocumentWrapper<WT> wrapDoc) throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handleGetAll(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract void handleGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handleDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public boolean handleDelete(DocumentWrapper<WT> wrapDoc) throws Exception {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#handleDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public boolean handleSync(DocumentWrapper<Object> wrapDoc) throws Exception {
    	// Do nothing. Subclasses can override if they want/need to.
    	return true;
    }
    

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#complete(org.collectionspace.services.common.document.DocumentHandler.Action, org.collectionspace.services.common.document.DocumentWrapper)
     */
	@Override
    final public void complete(Action action, DocumentWrapper<?> wrapDoc) throws Exception {
        switch (action) {
            case CREATE:
                completeCreate((DocumentWrapper<WT>) wrapDoc);
                break;

            case UPDATE:
                completeUpdate((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET:
                completeGet((DocumentWrapper<WT>) wrapDoc);
                break;

            case GET_ALL:
                completeGetAll((DocumentWrapper<WTL>) wrapDoc);
                break;

            case DELETE:
                completeDelete((DocumentWrapper<WT>) wrapDoc);
                break;
                
            case SYNC:
                completeSync((DocumentWrapper<Object>) wrapDoc);
                break;
                
			case WORKFLOW:
				logger.error("Should never get to this code path.  If you did, there is a bug in the code.");
				Thread.dumpStack();
				break;
				
			default:
				logger.error("Should never get to this code path.  If you did, there is a bug in the code.");
				Thread.dumpStack();
				break;                
        }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#completeCreate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeCreate(DocumentWrapper<WT> wrapDoc) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#completeUpdate(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeUpdate(DocumentWrapper<WT> wrapDoc) throws Exception {
        //no specific action needed
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#completeGet(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeGet(DocumentWrapper<WT> wrapDoc) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#completeGetAll(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeGetAll(DocumentWrapper<WTL> wrapDoc) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#completeDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeDelete(DocumentWrapper<WT> wrapDoc) throws Exception {
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#completeDelete(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void completeSync(DocumentWrapper<Object> wrapDoc) throws Exception {
    }    

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract T extractCommonPart(DocumentWrapper<WT> wrapDoc)
            throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper<WT> wrapDoc)
            throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public abstract TL extractCommonPartList(DocumentWrapper<WTL> wrapDoc)
            throws Exception;

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#fillCommonPartList(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    final public void fillCommonPartList(TL obj, DocumentWrapper<WTL> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("bulk create/update not yet supported");
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getCommonPart()
     */
    @Override
    public abstract T getCommonPart();

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#setCommonPart(java.lang.Object)
     */
    @Override
    public abstract void setCommonPart(T obj);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getCommonPartList()
     */
    @Override
    public abstract TL getCommonPartList();

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public abstract void setCommonPartList(TL obj);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getQProperty(java.lang.String)
     */
    @Override
    public abstract String getQProperty(String prop) throws DocumentException;

    /* 
     * Strip Nuxeo's schema name from the start of the field / element name.
     * (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getUnQProperty(java.lang.String)
     */
    @Override
    public String getUnQProperty(String qProp) {
        StringTokenizer tkz = new StringTokenizer(qProp, ":");
        if (tkz.countTokens() != 2) {
            String msg = "Property must be in the form xxx:yyy, "
                    + "e.g. collectionobjects_common:objectNumber";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        tkz.nextToken(); //skip
        return tkz.nextToken();
    }
    
    /**
     * Should return
     * @throws Exception 
     * @throws DocumentException 
     */
    @Override
    public String getDocumentsToIndexQuery(String indexId, String csid) throws DocumentException, Exception {
    	return null;
    }

    @Override
    public String getDocumentsToIndexQuery(String indexId, String documentType, String csid) throws DocumentException, Exception {
    	return null;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#getServiceContextPath()
     */
    @Override
    public String getServiceContextPath() {
        return "/" + getServiceContext().getServiceName().toLowerCase() + "/";
    }

    /**
     * Validate.
     *
     * @param action the action
     * @throws Exception the exception
     */
    private void validate(Action action) throws Exception {
        List<ValidatorHandler> valHandlers = serviceContext.getValidatorHandlers();
        for (ValidatorHandler handler : valHandlers) {
            handler.validate(action, serviceContext);
        }
    }
    
    /**
     * Creates the CMIS query from the service context.  Each document handler is responsible for returning a valid CMIS query using the
     * information in the current service context -which includes things like the query parameters, etc.
     * @throws DocumentException 
     */
    @Override
    public String getCMISQuery(QueryContext queryContext) throws DocumentException {
    	//
    	// By default, return nothing.  Child classes can override if they want.
    	//
    	return null;
    }
    
    @Override
    public boolean isCMISQuery() {
    	return false;
    }
    
    @Override
    public boolean isJDBCQuery() {
    	return false;
    }
    
    @Override
    public Map<String,String> getJDBCQueryParams() {
        return new HashMap<>();
    }
    
}
