/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * CollectionSpaceClientUtils.
 *
 * Base abstract class on which client tests of services are based.
 *
 * $LastChangedRevision: 2261 $
 * $LastChangedDate: 2010-05-28 16:52:22 -0700 (Fri, 28 May 2010) $
 */

// FIXME: http://issues.collectionspace.org/browse/CSPACE-1685

public class CollectionSpaceClientUtils {

    //Maven's base directory -i.e., the one containing the current pom.xml
    /** The Constant logger. */
    private static final Logger logger = LoggerFactory.getLogger(CollectionSpaceClientUtils.class);
        
    protected static final int STATUS_BAD_REQUEST =
        Response.Status.BAD_REQUEST.getStatusCode();
    protected static final int STATUS_CREATED =
        Response.Status.CREATED.getStatusCode();
    protected static final int STATUS_NOT_FOUND =
        Response.Status.NOT_FOUND.getStatusCode();
    protected static final int STATUS_OK =
        Response.Status.OK.getStatusCode();

    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    static public String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        return extractIdFromResponseMetadata(mvm);
    }
 
    /**
     * Extract id.
     *
     * @param res the res
     * @return the string
     */
    static public String extractId(Response res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        return extractIdFromResponseMetadata(mvm);
    }
 
    static protected String extractIdFromResponseMetadata(MultivaluedMap<String, Object> mvm) {
    	// mvm may return a java.net.URI which complains about casting to String...
    	String uri = ((List<Object>) mvm.get("Location")).get(0).toString();
        if (logger.isDebugEnabled()) {
            logger.debug("extractId:uri=" + uri);
        }
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        if (logger.isDebugEnabled()) {
            logger.debug("id=" + id);
        }
        return id;
    }
 
    /**
     * Extract part.
     *
     * @param input the input
     * @param label the label
     * @param clazz the clazz
     * @return the object
     * @throws Exception the exception
     */
    static protected Object extractPart(PoxPayloadIn input, String label, Class<?> clazz)
            throws Exception {
    	Object result = null;
    	PayloadInputPart payloadInputPart = input.getPart(label);
        if (payloadInputPart != null) {
        	result = payloadInputPart.getBody();
        } else if (logger.isWarnEnabled() == true) {
        	logger.warn("Payload part: " + label +
        			" is missing from payload: " + input.getName());
        }
        return result;
    }
    
    /**
     * Extract part.
     *
     * @param input the input
     * @param label the label
     * @param clazz the clazz
     * @return the object
     * @throws Exception the exception
     */
    @Deprecated
    static public Object extractPart(MultipartInput input, String label, Class<?> clazz)
            throws Exception {
        Object obj = null;
        String partLabel = "";
        List<InputPart> parts = input.getParts();
        if (parts.size() == 0) {
            logger.warn("No parts found in multipart body.");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Parts:");
            for (InputPart part : parts) {
                partLabel = part.getHeaders().getFirst("label");
                logger.debug("part = " + partLabel);
            }
        }
        boolean partLabelMatched = false;
        for (InputPart part : parts) {
            partLabel = part.getHeaders().getFirst("label");
            if (label.equalsIgnoreCase(partLabel)) {
                partLabelMatched = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("found part" + partLabel);
                }
                String partStr = part.getBodyAsString();
                if (partStr == null || partStr.trim().isEmpty()) {
                    logger.warn("Part '" + label + "' in multipart body is empty.");
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("extracted part as str=\n" + partStr);
                    }
                    obj = part.getBody(clazz, null);
                    if (logger.isDebugEnabled()) {
                        logger.debug("extracted part as obj=\n",
                                objectAsXmlString(obj, clazz));
                    }
                }
                break;
            }
        }
        if (!partLabelMatched) {
            logger.warn("Could not find part '" + label + "' in multipart body.");
            // In the event that getBodyAsString() or getBody(), above, do *not*
            // throw an IOException, but getBody() nonetheless retrieves a null object.
            // This *may* be unreachable.
        } else if (obj == null) {
            logger.warn("Could not extract part '" + label
                    + "' in multipart body as an object.");
        }
        return obj;
    }

    /**
     * Gets the part object.
     *
     * @param partStr the part str
     * @param clazz the clazz
     * @return the part object
     * @throws JAXBException the jAXB exception
     */
    static public Object getPartObject(String partStr, Class<?> clazz)
            throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        ByteArrayInputStream bais = null;
        Object obj = null;
        try {
            bais = new ByteArrayInputStream(partStr.getBytes());
            Unmarshaller um = jc.createUnmarshaller();
            obj = um.unmarshal(bais);
        } finally {
            if (bais != null) {
                try {
                    bais.close();
                } catch (Exception e) {
                	if (logger.isDebugEnabled() == true) {
                		e.printStackTrace();
                	}
                }
            }
        }
        return obj;
    }

    /**
     * Object as xml string.
     *
     * @param o the o
     * @param clazz the clazz
     * @return the string
     */
    static public String objectAsXmlString(Object o, Class<?> clazz) {
        StringWriter sw = new StringWriter();
        try {
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, sw);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    /**
     * getObjectFromFile get object of given class from given file (in classpath)
     * @param jaxbClass
     * @param fileName of the file to read to construct the object
     * @return
     * @throws Exception
     */
    static public Object getObjectFromFile(Class<?> jaxbClass, String fileName)
            throws Exception {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        InputStream is = tccl.getResourceAsStream(fileName);
        return getObjectFromStream(jaxbClass, is);
    }

    /**
     * Gets the xml document.
     *
     * @param fileName the file name
     * @return the xml document
     * @throws Exception the exception
     */
    static public Document getXmlDocument(String fileName) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        File f = new File(fileName);
        if (!f.exists()) {
            throw new IllegalArgumentException("test data file " + fileName + " not found!");
        }
        // Create the builder and parse the file
        return factory.newDocumentBuilder().parse(f);
    }

    /**
     * Gets the xml document as string.
     *
     * @param fileName the file name
     * @return the xml document as string
     * @throws Exception the exception
     */
    static public String getXmlDocumentAsString(String fileName) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(fileName));
        return new String(b);
    }

    /**
     * getObjectFromStream get object of given class from given inputstream
     * @param jaxbClass
     * @param is stream to read to construct the object
     * @return
     * @throws Exception
     */
    static public Object getObjectFromStream(Class<?> jaxbClass, InputStream is) throws Exception {
        JAXBContext context = JAXBContext.newInstance(jaxbClass);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        //note: setting schema to null will turn validator off
        unmarshaller.setSchema(null);
        return jaxbClass.cast(unmarshaller.unmarshal(is));
    }
}
