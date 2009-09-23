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
import javax.ws.rs.core.MediaType;
import org.collectionspace.services.common.repository.DocumentWrapper;
import org.collectionspace.services.common.repository.AbstractDocumentHandler;
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentUtils;
import org.collectionspace.services.common.service.ObjectPartType;
import org.collectionspace.services.nuxeo.client.*;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * DocumentModelHandler is a base abstract Nuxeo document handler
 * using Nuxeo Java Remote APIs for CollectionSpace services
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public abstract class DocumentModelHandler<T, TL>
        extends AbstractDocumentHandler<T, TL> {

    private final Logger logger = LoggerFactory.getLogger(DocumentModelHandler.class);
    private RepositoryInstance repositorySession;
    //key=schema, value=documentpart

    /**
     * getRepositorySession returns Nuxeo Repository Session
     * @return
     */
    public RepositoryInstance getRepositorySession() {
        return repositorySession;
    }

    /**
     * setRepositorySession sets repository session
     * @param repoSession
     */
    public void setRepositorySession(RepositoryInstance repoSession) {
        this.repositorySession = repoSession;
    }

    @Override
    public void handleCreate(DocumentWrapper wrapDoc) throws Exception {
        fillAllParts(wrapDoc);
    }

    @Override
    public void handleUpdate(DocumentWrapper wrapDoc) throws Exception {
        fillAllParts(wrapDoc);
    }

    @Override
    public void handleGet(DocumentWrapper wrapDoc) throws Exception {
        extractAllParts(wrapDoc);
    }

    @Override
    public void handleGetAll(DocumentWrapper wrapDoc) throws Exception {
        setCommonPartList(extractCommonPartList(wrapDoc));
    }

    @Override
    public void completeUpdate(DocumentWrapper wrapDoc) throws Exception {
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        //return at least those document part(s) that were received
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        List<InputPart> inputParts = getServiceContext().getInput().getParts();
        for(InputPart part : inputParts){
            String partLabel = part.getHeaders().getFirst("label");
            ObjectPartType partMeta = partsMetaMap.get(partLabel);
            extractObject(docModel, partLabel, partMeta);
        }
    }

    @Override
    public void extractAllParts(DocumentWrapper wrapDoc) throws Exception {

        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        String[] schemas = docModel.getDeclaredSchemas();
        Map<String, ObjectPartType> partsMetaMap = getServiceContext().getPartsMetadata();
        for(String schema : schemas){
            ObjectPartType partMeta = partsMetaMap.get(schema);
            if(partMeta == null){
                continue; //unknown part, ignore
            }
            extractObject(docModel, schema, partMeta);
        }
    }

    @Override
    public abstract T extractCommonPart(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public void fillAllParts(DocumentWrapper wrapDoc) throws Exception {

        //TODO filling extension parts should be dynamic
        //Nuxeo APIs lack to support stream/byte[] input, get/setting properties is
        //not an ideal way of populating objects.
        DocumentModel docModel = (DocumentModel) wrapDoc.getWrappedObject();
        MultipartInput input = getServiceContext().getInput();
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
            //skip if the part is not in metadata
            if(!partsMetaMap.containsKey(partLabel)){
                continue;
            }
            InputStream payload = part.getBody(InputStream.class, null);

            //check if this is an xml part
            if(part.getMediaType().equals(MediaType.APPLICATION_XML_TYPE)){
                if(payload != null){
                    Document document = DocumentUtils.parseDocument(payload);
                    //TODO: callback to handler if registered to validate the
                    //document
                    Map<String, Object> objectProps = DocumentUtils.parseProperties(document);
                    docModel.setProperties(partLabel, objectProps);
                }
            }
        }//rof

    }

    @Override
    public abstract void fillCommonPart(T obj, DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract TL extractCommonPartList(DocumentWrapper wrapDoc) throws Exception;

    @Override
    public abstract T getCommonPart();

    @Override
    public abstract void setCommonPart(T obj);

    @Override
    public abstract TL getCommonPartList();

    @Override
    public abstract void setCommonPartList(TL obj);

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.repository.DocumentHandler#getDocumentType()
     */
    @Override
    public abstract String getDocumentType();

    /**
     * extractObject extracts an XML object from given DocumentModel
     * @param docModel
     * @param schema of the object to extract
     * @param partMeta metadata for the object to extract
     * @throws Exception
     */
    protected void extractObject(DocumentModel docModel, String schema, ObjectPartType partMeta)
            throws Exception {
        MediaType mt = MediaType.valueOf(partMeta.getContent().getContentType());
        if(mt.equals(MediaType.APPLICATION_XML_TYPE)){
            Map<String, Object> objectProps = docModel.getProperties(schema);
            //unqualify properties before sending the doc over the wire (to save bandwidh)
            //FIXME: is there a better way to avoid duplication of a collection?
            Map<String, Object> unQObjectProperties = new HashMap<String, Object>();
            Set<Entry<String, Object>> qualifiedEntries = objectProps.entrySet();
            for(Entry<String, Object> entry : qualifiedEntries){
                String unqProp = getUnQProperty(entry.getKey());
                unQObjectProperties.put(unqProp, entry.getValue());
            }
            Document doc = DocumentUtils.buildDocument(partMeta, schema, unQObjectProperties);
            if(logger.isDebugEnabled()){
                DocumentUtils.writeDocument(doc, System.out);
            }
            getServiceContext().addOutputPart(schema, doc, partMeta.getContent().getContentType());
        } //TODO: handle other media types
    }

}
