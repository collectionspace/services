/**	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * ConditioncheckProxy.java
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 *
 */
@Path(ConditioncheckClient.SERVICE_PATH_PROXY)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public interface ConditioncheckProxy extends CollectionSpaceCommonListPoxProxy {
}