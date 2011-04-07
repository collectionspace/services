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
package org.collectionspace.services.relation;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.relation.IRelationsManager;
import org.collectionspace.services.common.relation.nuxeo.RelationsUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;

import org.collectionspace.services.jaxb.AbstractCommonList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	@GET
	@Produces("application/xml")
	public RelationsCommonList getRelationList(@Context UriInfo ui,
			@QueryParam(IRelationsManager.SUBJECT_QP) String subjectCsid,
			@QueryParam(IRelationsManager.SUBJECT_TYPE_QP) String subjectType,
			@QueryParam(IRelationsManager.PREDICATE_QP) String predicate,
			@QueryParam(IRelationsManager.OBJECT_QP) String objectCsid,
			@QueryParam(IRelationsManager.OBJECT_TYPE_QP) String objectType) {
		MultivaluedMap<String, String> queryParams = ui.getQueryParameters();
		return this.getRelationList(queryParams, subjectCsid, subjectType, predicate, objectCsid, objectType);
	}

    public RelationsCommonList getRelationList(MultivaluedMap<String, String> queryParams, String subjectCsid, String subjectType,
                                                                         String predicate, String objectCsid, String objectType) throws WebApplicationException {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(queryParams);
            DocumentHandler handler = createDocumentHandler(ctx);

            String relationClause = RelationsUtils.buildWhereClause(subjectCsid, subjectType, predicate, objectCsid, objectType);
            handler.getDocumentFilter().appendWhereClause(relationClause, IQueryManager.SEARCH_QUALIFIER_AND);

            return (RelationsCommonList)finish_getList(ctx, handler);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }


}

