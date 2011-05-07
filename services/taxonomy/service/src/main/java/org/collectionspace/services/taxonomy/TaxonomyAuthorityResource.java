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
package org.collectionspace.services.taxonomy;

import java.util.List;

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

import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.common.vocabulary.AuthorityJAXBSchema;
import org.collectionspace.services.TaxonJAXBSchema;
import org.collectionspace.services.common.AbstractMultiPartCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.authorityref.AuthorityRefDocList;
import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.MultipartServiceContextImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.BadRequestException;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.security.UnauthorizedException;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils;
import org.collectionspace.services.common.vocabulary.RefNameUtils;
import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.taxonomy.nuxeo.TaxonDocumentModelHandler;
import org.jboss.resteasy.util.HttpResponseCodes;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TaxonomyAuthorityResource.
 */
@Path(TaxonomyAuthorityClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class TaxonomyAuthorityResource
        extends AuthorityResource<TaxonomyauthorityCommon, TaxonomyauthorityCommonList, TaxonCommon, TaxonDocumentModelHandler> {

    private final static String taxonomyAuthorityServiceName = "taxonomyauthorities";
    private final static String TAXONOMYAUTHORITY_COMMON = "taxonomyauthority_common";
    private final static String taxonomyItemServiceName = "taxon";
    private final static String TAXONOMYITEM_COMMON = "taxon_common";
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(TaxonomyAuthorityResource.class);
    //FIXME retrieve client type from configuration
    /** The Constant CLIENT_TYPE. */
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    /**
     * Instantiates a new taxonomy authority resource.
     */
    public TaxonomyAuthorityResource() {
        super(TaxonomyauthorityCommon.class, TaxonomyAuthorityResource.class,
                TAXONOMYAUTHORITY_COMMON, TAXONOMYITEM_COMMON);
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl#getServiceName()
     */
    @Override
    public String getServiceName() {
        return taxonomyAuthorityServiceName;
    }

    /**
     * Gets the item service name.
     * 
     * @return the item service name
     */
    @Override
    public String getItemServiceName() {
        return taxonomyItemServiceName;
    }

    @Override
    public Class<TaxonomyauthorityCommon> getCommonPartClass() {
        return TaxonomyauthorityCommon.class;
    }
}
