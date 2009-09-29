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

import java.util.Map;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;

/**
 *
 * ServiceContext is used to pass along metadata and runtime context
 * between various components of the service framework while processing
 * a service request.
 */
public interface ServiceContext<T1, T2> {

    /**
     * getTenantId get tenant id
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
     * Get input parts as received over the wire from service consumer
     * @return the input
     */
    public T1 getInput();

    /**
     * setInput is used to set request input before starting to
     * process input data
     * @param input
     * @exception Exception
     */
    public void setInput(T1 input) throws Exception;

    /**
     * Get output parts to send over the wire to service consumer
     * @return the output
     */
    public T2 getOutput();

    /**
     * setOutput set output
     * @param output
     */
    public void setOutput(T2 output) throws Exception;

    /**
     * getPartsMetadata returns metadata for object parts used by the service
     * @return
     */
    public Map<String, ObjectPartType> getPartsMetadata();

    /**
     * getCommonPartLabel retruns label for common part of a service 
     * @return label
     */
    public String getCommonPartLabel();
}



