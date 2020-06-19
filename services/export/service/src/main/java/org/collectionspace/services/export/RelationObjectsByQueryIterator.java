package org.collectionspace.services.export;

import java.util.Iterator;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;
import org.collectionspace.services.relation.RelationsDocListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationObjectsByQueryIterator extends RelationsByQueryIterator implements Iterator<PoxPayloadOut> {
	private final Logger logger = LoggerFactory.getLogger(RelationObjectsByQueryIterator.class);

  RelationObjectsByQueryIterator(
    ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
    String docType,
    String vocabulary,
    InvocationContext.Query query) throws Exception {

    super(serviceContext, docType, vocabulary, query);
  }

  @Override
  protected PoxPayloadOut getDocument(RelationListItem item) {
    RelationsDocListItem relationObject = item.getObject();

    String relationObjectCsid = relationObject.getCsid();
    String relationObjectDocType = relationObject.getDocumentType();

		TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
    ServiceBindingType relationObjectServiceBinding = tenantBindingConfigReader.getServiceBindingForDocType(serviceContext.getTenantId(), relationObjectDocType);
    String relationObjectServiceType = relationObjectServiceBinding.getType();
    String relationObjectServiceName = relationObjectServiceBinding.getName();

    boolean relationObjectIsAuthorityItem = ServiceBindingUtils.SERVICE_TYPE_AUTHORITY.equals(relationObjectServiceType);

    try {
      NuxeoBasedResource relationObjectResource = relationObjectIsAuthorityItem
        ? AuthorityResource.getResourceForItem(serviceContext.getResourceMap(), serviceContext.getTenantId(), relationObjectDocType)
        : (NuxeoBasedResource) serviceContext.getResource(relationObjectServiceName.toLowerCase());

      return (relationObjectIsAuthorityItem
        ? ((AuthorityResource<?, ?>) relationObjectResource).getAuthorityItemWithExistingContext(serviceContext, AuthorityResource.PARENT_WILDCARD, relationObjectCsid)
        : relationObjectResource.getWithParentCtx(serviceContext, relationObjectCsid));
    }
    catch (Exception e) {
      logger.warn("Could not get document with csid " + relationObjectCsid, e);

      return null;
    }
  }
}
