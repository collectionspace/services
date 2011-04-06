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

import java.io.IOException;
import java.io.InputStream;

import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.dom4j.Element;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.dom4j.Document;

/**
 * RemoteServiceContext is used to encapsulate the service context of a
 * remotely invokable service
 */
public interface MultipartServiceContext
        extends RemoteServiceContext<PoxPayloadIn, PoxPayloadOut> {

    /**
     * Get input parts as received over the wire from service consumer
     * @return the input
     */
    @Override
    public PoxPayloadIn getInput();

    /**
     * setInput is used to set request input before starting to
     * process input data
     * @param input
     */
    @Override
    public void setInput(PoxPayloadIn input);

    /**
     * Get output parts to send over the wire to service consumer
     * @return the output
     */
    @Override
    public PoxPayloadOut getOutput();

    /**
     * Set output parts to send over the wire to service consumer
     * @return the output
     */
    @Override
    public void setOutput(PoxPayloadOut output);

    /**
     * getInputPart returns the input part object for given label and clazz
     * @param label
     * @param clazz class of the object
     * @return part
     */
    @Deprecated
    public Object getInputPart(String label, Class<?> clazz) throws IOException;
    
    /**
     * Gets the input part.
     *
     * @param label the label
     * @return the input part
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Object getInputPart(String label) throws IOException;

    /**
     * getInputPartAsString returns the input part with given label in the string form
     * @param label
     * @return
     * @throws IOException
     */
    public String getInputPartAsString(String label) throws IOException;

    /**
     * getInputPartAsStream returns input part as stream for given label
     * @param label
     * @return
     * @throws IOException
     */
    public InputStream getInputPartAsStream(String label) throws IOException;
    /**
     * addOutputPart adds given XML part with given label and content type to output
     * @param label
     * @param document
     * @param contentType media type
     */
    public void addOutputPart(String label, Element doc, String contentType) throws Exception;
    
    public void addOutputPart(PayloadOutputPart outputPart) throws Exception;    
    
    public void setRespositoryWorkspaceName(String workspaceName);    
}
