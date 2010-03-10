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
package org.collectionspace.services.common.vocabulary;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.service.ServiceBindingType;

/**
 * RefNameServiceUtils is a collection of services utilities related to refName usage.
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RefNameServiceUtils {
	
	public static final String AUTH_REF_PROP = "authRef";

    private final Logger logger = LoggerFactory.getLogger(RefNameServiceUtils.class);
    
    public AuthorityRefDocList getAuthorityRefDocs(RepositoryClient repoClient, 
    		String tenantId, String serviceType, String refName,
    		int pageSize, int pageNum, boolean computeTotal )
    		throws DocumentException, DocumentNotFoundException {
    	AuthorityRefDocList wrapperList = new AuthorityRefDocList();
        List<AuthorityRefDocList.AuthorityRefDocItem> list = 
        	wrapperList.getAuthorityRefDocItem();
    	TenantBindingConfigReaderImpl tReader =
            ServiceMain.getInstance().getTenantBindingConfigReader();
    	List<ServiceBindingType> servicebindings = tReader.getServiceBindingsByType(tenantId, serviceType);
    	if(servicebindings==null || servicebindings.size()>0)
    		return null;
    	String domain = tReader.getTenantBinding(tenantId).getRepositoryDomain();
    	ArrayList<String> docTypes = new ArrayList<String>(); 
    	StringBuilder whereClause = new StringBuilder();
    	for(ServiceBindingType sb:servicebindings) {
    		List<String> authRefFields = ServiceBindingUtils.getPropertyValues(sb, AUTH_REF_PROP);
    		String docType = sb.getObject().getName();
    		docTypes.add(docType);
    		for(String field:authRefFields) {
    			// Build up the where clause for each authRef field
    			throw new UnsupportedOperationException();
    		}
    	}
		// Now we have to issue the search
		DocumentWrapper<DocumentModelList> docListWrapper = repoClient.findDocs(
	    		docTypes, whereClause.toString(), domain, pageSize, pageNum, computeTotal );
    	return null;
    }

		
}

