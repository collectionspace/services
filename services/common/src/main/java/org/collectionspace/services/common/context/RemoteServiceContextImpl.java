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

import java.lang.reflect.Constructor;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RemoteServiceContextImpl
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RemoteServiceContextImpl<IT, OT>
        extends AbstractServiceContextImpl<IT, OT>
        implements RemoteServiceContext<IT, OT> {

    final Logger logger = LoggerFactory.getLogger(RemoteServiceContextImpl.class);
    //input stores original content as received over the wire
    private IT input;
    private OT output;

    public RemoteServiceContextImpl(String serviceName) throws UnauthorizedException {
        super(serviceName);
    }

    @Override
    public IT getInput() {
        return input;
    }

    @Override
    public void setInput(IT input) {
        //for security reasons, do not allow to set input again (from handlers)
        if (this.input != null) {
            String msg = "Non-null input cannot be set!";
            logger.error(msg);
            throw new IllegalStateException(msg);
        }
        this.input = input;
    }

    @Override
    public OT getOutput() {
        return output;
    }

    @Override
    public void setOutput(OT output) {
        this.output = output;
    }

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
}
