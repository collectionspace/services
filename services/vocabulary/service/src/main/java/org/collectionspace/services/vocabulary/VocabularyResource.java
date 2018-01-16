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
package org.collectionspace.services.vocabulary;

import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.VocabularyClient;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.vocabulary.nuxeo.VocabularyItemDocumentModelHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/" + VocabularyClient.SERVICE_PATH_COMPONENT)
public class VocabularyResource extends 
	AuthorityResource<VocabulariesCommon, VocabularyItemDocumentModelHandler> {

    private final static String vocabularyServiceName = VocabularyClient.SERVICE_PATH_COMPONENT;

	private final static String VOCABULARIES_COMMON = "vocabularies_common";
    
    private final static String vocabularyItemServiceName = "vocabularyitems";
	private final static String VOCABULARYITEMS_COMMON = "vocabularyitems_common";
    
    final Logger logger = LoggerFactory.getLogger(VocabularyResource.class);

	public VocabularyResource() {
		super(VocabulariesCommon.class, VocabularyResource.class,
				VOCABULARIES_COMMON, VOCABULARYITEMS_COMMON);
	}

    @GET
    @Path("{csid}")
    @Override
    public Response get(
            @Context Request request,
            @Context UriInfo uriInfo,
            @PathParam("csid") String specifier) {
    	Response result = null;
    	uriInfo = new UriInfoWrapper(uriInfo);
        
        try {
        	MultivaluedMap<String,String> queryParams = uriInfo.getQueryParameters();
        	String showItemsValue = (String)queryParams.getFirst(VocabularyClient.SHOW_ITEMS_QP);
            boolean showItems = Tools.isTrue(showItemsValue);
            if (showItems == true) {
            	//
            	// We'll honor paging params if we find any; otherwise we'll set the page size to 0 to get ALL the items
            	//
            	if (queryParams.containsKey(IClientQueryParams.PAGE_SIZE_PARAM) == false) {
            		queryParams.add(IClientQueryParams.PAGE_SIZE_PARAM, "0");
            	}
            }

            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(request, uriInfo);
            PoxPayloadOut payloadout = getAuthority(ctx, request, uriInfo, specifier, showItems);
            result = buildResponse(ctx, payloadout);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.GET_FAILED, specifier);
        }

        if (result == null) {
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "GET request failed. The requested Authority specifier:" + specifier + ": was not found.").type(
                    "text/plain").build();
            throw new CSWebApplicationException(response);
        }

        return result;
    }
    
    @Override
    public String getServiceName() {
        return vocabularyServiceName;
    }

    @Override
    public String getItemServiceName() {
        return vocabularyItemServiceName;
    }
    
	@Override
	public Class<VocabulariesCommon> getCommonPartClass() {
		return VocabulariesCommon.class;
	}

    /**
     * @return the name of the property used to specify references for items in this type of
     * authority. For most authorities, it is ServiceBindingUtils.AUTH_REF_PROP ("authRef").
     * Some types (like Vocabulary) use a separate property.
     */
	@Override
    protected String getRefPropName() {
    	return ServiceBindingUtils.TERM_REF_PROP;
    }
	
	@Override
	protected String getOrderByField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
		String result = null;

		result = authorityItemCommonSchemaName + ":" + VocabularyItemJAXBSchema.DISPLAY_NAME;

		return result;
	}
	
	@Override
	protected String getPartialTermMatchField(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
		return getOrderByField(ctx);
	}

	/*
	 * The item schema for the Vocabulary service does not support a multi-valued term list.  Only authorities that support
	 * term lists need to implement this method.
	 */
	@Override
	public String getItemTermInfoGroupXPathBase() {
		return null;
	}
}
