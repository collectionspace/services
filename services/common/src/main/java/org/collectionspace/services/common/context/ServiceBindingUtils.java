package org.collectionspace.services.common.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
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
    
    public static List<PropertyType> getPropertiesForPart(ServiceBindingType serviceBinding,
    		String partLabel) {
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
            if(partLabel.equals(objectPartType.getLabel())) {
            	return objectPartType.getProperties();
            }
        }
   		throw new RuntimeException("No such part found: "+partLabel);
    }

    public static List<String> getPropertyValuesForPart(ServiceBindingType serviceBinding,
    		String partLabel, String propName) {
    	List<PropertyType> partProps = getPropertiesForPart(serviceBinding, partLabel);
    	return getPropertyValuesByName(partProps, propName);
    }

    public static List<String> getPropertyValuesByName(List<PropertyType> partProps, String propName) {
    	List<String> values = new ArrayList<String>();
    	if(partProps.size()>0) {
        	List<PropertyItemType> propItems = partProps.get(0).getItem();
        	for(PropertyItemType propItem:propItems) {
        		if(propName.equals(propItem.getKey())) {
        			String value = propItem.getValue();
        			if(value!=null) {
        				values.add(value);
        			}
        		}
        	}
    	}
    	return values;
    }

	public static List<String> getPropertyValues(ServiceBindingType serviceBinding,
    		String propName) {
    	List<String> values = new ArrayList<String>();
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
        	List<PropertyType> partProps = objectPartType.getProperties();
        	if(partProps.size()>0) {
            	List<PropertyItemType> propItems = partProps.get(0).getItem();
            	for(PropertyItemType propItem:propItems) {
            		if(propName.equals(propItem.getKey())) {
            			String value = propItem.getValue();
            			if(value!=null) {
            				values.add(value);
            			}
            		}
            	}
        	}
        }
    	return values;
    }


}
