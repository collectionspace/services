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
package org.collectionspace.services.batch;

import java.util.List;

import org.collectionspace.services.BatchJAXBSchema;
import org.collectionspace.services.jaxb.InvocableJAXBSchema;
import org.collectionspace.services.batch.nuxeo.BatchDocumentModelHandler;
import org.collectionspace.services.client.BatchClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.ValidatorHandler;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.Invocable.InvocationError;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.query.QueryManager;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.collectionspace.services.common.ResourceMapHolder;
import org.collectionspace.services.jaxb.AbstractCommonList;

import javax.management.BadAttributeValueExpException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(BatchClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class BatchResource extends ResourceBase {
	
	protected final String COMMON_SCHEMA = "batch_common";

    @Override
    public String getServiceName(){
        return BatchClient.SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    //public Class<BatchCommon> getCommonPartClass() {
    public Class getCommonPartClass() {
    	try {
            return Class.forName("org.collectionspace.services.batch.BatchCommon");//.class;
        } catch (ClassNotFoundException e){
            return null;
        }
    }
    
	/**
	 * Gets the authorityItem list for the specified authority
	 * If partialPerm is specified, keywords will be ignored.
	 * 
	 * @param specifier either a CSID or one of the urn forms
	 * @param partialTerm if non-null, matches partial terms
	 * @param keywords if non-null, matches terms in the keyword index for items
	 * @param ui passed to include additional parameters, like pagination controls
	 * 
	 * @return the authorityItem list
	 */
	@GET
	@Produces("application/xml")
	public AbstractCommonList getBatchList(
			@QueryParam(IQueryManager.SEARCH_TYPE_DOCTYPE) String docType,
			@QueryParam(IQueryManager.SEARCH_TYPE_INVOCATION) String mode,
			@Context UriInfo ui) {
        AbstractCommonList list;
        if (docType != null && !docType.isEmpty() && mode != null && !mode.isEmpty()) {
            list = batchSearch(ui, docType, mode);
        } else {
            list = getList(ui);
        }
        return list;
	}

    private AbstractCommonList batchSearch(UriInfo ui, 
    										String docType, String mode) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
            DocumentHandler handler = createDocumentHandler(ctx);
            // perform a search by docType and invocation mode
            DocumentFilter documentFilter = handler.getDocumentFilter();
            if (docType != null && !docType.isEmpty()) {
                String whereClause = createWhereClauseForDocType(docType);
                documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            if (mode != null && !mode.isEmpty()) {
                String whereClause = createWhereClauseForMode(mode);
                documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
            }
            getRepositoryClient(ctx).getFiltered(ctx, handler);
            return (AbstractCommonList) handler.getCommonPartList();
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.SEARCH_FAILED);
        }
    }
    
	private String createWhereClauseForDocType(String docType) {
		String trimmed = (docType == null)?"":docType.trim(); 
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No docType specified.");
		}
		String ptClause = COMMON_SCHEMA + ":"
		+ BatchJAXBSchema.FOR_DOC_TYPES
			+ "='" + trimmed + "'";
		return ptClause;
	}

	private String createWhereClauseForMode(String mode) throws BadRequestException {
		String trimmed = (mode == null)?"":mode.trim(); 
		if (trimmed.isEmpty()) {
			throw new RuntimeException("No mode specified.");
		}
		String ptClause = COMMON_SCHEMA + ":";
		if(Invocable.INVOCATION_MODE_SINGLE.equalsIgnoreCase(trimmed)) {
			ptClause += BatchJAXBSchema.SUPPORTS_SINGLE_DOC + "!=0";
		} else if(Invocable.INVOCATION_MODE_LIST.equalsIgnoreCase(trimmed)) {
			ptClause += BatchJAXBSchema.SUPPORTS_DOC_LIST + "!=0";
		} else if(Invocable.INVOCATION_MODE_GROUP.equalsIgnoreCase(trimmed)) {
			ptClause += BatchJAXBSchema.SUPPORTS_GROUP + "!=0";
		} else {
			throw new BadRequestException("No mode specified.");
		}
		return ptClause;
	}



    
    @POST
    @Path("{csid}")
    public InvocationResults invokeBatchJob(
    		@Context ResourceMap resourceMap, 
    		@PathParam("csid") String csid,
    		InvocationContext invContext) {
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            BatchDocumentModelHandler handler = (BatchDocumentModelHandler)createDocumentHandler(ctx);
            
            return handler.invokeBatchJob(ctx, csid, resourceMap, invContext);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.POST_FAILED);
        }
    }
}
