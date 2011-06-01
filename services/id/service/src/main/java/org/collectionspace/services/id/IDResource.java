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

import java.util.Map;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

// May at some point instead use
// org.jboss.resteasy.spi.NotFoundException
import org.collectionspace.services.common.XmlTools;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentNotFoundException;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;

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
    final static String ID_GENERATOR_COMPONENTS_NAME = "idgenerator-components";
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

        // @TODO The JavaDoc description reflects an as-yet-to-be-carried out
        // refactoring, in which the highest object type in the ID service
        // is that of an IDGenerator, some or all of which may be composed
        // of IDParts.  Some IDGenerators generate IDs based on patterns,
        // which may be composed in part of incrementing numeric or alphabetic
        // components, while others may not (e.g. UUIDs, web services-based
        // responses).

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

            // Obtain a new ID from the specified ID generator instance.
            newId = service.createID(csid);

            // If the new ID is empty, return an error response.
            if (newId == null || newId.trim().isEmpty()) {
                response =
                        Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("ID Service returned null or empty ID").type(MediaType.TEXT_PLAIN).build();
                return response;
            }

            // Build the response, setting the:
            // - HTTP Status code (to '201 Created')
            // - Content-type header (to the relevant media type)
            // - Entity body (to the new ID)
            response = Response.status(Response.Status.CREATED).entity(newId).type(MediaType.TEXT_PLAIN).build();

            // @TODO Return an XML-based error results format with the
            // responses below.

            // @TODO An IllegalStateException often indicates an overflow
            // of an IDPart.  Consider whether returning a 400 Bad Request
            // status code is still warranted, or whether returning some other
            // status would be more appropriate.

        } catch (DocumentNotFoundException dnfe) {
            response = Response.status(Response.Status.NOT_FOUND).entity(dnfe.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (BadRequestException bre) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(bre.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (IllegalStateException ise) {
            response = Response.status(Response.Status.BAD_REQUEST).entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();

            // This is guard code that should never be reached.
        } catch (Exception e) {
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        return response;

    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Creates a new ID generator instance.
     *
     */
    @POST
    @Path("")
    @Consumes(MediaType.APPLICATION_XML)
    public Response createIDGenerator(String xmlPayload) {

        ResponseBuilder builder = Response.ok();
        Response response = builder.build();

        try {

            String csid = UUID.randomUUID().toString();
            service.createIDGenerator(csid, xmlPayload);

            // Build the URI to be returned in the Location header.
            //
            // Gets the base URL path to this resource.
            UriBuilder path = UriBuilder.fromResource(IDResource.class);
            // @TODO Look into whether we can create the path using the
            // URI template in the @Path annotation to this method, rather
            // than the hard-coded analog to that template currently used.
            path.path("" + csid);

            // Build the response, setting the:
            // - HTTP Status code (to '201 Created')
            // - Content-type header (to the relevant media type)
            // - Entity body (to the new ID)
            response =
                    Response.created(path.build()).entity("").type(MediaType.TEXT_PLAIN).build();

        } catch (Exception e) {
            response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

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

            // Make new elements for each of the components of this ID generator
            // instance, and attach them to the root element.

            // Append display name information for this ID generator instance.
            String displayname = instance.getDisplayName();
            root = appendDisplayNameIDGeneratorInformation(root, displayname);
            // Append detailed information for this ID generator instance.
            root = appendDetailedIDGeneratorInformation(root, instance);

            resourceRepresentation = XmlTools.prettyPrintXML(doc);
            response =
                    Response.status(Response.Status.OK).entity(resourceRepresentation).type(MediaType.APPLICATION_XML).build();

            // @TODO Return an XML-based error results format with the
            // responses below.

        } catch (DocumentNotFoundException dnfe) {
            response =
                    Response.status(Response.Status.NOT_FOUND).entity(dnfe.getMessage()).type(MediaType.TEXT_PLAIN).build();

        } catch (IllegalStateException ise) {
            response =
                    Response.status(Response.Status.BAD_REQUEST).entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();

            // This is guard code that should never be reached.
        } catch (Exception e) {
            response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
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
     * @param   format  A representation ("format") in which to return
     *                  list items, such as a "full" or "summary" format.
     *
     * @return  A list of representations of ID generator instance resources.
     */
    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_XML)
    public Response readIDGeneratorsList(
            @QueryParam(QUERY_PARAM_LIST_FORMAT) String listFormat,
            @QueryParam(QUERY_PARAM_ID_GENERATOR_ROLE) String role) {

        // @TODO The names and values of the query parameters above
        // ("format"and "role") are arbitrary, as are the format of the
        // results returned.  These should be standardized and
        // referenced project-wide.

        ResponseBuilder builder = Response.ok();
        Response response = builder.build();

        String resourceRepresentation = "";
        try {

            Map<String, IDGeneratorInstance> generators =
                    service.readIDGeneratorsList();

            // If no ID generator instances were found, return an empty list.
            if (generators == null || generators.isEmpty()) {

                Document doc = baseListDocument();
                resourceRepresentation = doc.asXML();
                response =
                        Response.status(Response.Status.OK).entity(resourceRepresentation).type(MediaType.APPLICATION_XML).build();
                return response;
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
                // Do nothing
                // Otherwise, return only ID generator instances
                // matching the requested role.
            } else {
                // @TODO Implement this stubbed code, by
                // iterating over generator instances and
                // calling generatorHasRole().
            }

            // Default to summary list if no list format is specified.
            if (listFormat == null || listFormat.trim().isEmpty()) {
                resourceRepresentation = formattedSummaryList(generators);
            } else if (listFormat.equalsIgnoreCase(LIST_FORMAT_SUMMARY)) {
                resourceRepresentation = formattedSummaryList(generators);
            } else if (listFormat.equalsIgnoreCase(LIST_FORMAT_FULL)) {
                resourceRepresentation = formattedFullList(generators);
                // Return an error if the value of the query parameter
                // is unrecognized.
                //
                // @TODO Return an appropriate XML-based entity body upon error.
            } else {
                String msg =
                        "Query parameter '" + listFormat + "' was not recognized.";
                if (logger.isDebugEnabled()) {
                    logger.debug(msg);
                }
                response =
                        Response.status(Response.Status.BAD_REQUEST).entity("").type(MediaType.TEXT_PLAIN).build();
            }

            response =
                    Response.status(Response.Status.OK).entity(resourceRepresentation).type(MediaType.APPLICATION_XML).build();

            // @TODO Return an XML-based error results format with the
            // responses below.
        } catch (IllegalStateException ise) {
            response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(ise.getMessage()).type(MediaType.TEXT_PLAIN).build();

            // This is guard code that should never be reached.
        } catch (Exception e) {
            response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        return response;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Creates a new ID generator instance.
     *
     */
    @DELETE
    @Path("/{csid}")
    public Response deleteIDGenerator(@PathParam("csid") String csid) {

        ResponseBuilder builder = Response.ok();
        Response response = builder.build();

        try {
            service.deleteIDGenerator(csid);
            response = Response.ok().entity("").type(MediaType.TEXT_PLAIN).build();
        } catch (Exception e) {
            response =
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR).
                    entity(e.getMessage()).type(MediaType.TEXT_PLAIN).build();
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
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a base XML document representing a list of
     * ID generator instances.
     *
     * @return  A base XML document representing a list
     *          of ID generator instances.
     */
    private Document baseListDocument() {

        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement(ID_GENERATOR_LIST_NAME);
        Namespace namespace =
                new Namespace(ID_SERVICE_NAMESPACE_PREFIX, ID_SERVICE_NAMESPACE);
        doc.getRootElement().add(namespace);

        return doc;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a list with summary information about ID generator instances.
     *
     * This format returns summary data about ID generator
     * instances, along with relative URIs that may be used
     * to retrieve more data about each instance.
     *
     * @param   generators A list of ID generator instances.
     *
     * @return  A summary list of ID generator instances.
     */
    private String formattedSummaryList(
            Map<String, IDGeneratorInstance> generators) {

        Document doc = baseListDocument();

        Element listitem = null;
        String displayname = "";
        // Retrieve the CSIDs from each ID generator instance,
        // and use these in summary information returned about
        // each instance.
        for (String csid : generators.keySet()) {
            // Add a new element for each item in the list.
            listitem =
                    doc.getRootElement().addElement(ID_GENERATOR_LIST_ITEM_NAME);
            // Append display name information for this ID generator instance.
            displayname = generators.get(csid).getDisplayName();
            listitem = appendDisplayNameIDGeneratorInformation(listitem, displayname);
            // Append summary information about this ID generator instance.
            listitem = appendSummaryIDGeneratorInformation(listitem, csid);
        }

        return XmlTools.prettyPrintXML(doc);
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Returns a list with full information about ID generator instances.
     *
     * @param   generators A list of ID generator instances, each
     *                     containing a CollectionSpace ID (CSID).
     *
     * @return  A full list of ID generator instances.
     */
    private String formattedFullList(
            Map<String, IDGeneratorInstance> generators) {

        Document doc = baseListDocument();

        Element listitem = null;
        String displayname = "";
        for (String csid : generators.keySet()) {
            // Add a new element for each item in the list.
            listitem =
                    doc.getRootElement().addElement(ID_GENERATOR_LIST_ITEM_NAME);
            // Append display name information for this ID generator instance.
            displayname = generators.get(csid).getDisplayName();
            listitem = appendDisplayNameIDGeneratorInformation(listitem, displayname);
            // Append summary information about this ID generator instance.
            listitem = appendSummaryIDGeneratorInformation(listitem, csid);
            // Append details of this ID generator instance.
            Element instance = listitem.addElement(ID_GENERATOR_NAME);
            listitem = appendDetailedIDGeneratorInformation(instance,
                    generators.get(csid));
        }

        return XmlTools.prettyPrintXML(doc);
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Appends a display name to an element representing
     * an ID generator instance.
     *
     * @param   instanceElement  An XML element representing an
     *                           ID generator instance.
     *
     * @param   displayname      A displayname for the resource representing that instance.
     *
     * @return  The XML element representing an ID generator instance,
     *          with the display name appended.
     */
    private Element appendDisplayNameIDGeneratorInformation(Element instanceElement,
            String displaynameValue) {

        Element displayname = instanceElement.addElement("displayname");
        displayname.addText(displaynameValue);

        return instanceElement;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Appends summary information to an element representing
     * an ID generator instance.
     *
     * @param   instanceElement  An XML element representing an
     *                           ID generator instance.
     *
     * @param   csid             A CollectionSpace ID (CISD) associated with
     *                           the resource representing that instance.
     *
     * @return  The XML element representing an ID generator instance,
     *          with summary information appended.
     */
    private Element appendSummaryIDGeneratorInformation(Element instanceElement,
            String csidValue) {

        Element uri = instanceElement.addElement("uri");
        uri.addText(getRelativePath(csidValue));
        Element csid = instanceElement.addElement("csid");
        csid.addText(csidValue);

        return instanceElement;
    }

    //////////////////////////////////////////////////////////////////////
    /**
     * Appends detailed information about an ID generator instance,
     * to an element representing that ID generator instance.
     *
     * @param   instanceElement    An XML element representing an
     *                             ID generator instance.
     *
     * @param   generatorInstance  An instance of an ID generator.
     *
     * @return  The XML element representing an ID generator instance,
     *          with detailed information appended.
     */
    private Element appendDetailedIDGeneratorInformation(
            Element instanceElement, IDGeneratorInstance generatorInstance) {

        // Append description information.
        Element description = instanceElement.addElement("description");
        description.addText(generatorInstance.getDescription());

        // Append a representative, or sample, ID - of a type that
        // can be generated by this ID generator instance - for display.
        Element displayid = instanceElement.addElement("displayid");
        // Return the last generated ID as a representative ID.
        // If no ID has ever been generated by this ID generator instance,
        // return the current ID instead.
        //
        // @TODO This is a short-term kludge.  We may wish to instead
        // generate a static, sample ID, at system initialization
        // or launch time; or generate or load this value once, at the
        // time that an ID generator instance is created.
        String lastgenerated = generatorInstance.getLastGeneratedID();
        if (lastgenerated != null & !lastgenerated.trim().isEmpty()) {
            displayid.addText(lastgenerated);
        } else {
            SettableIDGenerator gen;
            try {
                gen = IDGeneratorSerializer.deserialize(generatorInstance.getGeneratorState());
                String current = gen.getCurrentID();
                if (current != null & !current.trim().isEmpty()) {
                    displayid.addText(current);
                }
            } catch (Exception e) {
                // Do nothing here.
                // @TODO
                // Could potentially return an error message, akin to:
                // displayid.addText("No ID available for display");
            }
        }

        // Append components information.
        Element generator =
                instanceElement.addElement(ID_GENERATOR_COMPONENTS_NAME);
        // Get an XML string representation of the ID generator's components.
        String generatorStr = generatorInstance.getGeneratorState();
        // Convert the XML string representation of the ID generator's
        // components to a new XML document, copy its root element, and
        // append it to the relevant location within the current element.
        try {
            Document generatorDoc = XmlTools.textToXMLDocument(generatorStr);
            Element generatorRoot = generatorDoc.getRootElement();
            generator.add(generatorRoot.createCopy());
            // If an error occurs parsing the XML string representation,
            // the text of the components element will remain empty.
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Error parsing XML text: " + generatorStr);
            }
        }

        return instanceElement;
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

        // @TODO Verify that this is the correct relative path.
        // Do we need to check the path provided in the original request?

        if (csid != null && !csid.trim().isEmpty()) {
            return BASE_URL_PATH + "/" + csid;
        } else {
            return BASE_URL_PATH;
        }
    }
}
