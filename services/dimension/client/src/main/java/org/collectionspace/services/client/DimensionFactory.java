/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2010 University of California at Berkeley

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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.client;




import javax.ws.rs.core.MediaType;
import org.collectionspace.services.dimension.DimensionsCommon;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author
 */
public class DimensionFactory {

    static private final Logger logger =
            LoggerFactory.getLogger(DimensionFactory.class);

    /**
     * Creates the dimension instance.
     *
     * @param commpnPartName
     * @param dimension
     * @return the multipart output
     */
    public static MultipartOutput createDimensionInstance(String commonPartName, 
            DimensionsCommon dimension) {

        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
                multipart.addPart(dimension, MediaType.APPLICATION_XML_TYPE);
        commonPart.getHeaders().add("label", commonPartName);

        return multipart;
    }
}
