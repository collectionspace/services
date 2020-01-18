package org.collectionspace.services.listener;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public abstract class AbstractUpdateObjectLocationValues extends AbstractCSEventSyncListenerImpl {
    // FIXME: Make the following message, or its equivalent, a constant usable by all event listeners
    private final static String NO_FURTHER_PROCESSING_MESSAGE =
            "This event listener will not continue processing this event ...";
    
    private final static GregorianCalendar EARLIEST_COMPARISON_DATE = new GregorianCalendar(1600, 1, 1);
    private final static String RELATIONS_COMMON_SCHEMA = "relations_common"; // FIXME: Get from external constant
    
    private final static String COLLECTIONOBJECT_DOCTYPE = CollectionObjectConstants.NUXEO_DOCTYPE;
    private final static String RELATION_DOCTYPE = RelationConstants.NUXEO_DOCTYPE;//"Relation"; // FIXME: Get from external constant
    private final static String MOVEMENT_DOCTYPE = MovementConstants.NUXEO_DOCTYPE;
    
    private final static String SUBJECT_CSID_PROPERTY = "subjectCsid"; // FIXME: Get from external constant
    private final static String OBJECT_CSID_PROPERTY = "objectCsid"; // FIXME: Get from external constant
    private final static String SUBJECT_DOCTYPE_PROPERTY = "subjectDocumentType"; // FIXME: Get from external constant
    private final static String OBJECT_DOCTYPE_PROPERTY = "objectDocumentType"; // FIXME: Get from external constant
    protected final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common"; // FIXME: Get from external constant
    protected final static String COMPUTED_CURRENT_LOCATION_PROPERTY = "computedCurrentLocation"; // FIXME: Create and then get from external constant
    protected final static String CURRENT_LOCATION_ELEMENT_NAME = "currentLocation"; // From movement_commons schema.  FIXME: Get from external constant that already exists
    protected final static String MOVEMENTS_COMMON_SCHEMA = "movements_common"; // FIXME: Get from external constant
    private final static String LOCATION_DATE_PROPERTY = "locationDate"; // FIXME: Get from external constant
    protected final static String CURRENT_LOCATION_PROPERTY = "currentLocation"; // FIXME: Get from external constant
    protected final static String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core"; // FIXME: Get from external constant
    protected final static String CREATED_AT_PROPERTY = "createdAt"; // FIXME: Get from external constant
    protected final static String UPDATED_AT_PROPERTY = "updatedAt"; // FIXME: Get from external constant
    
    // Use this meta URN/refname to mark computed locations that are indeterminate
    private final static String INDETERMINATE_ID = "indeterminate";
    protected final static String INDETERMINATE_LOCATION = RefName.buildAuthorityItem(INDETERMINATE_ID, LocationAuthorityClient.SERVICE_NAME, INDETERMINATE_ID,
    		INDETERMINATE_ID, "~Indeterminate Location~").toString();

    // Used to set/get temp values in a DocumentModel instance
	private static final String IGNORE_LOCATION_UPDATE_EVENT_LABEL = "IGNORE_LOCATION_UPDATE_EVENT";
    
    public enum EventNotificationDocumentType {
        // Document type about which we've received a notification
        MOVEMENT, RELATION, COLLECTIONOBJECT;
    }

    public boolean shouldHandleEvent(Event event) {
        // Ensure we have all the event data we need to proceed.
        if (event.getContext() instanceof DocumentEventContext == false) {
            return false;
        }
        //
        // This event handler itself sometimes triggers additional events.  To prevent unnecessary cascading event handling, this event
        // handler sets a temp flag in the document model's context indicating we should ignore cascading events.  This method checks that flag and
        // exits if it is set.
        DocumentEventContext docEventContext = (DocumentEventContext) event.getContext();
        Boolean shouldIgnoreEvent = (Boolean) getContextPropertyValue(docEventContext, IGNORE_LOCATION_UPDATE_EVENT_LABEL);
        if (shouldIgnoreEvent != null && shouldIgnoreEvent) {
        	return false;
        }

        return true;
    }
    
    @Override
    public void handleCSEvent(Event event) {
        DocumentEventContext docEventContext = (DocumentEventContext) event.getContext();
        DocumentModel eventDocModel = docEventContext.getSourceDocument();        
        String eventType = event.getName();
        boolean isAboutToBeRemovedEvent = eventType.equals(DocumentEventTypes.ABOUT_TO_REMOVE);
        
        //
        // Ensure this event relates to a relationship record (between cataloging and movement records) or a movement record.  If so, get the CSID
        // of the corresponding movement record.  Otherwise, exit.
        //
        String eventMovementCsid = null;
        Enum<EventNotificationDocumentType> notificationDocumentType;
        if (documentMatchesType(eventDocModel, RELATION_DOCTYPE)) {
            notificationDocumentType = EventNotificationDocumentType.RELATION;
            // Ensure this relationship record is a CollectionObject/Movement tuple.
            eventMovementCsid = getCsidForDesiredDocTypeFromRelation(eventDocModel, MOVEMENT_DOCTYPE, COLLECTIONOBJECT_DOCTYPE);
            if (Tools.isBlank(eventMovementCsid)) {
                return;
            }
        } else if (documentMatchesType(eventDocModel, MOVEMENT_DOCTYPE)) {
            notificationDocumentType = EventNotificationDocumentType.MOVEMENT;
            // Otherwise, get a Movement CSID directly from the Movement record.
            eventMovementCsid = NuxeoUtils.getCsid(eventDocModel);
            if (Tools.isBlank(eventMovementCsid)) {
            	getLogger().warn("Could not obtain CSID for Movement record from document event.");
            	getLogger().warn(NO_FURTHER_PROCESSING_MESSAGE);
                return;
            }
        } else if (documentMatchesType(eventDocModel, COLLECTIONOBJECT_DOCTYPE) &&
        	eventType.equals(DocumentEventTypes.DOCUMENT_UPDATED)) {
        	notificationDocumentType = EventNotificationDocumentType.COLLECTIONOBJECT;
        } else {
        	// We don't need to handle this event.
            return;
        }

        // Note: currently, all Document lifecycle transitions on
        // the relevant doctype(s) are handled by this event handler,
        // not just transitions between 'soft deleted' and active states.
        //
        // We are assuming that we'll want to re-compute current locations
        // for related CollectionObjects on all such transitions, as the
        // semantics of such transitions are opaque to this event handler,
        // because arbitrary workflows can be bound to those doctype(s).
        //
        // If we need to filter out some of those lifecycle transitions,
        // such as excluding transitions to the 'locked' workflow state; or,
        // alternately, if we want to restrict this event handler's
        // scope to handle only transitions into the 'soft deleted' state,
        // we can add additional checks for doing so at this point in the code.

        //
        // Get a list of all the CollectionObject records affected by this event.
        // 
        CoreSessionInterface session = new CoreSessionWrapper(docEventContext.getCoreSession()); // NOTE: All Nuxeo sessions that get passed around to CollectionSpace code need to be wrapped inside of a CoreSessionWrapper
        Set<String> collectionObjectCsids = new HashSet<>();
        if (notificationDocumentType == EventNotificationDocumentType.RELATION) {
            String relatedCollectionObjectCsid = getCsidForDesiredDocTypeFromRelation(eventDocModel, COLLECTIONOBJECT_DOCTYPE, MOVEMENT_DOCTYPE);
            collectionObjectCsids.add(relatedCollectionObjectCsid);
        } else if (notificationDocumentType == EventNotificationDocumentType.MOVEMENT) {
            collectionObjectCsids.addAll(getCollectionObjectCsidsRelatedToMovement(eventMovementCsid, session));
        } else if (notificationDocumentType == EventNotificationDocumentType.COLLECTIONOBJECT) {
       		collectionObjectCsids.add(NuxeoUtils.getCsid(eventDocModel));
        } else {
            // This event did not involve a document relevant to us.
            return;
        }

        //
        // If we found no collectionobject records needing updating, then we're done.
        //
        if (collectionObjectCsids.isEmpty() == true) {
            return;
        }
        
        //
        // Now iterate through the list of affected CollectionObjects found.
        // For each CollectionObject, obtain its most recent, related Movement record,
        // and update update the Computed Current Location field if needed.
        //
        DocumentModel collectionObjectDocModel;
        for (String collectionObjectCsid : collectionObjectCsids) {            
            collectionObjectDocModel = getCurrentDocModelFromCsid(session, collectionObjectCsid);
            if (isActiveDocument(collectionObjectDocModel) == true) {
	            //
	            // Get the CollectionObject's most recent, valid related Movement to use for computing the
	            // object's current location.
	            //
				DocumentModel mostRecentMovement = getMostRecentMovementRecord(event, session, collectionObjectCsid,
						isAboutToBeRemovedEvent, eventMovementCsid);

	            //
	            // Update the CollectionObject's Computed Current Location field with the Movement record's location
				//
	            boolean didLocationChange = updateCollectionObjectLocation(collectionObjectDocModel, mostRecentMovement);
	            
	            //
	            // If the location changed, save/persist the change to the repository and log the change.
	            //
            	if (didLocationChange == true) {
            		persistLocationChange(session, collectionObjectDocModel);
    	            //
    	            // Log an INFO message if we've changed the cataloging record's location
    	            //	            
    	            if (getLogger().isInfoEnabled()) {
		                String computedCurrentLocationRefName =
		                        (String) collectionObjectDocModel.getProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, COMPUTED_CURRENT_LOCATION_PROPERTY);
		                getLogger().info(String.format("Updating cataloging record=%s current location to %s",
		                		NuxeoUtils.getCsid(collectionObjectDocModel), computedCurrentLocationRefName));
    	            }
            	}
            }
        }
    }
    
    //
    // Disable update/documentModified events and persist the location change.
    //
    private void persistLocationChange(CoreSessionInterface session, DocumentModel collectionObjectDocModel) {
    	
    	//
    	// Set a flag in the document model indicating that we want to ignore the update event that
    	// will be triggered by this save/persist request.
    	setDocModelContextProperty(collectionObjectDocModel, IGNORE_LOCATION_UPDATE_EVENT_LABEL, true);

    	//
    	// Save/Persist the document to the DB
        session.saveDocument(collectionObjectDocModel);
        
        //
        // Clear the flag we set to ignore events triggered by our save request.
        clearDocModelContextProperty(collectionObjectDocModel, IGNORE_LOCATION_UPDATE_EVENT_LABEL);
    }

    /**
     * Returns the CSIDs of active CollectionObject records related to a Movement record.
     *
     * @param movementCsid the CSID of a Movement record.
     * @param coreSession a repository session.
     * @throws ClientException
     * @return the CSIDs of the CollectionObject records, if any, which are
     * related to the Movement record.
     * @throws DocumentException 
     */
    private Set<String> getCollectionObjectCsidsRelatedToMovement(String movementCsid,
            CoreSessionInterface coreSession) throws ClientException {

        Set<String> csids = new HashSet<>();

        // Via an NXQL query, get a list of active relation records where:
        // * This movement record's CSID is the subject CSID of the relation,
        //   and its object document type is a CollectionObject doctype;
        // or
        // * This movement record's CSID is the object CSID of the relation,
        //   and its subject document type is a CollectionObject doctype.
        //
        // Some values below are hard-coded for readability, rather than
        // being obtained from constants.
        String query = String.format(
                "SELECT * FROM %1$s WHERE " // collectionspace_core:tenantId = 1 "
                + "("
                + "  (%2$s:subjectCsid = '%3$s' "
                + "  AND %2$s:objectDocumentType = '%4$s') "
                + " OR "
                + "  (%2$s:objectCsid = '%3$s' "
                + "  AND %2$s:subjectDocumentType = '%4$s') "
                + ")"
                + ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT,
                RELATION_DOCTYPE, RELATIONS_COMMON_SCHEMA, movementCsid, COLLECTIONOBJECT_DOCTYPE);
        
        DocumentModelList relationDocModels = null;
		try {
			relationDocModels = coreSession.query(query);
		} catch (DocumentException e) {
			getLogger().error(e);
		}
		
        if (relationDocModels == null || relationDocModels.isEmpty()) {
            return csids;
        }
        
        // Iterate through the list of Relation records found and build
        // a list of CollectionObject CSIDs, by extracting the relevant CSIDs
        // from those Relation records.
        String csid;
        for (DocumentModel relationDocModel : relationDocModels) {
            csid = getCsidForDesiredDocTypeFromRelation(relationDocModel, COLLECTIONOBJECT_DOCTYPE, MOVEMENT_DOCTYPE);
            if (Tools.notBlank(csid)) {
                csids.add(csid);
            }
        }
        
        return csids;
    }
    
    //
    // Returns true if this event is for the creation of a new relationship record
    //
    private static boolean isCreatingNewRelationship(Event event) {
    	boolean result = false;
    	
    	DocumentModel docModel = ((DocumentEventContext)event.getContext()).getSourceDocument();
    	if (event.getName().equals(DocumentEventTypes.DOCUMENT_CREATED) && documentMatchesType(docModel, RELATION_DOCTYPE)) {
        	result = true;
    	}
    	
    	return result;
    }

    // FIXME: A quick first pass, using an only partly query-based technique for
    // getting the most recent Movement record related to a CollectionObject,
    // augmented by procedural code.
    //
    // Could be replaced by a potentially more performant method, based on a query.
    //
    // E.g. the following is a sample CMIS query for retrieving Movement records
    // related to a CollectionObject, which might serve as the basis for that query.
    /*
     "SELECT DOC.nuxeo:pathSegment, DOC.dc:title, REL.dc:title,"
     + "REL.relations_common:objectCsid, REL.relations_common:subjectCsid FROM Movement DOC "
     + "JOIN Relation REL ON REL.relations_common:objectCsid = DOC.nuxeo:pathSegment "
     + "WHERE REL.relations_common:subjectCsid = '5b4c617e-53a0-484b-804e' "
     + "AND DOC.nuxeo:isVersion = false "
     + "ORDER BY DOC.collectionspace_core:updatedAt DESC";
     */
    /**
     * Returns the most recent Movement record related to a CollectionObject.
     *
     * This method currently returns the related Movement record with the latest
     * (i.e. most recent in time) Location Date field value.
     *
     * @param session a repository session.
     * @param collectionObjectCsid a CollectionObject identifier (CSID)
     * @param isAboutToBeRemovedEvent whether the current event involves a
     * record that is slated for removal (hard deletion)
     * @param movementCsidToFilter the CSID of a Movement record slated for
     * deletion, or of a Movement record referenced by a Relation record slated
     * for deletion. This record should be filtered out, prior to returning the
     * most recent Movement record.
     * @throws ClientException
     * @return the most recent Movement record related to the CollectionObject
     * identified by the supplied CSID.
     * @throws DocumentException 
     */
    protected DocumentModel getMostRecentMovementRecord(Event event,
    		CoreSessionInterface session, String collectionObjectCsid,
            boolean isAboutToBeRemovedEvent, String eventMovementCsid) throws ClientException {
    	DocumentModel result = null;
		
        //
        // Get active Relation records involving Movement records related to this CollectionObject.
        //
        String query = String.format(
                "SELECT * FROM %1$s WHERE " // collectionspace_core:tenantId = 1 "
                + "("
                + "  (%2$s:subjectCsid = '%3$s' "
                + "  AND %2$s:objectDocumentType = '%4$s') "
                + " OR "
                + "  (%2$s:objectCsid = '%3$s' "
                + "  AND %2$s:subjectDocumentType = '%4$s') "
                + ")"
                + ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT,
                RELATION_DOCTYPE, RELATIONS_COMMON_SCHEMA, collectionObjectCsid, MOVEMENT_DOCTYPE);
        getLogger().trace("query=" + query);

        DocumentModelList relationDocModels;
		try {
			relationDocModels = session.query(query);
		} catch (DocumentException e) {
			getLogger().error(e);
			return null;
		}
		
    	if (isCreatingNewRelationship(event) == true) {
        	DocumentModel newRelation = ((DocumentEventContext)event.getContext()).getSourceDocument();
        	relationDocModels.add(newRelation);
    	}
                
        //
        // Remove redundant document models from the list.
        //
        relationDocModels = removeRedundantRelations(relationDocModels);        
        
        //
        // Remove relationships that are with inactive movement records
        //
        relationDocModels = removeInactiveRelations(session, relationDocModels, isAboutToBeRemovedEvent, eventMovementCsid);
        
        //
        // If there are no candidate relationships after we removed the duplicates and inactive ones,
        // throw an exception.
        //
        if (relationDocModels == null || relationDocModels.size() == 0) {
        	return result;
        }
        
        //
        // If there is only one related movement record, then return it as the most recent
        // movement record -but only if it's current location element is not empty.
        //
        if (relationDocModels.size() == 1) {
        	DocumentModel relationDocModel = relationDocModels.get(0);
        	DocumentModel movementDocModel = getMovementDocModelFromRelation(session, relationDocModel);
            String location = (String) movementDocModel.getProperty(MOVEMENTS_COMMON_SCHEMA, CURRENT_LOCATION_ELEMENT_NAME);
            
            if (Tools.isBlank(location) == false) {
            	result = movementDocModel;
            } else { // currentLocation must be set
            	getLogger().error(String.format("Movement record=%s is missing its required location value and so is excluded from the computation of cataloging record=%s's current location.",
            			NuxeoUtils.getCsid(movementDocModel), collectionObjectCsid));
            }

            return result;
        }
        
        //
        // Iterate through the list (>2) of related movement records, to find the related
        // Movement record with the most recent location date.
        //
        GregorianCalendar mostRecentLocationDate = EARLIEST_COMPARISON_DATE;
        GregorianCalendar mostRecentUpdatedDate = EARLIEST_COMPARISON_DATE;

        for (DocumentModel relationDocModel : relationDocModels) {
        	//
        	// The movement record is either the subject or object of the relationship, but not both.
        	//
            String relatedMovementCsid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
            if (relatedMovementCsid.equals(collectionObjectCsid)) {
                relatedMovementCsid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
            }
            DocumentModel movementDocModel = getCurrentDocModelFromCsid(session, relatedMovementCsid);
            
            //
            // If the current Movement record lacks a location date, it cannot
            // be established as the most recent Movement record; skip over it.
            //
            GregorianCalendar locationDate = (GregorianCalendar) movementDocModel.getProperty(MOVEMENTS_COMMON_SCHEMA, LOCATION_DATE_PROPERTY);
            if (locationDate == null) {
            	getLogger().info(String.format("Movement record=%s has no location date and so is excluded from computation of cataloging record=%s current location.",
            			NuxeoUtils.getCsid(movementDocModel), collectionObjectCsid));
                continue;
            }
            
            GregorianCalendar updatedDate = (GregorianCalendar) movementDocModel.getProperty(COLLECTIONSPACE_CORE_SCHEMA, UPDATED_AT_PROPERTY);
            if (locationDate.after(mostRecentLocationDate)) {
                mostRecentLocationDate = locationDate;
                mostRecentUpdatedDate = updatedDate;
                result = movementDocModel;
            } else if (locationDate.compareTo(mostRecentLocationDate) == 0) {
                // If the current Movement record's location date is identical
                // to that of the (at this time) most recent Movement record, then
                // instead compare the two records using their update date values
                if (updatedDate.after(mostRecentUpdatedDate)) {
                    // The most recent location date value doesn't need to be
                    // updated here, as the two records' values are identical
                    mostRecentUpdatedDate = updatedDate;
                    result = movementDocModel;
                }
            }
        }
        
        return result;
    }
    
	//
    // This method assumes that the relation passed into this method is between a Movement record
    // and a CollectionObject (cataloging) record.
    //
    private DocumentModel getMovementDocModelFromRelation(CoreSessionInterface session, DocumentModel relationDocModel) {
    	String movementCsid = null;
    	
        String subjectDocType = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_DOCTYPE_PROPERTY);
        if (subjectDocType.endsWith(MOVEMENT_DOCTYPE)) {
        	movementCsid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
        } else {
        	movementCsid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
        }

		return getCurrentDocModelFromCsid(session, movementCsid);
	}

	//
    // Compares two Relation document models to see if they're either identical or
    // reciprocal equivalents. 
    //
    private static boolean compareRelationDocModels(DocumentModel r1, DocumentModel r2) {
    	boolean result = false;
    	
        String r1_subjectDocType = (String) r1.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_DOCTYPE_PROPERTY);
        String r1_objectDocType = (String) r1.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_DOCTYPE_PROPERTY);
        String r1_subjectCsid = (String) r1.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
        String r1_objectCsid = (String) r1.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);

        String r2_subjectDocType = (String) r2.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_DOCTYPE_PROPERTY);
        String r2_objectDocType = (String) r2.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_DOCTYPE_PROPERTY);
        String r2_subjectCsid = (String) r2.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
        String r2_objectCsid = (String) r2.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
        
        // Check to see if they're identical
        if (r1_subjectDocType.equalsIgnoreCase(r2_subjectDocType) && r1_objectDocType.equalsIgnoreCase(r2_objectDocType)
        		&& r1_subjectCsid.equalsIgnoreCase(r2_subjectCsid) && r1_objectCsid.equalsIgnoreCase(r2_objectCsid)) {
        	return true;
        }
        
        // Check to see if they're reciprocal
        if (r1_subjectDocType.equalsIgnoreCase(r2_objectDocType) && r1_objectDocType.equalsIgnoreCase(r2_subjectDocType)
        		&& r1_subjectCsid.equalsIgnoreCase(r2_objectCsid) && r1_objectCsid.equalsIgnoreCase(r2_subjectCsid)) {
        	return true;
        }

    	return result;
    }

    //
    // Return a Relation document model list with redundant (either identical or reciprocal) relations removed.
    //
    private static DocumentModelList removeRedundantRelations(DocumentModelList relationDocModelList) {
    	DocumentModelList resultList = null;
    	
    	if (relationDocModelList != null && relationDocModelList.size() > 0) {
	    	resultList = new DocumentModelListImpl();
	    	for (DocumentModel relationDocModel : relationDocModelList) {
	    		if (existsInResultList(resultList, relationDocModel) == false) {
	    			resultList.add(relationDocModel);
	    		}
	    	}
    	}
    	
		return resultList;
	}
    
    //
    // Return just the list of active relationships with active Movement records.  A value of 'true' for the 'isAboutToBeRemovedEvent'
    // argument indicates that relationships with the 'movementCsid' record should be considered inactive.
    //
    private DocumentModelList removeInactiveRelations(CoreSessionInterface session,
    		DocumentModelList relationDocModelList,
    		boolean isAboutToBeRemovedEvent,
    		String eventMovementCsid) {
    	DocumentModelList resultList = null;
    	
    	if (relationDocModelList != null && relationDocModelList.size() > 0) {
    		resultList = new DocumentModelListImpl();
	    	for (DocumentModel relationDocModel : relationDocModelList) {
	            String movementCsid = getCsidForDesiredDocTypeFromRelation(relationDocModel, MOVEMENT_DOCTYPE, COLLECTIONOBJECT_DOCTYPE);
	            DocumentModel movementDocModel = getCurrentDocModelFromCsid(session, movementCsid);
	            if (isActiveDocument(movementDocModel, isAboutToBeRemovedEvent, eventMovementCsid) == true) {
	    			resultList.add(relationDocModel);
	            } else {
	            	getLogger().debug(String.format("Disqualified relationship=%s with Movement record=%s from current location computation.",
	            			NuxeoUtils.getCsid(relationDocModel), movementCsid));
	            }
	    	}
    	}
    	
		return resultList;
	}

    //
    // Check to see if the Relation (or its equivalent reciprocal) is already in the list.
    //
	private static boolean existsInResultList(DocumentModelList relationDocModelList, DocumentModel relationDocModel) {
		boolean result = false;
		
    	for (DocumentModel target : relationDocModelList) {
    		if (compareRelationDocModels(relationDocModel, target) == true) {
    			result = true;
    			break;
    		}
    	}
		
		return result;
	}

	/**
     * Returns the CSID for a desired document type from a Relation record,
     * where the relationship involves two specified document types.
     *
     * @param relationDocModel a document model for a Relation record.
     * @param desiredDocType a desired document type.
     * @param relatedDocType a related document type.
     * @throws ClientException
     * 
     * @return the CSID from the desired document type in the relation. Returns
     * null if the Relation record does not involve both the desired
     * and related document types.
     */
    protected static String getCsidForDesiredDocTypeFromRelation(DocumentModel relationDocModel,
            String desiredDocType, String relatedDocType) {
        String csid = null;
        String subjectDocType = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_DOCTYPE_PROPERTY);
        String objectDocType = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_DOCTYPE_PROPERTY);
        
        if (subjectDocType.startsWith(desiredDocType) && objectDocType.startsWith(relatedDocType)) {  // Use startsWith() method, because customized tenant type names differ in their suffix.
            csid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
        } else if (subjectDocType.startsWith(relatedDocType) && objectDocType.startsWith(desiredDocType)) {
            csid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
        }
        
        return csid;
    }

    // The following method can be extended by sub-classes to update
    // different/multiple values; e.g. values for moveable locations ("crates").
    /**
     * Updates a CollectionObject record with selected values from a Movement
     * record.
     *
     * @param collectionObjectDocModel a document model for a CollectionObject
     * record.
     * @param movementDocModel a document model for a Movement record.
     * @return a potentially updated document model for the CollectionObject
     * record.
     * @throws ClientException
     */
    protected abstract boolean updateCollectionObjectLocation(DocumentModel collectionObjectDocModel, DocumentModel movmentDocModel);
}