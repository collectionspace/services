/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2012 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.collectionspace.services.account;

import org.collectionspace.services.account.storage.TenantStorageClient;
import org.collectionspace.services.client.TenantClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.common.SecurityResourceBase;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.jpa.JpaStorageUtils;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;


/** TenantResource provides RESTful interface to the tenant service 
 *
 *  The TenantResource is tied to the account package for historical
 *  reasons, and because it is toes so closely to the notion of accounts
 *  and IAM. 
 */
@Path(TenantClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class TenantResource extends SecurityResourceBase {

    final Logger logger = LoggerFactory.getLogger(TenantResource.class);
    final StorageClient storageClient = new TenantStorageClient();

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision: 1165 $";
    }

    @Override
    public String getServiceName() {
        return TenantClient.SERVICE_NAME;
    }

    @Override
    public Class<Tenant> getCommonPartClass() {
        return Tenant.class;
    }

    @Override
    public ServiceContextFactory<Tenant, Tenant> getServiceContextFactory() {
        return (ServiceContextFactory<Tenant, Tenant>) RemoteServiceContextFactory.get();
    }

    @Override
    public StorageClient getStorageClient(ServiceContext ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

    @POST
    public Response createTenant(Tenant input) {
        return create(input);
    }

    @GET
    @Path("{csid}")
    public Tenant getTenant(@PathParam("csid") String csid) {
        return (Tenant)get(csid, Tenant.class);
    }

    @GET
    @Produces("application/xml")
    public TenantsList getTenantList(@Context UriInfo ui) {
    	TenantsList result = (TenantsList)getList(ui, Tenant.class);
    	if(logger.isTraceEnabled()) {
        	PayloadOutputPart ppo = new PayloadOutputPart(TenantsList.class.getSimpleName(),
        			result);
    		System.out.println(ppo.asXML());
    	}
    	return result;
    }

    @PUT
    @Path("{csid}")
    public Tenant updateTenant(@PathParam("csid") String csid,Tenant theUpdate) {
        return (Tenant)update(csid, theUpdate, Tenant.class);
    }


    @DELETE
    @Path("{csid}")
    public Response deleteTenant(@Context UriInfo uriInfo, @PathParam("csid") String csid) {
        logger.debug("deleteTenant with csid=" + csid);
        ensureCSID(csid, ServiceMessages.DELETE_FAILED);
        try {
            ServiceContext<Tenant, Tenant> ctx = createServiceContext((Tenant) null,
                    Tenant.class, uriInfo);
            getStorageClient(ctx).delete(ctx, csid);
            return Response.status(HttpResponseCodes.SC_OK).build();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.DELETE_FAILED, csid);
        }

    }
}
