/**	
 * ReportProxy.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

/**
 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
@Path("/reports/")
@Produces({"application/xml;charset=UTF-8"})
@Consumes({"application/xml"})
public interface ReportProxy extends CollectionSpaceCommonListPoxProxy {

    @GET
    @Produces({"application/xml"})
    ClientResponse<AbstractCommonList> readListFiltered(
    		@QueryParam(IQueryManager.SEARCH_TYPE_DOCTYPE) String docType,
    		@QueryParam(IQueryManager.SEARCH_TYPE_INVOCATION_MODE) String mode);
    
}
