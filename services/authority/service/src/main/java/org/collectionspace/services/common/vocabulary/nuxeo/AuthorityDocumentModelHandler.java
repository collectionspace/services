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
package org.collectionspace.services.common.vocabulary.nuxeo;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.httpclient.HttpStatus;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.XmlTools;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.api.RefName.Authority;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentReferenceException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.query.UriInfoImpl;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.AuthorityServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.SpecifierForm;
import org.collectionspace.services.config.service.ListResultField;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.Element;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AuthorityDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
@SuppressWarnings("rawtypes")
public abstract class AuthorityDocumentModelHandler<AuthCommon> extends NuxeoDocumentModelHandler<AuthCommon> {
	private static final long SAS_SYNC_PAGE_SIZE = 500;

	private final Logger logger = LoggerFactory.getLogger(AuthorityDocumentModelHandler.class);

	protected String authorityCommonSchemaName;
	protected String authorityItemCommonSchemaName;
	protected boolean shouldUpdateRevNumber = true; // default to updating the revision number

	public AuthorityDocumentModelHandler(String authorityCommonSchemaName, String authorityItemCommonSchemaName) {
		this.authorityCommonSchemaName = authorityCommonSchemaName;
		this.authorityItemCommonSchemaName = authorityItemCommonSchemaName;
	}

	public void setShouldUpdateRevNumber(boolean flag) {
		this.shouldUpdateRevNumber = flag;
	}

	public boolean getShouldUpdateRevNumber() {
		return this.shouldUpdateRevNumber;
	}

	/**
	 * The entity type expected from the JAX-RS Response object
	 */
	public Class<String> getEntityResponseType() {
		return String.class;
	}

	@Override
	public void prepareSync() throws Exception {
		this.setShouldUpdateRevNumber(AuthorityServiceUtils.DONT_UPDATE_REV); // Never update rev nums on sync operations
	}

	protected PayloadInputPart extractPart(Response res, String partLabel) throws Exception {
		PoxPayloadIn input = new PoxPayloadIn((String) res.readEntity(getEntityResponseType()));
		PayloadInputPart payloadInputPart = input.getPart(partLabel);
		if (payloadInputPart == null) {
			logger.error("Part " + partLabel + " was unexpectedly null.");
		}
		return payloadInputPart;
	}

