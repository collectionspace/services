package org.collectionspace.services.listener.bampfa;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Map;
import java.util.*;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.ecm.core.api.event.DocumentEventTypes;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.impl.ListProperty;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateNationalitiesListener implements EventListener {

    private final static Log logger = LogFactory.getLog(UpdateNationalitiesListener.class);
    private final static String NO_FURTHER_PROCESSING_MESSAGE =
            "This event listener will not continue processing this event ...";
    
    private final static GregorianCalendar EARLIEST_COMPARISON_DATE = new GregorianCalendar(1600, 1, 1);

    private final static String SUBJECT_CSID_PROPERTY = "subjectCsid"; // FIXME: Get from external constant
    private final static String OBJECT_CSID_PROPERTY = "objectCsid"; // FIXME: Get from external constant
    private final static String SUBJECT_DOCTYPE_PROPERTY = "subjectDocumentType"; // FIXME: Get from external constant
    private final static String OBJECT_DOCTYPE_PROPERTY = "objectDocumentType"; // FIXME: Get from external constant

    private final static String COLLECTIONOBJECT_DOCTYPE = "CollectionObject";
    protected final static String COLLECTIONOBJECTS_COMMON_SCHEMA = "collectionobjects_common"; // FIXME: Get from external constant
    //private final static String COLLECTIONOBJECT_DOCTYPE = "CollectionObject"; // FIXME: Get from external constant
    private final static String COLLECTIONOBJECTS_BAMPFA_SCHEMA = "collectionobjects_bampfa";

    private final static String PERSON_DOCTYPE = "Person";
    private final static String PERSONS_SCHEMA = "persons_common";
    private final static String PERSONS_NATIONALITIES_SCHEMA = "persons_common_nationalities";

    protected final static String COLLECTIONSPACE_CORE_SCHEMA = "collectionspace_core"; // FIXME: Get from external constant
    protected final static String CREATED_AT_PROPERTY = "createdAt"; // FIXME: Get from external constant
    protected final static String UPDATED_AT_PROPERTY = "updatedAt"; // FIXME: Get from external constant
    private final static String NONVERSIONED_NONPROXY_DOCUMENT_WHERE_CLAUSE_FRAGMENT =
            "AND ecm:isCheckedInVersion = 0"
            + "AND ecm:isProxy = 0 ";
    private final static String ACTIVE_DOCUMENT_WHERE_CLAUSE_FRAGMENT =
            "AND (ecm:currentLifeCycleState <> 'deleted') "
            + NONVERSIONED_NONPROXY_DOCUMENT_WHERE_CLAUSE_FRAGMENT;

    public enum EventNotificationDocumentType {
        // Document type about which we've received a notification

        PERSONS, COLLECTIONOBJECT;
    }
    
    
    @Override
    public void handleEvent(Event event) throws ClientException {
        logger.trace("In handleEvent in Update Nationalities");

        EventContext eventContext = event.getContext();
        if (eventContext == null || !(eventContext instanceof DocumentEventContext)) {
            return;
        }

        // Check if the relationship is between a collection object and and a persons record
        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();

        String personCsid = "";
        Enum notificationDocummentType;

        // Two possibilities:
        // Either one Person record is being added to the document, or one from the dropdown is


        if (documentMatchesType(docModel, PERSON_DOCTYPE)) {
            logger.trace("Person document was received");

            personCsid = NuxeoUtils.getCsid(docModel);
            if (Tools.isBlank(personCsid)) {
                logger.warn("Could not obtain CSID for Person record from document event.");
                logger.warn(NO_FURTHER_PROCESSING_MESSAGE);
                return;
            }
        } else if (documentMatchesType(docModel, COLLECTIONOBJECT_DOCTYPE)) {

            if (event.getName().equals("documentCreated")) {
                // Trigger the before thingy
                docModel.getCoreSession().saveDocument(docModel);

                return;
            }

            // Obtain the previous document

            DocumentModel previousDoc = (DocumentModel) docEventContext.getProperty(CoreEventConstants.PREVIOUS_DOCUMENT_MODEL);

            CoreSession coreSession = docEventContext.getCoreSession();

            List<Object> nationalities = getNationalities(docModel, coreSession);

            docModel.setProperty(COLLECTIONOBJECTS_BAMPFA_SCHEMA, "nationalities", nationalities);

            return;
        } else {
            logger.trace("No pesons record was involved");
            return;
        }

//        personCsid = getCsidForDesiredDocTypeFromRelation(docModel, PERSON_DOCTYPE, COLLECTIONOBJECT_DOCTYPE);
    }

    public List<Object> getNationalities(DocumentModel docModel, CoreSession coreSession) {
        String fieldRequested = "bampfaObjectProductionPersonGroupList";
        List<Map<String, Object>> bampfaObjectProductionPersonGroupList = (List<Map<String, Object>>) docModel.getProperty(COLLECTIONOBJECTS_BAMPFA_SCHEMA, fieldRequested);

        Set allNationalities = new HashSet<String>();

        for (Map<String, Object> bampfaObjectProductionGroup : bampfaObjectProductionPersonGroupList) {
            String currRefName = (String) bampfaObjectProductionGroup.get("bampfaObjectProductionPerson");

            String query = "SELECT * FROM Person WHERE persons_common:refName=\"" + currRefName + '"';

            List nationalities = (List) coreSession.query(query).get(0).getProperty(PERSONS_SCHEMA, "nationalities");

            allNationalities.addAll(nationalities);
        }

        return Arrays.asList(allNationalities.toArray());

    }


    protected static boolean documentMatchesType(DocumentModel docModel, String docType) {
        if (docModel == null || Tools.isBlank(docType)) {
            return false;
        }
        if (docModel.getType().startsWith(docType)) {
            return true;
        }
        return false;
    }
    

}