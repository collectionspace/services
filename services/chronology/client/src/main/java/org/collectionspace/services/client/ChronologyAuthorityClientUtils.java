package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.collectionspace.services.chronology.ChronologyauthoritiesCommon;
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

}
