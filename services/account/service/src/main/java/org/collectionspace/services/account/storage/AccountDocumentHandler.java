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

import org.collectionspace.services.account.AccountsCommon;
import org.collectionspace.services.account.AccountsCommonList;
import org.collectionspace.services.common.document.AbstractDocumentHandler;
import org.collectionspace.services.common.document.DocumentWrapper;


/**
 *
 * @author 
 */
public class AccountDocumentHandler
        extends AbstractDocumentHandler<AccountsCommon, AccountsCommonList> {

    @Override
    public void handleCreate(DocumentWrapper wrapDoc) throws Exception {
    }

    @Override
    public void handleUpdate(DocumentWrapper wrapDoc) throws Exception {
    }

    @Override
    public void handleGet(DocumentWrapper wrapDoc) throws Exception {
    }

    @Override
    public void handleGetAll(DocumentWrapper wrapDoc) throws Exception {
    }

    @Override
    public void extractAllParts(DocumentWrapper wrapDoc)
            throws Exception {
    }

    @Override
    public void fillAllParts(DocumentWrapper wrapDoc)
            throws Exception {
    }

    @Override
    public AccountsCommon extractCommonPart(DocumentWrapper wrapDoc)
            throws Exception {
        return null;
    }

    @Override
    public void fillCommonPart(AccountsCommon obj, DocumentWrapper wrapDoc)
            throws Exception {
    }

    @Override
    public AccountsCommonList extractCommonPartList(DocumentWrapper wrapDoc)
            throws Exception {
        return null;
    }

    @Override
    public AccountsCommon getCommonPart() {
        return null;
    }

    @Override
    public void setCommonPart(AccountsCommon obj) {
    }

    @Override
    public AccountsCommonList getCommonPartList() {
        return null;
    }

    @Override
    public void setCommonPartList(AccountsCommonList obj) {
    }

    @Override
    public String getQProperty(String prop) {
        return null;
    }
}
