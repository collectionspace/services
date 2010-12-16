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
 */
package org.collectionspace.services.nuxeo.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.io.Serializable;
import java.util.Map;
import java.util.StringTokenizer;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.model.DocumentPart;

import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.SingleDocumentReader;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDocumentWriter;

import org.nuxeo.ecm.core.search.api.client.querymodel.descriptor.QueryModelDescriptor;
import org.nuxeo.ecm.core.storage.sql.jdbc.ResultSetQueryResult;
import org.nuxeo.ecm.core.query.sql.NXQL;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities related to Nuxeo API
 */
public class NuxeoUtils {

    /** The logger. */
    private static Logger logger = LoggerFactory.getLogger(NuxeoUtils.class);

    /**
     * getDocument retrieve org.dom4j.Document from Nuxeo DocumentModel
     * @param repoSession
     * @param nuxeoDoc
     * @return
     * @throws DocumentException
     */
    public static Document getDocument(RepositoryInstance repoSession, DocumentModel nuxeoDoc)
            throws DocumentException {
        Document doc = null;
        DocumentWriter writer = null;
        DocumentReader reader = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try {
            baos = new ByteArrayOutputStream();
            //nuxeo io.impl begin
            reader = new SingleDocumentReader(repoSession, nuxeoDoc);
            writer = new XMLDocumentWriter(baos);
            DocumentPipe pipe = new DocumentPipeImpl();
            //nuxeo io.impl end
            pipe.setReader(reader);
            pipe.setWriter(writer);
            pipe.run();
            bais = new ByteArrayInputStream(baos.toByteArray());
            SAXReader saxReader = new SAXReader();
            doc = saxReader.read(bais);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Caught exception while processing document ", e);
            }
            throw new DocumentException(e);
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close();
            }
            try {
                if (bais != null) {
                    bais.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException ioe) {
                String msg = "Failed to close io streams";
                logger.error(msg + " {}", ioe);
                throw new DocumentException(ioe);
            }
        }
        return doc;
    }

    /**
     * Gets the document.
     *
     * @param repoSession the repo session
     * @param csid the csid
     *
     * @return the document
     *
     * @throws DocumentException the document exception
     */
    public static Document getDocument(RepositoryInstance repoSession, String csid)
            throws DocumentException {
        Document result = null;

        DocumentModel docModel = getDocumentModel(repoSession, csid);
        result = getDocument(repoSession, docModel);

        return result;
    }

    /**
     * Gets the workspace model.
     *
     * @param repoSession the repo session
     * @param workspaceName the workspace name
     *
     * @return the workspace model
     *
     * @throws DocumentException the document exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @throws ClientException the client exception
     */
    public static DocumentModel getWorkspaceModel(
            RepositoryInstance repoSession, String workspaceName)
            throws DocumentException, IOException, ClientException {
        DocumentModel result = null;
        //FIXME: commented out as this does not work without tenant qualification
        String workspaceUUID = null;
//		String workspaceUUID = ServiceMain.getInstance().getWorkspaceId(
//				workspaceName);
        DocumentRef workspaceRef = new IdRef(workspaceUUID);
        result = repoSession.getDocument(workspaceRef);

        return result;
    }

    /**
     * Gets the document model.
     *
     * @param repoSession the repo session
     * @param csid the csid
     *
     * @return the document model
     *
     * @throws DocumentException the document exception
     */
    public static DocumentModel getDocumentModel(
            RepositoryInstance repoSession, String csid)
            throws DocumentException {
        DocumentModel result = null;

        try {
            DocumentRef documentRef = new IdRef(csid);
            result = repoSession.getDocument(documentRef);
        } catch (ClientException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void printDocumentModel(DocumentModel docModel) throws Exception {
        String[] schemas = docModel.getDeclaredSchemas();
        for (int i = 0; schemas != null && i < schemas.length; i++) {
            logger.debug("Schema-" + i + "=" + schemas[i]);
        }

        DocumentPart[] parts = docModel.getParts();
        Map<String, Serializable> propertyValues = null;
        for (int i = 0; parts != null && i < parts.length; i++) {
            logger.debug("Part-" + i + " name =" + parts[i].getName());
            logger.debug("Part-" + i + " path =" + parts[i].getPath());
            logger.debug("Part-" + i + " schema =" + parts[i].getSchema().getName());
            propertyValues = parts[i].exportValues();
        }

    }

    /**
     * createPathRef creates a PathRef for given service context using given id
     * @param ctx
     * @param id
     * @return PathRef
     */
    public static DocumentRef createPathRef(ServiceContext ctx, String id) {
        return new PathRef("/" + ctx.getRepositoryDomainName() +
                "/" + "workspaces" +
                "/" + ctx.getRepositoryWorkspaceName() +
                "/" + id);
    }

    /*
     * We're using the "name" field of Nuxeo's DocumentModel to store
     * the CSID.
     */
    public static String getCsid(DocumentModel docModel) {
    	return docModel.getName();
    }
    
    /**
     * extractId extracts id from given path string
     * @param pathString
     * @return
     */
    @Deprecated
    public static String xextractId(String pathString) {
        if (pathString == null) {
            throw new IllegalArgumentException("empty pathString");
        }
        String id = null;
        StringTokenizer stz = new StringTokenizer(pathString, "/");
        int tokens = stz.countTokens();
        for (int i = 0; i < tokens - 1; i++) {
            stz.nextToken();
        }
        id = stz.nextToken(); //last token is id
        return id;
    }
    
    public static boolean documentExists(RepositoryInstance repoSession,
    		String csid) throws ClientException {
		boolean result = false;
		
		/*
		 * This is the code that Nuxeo support suggested, however it will not work with their
		 * remote API's -it only works locally.
		
				String qname = QueryModelDescriptor.prepareStringLiteral(csid);
				String statement = String.format(
						"SELECT ecm:uuid FROM Document WHERE ecm:name = %s", qname);
				ResultSetQueryResult res = (ResultSetQueryResult) repoSession
						.queryAndFetch(statement, "NXQL");
				result = res.hasNext();
				if (result = false) {
					if (logger.isDebugEnabled() == true) {
						logger.debug("Existance check failed for document with CSID = " + csid);
					}
				} else {
					//String uuid = (String) res.next().get(NXQL.ECM_UUID);
				}
		*/
		
		/*
		 * Until I hear back from Nuxeo, we can use the following code:
		 */
		String qname = QueryModelDescriptor.prepareStringLiteral(csid);
		String statement = String.format(
				"SELECT ecm:uuid FROM Document WHERE ecm:name = %s", qname);
//		ResultSetQueryResult res = (ResultSetQueryResult) repoSession
//				.queryAndFetch(statement, "NXQL");
		DocumentModelList  res = repoSession.query(statement, 1/*return no more than 1*/);//, "NXQL");

//		result = res.hasNext();
		result = res.iterator().hasNext();
		if (result = false) {
			if (logger.isDebugEnabled() == true) {
				logger.debug("Existance check failed for document with CSID = " + csid);
			}
		} else {
			//String uuid = (String) res.next().get(NXQL.ECM_UUID);
		}
			
		return result;
    }
}
