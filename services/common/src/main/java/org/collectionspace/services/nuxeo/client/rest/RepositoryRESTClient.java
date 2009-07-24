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
package org.collectionspace.services.nuxeo.client.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.collectionspace.services.common.repository.DocumentException;
import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;

import org.restlet.data.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.ServiceConfig;
import org.collectionspace.services.common.ServiceConfig.NuxeoClientConfig;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;
import org.collectionspace.services.common.repository.DocumentHandler.Action;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultDocument;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

/**
 * RepositoryRESTClient is used to perform CRUD operations on documents
 * in Nuxeo repository using Nuxeo RESTful APIs. It uses @see DocumentHandler
 * as IOHandler with the client.
 *
 * v2 NuxeoClient
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RepositoryRESTClient implements RepositoryClient {

    private final Logger logger = LoggerFactory.getLogger(RepositoryRESTClient.class);
    private NuxeoRESTClient nuxeoRestClient;

    public RepositoryRESTClient() {
    }

    @Override
    public String create(String serviceName, String docType, DocumentHandler handler) throws BadRequestException, DocumentException {
        if(serviceName == null){
            throw new IllegalArgumentException("RepositoryRESTClient.create: serviceName is missing");
        }
        if(docType == null){
            throw new IllegalArgumentException("RepositoryRESTClient.create: docType is missing");
        }
        if(handler == null){
            throw new IllegalArgumentException("RepositoryRESTClient.create: handler is missing");
        }
        ServiceMain smain = ServiceMain.getInstance();
        String nuxeoWspaceId = smain.getWorkspaceId(serviceName);
        if(nuxeoWspaceId == null){
            throw new DocumentNotFoundException("Unable to find workspace for service " + serviceName +
                    " check if the mapping exists in service-config.xml or " +
                    " the the mapped workspace exists in the Nuxeo repository");
        }
        try{
            RepresentationHandler repHandler = (RepresentationHandler) handler;
            repHandler.prepare(Action.CREATE);
            List<String> pathParams = new ArrayList<String>();
            pathParams.add("default");
            pathParams.add(nuxeoWspaceId);
            pathParams.add("createDocument");
            if(repHandler.getPathParams().size() > 0){
                pathParams.addAll(repHandler.getPathParams());
            }
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("docType", docType);
            // a default title for the Dublin Core schema
            queryParams.put("dublincore:title", "CollectionSpace-" + docType);
            if(repHandler.getQueryParams().size() > 0){
                queryParams.putAll(repHandler.getQueryParams());
            }

            String completeURL = getNuxeoRestClient().buildUrl(pathParams, queryParams);
            if(logger.isDebugEnabled()){
                logger.debug("create using url=" + completeURL);
            }
            Request request = buildRequest(Method.POST, completeURL);

            //write out create stream, this is not needed as queryParams
            //contain the document
            final InputStream in = new ByteArrayInputStream(new byte[0]);
            request.setEntity(new OutputRepresentation(
                    MediaType.MULTIPART_FORM_DATA) {

                @Override
                public void write(OutputStream outputStream) throws IOException {
                    byte[] buffer = new byte[1024 * 64];
                    int read;
                    while((read = in.read(buffer)) != -1){
                        outputStream.write(buffer, 0, read);
                    }
                }
            });
            //following call to handler.handle is not needed as queryparams
            //contains the real data
            RepresentationWrapper wrapDoc = new RepresentationWrapper(new DefaultDocument());
            repHandler.handle(Action.CREATE, wrapDoc);
            //read response
            //Nuxeo does not set 201 SUCCESS_CREATED on successful creation
            Document document = executeRequest(request, completeURL, Status.SUCCESS_OK);
            //handle is not needed on create as queryparams have all data
            return extractId(document);

        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
        }
    }

    @Override
    public void get(String id, DocumentHandler handler) throws DocumentNotFoundException, DocumentException {

        if(handler == null){
            throw new IllegalArgumentException("RepositoryRESTClient.get: handler is missing");
        }

        try{
            RepresentationHandler repHandler = (RepresentationHandler) handler;
            repHandler.prepare(Action.GET);
            ArrayList pathParams = new ArrayList();
            pathParams.add("default");
            pathParams.add(id);
            pathParams.add("export");
            if(repHandler.getPathParams().size() > 0){
                pathParams.addAll(repHandler.getPathParams());
            }
            HashMap<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("format", "XML");
            if(repHandler.getQueryParams().size() > 0){
                queryParams.putAll(repHandler.getQueryParams());
            }
            String completeURL = getNuxeoRestClient().buildUrl(pathParams, queryParams);
            if(logger.isDebugEnabled()){
                logger.debug("get using url=" + completeURL);
            }
            Request request = buildRequest(Method.GET, completeURL);
            Document document = executeRequest(request, completeURL, Status.SUCCESS_OK);
            RepresentationWrapper wrapDoc = new RepresentationWrapper(document);
            repHandler.handle(Action.GET, wrapDoc);
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
        }
    }

    @Override
    public void getAll(String serviceName, DocumentHandler handler) throws DocumentNotFoundException, DocumentException {
        if(serviceName == null){
            throw new IllegalArgumentException("RepositoryRESTClient.getAll: serviceName is missing");
        }
        if(handler == null){
            throw new IllegalArgumentException("RepositoryRESTClient.getAll: handler is missing");
        }
        ServiceMain smain = ServiceMain.getInstance();
        String nuxeoWspaceId = smain.getWorkspaceId(serviceName);
        if(nuxeoWspaceId == null){
            throw new DocumentNotFoundException("Unable to find workspace for service " + serviceName +
                    " check if the mapping exists in service-config.xml or " +
                    " the the mapped workspace exists in the Nuxeo repository");
        }
        try{
            RepresentationHandler repHandler = (RepresentationHandler) handler;
            repHandler.prepare(Action.GET_ALL);
            ArrayList pathParams = new ArrayList();
            pathParams.add("default");
            pathParams.add(nuxeoWspaceId);
            pathParams.add("browse");
            if(repHandler.getPathParams().size() > 0){
                pathParams.addAll(repHandler.getPathParams());
            }
            String completeURL = getNuxeoRestClient().buildUrl(pathParams, repHandler.getQueryParams());
            if(logger.isDebugEnabled()){
                logger.debug("getAll using url=" + completeURL);
            }
            Request request = buildRequest(Method.GET, completeURL);
            Document document = executeRequest(request, completeURL, Status.SUCCESS_OK);
            RepresentationWrapper wrapDoc = new RepresentationWrapper(document);
            repHandler.handle(Action.GET_ALL, wrapDoc);
        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
        }
    }

    @Override
    public void update(String id, DocumentHandler handler) throws BadRequestException, DocumentNotFoundException, DocumentException {
        if(handler == null){
            throw new IllegalArgumentException("RepositoryRESTClient.update: handler is missing");
        }

        try{
            RepresentationHandler repHandler = (RepresentationHandler) handler;
            repHandler.prepare(Action.UPDATE);
            List<String> pathParams = new ArrayList<String>();
            pathParams.add("default");
            pathParams.add(id);
            pathParams.add("updateDocumentRestlet");
            if(repHandler.getPathParams().size() > 0){
                pathParams.addAll(repHandler.getPathParams());
            }
            String completeURL = getNuxeoRestClient().buildUrl(pathParams, repHandler.getQueryParams());
            if(logger.isDebugEnabled()){
                logger.debug("update using url=" + completeURL);
            }
            //repHandler.handle is not needed as queryParams contain all the data
            RepresentationWrapper wrapDoc = new RepresentationWrapper(new DefaultDocument());
            repHandler.handle(Action.UPDATE, wrapDoc);
            Request request = buildRequest(Method.PUT, completeURL);
            Document document = executeRequest(request, completeURL, Status.SUCCESS_OK);

        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
        }
    }

    @Override
    public void delete(String id) throws DocumentNotFoundException, DocumentException {

        if(logger.isDebugEnabled()){
            logger.debug("deleting document with id=" + id);
        }

        try{
            List<String> pathParams = new ArrayList<String>();
            pathParams.add("default");
            pathParams.add(id);
            pathParams.add("deleteDocumentRestlet");

            Map<String, String> queryParams = new HashMap<String, String>();
            String completeURL = getNuxeoRestClient().buildUrl(pathParams, queryParams);
            if(logger.isDebugEnabled()){
                logger.debug("delete using url=" + completeURL);
            }
            Request request = buildRequest(Method.DELETE, completeURL);
            Document document = executeRequest(request, completeURL, Status.SUCCESS_OK);
            //FIXME error handling?
            //            Document document = service.deleteCollectionObject(csid);
//            Element root = document.getRootElement();
//            for(Iterator i = root.elementIterator(); i.hasNext();){
//                Element element = (Element) i.next();
//                if("docRef".equals(element.getName())){
//                    String status = (String) element.getData();
//                    verbose("deleteCollectionObjectt response: " + status);
//                }
//            }

        }catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("Caught exception ", e);
            }
            throw new DocumentException(e);
        }finally{
        }
    }

    /**
     * buildRequest build HTTP request given parameters
     * @param method
     * @param completeURL
     * @return
     */
    private Request buildRequest(Method method, String completeURL) {
        Request request = new Request(method, completeURL);
        getNuxeoRestClient().setupAuth(request);
        getNuxeoRestClient().setupCookies(request);
        return request;
    }

    /**
     * executeRequest execute given HTTP request
     * @param request
     * @param completeURL
     * @return
     * @throws Exception
     */
    private Document executeRequest(Request request, String completeURL, Status expected) throws Exception {
        //execute
        Response res = getNuxeoRestClient().getRestClient().handle(request);
        Status status = res.getStatus();
        if(status.getCode() != expected.getCode()){
            logger.error("failed to execute request=" + request.getMethod() +
                    " with error status=" + status +
                    " url=" + completeURL +
                    " response=" + res.toString());
            throw new DocumentException(status.getCode(), status.getDescription());
        }
        Representation rep = res.getEntity();

        //read response
        return retrieveResponse(rep);
    }

    /**
     * retrieveResponse retrieves DOM document from Restlet Represeantion
     * @param request
     * @return
     * @throws Exception
     */
    private Document retrieveResponse(Representation rep) throws Exception {
        SAXReader reader = new SAXReader();
        Document document = reader.read(rep.getStream());
        return document;
    }

    /**
     * extractId extract document id from response
     * @param document
     * @return
     */
    private String extractId(Document document) {
        String csid = null;
        Element root = document.getRootElement();
        for(Iterator i = root.elementIterator(); i.hasNext();){
            Element element = (Element) i.next();
            if("docRef".equals(element.getName())){
                csid = (String) element.getData();
                break;
            }
        }
        return csid;
    }

    private NuxeoRESTClient getNuxeoRestClient() {
        if(nuxeoRestClient == null){
            ServiceConfig sconfig = ServiceMain.getInstance().getServiceConfig();
            NuxeoClientConfig nxConfig = sconfig.getNuxeoClientConfig();
            String protocol = "http";
            if(nxConfig.getProtocol() != null && !"".equals(nxConfig.getProtocol())){
                protocol = nxConfig.getProtocol();
            }
            NuxeoRESTClient tempClient = new NuxeoRESTClient(protocol,
                    nxConfig.getHost(), "" + nxConfig.getPort());

            tempClient.setBasicAuthentication(nxConfig.getUser(), nxConfig.getPassword());

            nuxeoRestClient = tempClient;

        }
        return nuxeoRestClient;
    }
}
