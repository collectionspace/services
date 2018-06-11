package org.collectionspace.services.common.relation;

import java.util.Arrays;
import java.util.List;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IQueryManager;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoRepositoryClientImpl;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationUtils {

    private static final Logger logger = LoggerFactory.getLogger(RelationUtils.class);
    private static final int DEFAULT_PAGE_SIZE = 1000;

    /*
     * Performs an NXQL query to find refName references in relationship records.
     */
    private static DocumentModelList findRelationsWithRefName(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            CoreSessionInterface repoSession,
            String refName,
            String targetField,
            String orderByClause,
            int pageNum,
            int pageSize,
            boolean computeTotal) throws DocumentException, DocumentNotFoundException {
        
        List<String> docTypes = Arrays.asList(IRelationsManager.DOC_TYPE);

        String escapedRefName = refName.replace("'", "\\'"); // We need to escape single quotes for NXQL
        // e.g., "SELECT * FROM Relation WHERE 
        // collectionspace_core:tenantid = '1' AND
        // relations_common:subjectRefName = 'urn:cspace:core.collectionspace.org:placeauthorities:name(place):item:name(Amystan1348082103923)\'Amystan\''"
        String query = String.format("%s:%s = '%s'", IRelationsManager.SERVICE_COMMONPART_NAME, targetField, escapedRefName);

        NuxeoRepositoryClientImpl nuxeoRepoClient = (NuxeoRepositoryClientImpl) repoClient;
        DocumentWrapper<DocumentModelList> docListWrapper = nuxeoRepoClient.findDocs(ctx, 
                repoSession,
                docTypes, 
                query, 
                orderByClause, 
                pageNum,
                pageSize, 
                true, // useDefaultOrderByClause if 'orderByClause' is null
                computeTotal);
        DocumentModelList docList = docListWrapper.getWrappedObject();
        
        return docList;
    }

    /*
     * Find all the relationship records with the targetField (either subjectRefName or objectRefName) set to the old refName and
     * update it to contain the new refName.
     */
    public static void updateRefNamesInRelations(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            CoreSessionInterface repoSession,
            String targetField,
            String oldRefName,
            String newRefName) throws Exception {

        int docsUpdated = 0;
        int currentPage = 0;
        int docsInCurrentPage = 0;
        final String ORDER_BY_VALUE = CollectionSpaceClient.CORE_CREATED_AT
                                          + ", " + IQueryManager.NUXEO_UUID; // CSPACE-6333: Add secondary sort on uuid, in case records have the same createdAt timestamp.

        try {
            boolean morePages = true;
            while (morePages) {
    
                DocumentModelList docModelList = findRelationsWithRefName(
                        ctx,
                        repoClient,
                        repoSession,
                        oldRefName,
                        targetField,
                        ORDER_BY_VALUE,
                        currentPage,
                        DEFAULT_PAGE_SIZE,
                        true);
    
                if (docModelList == null) {
                    logger.trace("updateRefNamesInRelations: no documents could be found that referenced the old refName");
                    break;
                }
                docsInCurrentPage = docModelList.size();
                logger.trace("updateRefNamesInRelations: current page=" + currentPage + " documents included in page=" + docsInCurrentPage);
                if (docsInCurrentPage == 0) {
                    logger.trace("updateRefNamesInRelations: no more documents requiring refName updates could be found");
                    break;
                }
                if (docsInCurrentPage < DEFAULT_PAGE_SIZE) {
                    logger.trace("updateRefNamesInRelations: assuming no more documents requiring refName updates will be found, as docsInCurrentPage < pageSize");
                    morePages = false;
                }
    
                for (DocumentModel docModel : docModelList) {
                    try {
                        docModel.setProperty(IRelationsManager.SERVICE_COMMONPART_NAME, targetField, newRefName);
                        repoSession.saveDocument(docModel);
                    } catch (ClientException e) {
                        logger.error(String.format("Could not update field '%s' with updated refName '%s' for relations record CSID=%s",
                                targetField, newRefName, docModel.getName()));
                    }
                }
    
                // FIXME: Per REM, set a limit of num objects - something like
                // 1000K objects - and also add a log Warning after some threshold
                docsUpdated += docsInCurrentPage;
                if (morePages) {
                    currentPage++;
                }
            }
        } catch (Exception e) {
            logger.error("Internal error updating the ref names in relations: " + e.getLocalizedMessage());
            logger.debug(Tools.errorToString(e, true));
            throw e;
        }

        //
        // Flush the results
        //
        try {
            repoSession.save();
        } catch (ClientException e) {
            // TODO Auto-generated catch block
            logger.error("Could not flush results of relation-refName payload updates to Nuxeo repository");
        }

        logger.debug("updateRefNamesInRelations updated " + docsUpdated + " relations document(s)"
                + " with new refName " + newRefName
                + " where " + targetField + " contained old refName " + oldRefName);

    }
}