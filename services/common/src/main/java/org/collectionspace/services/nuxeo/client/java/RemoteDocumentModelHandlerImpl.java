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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.common.vocabulary.RefNameUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * RemoteDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class RemoteDocumentModelHandlerImpl<T, TL>
        extends DocumentModelHandler<T, TL> {

    private final Logger logger = LoggerFactory.getLogger(RemoteDocumentModelHandlerImpl.class);

    @Override
    public void setServiceContext(ServiceContext ctx) {
        if(ctx instanceof MultipartServiceContext){
            super.setServiceContext(ctx);
        }else{
            throw new IllegalArgumentException("setServiceContext requires instance of " +
                    MultipartServiceContext.class.getName());
        }
    }

    @Override
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        DocumentModel docModel = wrapDoc.getWrappedObject();
        //return at least those document part(s) that were received
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        List<InputPart> inputParts = ctx.getInput().getParts();
        for(InputPart part : inputParts){
            String partLabel = part.getHeaders().getFirst("label");
            ObjectPartType partMeta = partsMetaMap.get(partLabel);
//            extractPart(docModel, partLabel, partMeta);
			Map<String, Object> unQObjectProperties = extractPart(docModel, partLabel, partMeta);
			addOutputPart(unQObjectProperties, partLabel, partMeta);
        }
    }

    private void addOutputPart(Map<String, Object> unQObjectProperties, String schema, ObjectPartType partMeta)
    		throws Exception {
		Document doc = DocumentUtils.buildDocument(partMeta, schema,
				unQObjectProperties);
		if (logger.isDebugEnabled()) {
			DocumentUtils.writeDocument(doc, System.out);
		}
		MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
		ctx.addOutputPart(schema, doc, partMeta.getContent().getContentType());
    }
    
	@Override
	public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc)
			throws Exception {

		DocumentModel docModel = wrapDoc.getWrappedObject();
		String[] schemas = docModel.getDeclaredSchemas();
		Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
		for (String schema : schemas) {
			ObjectPartType partMeta = partsMetaMap.get(schema);
			if (partMeta == null) {
				continue; // unknown part, ignore
			}
			Map<String, Object> unQObjectProperties = extractPart(docModel, schema, partMeta);
			addOutputPart(unQObjectProperties, schema, partMeta);
		}
	}

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {

        //TODO filling extension parts should be dynamic
        //Nuxeo APIs lack to support stream/byte[] input, get/setting properties is
        //not an ideal way of populating objects.
        DocumentModel docModel = wrapDoc.getWrappedObject();
        MultipartServiceContext ctx = (MultipartServiceContext) getServiceContext();
        MultipartInput input = ctx.getInput();
        if(input.getParts().isEmpty()){
            String msg = "No payload found!";
            logger.error(msg + "Ctx=" + getServiceContext().toString());
            throw new BadRequestException(msg);
        }

        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();

        //iterate over parts received and fill those parts
        List<InputPart> inputParts = input.getParts();
        for(InputPart part : inputParts){

            String partLabel = part.getHeaders().getFirst("label");
            if (partLabel == null) {
                String msg = "Part label is missing or empty!";
                logger.error(msg + "Ctx=" + getServiceContext().toString());
                throw new BadRequestException(msg);
            }
            
            //skip if the part is not in metadata
            if(!partsMetaMap.containsKey(partLabel)){
                continue;
            }
            ObjectPartType partMeta = partsMetaMap.get(partLabel);
            fillPart(part, docModel, partMeta);
        }//rof

    }

    /**
     * fillPart fills an XML part into given document model
     * @param part to fill
     * @param docModel for the given object
     * @param partMeta metadata for the object to fill
     * @throws Exception
     */
    protected void fillPart(InputPart part, DocumentModel docModel, ObjectPartType partMeta)
            throws Exception {
        InputStream payload = part.getBody(InputStream.class, null);

        //check if this is an xml part
        if(part.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
            if(payload != null){
                Document document = DocumentUtils.parseDocument(payload);
                //TODO: callback to handler if registered to validate the
                //document
                Map<String, Object> objectProps = DocumentUtils.parseProperties(document);
                docModel.setProperties(partMeta.getLabel(), objectProps);
            }
        }
    }

    /**
     * extractPart extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    protected Map<String, Object> extractPart(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
    	Map<String, Object> result = null;
    	
        MediaType mt = MediaType.valueOf(partMeta.getContent().getContentType());
        if (mt.equals(MediaType.APPLICATION_XML_TYPE)){
            Map<String, Object> objectProps = docModel.getProperties(schema);
            //unqualify properties before sending the doc over the wire (to save bandwidh)
            //FIXME: is there a better way to avoid duplication of a collection?
            Map<String, Object> unQObjectProperties = new HashMap<String, Object>();
            Set<Entry<String, Object>> qualifiedEntries = objectProps.entrySet();
            for(Entry<String, Object> entry : qualifiedEntries){
                String unqProp = getUnQProperty(entry.getKey());
                unQObjectProperties.put(unqProp, entry.getValue());
            }
            result = unQObjectProperties;
        } //TODO: handle other media types
        
        return result;
    }
    
    public AuthorityRefList getAuthorityRefs(
                DocumentWrapper<DocumentModel> docWrapper,
    		List<String> authRefFields) throws PropertyException {
    	AuthorityRefList authRefList = new AuthorityRefList();
        try {
            DocumentModel docModel = docWrapper.getWrappedObject();
            List<AuthorityRefList.AuthorityRefItem> list = 
            	authRefList.getAuthorityRefItem();

            for(String field:authRefFields){
                String refName = (String)docModel.getPropertyValue(field);
                if(refName==null)
                    continue;
            	try{
                    RefNameUtils.AuthorityTermInfo termInfo =
                        RefNameUtils.parseAuthorityTermInfo(refName);
                    AuthorityRefList.AuthorityRefItem ilistItem =
                        new AuthorityRefList.AuthorityRefItem();
                    ilistItem.setRefName(refName);
                    ilistItem.setAuthDisplayName(termInfo.inAuthority.displayName);
                    ilistItem.setItemDisplayName(termInfo.displayName);
                    ilistItem.setSourceField(field);
                    ilistItem.setUri(termInfo.getRelativeUri());
                    list.add(ilistItem);
            	} catch( Exception e ) {
                    // FIXME: Do we need to throw this Exception here?
                    if (logger.isDebugEnabled()) {
                        logger.debug("Caught exception in getAuthorityRefs", e);
                    }
            	}
            }
        } catch (PropertyException pe) {
            String msg =
                "Attempted to retrieve value for invalid or missing authority field. " +
                "Check authority field properties in tenant bindings.";
            logger.warn(msg, pe);
            throw pe;
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception in getAuthorityRefs", e);
            }
            Response response = Response.status(
                    Response.Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve authority references").type("text/plain").build();
            throw new WebApplicationException(response);
        }
        return authRefList;
    }


}
