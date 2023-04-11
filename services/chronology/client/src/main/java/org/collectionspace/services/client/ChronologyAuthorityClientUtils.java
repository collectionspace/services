/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 * <p>
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 * <p>
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 * <p>
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.collectionspace.services.ChronologyJAXBSchema;
import org.collectionspace.services.chronology.ChronologiesCommon;
import org.collectionspace.services.chronology.ChronologyTermGroup;
import org.collectionspace.services.chronology.ChronologyTermGroupList;
import org.collectionspace.services.chronology.ChronologyauthoritiesCommon;
import org.collectionspace.services.client.test.ServiceRequestType;
import org.collectionspace.services.common.api.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChronologyAuthorityClientUtils {

    private static final Logger logger = LoggerFactory.getLogger(ChronologyAuthorityClientUtils.class);

    /**
     * Create a new Chronology authority
     *
     * @param displayName           the display name
     * @param shortIdentifier       the short identifier
     * @param serviceCommonPartName the common part label
     * @return The PoxPayloadOut for the create call
     */
    public static PoxPayloadOut createChronologyAuthorityInstance(final String displayName,
                                                                  final String shortIdentifier,
                                                                  final String serviceCommonPartName) {
        final ChronologyauthoritiesCommon common = new ChronologyauthoritiesCommon();
        common.setDisplayName(displayName);
        common.setShortIdentifier(shortIdentifier);
        common.setVocabType(ChronologyAuthorityClient.SERVICE_BINDING_NAME);

        final PoxPayloadOut poxPayloadOut = new PoxPayloadOut(ChronologyAuthorityClient.SERVICE_PAYLOAD_NAME);
        final PayloadOutputPart payloadPart = poxPayloadOut.addPart(common, MediaType.APPLICATION_XML_TYPE);
        payloadPart.setLabel(serviceCommonPartName);

        logger.debug("to be created, chronologyAuthority common {}", common);
        return poxPayloadOut;
    }

    /**
     *
     * @param vcsid the csid of the authority
     * @param authRefName the refname of the authority
     * @param chronologyMap properties for the new chronology
     * @param terms terms for the new chronology
     * @param client the service client
     * @return the csid of the new item
     */
    public static String createItemInAuthority(final String vcsid,
                                               final String authRefName,
                                               final Map<String, String> chronologyMap,
                                               final List<ChronologyTermGroup> terms,
                                               final ChronologyAuthorityClient client) {
        // Expected status code: 201 Created
        final int EXPECTED_STATUS_CODE = Response.Status.CREATED.getStatusCode();
        // Type of service request being tested
        final ServiceRequestType REQUEST_TYPE = ServiceRequestType.CREATE;

        String displayName = "";
        if (terms != null && !terms.isEmpty()) {
            displayName = terms.get(0).getTermDisplayName();
        }
        logger.debug("Creating item with display name: {} in chronologyAuthority: {}", displayName, vcsid);
        PoxPayloadOut multipart = createChronologyInstance(chronologyMap, terms, client.getItemCommonPartName());
        String newID;

        final Response res = client.createItem(vcsid, multipart);
        try {
            int statusCode = res.getStatus();

            if (!REQUEST_TYPE.isValidStatusCode(statusCode)) {
                final String error = "Could not create Item: %s in chronologyAuthority: %s, %s";
                throw new RuntimeException(String.format(error,
                                                         chronologyMap.get(ChronologyJAXBSchema.SHORT_IDENTIFIER),
                                                         authRefName,
                                                         invalidStatusCodeMessage(REQUEST_TYPE, statusCode)));
            }
            if (statusCode != EXPECTED_STATUS_CODE) {
                final String error = "Unexpected Status when creating Item: %s in chronologyAuthority %s, Status: %d";
                throw new RuntimeException(String.format(error,
                                                         chronologyMap.get(ChronologyJAXBSchema.SHORT_IDENTIFIER),
                                                         authRefName,
                                                         statusCode));
            }
            newID = extractId(res);
        } finally {
            res.close();
        }

        return newID;
    }

    public static PoxPayloadOut createChronologyInstance(final Map<String, String> chronologyInfo,
                                                         final List<ChronologyTermGroup> terms,
                                                         final String headerLabel) {
        final ChronologiesCommon common = new ChronologiesCommon();
        common.setShortIdentifier(chronologyInfo.get(ChronologyJAXBSchema.SHORT_IDENTIFIER));

        ChronologyTermGroupList termList = new ChronologyTermGroupList();
        if (terms == null || terms.isEmpty()) {
            termList.getChronologyTermGroup().addAll(getTermGroupInstance(getGeneratedIdentifier()));
        } else {
            termList.getChronologyTermGroup().addAll(terms);
        }
        common.setChronologyTermGroupList(termList);

        PoxPayloadOut mp = new PoxPayloadOut(ChronologyAuthorityClient.SERVICE_ITEM_NAME);
        PayloadOutputPart commonPart = mp.addPart(common, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(headerLabel);
        logger.debug("To be created, chronologies common {}", common);

        return mp;
    }

    public static List<ChronologyTermGroup> getTermGroupInstance(String identifier) {
        if (Tools.isBlank(identifier)) {
            identifier = getGeneratedIdentifier();
        }

        ChronologyTermGroup term = new ChronologyTermGroup();
        term.setTermName(identifier);
        term.setTermDisplayName(identifier);
        return Collections.singletonList(term);
    }

    /**
     *
     * @param res
     * @return
     */
    public static String extractId(final Response res) {
        final MultivaluedMap<String, Object> mvm = res.getMetadata();
        final String uri = (String) mvm.get("Location").get(0);
        logger.debug("extractId:uri={}", uri);

        final String[] segments = uri.split("/");
        final String id = segments[segments.length - 1];
        logger.debug("id=" + id);
        return id;
    }

    /**
     *
     * @param requestType
     * @param statusCode
     * @return
     */
    public static String invalidStatusCodeMessage(final ServiceRequestType requestType, final int statusCode) {
        return String.format("Status code '%d' is not within the expected set: %s", statusCode,
                             requestType.validStatusCodesAsString());
    }

    private static String getGeneratedIdentifier() {
        return "id" + new Date().getTime();
    }

}
