package org.collectionspace.services.nuxeo.client.handler;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * A CollectionSpace version of the {@link  org.nuxeo.ecm.core.api.DocumentModelList}. Primarily used in order to
 * response with a full dataset rather than the simpler list views previously done in CollectionSpace.
 *
 * @since 8.3.0
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "abstract-common-list")
public class CSDocumentModelList extends AbstractCommonList {

    public static class CSDocumentModelResponse {
        private final String csid;
        private final PoxPayloadOut payload;

        public CSDocumentModelResponse(final String csid, final PoxPayloadOut out) {
            this.csid = csid;
            this.payload = out;
        }

        public String getCsid() {
            return csid;
        }

        public PoxPayloadOut getPayload() {
            return payload;
        }
    }

    private final List<CSDocumentModelResponse> responseList = new ArrayList<>();

    public List<CSDocumentModelResponse> getResponseList() {
        return responseList;
    }

    public void addResponsePayload(final String csid, final PoxPayloadOut out) {
        responseList.add(new CSDocumentModelResponse(csid, out));
    }
}
