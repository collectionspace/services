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

import javax.ws.rs.core.Response;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.core.api.model.PropertyNotFoundException;
import org.nuxeo.ecm.core.api.model.impl.primitives.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.CSWebApplicationException;
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
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
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

    public static enum SpecifierForm {
        CSID, URN_NAME // Either a CSID or a short ID
    };

    public static class Specifier {
        //
        // URN statics for things like urn:cspace:name(grover)
        //
        final static String URN_PREFIX = "urn:cspace:";
        final static int URN_PREFIX_LEN = URN_PREFIX.length();
        final static String URN_PREFIX_NAME = "name(";
        final static int URN_NAME_PREFIX_LEN = URN_PREFIX_LEN + URN_PREFIX_NAME.length();
        final static String URN_PREFIX_ID = "id(";
        final static int URN_ID_PREFIX_LEN = URN_PREFIX_LEN + URN_PREFIX_ID.length();
    	
        public SpecifierForm form;
        public String value;

        public Specifier(SpecifierForm form, String value) {
            this.form = form;
            this.value = value;
        }
        
        /*
         *  identifier can be a CSID form like a8ad38ec-1d7d-4bf2-bd31 or a URN form like urn:cspace:name(shortid) or urn:cspace:id(a8ad38ec-1d7d-4bf2-bd31)
         *
         */
        public static Specifier getSpecifier(String identifier) throws CSWebApplicationException {
        	return getSpecifier(identifier, "NO-OP", "NO-OP");
        }

        /*
         *  identifier can be a CSID form like a8ad38ec-1d7d-4bf2-bd31 or a URN form like urn:cspace:name(shortid) or urn:cspace:id(a8ad38ec-1d7d-4bf2-bd31)
         *
         */
        public static Specifier getSpecifier(String identifier, String method, String op) throws CSWebApplicationException {
        	Specifier result = null;

        	if (identifier != null) {
                if (!identifier.startsWith(URN_PREFIX)) {
                    // We'll assume it is a CSID and complain if it does not match
                    result = new Specifier(SpecifierForm.CSID, identifier);
                } else {
                    if (identifier.startsWith(URN_PREFIX_NAME, URN_PREFIX_LEN)) {
                        int closeParen = identifier.indexOf(')', URN_NAME_PREFIX_LEN);
                        if (closeParen >= 0) {
                            result = new Specifier(SpecifierForm.URN_NAME,
                                    identifier.substring(URN_NAME_PREFIX_LEN, closeParen));
                        }
                    } else if (identifier.startsWith(URN_PREFIX_ID, URN_PREFIX_LEN)) {
                        int closeParen = identifier.indexOf(')', URN_ID_PREFIX_LEN);
                        if (closeParen >= 0) {
                            result = new Specifier(SpecifierForm.CSID,
                                    identifier.substring(URN_ID_PREFIX_LEN, closeParen));
                        }
                    } else {
                        logger.error(method + ": bad or missing specifier!");
                        Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                                op + " failed on bad or missing Authority specifier").type(
                                "text/plain").build();
                        throw new CSWebApplicationException(response);
                    }
                }
            }
            
            return result;
        }
        
        /**
         * Creates a refName in the name / shortIdentifier form.
         *
         * @param shortId a shortIdentifier for an authority or one of its terms
         * @return a refName for that authority or term, in the name / shortIdentifier form.
         *         If the provided shortIdentifier is null or empty, returns
         *         the empty string.
         */
        public static String createShortIdURNValue(String shortId) {
        	String result = null;
        	
            if (shortId != null || !shortId.trim().isEmpty()) {
                result = String.format("urn:cspace:name(%s)", shortId);
            }
            
            return result;
        }        
        
        /**
         * Returns a URN string identifier -e.g., urn:cspace:name(patrick) or urn:cspace:id(579d18a6-b464-4b11-ba3a)
         * 
         * @return
         * @throws Exception
         */
        public String getURNValue() throws Exception {
        	String result = null;
        	
        	if (form == SpecifierForm.CSID) {
        		result = String.format("urn:cspace:id(%s)", value);
        	} else if (form == SpecifierForm.URN_NAME) {
        		result = String.format("urn:cspace:name(%s)", value);
        	} else {
        		throw new Exception(String.format("Unknown specifier form '%s'.", form));
        	}
        	
        	return result;
        }
    }
    
    public static class AuthorityItemSpecifier {
    	private Specifier parentSpecifier;
    	private Specifier itemSpecifier;
    	
    	public AuthorityItemSpecifier(Specifier parentSpecifier, Specifier itemSpecifier) {
    		this.parentSpecifier = parentSpecifier;
    		this.itemSpecifier = itemSpecifier;
    	}
    	
    	public AuthorityItemSpecifier(SpecifierForm form, String parentCsidOrShortId, String itemCsidOrShortId) {
    		this.parentSpecifier = new Specifier(form, parentCsidOrShortId);
    		this.itemSpecifier = new Specifier(form, itemCsidOrShortId);
    	}    	
    	
    	public Specifier getParentSpecifier() {
    		return this.parentSpecifier;
    	}
    	
    	public Specifier getItemSpecifier() {
    		return this.itemSpecifier;
    	}
    	
    	@Override
    	public String toString() {
    		String result = "%s/items/%s";
    		
    		try {
				result = String.format(result, this.parentSpecifier.getURNValue(), this.itemSpecifier.getURNValue());
			} catch (Exception e) {
				result = "Unknown error trying to get string representation of Specifier.";
				logger.error(result, e);
			}
    		
    		return result;
    	}
    }

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
            CoreSessionInterface repoSession,
            String oldRefName,
            String newRefName) throws Exception {
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
		List<String> authRefFields = ((AbstractServiceContextImpl) ctx).getAllPartsPropertyValues(
				ServiceBindingUtils.AUTH_REF_PROP, ServiceBindingUtils.QUALIFIED_PROP_NAMES);
		ArrayList<AuthRefConfigInfo> authRefsInfo = new ArrayList<AuthRefConfigInfo>(authRefFields.size());
		for (String spec : authRefFields) {
			AuthRefConfigInfo arci = new AuthRefConfigInfo(spec);
			authRefsInfo.add(arci);
		}
		return authRefsInfo;
	}

    public static AuthorityRefDocList getAuthorityRefDocs(
    		CoreSessionInterface repoSession,
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            List<String> serviceTypes,
            String refName,
            String refPropName, // authRef or termRef, authorities or vocab terms.
            DocumentFilter filter,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws DocumentException, DocumentNotFoundException {
        AuthorityRefDocList wrapperList = new AuthorityRefDocList();
        AbstractCommonList commonList = (AbstractCommonList) wrapperList;
        int pageNum = filter.getStartPage();
        int pageSize = filter.getPageSize();
        
        List<AuthorityRefDocList.AuthorityRefDocItem> list =
                wrapperList.getAuthorityRefDocItem();

        Map<String, ServiceBindingType> queriedServiceBindings = new HashMap<String, ServiceBindingType>();
        Map<String, List<AuthRefConfigInfo>> authRefFieldsByService = new HashMap<String, List<AuthRefConfigInfo>>();

        NuxeoRepositoryClientImpl nuxeoRepoClient = (NuxeoRepositoryClientImpl) repoClient;
        try {
            // Ignore any provided page size and number query parameters in
            // the following call, as they pertain to the list of authority
            // references to be returned, not to the list of documents to be
            // scanned for those references.
            
            // Get a list of possibly referencing documents. This list is
            // lazily loaded, page by page. Ideally, only one page will
            // need to be loaded to fill one page of results. Some number
            // of possibly referencing documents will be false positives,
            // so use a page size of double the requested page size to
            // account for those.
            DocumentModelList docList = findAllAuthorityRefDocs(ctx, 
                    repoClient, 
                    repoSession,
                    serviceTypes, 
                    refName, 
                    refPropName, 
                    queriedServiceBindings, 
                    authRefFieldsByService,
                    filter.getWhereClause(), 
                    null, // orderByClause
                    2, // pageScale
                    pageSize,
                    useDefaultOrderByClause,
                    computeTotal);

            if (docList == null) { // found no authRef fields - nothing to process
                return wrapperList;
            }

            String fieldList = "docType|docId|docNumber|docName|sourceField|uri|refName|updatedAt|workflowState";  // FIXME: Should not be hard-coded string
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
    		
    		// *** Need to pass in pagination info here. 
            long nRefsFound = processRefObjsDocListForList(docList, ctx.getTenantId(), strippedRefName, 
            		queriedServiceBindings, authRefFieldsByService, // the actual list size needs to be updated to the size of "list"
                    list, pageSize, pageNum);
            	
            commonList.setPageSize(pageSize);
            
            // Values returned in the pagination block above the list items
            // need to reflect the number of references to authority items
            // returned, rather than the number of documents originally scanned
            // to find such references.
            // This will be an estimate only...
            commonList.setPageNum(pageNum);
           	commonList.setTotalItems(nRefsFound);	// Accurate if total was scanned, otherwise, just an estimate
            commonList.setItemsInPage(list.size());
            
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
            CoreSessionInterface repoSession,
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
        final String ORDER_BY_VALUE = CollectionSpaceClient.CORE_CREATED_AT  // "collectionspace_core:createdAt";
                                          + ", " + IQueryManager.NUXEO_UUID; // CSPACE-6333: Add secondary sort on uuid, in case records have the same createdAt timestamp.

        if (repoClient instanceof NuxeoRepositoryClientImpl == false) {
            throw new InternalError("updateAuthorityRefDocs() called with unknown repoClient type!");
        }
        
        try { // REM - How can we deal with transaction and timeout issues here?
            final int pageSize = N_OBJS_TO_UPDATE_PER_LOOP;
            DocumentModelList docList;
            boolean morePages = true;
            while (morePages) {

                docList = findAuthorityRefDocs(ctx, 
                        repoClient, 
                        repoSession,
                        getRefNameServiceTypes(), 
                        oldRefName, 
                        refPropName,
                        queriedServiceBindings, 
                        authRefFieldsByService, 
                        WHERE_CLAUSE_ADDITIONS_VALUE, 
                        ORDER_BY_VALUE,
                        currentPage,
                        pageSize,
                        true,       // useDefaultOrderByClause
                        false);     // computeTotal

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
                long nRefsFoundThisPage = processRefObjsDocListForUpdate(ctx, docList, ctx.getTenantId(), oldRefName, 
                		queriedServiceBindings, authRefFieldsByService, // Perform the refName updates on the list of document models
                        newRefName);
                if (nRefsFoundThisPage > 0) {
                    ((NuxeoRepositoryClientImpl) repoClient).saveDocListWithoutHandlerProcessing(ctx, repoSession, docList, true); // Flush the document model list out to Nuxeo storage
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

    private static DocumentModelList findAllAuthorityRefDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            CoreSessionInterface repoSession, List<String> serviceTypes,
            String refName,
            String refPropName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            String whereClauseAdditions,
            String orderByClause,
            int pageScale,
            int pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws DocumentException, DocumentNotFoundException {
    	    	
        	return new LazyAuthorityRefDocList(ctx, 
        	        repoClient, 
        	        repoSession,
        			serviceTypes, 
        			refName, 
        			refPropName, 
        			queriedServiceBindings, 
        			authRefFieldsByService,
        			whereClauseAdditions, 
        			orderByClause,
        			pageSize*pageScale, 
        			useDefaultOrderByClause, 
        			computeTotal);
    }
    
    protected static DocumentModelList findAuthorityRefDocs(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            CoreSessionInterface repoSession, List<String> serviceTypes,
            String refName,
            String refPropName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            String whereClauseAdditions,
            String orderByClause,
            int pageNum,
            int pageSize,
            boolean useDefaultOrderByClause,
            boolean computeTotal) throws DocumentException, DocumentNotFoundException {

        // Get the service bindings for this tenant
        TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        
        // We need to get all the procedures, authorities, and objects.
        List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(ctx.getTenantId(), serviceTypes);
        if (servicebindings == null || servicebindings.isEmpty()) {
            logger.error("RefNameServiceUtils.getAuthorityRefDocs: No services bindings found, cannot proceed!");
            return null;
        }
        // Filter the list for current user rights
        servicebindings = SecurityUtils.getReadableServiceBindingsForCurrentUser(servicebindings);

        ArrayList<String> docTypes = new ArrayList<String>();

        String query = computeWhereClauseForAuthorityRefDocs(refName, refPropName, docTypes, servicebindings, // REM - Side effect that docTypes, authRefFieldsByService, and queriedServiceBindings get set/change.  Any others?
                queriedServiceBindings, authRefFieldsByService);
        if (query == null) { // found no authRef fields - nothing to query
            return null;
        }
        
        // Additional qualifications, like workflow state
        if (Tools.notBlank(whereClauseAdditions)) {
            query += " AND " + whereClauseAdditions;
        }
        
        // Now we have to issue the search
        NuxeoRepositoryClientImpl nuxeoRepoClient = (NuxeoRepositoryClientImpl) repoClient;
        DocumentWrapper<DocumentModelList> docListWrapper = nuxeoRepoClient.findDocs(
                ctx,
                repoSession,
                docTypes, 
                query, 
                orderByClause,
                pageNum,
                pageSize,
                useDefaultOrderByClause, 
                computeTotal);
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

        // Note that this will also match the term item itself, but that will get filtered out when
        // we compute actual matches.
        AuthorityTermInfo authTermInfo = RefNameUtils.parseAuthorityTermInfo(refName);
        
        // Example refname: urn:cspace:pahma.cspace.berkeley.edu:personauthorities:name(person):item:name(ReneRichie1586477168934)
        // Corresponding phrase: "urn cspace pahma cspace berkeley edu personauthorities name person item name ReneRichie1586477168934
        
        String refnamePhrase = String.format("urn cspace %s %s name %s item name %s",
        		RefNameUtils.domainToPhrase(authTermInfo.inAuthority.domain),
        		authTermInfo.inAuthority.resource,
        		authTermInfo.inAuthority.name,
        		authTermInfo.name
        		);
        refnamePhrase = String.format("\"%s\"", refnamePhrase); // surround the phase in double quotes to indicate this is a NXQL phrase search

        String whereClauseStr = QueryManager.createWhereClauseFromKeywords(refnamePhrase);

        if (logger.isTraceEnabled()) {
            logger.trace("The 'where' clause to find refObjs is: ", refnamePhrase);
        }

        return whereClauseStr;
    }
    
    // TODO there are multiple copies of this that should be put somewhere common.
	protected static String getRefname(DocumentModel docModel) throws ClientException {
		String result = (String)docModel.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
				CollectionSpaceClient.COLLECTIONSPACE_CORE_REFNAME);
		return result;
	}

    private static long processRefObjsDocListForUpdate(
    		ServiceContext ctx,
            DocumentModelList docList,
            String tenantId,
            String refName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            String newAuthorityRefName) {
    	boolean matchBaseOnly = false;
    	
    	if (ctx.shouldForceUpdateRefnameReferences() == true) {
    		refName = RefNameUtils.stripAuthorityTermDisplayName(refName);
    		matchBaseOnly = true;
    	}
    	
    	return processRefObjsDocList(docList, tenantId, refName, matchBaseOnly, queriedServiceBindings,
    			authRefFieldsByService, null, 0, 0, newAuthorityRefName);
    }
    			
    private static long processRefObjsDocListForList(
            DocumentModelList docList,
            String tenantId,
            String refName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            List<AuthorityRefDocList.AuthorityRefDocItem> list, 
            int pageSize, int pageNum) {
    	return processRefObjsDocList(docList, tenantId, refName, true, queriedServiceBindings,
    			authRefFieldsByService, list, pageSize, pageNum, null);
    }
    			

	/*
     * Runs through the list of found docs, processing them. If list is
     * non-null, then processing means gather the info for items. If list is
     * null, and newRefName is non-null, then processing means replacing and
     * updating. If processing/updating, this must be called in the context of
     * an open session, and caller must release Session after calling this.
     *
     */
    private static long processRefObjsDocList(
            DocumentModelList docList,
            String tenantId,
            String refName,
            boolean matchBaseOnly,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            List<AuthorityRefDocList.AuthorityRefDocItem> list,
            int pageSize, int pageNum,	// Only used when constructing a list.
            String newAuthorityRefName) {
        UriTemplateRegistry registry = ServiceMain.getInstance().getUriTemplateRegistry();
        Iterator<DocumentModel> iter = docList.iterator();
        long nRefsFoundTotal = 0;
        long nRefsFalsePositives = 0;
        boolean foundSelf = false;
        boolean warningLogged = false;

        // When paginating results, we have to guess at the total. First guess is the number of docs returned
        // by the query. However, this returns some false positives, so may be high. 
        // In addition, we can match multiple fields per doc, so this may be low. Fun, eh?
        long nDocsReturnedInQuery = (int)docList.totalSize();
        long nDocsProcessed = 0;
        long firstItemInPage = pageNum*pageSize;
        while (iter.hasNext()) {
        	if (!warningLogged && (float)nRefsFalsePositives / nDocsReturnedInQuery > 0.5) {
        		warningLogged = true;
        		String msg = String.format("When searching for documents referencing the term '%s', more than 1/2 of the results were false-positives.",
        				refName);
        		logger.warn(msg);
        	}
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
                pageSize = 0;
                firstItemInPage = 0;	// Do not paginate if updating, rather than building list
            } else {    // Have a list - refObjs case
                if (newAuthorityRefName != null) {
                    throw new InternalError("processRefObjsDocList() called with both an itemList and a new RefName!");
                }
                if (firstItemInPage > 100) {
                	String msg = String.format("Processing a large offset for records referencing (term:%s, size:%d, num:%d) - will be expensive!!!",
                			refName, pageSize, pageNum);
                	logger.warn(msg);
                }
                // Note that we have to go through check all the fields to determine the actual page start
                ilistItem = new AuthorityRefDocList.AuthorityRefDocItem();
                String csid = NuxeoUtils.getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
                try {
                	String itemRefName = getRefname(docModel);
                	ilistItem.setRefName(itemRefName);
                } catch (ClientException ce) {
                    throw new RuntimeException(
                            "processRefObjsDocList: Problem fetching refName from item Object: " 
                            		+ ce.getLocalizedMessage());
                }
                ilistItem.setDocId(csid);
                String uri = "";
                UriTemplateRegistryKey key = new UriTemplateRegistryKey(tenantId, docType);
                StoredValuesUriTemplate template = registry.get(key);
                if (template != null) {
                    Map<String, String> additionalValues = new HashMap<String, String>();
                    if (template.getUriTemplateType() == UriTemplateFactory.RESOURCE) {
                        additionalValues.put(UriTemplateFactory.IDENTIFIER_VAR, csid);
                        uri = template.buildUri(additionalValues);
                    } else if (template.getUriTemplateType() == UriTemplateFactory.ITEM) {
                        try {
                            String inAuthorityCsid = (String) NuxeoUtils.getProperyValue(docModel, "inAuthority"); //docModel.getPropertyValue("inAuthority"); // AuthorityItemJAXBSchema.IN_AUTHORITY
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
                    ilistItem.setUpdatedAt(NuxeoDocumentModelHandler.getUpdatedAtAsString(docModel));
                } catch (Exception e) {
                    logger.error("Error getting core values for doc [" + csid + "]: " + e.getLocalizedMessage());
                }
                ilistItem.setDocType(docType);
                ilistItem.setDocNumber(
                        ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NUMBER_PROP, docModel));
                ilistItem.setDocName(
                        ServiceBindingUtils.getMappedFieldInDoc(sb, ServiceBindingUtils.OBJ_NAME_PROP, docModel));
            }
            // Now, we have to loop over the authRefFieldsByService to figure out
            // out which field(s) matched this.
            List<AuthRefConfigInfo> matchingAuthRefFields = authRefFieldsByService.get(docType);
            if (matchingAuthRefFields == null || matchingAuthRefFields.isEmpty()) {
                throw new RuntimeException(
                        "getAuthorityRefDocs: internal logic error: can't fetch authRefFields for DocType.");
            }

            ArrayList<RefNameServiceUtils.AuthRefInfo> foundProps = new ArrayList<RefNameServiceUtils.AuthRefInfo>();
            try {
                findAuthRefPropertiesInDoc(docModel, matchingAuthRefFields, refName, matchBaseOnly, foundProps); // REM - side effect that foundProps is set
                if(!foundProps.isEmpty()) {
                    int nRefsFoundInDoc = 0;
                	for (RefNameServiceUtils.AuthRefInfo ari : foundProps) {
                		if (ilistItem != null) {
                			// So this is a true positive, and not a false one. We have to consider pagination now.
                			if(nRefsFoundTotal >= firstItemInPage) {	// skipped enough already
                				if (nRefsFoundInDoc == 0) {    // First one?
                					ilistItem.setSourceField(ari.getQualifiedDisplayName());
                				} else {    // duplicates from one object
                					ilistItem = cloneAuthRefDocItem(ilistItem, ari.getQualifiedDisplayName());
                				}
                				list.add(ilistItem);
                        		nRefsFoundInDoc++;	// Only increment if processed, or clone logic above will fail
                			}
                		} else {    // update refName case
                			Property propToUpdate = ari.getProperty();
                			propToUpdate.setValue(newAuthorityRefName);
                		}
                		nRefsFoundTotal++;		// Whether we processed or not, we found - essential to pagination logic
                	}
                } else if(ilistItem != null) {
                	String docRefName = ilistItem.getRefName();
                    if (matchBaseOnly?
	            			(docRefName!=null && docRefName.startsWith(refName))
	            			:refName.equals(docRefName)) {
                		// We found the self for an item
                		foundSelf = true;
                		logger.trace("getAuthorityRefDocs: Result: "
                						+ docType + " [" + NuxeoUtils.getCsid(docModel)
                						+ "] appears to be self for: ["
                						+ refName + "]");
                	} else {
                		nRefsFalsePositives++;
                		logger.trace("getAuthorityRefDocs: Result: "
                						+ docType + " [" + NuxeoUtils.getCsid(docModel)
                						+ "] does not reference ["
                						+ refName + "]");
                	}
                }
            } catch (ClientException ce) {
            	throw new RuntimeException(
            			"getAuthorityRefDocs: Problem fetching values from repo: " + ce.getLocalizedMessage());
            }

            nDocsProcessed++;

            // Done processing that doc. Are we done with the whole page?
            // Note pageSize <=0 means do them all
            if ((pageSize > 0) && ((nRefsFoundTotal - firstItemInPage) >= pageSize)) {
            	// Quitting early, so we need to estimate the total. Assume one per doc
            	// for the rest of the docs we matched in the query
            	long unprocessedDocs = nDocsReturnedInQuery - nDocsProcessed;
            	if (unprocessedDocs > 0) {
            		// We generally match ourselves in the keyword search. If we already saw ourselves
            		// then do not try to correct for this. Otherwise, decrement the total.
            		// Yes, this is fairly goofy, but the whole estimation mechanism is goofy. 
                	if (!foundSelf)
                		unprocessedDocs--;
                	nRefsFoundTotal += unprocessedDocs;
            	}
            	break;
            }
        } // close while(iterator)
        
        // Log a final warning if we find too many false-positives.
        if ((float)nRefsFalsePositives / nDocsReturnedInQuery > 0.33) {
        	String msg = String.format("Found %d false-positives and %d only true references the refname:%s",
        			nRefsFalsePositives, nRefsFoundTotal, refName);
        	logger.warn(msg);
        }

        return nRefsFoundTotal;
    }

    /**
     * Clone an AuthorityRefDocItem which is a JAX-B generated class.  Be sure we're copying every field defined in the XSD (XML Schema) that is
     * found here services\jaxb\src\main\resources\authorityrefdocs.xsd
     * @param ilistItem
     * @param sourceField
     * @return
     */
    private static AuthorityRefDocList.AuthorityRefDocItem cloneAuthRefDocItem(
            AuthorityRefDocList.AuthorityRefDocItem ilistItem, String sourceField) {
        AuthorityRefDocList.AuthorityRefDocItem newlistItem = new AuthorityRefDocList.AuthorityRefDocItem();
        newlistItem.setDocId(ilistItem.getDocId());
        newlistItem.setDocName(ilistItem.getDocName());
        newlistItem.setDocNumber(ilistItem.getDocNumber());
        newlistItem.setDocType(ilistItem.getDocType());
        newlistItem.setUri(ilistItem.getUri());
        newlistItem.setSourceField(sourceField);
        newlistItem.setRefName(ilistItem.getRefName());
        newlistItem.setUpdatedAt(ilistItem.getUpdatedAt());
        newlistItem.setWorkflowState(ilistItem.getWorkflowState());
        return newlistItem;
    }

    public static List<AuthRefInfo> findAuthRefPropertiesInDoc(
            DocumentModel docModel,
            List<AuthRefConfigInfo> authRefFieldInfoList,
            String refNameToMatch,
            List<AuthRefInfo> foundProps) {
    	return findAuthRefPropertiesInDoc(docModel, authRefFieldInfoList, 
    									refNameToMatch, false, foundProps);
    }
    
    public static List<AuthRefInfo> findAuthRefPropertiesInDoc(
            DocumentModel docModel,
            List<AuthRefConfigInfo> authRefFieldInfoList,
            String refNameToMatch,
            boolean matchBaseOnly,
            List<AuthRefInfo> authRefInfoList) {
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
        for (AuthRefConfigInfo arci : authRefFieldInfoList) {
            try {
                // Get first property and work down as needed.
                Property prop = docModel.getProperty(arci.pathEls[0]);
                findAuthRefPropertiesInProperty(authRefInfoList, prop, arci, 0, refNameToMatch, matchBaseOnly);
            } catch (Exception e) {
                logger.error("Problem fetching property: " + arci.pathEls[0]);
            }
        }
        return authRefInfoList;
    }

    private static List<AuthRefInfo> findAuthRefPropertiesInProperty(
            List<AuthRefInfo> authRefInfoList,
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
            return authRefInfoList;
        }

        if (prop instanceof StringProperty) {    // scalar string
            addARIifMatches(refNameToMatch, matchBaseOnly, arci, prop, authRefInfoList); // REM - Side effect that foundProps gets changed/updated
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
                        addARIifMatches(refNameToMatch, matchBaseOnly, arci, listItemProp, authRefInfoList);
                    }
                } else if (listItemProp.isComplex()) {
                    // Just recurse to handle this. Note that since this is a list of complex, 
                    // which should look like listName/*/... we add 2 to the path start index 
                    findAuthRefPropertiesInProperty(authRefInfoList, listItemProp, arci,
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
                findAuthRefPropertiesInProperty(authRefInfoList, localProp, arci,
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
            authRefInfoList.add(ari); //FIXME: REM - This is dead code.  'ari' is never touched after being initalized to null.  Why?
        }

        return authRefInfoList;
    }

    private static void addARIifMatches(
            String refNameToMatch,
            boolean matchBaseOnly,
            AuthRefConfigInfo arci,
            Property prop,
            List<AuthRefInfo> authRefInfoList) {
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
                authRefInfoList.add(ari);
            }
        } catch (PropertyException pe) {
            logger.debug("PropertyException on: " + prop.getPath() + pe.getLocalizedMessage());
        }
    }
    
    public static String buildWhereForAuthByName(String authorityCommonSchemaName, String name) {
        return authorityCommonSchemaName
                + ":" + AuthorityJAXBSchema.SHORT_IDENTIFIER
                + "='" + name + "'";
    }

    /**
     * Build an NXQL query for finding an item by its short ID
     * 
     * @param authorityItemCommonSchemaName
     * @param shortId
     * @param parentcsid
     * @return
     */
    public static String buildWhereForAuthItemByName(String authorityItemCommonSchemaName, String shortId, String parentcsid) {
    	String result = null;
    	
        result = String.format("%s:%s='%s'", authorityItemCommonSchemaName, AuthorityItemJAXBSchema.SHORT_IDENTIFIER, shortId);
        //
        // Technically, we don't need the parent CSID since the short ID is unique so it can be null
        //
        if (parentcsid != null) {
        	result = String.format("%s AND %s:%s='%s'",
        			result, authorityItemCommonSchemaName, AuthorityItemJAXBSchema.IN_AUTHORITY, parentcsid);
        }
        
        return result;
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
