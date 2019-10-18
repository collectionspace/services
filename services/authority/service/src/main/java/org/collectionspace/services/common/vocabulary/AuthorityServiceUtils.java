package org.collectionspace.services.common.vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentReferenceException;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthorityItemSpecifier;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.Specifier;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.RemoteClientConfig;
import org.collectionspace.services.config.tenant.RemoteClientConfigurations;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class AuthorityServiceUtils {
	private static final Logger logger = LoggerFactory.getLogger(AuthorityServiceUtils.class);
	//
	// Used to keep track if an authority item's is deprecated
	public static final String DEFAULT_REMOTECLIENT_CONFIG_NAME = "default";
	public static final String IS_DEPRECATED_PROPERTY = "IS_DEPRECATED_PROPERTY";
	public static final Boolean DEPRECATED = true;
	public static final Boolean NOT_DEPRECATED = !DEPRECATED;

	// Used to keep track if an authority item's rev number should be updated
	public static final String SHOULD_UPDATE_REV_PROPERTY = "SHOULD_UPDATE_REV_PROPERTY";
	public static final boolean UPDATE_REV = true;
	public static final boolean DONT_UPDATE_REV = !UPDATE_REV;

	public static final boolean ROLLBACK_ON_EXCEPTION = true;
	public static final boolean DONT_ROLLBACK_ON_EXCEPTION = !ROLLBACK_ON_EXCEPTION;

	// Used to keep track if an authority item is a locally proposed member of a SAS authority
	public static final String IS_PROPOSED_PROPERTY = "IS_PROPOSED";
	public static final Boolean PROPOSED = true;
	public static final Boolean NOT_PROPOSED = !PROPOSED;
	public static final Boolean SAS_ITEM = true;
	public static final Boolean NOT_SAS_ITEM = !SAS_ITEM;

	public static final Boolean NO_CHANGE = null;

	// Matches the domain name part of a refname. For example, "core.collectionspace.org" of
	// urn:cspace:core.collectionspace.org:personauthorities:name(person):item:name(BigBird1461101206103)'Big Bird'.
	public static final	Pattern REFNAME_DOMAIN_PATTERN = Pattern.compile("(urn:cspace:)(([a-z]{1,}\\.?)*)");

	/*
	 * Try to find a named remote client configuration in the current tenant bindings.  If the value of the incoming param 'remoteClientConfigName' is
	 * blank or null, we'll try to find a name in the authority service's bindings.  If we can't find a name there, we'll try using the default name.
	 *
	 * If the incoming param 'remoteClientConfigName' is not null, we'll look through all the named remote client configurations in the tenant's binding
	 * to find the configuration.  If we can't find the named configuration, we'll throw an exception.
	 *
	 * If there are no remote client configurations in the tenant's bindings, we'll throw an exception.
	 */
	public static final RemoteClientConfig getRemoteClientConfig(ServiceContext ctx, String remoteClientConfigName) throws Exception {
		RemoteClientConfig result = null;

		TenantBindingType tenantBinding = ServiceMain.getInstance().getTenantBindingConfigReader().getTenantBinding(ctx.getTenantId());
		RemoteClientConfigurations remoteClientConfigurations = tenantBinding.getRemoteClientConfigurations();
		if (remoteClientConfigurations != null) {
			if (Tools.isEmpty(remoteClientConfigName) == true) {
				// Since the authority instance didn't specify a remote client config name, let's see if the authority type's service bindings specifies one
				ServiceBindingType serviceBindingType =
						ServiceMain.getInstance().getTenantBindingConfigReader().getServiceBinding(ctx.getTenantId(), ctx.getServiceName());
				remoteClientConfigName = serviceBindingType.getRemoteClientConfigName();
			}
			//
			// If we still don't have a remote client config name, let's use the default value.
			//
			if (Tools.isEmpty(remoteClientConfigName) == true) {
				remoteClientConfigName = DEFAULT_REMOTECLIENT_CONFIG_NAME;
			}

			List<RemoteClientConfig> remoteClientConfigList = remoteClientConfigurations.getRemoteClientConfig();
			for (RemoteClientConfig config : remoteClientConfigList) {
				if (config.getName().equalsIgnoreCase(remoteClientConfigName)) {
					result = config;
					break;
				}
			}
		} else {
			String errMsg = String.format("No remote client configurations could be found in the tenant bindings for tenant named '%s'.",
					ctx.getTenantName());
			logger.error(errMsg);
			throw new Exception(errMsg);
		}

		if (result == null) {
			String errMsg = String.format("Could not find a remote client configuration named '%s' in the tenant bindings for tenant named '%s'",
					remoteClientConfigName, ctx.getTenantName());
			logger.error(errMsg);
			throw new Exception(errMsg);
		}

		return result;
	}

	/**
	 * Make a request to the SAS Server for an authority payload.
	 *
	 * @param ctx
	 * @param specifier
	 * @param responseType
	 * @return
	 * @throws Exception
	 */
	public static PoxPayloadIn requestPayloadInFromRemoteServer(ServiceContext ctx, String remoteClientConfigName, Specifier specifier, Class responseType) throws Exception {
		PoxPayloadIn result = null;

		RemoteClientConfig remoteClientConfig = getRemoteClientConfig(ctx, remoteClientConfigName);
		AuthorityClient client = (AuthorityClient) ctx.getClient(remoteClientConfig);

		Response res = client.read(specifier.getURNValue());
		try {
			int statusCode = res.getStatus();
			if (statusCode == org.apache.commons.httpclient.HttpStatus.SC_OK) {
				result = new PoxPayloadIn((String)res.readEntity(responseType)); // Get the entire response!
			} else {
				String errMsg = String.format("Could not retrieve authority information for '%s' on remote server '%s'.  Server returned status code %d",
						specifier.getURNValue(), remoteClientConfig.getUrl(), statusCode);
				if (logger.isDebugEnabled()) {
					logger.debug(errMsg);
				}
				throw new DocumentException(statusCode, errMsg);
			}
		} finally {
			res.close();
		}

		return result;
	}

	//
	// Makes a call to the remote SAS server for a authority item payload
	//
	public static PoxPayloadIn requestPayloadInFromRemoteServer(
			AuthorityItemSpecifier specifier,
			String remoteClientConfigName,
			String serviceName,
			Class responseType,
			boolean syncHierarchicalRelationships) throws Exception {
		PoxPayloadIn result = null;

		ServiceContext authorityCtx = new MultipartServiceContextImpl(serviceName);
		RemoteClientConfig remoteClientConfig = getRemoteClientConfig(authorityCtx, remoteClientConfigName);
		AuthorityClient client = (AuthorityClient) authorityCtx.getClient(remoteClientConfig);
		Response res = client.readItem(specifier.getParentSpecifier().getURNValue(), specifier.getItemSpecifier().getURNValue(),
				AuthorityClient.INCLUDE_DELETED_ITEMS, syncHierarchicalRelationships);

		try {
			int statusCode = res.getStatus();
			if (statusCode == org.apache.commons.httpclient.HttpStatus.SC_OK) {
				result = new PoxPayloadIn((String)res.readEntity(responseType)); // Get the entire response.
			} else {
				String errMsg = String.format("Could not retrieve authority item information for '%s:%s' on remote server '%s'.  Server returned status code %d",
						specifier.getParentSpecifier().getURNValue(), specifier.getItemSpecifier().getURNValue(), remoteClientConfig.getUrl(), statusCode);
				if (logger.isDebugEnabled()) {
					logger.debug(errMsg);
				}
				throw new DocumentException(statusCode, errMsg);
			}
		} finally {
			res.close();
		}

		return result;
	}

	/*
	 * The domain name part of refnames on a remote SAS may not match that of local refnames.
	 * Update all the payload's refnames with the local domain name.
	 */
	public static PoxPayloadIn localizeRefNameDomains(ServiceContext ctx, PoxPayloadIn payload) throws org.dom4j.DocumentException {
		String localDomain = ctx.getTenantName();
		Matcher matcher = REFNAME_DOMAIN_PATTERN.matcher(payload.getXmlPayload());
		StringBuffer localizedXmlBuffer = new StringBuffer();

		while (matcher.find() == true) {
			String remoteDomain = matcher.group(2);

			if (logger.isDebugEnabled()) {
				logger.debug("Replacing " + remoteDomain + " with " + localDomain);
			}

			matcher.appendReplacement(localizedXmlBuffer, matcher.group(1) + localDomain);
		}

		matcher.appendTail(localizedXmlBuffer);

		if (logger.isTraceEnabled()) {
			logger.trace(String.format("Updated payload:\n%s", localizedXmlBuffer));
		}

		return new PoxPayloadIn(localizedXmlBuffer.toString());
	}

	/**
	 * Localizes the relations list in an authority item payload from a remote server during SAS sync.
	 *
	 * Relations to items that do not exist in the local authority are removed. If the related item
	 * doesn't exist locally because it is new on the remote and hasn't been synced yet, the relations
	 * will be created when the new item is synced, because the new item's relations list will include
	 * them.
	 *
	 * The following elements are removed from each relation: uri, csid, subjectCsid, objectCsid,
	 * object/csid, object/uri, subject/csid, subject/uri. These apply only to the remote. By removing
	 * them, the relation will be created locally if necessary.
	 *
	 * @param ctx
	 * @param authorityResource
	 * @param parentCsid
	 * @param itemSpecifier
	 * @param payload
	 * @return
	 * @throws Exception
	 */
	public static PoxPayloadIn localizeRelations(ServiceContext ctx, AuthorityResource authorityResource, String parentCsid, Specifier itemSpecifier, PoxPayloadIn payload) throws Exception {
		// TODO: Relations to items that don't exist need to be removed, because a create/update will fail
		// if the subject/object of any supplied relation can't be found. Consider changing the create/update
		// code to ignore any relations to items that don't exist, but still save the record and any other
		// relations. This will speed up sync when many items have relations, since the checks to see if
		// all related items exist locally can be skipped.

		String itemShortId = itemSpecifier.value;
		Document document = payload.getDOMDocument();

		Map<String, String> namespaceUris = new HashMap<String, String>();
		namespaceUris.put("rel", "http://collectionspace.org/services/relation");

		XPath xPath = DocumentHelper.createXPath("//rel:relations-common-list/relation-list-item");
		xPath.setNamespaceURIs(namespaceUris);

		List<Node> listItemNodes = xPath.selectNodes(document);

		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Found %d relation list items", listItemNodes.size()));
		}

		for (Node listItemNode : listItemNodes) {
			String objectRefName = listItemNode.selectSingleNode("object/refName").getText();
			AuthorityTermInfo objectTermInfo = RefNameUtils.parseAuthorityTermInfo(objectRefName);
			String objectShortId = objectTermInfo.name;

			if (
				!objectShortId.equals(itemShortId)
				&& !checkItemExists(ctx, authorityResource, parentCsid, Specifier.createShortIdURNValue(objectShortId))
			) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Omitting remote relation: object with short id %s does does not exist locally", objectShortId));
				}

				listItemNode.detach();
				continue;
			}

			String subjectRefName = listItemNode.selectSingleNode("subject/refName").getText();
			AuthorityTermInfo subjectTermInfo = RefNameUtils.parseAuthorityTermInfo(subjectRefName);
			String subjectShortId = subjectTermInfo.name;

			if (
				!subjectShortId.equals(itemShortId)
				&& !checkItemExists(ctx, authorityResource, parentCsid, Specifier.createShortIdURNValue(subjectShortId))
			) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Omitting remote relation: subject with short id %s does does not exist locally", subjectShortId));
				}

				listItemNode.detach();
				continue;
			}

			listItemNode.selectSingleNode("csid").detach();
			listItemNode.selectSingleNode("objectCsid").detach();
			listItemNode.selectSingleNode("subjectCsid").detach();
			listItemNode.selectSingleNode("uri").detach();
			listItemNode.selectSingleNode("object/csid").detach();
			listItemNode.selectSingleNode("object/uri").detach();
			listItemNode.selectSingleNode("subject/csid").detach();
			listItemNode.selectSingleNode("subject/uri").detach();
		}

		String xml = document.asXML();

		if (logger.isTraceEnabled()) {
			logger.trace("Prepared remote relations:\n" + xml);
		}

		return new PoxPayloadIn(xml);
	}

	/**
	 * Check if an item with a given short ID exists in a parent.
	 *
	 * @param ctx
	 * @param authorityResource
	 * @param parentCsid
	 * @param itemSpecifier
	 * @return true if the item exists, false otherwise.
	 * @throws Exception
	 */
	public static boolean checkItemExists(ServiceContext ctx, AuthorityResource authorityResource, String parentCsid, String itemSpecifier) throws Exception {
		String itemCsid = null;

		try {
			itemCsid = authorityResource.lookupItemCSID(ctx, itemSpecifier, parentCsid, "checkItemExists()", "CHECK_ITEM_EXISTS");
		} catch (DocumentNotFoundException e) {
			itemCsid = null;
		}

		return (itemCsid != null);
	}

	public static boolean syncWorkflowState(
			ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
			AuthorityResource authorityResource,
			String sasWorkflowState,
			String localParentCsid,
			String localItemCsid,
			DocumentModel localItemDocModel) throws Exception {
		String localItemWorkflowState = localItemDocModel.getCurrentLifeCycleState();
		List<String> transitionList = AuthorityServiceUtils.getTransitionList(sasWorkflowState, localItemWorkflowState);

		if (!transitionList.isEmpty()) {
			try {
				// Transition the local item to the new workflow state. This might involve multiple transitions.

				for (String transition : transitionList) {
					authorityResource.updateItemWorkflowWithTransition(ctx, localParentCsid, localItemCsid, transition, AuthorityServiceUtils.DONT_UPDATE_REV, AuthorityServiceUtils.DONT_ROLLBACK_ON_EXCEPTION);
				}
			} catch (DocumentReferenceException de) {
				logger.info(String.format("Failed to soft-delete %s (transition from %s to %s): item is referenced, and will be deprecated instead", localItemCsid, localItemWorkflowState, sasWorkflowState));

				// One or more of the transitions may have succeeded, so refresh the document model to make sure it
				// reflects the current workflow state.
				localItemDocModel.refresh();

				AuthorityServiceUtils.setAuthorityItemDeprecated(ctx, authorityResource, localParentCsid, localItemCsid, localItemDocModel);
			}
		}

		return true;
	}

	/**
	 * Mark the authority item as deprecated.
	 *
	 * @param ctx
	 * @param itemInfo
	 * @throws Exception
	 */
	public static boolean setAuthorityItemDeprecated(ServiceContext ctx, AuthorityResource authorityResource, String authorityItemCommonSchemaName, AuthorityItemSpecifier authorityItemSpecifier) throws Exception {
		DocumentModel docModel = NuxeoUtils.getDocFromSpecifier(ctx, (CoreSessionInterface)ctx.getCurrentRepositorySession(),
				authorityItemCommonSchemaName, authorityItemSpecifier);

		return setAuthorityItemDeprecated(ctx, authorityResource, authorityItemSpecifier.getParentSpecifier().value, authorityItemSpecifier.getItemSpecifier().getURNValue(), docModel);
	}

	public static boolean setAuthorityItemDeprecated(ServiceContext ctx, AuthorityResource authorityResource, String parentIdentifier, String itemIdentifier, DocumentModel docModel) throws Exception {
		String currentWorkflowState = docModel.getCurrentLifeCycleState();

		// Find the transitions needed to get from the current state to the replicated deprecated state.
		List<String> transitions = AuthorityServiceUtils.getTransitionList(WorkflowClient.WORKFLOWSTATE_DEPRECATED, currentWorkflowState);

		if (!transitions.isEmpty()) {
			for (String transition : transitions) {
				authorityResource.updateItemWorkflowWithTransition(ctx, parentIdentifier, itemIdentifier, transition, AuthorityServiceUtils.DONT_UPDATE_REV);
			}
		}

		return true;
	}

	/**
	 * We need to change the local item's state to one that maps to the replication server's workflow
	 * state.  This might involve making multiple transitions.
	 *
	 * WIKI:
	 *	 See table at https://collectionspace.atlassian.net/wiki/spaces/SDR/pages/665886940/Workflow+transitions+to+map+SAS+item+states+to+Local+item+states
	 *	 (was https://wiki.collectionspace.org/pages/viewpage.action?pageId=162496564)
	 *
	 */
	public static List<String> getTransitionList(String sasWorkflowState, String localItemWorkflowState) throws DocumentException {
		List<String> result = new ArrayList<String>();
		//
		// The first set of conditions maps a replication-server "project" state to a local client state of "replicated"
		//
		if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
			// Do nothing.  We're good with this state
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
		//
		// The second set of conditions maps a replication-server "deleted" state to a local client state of "deleted"
		//
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
			// Do nothing.  We're good with this state
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
		//
		// The third set of conditions maps a replication-server "replicated" state to a local state of "replicated"
		//
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
			// Do nothing.  We're good with this state
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
		//
		// The fourth set of conditions maps a replicatation-server "deprecated" state to a local state of "replicated_deprecated"
		//
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DEPRECATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DEPRECATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DEPRECATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DEPRECATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DEPRECATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_DEPRECATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DEPRECATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DEPRECATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DEPRECATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED)) {
			// Do nothing.
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DEPRECATED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDELETE);
		//
		// The last set of conditions maps a replication-server "replicated_deleted" state to a local client state of "deleted"
		//
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_PROJECT)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_REPLICATE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED)) {
			// Do nothing.  We're good with this state
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
			result.add(WorkflowClient.WORKFLOWTRANSITION_DELETE);
		} else if (sasWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED) && localItemWorkflowState.equals(WorkflowClient.WORKFLOWSTATE_REPLICATED_DEPRECATED_DELETED)) {
			result.add(WorkflowClient.WORKFLOWTRANSITION_UNDEPRECATE);
		} else {
			//
			// If we get here, we've encountered a SAS workflow state that we don't recognize.
			//
			throw new DocumentException(String.format("Encountered an invalid workflow state of '%s' on a SAS authority item.", sasWorkflowState));
		}

		return result;
	}
}
