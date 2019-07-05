/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.index.nuxeo;

import java.util.List;

import org.collectionspace.services.client.index.IndexClient;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.api.Tools;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.types.PropertyItemType;
import org.collectionspace.services.index.IndexCommon;
import org.collectionspace.services.nuxeo.client.java.NuxeoDocumentModelHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntakeDocumentModelHandler
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class IndexDocumentModelHandler
        extends NuxeoDocumentModelHandler<IndexCommon> {
	private final Logger logger = LoggerFactory.getLogger(IndexDocumentModelHandler.class);

	@Override
	public String getDocumentsToIndexQuery(String indexId, String csid) throws DocumentException, Exception {
		return getDocumentsToIndexQuery(indexId, "Document", csid);
	}

	@Override
    public String getDocumentsToIndexQuery(String indexId, String documentType, String csid) throws DocumentException, Exception {
		String result = null;

    	switch (indexId) {
    		case IndexClient.FULLTEXT_ID:
    			result = getReindexQuery(indexId, documentType, csid);
    			break;
    		case IndexClient.ELASTICSEARCH_ID:
    			result = getReindexQuery(indexId, documentType, csid);

    			break;
    	}

    	if (Tools.isEmpty(result) == true) {
    		String msg = String.format("There is no reindex query in the Index service bindings for index '%s', so we'll use this default query: '%s'",
    				indexId, IndexClient.DEFAULT_REINDEX_QUERY);
    		logger.warn(msg);
    		result = IndexClient.DEFAULT_REINDEX_QUERY;
    	}

    	return result;
    }

	/**
	 * Reads the Index service bindings to get the query that will be used to find all documents needing
	 * reindexing.
	 *
	 * @param indexId
	 * @param documentType
	 * @param csid
	 * @return
	 * @throws DocumentException
	 * @throws Exception
	 *
	 * TODO: Use the incoming CSID param to qualify the returned query.
	 */
	private String getReindexQuery(String indexId, String documentType, String csid) throws DocumentException, Exception {
		String result = null;

		//
		// Read in the NXQL query to use when performing a full
		//
		TenantBindingConfigReaderImpl tReader = ServiceMain.getInstance().getTenantBindingConfigReader();
		ServiceContext ctx = this.getServiceContext();

		ServiceBindingType serviceBinding = tReader.getServiceBinding(ctx.getTenantId(), ctx.getServiceName());
		List<PropertyItemType> queryTypeList = ServiceBindingUtils.getPropertyValueList(serviceBinding, indexId);

		if (queryTypeList != null && queryTypeList.isEmpty() == false) {
			PropertyItemType propertyItemType = queryTypeList.get(0);
			if (propertyItemType != null) {
				String query = propertyItemType.getValue();

				result = String.format(query, documentType);
			}
		}

		return result;
	}

}
