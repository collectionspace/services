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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReader;
import org.collectionspace.services.common.repository.DocumentUtils;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.service.ServiceBindingType;
import org.collectionspace.services.common.service.ServiceObjectType;
import org.collectionspace.services.common.tenant.TenantBindingType;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * ServiceContextImpl
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class ServiceContextImpl
        implements ServiceContext {

    final Logger logger = LoggerFactory.getLogger(ServiceContextImpl.class);
    private TenantBindingType tenantBinding;
    private ServiceBindingType serviceBinding;
    //input stores original content as received over the wire
    private MultipartInput input;
    private MultipartOutput output;
    Map<String, ObjectPartType> objectPartMap = new HashMap<String, ObjectPartType>();

    @Override
    public String toString() {
        return "ServiceContextImpl [" +
                "service name=" + serviceBinding.getName() + " " +
                "service version=" + serviceBinding.getVersion() + " " +
                "tenant id=" + tenantBinding.getId() + " " +
                "tenant name=" + tenantBinding.getName() + " " + tenantBinding.getDisplayName() + " " +
                "tenant repository domain=" + tenantBinding.getRepositoryDomain() + " " +
                "]";
    }

    public ServiceContextImpl(String serviceName) {
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
        output = new MultipartOutput();
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
    public ServiceBindingType getServiceBinding() {
        return serviceBinding;
    }

    @Override
    public String getServiceName() {
        return serviceBinding.getName();
    }

    @Override
    public String getQualifiedServiceName() {
        return TenantBindingConfigReader.getTenantQualifiedServiceName(
                getTenantId(), getServiceName());
    }

    @Override
    public String getRepositoryDomainName() {
        return tenantBinding.getRepositoryDomain();
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
    public String getRepositoryWorkspaceName() {
        //service name is workspace name by convention
        return serviceBinding.getName();
    }

    @Override
    public String getRepositoryWorkspaceId() {
        TenantBindingConfigReader tbConfigReader =
                ServiceMain.getInstance().getTenantBindingConfigReader();
        return tbConfigReader.getWorkspaceId(getTenantId(), getServiceName());
    }

    @Override
    public MultipartInput getInput() {
        return input;
    }

    @Override
    public MultipartOutput getOutput() {
        return output;
    }

    @Override
    public Object getInputPart(String label, Class clazz) throws IOException {
        Object obj = null;
        if(getInput() != null){
            MultipartInput fdip = getInput();

            for(InputPart part : fdip.getParts()){
                String partLabel = part.getHeaders().getFirst("label");
                if(label.equalsIgnoreCase(partLabel)){
                    if(logger.isDebugEnabled()){
                        logger.debug("received part label=" + partLabel +
                                "\npayload=" + part.getBodyAsString());
                    }
                    obj = part.getBody(clazz, null);
                    break;
                }
            }
        }
        return obj;
    }

    @Override
    public void addOutputPart(String label, Document doc, String contentType) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try{
            DocumentUtils.writeDocument(doc, baos);
            baos.close();
            OutputPart part = output.addPart(new String(baos.toByteArray()),
                    MediaType.valueOf(contentType));
            part.getHeaders().add("label", label);
        }finally{
            if(baos != null){
                try{
                    baos.close();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    public void setInput(MultipartInput input) throws IOException {
        this.input = input;
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

    /**
     * getCommonPartLabel get common part label
     * @return
     */
    @Override
    public String getCommonPartLabel() {
        return getServiceName().toLowerCase() + "-common";
    }
}