	@Override
	public boolean handleSync(DocumentWrapper<Object> wrapDoc) throws Exception {
		boolean result = false;
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = getServiceContext();
		Specifier specifier = (Specifier) wrapDoc.getWrappedObject();
		//
		// Get the rev number of the authority so we can compare with rev number of
		// shared authority
		//
		DocumentModel docModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), authorityCommonSchemaName,
				specifier);
		if (docModel != null) {
			String authorityCsid = docModel.getName();
			Long localRev = (Long) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.REV);
			String shortId = (String) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.SHORT_IDENTIFIER);
			String remoteClientConfigName = (String) NuxeoUtils.getProperyValue(docModel,
					// If set, contains the name of the remote client configuration (remoteClientConfigName) from the tenant bindings
					AuthorityJAXBSchema.REMOTECLIENT_CONFIG_NAME);
			//
			// Using the short ID of the local authority, create a URN specifier to retrieve
			// the SAS authority
			//
			Specifier sasSpecifier = new Specifier(SpecifierForm.URN_NAME, shortId);
			PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.requestPayloadInFromRemoteServer(ctx, remoteClientConfigName,
					sasSpecifier, getEntityResponseType());
			//
			// If the authority on the SAS is newer, synch all the items and then the
			// authority record as well
			//
			//
			Long sasRev = getRevision(sasPayloadIn);
			// FIXME: Along with the revision number, we need to use other meta information
			// to determine if a sync should happen -for now, always sync
			if (sasRev > localRev || true) {
				//
				// First, sync all the authority items
				//
				syncAllItems(ctx, authorityCsid, sasSpecifier);
				//
				// Next, sync the authority resource/record itself
				//
				AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();
				// Don't update the rev number, use the rev number for the SAS instance instead
				ctx.setProperty(AuthorityServiceUtils.SHOULD_UPDATE_REV_PROPERTY, AuthorityServiceUtils.DONT_UPDATE_REV);
				PoxPayloadOut payloadOut = authorityResource.update(ctx, ctx.getResourceMap(), ctx.getUriInfo(),
						docModel.getName(), sasPayloadIn);
				if (payloadOut != null) {
					ctx.setOutput(payloadOut);
					result = true;
				}
				//
				// We may need to transition the authority into a replicated state the first
				// time we sync it.
				//
				String workflowState = docModel.getCurrentLifeCycleState();
				if (workflowState.contains(WorkflowClient.WORKFLOWSTATE_REPLICATED) == false) {
					authorityResource.updateWorkflowWithTransition(ctx, ctx.getUriInfo(), authorityCsid,
							WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
				}
			}
		} else {
			String errMsg = String.format("Authority of type '%s' with identifier '%s' does not exist.",
					getServiceContext().getServiceName(), specifier.getURNValue());
			logger.debug(errMsg);
			throw new DocumentException(errMsg);
		}

		return result;
	}

	/*
	 * Get the list of authority items from the remote shared authority server (SAS)
	 * and synchronize them with the local authority items. If items exist on the
	 * remote but not the local, create them.
	 */
	protected void syncAllItems(ServiceContext ctx, String parentCsid, Specifier sasAuthoritySpecifier) throws Exception {
		int createdCount = 0;
		int syncedCount = 0;
		int alreadySyncedCount = 0;
		int deletedCount = 0;
		int totalProcessedCount = 0;

		Set<String> remoteShortIds = new HashSet<String>();

		// Iterate over the list of items in the remote authority.

		long pageNum = 0;
		long pageSize = SAS_SYNC_PAGE_SIZE;

		List<Element> itemElements;

		do {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Reading remote items in %s: page size %d, page num %d", sasAuthoritySpecifier.value, pageSize, pageNum));
			}

			PoxPayloadIn itemListPayload = requestItemList(ctx, sasAuthoritySpecifier, pageSize, pageNum);

			itemElements = getItemList(itemListPayload);

			if (itemElements == null) {
				itemElements = Collections.EMPTY_LIST;
			}

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Found %d items", itemElements.size()));
			}

			for (Element e : itemElements) {
				String remoteRefName = XmlTools.getElementValue(e, AuthorityItemJAXBSchema.REF_NAME);
				String remoteShortId = XmlTools.getElementValue(e, AuthorityItemJAXBSchema.SHORT_IDENTIFIER);

				remoteShortIds.add(remoteShortId);

				long status = syncRemoteItem(ctx, parentCsid, remoteRefName);

				if (status == 1) {
					createdCount++;
				} else if (status == 0) {
					syncedCount++;
				} else {
					alreadySyncedCount++;
				}

				totalProcessedCount++;
			}

			pageNum = pageNum + 1;
		} while (itemElements.size() > 0 && itemElements.size() == SAS_SYNC_PAGE_SIZE);

		// Deprecate or delete items that have been hard-deleted from the SAS but still
		// exist locally.
		// Subtract (remove) the set of remote items from the set of local items to
		// determine which
		// of the remote items have been hard deleted.

		Set<String> localShortIds = getItemsInLocalAuthority(ctx, sasAuthoritySpecifier);

		localShortIds.removeAll(remoteShortIds);

		if (localShortIds.size() > 0) {
			// Delete the remaining items (or mark them as deprecated if they still have
			// records referencing them).

			deletedCount = deleteOrDeprecateItems(ctx, parentCsid, localShortIds);

			if (deletedCount != localShortIds.size()) {
				logger.warn("Could not delete or deprecate some items during sync.");
			}
		}

		logger.info(String.format("Total number of items processed during sync: %d", totalProcessedCount));
		logger.info(String.format("Number of items synchronized: %d", syncedCount));
		logger.info(String.format("Number of items created during sync: %d", createdCount));
		logger.info(String.format("Number of items not needing synchronization: %d", alreadySyncedCount));
		logger.info(String.format("Number of items hard deleted on remote: %d", deletedCount));
	}

	/**
	 * This method should ***only*** be used as part of a SAS sync operation.
	 *
	 * @param ctx
	 * @param refNameList
	 * @return
	 * @throws Exception
	 */
	private int deleteOrDeprecateItems(ServiceContext ctx, String parentCsid, Set<String> itemShortIds)
			throws Exception {

		// Don't update the revision number when we delete or deprecate items.
		ctx.setProperty(AuthorityServiceUtils.SHOULD_UPDATE_REV_PROPERTY, false);

		Specifier parentSpecifier = new Specifier(SpecifierForm.CSID, parentCsid);
		AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();
		int handledCount = 0;

		for (String itemShortId : itemShortIds) {
			Specifier itemSpecifier = new Specifier(SpecifierForm.URN_NAME, itemShortId);

			try {
				authorityResource.deleteAuthorityItem(ctx, parentSpecifier.getURNValue(),
						itemSpecifier.getURNValue(), AuthorityServiceUtils.DONT_UPDATE_REV, AuthorityServiceUtils.DONT_ROLLBACK_ON_EXCEPTION);

				handledCount++;
			} catch (DocumentReferenceException dre) {
				logger.info(String.format("Failed to delete %s: item is referenced, and will be deprecated instead", itemShortId));

				AuthorityItemSpecifier authorityItemSpecifier = new AuthorityItemSpecifier(parentSpecifier, itemSpecifier);
				boolean deprecated = AuthorityServiceUtils.setAuthorityItemDeprecated(ctx, authorityResource, authorityItemCommonSchemaName, authorityItemSpecifier);

				if (deprecated == true) {
					handledCount++;
				}
			}
		}

		return handledCount;
	}

	/**
	 * Gets the list of SAS related items in the local authority. Exludes items with
	 * the "proposed" flag to include only SAS created items.
	 *
	 * @param ctx
	 * @param authoritySpecifier
	 * @return
	 * @throws Exception
	 */
	private Set<String> getItemsInLocalAuthority(ServiceContext ctx, Specifier authoritySpecifier) throws Exception {
		Set<String> itemShortIds = new HashSet<String>();

		ResourceMap resourceMap = ctx.getResourceMap();
		String resourceName = ctx.getClient().getServiceName();
		AuthorityResource authorityResource = (AuthorityResource) resourceMap.get(resourceName);

		long pageNum = 0;
		long pageSize = SAS_SYNC_PAGE_SIZE;

		List<ListItem> listItems;

		do {
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Reading local items in %s: page size %d, page num %d", authoritySpecifier.value,
						pageSize, pageNum));
			}

			// Construct a UriInfo to retrieve one page of results.

			UriInfo uriInfo = new UriInfoImpl(
				new URI(""),
				new URI(""),
				"",
				"pgSz=" + pageSize + "&pgNum=" + pageNum,
				Collections.<PathSegment> emptyList()
			);

			AbstractCommonList acl = authorityResource.getAuthorityItemList(ctx, authoritySpecifier.getURNValue(), uriInfo);

			listItems = acl.getListItem();

			if (logger.isDebugEnabled()) {
				logger.debug(String.format("Found %d items", listItems.size()));
			}

			for (ListItem listItem : listItems) {
				Boolean proposed = getBooleanValue(listItem, AuthorityItemJAXBSchema.PROPOSED);

				if (proposed == false) { // exclude "proposed" (i.e., local-only items)
					itemShortIds.add(AbstractCommonListUtils.ListItemGetElementValue(listItem, AuthorityItemJAXBSchema.SHORT_IDENTIFIER));
				}
			}

			pageNum = pageNum + 1;
		} while (listItems.size() > 0 && listItems.size() == SAS_SYNC_PAGE_SIZE);

		return itemShortIds;
	}

	private Boolean getBooleanValue(ListItem listItem, String name) {
		Boolean result = null;

		String value = AbstractCommonListUtils.ListItemGetElementValue(listItem, name);
		if (value != null) {
			result = Boolean.valueOf(value);
		}

		return result;
	}

	private String getStringValue(ListItem listItem, String name) {
		return AbstractCommonListUtils.ListItemGetElementValue(listItem, AuthorityItemJAXBSchema.REF_NAME);
	}

	/**
	 * This method should only be used during a SAS synchronization request.
	 *
	 * @param ctx
	 * @param parentIdentifier - Must be in short-id-refname form -i.e., urn:cspace:name(shortid)
	 * @param itemIdentifier   - Must be in short-id-refname form -i.e., urn:cspace:name(shortid)
	 * @throws Exception
	 */
	protected void createLocalItem(ServiceContext ctx, String parentCsid, String parentIdentifier, String itemIdentifier, Boolean syncHierarchicalRelationships) throws Exception {
		//
		// Create a URN short ID specifier for the getting a copy of the remote authority item
		//
		Specifier parentSpecifier = Specifier.getSpecifier(parentIdentifier);
		Specifier itemSpecifier = Specifier.getSpecifier(itemIdentifier);
		AuthorityItemSpecifier sasAuthorityItemSpecifier = new AuthorityItemSpecifier(parentSpecifier, itemSpecifier);
		//
		// Get the remote client configuration name
		//
		DocumentModel docModel = NuxeoUtils.getDocFromSpecifier(ctx, getRepositorySession(), authorityCommonSchemaName, parentSpecifier);
		String remoteClientConfigName = (String) NuxeoUtils.getProperyValue(docModel, AuthorityJAXBSchema.REMOTECLIENT_CONFIG_NAME); // If set, contains the name of the remote client configuration (remoteClientConfigName) from the tenant bindings
		//
		// Get the remote payload
		//
		PoxPayloadIn sasPayloadIn = AuthorityServiceUtils.requestPayloadInFromRemoteServer(sasAuthorityItemSpecifier, remoteClientConfigName,
				ctx.getServiceName(), getEntityResponseType(), syncHierarchicalRelationships);

		AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();

		// Remove remote uris and csids from relations, and remove relations to items that don't exist locally.
		sasPayloadIn = AuthorityServiceUtils.localizeRelations(ctx, authorityResource, parentCsid, itemSpecifier, sasPayloadIn);

		// Localize domain name parts of refnames in the payload.
		sasPayloadIn = AuthorityServiceUtils.localizeRefNameDomains(ctx, sasPayloadIn);

		//
		// Using the payload from the remote server, create a local copy of the item
		//
		Response response = authorityResource.createAuthorityItemWithParentContext(ctx, parentSpecifier.getURNValue(),
				sasPayloadIn, AuthorityServiceUtils.DONT_UPDATE_REV, AuthorityServiceUtils.NOT_PROPOSED, AuthorityServiceUtils.SAS_ITEM);
		//
		// Check the response for successful POST result
		//
		if (response.getStatus() != Response.Status.CREATED.getStatusCode()) {
			throw new DocumentException(String.format("Could not create new authority item '%s' during synchronization of the '%s' authority.",
					itemIdentifier, parentIdentifier));
		}

		// Sync the workflow state.

		String itemLocation = response.getHeaderString("Location");
		String itemCsid = itemLocation.substring(itemLocation.lastIndexOf("/") + 1);

		DocumentModel itemDocModel = NuxeoUtils.getDocFromCsid(ctx, getRepositorySession(), itemCsid);

		AuthorityServiceUtils.syncWorkflowState(
			ctx,
			authorityResource,
			getWorkflowState(sasPayloadIn),
			parentIdentifier,
			itemIdentifier,
			itemDocModel);
	}

	/**
	 * Synchronize a remote item (using its refName) with a local item.  If the local doesn't yet
	 * exist, create it.
	 * Result values:
	 * 	-1 = sync not needed; i.e., already in sync
	 *   0 = sync succeeded
	 *   1 = local item was missing so we created it
	 * @param ctx
	 * @param refName
	 * @return
	 * @throws Exception
	 */
	protected long syncRemoteItem(ServiceContext ctx, String parentCsid, String itemRefName) throws Exception {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Syncing remote item %s", itemRefName));
		}

		// Create specifiers to find the local item corresponding to the remote refname.

		AuthorityTermInfo authorityTermInfo = RefNameUtils.parseAuthorityTermInfo(itemRefName);
		String parentIdentifier = Specifier.createShortIdURNValue(authorityTermInfo.inAuthority.name);
		String itemIdentifier = Specifier.createShortIdURNValue(authorityTermInfo.name);

		// Use the Authority JAX-RS resource to peform sync operations (creates and updates).

		AuthorityResource authorityResource = (AuthorityResource) ctx.getResource();
		PoxPayloadOut localItemPayload = null;

		// Find the local item.

		try {
			localItemPayload = authorityResource.getAuthorityItemWithExistingContext(ctx, parentIdentifier, itemIdentifier);
		} catch (DocumentNotFoundException dnf) {
			localItemPayload = null;
		}

		// If no local item exists, create one.

		if (localItemPayload == null) {
			createLocalItem(ctx, parentCsid, parentIdentifier, itemIdentifier, AuthorityClient.INCLUDE_RELATIONS);

			return 1;
		}

		// Sync the local item with the remote item.

		PoxPayloadOut updatePayload = null;

		try {
			updatePayload = authorityResource.synchronizeItemWithExistingContext(ctx, parentIdentifier, itemIdentifier, AuthorityClient.INCLUDE_RELATIONS);
		} catch (DocumentReferenceException de) {
			logger.error(String.format("Could not sync item %s because it is referenced by other records", itemIdentifier));
		}

		if (updatePayload != null) {
			logger.info(String.format("Synced item %s in authority %s", itemIdentifier, parentIdentifier));

			return 0;
		}

		return -1;
	}

	private void assertStatusCode(Response res, Specifier specifier, AuthorityClient client) throws Exception {
		int statusCode = res.getStatus();

		if (statusCode != HttpStatus.SC_OK) {
			String errMsg = String.format("Could not retrieve authority information for '%s' on remote server '%s'.  Server returned status code %d",
					specifier.getURNValue(), client.getBaseURL(), statusCode);
			throw new DocumentException(statusCode, errMsg);
		}
	}

	/**
	 * Request an authority item list payload from the SAS server.
	 *
	 * @param ctx
	 * @param specifier
	 * @return
	 * @throws Exception
	 */
	private PoxPayloadIn requestItemList(ServiceContext ctx, Specifier specifier, long pageSize, long pageNum) throws Exception {
		PoxPayloadIn result = null;
		AuthorityClient client = (AuthorityClient) ctx.getClient();

		Response res = client.readItemList(
			specifier.getURNValue(),
			null, // partial term string
			null, // keyword string
			pageSize,
			pageNum
		);

		assertStatusCode(res, specifier, client);

		try {
			result = new PoxPayloadIn((String) res.readEntity(getEntityResponseType())); // Get the entire response.
		} finally {
			res.close();
		}

		return result;
	}

	/*
	 * Non standard injection of CSID into common part, since caller may access through
	 * shortId, and not know the CSID.
	 * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#extractPart(org.nuxeo.ecm.core.api.DocumentModel, java.lang.String, org.collectionspace.services.common.service.ObjectPartType)
	 */
	@Override
	protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
			throws Exception {
		Map<String, Object> unQObjectProperties = super.extractPart(docModel, schema, partMeta);

		// Add the CSID to the common part
		if (partMeta.getLabel().equalsIgnoreCase(authorityCommonSchemaName)) {
			String csid = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
			unQObjectProperties.put("csid", csid);
		}

		return unQObjectProperties;
	}

	public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {
		super.fillAllParts(wrapDoc, action);
		//
		// Update the record's revision number on both CREATE and UPDATE actions, but not on SYNC
		//
		if (this.getShouldUpdateRevNumber() == true) { // We won't update rev numbers on synchronization with SAS
			updateRevNumbers(wrapDoc);
		}
	}

	protected void updateRevNumbers(DocumentWrapper<DocumentModel> wrapDoc) {
		DocumentModel documentModel = wrapDoc.getWrappedObject();
		Long rev = (Long)documentModel.getProperty(authorityCommonSchemaName, AuthorityJAXBSchema.REV);
		if (rev == null) {
			rev = (long)0;
		} else {
			rev++;
		}
		documentModel.setProperty(authorityCommonSchemaName, AuthorityJAXBSchema.REV, rev);
	}

	/*
	 * We consider workflow state changes as changes that should bump the revision number
	 * (non-Javadoc)
	 * @see org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl#handleWorkflowTransition(org.collectionspace.services.common.document.DocumentWrapper, org.collectionspace.services.lifecycle.TransitionDef)
	 */
	@Override
	public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef) throws Exception {
		boolean updateRevNumber = this.getShouldUpdateRevNumber();
		Boolean contextProperty = (Boolean) ctx.getProperty(AuthorityServiceUtils.SHOULD_UPDATE_REV_PROPERTY);
		if (contextProperty != null) {
			updateRevNumber = contextProperty;
		}

		if (updateRevNumber == true) { // We don't update the rev number of synchronization requests
			updateRevNumbers(wrapDoc);
		}
	}

	@Override
	public void handleCreate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
		super.handleCreate(wrapDoc);
		// CSPACE-3178:
		// Uncomment once debugged and App layer is read to integrate
		// Experimenting with this uncommented now ...
		handleDisplayNameAsShortIdentifier(wrapDoc.getWrappedObject(), authorityCommonSchemaName);
		updateRefnameForAuthority(wrapDoc, authorityCommonSchemaName);//CSPACE-3178
	}

	protected String buildWhereForShortId(String name) {
		return authorityCommonSchemaName
				+ ":" + AuthorityJAXBSchema.SHORT_IDENTIFIER
				+ "='" + name + "'";
	}

	private boolean isUnique(DocumentModel docModel, String schemaName) throws DocumentException {
		return true;
	}

	private boolean temp_isUnique(DocumentModel docModel, String schemaName) throws DocumentException {
		boolean result = true;

		ServiceContext ctx = this.getServiceContext();
		String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
		String nxqlWhereClause = buildWhereForShortId(shortIdentifier);
		try {
			DocumentWrapper<DocumentModel> searchResultWrapper = getRepositoryClient(ctx).findDoc(ctx, nxqlWhereClause);
			if (searchResultWrapper != null) {
				result = false;
				if (logger.isInfoEnabled() == true) {
					DocumentModel searchResult = searchResultWrapper.getWrappedObject();
					String debugMsg = String.format("Could not create a new authority with a short identifier of '%s', because one already exists with the same short identifer: CSID = '%s'",
							shortIdentifier, searchResult.getName());
					logger.trace(debugMsg);
				}
			}
		} catch (DocumentNotFoundException e) {
			// Not a problem, just means we couldn't find another authority with that short ID
		}

		return result;
	}

	/**
	 * If no short identifier was provided in the input payload,
	 * generate a short identifier from the display name. Either way though,
	 * the short identifier needs to be unique.
	 */
	private void handleDisplayNameAsShortIdentifier(DocumentModel docModel, String schemaName) throws Exception {
		String shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
		String displayName = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.DISPLAY_NAME);
		String shortDisplayName = "";
		String generateShortIdentifier = null;
		if (Tools.isEmpty(shortIdentifier)) {
			generateShortIdentifier = AuthorityIdentifierUtils.generateShortIdentifierFromDisplayName(displayName, shortDisplayName);
			docModel.setProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER, shortIdentifier);
		}

		if (isUnique(docModel, schemaName) == false) {
			String shortId = generateShortIdentifier == null ? shortIdentifier : generateShortIdentifier;
			String errMsgVerb = generateShortIdentifier == null ? "supplied" : "generated";
			String errMsg = String.format("The %s short identifier '%s' was not unique, so the new authority could not be created.",
					errMsgVerb, shortId);
			throw new DocumentException(errMsg);
		}
	}

	/**
	 * Generate a refName for the authority from the short identifier
	 * and display name.
	 *
	 * All refNames for authorities are generated.  If a client supplies
	 * a refName, it will be overwritten during create (per this method)
	 * or discarded during update (per filterReadOnlyPropertiesForPart).
	 *
	 * @see #filterReadOnlyPropertiesForPart(Map<String, Object>, org.collectionspace.services.common.service.ObjectPartType)
	 *
	 */
	protected void updateRefnameForAuthority(DocumentWrapper<DocumentModel> wrapDoc, String schemaName) throws Exception {
		DocumentModel docModel = wrapDoc.getWrappedObject();
		RefName.Authority authority = (Authority) getRefName(getServiceContext(), docModel);
		String refName = authority.toString();
		docModel.setProperty(schemaName, AuthorityJAXBSchema.REF_NAME, refName);
	}

	@Override
	public RefName.RefNameInterface getRefName(ServiceContext ctx,
			DocumentModel docModel) {
		RefName.RefNameInterface refname = null;

		try {
			String shortIdentifier = (String) docModel.getProperty(authorityCommonSchemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
			String displayName = (String) docModel.getProperty(authorityCommonSchemaName, AuthorityJAXBSchema.DISPLAY_NAME);
			RefName.Authority authority = RefName.Authority.buildAuthority(ctx.getTenantName(),
					ctx.getServiceName(),
					null,	// Only use shortId form!!!
					shortIdentifier,
					displayName);
			refname = authority;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return refname;
	}

	@Override
	protected String getRefnameDisplayName(DocumentWrapper<DocumentModel> docWrapper) {
		String result = null;

		DocumentModel docModel = docWrapper.getWrappedObject();
		ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx = this.getServiceContext();
		RefName.Authority refname = (RefName.Authority)getRefName(ctx, docModel);
		result = refname.getDisplayName();

		return result;
	}

	public String getShortIdentifier(ServiceContext ctx, String authCSID, String schemaName) throws Exception {
		String shortIdentifier = null;
		CoreSessionInterface repoSession = null;
		boolean releaseSession = false;

		NuxeoRepositoryClientImpl nuxeoRepoClient = (NuxeoRepositoryClientImpl)this.getRepositoryClient(ctx);
		try {
			repoSession = nuxeoRepoClient.getRepositorySession(ctx);
			DocumentWrapper<DocumentModel> wrapDoc = nuxeoRepoClient.getDocFromCsid(ctx, repoSession, authCSID);
			DocumentModel docModel = wrapDoc.getWrappedObject();
			if (docModel == null) {
				throw new DocumentNotFoundException(String.format("Could not find authority resource with CSID='%s'.", authCSID));
			}
			shortIdentifier = (String) docModel.getProperty(schemaName, AuthorityJAXBSchema.SHORT_IDENTIFIER);
		} catch (ClientException ce) {
			throw new RuntimeException("AuthorityDocHandler Internal Error: cannot get shortId!", ce);
		} finally {
			if (repoSession != null) {
				nuxeoRepoClient.releaseRepositorySession(ctx, repoSession);
			}
		}

		return shortIdentifier;
	}

	/**
	 * Filters out selected values supplied in an update request.
	 *
	 * @param objectProps the properties filtered out from the update payload
	 * @param partMeta metadata for the object to fill
	 */
	@Override
	public void filterReadOnlyPropertiesForPart(
			Map<String, Object> objectProps, ObjectPartType partMeta) {
		super.filterReadOnlyPropertiesForPart(objectProps, partMeta);
		String commonPartLabel = getServiceContext().getCommonPartLabel();
		if (partMeta.getLabel().equalsIgnoreCase(commonPartLabel)) {
			objectProps.remove(AuthorityJAXBSchema.CSID);
			objectProps.remove(AuthorityJAXBSchema.SHORT_IDENTIFIER);
			objectProps.remove(AuthorityJAXBSchema.REF_NAME);
		}
	}

	@Override
	protected Object getListResultValue(DocumentModel docModel, // REM - CSPACE-5133
			String schema, ListResultField field) throws DocumentException {
		Object result = null;

		result = super.getListResultValue(docModel, schema, field);

		return result;
	}
}
