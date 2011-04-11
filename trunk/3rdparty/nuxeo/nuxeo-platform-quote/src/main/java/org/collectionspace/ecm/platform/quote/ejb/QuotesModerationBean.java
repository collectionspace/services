/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

import java.util.ArrayList;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.runtime.api.Framework;

import org.collectionspace.ecm.platform.quote.workflow.services.QuotesModerationService;

@Stateless
@Remote(QuotesModerationService.class)
@Local(QuotesModerationService.class)
public class QuotesModerationBean implements QuotesModerationService {


    protected QuotesModerationService getQuotesModerationService() {
        return Framework.getLocalService(QuotesModerationService.class);
    }


    public void approveQuote(CoreSession session, DocumentModel document,
            String commentID) throws ClientException {
        getQuotesModerationService().approveQuote(session, document, commentID);
    }

    public void publishQuote(CoreSession session, DocumentModel comment)
            throws ClientException {
        getQuotesModerationService().publishQuote(session, comment);

    }

    public void rejectQuote(CoreSession session, DocumentModel document,
            String commentID) throws ClientException {
        getQuotesModerationService().rejectQuote(session, document, commentID);

    }

    public void startModeration(CoreSession session, DocumentModel document,
            String commentID, ArrayList<String> moderators)
            throws ClientException {
        getQuotesModerationService().startModeration(session, document, commentID, moderators);

    }

}
