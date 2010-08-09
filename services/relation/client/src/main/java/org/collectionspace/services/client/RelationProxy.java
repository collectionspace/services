package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.common.relation.IRelationsManager;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import javax.ws.rs.QueryParam;

/**
 * @version $Revision:$
 */
@Path("/relations/")
@Produces({"multipart/mixed"})
@Consumes({"multipart/mixed"})
public interface RelationProxy extends CollectionSpaceProxy {

    @GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> readList();
    
    @GET
    @Produces({"application/xml"})
    ClientResponse<RelationsCommonList> readList(
    		@QueryParam(IRelationsManager.SUBJECT_QP) String subjectCsid,
    		@QueryParam(IRelationsManager.SUBJECT_TYPE_QP) String subjectType,
    		@QueryParam(IRelationsManager.PREDICATE_QP) String predicate,
    		@QueryParam(IRelationsManager.OBJECT_QP) String objectCsid,
    		@QueryParam(IRelationsManager.OBJECT_TYPE_QP) String objectType);
    
    //(C)reate
    @POST
    ClientResponse<Response> create(MultipartOutput multipart);

    //(R)ead
    @GET
    @Path("/{csid}")
    ClientResponse<MultipartInput> read(@PathParam("csid") String csid);

    //(U)pdate
    @PUT
    @Path("/{csid}")
    ClientResponse<MultipartInput> update(@PathParam("csid") String csid, MultipartOutput multipart);

    //(D)elete
    @DELETE
    @Path("/{csid}")
    ClientResponse<Response> delete(@PathParam("csid") String csid);
}
