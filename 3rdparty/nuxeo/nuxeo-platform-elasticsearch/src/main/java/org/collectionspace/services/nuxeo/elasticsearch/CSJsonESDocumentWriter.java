package org.collectionspace.services.nuxeo.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.TextNode;

import org.collectionspace.services.common.api.RefNameUtils;

import org.nuxeo.ecm.automation.jaxrs.io.documents.JsonESDocumentWriter;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

public class CSJsonESDocumentWriter extends JsonESDocumentWriter {
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void writeDoc(JsonGenerator jg, DocumentModel doc, String[] schemas,
            Map<String, String> contextParameters, HttpHeaders headers)
            throws IOException {

        // Compute and store fields that should be indexed with this document in ElasticSearch.
        // TODO: Make this configurable. This is currently hardcoded for the materials profile and
        // the Material Order application.

        ObjectNode denormValues = objectMapper.createObjectNode();

        String docType = doc.getType();

        if (docType.startsWith("Materialitem")) {
            CoreSession session = doc.getCoreSession();

            // Store the csids of media records that reference this material authority item via the
            // coverage field.

            String refName = (String) doc.getProperty("collectionspace_core", "refName");

            if (StringUtils.isNotEmpty(refName)) {
                String escapedRefName = refName.replace("'", "\\'");
                String tenantId = (String) doc.getProperty("collectionspace_core", "tenantId");
                String mediaQuery = String.format("SELECT * FROM Media WHERE media_common:coverage = '%s' AND ecm:currentLifeCycleState = 'project' AND collectionspace_core:tenantId = '%s' ORDER BY media_common:identificationNumber", escapedRefName, tenantId);

                DocumentModelList mediaDocs = session.query(mediaQuery);
                List<JsonNode> mediaCsids = new ArrayList<JsonNode>();

                if (mediaDocs.size() > 0) {
                    Iterator<DocumentModel> iterator = mediaDocs.iterator();

                    while (iterator.hasNext()) {
                        DocumentModel mediaDoc = iterator.next();

                        if (isMediaPublished(mediaDoc)) {
                            String mediaCsid = (String) mediaDoc.getName();

                            mediaCsids.add(new TextNode(mediaCsid));
                        }
                    }
                }

                denormValues.putArray("mediaCsid").addAll(mediaCsids);
            }

            // Compute the title of the record for the public browser, and store it so that it can
            // be used for sorting ES query results.

            String title = computeTitle(doc);

            if (title != null) {
                denormValues.put("title", title);
            }

            List<Map<String, Object>> termGroups = (List<Map<String, Object>>) doc.getProperty("materials_common", "materialTermGroupList");
            List<String> commercialNames = findTermDisplayNamesWithFlag(termGroups, "commercial");
            List<String> commonNames = findTermDisplayNamesWithFlag(termGroups, "common");

            // Find and store the commercial names and common names for this item. This simplifies
            // search and display in the Material Order application.

            if (commercialNames.size() > 0) {
                denormValues.putArray("commercialNames").addAll(jsonNodes(commercialNames));
            }

            if (commonNames.size() > 0) {
                denormValues.putArray("commonNames").addAll(jsonNodes(commonNames));
            }

            // Combine term creator organizations and term editor organizations into a holding
            // institutions field.

            Set<String> holdingInstitutions = new HashSet<String>();

            holdingInstitutions.addAll(getTermAttributionContributors(doc));
            holdingInstitutions.addAll(getTermAttributionEditors(doc));

            if (holdingInstitutions.size() > 0) {
                denormValues.putArray("holdingInstitutions").addAll(jsonNodes(holdingInstitutions));
            }
        }

        // Below is sample code for denormalizing fields from the computed current location (place
        // item) into collection object documents. This was written for the public browser
        // prototype for public art.

        /*
        if (docType.startsWith("CollectionObject")) {
            CoreSession session = doc.getCoreSession();

            String refName = (String) doc.getProperty("collectionobjects_common", "computedCurrentLocation");

            if (StringUtils.isNotEmpty(refName)) {
                String escapedRefName = refName.replace("'", "\\'");
                String placeQuery = String.format("SELECT * FROM PlaceitemTenant5000 WHERE places_common:refName = '%s'", escapedRefName);

                DocumentModelList placeDocs = session.query(placeQuery, 1);

                if (placeDocs.size() > 0) {
                    DocumentModel placeDoc = placeDocs.get(0);

                    String placementType = (String) placeDoc.getProperty("places_publicart:placementType").getValue();

                    if (placementType != null) {
                        denormValues.put("placementType", placementType);
                    }

                    Property geoRefGroup;

                    try {
                        geoRefGroup = placeDoc.getProperty("places_common:placeGeoRefGroupList/0");
                    } catch (PropertyNotFoundException e) {
                        geoRefGroup = null;
                    }

                    if (geoRefGroup != null) {
                        Double decimalLatitude = (Double) geoRefGroup.getValue("decimalLatitude");
                        Double decimalLongitude = (Double) geoRefGroup.getValue("decimalLongitude");

                        if (decimalLatitude != null && decimalLongitude != null) {
                            ObjectNode geoPointNode = objectMapper.createObjectNode();

                            geoPointNode.put("lat", decimalLatitude);
                            geoPointNode.put("lon", decimalLongitude);

                            denormValues.put("geoPoint", geoPointNode);
                        }
                    }
                }
            }

            String uri = (String) doc.getProperty("collectionobjects_core", "uri");
            String csid = uri.substring(uri.lastIndexOf('/') + 1);
            String mediaQuery = String.format("SELECT media_common:blobCsid, media_common:title FROM Relation WHERE relations_common:subjectCsid = '%s' AND relations_common:objectDocumentType = 'Media'", csid);

            DocumentModelList mediaDocs = session.query(mediaQuery, 1);

            if (mediaDocs.size() > 0) {

            }
        }
        */

        jg.writeStartObject();

        writeSystemProperties(jg, doc);
        writeSchemas(jg, doc, schemas);
        writeContextParameters(jg, doc, contextParameters);
        writeDenormValues(jg, doc, denormValues);

        jg.writeEndObject();
        jg.flush();
    }

