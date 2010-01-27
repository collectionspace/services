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
package org.collectionspace.services.common.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;

/**
 *
 * ServiceContext is used to pass along metadata and runtime context
 * between various components of the service framework while processing
 * a service request.
 */
public interface ServiceContext<IT, OT> {

    /**
     * The character used to separate the words in a part label
     */
    public static final String PART_LABEL_SEPERATOR = "_";
    public static final String PART_COMMON_LABEL = "common";
	
    /**
     * getTenantId get id of tenant for which service is accessed
     * @return tenant id
     */
    public String getTenantId();

    /**
     * getTenantName get tenant name from the binding
     * @return tenant name such as movingimage.us
     */
    public String getTenantName();

    /**
     * getServiceBinding gets service binding metadata
     * @return service binding metadata
     */
    public ServiceBindingType getServiceBinding();

    /**
     * getServiceName returns the unqualified name of the service
     * @return service name
     */
    public String getServiceName();

    /**
     * getDocumentType returns the name of the (primary) DocumentType for this service
     * The value defaults to the Service Name, unless overridden with setDocumentType();
     * @return service name
     */
    public String getDocumentType();

    /**
     * setDocumentType sets the name of the Document Type for this service
     * The value defaults to the Service Name.
     * @return service name
     */
    public void setDocumentType(String docType);

    /**
     * getQualifiedServiceName returns tenant id qualified service name
     * @return tenant qualified service name
     */
    public String getQualifiedServiceName();

    /**
     * getRepositoryDomainName returns repository domain for the tenant
     * @return repository domain for the tenant
     */
    public String getRepositoryDomainName();

    /**
     * getRepositoryClientName returns the repository client name as
     * configured for the service
     */
    public String getRepositoryClientName();

    /**
     * getRepositoryClientType returns the type of client configured for the
     * service layer
     * @param ctx service context
     * @return
     */
    public ClientType getRepositoryClientType();

    /**
     * getRepositoryWorkspaceName returns repository workspace for the service for
     * the tenant. Not every service has a corresponding repository workspace.
     * If the service does not have any repository workspace, this method
     * returns null.
     * @return repository workspace
     */
    public String getRepositoryWorkspaceName();

    /**
     * getRepositoryWorksapceId returns workspace id for the service for the
     * tenant. Not every service has a corresponding repository workspace.
     * If the service does not have any repository workspace, this method
     * returns null.
     * @return repository workspace
     */
    public String getRepositoryWorkspaceId();

    /**
     * getDocumentHandlerClass returns the class name for document handler
     * @return class name of document handler
     */
    public String getDocumentHandlerClass();

    /**
     * Get input parts as received over the wire from service consumer
     * @return the input
     */
    public IT getInput();

    /**
     * setInput is used to set request input before starting to
     * process input data
     * @param input
     */
    public void setInput(IT input);

    /**
     * Get output parts to send over the wire to service consumer
     * @return the output
     */
    public OT getOutput();

    /**
     * setOutput set output
     * @param output
     */
    public void setOutput(OT output);

    /**
     * getPartsMetadata returns metadata for object parts used by the service
     * @return
     */
    public Map<String, ObjectPartType> getPartsMetadata();

    /**
     * getCommonPartLabel returns label for common part of a service 
     * @return label
     */
    public String getCommonPartLabel();

    /**
     * getCommonPartLabel returns label for common part of a specified schema.
     * This is useful for sub-resources. 
     * @return label
     */
    public String getCommonPartLabel(String schemaName);
    
    /**
     * getProperties retruns user-defined properties associated with this context
     * @return
     */
    public Map<String, Object> getProperties();


    /**
     * setProperties sets user-defined properties to this context
     * @param props
     */
    public void setProperties(Map<String, Object> props);


    /**
     * getProperty returns specified user-defined property
     */
    public Object getProperty(String name);

    /**
     * setProperty sets user-defined property with given name
     */
    public void setProperty(String name, Object o);

}



