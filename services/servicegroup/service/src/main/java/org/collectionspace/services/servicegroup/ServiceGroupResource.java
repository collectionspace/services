/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Copyright 2009 University of California at Berkeley
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
package org.collectionspace.services.servicegroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.ParserConfigurationException;
import org.collectionspace.services.ServiceGroupListItemJAXBSchema;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.ServiceGroupClient;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.UriInfoWrapper;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.client.java.CommonList;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentFilter;
import org.collectionspace.services.servicegroup.nuxeo.ServiceGroupDocumentModelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path(ServiceGroupClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class ServiceGroupResource extends AbstractCollectionSpaceResourceImpl<PoxPayloadIn, PoxPayloadOut> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final boolean EXCLUDE_AUTHORITIES = false;
    private static final boolean INCLUDE_AUTHORITIES = true;

    @Override
    public String getServiceName() {
        return ServiceGroupClient.SERVICE_NAME;
    }

    public String getServicePathComponent() {
        return ServiceGroupClient.SERVICE_NAME.toLowerCase();
    }

    @Override
    protected String getVersionString() {
        final String lastChangeRevision = "$LastChangedRevision: 2108 $";
        return lastChangeRevision;
    }

    @Override
    public Class<?> getCommonPartClass() {
        try {
            return Class.forName("org.collectionspace.services.servicegroup.ServicegroupsCommon");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> getServiceContextFactory() {
        return MultipartServiceContextFactory.get();
    }

    // ======================= GET without specifier: List  =====================================
    @GET
    public AbstractCommonList getList(@Context UriInfo ui) {
        try {
            CommonList commonList = new CommonList();
            AbstractCommonList list = (AbstractCommonList) commonList;
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
            for (String groupName : svcGroups) {
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

    // ======================= GET ====================================================
    // NOTE that csid is not a good name for the specifier, but if we name it anything else our AuthZ gets confused!!!
    @GET
    @Path("{csid}")
    public byte[] get(@Context UriInfo ui, @PathParam("csid") String groupname) {
        PoxPayloadOut result = null;
        ensureCSID(groupname, NuxeoBasedResource.READ);
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
            ServiceGroupDocumentModelHandler handler = (ServiceGroupDocumentModelHandler) createDocumentHandler(ctx);
            TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
            // We need to get all the procedures, authorities, and objects.
            ArrayList<String> groupsList = null;
            if ("common".equalsIgnoreCase(groupname)) {
                groupsList = ServiceBindingUtils.getCommonServiceTypes(INCLUDE_AUTHORITIES);
            } else {
                groupsList = new ArrayList<String>();
                groupsList.add(groupname);
            }
            List<ServiceBindingType> bindings = tReader.getServiceBindingsByType(ctx.getTenantId(), groupsList);
            if (bindings == null || bindings.isEmpty()) {
                // 404 if there are no mappings.
                Response response = Response.status(Response.Status.NOT_FOUND)
                        .entity(ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(groupname))
                        .type("text/plain")
                        .build();
                throw new CSWebApplicationException(response);
            }
            // Otherwise, build the response with a list
            ServicegroupsCommon common = new ServicegroupsCommon();
            common.setName(groupname);
            String uri = "/" + getServicePathComponent() + "/" + groupname;
            common.setUri(uri);
            result = new PoxPayloadOut(getServicePathComponent());
            result.addPart("ServicegroupsCommon", common);

            String queryTag = ctx.getQueryParams().getFirst(IQueryManager.TAG_QUERY_PARAM);
            ServicegroupsCommon.HasDocTypes wrapper = common.getHasDocTypes();
            if (wrapper == null) {
                wrapper = new ServicegroupsCommon.HasDocTypes();
                common.setHasDocTypes(wrapper);
            }
            List<String> hasDocTypes = wrapper.getHasDocType();
            for (ServiceBindingType binding : bindings) {
                boolean includeDocType = handler.acceptServiceBinding(binding, queryTag);

                ServiceObjectType serviceObj = binding.getObject();
                if (includeDocType && serviceObj != null) {
                    String docType = serviceObj.getName();
                    hasDocTypes.add(docType);
                }
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, groupname);
        }

        return result.getBytes();
    }

    /**
     * Get a list of the existing service tags for a give service type
     * e.g. the procedure services might have nagpra and legacy
     *
     * @param ui the uri info
     * @param serviceType the service type to retrieve the set of tags for
     * @return the set of service tags, as an abstract-common-list
     */
    @GET
    @Path("{csid}/tags")
    public AbstractCommonList getTagsForType(@Context UriInfo ui, @PathParam("csid") String serviceType) {
        Set<String> serviceObjects = new HashSet<>();
        ensureCSID(serviceType, NuxeoBasedResource.READ);
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext();
            TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();

            List<ServiceBindingType> bindings = tReader.getServiceBindingsByType(ctx.getTenantId(), serviceType);
            if (bindings == null || bindings.isEmpty()) {
                // 404 if there are no mappings.
                Response response = Response.status(Response.Status.NOT_FOUND)
                        .entity(ServiceMessages.READ_FAILED + ServiceMessages.resourceNotFoundMsg(serviceType))
                        .type("text/plain")
                        .build();
                throw new CSWebApplicationException(response);
            }

            for (ServiceBindingType binding : bindings) {
                ServiceObjectType serviceObj = binding.getObject();
                if (serviceObj != null) {
                    if (binding.getTags() != null) {
                        serviceObjects.addAll(binding.getTags().getTag());
                    }
                }
            }
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, serviceType);
        }

        try {
            CommonList commonList = new CommonList();
            commonList.setPageNum(0);
            commonList.setPageSize(serviceObjects.size());
            commonList.setItemsInPage(serviceObjects.size());
            commonList.setTotalItems(serviceObjects.size());

            String[] fields = new String[1];
            fields[0] = ServiceGroupListItemJAXBSchema.NAME;
            commonList.setFieldsReturned(fields);
            HashMap<String, Object> item = new HashMap<String, Object>();
            for (String service : serviceObjects) {
                item.put(ServiceGroupListItemJAXBSchema.NAME, service);
                commonList.addItem(item);
                item.clear();
            }
            return commonList;
        } catch (ParserConfigurationException e) {
            throw bigReThrow(e, ServiceMessages.UNKNOWN_ERROR_MSG, serviceType);
        }
    }

    @GET
    @Path("{csid}/items")
    public AbstractCommonList getResourceItemList(
            @Context UriInfo uriInfo, @PathParam("csid") String serviceGroupName) {
        UriInfoWrapper ui = new UriInfoWrapper(uriInfo);
        ensureCSID(serviceGroupName, NuxeoBasedResource.READ);
        AbstractCommonList list = null;
        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
            ServiceGroupDocumentModelHandler handler = (ServiceGroupDocumentModelHandler) createDocumentHandler(ctx);
            ArrayList<String> groupsList = null;
            if ("common".equalsIgnoreCase(serviceGroupName)) {
                groupsList = ServiceBindingUtils.getCommonServiceTypes(INCLUDE_AUTHORITIES);
            } else {
                groupsList = new ArrayList<String>();
                groupsList.add(serviceGroupName);
            }

            // check first for a csid query parameter
            MultivaluedMap<String, String> queryParams = ctx.getQueryParams();
            String csid = queryParams.getFirst(IQueryManager.CSID_QUERY_PARAM);
            if (csid != null && !csid.isEmpty()) {
                String whereClause = QueryManager.createWhereClauseFromCsid(csid);
                if (Tools.isEmpty(whereClause)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The WHERE clause is empty for csid: [" + csid + "]");
                    }
                } else {
                    DocumentFilter documentFilter = handler.getDocumentFilter();
                    documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
                    if (logger.isDebugEnabled()) {
                        logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
                    }
                }
            } else {
                // check to see if we have to set up a keyword search
                String keywords = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_KW);
                if (keywords != null && !keywords.isEmpty()) {
                    String whereClause = QueryManager.createWhereClauseFromKeywords(keywords);
                    if (Tools.isEmpty(whereClause)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("The WHERE clause is empty for keywords: [" + keywords + "]");
                        }
                    } else {
                        DocumentFilter documentFilter = handler.getDocumentFilter();
                        documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
                        if (logger.isDebugEnabled()) {
                            logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
                        }
                    }
                }
            }

            String advancedSearch = queryParams.getFirst(IQueryManager.SEARCH_TYPE_KEYWORDS_AS);
            if (advancedSearch != null && !advancedSearch.isEmpty()) {
                DocumentFilter documentFilter = handler.getDocumentFilter();
                String whereClause = QueryManager.createWhereClauseFromAdvancedSearch(advancedSearch);
                documentFilter.appendWhereClause(whereClause, IQueryManager.SEARCH_QUALIFIER_AND);
                if (logger.isDebugEnabled()) {
                    logger.debug("The WHERE clause is: " + documentFilter.getWhereClause());
                }
            }

            // make the query
            list = handler.getItemListForGroup(ctx, groupsList);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, serviceGroupName);
        }

        return list;
    }

    @GET
    @Path("{csid}/items/{specifier}")
    public byte[] getResourceItem(
            @Context ResourceMap resourceMap,
            @Context UriInfo uriInfo,
            @PathParam("csid") String serviceGroupName,
            @PathParam("specifier") String specifier) {
        UriInfoWrapper ui = new UriInfoWrapper(uriInfo);
        ensureCSID(serviceGroupName, NuxeoBasedResource.READ);
        PoxPayloadOut result = null;

        try {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = createServiceContext(ui);
            ServiceGroupDocumentModelHandler handler = (ServiceGroupDocumentModelHandler) createDocumentHandler(ctx);
            ArrayList<String> groupsList = null;
            if ("common".equalsIgnoreCase(serviceGroupName)) {
                groupsList = ServiceBindingUtils.getCommonServiceTypes(INCLUDE_AUTHORITIES);
            } else {
                groupsList = new ArrayList<String>();
                groupsList.add(serviceGroupName);
            }

            String whereClause = QueryManager.createWhereClauseFromCsid(specifier);
            DocumentFilter myFilter = new NuxeoDocumentFilter(whereClause, 0, 1);
            handler.setDocumentFilter(myFilter);

            result = handler.getResourceItemForCsid(ctx, groupsList, specifier);
        } catch (Exception e) {
            throw bigReThrow(e, ServiceMessages.READ_FAILED, serviceGroupName);
        }

        return result.getBytes();
    }
}
