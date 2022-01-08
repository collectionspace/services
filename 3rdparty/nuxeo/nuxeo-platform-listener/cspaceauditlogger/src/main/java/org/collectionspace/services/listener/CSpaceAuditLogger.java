package org.collectionspace.services.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.LifeCycleConstants;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.impl.ArrayProperty;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.api.model.impl.primitives.BlobProperty;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.schema.types.ListType;
import org.nuxeo.ecm.platform.audit.api.AuditLogger;
import org.nuxeo.ecm.platform.audit.api.ExtendedInfo;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.runtime.api.Framework;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * As of June 2021, here is a list of ALL Nuxeo auditable events.  Those marked with an '*' are events that
 * CollectionSpace's Audit Trail service logs.
	- documentCreated*
	- sectionContentPublished
	- user_created
	- documentCreatedByCopy
	- documentSecurityUpdated
	- documentDuplicated
	- logout
	- download
	- search
	- documentProxyPublished
	- registrationValidated
	- documentRemoved*
	- documentMoved
	- documentModified*
	- documentUnlocked
	- loginFailed
	- documentCheckedIn
	- retentionActiveChanged
	- documentRestored
	- registrationSubmitted
	- lifecycle_transition_event* (CSpace workflow changes --e.g., lock, soft-delete, etc
	- user_modified
	- group_deleted
	- group_modified
	- loginSuccess
	- group_created
	- versionRemoved
	- registrationAccepted
	- documentLocked*
	- user_deleted
 */

import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.client.AuditClientUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;

	public class CSpaceAuditLogger extends AbstractCSEventSyncListenerImpl {

	private static final Logger logger = LoggerFactory.getLogger(CSpaceAuditLogger.class);
	
	private static final List<String> SYSTEM_PROPS = Arrays.asList("dc:created", "dc:creator", "dc:modified",
			"dc:contributors", "dc:title", "collectionspace_core:updatedAt");

	public static final String FIELD_NAME = "fieldname";
	public static final String OLD_VALUE = "oldValue";
	public static final String NEW_VALUE = "newValue";
	public static final String COMMENT_VALUE = "commentValue";
	public static final String EMPTY_VALUE = "[EMPTY]";

	private static final Serializable HARD_DELETED_STATE = "hard-deleted";

	@Override
	public void handleCSEvent(Event event) {
		EventContext ectx = event.getContext(); 
		if (!(ectx instanceof DocumentEventContext)) {
			return;
		}

		AuditLogger logger = Framework.getLocalService(AuditLogger.class);
		if (logger == null) {
			getLogger().error("No AuditLogger implementation is available");
			return;
		}
	
		try {
			DocumentEventContext docCtx = (DocumentEventContext) ectx;
			DocumentModel newDoc = docCtx.getSourceDocument();
			DocumentModel oldDoc = null;
			if (!event.getName().equalsIgnoreCase(DocumentEventTypes.DOCUMENT_REMOVED)) {
				oldDoc = newDoc.getCoreSession().getDocument(newDoc.getRef());
			}

			Context context = new Context(newDoc, oldDoc, event, logger);
			processDocument(context);
		} catch (Throwable t) {
			getLogger().error("Error processing audit event", t);
		}
	}
	
	private List<FieldEntry> processNewDocument(Context context) {
		List<FieldEntry> entries = new ArrayList<>();

		String[] schemas = context.newDoc.getSchemas();
		for (String schema : schemas) {
			Collection<Property> properties = context.newDoc.getPropertyObjects(schema);
			for (Property property : properties) {
				String fieldName = property.getName();
				// skip system properties
				if (SYSTEM_PROPS.contains(fieldName)) {
					continue;
				}

				List<FieldEntry> subEntries = processProperty(context, null, property);
				if (subEntries != null && !subEntries.isEmpty()) {
					entries.addAll(subEntries);
				}
			}
		}

		return entries;
	}

	protected void processDocument(Context context) {
		List<FieldEntry> entries = new ArrayList<>();
		String eventName = context.event.getName();
	
		if (eventName.equalsIgnoreCase(DocumentEventTypes.DOCUMENT_CREATED)) {
			List<FieldEntry> subEntries = processNewDocument(context);
			if (subEntries != null && !subEntries.isEmpty()) {
				entries.addAll(subEntries);
			}
		} else if (eventName.equalsIgnoreCase(DocumentEventTypes.BEFORE_DOC_UPDATE))  {
			String[] schemas = context.newDoc.getSchemas();
			for (String schema : schemas) {
				Collection<Property> properties = context.newDoc.getPropertyObjects(schema);
				for (Property property : properties) {
					String fieldName = property.getName();
					// skip system properties
					if (SYSTEM_PROPS.contains(fieldName)) {
						continue;
					}
		
					if (property.isDirty()) {
						Property oldProperty = context.oldDoc.getProperty(fieldName);
						List<FieldEntry> subEntries = processProperty(context, oldProperty, property);
						if (subEntries != null && !subEntries.isEmpty()) {
							entries.addAll(subEntries);
						}
					}
				}
			}
		} else if (eventName.equalsIgnoreCase(LifeCycleConstants.TRANSITION_EVENT)) {
			List<FieldEntry> subEntries = processLifecyleEvent(context);
			if (subEntries != null && !subEntries.isEmpty()) {
				entries.addAll(subEntries);
			}			
		} else if (eventName.equalsIgnoreCase(DocumentEventTypes.DOCUMENT_REMOVED)) {
			List<FieldEntry> subEntries = processDeleteEvent(context);
			if (subEntries != null && !subEntries.isEmpty()) {
				entries.addAll(subEntries);
			}			
		}

		if (entries.size() > 0) {
			context.addFieldEntries(entries);
		}
	}

	private List<FieldEntry> processDeleteEvent(Context context) {
		List<FieldEntry> entries = new ArrayList<>();
		
		FieldEntry entry = new FieldEntry(context);
		String fieldName = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE;
		entry.setOldValue(fieldName, context.getContextProperty(CoreEventConstants.DOC_LIFE_CYCLE));
		entry.setNewValue(fieldName, HARD_DELETED_STATE);
		entries.add(entry);

		return entries;
	}

	private List<FieldEntry> processLifecyleEvent(Context context) {
		List<FieldEntry> entries = new ArrayList<>();

		FieldEntry entry = new FieldEntry(context);
		String fieldName = CollectionSpaceClient.COLLECTIONSPACE_CORE_WORKFLOWSTATE;
		entry.setOldValue(fieldName, context.getContextProperty(LifeCycleConstants.TRANSTION_EVENT_OPTION_FROM));
		entry.setNewValue(fieldName, context.newDoc.getCurrentLifeCycleState());
		entries.add(entry);

		return entries;
	}

	private boolean isScalar(Property oldProperty, Property newProperty) {
		boolean result = false;
		
		if (oldProperty != null && oldProperty.isScalar()) {
			return true;
		}
		
		if (newProperty != null && newProperty.isScalar()) {
			return true;
		}
		
		return result;
	}
	
	private boolean isComplex(Property oldProperty, Property newProperty) {
		boolean result = false;
		
		if (oldProperty != null && oldProperty.isComplex()) {
			return true;
		}
		
		if (newProperty != null && newProperty.isComplex()) {
			return true;
		}
		
		return result;
	}
	
	private boolean isList(Property oldProperty, Property newProperty) {
		boolean result = false;
		
		if (oldProperty != null && oldProperty.isList()) {
			return true;
		}
		
		if (newProperty != null && newProperty.isList()) {
			return true;
		}
		
		return result;
	}
	
	private boolean isBlob(Property oldProperty, Property newProperty) {
		boolean result = false;
		
		if (oldProperty != null && oldProperty instanceof BlobProperty) {
			return true;
		}
		
		if (newProperty != null && newProperty instanceof BlobProperty) {
			return true;
		}
		
		return result;
	}
	
	private boolean isArray(Property oldProperty, Property newProperty) {
		boolean result = false;
		
		if (oldProperty != null && oldProperty instanceof ArrayProperty) {
			return true;
		}
		
		if (newProperty != null && newProperty instanceof ArrayProperty) {
			return true;
		}
		
		return result;
	}
	
	private String getXPath(Property oldProperty, Property newProperty) {
		String result = null;

		if (oldProperty != null) {
			return oldProperty.getXPath();
		}
		
		if (newProperty != null) {
			return newProperty.getXPath();
		}

		return result;
	}
	
	private String getName(Property oldProperty, Property newProperty) {
		String result = null;

		if (oldProperty != null) {
			return oldProperty.getName();
		}
		
		if (newProperty != null) {
			return newProperty.getName();
		}

		return result;
	}

	
	protected List<FieldEntry> processProperty(Context context, Property oldProperty, Property newProperty) {
	
		List<FieldEntry> entries = new ArrayList<>();
	
		// Handle Scalar Properties
		if (isScalar(oldProperty, newProperty)) {
			FieldEntry entry = processScalarProperty(context, oldProperty, newProperty);
			if (entry != null) {
				entries.add(entry);
			}
		}

		// Handle Complex Properties
		if (isComplex(oldProperty, newProperty) && !isList(oldProperty, newProperty)) {
			if (isBlob(oldProperty, newProperty)) {
				FieldEntry entry = processBlobProperty(context, oldProperty, newProperty);
				if (entry != null) {
					entries.add(entry);
				}
			} else {
				List<FieldEntry> subEntries = processComplexProperty(context, oldProperty, newProperty);
				if (subEntries != null && !subEntries.isEmpty()) {
					entries.addAll(subEntries);
				}
			}
		}

		if (isList(oldProperty, newProperty)) {
			if (isArray(oldProperty, newProperty)) {
				List<FieldEntry> subEntries = 
						processScalarList(context, getXPath(oldProperty, newProperty),
								oldProperty != null ? oldProperty.getValue() : null, 
								newProperty != null ? newProperty.getValue() : null);
				if (subEntries != null && !subEntries.isEmpty()) {
					entries.addAll(subEntries);
				}
			} else {
				List<FieldEntry> subEntries = processComplexProperty(context, oldProperty, newProperty);
				if (subEntries != null && !subEntries.isEmpty()) {
					entries.addAll(subEntries);
				}
			}
		}

		// org.nuxeo.ecm.core.api.model.Property
		return entries;
	}
	
	protected FieldEntry processScalarProperty(Context context, Property oldProperty, Property newProperty) {
		return getEntry(context, getXPath(oldProperty, newProperty),
				oldProperty != null ? oldProperty.getValue() : null,
				newProperty != null ? newProperty.getValue() : null);
	}
	
	private String getFieldQualifier(long value) {
	    String result = String.format(".%04X", value);

	    return result;
	}

	/*
	 * 
	 */
	protected List<FieldEntry> processScalarList(Context context, String fieldName, Serializable oldValue, Serializable newValue) {
		List<FieldEntry> entries = new ArrayList<>();
		long fieldQualifierValue = 1000; // use to create a unique suffix to qualify field name for list items
	
		ArrayList<?> oldList = null;
		if (oldValue != null) {
			oldList = (ArrayList<?>) ((ArrayList<?>)oldValue).clone();
		}

		ArrayList<?> newList = null;
		if (newValue !=null) {
			newList = (ArrayList<?>) ((ArrayList<?>)newValue).clone();
		}

		fieldName = normalizeFieldName(fieldName);
	
		// log New/Added list items
		if (newList != null) {
			ArrayList<?> added = new ArrayList<>(newList);
			if (oldList != null) {
				added.removeAll(oldList);
			}
			for (Object addedValue : added) {
				String fieldQualifier = getFieldQualifier(fieldQualifierValue++);
				FieldEntry entry = getEntry(context, fieldName + fieldQualifier, null, (Serializable) addedValue);
				if (entry != null) {
					entry.setComment(fieldName + fieldQualifier, "Added " + formatPropertyValue((Serializable) addedValue));
					entries.add(entry);
				}
			}
		}

		// log Old/Removed list items
		if (oldList != null) {
			ArrayList<?> removed = new ArrayList<>(oldList);
			removed.removeAll(newList);
			for (Object removedValue : removed) {
				String fieldQualifier = getFieldQualifier(fieldQualifierValue++);
				FieldEntry entry = getEntry(context, fieldName + fieldQualifier, (Serializable) removedValue, null);
				if (entry != null) {
					entry.setComment(fieldName + fieldQualifier, "Removed " + formatPropertyValue((Serializable) removedValue));
					entries.add(entry);
				}
			}
		}

		return entries;
	}
	
	private boolean isListOfComplexType(Property property) {
		boolean result = false;
		
		if (property != null && property.isList()) {
			ListType listType = (ListType) property.getType();
			if (listType.isScalarList() == false) {
				result = true;
			}
		}

		return result;
	}
	
	private boolean isListOfScalarType(Property property) {
		boolean result = false;

		if (property != null && property.isList()) {
			ListType listType = (ListType) property.getType();
			result = listType.isScalarList();
		}

		return result;
	}
	
	private ArrayList<Property> getListAsArray(ListProperty listProperty) {
		ArrayList<Property> resultList = new ArrayList<Property>();

		if (listProperty != null) {
			Iterator<Property> properties = listProperty.listIterator();
			while (properties.hasNext()) {
				resultList.add(properties.next());
			}
		}

		return resultList;
	}

	protected List<FieldEntry> processComplexProperty(Context context, Property oldProperty, Property newProperty) {
		List<FieldEntry> entries = new ArrayList<>();
		
		if (isListOfComplexType(oldProperty) || isListOfComplexType(newProperty)) {
			ArrayList<Property> oldPropertyList = getListAsArray((ListProperty)oldProperty);
			long oldListSize = oldPropertyList.size();

			ArrayList<Property> newPropertyList = getListAsArray((ListProperty)newProperty);
			long newListSize = newPropertyList.size();
			
			String commentTemplate = "List %s.  %d -> %d";
			if (newListSize > oldListSize) {
				String comment = String.format(commentTemplate,
						"grew", oldPropertyList.size(), newPropertyList.size());
				entries.add(getEntry(context, getXPath(oldProperty, newProperty), comment, oldPropertyList.size(), newPropertyList.size()));
			} else if (newListSize < oldListSize) {
				String comment = String.format(commentTemplate,
						"shrank", oldPropertyList.size(), newPropertyList.size());
				entries.add(getEntry(context, getXPath(oldProperty, newProperty), comment, oldPropertyList.size(), newPropertyList.size()));
			}

			long listSize = Math.max(oldListSize, newListSize);
			for (int i = 0; i < listSize; i++) {
				Property oldListItem = (i < oldListSize) ? oldPropertyList.get(i) : null;
				Property newListItem = (i < newListSize) ? newPropertyList.get(i) : null;
				List<FieldEntry> subEntries = processComplexProperty(context, oldListItem, newListItem);
				if (subEntries != null && !subEntries.isEmpty()) {
					entries.addAll(subEntries);
				}
			}

		} else if (isListOfScalarType(oldProperty) || isListOfScalarType(newProperty)) {
			entries.addAll(processScalarList(context, getXPath(oldProperty, newProperty), 
					oldProperty != null ? oldProperty.getValue() : null,
					newProperty != null ? newProperty.getValue() : null));	
		} else {
			if (newProperty != null) {
				Iterator<Property> childProperties = null;
				if (oldProperty == null) {
					childProperties = newProperty.getChildren().iterator();
				} else {
					childProperties = newProperty.getDirtyChildren();
				}
				while (childProperties.hasNext()) {
					Property dirtyProperty = childProperties.next();
					entries.addAll(processProperty(context, 
							oldProperty != null ? oldProperty.get(dirtyProperty.getName()) : null,
									dirtyProperty));
				}
			} else {
				Iterator<Property> childProperties = oldProperty.getChildren().iterator();
				while (childProperties.hasNext()) {
					Property childPropery = childProperties.next();
					entries.addAll(processProperty(context, childPropery, null));
				}
			}
		}

		return entries;
	}

	protected FieldEntry processBlobProperty(Context context, Property oldProperty, Property newProperty) {
		Blob oldBlob = (Blob) (oldProperty != null ? oldProperty.getValue() : null);
		String oldFilename = oldBlob != null ? oldBlob.getFilename() : null;
		String oldPropertyXPath = oldProperty != null ? oldProperty.getXPath() : null;
		
		Blob newBlob = (Blob) newProperty.getValue();
		String newFilename = newBlob != null ? newBlob.getFilename() : null;
		return getEntry(context, oldPropertyXPath, oldFilename, newFilename);
	}

	protected FieldEntry getEntry(Context context, String fieldName, String comment, Serializable oldValue, Serializable newValue) {
		String formatedOldValue = formatPropertyValue(oldValue);
		String formatedNewValue = formatPropertyValue(newValue);

		if (formatedOldValue == null && formatedNewValue == null) {
			// no values to log
			return null;
		}

		if (formatedOldValue != null && formatedNewValue != null) {
			if (formatedOldValue.trim().equals(formatedNewValue.trim())) {
				// old and new values are the same, so nothing to log
				return null;
			}
		}

		FieldEntry entry = new FieldEntry(context);
		fieldName = normalizeFieldName(fieldName);
		entry.setOldValue(fieldName, formatedOldValue);
		entry.setNewValue(fieldName, formatedNewValue);

		if (comment == null) {
			entry.setComment(fieldName, (formatedOldValue != null ? formatedOldValue : EMPTY_VALUE) + " -> " +
					(formatedNewValue != null ? formatedNewValue : EMPTY_VALUE));
		} else {
			entry.setComment(fieldName, comment);
		}

		return entry;
	}

	protected FieldEntry getEntry(Context context, String fieldName, Serializable oldValue, Serializable newValue) {
		return getEntry(context, fieldName, null /*no comment*/, oldValue, newValue);
	}

	protected String formatPropertyValue(Serializable value) {
		String result = null;

		if (value instanceof Calendar) {
			Calendar calendar = (Calendar) value;
			Instant instant = calendar.getTime().toInstant();
			result = instant.toString();
		} else if (value != null) {
			result = value.toString();
		}

		return result;
	}

	protected String normalizeFieldName(String fieldName) {
		if (fieldName != null && fieldName.startsWith("/")) {
			return fieldName.substring(1);
		} else {
			return fieldName;
		}
	}

	class FieldEntry extends HashMap<String, ExtendedInfo> {
		Context context;
		
		public FieldEntry(Context context) {
			this.context = context;
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public void setOldValue(String fieldName, Serializable oldValue) {
			put(fieldName + "." + OLD_VALUE, context.logger.newExtendedInfo(oldValue != null ? oldValue : EMPTY_VALUE));
		}	
		
		public void setNewValue(String fieldName, Serializable newValue) {
			put(fieldName + "." + NEW_VALUE, context.logger.newExtendedInfo(newValue != null ? newValue : EMPTY_VALUE));
		}
		
		public void setComment(String fieldName, Serializable oldValue, Serializable newValue) {
			put(fieldName + "." + COMMENT_VALUE, context.logger.newExtendedInfo(oldValue != null ? oldValue : EMPTY_VALUE));
		}
		
		public void setComment(String fieldName, Serializable comment) {
			put(fieldName + "." + COMMENT_VALUE, context.logger.newExtendedInfo(comment));
		}
	}

	//
	// Inner class for managing Nuxeo audit service context
	//
	class Context {
		private String eventID;
		private DocumentModel newDoc;
		private DocumentModel oldDoc;
		private Event event;
		private AuditLogger logger;
		private LogEntry entry;
		
		public Context(DocumentModel newDoc, DocumentModel oldDoc, Event event, AuditLogger logger) {
			this.newDoc = newDoc;
			this.oldDoc = oldDoc;
			this.event = event;
			this.logger = logger;

			// generateCSID:
			this.eventID = UUID.randomUUID().toString();

			// Create and set the Nuxeo audit entry
			entry = logger.newLogEntry();
			entry.setEventId(eventID); // use this value identify CollectionSpace events in the Nuxeo audit trail
			entry.setCategory(event.getName());
			entry.setEventDate(new Date(event.getTime()));
			entry.setDocUUID(newDoc.getRef()); // Nuxeo repo ID
			entry.setDocType(newDoc.getType()); // Nuxeo document type
			entry.setDocPath(newDoc.getName()); // CSpace CSID
			entry.setDocLifeCycle(newDoc.getCurrentLifeCycleState()); // CSpace workflow state
			entry.setRepositoryId(newDoc.getRepositoryName());
			entry.setComment("No Comment");
			//
			// Set the actor/user who's action triggered this audit entry
			//
			String cspaceUser = AuthN.get().getUserId();
			entry.setPrincipalName(cspaceUser);
			//
			// Create an extended map for logging field value changes
			//
			Map<String, ExtendedInfo> extended = new HashMap<>();
			entry.setExtendedInfos(extended);
		}
		
		public String getContextProperty(String propertyName) {
			return formatPropertyValue(event.getContext().getProperty(propertyName));
		}

		//
		// Add fieldEntries to the Nuxeo audit entry
		//
		public void addFieldEntries(List<FieldEntry> fieldEntries) {
			if (fieldEntries != null && !fieldEntries.isEmpty()) {
				Map<String, ExtendedInfo> extendedInfos = entry.getExtendedInfos();
				for (FieldEntry entry : fieldEntries) {
					extendedInfos.putAll(entry);
				}
				//
				// Each audit event will result in a single Nuxeo log entry
				//
				List<LogEntry> entries = new ArrayList<>();
				entries.add(entry);
				logger.addLogEntries(entries);
			}
		}
	}

	@Override
	public boolean shouldHandleEvent(Event event) {
		return true;
	}
	
	@Override
	protected Logger getLogger() {
		return logger;
	}
}