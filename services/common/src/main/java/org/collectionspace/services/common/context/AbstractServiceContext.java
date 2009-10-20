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
import java.util.List;
import java.util.Map;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReader;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractServiceContext
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class AbstractServiceContext<T1, T2>
        implements ServiceContext<T1, T2> {

    final Logger logger = LoggerFactory.getLogger(AbstractServiceContext.class);
    Map<String, ObjectPartType> objectPartMap = new HashMap<String, ObjectPartType>();
    private ServiceBindingType serviceBinding;
    private TenantBindingType tenantBinding;
    
    public AbstractServiceContext(String serviceName) {
        TenantBindingConfigReader tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        //TODO: get tenant binding from security context (Subject.g
        String tenantId = "1"; //hardcoded for movingimages.us
        tenantBinding = tReader.getTenantBinding(tenantId);
        if(tenantBinding == null){
            String msg = "No tenant binding found while processing request for " +
                    serviceName;
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        serviceBinding = tReader.getServiceBinding(tenantId, serviceName);
        if(serviceBinding == null){
            String msg = "No service binding found while processing request for " +
                    serviceName + " for tenant id=" + getTenantId() +
                    " name=" + getTenantName();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        if(logger.isDebugEnabled()){
            logger.debug("tenantId=" + tenantId +
                    " service binding=" + serviceBinding.getName());
        }
    }

    /**
     * getCommonPartLabel get common part label
     * @return
     */
    @Override
    public String getCommonPartLabel() {
        return getCommonPartLabel(getServiceName());
    }

    /**
     * getCommonPartLabel get common part label
     * @return
     */
    public String getCommonPartLabel(String schemaName) {
        return schemaName.toLowerCase() + PART_LABEL_SEPERATOR + PART_COMMON_LABEL;
    }

    @Override
    public Map<String, ObjectPartType> getPartsMetadata() {
        if(objectPartMap.size() != 0){
            return objectPartMap;
        }
        ServiceBindingType serviceBinding = getServiceBinding();
        List<ServiceObjectType> objectTypes = serviceBinding.getObject();
        for(ServiceObjectType objectType : objectTypes){
            List<ObjectPartType> objectPartTypes = objectType.getPart();
            for(ObjectPartType objectPartType : objectPartTypes){
                objectPartMap.put(objectPartType.getLabel(), objectPartType);
            }
        }
        return objectPartMap;
    }

    @Override
    public String getQualifiedServiceName() {
        return TenantBindingConfigReader.getTenantQualifiedServiceName(getTenantId(), getServiceName());
    }

    @Override
    public String getRepositoryClientName() {
        return serviceBinding.getRepositoryClient();
    }

    @Override
    public ClientType getRepositoryClientType() {
        //assumption: there is only one repository client configured
        return ServiceMain.getInstance().getClientType();
    }

    @Override
    public String getRepositoryDomainName() {
        return tenantBinding.getRepositoryDomain();
    }

    @Override
    public String getRepositoryWorkspaceId() {
        TenantBindingConfigReader tbConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
        return tbConfigReader.getWorkspaceId(getTenantId(), getServiceName());
    }

    @Override
    public String getRepositoryWorkspaceName() {
        //service name is workspace name by convention
        return serviceBinding.getName();
    }

    @Override
    public ServiceBindingType getServiceBinding() {
        return serviceBinding;
    }

    @Override
    public String getServiceName() {
        return serviceBinding.getName();
    }

    @Override
    public String getTenantId() {
        return tenantBinding.getId();
    }

    @Override
    public String getTenantName() {
        return tenantBinding.getName();
    }

    @Override
    public abstract T1 getInput();

    @Override
    public abstract void setInput(T1 input) throws Exception;

    @Override
    public abstract T2 getOutput();

    @Override
    public abstract void setOutput(T2 output) throws Exception;

    @Override
    public String toString() {
        return "AbstractServiceContext [" +
                "service name=" + serviceBinding.getName() + " " +
                "service version=" + serviceBinding.getVersion() + " " +
                "tenant id=" + tenantBinding.getId() + " " +
                "tenant name=" + tenantBinding.getName() + " " +
                tenantBinding.getDisplayName() + " " +
                "tenant repository domain=" + tenantBinding.getRepositoryDomain() + " " +
                "]";
    }
}
