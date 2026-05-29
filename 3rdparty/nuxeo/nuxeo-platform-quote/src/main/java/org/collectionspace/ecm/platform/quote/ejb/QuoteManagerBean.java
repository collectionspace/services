/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

package org.collectionspace.ecm.platform.quote.ejb;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.DocumentModel;

import org.collectionspace.ecm.platform.quote.api.QuoteManager;
import org.collectionspace.ecm.platform.quote.service.QuoteService;
import org.collectionspace.ecm.platform.quote.service.QuoteServiceHelper;

/**
 * @author <a href="mailto:glefter@nuxeo.com">George Lefter</a>
 *
 */
@Stateless
@Remote(QuoteManager.class)
@Local(QuoteManagerLocal.class)
public class QuoteManagerBean implements QuoteManager {

    @Resource
    EJBContext context;

    private QuoteManager quoteManager;

    @PostConstruct
    public void initialize() {
        QuoteService quoteService = QuoteServiceHelper.getQuoteService();
        quoteManager = quoteService.getQuoteManager();
    }

    public void cleanup() {}

    public void remove() {}

    @Override
    public DocumentModel createQuote(DocumentModel docModel,
            String quote) throws NuxeoException {
        try {
            String author = context.getCallerPrincipal().getName();
            return quoteManager.createQuote(docModel, quote, author);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public DocumentModel createQuote(DocumentModel docModel,
            String quote, String author) throws NuxeoException {
        try {
            return quoteManager.createQuote(docModel, quote, author);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    private String updateAuthor(DocumentModel docModel) {
        String author;
        try {
            author = (String) docModel.getProperty("comment", "author");
        } catch (NuxeoException e) {
            author = null;
        }
        if (author == null) {
            author = context.getCallerPrincipal().getName();
            try {
                docModel.setProperty("comment", "author", author);
            } catch (NuxeoException e) {
                throw new ClientRuntimeException(e);
            }
        }
        return author;
    }

    @Override
    public DocumentModel createQuote(DocumentModel docModel,
            DocumentModel quote) throws NuxeoException {
        try {
            updateAuthor(quote);
            return quoteManager.createQuote(docModel, quote);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public void deleteQuote(DocumentModel docModel, DocumentModel quote)
            throws NuxeoException {
        try {
            quoteManager.deleteQuote(docModel, quote);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public List<DocumentModel> getQuotes(DocumentModel docModel)
            throws NuxeoException {
        try {
            return quoteManager.getQuotes(docModel);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public DocumentModel createQuote(DocumentModel docModel,
            DocumentModel parent, DocumentModel child) throws NuxeoException {
        try {
            updateAuthor(child);
            return quoteManager.createQuote(docModel, parent, child);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public List<DocumentModel> getQuotes(DocumentModel docModel,
            DocumentModel parent) throws NuxeoException {
        try {
            return quoteManager.getQuotes(docModel, parent);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public List<DocumentModel> getDocumentsForQuote(DocumentModel commentDoc)
            throws NuxeoException{
        try {
            return quoteManager.getDocumentsForQuote(commentDoc);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

    @Override
    public DocumentModel createLocatedQuote(DocumentModel docModel,
            DocumentModel comment, String path) throws NuxeoException {
        try {
            return quoteManager.createLocatedQuote(docModel, comment, path);
        } catch (Throwable e) {
            throw NuxeoException.wrap(e);
        }
    }

}
