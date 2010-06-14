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
 *  See the License for the specific language governing accountRoles and
 *  limitations under the License.
 */
package org.collectionspace.services.account.storage;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.authorization.AccountRole;
import org.collectionspace.services.authorization.AccountRoleRel;
import org.collectionspace.services.authorization.AccountValue;
import org.collectionspace.services.authorization.PermissionsRolesList;
import org.collectionspace.services.authorization.RoleValue;
import org.collectionspace.services.authorization.SubjectType;
import org.collectionspace.services.common.context.ServiceContext;

import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.common.context.ServiceContextProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Document handler for AccountRole association
 * @author 
 */
public class AccountRoleDocumentHandler
        extends AbstractDocumentHandlerImpl<AccountRole, PermissionsRolesList, List<AccountRoleRel>, List<AccountRoleRel>> {

    private final Logger logger = LoggerFactory.getLogger(AccountRoleDocumentHandler.class);
    private AccountRole accountRole;
    private PermissionsRolesList accountRolesList;

    @Override
    public void handleCreate(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc);
    }

    @Override
    public void handleUpdate(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    @Override
    public void completeUpdate(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    @Override
    public void handleGet(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        setCommonPart(extractCommonPart(wrapDoc));
        getServiceContext().setOutput(accountRole);
    }

    @Override
    public void handleGetAll(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    @Override
    public void handleDelete(DocumentWrapper<List<AccountRoleRel>> wrapDoc) throws Exception {
        fillCommonPart(getCommonPart(), wrapDoc);
    }

    @Override
    public AccountRole extractCommonPart(
            DocumentWrapper<List<AccountRoleRel>> wrapDoc)
            throws Exception {
        List<AccountRoleRel> arrl = wrapDoc.getWrappedObject();
        AccountRole ar = new AccountRole();
        SubjectType subject = getSubject(getServiceContext());
        if (arrl.size() == 0) {
            return ar;
        }
        AccountRoleRel ar0 = arrl.get(0);
        if (SubjectType.ROLE.equals(subject)) {

            List<AccountValue> avs = new ArrayList<AccountValue>();
            ar.setAccounts(avs);
            AccountValue av = buildAccountValue(ar0);
            avs.add(av);

            //add roles
            List<RoleValue> rvs = new ArrayList<RoleValue>();
            ar.setRoles(rvs);
            for (AccountRoleRel arr : arrl) {
                RoleValue rv = buildRoleValue(arr);
                rvs.add(rv);
            }
        } else if (SubjectType.ACCOUNT.equals(subject)) {

            List<RoleValue> rvs = new ArrayList<RoleValue>();
            ar.setRoles(rvs);
            RoleValue rv = buildRoleValue(ar0);
            rvs.add(rv);

            //add accounts
            List<AccountValue> avs = new ArrayList<AccountValue>();
            ar.setAccounts(avs);
            for (AccountRoleRel arr : arrl) {
                AccountValue av = buildAccountValue(arr);
                avs.add(av);
            }
        }
        return ar;
    }

    @Override
    public void fillCommonPart(AccountRole ar, DocumentWrapper<List<AccountRoleRel>> wrapDoc)
            throws Exception {
        List<AccountRoleRel> arrl = wrapDoc.getWrappedObject();
        SubjectType subject = ar.getSubject();
        if (subject == null) {
            //it is not required to give subject as URI determines the subject
            subject = getSubject(getServiceContext());
        } else {
            //subject mismatch should have been checked during validation
        }
        if (subject.equals(SubjectType.ROLE)) {
            //FIXME: potential index out of bounds exception...negative test needed
            AccountValue av = ar.getAccounts().get(0);

            for (RoleValue rv : ar.getRoles()) {
                AccountRoleRel arr = buildAccountRoleRel(av, rv);
                arrl.add(arr);
            }
        } else if (SubjectType.ACCOUNT.equals(subject)) {
            //FIXME: potential index out of bounds exception...negative test needed
            RoleValue rv = ar.getRoles().get(0);
            for (AccountValue av : ar.getAccounts()) {
                AccountRoleRel arr = buildAccountRoleRel(av, rv);
                arrl.add(arr);
            }
        }
    }

    @Override
    public PermissionsRolesList extractCommonPartList(
            DocumentWrapper<List<AccountRoleRel>> wrapDoc)
            throws Exception {

        throw new UnsupportedOperationException("operation not relevant for AccountRoleDocumentHandler");
    }

    @Override
    public AccountRole getCommonPart() {
        return accountRole;
    }

    @Override
    public void setCommonPart(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    @Override
    public PermissionsRolesList getCommonPartList() {
        return accountRolesList;
    }

    @Override
    public void setCommonPartList(PermissionsRolesList accountRolesList) {
        this.accountRolesList = accountRolesList;
    }

    @Override
    public String getQProperty(
            String prop) {
        return null;
    }

    @Override
    public DocumentFilter createDocumentFilter() {
        return new DocumentFilter(this.getServiceContext());
    }

    private AccountValue buildAccountValue(AccountRoleRel arr) {
        AccountValue av = new AccountValue();
        av.setAccountId(arr.getAccountId());
        av.setUserId(arr.getUserId());
        av.setScreenName(arr.getScreenName());
        return av;
    }

    private RoleValue buildRoleValue(AccountRoleRel arr) {
        RoleValue rv = new RoleValue();
        rv.setRoleId(arr.getRoleId());
        rv.setRoleName(arr.getRoleName());
        return rv;
    }

    private AccountRoleRel buildAccountRoleRel(AccountValue av, RoleValue rv) {
        AccountRoleRel arr = new AccountRoleRel();
        arr.setAccountId(av.getAccountId());
        arr.setUserId(av.getUserId());
        arr.setScreenName(av.getScreenName());

        arr.setRoleId(rv.getRoleId());
        arr.setRoleName(rv.getRoleName());
        return arr;
    }

    static SubjectType getSubject(ServiceContext ctx) {
        Object o = ctx.getProperty(ServiceContextProperties.SUBJECT);
        if (o == null) {
            throw new IllegalArgumentException(ServiceContextProperties.SUBJECT
                    + " property is missing in context "
                    + ctx.toString());
        }
        return (SubjectType) o;
    }
}
