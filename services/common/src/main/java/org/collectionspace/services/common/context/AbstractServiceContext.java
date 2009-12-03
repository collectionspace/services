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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReader;
import org.collectionspace.services.common.security.UnauthorizedException;
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
public abstract class AbstractServiceContext<IT, OT>
        implements ServiceContext<IT, OT> {

    final Logger logger = LoggerFactory.getLogger(AbstractServiceContext.class);
    Map<String, Object> properties = new HashMap<String, Object>();
    Map<String, ObjectPartType> objectPartMap = new HashMap<String, ObjectPartType>();
    private ServiceBindingType serviceBinding;
    private TenantBindingType tenantBinding;
    private String overrideDocumentType = null;

    public AbstractServiceContext(String serviceName) throws UnauthorizedException {
        TenantBindingConfigReader tReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        //TODO: get tenant binding from security context
        String tenantId = retrieveTenantId();
        if (tenantId == null) {
            //for testing purposes
            tenantId = "1"; //hardcoded for movingimages.us
        }
        tenantBinding = tReader.getTenantBinding(tenantId);
        if (tenantBinding == null) {
            String msg = "No tenant binding found while processing request for " +
                    serviceName;
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        serviceBinding = tReader.getServiceBinding(tenantId, serviceName);
        if (serviceBinding == null) {
            String msg = "No service binding found while processing request for " +
                    serviceName + " for tenant id=" + getTenantId() +
                    " name=" + getTenantName();
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        if (logger.isDebugEnabled()) {
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
        if (objectPartMap.size() != 0) {
            return objectPartMap;
        }
        ServiceBindingType serviceBinding = getServiceBinding();
        List<ServiceObjectType> objectTypes = serviceBinding.getObject();
        for (ServiceObjectType objectType : objectTypes) {
            List<ObjectPartType> objectPartTypes = objectType.getPart();
            for (ObjectPartType objectPartType : objectPartTypes) {
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
    public String getDocumentType() {
        // If they have not overridden the setting, use the type of the service
        // object.
        return (overrideDocumentType != null) ? overrideDocumentType : serviceBinding.getObject().get(0).getName();
    }

    @Override
    public void setDocumentType(String docType) {
        overrideDocumentType = docType;
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
    public abstract IT getInput();

    @Override
    public abstract void setInput(IT input);

    @Override
    public abstract OT getOutput();

    @Override
    public abstract void setOutput(OT output);

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, Object> props) {
        properties.putAll(props);
    }

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object o) {
        properties.put(name, o);
    }
    private static final String SUBJECT_CONTEXT_KEY = "javax.security.auth.Subject.container";

    private String retrieveTenantId() throws UnauthorizedException {

        String tenantId = null;
        Subject caller = null;
        Set<Principal> principals = null;
        try {
            caller = (Subject) PolicyContext.getContext(SUBJECT_CONTEXT_KEY);
            if (caller == null) {
                //logger.warn("security not enabled...");
                return tenantId;
            }
            principals = caller.getPrincipals(Principal.class);
            if (principals != null && principals.size() == 0) {
                //TODO: find out why subject is not null
                if (logger.isDebugEnabled()) {
                    logger.debug("weird case where subject is not null and there are no principals");
                }
                return tenantId;
            }
        } catch (PolicyContextException pce) {
            String msg = "Could not retrieve principal information";
            logger.error(msg, pce);
            throw new UnauthorizedException(msg);
        }
        for (Principal p : principals) {
            try {
                Method m = p.getClass().getMethod("getTenantId");
                Object r = m.invoke(p);
                if (logger.isDebugEnabled()) {
                    logger.debug("retrieved tenantid=" + r +
                            " for principal=" + p.getName());
                }
                tenantId = (String) r;
                break;
            } catch (Exception e) {
                //continue with next principal
            }
        }
        if (tenantId == null) {
            String msg = "Could not find tenant context";
            logger.error(msg);
            throw new UnauthorizedException(msg);
        }
        return tenantId;
    }

    @Override
    public String toString() {
        StringBuilder msg = new StringBuilder();
        msg.append("AbstractServiceContext [");
        msg.append("service name=" + serviceBinding.getName() + " ");
        msg.append("service version=" + serviceBinding.getVersion() + " ");
        msg.append("tenant id=" + tenantBinding.getId() + " ");
        msg.append("tenant name=" + tenantBinding.getName() + " ");
        msg.append(tenantBinding.getDisplayName() + " ");
        msg.append("tenant repository domain=" + tenantBinding.getRepositoryDomain());
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            msg.append("property name=" + entry.getKey() + " value=" + entry.getValue().toString());
        }
        msg.append("]");
        return msg.toString();
    }
}
