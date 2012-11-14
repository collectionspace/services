/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.common.config;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.config.RepositoryClientConfigType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.service.DocHandlerParams;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.config.types.PropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pschmitz
 *
 */
public class ServiceConfigUtils {

    final static Logger logger = LoggerFactory.getLogger(ServiceConfigUtils.class);

    /*
     * Returns the document handler parameters that were loaded at startup from the
     * tenant bindings config file.
     */
	public static DocHandlerParams.Params getDocHandlerParams(ServiceContext ctx) throws DocumentException {
		ServiceBindingType sb = ctx.getServiceBinding();
		DocHandlerParams dhb = sb.getDocHandlerParams();
		if (dhb != null && dhb.getParams() != null) {
			return dhb.getParams();
		}
		throw new DocumentException("No DocHandlerParams configured for: "
				+ sb.getName());
	}
	
    /**
     * Creates the document handler instance.
     * 
     * @return the document handler
     * 
     * @throws Exception the exception
     */
    public static DocumentHandler createDocumentHandlerInstance(TenantBindingType tenantBinding,
    		ServiceBindingType serviceBinding) throws Exception {
    	DocumentHandler docHandler = null;
    	
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Class<?> c = tccl.loadClass(getDocumentHandlerClass(tenantBinding, serviceBinding));
        if (DocumentHandler.class.isAssignableFrom(c)) {
            docHandler = (DocumentHandler) c.newInstance();
            if (logger.isDebugEnabled()) {
            	logger.debug("Created an instance of the DocumentHandler for: " + getDocumentHandlerClass(tenantBinding, serviceBinding));
            }
        } else {
            throw new IllegalArgumentException("Not of type "
                    + DocumentHandler.class.getCanonicalName());
        }

        return docHandler;
    }

    /**
     * Gets the document handler class.
     * 
     * @return the document handler class
     */
    private static String getDocumentHandlerClass(TenantBindingType tenantBinding,
    		ServiceBindingType serviceBinding) {
        if (serviceBinding.getDocumentHandler() == null
                || serviceBinding.getDocumentHandler().isEmpty()) {
            String msg = "Missing documentHandler in service binding for service name \""
                    + serviceBinding.getName() + "\" for tenant id=" + tenantBinding.getId()
                    + " name=" + tenantBinding.getName();
            logger.warn(msg);
            throw new IllegalStateException(msg);
        }
        return serviceBinding.getDocumentHandler().trim();
    }

    /**
     * Gets the values of a configured property for a service.
     *
     * @param serviceConfiga tenant binding
     * @param propName the property to fetch (can return multiple values for this property).
     * @return a list of values for the supplied property.
     */
    public static List<String> getPropertyValues(ServiceConfig serviceConfig,
    		String propName) {
    	List<String> propValues = null;
    	if(propName==null) {
    		throw new IllegalArgumentException("ServiceConfilUtils.getPropertyValues: null property name!");
    	}
    	RepositoryClientConfigType repoClient = serviceConfig.getRepositoryClient();
    	if(repoClient==null) {
    		throw new RuntimeException("ServiceConfilUtils.getPropertyValues: serviceConfig has NULL repoClient!");
    	}
    	List<PropertyType> propList = repoClient.getProperties();
    	if(propList!=null && propList.size()>0) {
        	List<PropertyItemType> propItems = propList.get(0).getItem();
        	for(PropertyItemType propItem:propItems) {
        		if(propName.equals(propItem.getKey())) {
        			String value = propItem.getValue();
        			if(value!=null) {
        				if(propValues==null) {
        					propValues = new ArrayList<String>(); 
        				}
        				propValues.add(value);
        			}
        		}
        	}
    	}
    	return propValues;
    }
    

}
