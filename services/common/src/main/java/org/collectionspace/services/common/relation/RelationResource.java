/**	
 * RelationResource.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision$
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 *  Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.relation;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.common.relation.nuxeo.RelationsUtils;
import org.collectionspace.services.relation.RelationsCommon;
import org.collectionspace.services.relation.RelationsCommonList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

@Path("/relations")
@Consumes("application/xml")
@Produces("application/xml")
public class RelationResource extends ResourceBase {
	public final static String serviceName = "relations";
	final Logger logger = LoggerFactory.getLogger(RelationResource.class);
	
	@Override
	protected String getVersionString() {
		final String lastChangeRevision = "$LastChangedRevision$";
		return lastChangeRevision;
	}
	@Override
	public String getServiceName() {
		return serviceName;
	}
	@Override
    public Class<RelationsCommon> getCommonPartClass() {
    	return RelationsCommon.class;
    }
	
	@Override
	@GET
	@Produces("application/xml")
	public RelationsCommonList getList(@Context UriInfo uriInfo) {
		return this.getList(null, uriInfo);
	}

	public RelationsCommonList getList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx) {
		return this.getList(parentCtx, parentCtx.getUriInfo());
	}
	
	private RelationsCommonList getList(ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx, UriInfo uriInfo) {
		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		
		String subjectCsid = queryParams.getFirst(IRelationsManager.SUBJECT_QP);
		String subjectType = queryParams.getFirst(IRelationsManager.SUBJECT_TYPE_QP);
		String predicate = queryParams.getFirst(IRelationsManager.PREDICATE_QP);
		String objectCsid = queryParams.getFirst(IRelationsManager.OBJECT_QP);
		String objectType = queryParams.getFirst(IRelationsManager.OBJECT_TYPE_QP);

		return this.getRelationList(parentCtx, uriInfo, subjectCsid, subjectType, predicate, objectCsid, objectType);
	}

    private RelationsCommonList getRelationList(
    		ServiceContext<PoxPayloadIn, PoxPayloadOut> parentCtx,
    		UriInfo uriInfo,
    		String subjectCsid, String subjectType,
    		String predicate,
    		String objectCsid,
    		String objectType) throws WebApplicationException {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(uriInfo);
            if (parentCtx != null) { // If the parent context has an open repository session then use it
            	ctx.setCurrentRepositorySession(parentCtx.getCurrentRepositorySession());
            }
            DocumentHandler handler = createDocumentHandler(ctx);

            String relationClause = RelationsUtils.buildWhereClause(subjectCsid, subjectType, predicate, objectCsid, objectType);
            handler.getDocumentFilter().appendWhereClause(relationClause, IQueryManager.SEARCH_QUALIFIER_AND);
            //
            // Handle keyword clause
            //
            String keywords = uriInfo.getQueryParameters().getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);            
            if (keywords != null && keywords.isEmpty() == false) {
            	String keywordClause = QueryManager.createWhereClauseFromKeywords(keywords);
            	handler.getDocumentFilter().appendWhereClause(keywordClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            
            return (RelationsCommonList)finish_getList(ctx, handler);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }


}

