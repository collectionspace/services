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
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

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
        return RemoteServiceContextFactory.get();
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
            List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), groupname);
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
			HashMap<String,String> item = new HashMap<String,String>();
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

    
}
