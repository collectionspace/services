package org.collectionspace.services.common.config;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.common.types.PropertyItemType;
import org.collectionspace.services.common.types.PropertyType;

public class PropertyItemUtils {

	/**
     * @param propNodeList the wrapping list node from JAXB
     * @param propName the property to fetch
     * @return the String value of the named property
     */
    public static String getPropertyValueFromNodeList(List<PropertyType> propNodeList,
    		String propName) {
		if(propNodeList.isEmpty()) {
			return null;
		}
		return getPropertyValue(propNodeList.get(0).getItem(), propName);
    }
    	
	
	/**
     * @param propList the list of properties.
     * @param propName the property to fetch
     * @return the String value of the named property
     */
    public static String getPropertyValue(List<PropertyItemType> propList,
    		String propName) {
    	if(propName==null) {
    		throw new IllegalArgumentException("PropertyItemUtils.getPropertyValues: null property name!");
    	}
    	for(PropertyItemType propItem:propList) {
    		if(propName.equals(propItem.getKey())) {
    			return propItem.getValue();
    		}
    	}
    	return null;
    }
    
    public static List<String> getPropertyValuesByNameInNodeList(
    		List<PropertyType> propNodeList, String propName) {
    	return getPropertyValuesByNameInNodeList(propNodeList, propName, null);
    }
    
    public static List<String> getPropertyValuesByNameInNodeList(
    		List<PropertyType> propNodeList, String propName, List<String> values) {
		if(propNodeList.isEmpty()) {
	    	if(values==null)
	    		return new ArrayList<String>();
		}
    	return getPropertyValuesByName(propNodeList.get(0).getItem(), propName, null);
    }
    
    public static List<String> getPropertyValuesByName(
    		List<PropertyItemType> propItems, String propName) {
    	return getPropertyValuesByName(propItems, propName, null);
    }
    
    public static List<String> getPropertyValuesByName(
    		List<PropertyItemType> propItems, String propName,
    		List<String> values ) {
    	if(values==null)
    		values = new ArrayList<String>();
    	for(PropertyItemType propItem:propItems) {
    		if(propName.equals(propItem.getKey())) {
    			String value = propItem.getValue();
    			if(value!=null) {
    				values.add(value);
    			}
    		}
    	}
    	return values;
    }

    /**
     * @param propNodeList the wrapping list node from JAXB
     * @param propName the property to set
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static boolean setPropertyValueInNodeList(List<PropertyType> propNodeList,
    		String propName, String value,
    		boolean onlyIfNotSet) {
		if(propNodeList.isEmpty()) {
			propNodeList.add(new PropertyType());
		}
		List<PropertyItemType> propList = propNodeList.get(0).getItem();
		return setPropertyValue(propList, propName, value, onlyIfNotSet);
    }
   
    /**
     * @param propName the property to set
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static boolean setPropertyValue(List<PropertyItemType> propList,
    		String propName, String value,
    		boolean onlyIfNotSet) {
    	boolean valueFound = false;
    	boolean valueSet = false;
    	if(propName==null) {
    		throw new IllegalArgumentException("ServiceBindingUtils.setPropertyValue: null property name!");
    	}
    	for(PropertyItemType propItem:propList) {
    		if(propName.equals(propItem.getKey())) {
    			if(!onlyIfNotSet) {
    				propItem.setValue(value);
    				valueSet = true;
    			}
    			// whether we set it or not, we found it, so break;
				valueFound = true;
				break;
    		}
    	}
    	if(!valueFound) {
    		PropertyItemType propItem = new PropertyItemType();
    		propItem.setKey(propName);
			propItem.setValue(value);
			propList.add(propItem);
			valueSet = true;
    	}
    	return valueSet;
    }
    
}
