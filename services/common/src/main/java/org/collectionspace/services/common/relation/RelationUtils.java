package org.collectionspace.services.common.relation;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.nuxeo.client.java.RepositoryJavaClientImpl;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationUtils {

    private static final Logger logger = LoggerFactory.getLogger(RelationUtils.class);
    private static final int DEFAULT_PAGE_SIZE = 1000;


    /*
     * Performs an NXQL query to find refName references in relationship records.
     */
    private static DocumentModelList findRelationsWithRefName(
            ServiceContext ctx,
            RepositoryInstance repoSession,
            String refName,
            String targetField,
            String orderByField,
            int pageSize,
            int pageNum,
            boolean computeTotal) {
        DocumentModelList result = null;
        
        String escapedRefName = refName.replace("'", "\\'"); // We need to escape single quotes for NXQL
        // e.g., "SELECT * FROM Relation WHERE 
        // collectionspace_core:tenantid = '1' AND
        // relations_common:subjectRefName = 'urn:cspace:core.collectionspace.org:placeauthorities:name(place):item:name(Amystan1348082103923)\'Amystan\''"
        String query = String.format("SELECT * FROM %s WHERE %s:%s = '%s' AND %s:%s = '%s'",
                IRelationsManager.DOC_TYPE,
                CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA,
                CollectionSpaceClient.COLLECTIONSPACE_CORE_TENANTID,
                ctx.getTenantId(),
                IRelationsManager.SERVICE_COMMONPART_NAME,
                targetField,
                escapedRefName);

        if (logger.isDebugEnabled() == true) {
            logger.debug(String.format("findRelationsWithRefName NXQL query is %s", query));
        }

        try {
            result = repoSession.query(query, null, pageSize, pageNum, computeTotal);
        } catch (ClientException e) {
            if (logger.isDebugEnabled() == true) {
                logger.debug(String.format("Exception caught while looking for refNames in relationship records for updating: refName %s",
                        refName), e);
            }
        }

        return result;
    }

    /*
     * Find all the relationship records with the targetField (either subjectRefName or objectRefName) set to the old refName and
     * update it to contain the new refName.
     */
    public static void updateRefNamesInRelations(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            RepositoryInstance repoSession,
            String targetField,
            String oldRefName,
            String newRefName) {

        int docsUpdated = 0;
        int currentPage = 0;
        int docsInCurrentPage = 0;

        boolean morePages = true;
        while (morePages) {

            DocumentModelList docModelList = findRelationsWithRefName(
                    ctx,
                    repoSession,
                    oldRefName,
                    targetField,
                    CollectionSpaceClient.CORE_CREATED_AT,
                    DEFAULT_PAGE_SIZE,
                    currentPage,
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
            //
            // Flush the results
            //
            try {
                repoSession.save();
            } catch (ClientException e) {
                // TODO Auto-generated catch block
                logger.error("Could not flush results of relation-refName payload updates to Nuxeo repository");
            }

            // FIXME: Per REM, set a limit of num objects - something like
            // 1000K objects - and also add a log Warning after some threshold
            docsUpdated += docsInCurrentPage;
            if (morePages) {
                currentPage++;
            }
        }
        
        logger.debug("updateRefNamesInRelations updated " + docsUpdated + " relations document(s)"
                + " with new refName " + newRefName
                + " where " + targetField + " contained old refName " + oldRefName);

    }
}