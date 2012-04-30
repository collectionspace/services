package org.collectionspace.services.common.init;

import java.util.List;

import javax.sql.DataSource;

import org.collectionspace.services.config.service.InitHandler.Params.Field;
import org.collectionspace.services.config.service.InitHandler.Params.Property;
import org.collectionspace.services.config.service.ServiceBindingType;

/**
 * User: laramie
 * $LastChangedRevision$
 * $LastChangedDate$
 */

public interface IInitHandler {
    public void onRepositoryInitialized(DataSource dataSource,
    		ServiceBindingType sbt, 
    		List<Field> fields, 
    		List<Property> property) throws Exception;
}
