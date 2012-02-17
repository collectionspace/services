package org.collectionspace.services.common.init;

import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.InitHandler.Params.Field;
import org.collectionspace.services.common.service.InitHandler.Params.Property;

import java.util.List;

import javax.sql.DataSource;

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
