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
package org.collectionspace.services.servicegroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.collectionspace.services.ServiceGroupListItemJAXBSchema;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ServiceGroupClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.collectionspace.services.servicegroup.nuxeo.ServiceGroupDocumentModelHandler;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(ServiceGroupClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class ServiceGroupResource extends AbstractCollectionSpaceResourceImpl {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final static boolean EXCLUDE_AUTHORITIES = false;
    private final static boolean INCLUDE_AUTHORITIES = true;
    
    @Override
    public String getServiceName(){
        return ServiceGroupClient.SERVICE_NAME;
    }

    public String getServicePathComponent(){
        return ServiceGroupClient.SERVICE_NAME.toLowerCase();
    }

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    //public Class<ServicegroupsCommon> getCommonPartClass() {
    public Class getCommonPartClass() {
    	try {
            return Class.forName("org.collectionspace.services.servicegroup.ServicegroupsCommon");//.class;
        } catch (ClassNotFoundException e){
            return null;
        }
    }

    @Override
    public ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> getServiceContextFactory() {
        return MultipartServiceContextFactory.get();
    }


    //======================= GET without specifier: List  =====================================
    @GET
    public AbstractCommonList getList(@Context UriInfo ui) {
        try {
            CommonList commonList = new CommonList();
            AbstractCommonList list = (AbstractCommonList)commonList;
	        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
	    	String commonSchema = ctx.getCommonPartLabel();
	    	ArrayList<String> svcGroups = new ArrayList<String>();
	    	svcGroups.add("procedure");
	    	svcGroups.add("object");
	    	svcGroups.add("authority");
	    	// Fetch the list of groups from the tenant-bindings config, and prepare a list item
	    	// for each one.
	        // We always declare this a full list, of the size that we are returning. 
	    	// Not quite in the spirit of what paging means, but tells callers not to ask for more.
	    	list.setPageNum(0);
	    	list.setPageSize(svcGroups.size());
	        list.setItemsInPage(svcGroups.size());
	        list.setTotalItems(svcGroups.size());
	        String fields[] = new String[2];
	        fields[0] = ServiceGroupListItemJAXBSchema.NAME;
	        fields[1] = ServiceGroupListItemJAXBSchema.URI;
	        commonList.setFieldsReturned(fields);
			HashMap<String, Object> item = new HashMap<String, Object>();
	        for(String groupName:svcGroups){
	            item.put(ServiceGroupListItemJAXBSchema.NAME, groupName);
	            String uri = "/" + getServiceName().toLowerCase() + "/" + groupName;
	            item.put(ServiceGroupListItemJAXBSchema.URI, uri);
	            commonList.addItem(item);
	            item.clear();
	        }
	        return list;
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.LIST_FAILED);
        }
        
    }

    
    //======================= GET ====================================================
    // NOTE that csid is not a good name for the specifier, but if we name it anything else, 
    // our AuthZ gets confused!!!
    @GET
    @Path("{csid}")
    public byte[] get(
            @Context UriInfo ui,
            @PathParam("csid") String groupname) {
        PoxPayloadOut result = null;
        ensureCSID(groupname, ResourceBase.READ);
        try {
	        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            TenantBindingConfigReaderImpl tReader =
                    ServiceMain.getInstance().getTenantBindingConfigReader();
            // We need to get all the procedures, authorities, and objects.
	        ArrayList<String> groupsList = null;  
	        if("common".equalsIgnoreCase(groupname)) {
	        	groupsList = ServiceBindingUtils.getCommonServiceTypes(EXCLUDE_AUTHORITIES); // CSPACE-5359: Excluding Authority type to stay backward compat with v2.4
	        } else {
	        	groupsList = new ArrayList<String>();
	        	groupsList.add(groupname);
	        }
            List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), groupsList);
            if (servicebindings == null || servicebindings.isEmpty()) {
            	// 404 if there are no mappings.
                Response response = Response.status(Response.Status.NOT_FOUND).entity(
                        ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(groupname)).type("text/plain").build();
                throw new WebApplicationException(response);
            }
        	//Otherwise, build the response with a list
            ServicegroupsCommon common = new ServicegroupsCommon();
            common.setName(groupname);
            String uri = "/" + getServicePathComponent() + "/" + groupname;
            common.setUri(uri);
            result = new PoxPayloadOut(getServicePathComponent());
            result.addPart("ServicegroupsCommon", common);
            
        	ServicegroupsCommon.HasDocTypes wrapper = common.getHasDocTypes();
        	if(wrapper==null) {
        		wrapper = new ServicegroupsCommon.HasDocTypes();
        		common.setHasDocTypes(wrapper);
        	}
        	List<String> hasDocTypes = wrapper.getHasDocType();
        	for(ServiceBindingType binding:servicebindings) {
        		ServiceObjectType serviceObj = binding.getObject();
        		if(serviceObj!=null) {
	                String docType = serviceObj.getName();
	                hasDocTypes.add(docType);
        		}
        	}
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, groupname);
        }

        return result.getBytes();
    }


    @GET
    @Path("{csid}/items")
    public AbstractCommonList getItems(
            @Context UriInfo ui,
            @PathParam("csid") String serviceGroupName) {
        ensureCSID(serviceGroupName, ResourceBase.READ);
        AbstractCommonList list = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
	        ServiceGroupDocumentModelHandler handler = (ServiceGroupDocumentModelHandler)
	        				createDocumentHandler(ctx);
	        ArrayList<String> groupsList = null;  
	        if("common".equalsIgnoreCase(serviceGroupName)) {
	        	groupsList = ServiceBindingUtils.getCommonServiceTypes(EXCLUDE_AUTHORITIES); //CSPACE-5359: Exclude authorities to remain backward compat with v2.4
	        } else {
	        	groupsList = new ArrayList<String>();
	        	groupsList.add(serviceGroupName);
	        }
	        // set up a keyword search
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
            String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
	        if (keywords != null && !keywords.isEmpty()) {
	            String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
	            if(Tools.isEmpty(whereClause)) {
	                if (logger.isDebugEnabled()) {
	                	logger.debug("The WHERE clause is empty for keywords: ["+keywords+"]");
	                }
	            } else {
		            DocumentFilter documentFilter = handler.getDocumentFilter();
		            documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
		            if (logger.isDebugEnabled()) {
		                logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
		            }
	            }
	        }
            list = handler.getItemsForGroup(ctx, groupsList);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, serviceGroupName);
        }

        return list;
    }


}
