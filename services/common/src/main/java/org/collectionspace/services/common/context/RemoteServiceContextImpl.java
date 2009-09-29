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
import java.lang.reflect.Constructor;
import javax.ws.rs.core.MediaType;
import org.collectionspace.services.common.repository.DocumentUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * RemoteServiceContextImpl
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RemoteServiceContextImpl
        extends AbstractServiceContext<MultipartInput, MultipartOutput>
        implements RemoteServiceContext {

    final Logger logger = LoggerFactory.getLogger(RemoteServiceContextImpl.class);
    //input stores original content as received over the wire
    private MultipartInput input;
    private MultipartOutput output;

    public RemoteServiceContextImpl(String serviceName) {
        super(serviceName);
        output = new MultipartOutput();
    }

    @Override
    public MultipartInput getInput() {
        return input;
    }

    @Override
    public void setInput(MultipartInput input) throws IOException {
        this.input = input;
    }

    @Override
    public MultipartOutput getOutput() {
        return output;
    }

    @Override
    public void setOutput(MultipartOutput output) throws Exception {
        this.output = output;
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
    public ServiceContext getLocalContext(String localContextClassName) throws Exception {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        Class ctxClass = cloader.loadClass(localContextClassName);
        if(!ServiceContext.class.isAssignableFrom(ctxClass)) {
            throw new IllegalArgumentException("getLocalContext requires " +
                    " implementation of " + ServiceContext.class.getName());
        }
        
        Constructor ctor = ctxClass.getConstructor(java.lang.String.class);
        ServiceContext ctx = (ServiceContext)ctor.newInstance(getServiceName());
        return ctx;
    }
}
