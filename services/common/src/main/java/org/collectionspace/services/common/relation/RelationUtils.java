package org.collectionspace.services.common.relation;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.query.QueryContext;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

public class RelationUtils {

    private static void updateRefNamesInRelations(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            RepositoryInstance repoSession,
            String targetField,
            String oldRefName,
            String newRefName) {
    	
        DocumentFilter filter = new DocumentFilter();
        String oldOrderBy = filter.getOrderByClause();
        if (Tools.isEmpty(oldOrderBy) == true){
            filter.setOrderByClause(DocumentFilter.ORDER_BY_LAST_UPDATED);
        }
        QueryContext queryContext = new QueryContext(ctx, handler);

        RepositoryInstance repoSession = null;
        
        DocumentModelList docList = null;
        String query = NuxeoUtils.buildNXQLQuery(ctx, queryContext);
        docList = repoSession.query(query);
    }
	

}
