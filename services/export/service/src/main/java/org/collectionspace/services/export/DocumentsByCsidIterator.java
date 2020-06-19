package org.collectionspace.services.export;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentsByCsidIterator implements Iterator<PoxPayloadOut> {
	private final Logger logger = LoggerFactory.getLogger(DocumentsByCsidIterator.class);

  private NuxeoBasedResource resource;
  private String vocabulary;
  private ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext;
  private Iterator<String> csidIterator;
  private boolean isAuthorityItem = false;

  DocumentsByCsidIterator(
    ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
    String docType,
    String vocabulary,
    List<String> csids) throws Exception {

    TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
    ServiceBindingType serviceBinding = tenantBindingConfigReader.getServiceBindingForDocType(serviceContext.getTenantId(), docType);
    String serviceType = serviceBinding.getType();
    String serviceName = serviceBinding.getName();

    this.serviceContext = serviceContext;
    this.csidIterator = csids.iterator();
    this.isAuthorityItem = ServiceBindingUtils.SERVICE_TYPE_AUTHORITY.equals(serviceType);
    this.vocabulary = vocabulary;

    this.resource = isAuthorityItem
      ? AuthorityResource.getResourceForItem(serviceContext.getResourceMap(), serviceContext.getTenantId(), docType)
      : (NuxeoBasedResource) serviceContext.getResource(serviceName.toLowerCase());
  }

  @Override
  public boolean hasNext() {
    return csidIterator.hasNext();
  }

  @Override
  public PoxPayloadOut next() {
    String csid = csidIterator.next();

    try {
      return (isAuthorityItem
        ? ((AuthorityResource<?, ?>) resource).getAuthorityItemWithExistingContext(serviceContext, vocabulary == null ? AuthorityResource.PARENT_WILDCARD : vocabulary, csid)
        : resource.getWithParentCtx(serviceContext, csid));
    }
    catch (Exception e) {
      logger.warn("Could not get document with csid " + csid, e);

      return null;
    }
  }
}
