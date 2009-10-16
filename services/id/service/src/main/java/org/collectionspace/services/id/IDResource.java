/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.id;

import java.io.StringWriter;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

// May at some point instead use
// org.jboss.resteasy.spi.NotFoundException
import org.collectionspace.services.common.repository.BadRequestException;
import org.collectionspace.services.common.repository.DocumentNotFoundException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IDResource
 *
 * Resource class to handle requests to the ID Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */
// Set the base path component for URLs that access this service.
@Path("/idgenerators")
// Identify the default MIME media types consumed and produced by this service.
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class IDResource {

    final Logger logger = LoggerFactory.getLogger(IDResource.class);
    final static IDService service = new IDServiceJdbcImpl();

    // Query parameter names and values.
    final static String QUERY_PARAM_LIST_FORMAT = "format";
    final static String LIST_FORMAT_SUMMARY = "summary";
    final static String LIST_FORMAT_FULL = "full";
    final static String QUERY_PARAM_ID_GENERATOR_ROLE = "role";

    // XML namespace for the ID Service.
    final static String ID_SERVICE_NAMESPACE =
       "http://collectionspace.org/services/id";
    final static String ID_SERVICE_NAMESPACE_PREFIX = "ns2";
    
    // Names of elements for ID generator instances, lists and list items.
    final static String ID_GENERATOR_NAME = "idgenerator";
    final static String ID_GENERATOR_LIST_NAME = "idgenerator-list";
    final static String ID_GENERATOR_LIST_ITEM_NAME = "idgenerator-list-item";

    // Base URL path for REST-based requests to the ID Service.
    //
    // @TODO Investigate whether this can be obtained from the
    // value used in the class-level @PATH annotation, above.
    final static String BASE_URL_PATH = "/idgenerators";

    //////////////////////////////////////////////////////////////////////
    /**
     * Constructor (no argument).
     */
    public IDResource() {
        // do nothing
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Generates and returns a new ID, from the specified ID generator.
     *
     * @param  csid  An identifier for an ID generator.
     *
     * @return  A new ID created ("generated") by the specified ID generator.
     */
    @POST
    @Path("/{csid}/ids")
    public Response newID(@PathParam("csid") String csid) {

        logger.debug("> in newID(String)");

        // @TODO The JavaDoc description reflects an as-yet-to-be-carried out
        // refactoring, in which the highest object type in the ID service
        // is that of an IDGenerator, some or all of which may be composed
        // of IDParts.  Some IDGenerators generate IDs based on patterns,
        // which may be composed in part of incrementing numeric or alphabetic
        // components, while others may not (e.g. UUIDs, web services-based
        // responses).

        // @TODO We're currently using simple integer IDs to identify ID generators
        // in this initial iteration.
        //
        // To uniquely identify ID generators in production, we'll need to handle
        // both CollectionSpace IDs (csids) - a form of UUIDs/GUIDs - and some
        // other form of identifier to be determined, such as URLs or URNs.

        // @TODO We're currently returning IDs in plain text.  Identify whether
        // there is a requirement to return an XML representation, and/or any
        // other representations.

        ResponseBuilder builder = Response.ok();
        Response response = builder.build();

        String newId = "";
        try {

            // Obtain a new ID from the specified ID generator,
            // and return it in the entity body of the response.
            newId = service.createID(csid);

            if (newId == null || newId.trim().isEmpty()) {
                response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("ID Service returned null or empty ID")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
                return response;
            }

            response =
                Response.status(Response.Status.CREATED)
                    .entity(newId)
                    .type(MediaType.TEXT_PLAIN)
                    .build();

            // @TODO Return an XML-based error results format with the
            // responses below.

            // @TODO An IllegalStateException often indicates an overflow
            // of an IDPart.  Consider whether returning a 400 Bad Request
            // status code is still warranted, or whether returning some other
            // status would be more appropriate.

        } catch (DocumentNotFoundException dnfe) {
            response = Response.status(Response.Status.NOT_FOUND)
                .entity(dnfe.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (BadRequestException bre) {
            response = Response.status(Response.Status.BAD_REQUEST)
                .entity(bre.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (IllegalStateException ise) {
            response = Response.status(Response.Status.BAD_REQUEST)
                .entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (IllegalArgumentException iae) {
            response = Response.status(Response.Status.BAD_REQUEST)
                .entity(iae.getMessage()).type(MediaType.TEXT_PLAIN).build();

            // This is guard code that should never be reached.
        } catch (Exception e) {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        return response;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Creates a new ID generator instance.
     *
     * @param  generatorRepresentation
     *         A representation of an ID generator instance.
     */
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_XML)
    public Response createIDGenerator() {

        logger.debug("> in createIDGenerator(String)");

        // @TODO Implement this stubbed method

        // @TODO Replace this placeholder code.
        Response response =
            Response.status(Response.Status.CREATED)
                .entity("")
                .type(MediaType.TEXT_PLAIN)
                .build();

        /*
        // @TODO Replace this placeholder code.
        // Return a URL for the newly-created resource in the
        // Location header
        String csid = "TEST-1";
        String url = "/idgenerators/" + csid;
        List locationList = Collections.singletonList(url);
        response.getMetadata()
        .get("Location")
        .putSingle("Location", locationList);
         */

        return response;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a representation of a single ID generator instance resource.
     *
     * @param    csid  An identifier for an ID generator instance.
     *
     * @return  A representation of an ID generator instance resource.
     */
    @GET
    @Path("/{csid}")
    @Produces(MediaType.APPLICATION_XML)
    public Response readIDGenerator(@PathParam("csid") String csid) {

        logger.debug("> in readIDGenerator(String)");

        ResponseBuilder builder = Response.ok();
        Response response = builder.build();

        String resourceRepresentation = "";
        try {

            IDGeneratorInstance instance = service.readIDGenerator(csid);

            Document doc = DocumentHelper.createDocument();
            Element root = doc.addElement(ID_GENERATOR_NAME);
            Namespace namespace =
                new Namespace(ID_SERVICE_NAMESPACE_PREFIX, ID_SERVICE_NAMESPACE);
            doc.getRootElement().add(namespace);

            Element generatorElement = root.addElement(ID_GENERATOR_NAME);
            // Add summary data about this ID generator instance.
            generatorElement = addInstanceElementSummary(generatorElement, csid);
            // Add detailed data about this ID generator instance.
            generatorElement =
                addInstanceElementDetails(generatorElement, csid, instance);

            try {
                resourceRepresentation = prettyPrintXML(doc);
            } catch(Exception e) {
                logger.debug("Error pretty-printing XML: " + e.getMessage());
                resourceRepresentation = doc.asXML();
            }

            if (
                resourceRepresentation == null ||
                resourceRepresentation.trim().isEmpty()
            ) {
                response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("ID Service returned null or empty ID Generator")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
                return response;
            }

            response =
                Response.status(Response.Status.OK)
                    .entity(resourceRepresentation)
                    .type(MediaType.APPLICATION_XML)
                    .build();

        // @TODO Return an XML-based error results format with the
        // responses below.

        } catch (DocumentNotFoundException dnfe) {
            response =
                Response.status(Response.Status.NOT_FOUND)
                    .entity(dnfe.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (IllegalStateException ise) {
            response =
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(ise.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        } catch (IllegalArgumentException iae) {
            response =
                Response.status(Response.Status.BAD_REQUEST)
                    .entity(iae.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

            // This is guard code that should never be reached.
        } catch (Exception e) {
            response =
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        return response;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Retrieve a list of available ID Generator instance resources.
     *
     * Note: This REST method is required by a HEAD method test
     * in org.collectionspace.services.client.test.ServiceLayerTest.
     *
     * @param   format  A representation ("format") in which to return list items,
     *                  such as a "full" or "summary" format.
     *
     * @return  A list of representations of ID generator instance resources.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_XML)
    public Response readIDGeneratorsList(
        @QueryParam(QUERY_PARAM_LIST_FORMAT) String listFormat,
        @QueryParam(QUERY_PARAM_ID_GENERATOR_ROLE) String role) {

        logger.debug("> in readIDGeneratorsList()");

        // @TODO The names and values of the query parameters above
        // ("format"and "role") are arbitrary, as are the format of the
        // results returned.  These should be standardized and
        // referenced project-wide.

        ResponseBuilder builder = Response.ok();
        Response response = builder.build();

        String resourceRepresentation = "";

        // @TODO We're currently overloading the String items in
        // the 'generators' list with distinctly different types of
        // list data, depending on the list format requested.  This may
        // or may not be a good idea.
        try {

            Map<String,IDGeneratorInstance> generators =
                    service.readIDGeneratorsList();

            // @TODO Filtering by role will likely take place here ...

            // Default to summary list if no list format is specified.
            if (listFormat == null || listFormat.trim().isEmpty()) {
                resourceRepresentation = formattedSummaryList(generators);
            } else if (listFormat.equalsIgnoreCase(LIST_FORMAT_SUMMARY)) {
                resourceRepresentation = formattedSummaryList(generators);
            } else if (listFormat.equalsIgnoreCase(LIST_FORMAT_FULL)) {
                resourceRepresentation = formattedFullList(generators);
            // Return an error if the value of the query parameter
            // is unrecognized.
            } else {
                // @TODO Return an appropriate XML-based entity body upon error.
                String msg = "Query parameter '" + listFormat + "' was not recognized.";
                logger.debug(msg);
                response =
                    Response.status(Response.Status.BAD_REQUEST)
                        .entity("")
                        .type(MediaType.TEXT_PLAIN)
                        .build();
            }

            // Filter list by role
            //
            // @TODO Checking for roles here is a short-term expedient;
            // this should likely be done through a database join on a
            // table that associates ID generator roles to ID generator
            // instances.

            // @TODO The summary list currently returns only CSIDs.
            // It should additionally return relative URLs,
            // and possibly also human-readable descriptions.

            // If the request didn't filter by role, return all
            // ID generator instances.
            if (role == null || role.trim().isEmpty()) {
                if (generators != null) {
                    // Do nothing
                }
                // Otherwise, return only ID generator instances
                // matching the requested role.
            } else {
                // @TODO Implement this stubbed code.
            }          

            response =
                Response.status(Response.Status.OK)
                    .entity(resourceRepresentation)
                    .type(MediaType.APPLICATION_XML)
                    .build();

        // @TODO Return an XML-based error results format with the
        // responses below.
        } catch (IllegalStateException ise) {
            response =
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ise.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();

        // This is guard code that should never be reached.
        } catch (Exception e) {
            response =
                Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e.getMessage())
                    .type(MediaType.TEXT_PLAIN)
                    .build();
        }

        return response;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Identifies whether the specified ID generator instance can
     * generate and validate IDs in a specified role (aka type or context).
     *
     * Example: Can a particular ID generator instance generate IDs for
     * accession numbers?  For intake numbers?
     *
     * @param   csid   A CollectionSpace ID (CSID) identifying an
     *                 ID generator instance.
     *
     * @param   role   A role (aka type or context) in which that
     *                 ID generator instance can generate and
     *                 validate IDs.
     *
     * @return  True if the specified ID generator can generate and validate
     *          IDs in the specified role; false if it cannot.
     */
    private boolean generatorHasRole(String csid, String role) {

        // @TODO Implement this stubbed method, replacing
        // this with a lookup of associations of ID generator
        // instances to ID generator roles; perhaps in the
        // short term with an external configuration file
        // and ultimately in a database table.

        return true;

        // Pseudocode (with static string examples) of what we might
        // want to do instead:
        //
        // getCSID(), below, would retrieve the value of the <csid> element,
        // present in all list formats for ID generator instances,
        // via xpath or similar.
        //
        // if (csid.equals("1a67470b-19b1-4ae3-88d4-2a0aa936270e")
        //        && role.equalsIgnoreCase("ID_ROLE_ACCESSION_NUMBER")) {
        //     // Return true if the ID generator instance identified by
        //     // the provided CSID is associated with the provided role.
        //     return true;
        // } else {
        //     return false;
        // }

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a summary list of ID generator instances.
     *
     * This is an XML-based list format that returns only
     * basic data about each ID generator instance, along
     * with relative URIs that may be used to retrieve more
     * data about each instance.
     *
     * @param   generators A list of ID generator instances, each
     *                     containing a CollectionSpace ID (CSID).
     *
     * @return  A summary list of ID generator instances.
     */
    private String formattedSummaryList(
        Map<String,IDGeneratorInstance> generators) {

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(ID_GENERATOR_LIST_NAME);
        Namespace namespace =
            new Namespace(ID_SERVICE_NAMESPACE_PREFIX, ID_SERVICE_NAMESPACE);
        doc.getRootElement().add(namespace);

        Element listitem = null;
        for (String csid : generators.keySet() )
        {
            listitem = root.addElement(ID_GENERATOR_LIST_ITEM_NAME);
            // Add summary data about this ID generator instance.
            listitem = addInstanceElementSummary(listitem, csid);
        }

        String summaryList = "";
        try {
            summaryList = prettyPrintXML(doc);
        } catch(Exception e) {
            logger.debug("Error pretty-printing XML: " + e.getMessage());
            summaryList = doc.asXML();
        }

        return summaryList;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a full list of ID generator instances.
     *
     * This is an XML-based list format that returns
     * full data about each ID generator instance.
     *
     * @param   generators A list of ID generator instances, each
     *                     containing a CollectionSpace ID (CSID).
     *
     * @return  A full list of ID generator instances.
     */
    private String formattedFullList(
        Map<String,IDGeneratorInstance> generators) {

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(ID_GENERATOR_LIST_NAME);
        Namespace namespace =
            new Namespace(ID_SERVICE_NAMESPACE_PREFIX, ID_SERVICE_NAMESPACE);
        doc.getRootElement().add(namespace);

        Element listitem = null;
        for (String csid : generators.keySet() )
        {
            listitem = root.addElement(ID_GENERATOR_LIST_ITEM_NAME);
            // Add summary data about this ID generator instance.
            listitem = addInstanceElementSummary(listitem, csid);
            // Add detailed data about this ID generator instance.
            listitem =
                addInstanceElementDetails(listitem, csid, generators.get(csid));
        }

        String summaryList = "";
        try {
            summaryList = prettyPrintXML(doc);
        } catch(Exception e) {
            logger.debug("Error pretty-printing XML: " + e.getMessage());
            summaryList = doc.asXML();
        }

        return summaryList;
    }

    private Element addInstanceElementSummary(Element instanceElement,
        String csidValue) {

        Element csid = null;
        Element uri = null;
        csid = instanceElement.addElement("csid");
        csid.addText(csidValue);
        uri = instanceElement.addElement("uri");
        uri.addText(getRelativePath(csidValue));

        return instanceElement;
    }


    private Element addInstanceElementDetails(Element instanceElement,
        String csidValue, IDGeneratorInstance generatorInstance) {

        Element displayname = instanceElement.addElement("displayname");
        displayname.addText(generatorInstance.getDisplayName());
        Element description = instanceElement.addElement("description");
        description.addText(generatorInstance.getDescription());
        Element generator = instanceElement.addElement("idgenerator");
        // Using the CSID as a key, get the XML string
        // representation of the ID generator.
        String generatorStr = generatorInstance.getGeneratorState();
        // Convert the XML string representation of the
        // ID generator to a new XML document, copy its
        // root element, and append it to the relevant location
        // in the current list item.
        try {
            Document generatorDoc = textToXMLDocument(generatorStr);
            Element generatorRoot = generatorDoc.getRootElement();
            generator.add(generatorRoot.createCopy());
        // If an error occurs parsing the XML string representation,
        // the text of the ID generator element will remain empty.
        } catch (Exception e) {
            logger.warn("Error parsing XML text: " + generatorStr);
        }

        return instanceElement;
    }


    // @TODO Refactoring opportunity: the utility methods below
    // might be moved into the 'common' module.

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a 'pretty printed' String representation of
     * an XML document.
     *
     * Uses the default settings for indentation, whitespace, etc.
     * of a pre-defined dom4j output format.
     *
     * @param   doc  A dom4j XML Document.
     *
     * @return  A pretty-printed String representation of that document.
     */
    private String prettyPrintXML(Document doc)
       throws Exception {

        StringWriter sw = new StringWriter();
        try {
            final OutputFormat PRETTY_PRINT_FORMAT =
                OutputFormat.createPrettyPrint();
            final XMLWriter writer =
                new XMLWriter(sw, PRETTY_PRINT_FORMAT);
            // Print the document to the current writer.
            writer.write(doc);
        }
        catch (Exception e) {
            throw e;
        }
        return sw.toString();
    }

     //////////////////////////////////////////////////////////////////////
    /**
     * Returns an XML document, when provided with a String
     * representation of that XML document.
     *
     * @param   xmlStr  A String representation of an XML document.
     *
     * @return  A dom4j XML document.
     */
    private Document textToXMLDocument(String xmlStr) throws Exception {
        
        Document doc = null;
        try {
         doc = DocumentHelper.parseText(xmlStr);
        } catch (DocumentException e) {
          throw e;
        }
        return doc;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a relative URI path to a resource
     * that represents an instance of an ID generator.
     *
     * @param   csid  A CollectionSpace ID (CSID).
     *
     * @return  A relative URI path to a resource that
     *          represents an ID generator instance.
     */
    private String getRelativePath(String csid) {
      if (csid !=null && ! csid.trim().isEmpty()) {
        return BASE_URL_PATH + "/" + csid;
      } else {
        return BASE_URL_PATH;
      }
    }

}
