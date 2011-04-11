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

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class DocumentListWrapperImpl.
 *
 * @param <T> the generic type
 */
public class DocumentListWrapperImpl<T> implements DocumentListWrapper {

    /** The document list wrapper. */
    private List<T> documentListWrapper;

    /**
     * Instantiates a new document list wrapper impl.
     *
     * @param theDocumentListWrapper the the document list wrapper
     */
    public DocumentListWrapperImpl(List<T> theDocumentListWrapper) {
        documentListWrapper = theDocumentListWrapper;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentListWrapper#getWrappedObject()
     */
    public List<T> getWrappedObject() {
        return documentListWrapper;
    }
}