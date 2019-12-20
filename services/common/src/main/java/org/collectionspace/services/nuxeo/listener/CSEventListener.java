package org.collectionspace.services.nuxeo.listener;

import java.io.Serializable;
import java.util.Map;

import org.collectionspace.services.config.tenant.EventListenerConfig;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;

public interface CSEventListener {
	/**
	 * Register ourself as an event listener for the named repository -the repo name corresponds to a specific tenant.
	 * @param respositoryName - The name of the Nuxeo repository which links us to a CollectionSpace tenant.
	 * @param eventListenerConfig - Tenant bindings config for our listener.
	 * @return
	 */
	boolean register(String respositoryName, EventListenerConfig eventListenerConfig);
	
	/**
	 * If the listener wants to handle the event, it should return 'true'.
	 * @param event
	 * @return
	 */
	boolean shouldHandleEvent(Event event);
	
	/**
	 * Processing of the event.
	 */
	void handleCSEvent(Event event);
	
	/**
	 * Determines if we are a registered event listener for the given event.
	 * @param event
	 * @return
	 */
	boolean isRegistered(Event event);
	
	/**
	 * Returns event listener related params that we're supplied in the tenant bindings.
	 * @param event
	 * @return
	 */
	Map<String, String> getParams(Event event);
	
	/**
	 * Set's a property in a DocumetModel's transient data context.
	 * 
	 * @param docModel
	 * @param key
	 * @param value
	 */
	void setDocModelContextProperty(DocumentModel docModel, String key, Serializable value);
	
	/**
	 * Clears a property from a DocumentModel's transient data context.
	 * @param docModel
	 * @param key
	 */
	void clearDocModelContextProperty(DocumentModel docModel, String key);
	
	/**
	 * Returns the name of the event listener as defined during registration -see register() method.
	 * @return
	 */
	String getName(String repositoryName);	
}
