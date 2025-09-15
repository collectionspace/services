package org.collectionspace.services.nuxeo.client.handler;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentUtils;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.collectionspace.services.nuxeo.client.java.DocumentModelHandler;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentFilter;
import org.dom4j.io.DOMReader;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.schema.types.Field;
import org.nuxeo.ecm.core.schema.types.Schema;

/**
 * A DocumentModelHandler which does not restrict the view to a single xml part (e.g. collectionobjects_common)
 *
 * Note: currently the only use for this is the AdvancedSearch API which requires a full view of the Document being
 * returned to us by Nuxeo.
 *
 * @since 8.3.0
 */
public class UnfilteredDocumentModelHandler extends DocumentModelHandler<DocumentModel, AbstractCommonList> {

    private static final String NAME = "unfiltered-search-result";

    private AbstractCommonList commonList;
    private DocumentModel document;
    private DocumentFilter filter;

    @Override
    public DocumentFilter getDocumentFilter() {
        if (filter == null) {
            ServiceContext<PoxPayloadIn, PoxPayloadOut> context = getServiceContext();
            if (context == null) {
                throw new RuntimeException("DocumentModelHandler is missing its ServiceContext and cannot complete the "
                                           + "current request");
            }

            filter = new NuxeoDocumentFilter(context);

            // Initialize the sort, paging, and workflow params
            MultivaluedMap<String, String> queryParameters = context.getQueryParams();
            if (queryParameters != null && !queryParameters.isEmpty()) {
                filter.setSortOrder(queryParameters);
                filter.setPagination(queryParameters);
                String workflowWhereClause = buildWorkflowWhereClause(queryParameters);
                if (workflowWhereClause != null) {
                    filter.appendWhereClause(workflowWhereClause, IQueryManager.SEARCH_QUALIFIER_AND);
                }
            }
        }

        return filter;
    }

    /**
     * yoinked from AbstractServiceContextImpl. Might move into a common class since it's only returning a string.
     */
    private String buildWorkflowWhereClause(MultivaluedMap<String, String> queryParams) {
        String result = null;

        String includeDeleted = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_DELETED_QP);
        // if set to true, it doesn't matter what the value is for 'includeDeleted'
        String includeOnlyDeleted = queryParams.getFirst(WorkflowClient.WORKFLOW_QUERY_ONLY_DELETED_QP);

        if (includeOnlyDeleted != null) {
            if (Boolean.parseBoolean(includeOnlyDeleted)) {
                // A value of 'true' for 'includeOnlyDeleted' means we're looking *only* for soft-deleted records/documents.
                result = String.format("(ecm:currentLifeCycleState = '%s' OR ecm:currentLifeCycleState = '%s' OR ecm:currentLifeCycleState = '%s')",
                                       WorkflowClient.WORKFLOWSTATE_DELETED,
                                       WorkflowClient.WORKFLOWSTATE_LOCKED_DELETED,
                                       WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED);
            }
        } else if (includeDeleted != null && !Boolean.parseBoolean(includeDeleted)) {
            // We can only get here if the 'includeOnlyDeleted' query param is missing altogether.
            // Ensure we don't return soft-deleted records
            result = String.format("(ecm:currentLifeCycleState <> '%s' AND ecm:currentLifeCycleState <> '%s' AND ecm:currentLifeCycleState <> '%s')",
                                   WorkflowClient.WORKFLOWSTATE_DELETED,
                                   WorkflowClient.WORKFLOWSTATE_LOCKED_DELETED,
                                   WorkflowClient.WORKFLOWSTATE_REPLICATED_DELETED);
        }

        return result;
    }


    /**
     * This is similar to the implementation in
     * {@link org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler#extractAllParts(DocumentWrapper)}
     * except for a list result
     *
     * @param wrapDoc The document model list from nuxeo
     * @return A {@link CSDocumentModelList} with the {@link org.collectionspace.services.client.PayloadOutputPart}s
     *  for each document
     * @throws Exception if there are any errors handling the XML
     */
    @Override
    public AbstractCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        final String xmlNs = "ns2";
        final DocumentModelList documentModelList = wrapDoc.getWrappedObject();

        final CSDocumentModelList cspaceDocModelList = new CSDocumentModelList();
        final DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        final DocumentFilter docFilter = getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        cspaceDocModelList.setPageNum(pageNum);
        cspaceDocModelList.setPageSize(pageSize);
        DocumentModelList docList = wrapDoc.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        cspaceDocModelList.setItemsInPage(docList.size());
        // set the total result size
        cspaceDocModelList.setTotalItems(docList.totalSize());

        for (DocumentModel documentModel : documentModelList) {
            final PoxPayloadOut out = new PoxPayloadOut(NAME);

            // todo: only work on schemas we support
            for (Schema schema : documentModel.getDocumentType().getSchemas()) {
                final String schemaName = schema.getName();
                // todo: use dom4j so we don't convert between the two impls
                org.w3c.dom.Document document = documentBuilder.newDocument();
                final String namespaceUri = schema.getNamespace().uri;
                final String qualifiedName = xmlNs + ":" + schemaName;

                org.w3c.dom.Element root = document.createElementNS(namespaceUri, qualifiedName);
                root.setAttribute("xmlns:" + xmlNs, namespaceUri);
                document.appendChild(root);

                final Collection<Property> properties = documentModel.getPropertyObjects(schemaName);
                for (Property property : properties) {
                    final Field field = property.getField();
                    final Object value = property.getValue();

                    if (value != null) {
                        DocumentUtils.buildProperty(document, root, field, value);
                    }
                }

                final DOMReader reader = new DOMReader();
                final org.dom4j.Document dom4jDoc = reader.read(document);
                org.dom4j.Element result = dom4jDoc.getRootElement();
                result.detach();
                out.addPart(schemaName, result);
            }

            cspaceDocModelList.addResponsePayload(documentModel.getName(), out);
        }

        return cspaceDocModelList;
    }

    @Override
    public DocumentModel extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public DocumentModel getCommonPart() {
        return document;
    }

    @Override
    public void setCommonPart(DocumentModel document) {
        this.document = document;
    }

    @Override
    public AbstractCommonList getCommonPartList() {
        return commonList;
    }

    @Override
    public void setCommonPartList(AbstractCommonList obj) {
        commonList = obj;
    }

    @Override
    public String getQProperty(String prop) throws DocumentException {
        return "";
    }

    @Override
    public boolean supportsWorkflowStates() {
        return false;
    }

    // unsupported (for now)

    @Override
    protected String getRefnameDisplayName(DocumentWrapper<DocumentModel> docWrapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc,
                                         TransitionDef transitionDef) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void completeUpdate(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void extractAllParts(DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillAllParts(DocumentWrapper<DocumentModel> wrapDoc, Action action) throws Exception {

        throw new UnsupportedOperationException();
    }

    @Override
    public void fillCommonPart(DocumentModel obj, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public AbstractCommonList extractPagingInfo(AbstractCommonList theCommonList,
                                                DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public AuthorityRefList getAuthorityRefs(String csid,
                                             List<RefNameServiceUtils.AuthRefConfigInfo> authRefConfigInfoList)
        throws Exception {
        throw new UnsupportedOperationException();
    }
}
