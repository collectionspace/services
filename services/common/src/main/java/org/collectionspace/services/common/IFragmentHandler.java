/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2011 University of California at Berkeley

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
package org.collectionspace.services.common;

import org.dom4j.Document;
import org.dom4j.Element;

/** Define this interface to listen for events from the driving class:
 *   org.collectionspace.services.common.XmlSaxFragmenter , so that
 *   the XmlSaxFragmenter class may be passed a large file or InputSource (stream)
 *   and it will be parsed with SAX, but you will get fragments from it that you can
 *   parse with DOM.
 *
 *  You will be passed a Document context, which is a Dom4j document that represents the
 *  skeleton of the document you started with, but without any fragments, so the Document
 *  will just be context information of how the XmlSaxFragmenter found this fragment.
 *
 *  You will receive onFragmentReady() events whenever a fragment is parsed completely.
 *  the fragment parameter will be just the inner XML String of fragmentParent, and will
 *  not be represented in the DOM of the Document context.
 *
 *  @author Laramie Crocker
 */
public interface IFragmentHandler {
    /** @param fragmentIndex is the zero-based index of the current fragment; you will first get this event
     *  on fragmentIndex==0, which is a fragmentCount of 1. */
    public void onFragmentReady(Document context,
                                Element fragmentParent,
                                String currentPath,
                                int fragmentIndex,
                                String fragment);

    /** @param fragmentCount is the count of fragments processed - a value of 1 means 1 fragment was found. */
    public void onEndDocument(Document context, int fragmentCount);
}
