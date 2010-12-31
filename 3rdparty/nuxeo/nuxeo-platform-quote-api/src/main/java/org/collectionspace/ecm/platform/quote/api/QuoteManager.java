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

package org.collectionspace.ecm.platform.quote.api;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;

/**
 * @author <a href="mailto:glefter@nuxeo.com">George Lefter</a>
 *
 */
public interface QuoteManager {

    List<DocumentModel> getQuotes(DocumentModel docModel)
            throws ClientException;

    List<DocumentModel> getQuotes(DocumentModel docModel, DocumentModel parent)
            throws ClientException;

    /**
     * @deprecated CommentManager cannot find the author if invoked remotely so
     *             one should use {@link #createQuote(DocumentModel, String, String)}
     */
    @Deprecated
    DocumentModel createQuote(DocumentModel docModel, String quote)
            throws ClientException;

    /**
     * Creates a comment document model, filling its properties with given info
     * and linking it to given document.
     *
     * @param docModel the document to comment
     * @param comment the comment content
     * @param author the comment author
     * @return the comment document model.
     * @throws ClientException
     */
    DocumentModel createQuote(DocumentModel docModel, String comment,
            String author) throws ClientException;

    DocumentModel createQuote(DocumentModel docModel, DocumentModel comment)
            throws ClientException;

    DocumentModel createQuote(DocumentModel docModel, DocumentModel parent,
            DocumentModel child) throws ClientException;

    void deleteQuote(DocumentModel docModel, DocumentModel comment)
            throws ClientException;

    /**
     * Gets documents in relation with a particular comment.
     *
     * @param quote the comment
     * @return the list of documents
     * @throws ClientException
     */
    List<DocumentModel> getDocumentsForQuote(DocumentModel quote)
            throws ClientException;

    /**
     * Creates a comment document model. It gives opportunity to save the comments in a
     * specified location.
     *
     * @param docModel the document to comment
     * @param quote the comment content
     * @param path the location path
     * @return the comment document model.
     * @throws ClientException
     */
    DocumentModel createLocatedQuote(DocumentModel docModel,
            DocumentModel quote, String path) throws ClientException;

}
