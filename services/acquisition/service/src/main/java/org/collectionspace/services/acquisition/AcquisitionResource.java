/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Copyright © 2009 Regents of the University of California
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.acquisition;


import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.collectionspace.services.client.AcquisitionClient;
import org.collectionspace.services.common.ResourceBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcquisitionResource.java
 *
 * Handles requests to the Acquisition service, orchestrates the retrieval
 * of relevant resources, and returns responses to the client.
 */
@Path(AcquisitionClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class AcquisitionResource extends ResourceBase {

    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(AcquisitionResource.class);

    @Override
    protected String getVersionString() {
        final String lastChangeRevision = "$LastChangedRevision$";
        return lastChangeRevision;
    }

    @Override
    public String getServiceName() {
        return AcquisitionClient.SERVICE_NAME;
    }

    @Override
    public Class<AcquisitionsCommon> getCommonPartClass() {
        return AcquisitionsCommon.class;
    }

    /**
     * Instantiates a new acquisition resource.
     */
    public AcquisitionResource() {
        // Empty constructor.  Note that all JAX-RS resource classes are singletons.
    }


}
