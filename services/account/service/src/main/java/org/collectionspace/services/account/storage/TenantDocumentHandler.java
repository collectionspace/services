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
package org.collectionspace.services.account.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.account.Tenant;
import org.collectionspace.services.account.TenantsList;
import org.collectionspace.services.account.TenantListItem;

import org.collectionspace.services.client.TenantClient;
import org.collectionspace.services.common.storage.jpa.JpaDocumentHandler;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.document.JaxbUtils;
import org.collectionspace.services.common.security.SecurityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author
 */
public class TenantDocumentHandler
        extends JpaDocumentHandler<Tenant, TenantsList, Tenant, List> {

    private final Logger logger = LoggerFactory.getLogger(TenantDocumentHandler.class);
    private Tenant tenant;
    private TenantsList tenantList;

    @Override
    public void handleCreate(DocumentWrapper<Tenant> wrapDoc) throws Exception {
    }

    @Override
    public void handleUpdate(DocumentWrapper<Tenant> wrapDoc) throws Exception {
        Tenant tenantFound = wrapDoc.getWrappedObject();
        Tenant tenantReceived = getCommonPart();
        // If marked as metadata immutable, do not do update
       	merge(tenantReceived, tenantFound);
    }

    /**
     * merge manually merges the from account to the to account
     * -this method is created due to inefficiency of JPA EM merge
     * @param from
     * @param to
     * @return merged account
     */
    private Tenant merge(Tenant from, Tenant to) {
        Date now = new Date();
        to.setUpdatedAtItem(now);
        // The only thing we allow changing at this point are the 'disabled' and 'authoritiesInitialized' flags
        to.setDisabled(from.isDisabled());
        to.setAuthoritiesInitialized(from.isAuthoritiesInitialized());

        if (logger.isDebugEnabled()) {
            logger.debug("merged account="
                    + JaxbUtils.toString(to, Tenant.class));
        }
        return to;
    }


    @Override
    public void completeUpdate(DocumentWrapper<Tenant> wrapDoc) throws Exception {
        Tenant upAcc = wrapDoc.getWrappedObject();
        getServiceContext().setOutput(upAcc);
    }

    @Override
    public void handleGet(DocumentWrapper<Tenant> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        getServiceContext().setOutput(tenant);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List> wrapDoc) throws Exception {
        TenantsList tenList = extractCommonPartList(wrapDoc);
        setCommonPartList(tenList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public Tenant extractCommonPart(
            DocumentWrapper<Tenant> wrapDoc)
            throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public void fillCommonPart(Tenant obj, DocumentWrapper<Tenant> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException("operation not relevant for TenantDocumentHandler");
    }

    @Override
    public TenantsList extractCommonPartList(
            DocumentWrapper<List> wrapDoc)
            throws Exception {

    	TenantsList tenList = this.extractPagingInfo(new TenantsList(), wrapDoc);
//        TenantsList accList = new TenantsList();
        List<TenantListItem> list = tenList.getTenantListItem();

        for (Object obj : wrapDoc.getWrappedObject()) {
            Tenant tenant = (Tenant) obj;
            TenantListItem tenListItem = new TenantListItem();
            tenListItem.setId(tenant.getId());
            tenListItem.setName(tenant.getName());
            tenListItem.setDisabled(tenant.isDisabled());
            list.add(tenListItem);
        }
        return tenList;
    }

    @Override
    public Tenant getCommonPart() {
        return tenant;
    }

    @Override
    public void setCommonPart(Tenant tenant) {
        this.tenant = tenant;
    }

    @Override
    public TenantsList getCommonPartList() {
        return tenantList;
    }

    @Override
    public void setCommonPartList(TenantsList tenantList) {
        this.tenantList = tenantList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter() {
        DocumentFilter filter = new TenantJpaFilter(this.getServiceContext());
        return filter;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.DocumentHandler#initializeDocumentFilter(org.collectionspace.services.common.context.ServiceContext)
     */
    public void initializeDocumentFilter(ServiceContext ctx) {
        // set a default document filter in this method
    }
}
