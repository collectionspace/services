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

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

/**
 *
 * MultipartRemoteServiceContextFactory creates a service context for
 * a service processing multipart messages
 *
 */
public class MultipartServiceContextFactory
        implements ServiceContextFactory<MultipartInput> {

    final private static MultipartServiceContextFactory self = new MultipartServiceContextFactory();

    private MultipartServiceContextFactory() {
    }

    public static MultipartServiceContextFactory get() {
        return self;
    }

    /**
     * createServiceContext is a factory method to create a service context
     * a service context is created on every service request call
     * @param input
     * @param serviceName which service/repository context to use
     * @return
     */
    @Override
    public ServiceContext createServiceContext(MultipartInput input, String serviceName) throws Exception {
        MultipartServiceContext ctx = new MultipartServiceContextImpl(serviceName);
        ctx.setInput(input);
        return ctx;
    }
}
