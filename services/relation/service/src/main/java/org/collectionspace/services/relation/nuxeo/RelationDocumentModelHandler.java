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
package org.collectionspace.services.relation.nuxeo;

import java.util.HashMap;
import java.util.Iterator;
import java.net.HttpURLConnection;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceException;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.InvalidDocumentException;
import org.collectionspace.services.common.relation.RelationJAXBSchema;
import org.collectionspace.services.common.relation.nuxeo.RelationConstants;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;
import org.collectionspace.services.relation.RelationsDocListItem;

// HACK HACK HACK
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PersonAuthorityClient;
import org.collectionspace.services.client.OrgAuthorityClient;
import org.collectionspace.services.client.LocationAuthorityClient;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.client.PlaceAuthorityClient;
import org.collectionspace.services.client.ConceptAuthorityClient;
import org.collectionspace.services.client.workflow.WorkflowClient;

import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RelationDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RelationDocumentModelHandler
        extends RemoteDocumentModelHandlerImpl<RelationsCommon, RelationsCommonList> {

    private final Logger logger = LoggerFactory.getLogger(RelationDocumentModelHandler.class);
    /**
     * relation is used to stash JAXB object to use when handle is called
     * for Action.CREATE, Action.UPDATE or Action.GET
     */
    private RelationsCommon relation;
    /**
     * relationList is stashed when handle is called
     * for ACTION.GET_ALL
     */
    private RelationsCommonList relationList;
    
    private static final String ERROR_TERMS_IN_WORKFLOWSTATE = "Cannot modify a relationship if either end is in the workflow state: ";

    
    private boolean subjectOrObjectInWorkflowState(DocumentWrapper<DocumentModel> wrapDoc, String workflowState) throws ServiceException {
    	boolean result = false;
    	DocumentModel relationDocModel = wrapDoc.getWrappedObject();
    	String errMsg = ERROR_TERMS_IN_WORKFLOWSTATE + workflowState;
    			
        RepositoryInstance repoSession = this.getRepositorySession();
        try {
			DocumentModel subjectDocModel = getSubjectOrObjectDocModel(repoSession, relationDocModel, SUBJ_DOC_MODEL);
			DocumentModel objectDocModel = getSubjectOrObjectDocModel(repoSession, relationDocModel, OBJ_DOC_MODEL);
			if (subjectDocModel.getCurrentLifeCycleState().equalsIgnoreCase(workflowState) ||
					objectDocModel.getCurrentLifeCycleState().equalsIgnoreCase(workflowState)) {
				result = true;
			}
		} catch (Exception e) {
			if (logger.isInfoEnabled() == true) {
				logger.info(errMsg, e);
			}
		}
    	        
    	return result;
    }
    
	@Override
	/*
	 * Until we rework the RepositoryClient to handle the workflow transition (just like it does for 'create', 'get', 'update', and 'delete', this method will only check to see if the transition is allowed.  Until then,
	 * the WorkflowDocumentModelHandler class does the actual workflow transition.
	 * 
	 * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#handleWorkflowTransition(org.collectionspace.services.common.document.DocumentWrapper, org.collectionspace.services.lifecycle.TransitionDef)
	 */
	public void handleWorkflowTransition(DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef)
			throws Exception {
		String workflowState = transitionDef.getDestinationState();
		if (subjectOrObjectInWorkflowState(wrapDoc, workflowState) == true) {
    		throw new ServiceException(HttpURLConnection.HTTP_FORBIDDEN,
                    "Cannot change a relationship if either end of it is in the workflow state: " + workflowState);
		}
	}

    @Override
    public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// Merge in the data from the payload
        super.handleCreate(wrapDoc);

        // And take care of ensuring all the values for the relation info are correct 
        populateSubjectAndObjectValues(wrapDoc);
    	
        // both subject and object cannot be locked
    	String workflowState = WorkflowClient.WORKFLOWSTATE_LOCKED;
    	if (subjectOrObjectInWorkflowState(wrapDoc, workflowState) == true) {
    		throw new ServiceException(HttpURLConnection.HTTP_FORBIDDEN,
                    "Cannot create a relationship if either end is in the workflow state: " + workflowState);
    	}
    }

    @Override
    public void handleUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	// Merge in the data from the payload
        super.handleUpdate(wrapDoc);
        
        // And take care of ensuring all the values for the relation info are correct 
        populateSubjectAndObjectValues(wrapDoc);
    }
    
    @Override
    public void handleDelete(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
    	String workflowState = WorkflowClient.WORKFLOWSTATE_LOCKED;
    	// both subject and object cannot be locked
    	if (subjectOrObjectInWorkflowState(wrapDoc, workflowState) == false) {
    		super.handleDelete(wrapDoc);
    	} else {
    		throw new ServiceException(HttpURLConnection.HTTP_FORBIDDEN,
                    "Cannot delete a relationship if either end is in the workflow state: " + workflowState);
    	}
    }
    
    private void populateSubjectAndObjectValues(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        // Obtain document models for the subject and object of the relation, so that
        // we ensure we have value docType, URI info. If the docModels support refNames, 
        // we will also set those.
        // Note that this introduces another caching problem... 
        DocumentModel relationDocModel = wrapDoc.getWrappedObject();
        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
        RepositoryInstance repoSession = this.getRepositorySession();
        
        DocumentModel subjectDocModel = getSubjectOrObjectDocModel(repoSession, relationDocModel, SUBJ_DOC_MODEL);
        DocumentModel objectDocModel = getSubjectOrObjectDocModel(repoSession, relationDocModel, OBJ_DOC_MODEL);

        // Use values from the subject and object document models to populate the
        // relevant fields of the relation's own document model.
        if (subjectDocModel != null) {
            populateSubjectOrObjectValues(relationDocModel, subjectDocModel, SUBJ_DOC_MODEL);
        }
        if (objectDocModel != null) {
            populateSubjectOrObjectValues(relationDocModel, objectDocModel, OBJ_DOC_MODEL);
        }
    }

    @Override
    public RelationsCommon getCommonPart() {
        return relation;
    }

    @Override
    public void setCommonPart(RelationsCommon theRelation) {
        this.relation = theRelation;
    }

    /**get associated Relation (for index/GET_ALL)
     */
    @Override
    public RelationsCommonList getCommonPartList() {
        return relationList;
    }

    @Override
    public void setCommonPartList(RelationsCommonList theRelationList) {
        this.relationList = theRelationList;
    }

    @Override
    public RelationsCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(RelationsCommon theRelation, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public RelationsCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        RelationsCommonList relList = this.extractPagingInfo(new RelationsCommonList(), wrapDoc);
        relList.setFieldsReturned("subjectCsid|relationshipType|predicateDisplayName|relationshipMetaType|objectCsid|uri|csid|subject|object");
        ServiceContext ctx = getServiceContext();
        String serviceContextPath = getServiceContextPath();

        TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        String serviceName = getServiceContext().getServiceName().toLowerCase();
        ServiceBindingType sbt = tReader.getServiceBinding(ctx.getTenantId(), serviceName);

        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while (iter.hasNext()) {
            DocumentModel docModel = iter.next();
            RelationListItem relListItem = getRelationListItem(ctx, sbt, tReader, docModel, serviceContextPath);
            relList.getRelationListItem().add(relListItem);
        }
        return relList;
    }

    /** Gets the relation list item, looking up the subject and object documents, and getting summary
     *  info via the objectName and objectNumber properties in tenant-bindings.
     * @param ctx the ctx
     * @param sbt the ServiceBindingType of Relations service
     * @param tReader the tenant-bindings reader, for looking up docnumber and docname
     * @param docModel the doc model
     * @param serviceContextPath the service context path
     * @return the relation list item, with nested subject and object summary info.
     * @throws Exception the exception
     */
    private RelationListItem getRelationListItem(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            ServiceBindingType sbt,
            TenantBindingConfigReaderImpl tReader,
            DocumentModel docModel,
            String serviceContextPath) throws Exception {
        RelationListItem relationListItem = new RelationListItem();
        String id = getCsid(docModel);
        relationListItem.setCsid(id);

        relationListItem.setSubjectCsid((String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.SUBJECT_CSID));

        String predicate = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.RELATIONSHIP_TYPE);
        relationListItem.setRelationshipType(predicate);
        relationListItem.setPredicate(predicate); //predicate is new name for relationshipType.
        relationListItem.setPredicateDisplayName((String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.RELATIONSHIP_TYPE_DISPLAYNAME));

        relationListItem.setRelationshipMetaType((String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.RELATIONSHIP_META_TYPE));
        relationListItem.setObjectCsid((String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.OBJECT_CSID));

        relationListItem.setUri(serviceContextPath + id);

        //Now fill in summary info for the related docs: subject and object.
        String subjectCsid = relationListItem.getSubjectCsid();
        String subjectDocumentType = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.SUBJECT_DOCTYPE);
        RelationsDocListItem subject = createRelationsDocListItem(ctx, sbt, subjectCsid, tReader, subjectDocumentType);

        String subjectUri = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.SUBJECT_URI);
        subject.setUri(subjectUri);
        String subjectRefName = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.SUBJECT_REFNAME);
        subject.setRefName(subjectRefName);
        relationListItem.setSubject(subject);

        String objectCsid = relationListItem.getObjectCsid();
        String objectDocumentType = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.OBJECT_DOCTYPE);
        RelationsDocListItem object = createRelationsDocListItem(ctx, sbt, objectCsid, tReader, objectDocumentType);

        String objectUri = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.OBJECT_URI);
        object.setUri(objectUri);
        String objectRefName = (String) docModel.getProperty(ctx.getCommonPartLabel(), 
        												RelationJAXBSchema.OBJECT_REFNAME);
        object.setRefName(objectRefName);
        relationListItem.setObject(object);

        return relationListItem;
    }

    // DocumentModel itemDocModel = docModelFromCSID(ctx, itemCsid);
    protected RelationsDocListItem createRelationsDocListItem(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            ServiceBindingType sbt,
            String itemCsid,
            TenantBindingConfigReaderImpl tReader,
            String documentType) throws Exception {
        RelationsDocListItem item = new RelationsDocListItem();
        item.setDocumentType(documentType);//this one comes from the record, as subjectDocumentType, objectDocumentType.
        item.setCsid(itemCsid);

        DocumentModel itemDocModel = NuxeoUtils.getDocFromCsid(ctx, this.getRepositorySession(), itemCsid);    //null if not found.
        if (itemDocModel != null) {
            String itemDocType = itemDocModel.getDocumentType().getName();
            itemDocType = ServiceBindingUtils.getUnqualifiedTenantDocType(itemDocType);
            if (Tools.isBlank(documentType)) {
                item.setDocumentType(itemDocType);
            }

            //TODO: ensure that itemDocType is really the entry point, i.e. servicename==doctype
            //ServiceBindingType itemSbt2 = tReader.getServiceBinding(ctx.getTenantId(), itemDocType);
            String propName = "ERROR-FINDING-PROP-VALUE";
            ServiceBindingType itemSbt = tReader.getServiceBindingForDocType(ctx.getTenantId(), itemDocType);
            try {
                propName = ServiceBindingUtils.getPropertyValue(itemSbt, ServiceBindingUtils.OBJ_NAME_PROP);
                String itemDocname = ServiceBindingUtils.getMappedFieldInDoc(itemSbt, ServiceBindingUtils.OBJ_NAME_PROP, itemDocModel);
                if (propName == null || itemDocname == null) {
                } else {
                    item.setName(itemDocname);
                }
            } catch (Throwable t) {
            	logger.error("====Error finding objectNameProperty: " + itemDocModel + " field " + ServiceBindingUtils.OBJ_NAME_PROP + "=" + propName
                        + " not found in itemDocType: " + itemDocType + " inner: " + t.getMessage());
            }
            propName = "ERROR-FINDING-PROP-VALUE";
            try {
                propName = ServiceBindingUtils.getPropertyValue(itemSbt, ServiceBindingUtils.OBJ_NUMBER_PROP);
                String itemDocnumber = ServiceBindingUtils.getMappedFieldInDoc(itemSbt, ServiceBindingUtils.OBJ_NUMBER_PROP, itemDocModel);

                if (propName == null || itemDocnumber == null) {
                } else {
                    item.setNumber(itemDocnumber);
                }
            } catch (Throwable t) {
                logger.error("====Error finding objectNumberProperty: " + ServiceBindingUtils.OBJ_NUMBER_PROP + "=" + propName
                        + " not found in itemDocType: " + itemDocType + " inner: " + t.getMessage());
            }
        } else {
            item.setError("INVALID: related object is absent");
            // Laramie20110510 CSPACE-3739  throw the exception for 3739, otherwise, don't throw it.
            //throw new Exception("INVALID: related object is absent "+itemCsid);
        }
        return item;
    }

    @Override
    public String getQProperty(String prop) {
        return "/" + RelationConstants.NUXEO_SCHEMA_ROOT_ELEMENT + "/" + prop;
    }

    private final boolean SUBJ_DOC_MODEL = true;
    private final boolean OBJ_DOC_MODEL = false;
    
    private DocumentModel getSubjectOrObjectDocModel(
    		RepositoryInstance repoSession,
    		DocumentModel relationDocModel,
    		boolean fSubject) throws Exception {
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
    	
        // Get the document model for the object of the relation.
    	String commonPartLabel = ctx.getCommonPartLabel();
        String csid = "";
        String refName = "";
        DocumentModel docModel = null;
        // FIXME: Currently assumes that the object CSID is valid if present
        // in the incoming payload.
        try {
            csid = (String) relationDocModel.getProperty(commonPartLabel, 
            		(fSubject?RelationJAXBSchema.SUBJECT_CSID:RelationJAXBSchema.OBJECT_CSID));
        } catch (PropertyException pe) {
            // Per CSPACE-4468, ignore any property exception here.
            // The objectCsid and/or subjectCsid field in a relation record
            // can now be null (missing), because a refName value can be
            // provided as an alternate identifier.
        }
        if (Tools.notBlank(csid)) {
        	RepositoryJavaClientImpl nuxeoRepoClient = (RepositoryJavaClientImpl)getRepositoryClient(ctx);
            DocumentWrapper<DocumentModel> docWrapper = nuxeoRepoClient.getDocFromCsid(ctx, repoSession, csid);
            docModel = docWrapper.getWrappedObject();
        } else { //  if (Tools.isBlank(objectCsid)) {
            try {
            	refName = (String) relationDocModel.getProperty(commonPartLabel, 
            			(fSubject?RelationJAXBSchema.SUBJECT_REFNAME:RelationJAXBSchema.OBJECT_REFNAME));
            	docModel = ResourceBase.getDocModelForRefName(repoSession, refName, ctx.getResourceMap());
            } catch (Exception e) {
                throw new InvalidDocumentException(
                        "Relation record must have a CSID or refName to identify the object of the relation.", e);
            }
        }
        if(docModel==null) {
           	throw new DocumentNotFoundException("RelationDMH.getSubjectOrObjectDocModel could not find doc with CSID: "
           				+csid+" and/or refName: "+refName );
        }
        return docModel;
    }
    
    private void populateSubjectOrObjectValues(
    		DocumentModel relationDocModel, 
    		DocumentModel subjectOrObjectDocModel,
    		boolean fSubject ) {
    	ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
    	
        HashMap<String,Object> properties = new HashMap<String,Object>();
        try {
	        String doctype = subjectOrObjectDocModel.getDocumentType().getName();
            doctype = ServiceBindingUtils.getUnqualifiedTenantDocType(doctype);
	        properties.put((fSubject?RelationJAXBSchema.SUBJECT_DOCTYPE:RelationJAXBSchema.OBJECT_DOCTYPE),
	        					doctype);
	
	        String csid = (String) subjectOrObjectDocModel.getName();
	        properties.put((fSubject?RelationJAXBSchema.SUBJECT_CSID:RelationJAXBSchema.OBJECT_CSID),
	        					csid);
	
	        String uri = (String) subjectOrObjectDocModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
	        		CollectionSpaceClient.COLLECTIONSPACE_CORE_URI);
	        properties.put((fSubject?RelationJAXBSchema.SUBJECT_URI:RelationJAXBSchema.OBJECT_URI),
	        					uri);
	        
	    	/*
	    	String common_schema = getCommonSchemaNameForDocType(doctype);
	    	
	    	if(common_schema!=null) {
	    		String refname = (String)subjectOrObjectDocModel.getProperty(common_schema, 
	    														RefName.REFNAME );
	            properties.put((fSubject?RelationJAXBSchema.SUBJECT_REFNAME:RelationJAXBSchema.OBJECT_REFNAME),
	            		refname);
	    	}
	    	*/
	        String refname = (String) 
	        		subjectOrObjectDocModel.getProperty(
	        				CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
	        				CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
            properties.put((fSubject?
            					RelationJAXBSchema.SUBJECT_REFNAME
            					:RelationJAXBSchema.OBJECT_REFNAME),
            				refname);
        } catch (ClientException ce) {
            throw new RuntimeException(
                    "populateSubjectOrObjectValues: Problem fetching field " + ce.getLocalizedMessage());
        }

        // FIXME: Call below is based solely on Nuxeo API docs; have not yet verified that it correctly updates existing
        // property values in the target document model.
        try {
        	relationDocModel.setProperties(ctx.getCommonPartLabel(), properties);
        } catch (ClientException ce) {
            throw new RuntimeException(
                    "populateSubjectValues: Problem setting fields " + ce.getLocalizedMessage());
        }
    }
    
    /*
    private String getCommonSchemaNameForDocType(String docType) {
    	String common_schema = null;
    	if(docType!=null) {
    		// HACK - Use startsWith to allow for extension of schemas.
	    	if(docType.startsWith("Person"))
	    		common_schema = PersonAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME;
	    	else if(docType.startsWith("Organization"))
	    		common_schema = OrgAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME;
	    	else if(docType.startsWith("Locationitem"))
	    		common_schema = LocationAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME;
	    	else if(docType.startsWith("Taxon"))
	    		common_schema = TaxonomyAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME;
    		else if(docType.startsWith("Placeitem"))
    			common_schema = PlaceAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME;
	    	else if(docType.startsWith("Conceptitem"))
	    		common_schema = ConceptAuthorityClient.SERVICE_ITEM_COMMON_PART_NAME;
	    	//else leave it null.
    	}
    	return common_schema;
    }
    */

}
