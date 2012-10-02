/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009 University of California at Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.common.vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.api.model.impl.primitives.StringProperty;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.StoredValuesUriTemplate;
import org.collectionspace.services.common.UriTemplateFactory;
import org.collectionspace.services.common.UriTemplateRegistry;
import org.collectionspace.services.common.UriTemplateRegistryKey;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.AbstractServiceContextImpl;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.QueryManager;
import org.collectionspace.services.common.relation.RelationUtils;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.nuxeo.client.java.DocHandlerBase;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

/**
 * RefNameServiceUtils is a collection of services utilities related to refName
 * usage.
 *
 * $LastChangedRevision: $ $LastChangedDate: $
 */
public class RefNameServiceUtils {

    public static class AuthRefConfigInfo {

        public String getQualifiedDisplayName() {
            return (Tools.isBlank(schema))
                    ? displayName : DocumentUtils.appendSchemaName(schema, displayName);
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        String displayName;
        String schema;

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getFullPath() {
            return fullPath;
        }

        public void setFullPath(String fullPath) {
            this.fullPath = fullPath;
        }
        String fullPath;
        protected String[] pathEls;

        public AuthRefConfigInfo(AuthRefConfigInfo arci) {
            this.displayName = arci.displayName;
            this.schema = arci.schema;
            this.fullPath = arci.fullPath;
            this.pathEls = arci.pathEls;
            // Skip the pathElse check, since we are creatign from another (presumably valid) arci.
        }

        public AuthRefConfigInfo(String displayName, String schema, String fullPath, String[] pathEls) {
            this.displayName = displayName;
            this.schema = schema;
            this.fullPath = fullPath;
            this.pathEls = pathEls;
            checkPathEls();
        }

        // Split a config value string like "intakes_common:collector", or
        // "collectionobjects_common:contentPeoples|contentPeople"
        // "collectionobjects_common:assocEventGroupList/*/assocEventPlace"
        // If has a pipe ('|') second part is a displayLabel, and first is path
        // Otherwise, entry is a path, and can use the last pathElement as displayName
        // Should be schema qualified.
        public AuthRefConfigInfo(String configString) {
            String[] pair = configString.split("\\|", 2);
            String[] pathEls;
            String displayName, fullPath;
            if (pair.length == 1) {
                // no label specifier, so we'll defer getting label
                fullPath = pair[0];
                pathEls = pair[0].split("/");
                displayName = pathEls[pathEls.length - 1];
            } else {
                fullPath = pair[0];
                pathEls = pair[0].split("/");
                displayName = pair[1];
            }
            String[] schemaSplit = pathEls[0].split(":", 2);
            String schema;
            if (schemaSplit.length == 1) {    // schema not specified
                schema = null;
            } else {
                schema = schemaSplit[0];
                if (pair.length == 1 && pathEls.length == 1) {    // simplest case of field in top level schema, no labelll
                    displayName = schemaSplit[1];    // Have to fix up displayName to have no schema
                }
            }
            this.displayName = displayName;
            this.schema = schema;
            this.fullPath = fullPath;
            this.pathEls = pathEls;
            checkPathEls();
        }

        protected void checkPathEls() {
            int len = pathEls.length;
            if (len < 1) {
                throw new InternalError("Bad values in authRef info - caller screwed up:" + fullPath);
            }
            // Handle case of them putting a leading slash on the path
            if (len > 1 && pathEls[0].endsWith(":")) {
                len--;
                String[] newArray = new String[len];
                newArray[0] = pathEls[0] + pathEls[1];
                if (len >= 2) {
                    System.arraycopy(pathEls, 2, newArray, 1, len - 1);
                }
                pathEls = newArray;
            }
        }
    }

    public static class AuthRefInfo extends AuthRefConfigInfo {

        public Property getProperty() {
            return property;
        }

        public void setProperty(Property property) {
            this.property = property;
        }
        Property property;

        public AuthRefInfo(String displayName, String schema, String fullPath, String[] pathEls, Property prop) {
            super(displayName, schema, fullPath, pathEls);
            this.property = prop;
        }

