package org.collectionspace.services.common.document;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.service.ServiceBindingType;

import java.util.List;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public interface IInitHandler {
   public void onRepositoryInitialized(ServiceBindingType sbt, List<String> fields) throws Exception;
}
