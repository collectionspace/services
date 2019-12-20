package org.collectionspace.services.nuxeo.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.config.tenant.EventListenerConfig;
import org.collectionspace.services.config.tenant.Param;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.common.collections.ScopedMap;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public abstract class AbstractCSEventListenerImpl implements CSEventListener {
	private static Map<String, List<String>> mapOfrepositoryNames = new HashMap<String, List<String>>(); // <className, repositoryName>
	private static Map<String, Map<String, Map<String, String>>> eventListenerParamsMap = new HashMap<String, Map<String, Map<String, String>>>();  // <repositoryName, Map<EventListenerId, Map<key, value>>>
	private static Map<String, String> nameMap = new HashMap<String, String>();
	
    // SQL clauses
    private final static String NONVERSIONED_NONPROXY_DOCUMENT_WHERE_CLAUSE_FRAGMENT =
            "AND ecm:isCheckedInVersion = 0"
            + " AND ecm:isProxy = 0 ";
    protected final static String ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT =
            "AND (ecm:currentLifeCycleState <> 'deleted') "
            + NONVERSIONED_NONPROXY_DOCUMENT_WHERE_CLAUSE_FRAGMENT;
    static final String DOCMODEL_CONTEXT_PROPERTY_PREFIX = ScopeType.DEFAULT.getScopePrefix();

	public AbstractCSEventListenerImpl() {
		// Intentionally left blank
	}

	/**
	 * Find out if we (the event listener) are registered (via tenant bindings config) to respond events.
	 */
	@Override
	public boolean isRegistered(Event event) {
		boolean result = false;
		
		if (event != null && event.getContext() != null) {
			result = getRepositoryNameList().contains(event.getContext().getRepositoryName());
		}
		
		return result;
	}
	
	/*
	 * This method is meant to be the bottleneck for handling a Nuxeo document event.
	 */
	final public void handleEvent(Event event) {
		getLogger().trace(String.format("Eventlistener '%s' presenented with '%s' event.",
				getClass().getName(), event.getName()));
		boolean isRegistered = isRegistered(event);

		try {
			if (isRegistered && shouldHandleEvent(event)) {
				handleCSEvent(event);
				getLogger().debug(String.format("Eventlistener '%s' accepted '%s' event.",
						getClass().getName(), event.getName()));
			} else {
				if (isRegistered) {
					getLogger().debug(String.format("Eventlistener '%s' declined to handle '%s' event.",
							getClass().getName(), event.getName()));
				} else {
					getLogger().trace(String.format("Eventlistener '%s' was not registered in the service bindings for the tenant with repo '%s'.",
							getClass().getName(), event.getContext().getRepositoryName()));
				}
			}
		} catch (Exception e) {
			String errMsg = String.format("Eventlistener '%s' presenented with '%s' event but encountered an error: %s",
					getClass().getName(), event.getName(), e.getMessage());
			if (getLogger().isTraceEnabled()) {
				getLogger().error(errMsg, e);
			} else {
				getLogger().error(errMsg);
			}
		}
	}
	
	/**
	 * An event listener can be registered by multiple tenants, so we keep track of that here.
	 * 
	 * @return - the list of tenants/repositories that an event listener is registered with.
	 */
	protected List<String> getRepositoryNameList() {
		String key = this.getClass().getName();
		List<String> result = mapOfrepositoryNames.get(key);
		
		if (result == null) synchronized(this) {
			result = new ArrayList<String>();
			mapOfrepositoryNames.put(key, result);
		}
		
		return result;
	}
	
	/**
	 * The list of parameters (specified in a tenant's bindings) for event listeners
	 * @return
	 */
	protected Map<String, Map<String, Map<String, String>>> getEventListenerParamsMap() {
		return eventListenerParamsMap;
	}

	/**
	 * Returns 'true' if this collection changed as a result of the call. 
	 */
	@Override
	public boolean register(String respositoryName, EventListenerConfig eventListenerConfig) {
		boolean result = false;
		
		// Using the repositoryName as a qualifier, register this event listener's name as specified in the tenant bindings.
		setName(respositoryName, eventListenerConfig.getId());
		
		// Register this event listener with the given repository name
		if (getRepositoryNameList().add(respositoryName)) {
			result = true;
		}
		
		if (eventListenerConfig.getParamList() != null) {
			// Set this event listeners parameters, if any.  Params are qualified with the repositoryName since multiple tenants might be registering the same event listener but with different params.
			List<Param> paramList = eventListenerConfig.getParamList().getParam(); // values from the tenant bindings that we need to copy into the event listener
			if (paramList != null) {
				//
				// Get the list of event listeners for a given repository
				Map<String, Map<String, String>> eventListenerRepoParams = getEventListenerParamsMap().get(respositoryName); // Get the set of event listers for a given repository
				if (eventListenerRepoParams == null) {
					eventListenerRepoParams = new HashMap<String, Map<String, String>>();
					getEventListenerParamsMap().put(respositoryName, eventListenerRepoParams); // create and put an empty map
					result = true;
				}
				//
				// Get the list of params for a given event listener for a given repository
				Map<String, String> eventListenerParams = eventListenerRepoParams.get(eventListenerConfig.getId()); // Get the set of params for a given event listener for a given repository
				if (eventListenerParams == null) {
					eventListenerParams = new HashMap<String, String>();
					eventListenerRepoParams.put(eventListenerConfig.getId(), eventListenerParams); // create and put an empty map
					result = true;
				}
				//
				// copy all the values from the tenant bindings into the event listener
				for (Param params : paramList) {
					String key = params.getKey();
					String value = params.getValue();
					if (Tools.notBlank(key)) {
						eventListenerParams.put(key, value);
						result = true;
					}
				}
			}
		}
		
		return result;
	}
	
	protected void setName(String repositoryName, String eventListenerName) {
		nameMap.put(repositoryName, eventListenerName);
	}

	@Override
	public Map<String, String> getParams(Event event) {
		Map<String, String> result = null;
		try {
			String repositoryName = event.getContext().getRepositoryName();
			result = getEventListenerParamsMap().get(repositoryName).get(getName(repositoryName));  // We need to qualify with the repositoryName since this event listener might be register by multiple tenants using different params
		} catch (NullPointerException e) {
			// Do nothing.  Just means no params were configured.
		}
		return result;
	}
	
	@Override
	public String getName(String repositoryName) {
		return nameMap.get(repositoryName);
	}
	
	//
	// Return a property in the document model's transient context.
	//
	protected Serializable getContextPropertyValue(DocumentEventContext docEventContext, String key) {
		return docEventContext.getProperties().get(key);
	}

	//
	// Set a property in the document model's transient context.
	//
	@Override
    public void setDocModelContextProperty(DocumentModel collectionObjectDocModel, String key, Serializable value) {
    	ScopedMap contextData = collectionObjectDocModel.getContextData();
    	contextData.putIfAbsent(DOCMODEL_CONTEXT_PROPERTY_PREFIX + key, value);        
    }
	
    //
    // Clear a property from the docModel's context
	//
	@Override
	public void clearDocModelContextProperty(DocumentModel docModel, String key) {
    	ScopedMap contextData = docModel.getContextData();
    	contextData.remove(DOCMODEL_CONTEXT_PROPERTY_PREFIX + key);	
	}

	//
	// Derived classes need to implement.
	//
	abstract protected Log getLogger();

	//FIXME: Does not include all the sync-related "delete" workflow states
	/**
     * Identifies whether a supplied event concerns a document that has
     * been transitioned to the 'deleted' workflow state.
     * 
     * @param eventContext an event context
     * 
     * @return true if this event concerns a document that has
     * been transitioned to the 'deleted' workflow state.
     */
    protected boolean isDocumentSoftDeletedEvent(EventContext eventContext) {
        boolean isSoftDeletedEvent = false;
        
        if (eventContext instanceof DocumentEventContext) {
            if (eventContext.getProperties().containsKey(WorkflowClient.WORKFLOWTRANSITION_TO)
                    &&
                (eventContext.getProperties().get(WorkflowClient.WORKFLOWTRANSITION_TO).equals(WorkflowClient.WORKFLOWSTATE_DELETED)
                		||
                eventContext.getProperties().get(WorkflowClient.WORKFLOWTRANSITION_TO).equals(WorkflowClient.WORKFLOWSTATE_LOCKED_DELETED))) {
                isSoftDeletedEvent = true;
            }
        }
        
        return isSoftDeletedEvent;
    }
    
	 /**
	  * Identifies whether a document matches a supplied document type.
	  *
	  * @param docModel a document model.
	  * @param docType a document type string.
	  * @return true if the document matches the supplied document type; false if
	  * it does not.
	  */
     protected static boolean documentMatchesType(DocumentModel docModel, String docType) {
         if (docModel == null || Tools.isBlank(docType)) {
             return false;
         }
         if (docModel.getType().startsWith(docType)) {
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * Identifies whether a document is an active document; currently, whether
      * it is not in a 'deleted' workflow state.
      *
      * @param docModel
      * @return true if the document is an active document; false if it is not.
      */
     protected static boolean isActiveDocument(DocumentModel docModel) {
     	return isActiveDocument(docModel, false, null);
     }
     
     protected static boolean isActiveDocument(DocumentModel docModel, boolean isAboutToBeRemovedEvent, String aboutToBeRemovedCsid) {
         boolean isActiveDocument = false;

         if (docModel != null) {	        
             if (!docModel.getCurrentLifeCycleState().contains(WorkflowClient.WORKFLOWSTATE_DELETED)) {
                 isActiveDocument = true;
             }
 	        //
 	        // If doc model is the target of the "aboutToBeRemoved" event, mark it as not active.
 	        //
 	        if (isAboutToBeRemovedEvent && Tools.notBlank(aboutToBeRemovedCsid)) {
 	        	if (NuxeoUtils.getCsid(docModel).equalsIgnoreCase(aboutToBeRemovedCsid)) {
 	        		isActiveDocument = false;
 	        	}
 	        }
         }
         
         return isActiveDocument;
     }

     /**
      * Returns the current document model for a record identified by a CSID.
      *
      * Excludes documents which have been versioned (i.e. are a non-current
      * version of a document), are a proxy for another document, or are
      * un-retrievable via their CSIDs.
      *
      * @param session a repository session.
      * @param csid a CollectionObject identifier (CSID)
      * @return a document model for the document identified by the supplied
      * CSID.
      */
     protected DocumentModel getCurrentDocModelFromCsid(CoreSessionInterface session, String csid) {
         DocumentModelList docModelList = null;
         
         if (Tools.isEmpty(csid)) {
         	return null;
         }
         
         try {
             final String query = "SELECT * FROM "
                     + NuxeoUtils.BASE_DOCUMENT_TYPE
                     + " WHERE "
                     + NuxeoUtils.getByNameWhereClause(csid)
                     + " "
                     + NONVERSIONED_NONPROXY_DOCUMENT_WHERE_CLAUSE_FRAGMENT;
             docModelList = session.query(query);
         } catch (Exception e) {
             getLogger().warn("Exception in query to get active document model for CSID: " + csid, e);
         }
         
         if (docModelList == null || docModelList.isEmpty()) {
        	 getLogger().warn("Could not get active document models for CSID=" + csid);
             return null;
         } else if (docModelList.size() != 1) {
        	 getLogger().error("Found more than 1 active document with CSID=" + csid);
             return null;
         }

         return docModelList.get(0);
     }
}
