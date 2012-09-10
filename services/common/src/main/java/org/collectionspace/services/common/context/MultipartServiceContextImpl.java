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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.dom4j.DocumentException;
import org.dom4j.Element;
//import org.jboss.resteasy.plugins.providers.multipart.InputPart;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
//import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MultipartServiceContextImpl takes Multipart Input/Output
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class MultipartServiceContextImpl
        extends RemoteServiceContextImpl<PoxPayloadIn, PoxPayloadOut>
        implements MultipartServiceContext {

    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(MultipartServiceContextImpl.class);
	private String repositoryWorkspaceName;

    /**
     * Instantiates a new multipart service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected MultipartServiceContextImpl(String serviceName)
    		throws DocumentException, UnauthorizedException {
    	super(serviceName);
    	setOutput(new PoxPayloadOut(serviceName));
    }
    
    /**
     * Instantiates a new multipart service context impl.
     * 
     * @param serviceName the service name
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected MultipartServiceContextImpl(String serviceName, PoxPayloadIn theInput)
    		throws DocumentException, UnauthorizedException {
        super(serviceName, theInput);
        setOutput(new PoxPayloadOut(serviceName));
    }

    /**
     * Instantiates a new multipart service context impl.
     * 
     * @param serviceName the service name
     * @param queryParams the query params
     * 
     * @throws UnauthorizedException the unauthorized exception
     */
    protected MultipartServiceContextImpl(
    		String serviceName,
    		PoxPayloadIn theInput,
    		ResourceMap resourceMap,
    		UriInfo uriInfo) 
    			throws DocumentException, UnauthorizedException {
    	super(serviceName, theInput, resourceMap, uriInfo);
    	setOutput(new PoxPayloadOut(serviceName));
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPart(java.lang.String, java.lang.Class)
     */
    @Override
    @Deprecated
    public Object getInputPart(String label, Class clazz) throws IOException {
        return getInputPart(label);
                    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPart(java.lang.String, java.lang.Class)
     */
    @Override
    public Object getInputPart(String label) throws IOException {
    	Object result = null;
        PayloadInputPart payloadInputPart = getInput().getPart(label);
        if (payloadInputPart != null) {
        	result = payloadInputPart.getBody();
        }
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPartAsString(java.lang.String)
     */
    @Override
    public String getInputPartAsString(String label) throws IOException {
    	String result = null;
    	PayloadInputPart part = getInput().getPart(label);
        if (part != null) {
        	Element element = part.asElement();
        	if (element != null) {
        		result = element.asXML();
        }
    }
        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#getInputPartAsStream(java.lang.String)
     */
    @Override
    public InputStream getInputPartAsStream(String label) throws IOException {
    	InputStream result = null;
    	String part = getInputPartAsString(label);
        if (part != null) {
        	result = new ByteArrayInputStream(part.getBytes());
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.MultipartServiceContext#addOutputPart(java.lang.String, org.w3c.dom.Document, java.lang.String)
     */
    @Override
    public void addOutputPart(String label, Element element, String contentType) throws Exception {
	    PayloadOutputPart part = getOutput().addPart(label, element);
	    if (logger.isTraceEnabled() == true) {
	    	logger.trace("Adding part:" + label +
	    			" to " + getOutput().getName() + " document.");
	    }
    }

    @Override
    public void addOutputPart(PayloadOutputPart outputPart) throws Exception {
	    PayloadOutputPart part = getOutput().addPart(outputPart);
	    if (logger.isTraceEnabled() == true) {
	    	logger.trace("Adding part:" + part.getLabel() +
	    			" to " + getOutput().getName() + " document.");
	    }
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.RemoteServiceContextImpl#getLocalContext(java.lang.String)
     */
    @Override
    public ServiceContext getLocalContext(String localContextClassName) throws Exception {
        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        Class ctxClass = cloader.loadClass(localContextClassName);
        if (!ServiceContext.class.isAssignableFrom(ctxClass)) {
            throw new IllegalArgumentException("getLocalContext requires "
                    + " implementation of " + ServiceContext.class.getName());
        }

        Constructor ctor = ctxClass.getConstructor(java.lang.String.class);
        ServiceContext ctx = (ServiceContext) ctor.newInstance(getServiceName());
        return ctx;
    }
    
    /* (non-Javadoc)
     * @see org.collectionspace.services.common.context.ServiceContext#getRepositoryWorkspaceName()
     */
    @Override
    public String getRepositoryWorkspaceName() {
    	String result = repositoryWorkspaceName;
        //service name is workspace name by convention
        if (result == null) {
        	result = serviceBinding.getName();
        }
        return result;
    }
    
    @Override
    public void setRespositoryWorkspaceName(String workspaceName) {
    	repositoryWorkspaceName = workspaceName;
    }
}
