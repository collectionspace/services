/*
 * (C) Copyright 2009 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     mcedica
 */
package org.collectionspace.ecm.platform.quote.workflow.services;

import java.util.ArrayList;

import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

public interface QuotesModerationService {

    /**
     * Starts the moderation process on given Comment posted on a documentModel.
     *
     * @param session the coreSession
     * @param document the document were the comment is posted
     * @param commentId the commentId
     * @throws NuxeoException
     */
    void startModeration(CoreSession session, DocumentModel document,
            String commentId, ArrayList<String> moderators)
            throws NuxeoException;

    /**
     * Gets the moderation process for the given Comment posted on a documentModel.
     *
     * @param session the coreSession
     * @param document the document were the comment is posted
     * @param commentId the commentId
     * @throws NuxeoException
     */
    /**
    ProcessInstance getModerationProcess(JbpmService jbpmService,
            CoreSessionInterface session, DocumentModel doc, String commentId)
            throws NuxeoException;
            **/

    /**
     * Approve the comment with the given commentId.
     *
     * @param session the coreSession
     * @param document the document were the comment is posted
     * @param commentId the commentId
     * @throws NuxeoException
     */
    void approveQuote(CoreSession session, DocumentModel document,
            String commentId) throws NuxeoException;

    /**
     * Reject the comment with the given commentId.
     *
     * @param session the coreSession
     * @param document the document were the comment is posted
     * @param commentId the commentId
     * @throws NuxeoException
     */
    void rejectQuote(CoreSession session, DocumentModel document,
            String commentId) throws NuxeoException;

    /**
     * Publish the given comment.
     *
     * @param session the coreSession
     * @param comment the comment to publish
     * @throws NuxeoException
     */
    void publishQuote(CoreSession session, DocumentModel comment)
            throws NuxeoException;

}
