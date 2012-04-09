package org.collectionspace.services.common.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.config.PropertyItemUtils;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import java.lang.IndexOutOfBoundsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceBindingUtils {
	public static final boolean QUALIFIED_PROP_NAMES = true;
	public static final boolean UNQUALIFIED_PROP_NAMES = false;
	public static final String AUTH_REF_PROP = "authRef";
	public static final String TERM_REF_PROP = "termRef";
	public static final String OBJ_NUMBER_PROP = "objectNumberProperty";
	public static final String OBJ_NAME_PROP = "objectNameProperty";
	public static final String SERVICE_TYPE_PROP = "type";
	public static final String SERVICE_TYPE_OBJECT = "object";
	public static final String SERVICE_TYPE_PROCEDURE = "procedure";
	public static final String SERVICE_TYPE_AUTHORITY = "authority";
	public static final String SERVICE_TYPE_UTILITY = "utility";
	public static final String SERVICE_TYPE_SECURITY = "security";

	private static final Logger logger = LoggerFactory.getLogger(ServiceBindingUtils.class);
	
    public static String getTenantQualifiedDocType(String tenantId, String docType) {
    	String result = docType + ServiceContext.TENANT_SUFFIX + tenantId;
    	return result;
    }
    	
	// TODO consider building up a hashTable of the properties for each
	// service binding. There will be generic properties, as well as
	// properties on each part. Could build up a key from tenant id, 
	// servicename, (partname for those props), propName

    public static void getPartsMetadata(ServiceBindingType serviceBinding, 
    		Map<String, ObjectPartType> objectPartMap) {
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
            objectPartMap.put(objectPartType.getLabel(), objectPartType);
        }
    }
    
    private static List<PropertyItemType> getPropertiesForPart(ServiceBindingType serviceBinding,
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
    		String partLabel, String propName, boolean qualify) {
    	List<PropertyItemType> partProps = getPropertiesForPart(serviceBinding, partLabel);
    	return PropertyItemUtils.getPropertyValuesByName(partProps, propName, 
    													(qualify?(partLabel+":"):null));
    }

	/**
	 * @param serviceBinding the service to work from
	 * @param propName the name of the property of interest
	 * @param qualify if QUALIFIED_PROP_NAMES, will prefix all values with the part label
	 * @return a list of (qualified)
	 */
	public static List<String> getAllPartsPropertyValues(ServiceBindingType serviceBinding,
    		String propName, boolean qualify) {
    	List<String> values = new ArrayList<String>();
        ServiceObjectType objectType = serviceBinding.getObject();
        List<ObjectPartType> objectPartTypes = objectType.getPart();
        for (ObjectPartType objectPartType : objectPartTypes) {
        	List<PropertyType> propNodeList = objectPartType.getProperties();
        	PropertyItemUtils.getPropertyValuesByNameInNodeList(propNodeList, 
        			propName, (qualify?(objectPartType.getLabel()+":"):null), values);
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
		return PropertyItemUtils.getPropertyValueByNameFromNodeList(servicePropList, propName );
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
    
    public static String getMappedFieldInDoc( ServiceBindingType sb,
    		String logicalFieldName, DocumentModel docModel ) {
    	// Now we have to get the number, which is configured as some field
    	// on each docType
    	/* If we go to qualified field names, we'll need this
    	String[] strings = qPropName.split(":");
    	if(strings.length!=2) {
    		throw new RuntimeException(
    				"getMappedFieldInDoc: Bad configuration of "
    				+logicalFieldName+" field for: "+docModel.getDocumentType().getName());
    	}
    	*/
    	String propName = getPropertyValue(sb, logicalFieldName);
    	if(propName==null||propName.isEmpty())
    		return null;
    	try {
    		return (String)docModel.getPropertyValue(propName);
    	} catch(IndexOutOfBoundsException ioobe) {
				// Should not happen, but may with certain array forms
				if(logger.isTraceEnabled()) {
					logger.trace("SBUtils.getMappedField caught OOB exc, for Prop: "+propName
						+ " in: " + docModel.getDocumentType().getName()
						+ " csid: " + NuxeoUtils.getCsid(docModel));
				}
				return null;
    	} catch(ClientException ce) {
    		throw new RuntimeException(
    				"getMappedFieldInDoc: Problem fetching: "+propName+" logicalfieldName: "+logicalFieldName+" docModel: "+docModel, ce);
    	}
    } 
    
    private static ArrayList<String> commonServiceTypes = null;
    
    public static ArrayList<String> getCommonServiceTypes() {
    	if(commonServiceTypes == null) {
    		commonServiceTypes = new ArrayList<String>();
				// Problematic at this point:	commonServiceTypes.add(SERVICE_TYPE_AUTHORITY);
    		commonServiceTypes.add(SERVICE_TYPE_OBJECT);
    		commonServiceTypes.add(SERVICE_TYPE_PROCEDURE);
    	}
    	return commonServiceTypes;
    }
    


}
