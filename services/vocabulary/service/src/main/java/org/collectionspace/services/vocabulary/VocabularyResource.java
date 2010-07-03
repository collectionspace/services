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
package org.collectionspace.services.vocabulary;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityItemJAXBSchema;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.vocabulary.nuxeo.VocabularyItemDocumentModelHandler;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class VocabularyResource.
 */
@Path("/vocabularies")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class VocabularyResource extends 
	AuthorityResource<VocabulariesCommon, VocabulariesCommonList, VocabularyitemsCommonList,
						VocabularyItemDocumentModelHandler> {

    private final static String vocabularyServiceName = "vocabularies";
	private final static String VOCABULARIES_COMMON = "vocabularies_common";
    
    private final static String vocabularyItemServiceName = "vocabularyitems";
	private final static String VOCABULARYITEMS_COMMON = "vocabularyitems_common";
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(VocabularyResource.class);

	/**
	 * Instantiates a new VocabularyResource.
	 */
	public VocabularyResource() {
		super(VocabulariesCommon.class, VocabularyResource.class,
				VOCABULARIES_COMMON, VOCABULARYITEMS_COMMON);
	}

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return vocabularyServiceName;
    }

    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    @Override
    public String getItemServiceName() {
        return vocabularyItemServiceName;
    }
    
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.CollectionSpaceResource#getCommonPartClass()
	 */
	@Override
	public Class<VocabulariesCommon> getCommonPartClass() {
		return VocabulariesCommon.class;
	}

}
