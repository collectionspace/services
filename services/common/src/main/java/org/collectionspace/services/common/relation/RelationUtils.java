package org.collectionspace.services.common.relation;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IRelationsManager;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RelationUtils {	
    private static final Logger logger = LoggerFactory.getLogger(RelationUtils.class);
	
	private static final int DEFAUT_PAGE_SIZE = 1000;

	/*
	 * Performs an NXQL query to find refName references in relationship records.
	 */
	private static DocumentModelList findRelationsWithRefName(
            RepositoryInstance repoSession,
			String refName,
			String targetField,
			String orderByField,
            int pageSize,
            int pageNum,
            boolean computeTotal) {
		DocumentModelList result = null;
		
		String escapedRefName = refName.replace("'", "\\'"); // We need to escape single quotes for NXQL
		String query = String.format("SELECT * FROM %s WHERE %s:%s = '%s'", // e.g., "SELECT * FROM Relation WHERE relations_common:subjectRefName = 'urn:cspace:core.collectionspace.org:placeauthorities:name(place):item:name(Amystan1348082103923)\'Amystan\''"
				IRelationsManager.DOC_TYPE,
				IRelationsManager.SERVICE_COMMONPART_NAME,
				targetField,
				escapedRefName);
		
		if (logger.isDebugEnabled() == true) {
			logger.debug(String.format("findRelationsWithRefName NXQL query is %s", query));
		}

		try {
			result = repoSession.query(query, null,
			        pageSize, pageNum, computeTotal);
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
    	
			DocumentModelList docModelList = findRelationsWithRefName( // FIXME: REM - Step through the pages correctly.
					repoSession,
					oldRefName,
					targetField,
					CollectionSpaceClient.CORE_CREATED_AT,
					DEFAUT_PAGE_SIZE,
					0,
					true);
			
			if (docModelList != null) {
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
			} else {
				// if docModelList was null then we already wrote out the error message to the logs
			}
    }
}
