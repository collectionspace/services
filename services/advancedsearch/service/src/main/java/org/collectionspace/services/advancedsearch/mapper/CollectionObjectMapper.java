package org.collectionspace.services.advancedsearch.mapper;

import static org.collectionspace.services.client.CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA;
import static org.collectionspace.services.client.CollectionSpaceClient.NAGPRA_EXTENSION_NAME;
import static org.collectionspace.services.client.CollectionSpaceClient.NATURALHISTORY_EXT_EXTENSION_NAME;
import static org.collectionspace.services.client.CollectionSpaceClient.PART_COMMON_LABEL;
import static org.collectionspace.services.client.CollectionSpaceClient.PART_LABEL_SEPARATOR;

import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.collectionspace.collectionspace_core.CollectionSpaceCore;
import org.collectionspace.services.advancedsearch.AdvancedsearchCommonList.AdvancedsearchListItem;
import org.collectionspace.services.advancedsearch.ObjectFactory;
import org.collectionspace.services.advancedsearch.model.AgentModel;
import org.collectionspace.services.advancedsearch.model.BriefDescriptionListModel;
import org.collectionspace.services.advancedsearch.model.ContentConceptListModel;
import org.collectionspace.services.advancedsearch.model.FieldCollectionModel;
import org.collectionspace.services.advancedsearch.model.NAGPRACategoryModel;
import org.collectionspace.services.advancedsearch.model.ObjectNameListModel;
import org.collectionspace.services.advancedsearch.model.ObjectProductionModel;
import org.collectionspace.services.advancedsearch.model.ResponsibleDepartmentsListModel;
import org.collectionspace.services.advancedsearch.model.TaxonModel;
import org.collectionspace.services.advancedsearch.model.TitleGroupListModel;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.collectionobject.domain.nagpra.CollectionObjectsNAGPRA;
import org.collectionspace.services.collectionobject.domain.naturalhistory_extension.CollectionobjectsNaturalhistory;
import org.collectionspace.services.nuxeo.client.handler.CSDocumentModelList.CSDocumentModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * A class for mapping {@link CSDocumentModelResponse} to {@link AdvancedsearchListItem}.
 *
 * @since 8.3.0
 */
public class CollectionObjectMapper {

    private static final Logger logger = LoggerFactory.getLogger(CollectionObjectMapper.class);

    private static final String COMMON_PART_NAME =
            CollectionObjectClient.SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;

    private static final String NATHIST_PART_NAME =
            CollectionObjectClient.SERVICE_NAME + PART_LABEL_SEPARATOR + NATURALHISTORY_EXT_EXTENSION_NAME;

    private static final String NAGPRA_PART_NAME =
            CollectionObjectClient.SERVICE_NAME + PART_LABEL_SEPARATOR + NAGPRA_EXTENSION_NAME;

    private final ObjectFactory objectFactory;
    private final Unmarshaller unmarshaller;

    public CollectionObjectMapper(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
        this.objectFactory = new ObjectFactory();
    }