        public AuthRefInfo(AuthRefConfigInfo arci, Property prop) {
            super(arci);
            this.property = prop;
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(RefNameServiceUtils.class);
    private static ArrayList<String> refNameServiceTypes = null;

    public static void updateRefNamesInRelations(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            RepositoryInstance repoSession,
            String oldRefName,
            String newRefName) {
    	//
    	// First, look for and update all the places where the refName is the "subject" of the relationship
    	//
    	RelationUtils.updateRefNamesInRelations(ctx, repoClient, repoSession, IRelationsManager.SUBJECT_REFNAME, oldRefName, newRefName);
    	
    	//
    	// Next, look for and update all the places where the refName is the "object" of the relationship
    	//
    	RelationUtils.updateRefNamesInRelations(ctx, repoClient, repoSession, IRelationsManager.OBJECT_REFNAME, oldRefName, newRefName);
    }
    
    public static List<AuthRefConfigInfo> getConfiguredAuthorityRefs(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
        List<String> authRefFields =
                ((AbstractServiceContextImpl) ctx).getAllPartsPropertyValues(
                ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
        ArrayList<AuthRefConfigInfo> authRefsInfo = new ArrayList<AuthRefConfigInfo>(authRefFields.size());
        for (String spec : authRefFields) {
            AuthRefConfigInfo arci = new AuthRefConfigInfo(spec);
            authRefsInfo.add(arci);
        }
        return authRefsInfo;
    }

    public static AuthorityRefDocList getAuthorityRefDocs(
            RepositoryInstance repoSession,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            UriTemplateRegistry uriTemplateRegistry,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            List<String> serviceTypes,
            String refName,
            String refPropName, // authRef or termRef, authorities or vocab terms.
            DocumentFilter filter, boolean computeTotal)
            throws DocumentException, DocumentNotFoundException {
        AuthorityRefDocList wrapperList = new AuthorityRefDocList();
        AbstractCommonList commonList = (AbstractCommonList) wrapperList;
        int pageNum = filter.getStartPage();
        int pageSize = filter.getPageSize();
        
        List<AuthorityRefDocList.AuthorityRefDocItem> list =
                wrapperList.getAuthorityRefDocItem();

        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        Map<String, List<AuthRefConfigInfo>> authRefFieldsByService = new HashMap<String, List<AuthRefConfigInfo>>();

        RepositoryJavaClientImpl nuxeoRepoClient = (RepositoryJavaClientImpl) repoClient;
        try {
            // Ignore any provided page size and number query parameters in
            // the following call, as they pertain to the list of authority
            // references to be returned, not to the list of documents to be
            // scanned for those references.
            DocumentModelList docList = findAuthorityRefDocs(ctx, repoClient, repoSession,
                    serviceTypes, refName, refPropName, queriedServiceBindings, authRefFieldsByService,
                    filter.getWhereClause(), null, 0 /* pageSize */, 0 /* pageNum */, computeTotal);

            if (docList == null) { // found no authRef fields - nothing to process
                return wrapperList;
            }

            // set the fieldsReturned list. Even though this is a fixed schema, app layer treats
            // this like other abstract common lists
            /*
             * <xs:element name="docType" type="xs:string" minOccurs="1" />
             * <xs:element name="docId" type="xs:string" minOccurs="1" />
             * <xs:element name="docNumber" type="xs:string" minOccurs="0" />
             * <xs:element name="docName" type="xs:string" minOccurs="0" />
             * <xs:element name="sourceField" type="xs:string" minOccurs="1" />
             * <xs:element name="uri" type="xs:anyURI" minOccurs="1" />
             * <xs:element name="updatedAt" type="xs:string" minOccurs="1" />
             * <xs:element name="workflowState" type="xs:string" minOccurs="1"
             * />
             */
            String fieldList = "docType|docId|docNumber|docName|sourceField|uri|updatedAt|workflowState";
            commonList.setFieldsReturned(fieldList);

            // As a side-effect, the method called below modifies the value of
            // the 'list' variable, which holds the list of references to
            // an authority item.
            //
            // There can be more than one reference to a particular authority
            // item within any individual document scanned, so the number of
            // authority references may potentially exceed the total number
            // of documents scanned.

            // Strip off displayName and only match the base, so we get references to all 
            // the NPTs as well as the PT.
    		String strippedRefName = RefNameUtils.stripAuthorityTermDisplayName(refName);
            int nRefsFound = processRefObjsDocList(docList, ctx.getTenantId(), strippedRefName, true, queriedServiceBindings, authRefFieldsByService, // the actual list size needs to be updated to the size of "list"
                    list, null);

            commonList.setPageSize(pageSize);
            
            // Values returned in the pagination block above the list items
            // need to reflect the number of references to authority items
            // returned, rather than the number of documents originally scanned
            // to find such references.
            commonList.setPageNum(pageNum);
            commonList.setTotalItems(list.size());

            // Slice the list to return only the specified page of items
            // in the list results.
            //
            // FIXME: There may well be a pattern-based way to do this
            // in our framework, and if we can eliminate much of the
            // non-DRY code below, that would be desirable.
            
            int startIndex = 0;
            int endIndex = 0;
            
            // Return all results if pageSize is 0.
            if (pageSize == 0) {
                startIndex = 0;
                endIndex = list.size();
            } else {
               startIndex = pageNum * pageSize;
            }
            
            // Return an empty list when the start of the requested page is
            // beyond the last item in the list.
            if (startIndex > list.size()) {
                wrapperList.getAuthorityRefDocItem().clear();
                commonList.setItemsInPage(wrapperList.getAuthorityRefDocItem().size());
                return wrapperList;
            }

            // Otherwise, return a list of items from the start of the specified
            // page through the last item on that page, or otherwise through the
            // last item in the entire list, if that occurs earlier than the end
            // of the specified page.
            if (endIndex == 0) {
                int pageEndIndex = ((startIndex + pageSize));
                endIndex = (pageEndIndex > list.size()) ? list.size() : pageEndIndex;
            }
            
            // Slice the list to return only the specified page of results.
            // Note: the second argument to List.subList(), endIndex, is
            // exclusive of the item at its index position, reflecting the
            // zero-index nature of the list.
            List<AuthorityRefDocList.AuthorityRefDocItem> currentPageList =
                    new ArrayList<AuthorityRefDocList.AuthorityRefDocItem>(list.subList(startIndex, endIndex));
            wrapperList.getAuthorityRefDocItem().clear();
            wrapperList.getAuthorityRefDocItem().addAll(currentPageList);
            commonList.setItemsInPage(currentPageList.size());
            
            if (logger.isDebugEnabled() && (nRefsFound < docList.size())) {
                logger.debug("Internal curiosity: got fewer matches of refs than # docs matched..."); // We found a ref to ourself and have excluded it.
            }
        } catch (Exception e) {
            logger.error("Could not retrieve a list of documents referring to the specified authority item", e);
            wrapperList = null;
        }

        return wrapperList;
    }

    private static ArrayList<String> getRefNameServiceTypes() {
        if (refNameServiceTypes == null) {
            refNameServiceTypes = new ArrayList<String>();
            refNameServiceTypes.add(ServiceBindingUtils.SERVICE_TYPE_AUTHORITY);
            refNameServiceTypes.add(ServiceBindingUtils.SERVICE_TYPE_OBJECT);
            refNameServiceTypes.add(ServiceBindingUtils.SERVICE_TYPE_PROCEDURE);
        }
        return refNameServiceTypes;
    }
    
    // Seems like a good value - no real data to set this well.
    // Note: can set this value lower during debugging; e.g. to 3 - ADR 2012-07-10
    private static final int N_OBJS_TO_UPDATE_PER_LOOP = 100;

    public static int updateAuthorityRefDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            RepositoryInstance repoSession,
            String oldRefName,
            String newRefName,
            String refPropName) throws Exception {
        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        Map<String, List<AuthRefConfigInfo>> authRefFieldsByService = new HashMap<String, List<AuthRefConfigInfo>>();

        int docsScanned = 0;
        int nRefsFound = 0;
        int currentPage = 0;
        int docsInCurrentPage = 0;
        final String WHERE_CLAUSE_ADDITIONS_VALUE = null;
        final String ORDER_BY_VALUE = CollectionSpaceClient.CORE_CREATED_AT; // "collectionspace_core:createdAt";

        if (repoClient instanceof RepositoryJavaClientImpl == false) {
            throw new InternalError("updateAuthorityRefDocs() called with unknown repoClient type!");
        }
        
        try { // REM - How can we deal with transaction and timeout issues here?
            final int pageSize = N_OBJS_TO_UPDATE_PER_LOOP;
            DocumentModelList docList;
            boolean morePages = true;
            while (morePages) {

                docList = findAuthorityRefDocs(ctx, repoClient, repoSession,
                        getRefNameServiceTypes(), oldRefName, refPropName,
                        queriedServiceBindings, authRefFieldsByService, WHERE_CLAUSE_ADDITIONS_VALUE, ORDER_BY_VALUE, pageSize, currentPage, false);

                if (docList == null) {
                    logger.debug("updateAuthorityRefDocs: no documents could be found that referenced the old refName");
                    break;
                }
                docsInCurrentPage = docList.size();
                logger.debug("updateAuthorityRefDocs: current page=" + currentPage + " documents included in page=" + docsInCurrentPage);
                if (docsInCurrentPage == 0) {
                    logger.debug("updateAuthorityRefDocs: no more documents requiring refName updates could be found");
                    break;
                }
                if (docsInCurrentPage < pageSize) {
                    logger.debug("updateAuthorityRefDocs: assuming no more documents requiring refName updates will be found, as docsInCurrentPage < pageSize");
                    morePages = false;
                }

                // Only match complete refNames - unless and until we decide how to resolve changes
                // to NPTs we will defer that and only change PTs or refNames as passed in.
                int nRefsFoundThisPage = processRefObjsDocList(docList, ctx.getTenantId(), oldRefName, false, queriedServiceBindings, authRefFieldsByService, // Perform the refName updates on the list of document models
                        null, newRefName);
                if (nRefsFoundThisPage > 0) {
                    ((RepositoryJavaClientImpl) repoClient).saveDocListWithoutHandlerProcessing(ctx, repoSession, docList, true); // Flush the document model list out to Nuxeo storage
                    nRefsFound += nRefsFoundThisPage;
                }

                // FIXME: Per REM, set a limit of num objects - something like
                // 1000K objects - and also add a log Warning after some threshold
                docsScanned += docsInCurrentPage;
                if (morePages) {
                    currentPage++;
                }

            }
        } catch (Exception e) {
            logger.error("Internal error updating the AuthorityRefDocs: " + e.getLocalizedMessage());
            logger.debug(Tools.errorToString(e, true));
            throw e;
        }
        logger.debug("updateAuthorityRefDocs replaced a total of " + nRefsFound + " authority references, within as many as " + docsScanned + " scanned document(s)");
        return nRefsFound;
    }

    private static DocumentModelList findAuthorityRefDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            RepositoryInstance repoSession, List<String> serviceTypes,
            String refName,
            String refPropName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            String whereClauseAdditions,
            String orderByClause,
            int pageSize,
            int pageNum,
            boolean computeTotal) throws DocumentException, DocumentNotFoundException {

        // Get the service bindings for this tenant
        TenantBindingConfigReaderImpl tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        // We need to get all the procedures, authorities, and objects.
        List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), serviceTypes);
        if (servicebindings == null || servicebindings.isEmpty()) {
            logger.error("RefNameServiceUtils.getAuthorityRefDocs: No services bindings found, cannot proceed!");
            return null;
        }
        // Filter the list for current user rights
        servicebindings = SecurityUtils.getReadableServiceBindingsForCurrentUser(servicebindings);

