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

package org.collectionspace.ecm.platform.quote.listener;

import java.util.List;

import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.platform.relations.api.RelationManager;
import org.nuxeo.ecm.platform.relations.api.Resource;
import org.nuxeo.ecm.platform.relations.api.Statement;
import org.nuxeo.ecm.platform.relations.api.impl.StatementImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.ecm.platform.quote.service.QuoteServiceConfig;

public class QuoteRemovedEventListener extends AbstractQuoteListener
        implements EventListener {

    private static final Logger logger = LoggerFactory.getLogger(QuoteRemovedEventListener.class);

    @Override
    protected void doProcess(CoreSession coreSession,
            RelationManager relationManager, QuoteServiceConfig config,
            DocumentModel docMessage) throws Exception {
        log.debug("Processing relations cleanup on Comment removal");
        String typeName = docMessage.getType();
        if ("Comment".equals(typeName) || "Post".equals(typeName)) {
            onQuoteRemoved(relationManager, config, docMessage);
        }
    }

    private static void onQuoteRemoved(RelationManager relationManager,
            QuoteServiceConfig config, DocumentModel docModel)
            throws ClientException {
        Resource quoteRes = relationManager.getResource(
                config.commentNamespace, docModel, null);
        if (quoteRes == null) {
            log.warn("Could not adapt document model to relation resource; "
                    + "check the service relation adapters configuration");
            return;
        }
        Statement pattern = new StatementImpl(quoteRes, null, null);
        List<Statement> statementList = relationManager.getStatements(
                config.graphName, pattern);
        relationManager.remove(config.graphName, statementList);
    }

}
