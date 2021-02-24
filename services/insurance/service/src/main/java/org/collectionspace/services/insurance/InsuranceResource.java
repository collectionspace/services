/*
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.insurance;

import org.collectionspace.services.client.InsuranceClient;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path(InsuranceClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class InsuranceResource extends NuxeoBasedResource {

    final Logger logger = LoggerFactory.getLogger(InsuranceResource.class);

    @Override
    protected String getVersionString() {
        final String lastChangeRevision = "$LastChangedRevision$";
        return lastChangeRevision;
    }

    @Override
    public String getServiceName() {
        return InsuranceClient.SERVICE_NAME;
    }

    @Override
    public Class<InsurancesCommon> getCommonPartClass() {
        return InsurancesCommon.class;
    }

}

