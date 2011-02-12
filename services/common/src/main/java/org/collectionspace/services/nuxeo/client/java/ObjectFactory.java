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
package org.collectionspace.services.nuxeo.client.java;

import javax.xml.bind.annotation.XmlRegistry;

/**
 * ObjectFactory for CommonList 
 */
@XmlRegistry
public class ObjectFactory {
    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.collectionspace.services.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link tCommonList }
     * 
     */
    public CommonList createCommonList() {
    	try {
    		//-System.out.println("CL_ObjectFactory:createAbstractCommonList");
    		return new CommonList();
    	} catch(Exception e) {
    		return null;
    	}
    }

}
