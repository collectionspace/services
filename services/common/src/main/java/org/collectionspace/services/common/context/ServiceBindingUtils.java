package org.collectionspace.services.common.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.collectionspace.services.common.config.PropertyItemUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.config.service.ObjectPartType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

import java.lang.IndexOutOfBoundsException;

import org.collectionspace.services.client.index.IndexClient;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.document.DocumentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceBindingUtils {
	public static final boolean QUALIFIED_PROP_NAMES = true;
	public static final boolean UNQUALIFIED_PROP_NAMES = false;
	public static final String OUTPUT_MIME_PROP = "outputMIME";
	public static final String FULLTEXT_QUERY = IndexClient.FULLTEXT_ID;
	public static final String ELASTICSEARCH_QUERY = IndexClient.ELASTICSEARCH_ID;
	public static final String AUTH_REF_PROP = "authRef";
	public static final String TERM_REF_PROP = "termRef";
	public static final String OBJ_NUMBER_PROP = "objectNumberProperty";
	public static final String OBJ_NAME_PROP = "objectNameProperty";
	public static final String SERVICE_TYPE_PROP = "type";
	public static final String SERVICE_TYPE_OBJECT = "object";
	public static final String SERVICE_TYPE_PROCEDURE = "procedure";
	public static final String SERVICE_TYPE_AUTHORITY = "authority";
	public static final String SERVICE_TYPE_VOCABULARY = "vocabulary";
	public static final String SERVICE_TYPE_UTILITY = "utility";
	public static final String SERVICE_TYPE_SECURITY = "security";
	public static final String SERVICE_COMMONPART_ID = "1";
	
	private static final String TENANT_EXTENSION_PATTERN = "(.*)"+ServiceContext.TENANT_SUFFIX+"[\\d]+$";
	private static final String TENANT_REPLACEMENT_PATTERN = "$1";
	private static Pattern tenantSuffixPattern = null;

	private static final Logger logger = LoggerFactory.getLogger(ServiceBindingUtils.class);
	
    public static String getTenantQualifiedDocType(String tenantId, String docType) {
    	String result = docType + ServiceContext.TENANT_SUFFIX + tenantId;
    	return result;
    }
    	
    public static String getUnqualifiedTenantDocType(String docType) {
        try {
            if(tenantSuffixPattern == null ) {
            	tenantSuffixPattern = Pattern.compile(TENANT_EXTENSION_PATTERN);
            }
            Matcher tenantSuffixMatcher = tenantSuffixPattern.matcher(docType);
            return tenantSuffixMatcher.replaceFirst(TENANT_REPLACEMENT_PATTERN);
        } catch (PatternSyntaxException pe) {
            logger.warn("TENANT_EXTENSION_PATTERN regex pattern '" + TENANT_EXTENSION_PATTERN
                    + "' could not be compiled: " + pe.getMessage());
            // If reached, method will return a value of false.
        }
    	return docType;
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
        if (objectType != null) {
	        List<ObjectPartType> objectPartTypes = objectType.getPart();
	        for (ObjectPartType objectPartType : objectPartTypes) {
	        	List<PropertyType> propNodeList = objectPartType.getProperties();
	        	PropertyItemUtils.getPropertyValuesByNameInNodeList(propNodeList, 
	        			propName, (qualify?(objectPartType.getLabel()+":"):null), values);
	        }
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
    
    public static List<PropertyItemType> getPropertyValueList(ServiceBindingType service,
    		String propName) {
    	if (propName == null || propName.trim().isEmpty()) {
    		throw new IllegalArgumentException("ServiceBindingUtils.getPropertyValues: null property name!");
    	}
		List<PropertyType> servicePropList = service.getProperties();
		return PropertyItemUtils.getPropertyValueListByNameFromNodeList(servicePropList, propName);
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
    
    public static String getMappedFieldInDoc(ServiceBindingType sb,
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
    	if (Tools.isBlank(propName)) {
                logger.warn("Property name is empty for property " + logicalFieldName + " in service " + sb.getName());
                logger.warn("This may be due to an improperly configured or missing "
                        + "generic property (objectNameProperty, objectNumberProperty ...) in tenant bindings configuration");
    		return "";
        }
    	//
    	// Try to get a value from the Nuxeo document for the property name.
    	//
    	String result = null;
		try {
			Object obj = NuxeoUtils.getProperyValue(docModel, propName);
			result = DocumentUtils.propertyValueAsString(obj, docModel, propName);
		} catch (IndexOutOfBoundsException ioobe) {
			// Should not happen, but may with certain array forms
			logger.trace("SBUtils.getMappedField caught OOB exc, for Prop: "
					+ propName + " in: " + docModel.getDocumentType().getName()
					+ " csid: " + NuxeoUtils.getCsid(docModel));
		} catch (ClientException ce) {
			throw new RuntimeException(
					"getMappedFieldInDoc: Problem fetching: " + propName
							+ " logicalfieldName: " + logicalFieldName
							+ " docModel CSID: " + docModel.getName(), ce);
		} catch (Exception e) {
			logger.warn(String.format("Could not get a value for the property '%s' in Nuxeo document with CSID '%s'.",
					propName, docModel.getName()));
		}
    	
    	return result;
    } 
    
    private static ArrayList<String> commonProcedureServiceTypes = null;
    
    /**
     * Get the service name (service resource path) from the object name (Nuxeo document type)
     * @param serviceName
     * @param sb
     * @return
     */
    public static String getServiceNameFromObjectName(TenantBindingConfigReaderImpl bindingReader, String tenantId, String docType) {
    	String result = null;
    	
    	ServiceBindingType bindingType = bindingReader.getServiceBindingForDocType(tenantId, docType);
    	result = bindingType.getName().toLowerCase();
    	
    	return result;
    }
   
    public static ArrayList<String> getCommonServiceTypes(boolean includeAuthorities) {
        ArrayList<String> commonServiceTypes = new ArrayList<String>();
		if (includeAuthorities == true) {
			commonServiceTypes.add(SERVICE_TYPE_AUTHORITY); // REM - CSPACE-5359: Added back authorities on demand to resolve this issue.
		}
		commonServiceTypes.add(SERVICE_TYPE_OBJECT);
		commonServiceTypes.add(SERVICE_TYPE_PROCEDURE);
		
    	return commonServiceTypes;
    }
    
    // Temporary workaround for CSPACE-4983, to help reduce the
    // number of service types searched for authority references
    // in AuthorityResource.getReferencingObjects(), to in turn
    // help reduce database query complexity.
    //
    // FIXME; this method is intended to be temporary.  It was added in part to
    // make the effect of the workaround more explicit, and in part to avoid
    // breaking the use of the getCommonServiceTypes method in ServiceGroups.
    @Deprecated
    public static ArrayList<String> getCommonProcedureServiceTypes() {
        if(commonProcedureServiceTypes == null) {
            commonProcedureServiceTypes = new ArrayList<String>();
            commonProcedureServiceTypes.add(SERVICE_TYPE_PROCEDURE);
        }
    	return commonProcedureServiceTypes;
    }
    


}
