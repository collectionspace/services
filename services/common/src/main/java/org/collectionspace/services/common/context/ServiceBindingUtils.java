package org.collectionspace.services.common.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.config.PropertyItemUtils;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.collectionspace.services.common.types.PropertyItemType;
import org.collectionspace.services.common.types.PropertyType;

public class ServiceBindingUtils {

    public static void getPartsMetadata(ServiceBindingType serviceBinding, 
    		Map<String, ObjectPartType> objectPartMap) {
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
            objectPartMap.put(objectPartType.getLabel(), objectPartType);
        }
    }
    
    public static List<PropertyItemType> getPropertiesForPart(ServiceBindingType serviceBinding,
    		String partLabel) {
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
            if(partLabel.equals(objectPartType.getLabel())) {
            	List<PropertyType> propNodeList = objectPartType.getProperties();
            	return propNodeList.isEmpty()?null:propNodeList.get(0).getItem();
            }
        }
   		throw new RuntimeException("No such part found: "+partLabel);
    }

    public static List<String> getPropertyValuesForPart(ServiceBindingType serviceBinding,
    		String partLabel, String propName) {
    	List<PropertyItemType> partProps = getPropertiesForPart(serviceBinding, partLabel);
    	return PropertyItemUtils.getPropertyValuesByName(partProps, propName);
    }

	public static List<String> getAllPartsPropertyValues(ServiceBindingType serviceBinding,
    		String propName) {
    	List<String> values = new ArrayList<String>();
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
        	List<PropertyType> propNodeList = objectPartType.getProperties();
        	PropertyItemUtils.getPropertyValuesByNameInNodeList(propNodeList, propName, values);
        }
    	return values;
    }

    /**
     * @param service
     * @param propName the property to fetch
     * @return the String value of the named property
     */
    public static String getPropertyValue(ServiceBindingType service,
    		String propName) {
    	if(propName==null) {
    		throw new IllegalArgumentException("ServiceBindingUtils.getPropertyValues: null property name!");
    	}
		List<PropertyType> servicePropList = service.getProperties();
		return PropertyItemUtils.getPropertyValueFromNodeList(servicePropList, propName );
    }
    
    /**
     * @param service
     * @param propName the property to set
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static boolean setPropertyValue(ServiceBindingType service,
    		PropertyItemType prop, boolean onlyIfNotSet) {
    	if(prop==null) {
    		throw new IllegalArgumentException(
    				"ServiceBindingUtils.setPropertyValue: null property!");
    	}
    	return setPropertyValue(service, prop.getKey(), prop.getValue(),
    								onlyIfNotSet);
    }
    
    /**
     * @param service
     * @param propName the property to set
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static boolean setPropertyValue(ServiceBindingType service,
    		String propName, String value,
    		boolean onlyIfNotSet) {
    	if(propName==null) {
    		throw new IllegalArgumentException("ServiceBindingUtils.setPropertyValue: null property name!");
    	}
		List<PropertyType> servicePropertiesNode = service.getProperties();
		return PropertyItemUtils.setPropertyValueInNodeList(servicePropertiesNode,
				propName, value, onlyIfNotSet);
    }
    
}
