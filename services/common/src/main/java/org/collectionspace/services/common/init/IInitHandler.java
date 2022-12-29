package org.collectionspace.services.common.init;

import java.util.List;

import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ServiceBindingType;

/**
 * User: laramie
 * $LastChangedRevision$
 * $LastChangedDate$
 */

public interface IInitHandler {
    public void onRepositoryInitialized(String dataSourceName,
    		String repositoryName,
    		String cspaceInstanceId,
    		String tenantShortName,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> property) throws Exception;
}