    /**
     * Map a {@link CSDocumentModelResponse} to a {@link AdvancedsearchListItem}. This looks at the response for each
     * of the collectionspace_core, collectionobjects_common, collectionobjects_nagpra, and
     * collectionobjects_naturalhistory_extension parts and pulls fields out of each based on the search specification.
     * We don't differentiate between profiles here and instead return everything available for the ui.
     * <p>
     * Note that this doesn't handle the {@link AdvancedsearchListItem#setRelated(Boolean)} as that requires access to
     * the RelationResource. Maybe worth doing through an additional parameter.
     * @param response The response from the CollectionObjectResource for a single object
     * @param blobCsids The blobs associated with the object
     * @return the advanced search list item
     */
    public AdvancedsearchListItem asListItem(final CSDocumentModelResponse response, final List<String> blobCsids) {
        // todo: what makes sense here?
        if (response == null || response.getPayload() == null) {
            return objectFactory.createAdvancedsearchCommonListAdvancedsearchListItem();
        }

        final AdvancedsearchListItem item = objectFactory.createAdvancedsearchCommonListAdvancedsearchListItem();

        final PoxPayloadOut outputPayload = response.getPayload();

        final CollectionSpaceCore core =
                unmarshall(CollectionSpaceCore.class, COLLECTIONSPACE_CORE_SCHEMA, outputPayload, unmarshaller);

        final CollectionobjectsCommon collectionObject =
                unmarshall(CollectionobjectsCommon.class, COMMON_PART_NAME, outputPayload, unmarshaller);

        final CollectionObjectsNAGPRA objectsNAGPRA =
                unmarshall(CollectionObjectsNAGPRA.class, NAGPRA_PART_NAME, outputPayload, unmarshaller);

        final CollectionobjectsNaturalhistory naturalHistory =
                unmarshall(CollectionobjectsNaturalhistory.class, NATHIST_PART_NAME, outputPayload, unmarshaller);

        final String csid = response.getCsid();
        item.setCsid(csid);
        if (core != null) {
            item.setRefName(core.getRefName());
            item.setUri(core.getUri());
            item.setUpdatedAt(core.getUpdatedAt());
        } else {
            logger.warn("advancedsearch: could not find collectionspace_core associated with csid {}", csid);
        }

        if (collectionObject != null) {
            item.setObjectNumber(collectionObject.getObjectNumber());
            item.setBriefDescription(BriefDescriptionListModel.briefDescriptionListToDisplayString(
                    collectionObject.getBriefDescriptions()));
            item.setComputedCurrentLocation(collectionObject.getComputedCurrentLocation());

            item.setTitle(TitleGroupListModel.titleGroupListToDisplayString(collectionObject.getTitleGroupList()));
            item.setResponsibleDepartment(
                    ResponsibleDepartmentsListModel.responsibleDepartmentString(collectionObject));

            item.setObjectName(ObjectNameListModel.objectName(collectionObject));
            item.setObjectNameControlled(ObjectNameListModel.objectNameControlled(collectionObject));

            item.setContentConcepts(ContentConceptListModel.contentConceptList(collectionObject));

            // Field collection items (place, site, date, collector, role)
            item.setFieldCollectionPlace(FieldCollectionModel.fieldCollectionPlace(collectionObject));
            item.setFieldCollectionSite(FieldCollectionModel.fieldCollectionSite(collectionObject));
            item.setFieldCollectionDate(FieldCollectionModel.fieldCollectionDate(collectionObject));
            FieldCollectionModel.fieldCollector(collectionObject).ifPresent(collector -> {
                item.setFieldCollector(collector);
                item.setFieldCollectorRole("field collector"); // todo: how would we i18n this?
            });

            // Object Production Information (place, date, agent, agent role)
            item.setObjectProductionDate(ObjectProductionModel.objectProductionDate(collectionObject));
            item.setObjectProductionPlace(ObjectProductionModel.objectProductionPlace(collectionObject));

            AgentModel.agent(collectionObject).ifPresent(agent -> {
                item.setAgent(agent.getAgent());
                item.setAgentRole(agent.getRole());
            });

            item.setForm(TaxonModel.preservationForm(collectionObject));

            // from media resource
            if (blobCsids.size() > 0) {
                item.setBlobCsid(blobCsids.get(0));
            }
        } else {
            logger.warn("advancedsearch: could not find CollectionobjectsCommon associated with csid {}", csid);
        }

        if (naturalHistory != null) {
            item.setTaxon(TaxonModel.taxon(naturalHistory));
        }

        if (objectsNAGPRA != null) {
            item.setNagpraCategories(NAGPRACategoryModel.napgraCategories(objectsNAGPRA));
        }

        return item;
    }

    public <T> T unmarshall(Class<T> clazz, String namespace, PoxPayloadOut out, Unmarshaller unmarshaller) {
        PayloadOutputPart part = out.getPart(namespace);
        if (part == null) {
            return null;
        }

        try {
            return clazz.cast(unmarshaller.unmarshal((Document) part.getBody()));
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }
}