    public void writeDenormValues(JsonGenerator jg, DocumentModel doc, ObjectNode denormValues) throws IOException {
        if (denormValues != null && denormValues.size() > 0) {
            if (jg.getCodec() == null) {
                jg.setCodec(objectMapper);
            }

            Iterator<Map.Entry<String, JsonNode>> entries = denormValues.getFields();

            while (entries.hasNext()) {
                Map.Entry<String, JsonNode> entry = entries.next();

                jg.writeFieldName("collectionspace_denorm:" + entry.getKey());
                jg.writeTree(entry.getValue());
            }
        }
    }

    /**
     * Compute a title for the public browser. This needs to be indexed in ES so that it can
     * be used for sorting. (Even if it's just extracting the primary value.)
     */
    private String computeTitle(DocumentModel doc) {
        List<Map<String, Object>> termGroups = (List<Map<String, Object>>) doc.getProperty("materials_common", "materialTermGroupList");
        String primaryDisplayName = null;

        if (termGroups.size() > 0) {
            Map<String, Object> primaryTermGroup = termGroups.get(0);
            primaryDisplayName = (String) primaryTermGroup.get("termDisplayName");
        }

        return primaryDisplayName;
    }

    private String findFirstTermDisplayNameWithFlag(List<Map<String, Object>> termGroups, String flagShortId) {
        String termDisplayName = null;

        for (Map<String, Object> termGroup : termGroups) {
            String termFlag = (String) termGroup.get("termFlag");

            if (termFlag != null && termFlag.contains("(" + flagShortId + ")")) {
                String candidateTermDisplayName = (String) termGroup.get("termDisplayName");

                if (StringUtils.isNotEmpty(candidateTermDisplayName)) {
                    termDisplayName = candidateTermDisplayName;
                    break;
                }
            }
        }

        return termDisplayName;
    }

    private List<String> findTermDisplayNamesWithFlag(List<Map<String, Object>> termGroups, String flagShortId) {
        List<String> termDisplayNames = new ArrayList<String>();

        for (Map<String, Object> termGroup : termGroups) {
            String termFlag = (String) termGroup.get("termFlag");

            if (termFlag != null && termFlag.contains("(" + flagShortId + ")")) {
                String candidateTermDisplayName = (String) termGroup.get("termDisplayName");

                if (StringUtils.isNotEmpty(candidateTermDisplayName)) {
                    termDisplayNames.add(candidateTermDisplayName);
                }
            }
        }

        return termDisplayNames;
    }

    private Set<String> getTermAttributionContributors(DocumentModel doc) {
        Set orgs = new HashSet<String>();

        List<Map<String, Object>> groups = (List<Map<String, Object>>) doc.getProperty("materials_common", "materialTermAttributionContributingGroupList");

        for (Map<String, Object> group : groups) {
            String org = (String) group.get("materialTermAttributionContributingOrganization");

            orgs.add(org);
        }

        return orgs;
    }

    private Set<String> getTermAttributionEditors(DocumentModel doc) {
        Set orgs = new HashSet<String>();

        List<Map<String, Object>> groups = (List<Map<String, Object>>) doc.getProperty("materials_common", "materialTermAttributionEditingGroupList");

        for (Map<String, Object> group : groups) {
            String org = (String) group.get("materialTermAttributionEditingOrganization");

            orgs.add(org);
        }

        return orgs;
    }

    private boolean isMediaPublished(DocumentModel mediaDoc) {
        List<String> publishToValues = (List<String>) mediaDoc.getProperty("media_materials", "publishToList");
        boolean isPublished = false;

        for (int i=0; i<publishToValues.size(); i++) {
            String value = publishToValues.get(i);
            String shortId = RefNameUtils.getItemShortId(value);

            if (shortId.equals("all") || shortId.equals("materialorder")) {
                isPublished = true;
                break;
            }
        }

        return isPublished;
    }

    private List<JsonNode> jsonNodes(Collection<String> values) {
        List<JsonNode> nodes = new ArrayList<JsonNode>();
        Iterator<String> iterator = values.iterator();

        while (iterator.hasNext()) {
            String value = iterator.next();

            nodes.add(new TextNode(value));
        }

        return nodes;
    }
}
