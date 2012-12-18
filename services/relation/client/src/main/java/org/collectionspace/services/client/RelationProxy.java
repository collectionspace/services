package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.workflow.WorkflowClient;

import org.jboss.resteasy.client.ClientResponse;
import javax.ws.rs.QueryParam;

/**
 * @version $Revision:$
 */
@Path("/relations/")
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface RelationProxy extends CollectionSpacePoxProxy<RelationsCommonList> {

    @GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> readList();
    
    @Override
	@GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> readIncludeDeleted(
            @QueryParam(WorkflowClient.WORKFLOWSTATE_QUERY) String workflowState);        
    
    @Override
    @GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> keywordSearchIncludeDeleted(
    		@QueryParam(IQueryManager.SEARCH_TYPE_KEYWORDS_KW) String keywords,
            @QueryParam(WorkflowClient.WORKFLOWSTATE_QUERY) String workflowState);

    @GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> readList(
    		@QueryParam(IRelationsManager.SUBJECT_QP) String subjectCsid,
    		@QueryParam(IRelationsManager.SUBJECT_TYPE_QP) String subjectType,
    		@QueryParam(IRelationsManager.PREDICATE_QP) String predicate,
    		@QueryParam(IRelationsManager.OBJECT_QP) String objectCsid,
    		@QueryParam(IRelationsManager.OBJECT_TYPE_QP) String objectType);

    @GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> readList(
    		@QueryParam(IRelationsManager.SUBJECT_QP) String subjectCsid,
    		@QueryParam(IRelationsManager.SUBJECT_TYPE_QP) String subjectType,
    		@QueryParam(IRelationsManager.PREDICATE_QP) String predicate,
    		@QueryParam(IRelationsManager.OBJECT_QP) String objectCsid,
    		@QueryParam(IRelationsManager.OBJECT_TYPE_QP) String objectType,
            @QueryParam(IClientQueryParams.ORDER_BY_PARAM) String sortBy,
            @QueryParam(IClientQueryParams.PAGE_SIZE_PARAM) Long pageSize,
    	    @QueryParam(IClientQueryParams.START_PAGE_PARAM) Long pageNumber);
}
