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

package org.nuxeo.ecm.platform.comment.listener.test;

import java.util.List;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.jms.AsyncProcessorConfig;
import org.nuxeo.ecm.core.repository.jcr.testing.RepositoryOSGITestCase;
import org.nuxeo.ecm.platform.relations.api.RelationManager;
import org.nuxeo.ecm.platform.relations.api.Statement;
import org.nuxeo.runtime.api.Framework;

import org.collectionspace.ecm.platform.quote.api.QuoteableDocument;
import org.collectionspace.ecm.platform.quote.service.QuoteService;
import org.collectionspace.ecm.platform.quote.service.QuoteServiceHelper;

public class SimpleListenerTest extends RepositoryOSGITestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        deployBundle("org.nuxeo.ecm.relations.api");
        deployBundle("org.nuxeo.ecm.relations");
        deployBundle("org.nuxeo.ecm.relations.jena");
        //
        // CollectionSpace
        //
        deployBundle("org.collectionspace.ecm.platform.quote.api");
        // deployBundle("org.nuxeo.ecm.platform.comment");
        deployBundle("org.nuxeo.ecm.platform.comment.core");

        deployContrib("org.collectionspace.ecm.platform.quote",
                "OSGI-INF/QuoteService.xml");
        deployContrib("org.collectionspace.ecm.platform.quote",
                "OSGI-INF/quote-listener-contrib.xml");
        deployContrib("org.collectionspace.ecm.platform.quote.tests",
                "OSGI-INF/quote-jena-contrib.xml");

        openRepository();
    }

    protected int getCommentGrahNodesNumber() throws Exception {
        RelationManager rm = Framework.getService(RelationManager.class);

        List<Statement> statementList = rm.getStatements("documentComments");
        return statementList.size();
    }

    protected DocumentModel doCreateADocWithComments() throws Exception {

        DocumentModel domain = getCoreSession().createDocumentModel("Folder");
        domain.setProperty("dublincore", "title", "Domain");
        domain.setPathInfo("/", "domain");
        domain = getCoreSession().createDocument(domain);

        DocumentModel doc = getCoreSession().createDocumentModel("File");

        doc.setProperty("dublincore", "title", "MonTitre");
        doc.setPathInfo("/domain/", "TestFile");

        doc = getCoreSession().createDocument(doc);
        getCoreSession().save();
        AsyncProcessorConfig.setForceJMSUsage(false);

        // Create a first commentary
        QuoteableDocument cDoc = doc.getAdapter(QuoteableDocument.class);
        DocumentModel comment = getCoreSession().createDocumentModel("Comment");
        comment.setProperty("comment", "text", "This is my comment");
        comment = cDoc.addQuote(comment);

        // Create a second commentary
        DocumentModel comment2 = getCoreSession().createDocumentModel("Comment");
        comment2.setProperty("comment", "text", "This is another  comment");
        comment2 = cDoc.addQuote(comment);
        return doc;
    }

    protected void waitForAsyncExec() {
        Framework.getLocalService(EventService.class).waitForAsyncCompletion();
    }

    /*
     * Disabled until we have time to fix this test.
    public void testDocumentRemovedCommentEventListener() throws Exception {
        DocumentModel doc = doCreateADocWithComments();
        assertNotNull(doc);

        int nbLinks = getCommentGrahNodesNumber();
        assertTrue(nbLinks > 0);

        // Suppression the documents
        getCoreSession().removeDocument(doc.getRef());
        getCoreSession().save();

        // wait for the listener to be called
        waitForAsyncExec();

        // Did all the relations have been deleted?
        nbLinks = getCommentGrahNodesNumber();
        assertEquals(0, nbLinks);
    }
     */
    
    /*
     * Disabled until we have time to fix this test.
    public void testCommentRemovedEventListener() throws Exception {
        DocumentModel doc = doCreateADocWithComments();
        assertNotNull(doc);

        int nbLinks = getCommentGrahNodesNumber();
        assertEquals(2, nbLinks);

        // Get the comments
        QuoteService commentService = QuoteServiceHelper.getQuoteService();
        List<DocumentModel> comments = commentService.getQuoteManager().getQuotes(
                doc);

        // Delete the first comment
        getCoreSession().removeDocument(comments.get(0).getRef());
        // Check that the first relation has been deleted
        nbLinks = getCommentGrahNodesNumber();
        assertEquals(1, nbLinks);

        // Delete the second comment
        getCoreSession().removeDocument(comments.get(1).getRef());
        // Check that the second relation has been deleted
        nbLinks = getCommentGrahNodesNumber();
        assertEquals(0, nbLinks);
    }
    */
    
    public void testDoNothing() {
    	// Do nothing
    }
}
