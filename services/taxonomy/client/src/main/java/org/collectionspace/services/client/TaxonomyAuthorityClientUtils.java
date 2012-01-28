package org.collectionspace.services.client;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.collectionspace.services.TaxonJAXBSchema;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.taxonomy.TaxonAuthorGroupList;
import org.collectionspace.services.taxonomy.TaxonCitationList;
import org.collectionspace.services.taxonomy.TaxonCommon;
import org.collectionspace.services.taxonomy.TaxonomyauthorityCommon;
import org.dom4j.DocumentException;
import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaxonomyAuthorityClientUtils {

    private static final Logger logger =
            LoggerFactory.getLogger(TaxonomyAuthorityClientUtils.class);

    /**
     * Creates a new Taxonomy Authority
     * @param displayName	The displayName used in UI, etc.
     * @param refName		The proper refName for this authority
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createTaxonomyAuthorityInstance(
            String displayName, String shortIdentifier, String headerLabel) {
        TaxonomyauthorityCommon Taxonomyauthority = new TaxonomyauthorityCommon();
        Taxonomyauthority.setDisplayName(displayName);
        Taxonomyauthority.setShortIdentifier(shortIdentifier);
        // String refName = createTaxonomyAuthRefName(shortIdentifier, displayName);
        // Taxonomyauthority.setRefName(refName);
        Taxonomyauthority.setVocabType("Taxonomyauthority"); //FIXME: REM - Should this really be hard-coded?
        PoxPayloadOut multipart = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(Taxonomyauthority, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, Taxonomyauthority common ",
                    Taxonomyauthority, TaxonomyauthorityCommon.class);
        }

        return multipart;
    }

    /**
     * @param taxonomyAuthRefName  The proper refName for this authority.
     * @param taxonInfo the properties for the new instance of a term in this authority.
     * @param taxonAuthorGroupList an author group list (values of a repeatable group in the term record).
     * @param taxonCitationList a citation list (values of a repeatable scalar in the term record).
     * @param headerLabel	The common part label
     * @return	The PoxPayloadOut payload for the create call
     */
    public static PoxPayloadOut createTaxonInstance(
            String taxonomyAuthRefName, Map<String, String> taxonInfo,
            TaxonAuthorGroupList taxonAuthorGroupList, TaxonCitationList taxonCitationList,
            String headerLabel) {
        TaxonCommon taxon = new TaxonCommon();
        String shortId = taxonInfo.get(TaxonJAXBSchema.SHORT_IDENTIFIER);
        String displayName = taxonInfo.get(TaxonJAXBSchema.DISPLAY_NAME);
        taxon.setShortIdentifier(shortId);
        // String taxonomyRefName = createTaxonomyRefName(taxonomyAuthRefName, shortId, displayName);
        // taxon.setRefName(taxonomyRefName);
        String value = null;
        value = taxonInfo.get(TaxonJAXBSchema.DISPLAY_NAME_COMPUTED);
        boolean displayNameComputed = (value == null) || value.equalsIgnoreCase("true");
        taxon.setDisplayNameComputed(displayNameComputed);
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.TERM_STATUS)) != null) {
            taxon.setTermStatus(value);
        }

        // Fields specific to this authority record type.
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.NAME)) != null) {
            taxon.setTaxonFullName(value);
        }
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.TAXON_RANK)) != null) {
            taxon.setTaxonRank(value);
        }
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.TAXON_CURRENCY)) != null) {
            taxon.setTaxonCurrency(value);
        }
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.TAXON_YEAR)) != null) {
            taxon.setTaxonYear(value);
        }
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.TAXONOMIC_STATUS)) != null) {
            taxon.setTaxonomicStatus(value);
        }
        if ((value = (String) taxonInfo.get(TaxonJAXBSchema.TAXON_IS_NAMED_HYBRID)) != null) {
            taxon.setTaxonIsNamedHybrid(value);
        }
        if (taxonCitationList != null) {
            taxon.setTaxonCitationList(taxonCitationList);
        }

        if (taxonAuthorGroupList != null) {
            taxon.setTaxonAuthorGroupList(taxonAuthorGroupList);
        }

        // FIXME: When the field isNamedHybrid becomes Boolean, add it as such to sample instances.

        PoxPayloadOut multipart = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(taxon,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, taxon common ", taxon, TaxonCommon.class);
        }

        return multipart;
    }

    /**
     * @param vcsid CSID of the authority to create a new taxon in
     * @param TaxonomyauthorityRefName The refName for the authority
     * @param taxonMap the properties for the new Taxon
     * @param client the service client
     * @return the CSID of the new item
     */
    public static String createItemInAuthority(String vcsid,
            String TaxonomyauthorityRefName, Map<String, String> taxonMap,
            TaxonAuthorGroupList taxonAuthorGroupList,
            TaxonCitationList taxonCitationList, TaxonomyAuthorityClient client) {
        // Expected status code: 201 Created
        int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

        String displayName = taxonMap.get(TaxonJAXBSchema.DISPLAY_NAME);
        String displayNameComputedStr = taxonMap.get(TaxonJAXBSchema.DISPLAY_NAME_COMPUTED);
        boolean displayNameComputed = (displayNameComputedStr == null) || displayNameComputedStr.equalsIgnoreCase("true");
        if (displayName == null) {
            if (!displayNameComputed) {
                throw new RuntimeException(
                        "CreateItem: Must supply a displayName if displayNameComputed is set to false.");
            }
            displayName =
                    prepareDefaultDisplayName(
                    taxonMap.get(TaxonJAXBSchema.NAME));
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Import: Create Item: \"" + displayName
                    + "\" in Taxonomyauthority: \"" + TaxonomyauthorityRefName + "\"");
        }
        PoxPayloadOut multipart =
                createTaxonInstance(TaxonomyauthorityRefName,
                taxonMap, taxonAuthorGroupList, taxonCitationList, client.getItemCommonPartName());
        String newID = null;
        ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();

            if (!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \""
                        + taxonMap.get(TaxonJAXBSchema.SHORT_IDENTIFIER)
                        + "\" in Taxonomyauthority: \"" + TaxonomyauthorityRefName
                        + "\" " + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if (statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when creating Item: \""
                        + taxonMap.get(TaxonJAXBSchema.SHORT_IDENTIFIER)
                        + "\" in Taxonomyauthority: \"" + TaxonomyauthorityRefName + "\", Status:" + statusCode);
            }
            newID = extractId(res);
        } finally {
            res.releaseConnection();
        }

        return newID;
    }

    public static PoxPayloadOut createTaxonInstance(
            String commonPartXML, String headerLabel) throws DocumentException {
        PoxPayloadOut multipart = new PoxPayloadOut(TaxonomyAuthorityClient.SERVICE_ITEM_PAYLOAD_NAME);
        PayloadOutputPart commonPart = multipart.addPart(commonPartXML,
                MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);

        if (logger.isDebugEnabled()) {
            logger.debug("to be created, Taxon common ", commonPartXML);
        }

        return multipart;
    }

    public static String createItemInAuthority(String vcsid,
            String commonPartXML,
            TaxonomyAuthorityClient client) throws DocumentException {
        // Expected status code: 201 Created
        int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

        PoxPayloadOut multipart =
                createTaxonInstance(commonPartXML, client.getItemCommonPartName());
        String newID = null;
        ClientResponse<Response> res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();

            if (!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                throw new RuntimeException("Could not create Item: \"" + commonPartXML
                        + "\" in Taxonomyauthority: \"" + vcsid
                        + "\" " + invalidStatusCodeMessage(REQUEST_TYPE, statusCode));
            }
            if (statusCode != EXPECTED_STATUS_CODE) {
                throw new RuntimeException("Unexpected Status when creating Item: \"" + commonPartXML
                        + "\" in Taxonomyauthority: \"" + vcsid + "\", Status:" + statusCode);
            }
            newID = extractId(res);
        } finally {
            res.releaseConnection();
        }

        return newID;
    }

    /**
     * Creates the from xml file.
     *
     * @param fileName the file name
     * @return new CSID as string
     * @throws Exception the exception
     */
    private String createItemInAuthorityFromXmlFile(String vcsid, String commonPartFileName,
            TaxonomyAuthorityClient client) throws Exception {
        byte[] b = FileUtils.readFileToByteArray(new File(commonPartFileName));
        String commonPartXML = new String(b);
        return createItemInAuthority(vcsid, commonPartXML, client);
    }

    /**
     * Creates the Taxonomyauthority ref name.
     *
     * @param shortId the Taxonomyauthority shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createTaxonomyAuthRefName(String shortId, String displaySuffix) {
        String refName = "urn:cspace:org.collectionspace.demo:taxonomyauthority:name("
                + shortId + ")";
        if (displaySuffix != null && !displaySuffix.isEmpty()) {
            refName += "'" + displaySuffix + "'";
        }
        return refName;
    }

    /**
     * Creates the taxon ref name.
     *
     * @param taxonomyAuthRefName the Taxonomyauthority ref name
     * @param shortId the taxon shortIdentifier
     * @param displaySuffix displayName to be appended, if non-null
     * @return the string
     */
    public static String createTaxonomyRefName(
            String taxonomyAuthRefName, String shortId, String displaySuffix) {
        String refName = taxonomyAuthRefName + ":taxon:name(" + shortId + ")";
        if (displaySuffix != null && !displaySuffix.isEmpty()) {
            refName += "'" + displaySuffix + "'";
        }
        return refName;
    }

    public static String extractId(ClientResponse<Response> res) {
        MultivaluedMap<String, Object> mvm = res.getMetadata();
        String uri = (String) ((ArrayList<Object>) mvm.get("Location")).get(0);
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
     * Returns an error message indicating that the status code returned by a
     * specific call to a service does not fall within a set of valid status
     * codes for that service.
     *
     * @param serviceRequestType  A type of service request (e.g. CREATE, DELETE).
     *
     * @param statusCode  The invalid status code that was returned in the response,
     *                    from submitting that type of request to the service.
     *
     * @return An error message.
     */
    public static String invalidStatusCodeMessage(ServiceRequestType requestType, int statusCode) {
        return "Status code '" + statusCode + "' in response is NOT within the expected set: "
                + requestType.validStatusCodesAsString();
    }

    /**
     * Produces a default displayName from the basic name and dates fields.
     * @see TaxonomyDocumentModelHandler.prepareDefaultDisplayName() which
     * duplicates this logic, until we define a service-general utils package
     * that is neither client nor service specific.
     * @param name	
     * @return
     */
    public static String prepareDefaultDisplayName(
            String name) {
        StringBuilder newStr = new StringBuilder();
        newStr.append(name);
        return newStr.toString();
    }
}
