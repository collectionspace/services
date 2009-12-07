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

import java.util.List;
import java.util.UUID;
import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.account.AccountsCommonList.AccountListItem;
import org.collectionspace.services.account.Status;
import org.collectionspace.services.common.document.AbstractDocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;

/**
 *
 * @author 
 */
public class AccountDocumentHandler
        extends AbstractDocumentHandler<AccountsCommon, AccountsCommonList, AccountsCommon, List> {

    private AccountsCommon account;
    private AccountsCommonList accountList;

    @Override
    public void handleCreate(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        String id = UUID.randomUUID().toString();
        AccountsCommon account = wrapDoc.getWrappedObject();
        account.setCsid(id);
        account.setStatus(Status.ACTIVE);
    }

    @Override
    public void handleUpdate(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        getServiceContext().setOutput(account);
    }

    @Override
    public void completeUpdate(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        AccountsCommon upAcc = wrapDoc.getWrappedObject();
        sanitize(upAcc);
    }

    @Override
    public void handleGet(DocumentWrapper<AccountsCommon> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        sanitize(getCommonPart());
        getServiceContext().setOutput(account);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List> wrapDoc) throws Exception {
        AccountsCommonList accList = extractCommonPartList(wrapDoc);
        setCommonPartList(accList);
        getServiceContext().setOutput(getCommonPartList());
    }

    @Override
    public AccountsCommon extractCommonPart(
            DocumentWrapper<AccountsCommon> wrapDoc)
            throws Exception {
        return wrapDoc.getWrappedObject();
    }

    @Override
    public void fillCommonPart(AccountsCommon obj, DocumentWrapper<AccountsCommon> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountDocumentHandler");
    }

    @Override
    public AccountsCommonList extractCommonPartList(
            DocumentWrapper<List> wrapDoc)
            throws Exception {

        AccountsCommonList accList = new AccountsCommonList();
        List<AccountsCommonList.AccountListItem> list = accList.getAccountListItem();

        for (Object obj : wrapDoc.getWrappedObject()) {
            AccountsCommon account = (AccountsCommon) obj;
            AccountListItem accListItem = new AccountListItem();
            accListItem.setScreenName(account.getScreenName());
            accListItem.setEmail(account.getEmail());
            accListItem.setFirstName(account.getFirstName());
            accListItem.setLastName(account.getLastName());
            accListItem.setStatus(account.getStatus());
            String id = account.getCsid();
            accListItem.setUri(getServiceContextPath() + id);
            accListItem.setCsid(id);
            list.add(accListItem);
        }
        return accList;
    }

    @Override
    public AccountsCommon getCommonPart() {
        return account;
    }

    @Override
    public void setCommonPart(AccountsCommon account) {
        this.account = account;
    }

    @Override
    public AccountsCommonList getCommonPartList() {
        return accountList;
    }

    @Override
    public void setCommonPartList(AccountsCommonList accountList) {
        this.accountList = accountList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    /**
     * sanitize removes data not needed to be sent to the consumer
     * @param account
     */
    private void sanitize(AccountsCommon account) {
        account.setTenantid("");
    }
}
