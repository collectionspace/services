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

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.config.service.ObjectPartType;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import javax.ws.rs.core.MediaType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * RemoteDocumentModelHandler
 *
 * @author pschmitz
 * $LastChangedRevision: $
 * $LastChangedDate: $
 * @param <T> The {DocumentType}Common class
 * @param <TL> The {DocumentType}CommonList class
 */
public abstract class RemoteSubItemDocumentModelHandlerImpl<T, TL> extends
		RemoteDocumentModelHandlerImpl<T, TL> {

    private final Logger logger = LoggerFactory.getLogger(RemoteSubItemDocumentModelHandlerImpl.class);
    private final String SI_LABEL = "subitem";
    // We must align this to the schema:
    //   <xs:element name="owner" type="xs:string" />
	//   <xs:element name="isPrimary" type="xs:boolean"/>
	//   <xs:element name="order" type="xs:unsignedInt"/>
    private final String[] fields = {"owner", "isPrimary", "order"};

    /**
     * Override fillPart to handle the Subitem XML part into the Subitem document model
     * @param part to fill
     * @param docModel for the given object
     * @param partMeta metadata for the object to fill
     * @throws Exception
     */
	@Override
    protected void fillPart(PayloadInputPart part, DocumentModel docModel, 
    						ObjectPartType partMeta, Action action, ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx)
            throws Exception {
		ByteArrayInputStream bas = new ByteArrayInputStream(part.getElementBody().asXML().getBytes());
        InputStream payload = bas;//part.getBody(/*InputStream.class, null*/);

        //check if this is an xml part
        // TODO - we could configure the parts that have subitem content, 
        // and then check that here, so skip other parts.
        if(part.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            if(payload != null){
                Document document = DocumentUtils.parseDocument(payload, partMeta,
                		false /*don't validate*/);
                //TODO: callback to handler if registered to validate the
                //document
                Map<String, Object> objectProps = DocumentUtils.parseProperties(document.getFirstChild());
                // Now pull out the subitem props and set them into the Subitem schema
                Map<String, Object> subitemProps = null;
                for(String key:fields){
                	// Fetch and remove as we go, so can safely set remaining values below
                	String value = (String)(objectProps.remove(key));
                	if(value!=null) {
                    	if(subitemProps == null) {
                    		subitemProps = new HashMap<String, Object>();
                    	}
                    	subitemProps.put(key, value);
                	}
                }
            	if(subitemProps != null) {
            		docModel.setProperties(SI_LABEL, subitemProps);
            	}
            	// Set all remaining values on the common part.
                docModel.setProperties(partMeta.getLabel(), objectProps);
            }
        }
    }
    
    /**
     * extractPart extracts an XML object from given DocumentModel
     * This overridden form checks for schemas that extend subitem, and merges
     * in the subitem properties for that part.
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
	@Override
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> map = extractPart( docModel, schema, partMeta, null ); 
		if(schemaHasSubItem(schema)) {
			extractPart(docModel, SI_LABEL, partMeta, map);
		}
    	return map;
    }
	
	// TODO HACK - should make this info be configured in the part metadata.
	public abstract boolean schemaHasSubItem(String schema);

}
