package org.collectionspace.services.common.config;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;

public class PropertyItemUtils {

	/**
     * @param propNodeList the wrapping list node from JAXB
     * @param propName the property to fetch
     * @return the String value of the named property
     */
    public static String getPropertyValueByNameFromNodeList(List<PropertyType> propNodeList,
    		String propName) {
		if(propNodeList.isEmpty()) {
			return null;
		}
		return getPropertyValueByName(propNodeList.get(0).getItem(), propName);
    }
    
    public static List<PropertyItemType> getPropertyValueListByNameFromNodeList(List<PropertyType> propNodeList,
    		String propName) {
		if (propNodeList == null || propNodeList.isEmpty()) {
			return null;
		}

		ArrayList<PropertyItemType> result = new ArrayList<PropertyItemType>();
		for (PropertyItemType propItem :  propNodeList.get(0).getItem()) {
    		if (propName.equals(propItem.getKey())) {
    			result.add(propItem);
    		}
		}

		return result;
    }
	
	/**
     * @param propList the list of properties.
     * @param propName the property to fetch
     * @return the String value of the named property
     */
    public static String getPropertyValueByName(List<PropertyItemType> propList,
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
    
    /**
     * @param propNodeList the JAXB wrapping node of for the list to search for the named property
     * @param propName the name of the property of interest
     * @param qualPrefix a namespace qualifier prefix (with ':') to prepend, or null
     * @return a List of string values found for the named property
     */
    public static List<String> getPropertyValuesByNameInNodeList(
    		List<PropertyType> propNodeList, String propName, String qualPrefix) {
    	return getPropertyValuesByNameInNodeList(propNodeList, propName, qualPrefix, null);
    }
    
    /**
     * @param propNodeList the JAXB wrapping node of for the list to search for the named property
     * @param propName the name of the property of interest
     * @param qualPrefix a namespace qualifier prefix (with ':') to prepend, or null
     * @param values and existing list to append values to. If null, a new one will be created.
     * @return values, or that is null, a new List of string values found for the named property
     */
    public static List<String> getPropertyValuesByNameInNodeList(
    		List<PropertyType> propNodeList, String propName, String qualPrefix,
    		List<String> values) {
		if(propNodeList.isEmpty()) {
	    	if(values==null)
	    		values = new ArrayList<String>();
	    	return values;
		}
    	return getPropertyValuesByName(propNodeList.get(0).getItem(), 
    									propName, qualPrefix, values);
    }
    
    /**
     * @param propNodeList the Item list to search for the named property
     * @param propName the name of the property of interest
     * @param qualPrefix a namespace qualifier prefix (with ':') to prepend, or null
     * @return a List of string values found for the named property
     */
    public static List<String> getPropertyValuesByName(
    		List<PropertyItemType> propItems, String propName, String qualPrefix) {
    	return getPropertyValuesByName(propItems, propName, qualPrefix, null);
    }
    
    /**
     * @param propNodeList the Item list to search for the named property
     * @param propName the name of the property of interest
     * @param qualPrefix a namespace qualifier prefix (with ':') to prepend, or null
     * @param values and existing list to append values to. If null, a new one will be created.
     * @return values, or that is null, a new List of string values found for the named property
     */
    public static List<String> getPropertyValuesByName(
    		List<PropertyItemType> propItems, String propName, String qualPrefix,
    		List<String> values ) {
    	if(values==null)
    		values = new ArrayList<String>();
    	for(PropertyItemType propItem:propItems) {
    		if(propName.equals(propItem.getKey())) {
    			// TODO - the trim() belongs here, not down a few lines.
    			String value = propItem.getValue();
    			if(value!=null) {
    				values.add((qualPrefix!=null)?(qualPrefix+value):value.trim());
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
