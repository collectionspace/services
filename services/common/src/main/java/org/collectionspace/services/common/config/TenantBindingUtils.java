package org.collectionspace.services.common.config;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;


public class TenantBindingUtils {

    public static final boolean SET_PROP_IF_MISSING = true;
    public static final boolean SET_PROP_ALWAYS = false;

    /**
     * @param tenantBinding
     * @param propName the property to fetch
     * @return the String value of the named property
     */
    public static String getPropertyValue(TenantBindingType tenantBinding,
            String propName) {
        if (propName == null) {
            throw new IllegalArgumentException("TenantBindingUtils.getPropertyValue: null property name!");
        }
        List<PropertyType> tenantPropList = tenantBinding.getProperties();
        return PropertyItemUtils.getPropertyValueByNameFromNodeList(tenantPropList, propName);
    }

    /**
     * Gets the values of a configured property for a tenant.
     *
     * @param tenantBinding a tenant binding
     * @param propName the property to fetch (can return multiple values for this property).
     * @return a list of values for the supplied property.
     */
    public static List<String> getPropertyValues(TenantBindingType tenantBinding,
            String propName) {
        List<String> propValues = null;
        List<PropertyType> propList = tenantBinding.getProperties();
        if (propList != null && propList.size() > 0) {
            List<PropertyItemType> propItems = propList.get(0).getItem();
            for (PropertyItemType propItem : propItems) {
                if (propName.equals(propItem.getKey())) {
                    String value = propItem.getValue();
                    if (value != null) {
                        if (propValues == null) {
                            propValues = new ArrayList<String>();
                        }
                        propValues.add(value);
                    }
                }
            }
        }
        return propValues;
    }

    /**
     * @param service
     * @param propName the property to fetch
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static boolean setPropertyValue(TenantBindingType tenantBinding,
            PropertyItemType prop, boolean onlyIfNotSet) {
        if (prop == null) {
            throw new IllegalArgumentException(
                    "TenantBindingUtils.setPropertyValue: null property!");
        }
        return setPropertyValue(tenantBinding, prop.getKey(), prop.getValue(),
                onlyIfNotSet);
    }

    /**
     * @param tenantBinding
     * @param propName the property to fetch
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static boolean setPropertyValue(TenantBindingType tenantBinding,
            String propName, String value,
            boolean onlyIfNotSet) {
        boolean valueFound = false;
        boolean valueSet = false;
        if (propName == null) {
            throw new IllegalArgumentException("TenantBindingUtils.setPropertyValue: null property name!");
        }
        List<PropertyType> tenantPropertiesNode = tenantBinding.getProperties();
        return PropertyItemUtils.setPropertyValueInNodeList(tenantPropertiesNode,
                propName, value, onlyIfNotSet);
    }

    /**
     * @param tenantBinding
     * @param propName the property to fetch
     * @param value the new value to set
     * @param onlyIfNotSet if true, will not override an existing value
     * @return true if set, false if an existing value was left as is.
     */
    public static void propagatePropertiesToServices(
            TenantBindingType tenantBinding, boolean onlyIfNotSet) {
        List<PropertyItemType> tenantPropList =
                tenantBinding.getProperties().get(0).getItem();
        for (PropertyItemType tenantPropItem : tenantPropList) {
            for (ServiceBindingType service : tenantBinding.getServiceBindings()) {
                ServiceBindingUtils.setPropertyValue(service,
                        tenantPropItem, onlyIfNotSet);
            }
        }
    }
}
