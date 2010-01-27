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
package org.collectionspace.services.common.document;

/**
 *
 * DocumentHandlerFactory creates document handler
 *
 */
public class DocumentHandlerFactory {

    private static final DocumentHandlerFactory self = new DocumentHandlerFactory();

    private DocumentHandlerFactory() {
    }

    public static DocumentHandlerFactory getInstance() {
        return self;
    }

    /**
     * getHandler returns a document handler. The factory may create a new
     * stateful handler or return an existing stateless handler.
     * @param clazz name of the class to instantiate. The class should implement
     * DocumentHandler
     */
    public DocumentHandler getHandler(String clazz)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Class c = tccl.loadClass(clazz);
        if (DocumentHandler.class.isAssignableFrom(c)) {
            return (DocumentHandler) c.newInstance();
        } else {
            throw new IllegalArgumentException("Not of type " + DocumentHandler.class.getCanonicalName());
        }
    }
}
