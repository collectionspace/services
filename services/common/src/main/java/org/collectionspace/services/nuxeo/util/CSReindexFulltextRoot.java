package org.collectionspace.services.nuxeo.util;

import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.util.ReindexFulltextRoot;
import org.nuxeo.ecm.core.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Use the inherited reindexFulltext() method to reindex the Nuxeo full-text index.
 */
public class CSReindexFulltextRoot extends ReindexFulltextRoot {

	/** The logger. */
	private final Logger logger = LoggerFactory.getLogger(CSReindexFulltextRoot.class);

	public CSReindexFulltextRoot(CoreSessionInterface repoSession) {
		this.coreSession = repoSession.getCoreSession();
	}

    public String reindexFulltext(int batchSize, int batch, String query) throws StorageException {
		return super.reindexFulltext(batchSize, batch, query);
	}
}
