package org.collectionspace.services.listener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.CoreSessionWrapper;
import org.collectionspace.services.nuxeo.listener.AbstractCSEventSyncListenerImpl;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * Event listener that removes entries from a collectionobject's media priority list when the
 * media record an entry refers to is unrelated from the collectionobject, or deleted.
 */
public class UpdateMediaPriorityOnDelete extends AbstractCSEventSyncListenerImpl {

    private static final Logger logger = LoggerFactory.getLogger(UpdateMediaPriorityOnDelete.class);

    // FIXME: Get these constant values from external sources rather than redeclaring here
    final static String RELATION_DOCTYPE = "Relation";
    final static String MEDIA_DOCTYPE = "Media";
    final static String COLLECTIONOBJECT_DOCTYPE = "CollectionObject";
    final static String RELATIONS_COMMON_SCHEMA = "relations_common";
    final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common";
    final static String SUBJECT_CSID_PROPERTY = "subjectCsid";
    final static String OBJECT_CSID_PROPERTY = "objectCsid";
    final static String SUBJECT_REFNAME_PROPERTY = "subjectRefName";
    final static String OBJECT_REFNAME_PROPERTY = "objectRefName";
    final static String SUBJECT_DOCTYPE_PROPERTY = "subjectDocumentType";
    final static String OBJECT_DOCTYPE_PROPERTY = "objectDocumentType";
    final static String MEDIA_PRIORITY_LIST_FIELD = "mediaPriorityList";

    @Override
    public boolean shouldHandleEvent(Event event) {
        EventContext eventContext = event.getContext();

        if (!(eventContext instanceof DocumentEventContext)) {
            return false;
        }

        DocumentModel docModel = ((DocumentEventContext) eventContext).getSourceDocument();

        if (!documentMatchesType(docModel, RELATION_DOCTYPE)) {
            return false;
        }

        return DocumentEventTypes.ABOUT_TO_REMOVE.equals(event.getName())
                || isDocumentSoftDeletedEvent(eventContext);
    }

    @Override
    public void handleCSEvent(Event event) {
        DocumentEventContext docContext = (DocumentEventContext) event.getContext();
        DocumentModel relationDocModel = docContext.getSourceDocument();

        String subjectDocType = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_DOCTYPE_PROPERTY);
        String objectDocType = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_DOCTYPE_PROPERTY);

        String mediaRefName;
        String collectionObjectCsid;

        if (MEDIA_DOCTYPE.equals(subjectDocType) && COLLECTIONOBJECT_DOCTYPE.equals(objectDocType)) {
            mediaRefName = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_REFNAME_PROPERTY);
            collectionObjectCsid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_CSID_PROPERTY);
        } else if (MEDIA_DOCTYPE.equals(objectDocType) && COLLECTIONOBJECT_DOCTYPE.equals(subjectDocType)) {
            mediaRefName = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, OBJECT_REFNAME_PROPERTY);
            collectionObjectCsid = (String) relationDocModel.getProperty(RELATIONS_COMMON_SCHEMA, SUBJECT_CSID_PROPERTY);
        } else {
            // Not a media/collectionobject relation.
            return;
        }

        CoreSessionInterface session = new CoreSessionWrapper(docContext.getCoreSession());
        DocumentModel collectionObjectDocModel = getCurrentDocModelFromCsid(session, collectionObjectCsid);

        if (collectionObjectDocModel == null || !isActiveDocument(collectionObjectDocModel)) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> mediaPriorityList = (List<String>) collectionObjectDocModel.getProperty(
                COLLECTIONOBJECTS_COMMON_SCHEMA, MEDIA_PRIORITY_LIST_FIELD);

        if (mediaPriorityList == null) {
            return;
        }

        List<String> updatedList = new ArrayList<>(mediaPriorityList);

        if (!updatedList.removeAll(List.of(mediaRefName))) {
            // The media record was not in the priority list.
            return;
        }

        collectionObjectDocModel.setProperty(COLLECTIONOBJECTS_COMMON_SCHEMA, MEDIA_PRIORITY_LIST_FIELD,
                (Serializable) updatedList);
        session.saveDocument(collectionObjectDocModel);

        logger.info("Removed media record {} from the media priority list of collectionobject {}",
                mediaRefName, collectionObjectCsid);
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }
}
