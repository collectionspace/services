package org.collectionspace.services.chronology.nuxeo;

import org.collectionspace.services.chronology.ChronologyauthoritiesCommon;
import org.collectionspace.services.client.ChronologyAuthorityClient;
import org.collectionspace.services.common.vocabulary.nuxeo.AuthorityDocumentModelHandler;

/**
 * ChronologyAuthorityDocumentModelHandler
 */
public class ChronologyAuthorityDocumentModelHandler
    extends AuthorityDocumentModelHandler<ChronologyauthoritiesCommon> {
    public ChronologyAuthorityDocumentModelHandler() {
        super(ChronologyAuthorityClient.SERVICE_COMMON_PART_NAME,
              ChronologyAuthorityClient.SERVICE_ITEM_NAME_COMMON_PART_NAME);
    }

    /**
     * getQProperty converts the given property to qualified schema property
     * @param prop
     * @return
     */
    @Override
    public String getQProperty(final String prop) {
        return ChronologyAuthorityConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}