        ArrayList<String> docTypes = new ArrayList<String>();

        String query = computeWhereClauseForAuthorityRefDocs(refName, refPropName, docTypes, servicebindings, // REM - Side effect that docTypes array gets set.  Any others?
                queriedServiceBindings, authRefFieldsByService);
        if (query == null) { // found no authRef fields - nothing to query
            return null;
        }
        // Additional qualifications, like workflow state
        if (Tools.notBlank(whereClauseAdditions)) {
            query += " AND " + whereClauseAdditions;
        }
        // Now we have to issue the search
        RepositoryJavaClientImpl nuxeoRepoClient = (RepositoryJavaClientImpl) repoClient;
        DocumentWrapper<DocumentModelList> docListWrapper = nuxeoRepoClient.findDocs(ctx, repoSession,
                docTypes, query, orderByClause, pageSize, pageNum, computeTotal);
        // Now we gather the info for each document into the list and return
        DocumentModelList docList = docListWrapper.getWrappedObject();
        return docList;
    }
    private static final boolean READY_FOR_COMPLEX_QUERY = true;

    private static String computeWhereClauseForAuthorityRefDocs(
            String refName,
            String refPropName,
            ArrayList<String> docTypes,
            List<ServiceBindingType> servicebindings,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService) {

        boolean fFirst = true;
        List<String> authRefFieldPaths;
        for (ServiceBindingType sb : servicebindings) {
            // Gets the property names for each part, qualified with the part label (which
            // is also the table name, the way that the repository works).
            authRefFieldPaths =
                    ServiceBindingUtils.getAllPartsPropertyValues(sb,
                    refPropName, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
            if (authRefFieldPaths.isEmpty()) {
                continue;
            }
            ArrayList<AuthRefConfigInfo> authRefsInfo = new ArrayList<AuthRefConfigInfo>();
            for (String spec : authRefFieldPaths) {
                AuthRefConfigInfo arci = new AuthRefConfigInfo(spec);
                authRefsInfo.add(arci);
            }

            String docType = sb.getObject().getName();
            queriedServiceBindings.put(docType, sb);
            authRefFieldsByService.put(docType, authRefsInfo);
            docTypes.add(docType);
            fFirst = false;
        }
        if (fFirst) { // found no authRef fields - nothing to query
            return null;
        }
        // We used to build a complete matches query, but that was too complex.
        // Just build a keyword query based upon some key pieces - the urn syntax elements and the shortID
        // Note that this will also match the Item itself, but that will get filtered out when
        // we compute actual matches.
        AuthorityTermInfo authTermInfo = RefNameUtils.parseAuthorityTermInfo(refName);

        String keywords = RefNameUtils.URN_PREFIX
                + " AND " + (authTermInfo.inAuthority.name != null
                ? authTermInfo.inAuthority.name : authTermInfo.inAuthority.csid)
                + " AND " + (authTermInfo.name != null
                ? authTermInfo.name : authTermInfo.csid);

        String whereClauseStr = QueryManager.createWhereClauseFromKeywords(keywords);

        if (logger.isTraceEnabled()) {
            logger.trace("The 'where' clause to find refObjs is: ", whereClauseStr);
        }

        return whereClauseStr;
    }

    /*
     * Runs through the list of found docs, processing them. If list is
     * non-null, then processing means gather the info for items. If list is
     * null, and newRefName is non-null, then processing means replacing and
     * updating. If processing/updating, this must be called in the context of
     * an open session, and caller must release Session after calling this.
     *
     */
    private static int processRefObjsDocList(
            DocumentModelList docList,
            String tenantId,
            String refName,
            boolean matchBaseOnly,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            List<AuthorityRefDocList.AuthorityRefDocItem> list,
            String newAuthorityRefName) {
        Iterator<DocumentModel> iter = docList.iterator();
        int nRefsFoundTotal = 0;
        while (iter.hasNext()) {
            DocumentModel docModel = iter.next();
            AuthorityRefDocList.AuthorityRefDocItem ilistItem;

            String docType = docModel.getDocumentType().getName(); // REM - This will be a tentant qualified document type
            docType = ServiceBindingUtils.getUnqualifiedTenantDocType(docType);
            ServiceBindingType sb = queriedServiceBindings.get(docType);
            if (sb == null) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: No Service Binding for docType: " + docType);
            }

            if (list == null) { // no list - should be update refName case.
                if (newAuthorityRefName == null) {
                    throw new InternalError("processRefObjsDocList() called with neither an itemList nor a new RefName!");
                }
                ilistItem = null;
            } else {    // Have a list - refObjs case
                if (newAuthorityRefName != null) {
                    throw new InternalError("processRefObjsDocList() called with both an itemList and a new RefName!");
                }
                ilistItem = new AuthorityRefDocList.AuthorityRefDocItem();
                String csid = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
                ilistItem.setDocId(csid);
                String uri = "";
                UriTemplateRegistry registry = ServiceMain.getInstance().getUriTemplateRegistry();
                UriTemplateRegistryKey key = new UriTemplateRegistryKey(tenantId, docType);
                StoredValuesUriTemplate template = registry.get(key);
                if (template != null) {
                    Map<String, String> additionalValues = new HashMap<String, String>();
                    if (template.getUriTemplateType() == UriTemplateFactory.RESOURCE) {
                        additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, csid);
                        uri = template.buildUri(additionalValues);
                    } else if (template.getUriTemplateType() == UriTemplateFactory.ITEM) {
                        try {
                            String inAuthorityCsid = (String) docModel.getPropertyValue("inAuthority"); // AuthorityItemJAXBSchema.IN_AUTHORITY
                            additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, inAuthorityCsid);
                            additionalValues.put(UriTemplateFactory.ITEM_IDENTIFIER_VAR, csid);
                            uri = template.buildUri(additionalValues);
                        } catch (Exception e) {
                            logger.warn("Could not extract inAuthority property from authority item record: " + e.getMessage());
                        }
                    } else if (template.getUriTemplateType() == UriTemplateFactory.CONTACT) {
                        // FIXME: Generating contact sub-resource URIs requires additional work,
                        // as a follow-on to CSPACE-5271 - ADR 2012-08-16
                        // Sets the default (empty string) value for uri, for now
                    } else {
                        logger.warn("Unrecognized URI template type = " + template.getUriTemplateType());
                        // Sets the default (empty string) value for uri
                    }
                } else { // (if template == null)
                    logger.warn("Could not retrieve URI template from registry via tenant ID "
                            + tenantId + " and docType " + docType);
                    // Sets the default (empty string) value for uri
                }
                ilistItem.setUri(uri);
                try {
                    ilistItem.setWorkflowState(docModel.getCurrentLifeCycleState());
                    ilistItem.setUpdatedAt(DocHandlerBase.getUpdatedAtAsString(docModel));
                } catch (Exception e) {
                    logger.error("Error getting core values for doc [" + csid + "]: " + e.getLocalizedMessage());
                }
                ilistItem.setDocType(docType);
                ilistItem.setDocNumber(
                        ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NUMBER_PROP, docModel));
                ilistItem.setDocName(
                        ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NAME_PROP, docModel));
            }
            // Now, we have to loop over the authRefFieldsByService to figure
            // out which field(s) matched this.
            List<AuthRefConfigInfo> matchingAuthRefFields = authRefFieldsByService.get(docType);
            if (matchingAuthRefFields == null || matchingAuthRefFields.isEmpty()) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: internal logic error: can't fetch authRefFields for DocType.");
            }
            //String authRefAncestorField = "";
            //String authRefDescendantField = "";
            //String sourceField = "";
            int nRefsFoundInDoc = 0;

            ArrayList<RefNameServiceUtils.AuthRefInfo> foundProps = new ArrayList<RefNameServiceUtils.AuthRefInfo>();
            try {
                findAuthRefPropertiesInDoc(docModel, matchingAuthRefFields, refName, matchBaseOnly, foundProps); // REM - side effect that foundProps is set
                for (RefNameServiceUtils.AuthRefInfo ari : foundProps) {
                    if (ilistItem != null) {
                        if (nRefsFoundInDoc == 0) {    // First one?
                            ilistItem.setSourceField(ari.getQualifiedDisplayName());
                        } else {    // duplicates from one object
                            ilistItem = cloneAuthRefDocItem(ilistItem, ari.getQualifiedDisplayName());
                        }
                        list.add(ilistItem);
                    } else {    // update refName case
                        Property propToUpdate = ari.getProperty();
                        propToUpdate.setValue(newAuthorityRefName);
                    }
                    nRefsFoundInDoc++;
                }
            } catch (ClientException ce) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: Problem fetching values from repo: " + ce.getLocalizedMessage());
            }
            if (nRefsFoundInDoc == 0) {
                logger.warn(
                        "getAuthorityRefDocs: Result: "
                        + docType + " [" + NuxeoUtils.getCsid(docModel)
                        + "] does not reference ["
                        + refName + "]");
            }
            nRefsFoundTotal += nRefsFoundInDoc;
        }
        return nRefsFoundTotal;
    }

    private static AuthorityRefDocList.AuthorityRefDocItem cloneAuthRefDocItem(
            AuthorityRefDocList.AuthorityRefDocItem ilistItem, String sourceField) {
        AuthorityRefDocList.AuthorityRefDocItem newlistItem = new AuthorityRefDocList.AuthorityRefDocItem();
        newlistItem.setDocId(ilistItem.getDocId());
        newlistItem.setDocName(ilistItem.getDocName());
        newlistItem.setDocNumber(ilistItem.getDocNumber());
        newlistItem.setDocType(ilistItem.getDocType());
        newlistItem.setUri(ilistItem.getUri());
        newlistItem.setSourceField(sourceField);
        return newlistItem;
    }

    public static List<AuthRefInfo> findAuthRefPropertiesInDoc(
            DocumentModel docModel,
            List<AuthRefConfigInfo> authRefFieldInfo,
            String refNameToMatch,
            List<AuthRefInfo> foundProps) {
    	return findAuthRefPropertiesInDoc(docModel, authRefFieldInfo, 
    									refNameToMatch, false, foundProps);
    }
    
    public static List<AuthRefInfo> findAuthRefPropertiesInDoc(
            DocumentModel docModel,
            List<AuthRefConfigInfo> authRefFieldInfo,
            String refNameToMatch,
            boolean matchBaseOnly,
            List<AuthRefInfo> foundProps) {
        // Assume that authRefFieldInfo is keyed by the field name (possibly mapped for UI)
        // and the values are elPaths to the field, where intervening group structures in
        // lists of complex structures are replaced with "*". Thus, valid paths include
        // the following (note that the ServiceBindingUtils prepend schema names to configured values):
        // "schemaname:fieldname"
        // "schemaname:scalarlistname"
        // "schemaname:complexfieldname/fieldname"
        // "schemaname:complexlistname/*/fieldname"
        // "schemaname:complexlistname/*/scalarlistname"
        // "schemaname:complexlistname/*/complexfieldname/fieldname"
        // "schemaname:complexlistname/*/complexlistname/*/fieldname"
        // etc.
        for (AuthRefConfigInfo arci : authRefFieldInfo) {
            try {
                // Get first property and work down as needed.
                Property prop = docModel.getProperty(arci.pathEls[0]);
                findAuthRefPropertiesInProperty(foundProps, prop, arci, 0, refNameToMatch, matchBaseOnly);
            } catch (Exception e) {
                logger.error("Problem fetching property: " + arci.pathEls[0]);
            }
        }
        return foundProps;
    }

    private static List<AuthRefInfo> findAuthRefPropertiesInProperty(
            List<AuthRefInfo> foundProps,
            Property prop,
            AuthRefConfigInfo arci,
            int pathStartIndex, // Supports recursion and we work down the path
            String refNameToMatch,
            boolean matchBaseOnly ) {
        if (pathStartIndex >= arci.pathEls.length) {
            throw new ArrayIndexOutOfBoundsException("Index = " + pathStartIndex + " for path: "
                    + arci.pathEls.toString());
        }
        AuthRefInfo ari = null;
        if (prop == null) {
            return foundProps;
        }

        if (prop instanceof StringProperty) {    // scalar string
            addARIifMatches(refNameToMatch, matchBaseOnly, arci, prop, foundProps);
        } else if (prop instanceof List) {
            List<Property> propList = (List<Property>) prop;
            // run through list. Must either be list of Strings, or Complex
            for (Property listItemProp : propList) {
                if (listItemProp instanceof StringProperty) {
                    if (arci.pathEls.length - pathStartIndex != 1) {
                        logger.error("Configuration for authRefs does not match schema structure: "
                                + arci.pathEls.toString());
                        break;
                    } else {
                        addARIifMatches(refNameToMatch, matchBaseOnly, arci, listItemProp, foundProps);
                    }
                } else if (listItemProp.isComplex()) {
                    // Just recurse to handle this. Note that since this is a list of complex, 
                    // which should look like listName/*/... we add 2 to the path start index 
                    findAuthRefPropertiesInProperty(foundProps, listItemProp, arci,
                            pathStartIndex + 2, refNameToMatch, matchBaseOnly);
                } else {
                    logger.error("Configuration for authRefs does not match schema structure: "
                            + arci.pathEls.toString());
                    break;
                }
            }
        } else if (prop.isComplex()) {
            String localPropName = arci.pathEls[pathStartIndex];
            try {
                Property localProp = prop.get(localPropName);
                // Now just recurse, pushing down the path 1 step
                findAuthRefPropertiesInProperty(foundProps, localProp, arci,
                        pathStartIndex, refNameToMatch, matchBaseOnly);
            } catch (PropertyNotFoundException pnfe) {
                logger.error("Could not find property: [" + localPropName + "] in path: "
                        + arci.getFullPath());
                // Fall through - ari will be null and we will continue...
            }
        } else {
            logger.error("Configuration for authRefs does not match schema structure: "
                    + arci.pathEls.toString());
        }

        if (ari != null) {
            foundProps.add(ari); //FIXME: REM - This is dead code.  'ari' is never touched after being initalized to null.  Why?
        }

        return foundProps;
    }

    private static void addARIifMatches(
            String refNameToMatch,
            boolean matchBaseOnly,
            AuthRefConfigInfo arci,
            Property prop,
            List<AuthRefInfo> foundProps) {
        // Need to either match a passed refName 
        // OR have no refName to match but be non-empty
        try {
            String value = (String) prop.getValue();
            if (((refNameToMatch != null) && 
	            		(matchBaseOnly?
	            			(value!=null && value.startsWith(refNameToMatch))
	            			:refNameToMatch.equals(value)))
                    || ((refNameToMatch == null) && Tools.notBlank(value))) {
                // Found a match
                logger.debug("Found a match on property: " + prop.getPath() + " with value: [" + value + "]");
                AuthRefInfo ari = new AuthRefInfo(arci, prop);
                foundProps.add(ari);
            }
        } catch (PropertyException pe) {
            logger.debug("PropertyException on: " + prop.getPath() + pe.getLocalizedMessage());
        }
    }

    /*
     * Identifies whether the refName was found in the supplied field. If passed
     * a new RefName, will set that into fields in which the old one was found.
     *
     * Only works for: * Scalar fields * Repeatable scalar fields (aka
     * multi-valued fields)
     *
     * Does not work for: * Structured fields (complexTypes) * Repeatable
     * structured fields (repeatable complexTypes) private static int
     * refNameFoundInField(String oldRefName, Property fieldValue, String
     * newRefName) { int nFound = 0; if (fieldValue instanceof List) {
     * List<Property> fieldValueList = (List) fieldValue; for (Property
     * listItemValue : fieldValueList) { try { if ((listItemValue instanceof
     * StringProperty) &&
     * oldRefName.equalsIgnoreCase((String)listItemValue.getValue())) {
     * nFound++; if(newRefName!=null) { fieldValue.setValue(newRefName); } else
     * { // We cannot quit after the first, if we are replacing values. // If we
     * are just looking (not replacing), finding one is enough. break; } } }
     * catch( PropertyException pe ) {} } } else { try { if ((fieldValue
     * instanceof StringProperty) &&
     * oldRefName.equalsIgnoreCase((String)fieldValue.getValue())) { nFound++;
     * if(newRefName!=null) { fieldValue.setValue(newRefName); } } } catch(
     * PropertyException pe ) {} } return nFound; }
     */
}
