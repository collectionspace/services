package org.collectionspace.services.nuxeo.client.java;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.nuxeo.util.ReindexFulltextRoot;
import org.collectionspace.services.nuxeo.util.ReindexFulltextRoot.ReindexInfo;
import org.nuxeo.ecm.core.api.IterableQueryResult;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.query.QueryFilter;
import org.nuxeo.ecm.core.query.sql.NXQL;
import org.nuxeo.ecm.core.storage.StorageException;
import org.nuxeo.runtime.transaction.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Use the inherited reindexFulltext() method to reindex the Nuxeo full-text index.
 */
public class CSReindexFulltextRoot extends ReindexFulltextRoot {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(CSReindexFulltextRoot.class);
	protected String repoQuery;

	public CSReindexFulltextRoot(CoreSessionInterface repoSession, String repoQuery) {
		this.coreSession = repoSession.getCoreSession();
		this.repoQuery = repoQuery;
	}
	
	
	@Override
    protected List<ReindexInfo> getInfos() throws StorageException {
        getLowLevelSession();
        List<ReindexInfo> infos = new ArrayList<ReindexInfo>();
//        String query = "SELECT ecm:uuid, ecm:primaryType FROM Document"
//                + " WHERE ecm:isProxy = 0"
//                + " AND ecm:currentLifeCycleState <> 'deleted'"
//                + " ORDER BY ecm:uuid";
        IterableQueryResult it = session.queryAndFetch(this.repoQuery, NXQL.NXQL,
                QueryFilter.EMPTY);
        try {
            for (Map<String, Serializable> map : it) {
                Serializable id = map.get(NXQL.ECM_UUID);
                String type = (String) map.get(NXQL.ECM_PRIMARYTYPE);
                infos.add(new ReindexInfo(id, type));
            }
        } finally {
            it.close();
        }
        return infos;
    }    

}
