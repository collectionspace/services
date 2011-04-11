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

import javax.ws.rs.core.MultivaluedMap;

/**
 * RemoteServiceContext is used to encapsulate the service context of a
 * remotely invokable service
 */
public interface RemoteServiceContext<IT, OT>
        extends ServiceContext<IT, OT> {

    public void setJaxRsContext(JaxRsContext theJaxRsContext);
    
    public JaxRsContext getJaxRsContext();
	
    /**
     * Get input parts as received over the wire from service consumer
     * @return the input
     */
    @Override
    public IT getInput();

    /**
     * setInput is used to set request input before starting to
     * process input data
     * @param input
     * @exception IOException
     */
    @Override
    public void setInput(IT input);

    /**
     * Get output parts to send over the wire to service consumer
     * @return the output
     */
    @Override
    public OT getOutput();

    /**
     * Set output parts to send over the wire to service consumer
     * @return the output
     */
    @Override
    public void setOutput(OT output);


    /**
     * getLocalContext clones the remote context minus remote messaging data parts
     * this method is useful to object a local service context to invoke a service locally
     * @param local context class namee
     * @return local service context
     */
    public ServiceContext getLocalContext(String localContextClassName) throws Exception;
    
    /**
     * Gets the query params.
     * 
     * @return the query params
     */
    public MultivaluedMap<String, String> getQueryParams();

    /**
     * Gets the query params.
     * 
     * @return the query params
     */
    public void setQueryParams(MultivaluedMap<String, String> queryParams);
}

