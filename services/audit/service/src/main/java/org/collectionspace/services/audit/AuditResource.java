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
package org.collectionspace.services.audit;

import org.collectionspace.services.audit.nuxeo.AuditDocumentHandler;
import org.collectionspace.services.client.AuditClient;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.context.RemoteServiceContext;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.elasticsearch.ESDocumentFilter;
import org.collectionspace.services.common.storage.StorageClient;
import org.collectionspace.services.common.storage.elasticsearch.ESAuditStorageClientImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(AuditClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class AuditResource extends AbstractCollectionSpaceResourceImpl<AuditCommon, AuditCommon> {

	final Logger logger = LoggerFactory.getLogger(AuditResource.class);
	
    public static final String READ = "get";
    public static final String UPDATE = "update";
    public static final String LIST = "list";
    final StorageClient storageClient = new ESAuditStorageClientImpl();

    @Override
    public StorageClient getStorageClient(ServiceContext<AuditCommon, AuditCommon> ctx) {
        //FIXME use ctx to identify storage client
        return storageClient;
    }

	@Override
	protected String getVersionString() {
		final String lastChangeRevision = "$LastChangedRevision$";
		return lastChangeRevision;
	}

	@Override
	public String getServiceName() {
		return AuditClient.SERVICE_NAME;
	}

	@Override
	public Class<AuditCommon> getCommonPartClass() {
		return AuditCommon.class;
	}

	
	@Override
	public ServiceContextFactory<AuditCommon, AuditCommon> getServiceContextFactory() {
		// TODO Auto-generated method stub
		return RemoteServiceContextFactory.get();
	}
	
    public AuditCommonList getList(Request request, UriInfo uriInfo) {
        try {
            RemoteServiceContext<AuditCommon, AuditCommon> ctx = 
            		(RemoteServiceContext<AuditCommon, AuditCommon>) createServiceContext(request, uriInfo);
            AuditDocumentHandler handler = (AuditDocumentHandler) createDocumentHandler(ctx);
            MultivaluedMap<String, String> queryParams = (uriInfo != null ? uriInfo.getQueryParameters() : null);
            ESDocumentFilter myFilter = (ESDocumentFilter) handler.createDocumentFilter();
            myFilter.setPagination(queryParams);
            myFilter.setQueryParams(queryParams);
            handler.setDocumentFilter(myFilter);
            getStorageClient(ctx).getFiltered(ctx, handler);

            return handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
    }


    @GET
    public AuditCommonList getAuditCommonList(@Context Request request, @Context UriInfo ui) {
    	UriInfoWrapper uriInfoWrapper = new UriInfoWrapper(ui);
    	AuditCommonList result = (AuditCommonList)getList(request, uriInfoWrapper);
    	return result;
    }
    
	@GET
	@Path("{csid}")
	public AuditCommon getAuditCommon(@Context Request request, @Context ResourceMap resourceMap,
			@Context UriInfo uriInfo, @PathParam("csid") String csid) {
		uriInfo = new UriInfoWrapper(uriInfo);
		AuditCommon result = null;

		try {
			result = getResourceFromCsid(request, uriInfo, csid);
		} catch (Exception e) {
			throw bigReThrow(e, ServiceMessages.READ_FAILED, csid);
		}

		if (result == null) {
			Response response = Response.status(Response.Status.NOT_FOUND)
					.entity(ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(csid)).type("text/plain")
					.build();
			throw new CSWebApplicationException(response);
		}

		return result;
	}
	
    public AuditCommon getResourceFromCsid(
            Request request,
            UriInfo uriInfo,
            String csid) throws Exception {
    	AuditCommon result = null;
        
        ensureCSID(csid, READ);
        RemoteServiceContext<AuditCommon, AuditCommon> ctx = 
        		(RemoteServiceContext<AuditCommon, AuditCommon>) createServiceContext(request, uriInfo);
        
        result = get(csid, ctx); // ==> CALL an implementation method, which subclasses may override.

        return result;
    }

    protected AuditCommon get(String csid, ServiceContext<AuditCommon, AuditCommon> ctx) throws Exception {
    	AuditCommon result = null;

        ensureCSID(csid, READ);
        DocumentHandler handler = createDocumentHandler(ctx);
        getStorageClient(ctx).get(ctx, csid, handler);
        result = ctx.getOutput();
        
        if (result == null) {
            String msg = "Could not find document with id = " + csid;
            if (logger.isErrorEnabled() == true) {
                logger.error(msg);
            }
            throw new DocumentNotFoundException(msg);
        }

        return result;
    }

}
