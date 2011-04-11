/*
 * (C) Copyright 2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.collectionspace.ecm.platform.quote.impl;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.collectionspace.ecm.platform.quote.api.QuoteManager;
import org.collectionspace.ecm.platform.quote.api.QuoteableDocument;
import org.nuxeo.runtime.api.Framework;

/**
 * @author <a href="mailto:glefter@nuxeo.com">George Lefter</a>
 *
 */
public class QuoteableDocumentAdapter implements QuoteableDocument {

    private static final long serialVersionUID = 2996381735762615450L;

    final DocumentModel docModel;

    public QuoteableDocumentAdapter(DocumentModel docModel) {
        this.docModel = docModel;
    }

    private static QuoteManager getQuoteManager() {
        try {
            return Framework.getService(QuoteManager.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public DocumentModel addQuote(DocumentModel comment) throws ClientException {
        QuoteManager quoteManager = getQuoteManager();
        return quoteManager.createQuote(docModel, comment);
    }

    @Deprecated
    public DocumentModel addQuote(String comment) throws ClientException {
        QuoteManager quoteManager = getQuoteManager();
        return quoteManager.createQuote(docModel, comment);
    }

    @Override
    public DocumentModel addQuote(DocumentModel parent, DocumentModel comment) throws ClientException {
        QuoteManager quoteManager = getQuoteManager();
        return quoteManager.createQuote(docModel, parent, comment);
    }

    @Override
    public void removeQuote(DocumentModel comment) throws ClientException {
        QuoteManager quoteManager = getQuoteManager();
        quoteManager.deleteQuote(docModel, comment);
    }

    @Override
    public List<DocumentModel> getQuotes() throws ClientException {
        QuoteManager quoteManager = getQuoteManager();
        return quoteManager.getQuotes(docModel);
    }

    @Override
    public List<DocumentModel> getQuotes(DocumentModel parent) throws ClientException {
        QuoteManager quoteManager = getQuoteManager();
        return quoteManager.getQuotes(docModel, parent);
    }

}
