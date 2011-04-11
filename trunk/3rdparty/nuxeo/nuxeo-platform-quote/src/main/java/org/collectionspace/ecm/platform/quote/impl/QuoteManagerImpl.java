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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.ClientRuntimeException;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.api.repository.RepositoryManager;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.SecurityConstants;
import org.nuxeo.ecm.core.api.security.impl.ACLImpl;
import org.nuxeo.ecm.core.api.security.impl.ACPImpl;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventProducer;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.relations.api.RelationManager;
import org.nuxeo.ecm.platform.relations.api.Resource;
import org.nuxeo.ecm.platform.relations.api.ResourceAdapter;
import org.nuxeo.ecm.platform.relations.api.Statement;
import org.nuxeo.ecm.platform.relations.api.impl.QNameResourceImpl;
import org.nuxeo.ecm.platform.relations.api.impl.ResourceImpl;
import org.nuxeo.ecm.platform.relations.api.impl.StatementImpl;
import org.nuxeo.ecm.platform.usermanager.UserManager;
import org.nuxeo.runtime.api.Framework;

import org.collectionspace.ecm.platform.quote.api.QuoteConstants;
import org.collectionspace.ecm.platform.quote.api.QuoteConverter;
import org.collectionspace.ecm.platform.quote.api.QuoteEvents;
import org.collectionspace.ecm.platform.quote.api.QuoteManager;
import org.collectionspace.ecm.platform.quote.service.QuoteServiceConfig;

/**
 * @author <a href="mailto:glefter@nuxeo.com">George Lefter</a>
 *
 */
public class QuoteManagerImpl implements QuoteManager {

    private static final Log log = LogFactory.getLog(QuoteManagerImpl.class);

    final SimpleDateFormat timeFormat = new SimpleDateFormat("dd-HHmmss.S");

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");

    final QuoteServiceConfig config;

    final QuoteConverter quoteConverter;

    public static final  String COMMENTS_DIRECTORY = "Comments";

    public QuoteManagerImpl(QuoteServiceConfig config) {
        if (config == null) {
        	config = new QuoteServiceConfig();
        	config.quoteConverterClassName = QuoteConverterImpl.class.getName();
        	config.graphName = "documentQuotes";
        	config.commentNamespace = "http://www.collectionspace.org/quotes/uid";
        	config.documentNamespace = "http://www.collectionspace.org/document/uid";
        	config.predicateNamespace = "http://www.nuxeo.org/predicates/isQuoteFor";
        }
        this.config = config;
       	quoteConverter = config.getQuoteConverter();
    }

    protected CoreSession openCoreSession(String repositoryName)
            throws ClientException {
        try {
            RepositoryManager repoMgr = Framework.getService(RepositoryManager.class);
            return repoMgr.getRepository(repositoryName).open();
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    protected void closeCoreSession(LoginContext loginContext,
            CoreSession session) throws ClientException {
        if (loginContext != null) {
            try {
                loginContext.logout();
            } catch (LoginException e) {
                throw new ClientException(e);
            }
        }
        if (session != null) {
            CoreInstance.getInstance().close(session);
        }
    }

    private static RelationManager getRelationManager() throws Exception {
        return Framework.getService(RelationManager.class);
    }

    public List<DocumentModel> getQuotes(DocumentModel docModel)
            throws ClientException {
        RelationManager relationManager;
        Map<String, Serializable> ctxMap = new HashMap<String, Serializable>();
        ctxMap.put(ResourceAdapter.CORE_SESSION_ID_CONTEXT_KEY, docModel.getSessionId());
        try {
            relationManager = getRelationManager();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        Resource docResource = relationManager.getResource(
                config.documentNamespace, docModel, ctxMap);
        if (docResource == null) {
            throw new ClientException(
                    "Could not adapt document model to relation resource ; "
                            + "check the service relation adapters configuration");
        }

        // FIXME AT: why no filter on the predicate?
        Statement pattern = new StatementImpl(null, null, docResource);
        List<Statement> statementList = relationManager.getStatements(
                config.graphName, pattern);
        // XXX AT: BBB for when repository name was not included in the resource
        // uri
        Resource oldDocResource = new QNameResourceImpl(
                config.documentNamespace, docModel.getId());
        Statement oldPattern = new StatementImpl(null, null, oldDocResource);
        statementList.addAll(relationManager.getStatements(config.graphName,
                oldPattern));

        List<DocumentModel> commentList = new ArrayList<DocumentModel>();
        for (Statement stmt : statementList) {
            QNameResourceImpl subject = (QNameResourceImpl) stmt.getSubject();

            DocumentModel commentDocModel = null;
            try {
                commentDocModel = (DocumentModel) relationManager.getResourceRepresentation(
                        config.commentNamespace, subject, ctxMap);
            } catch (Exception e) {
                log.error("failed to retrieve commentDocModel from relations");
            }
            if (commentDocModel == null) {
                // XXX AT: maybe user cannot see the comment
                log.warn("Could not adapt comment relation subject to a document "
                        + "model; check the service relation adapters configuration");
                continue;
            }
            commentList.add(commentDocModel);
        }

        QuoteSorter sorter = new QuoteSorter(true);
        Collections.sort(commentList, sorter);

        return commentList;
    }

    public DocumentModel createQuote(DocumentModel docModel, String comment,
            String author) throws ClientException {
        LoginContext loginContext = null;
        CoreSession session = null;
        try {
            loginContext = Framework.login();
            session = openCoreSession(docModel.getRepositoryName());

            DocumentModel commentDM = session.createDocumentModel("Comment");
            commentDM.setProperty("comment", "text", comment);
            commentDM.setProperty("comment", "author", author);
            commentDM.setProperty("comment", "creationDate",
                    Calendar.getInstance());
            commentDM = internalCreateQuote(session, docModel, commentDM, null);
            session.save();

            return commentDM;
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            closeCoreSession(loginContext, session);
        }
    }

    public DocumentModel createQuote(DocumentModel docModel, String quote)
            throws ClientException {
        String author = getCurrentUser(docModel);
        return createQuote(docModel, quote, author);
    }

    /**
     * If the author property on comment is not set, retrieve the author name
     * from the session
     *
     * @param docModel The document model that holds the session id
     * @param comment The comment to update
     * @throws ClientException
     */
    private static String updateAuthor(DocumentModel docModel,
            DocumentModel comment) throws ClientException {
        // update the author if not set
        String author = (String) comment.getProperty("comment", "author");
        if (author == null) {
            log.debug("deprecated use of createComment: the client should set the author property on document");
            author = getCurrentUser(docModel);
            comment.setProperty("comment", "author", author);
        }
        return author;
    }

    public DocumentModel createQuote(DocumentModel docModel,
            DocumentModel comment) throws ClientException {
        LoginContext loginContext = null;
        CoreSession session = null;
        try {
            loginContext = Framework.login();
            session = openCoreSession(docModel.getRepositoryName());
            DocumentModel doc = internalCreateQuote(session, docModel, comment, null);
            session.save();
            return doc;
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            closeCoreSession(loginContext, session);
        }
    }

    protected DocumentModel internalCreateQuote(CoreSession session,
            DocumentModel docModel, DocumentModel comment, String path)
            throws ClientException {
        String author = updateAuthor(docModel, comment);
        DocumentModel createdComment;

        try {
            createdComment = createQuoteDocModel(session, docModel, comment, path);

            RelationManager relationManager = getRelationManager();
            List<Statement> statementList = new ArrayList<Statement>();

            Resource commentRes = relationManager.getResource(
                    config.commentNamespace, createdComment, null);

            Resource documentRes = relationManager.getResource(
                    config.documentNamespace, docModel, null);

            if (commentRes == null || documentRes == null) {
                throw new ClientException(
                        "Could not adapt document model to relation resource ; "
                                + "check the service relation adapters configuration");
            }

            Resource predicateRes = new ResourceImpl(config.predicateNamespace);

            Statement stmt = new StatementImpl(commentRes, predicateRes,
                    documentRes);
            statementList.add(stmt);
            relationManager.add(config.graphName, statementList);
        } catch (Exception e) {
            throw new ClientException("failed to create comment", e);
        }

        NuxeoPrincipal principal = null;
        try {
            UserManager userManager = Framework.getService(UserManager.class);
            principal = userManager.getPrincipal(author);
        } catch (Exception e) {
            log.error("Error building principal for notification", e);
        }
        notifyEvent(session, docModel, QuoteEvents.COMMENT_ADDED, null,
                createdComment, principal);

        return createdComment;
    }

    private DocumentModel createQuoteDocModel(CoreSession mySession,
            DocumentModel docModel, DocumentModel comment, String path)
            throws ClientException {

        String domainPath;
        updateAuthor(docModel, comment);

        String[] pathList = getQuotePathList(comment);

        if (path == null) {
            domainPath = docModel.getPath().segment(0);
        } else {
            domainPath = path;
        }
        if (mySession == null) {
            return null;
        }

        // TODO GR upgrade this code. It can't work if current user
        // doesn't have admin rights

        DocumentModel parent = mySession.getDocument(new PathRef(domainPath));
        for (String name : pathList) {
            boolean found = false;
            String pathStr = parent.getPathAsString();
            if (name.equals(COMMENTS_DIRECTORY)) {
                List<DocumentModel> children = mySession.getChildren(new PathRef(pathStr),
                        "HiddenFolder");
                for (DocumentModel documentModel : children) {
                    if (documentModel.getTitle().equals(COMMENTS_DIRECTORY)) {
                        found = true;
                        parent = documentModel;
                        break;
                    }
                }
            } else {
                DocumentRef ref = new PathRef(pathStr, name);
                if (mySession.exists(ref)) {
                    parent = mySession.getDocument(ref);
                    found = true;
                }

            }
            if (!found) {
                DocumentModel dm = mySession.createDocumentModel(pathStr, name,
                        "HiddenFolder");
                dm.setProperty("dublincore", "title", name);
                dm.setProperty("dublincore", "description", "");
                dm.setProperty("dublincore", "created", Calendar.getInstance());
                dm = mySession.createDocument(dm);
                setFolderPermissions(dm);
                parent = dm;
            }
        }

        String pathStr = parent.getPathAsString();
        String commentName = getQuoteName(docModel, comment);
        QuoteConverter converter = config.getQuoteConverter();
        DocumentModel commentDocModel = mySession.createDocumentModel(pathStr,
                IdUtils.generateId(commentName), comment.getType());
        converter.updateDocumentModel(commentDocModel, comment);
        commentDocModel.setProperty("dublincore", "title", commentName);
        commentDocModel = mySession.createDocument(commentDocModel);
        setQuotePermissions(commentDocModel);
        log.debug("created comment with id=" + commentDocModel.getId());

        return commentDocModel;
    }

    private static void notifyEvent(CoreSession session, DocumentModel docModel, String eventType,
            DocumentModel parent, DocumentModel child, NuxeoPrincipal principal)
            throws ClientException {

        DocumentEventContext ctx = new DocumentEventContext(session, principal, docModel);
        Map<String, Serializable> props = new HashMap<String, Serializable>();
        if (parent != null) {
            props.put(QuoteConstants.PARENT_COMMENT, parent);
        }
        props.put(QuoteConstants.COMMENT, child);
        props.put(QuoteConstants.COMMENT_TEXT, (String) child.getProperty(
                "comment", "text"));
        props.put("category", QuoteConstants.EVENT_COMMENT_CATEGORY);
        ctx.setProperties(props);
        Event event = ctx.newEvent(eventType);

        try {
            EventProducer evtProducer = Framework.getService(EventProducer.class);
            evtProducer.fireEvent(event);
        } catch (Exception e) {
            log.error("Error while send message", e);
        }
        // send also a synchronous Seam message so the CommentManagerActionBean
        // can rebuild its list
        // Events.instance().raiseEvent(eventType, docModel);
    }

    private static void setFolderPermissions(DocumentModel dm) {
        ACP acp = new ACPImpl();
        ACE grantAddChildren = new ACE("members",
                SecurityConstants.ADD_CHILDREN, true);
        ACE grantRemoveChildren = new ACE("members",
                SecurityConstants.REMOVE_CHILDREN, true);
        ACE grantRemove = new ACE("members", SecurityConstants.REMOVE, true);
        ACL acl = new ACLImpl();
        acl.setACEs(new ACE[] { grantAddChildren, grantRemoveChildren,
                grantRemove });
        acp.addACL(acl);
        try {
            dm.setACP(acp, true);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private static void setQuotePermissions(DocumentModel dm) {
        ACP acp = new ACPImpl();
        ACE grantRead = new ACE(SecurityConstants.EVERYONE,
                SecurityConstants.READ, true);
        ACE grantRemove = new ACE("members", SecurityConstants.REMOVE, true);
        ACL acl = new ACLImpl();
        acl.setACEs(new ACE[] { grantRead, grantRemove });
        acp.addACL(acl);
        try {
            dm.setACP(acp, true);
        } catch (ClientException e) {
            throw new ClientRuntimeException(e);
        }
    }

    private String[] getQuotePathList(DocumentModel comment) {
        String[] pathList = new String[2];
        pathList[0] = COMMENTS_DIRECTORY;

        pathList[1] = dateFormat.format(getQuoteTimeStamp(comment));
        return pathList;
    }

    private static CoreSession getUserSession(String sid) {
        return CoreInstance.getInstance().getSession(sid);
    }

    /**
     * @deprecated if the caller is remote, we cannot obtain the session
     */
    @Deprecated
    private static String getCurrentUser(DocumentModel target)
            throws ClientException {
        String sid = target.getSessionId();
        CoreSession userSession = getUserSession(sid);
        if (userSession == null) {
            throw new ClientException(
                    "userSession is null, do not invoke this method when the user is not local");
        }
        return userSession.getPrincipal().getName();
    }

    private String getQuoteName(DocumentModel target, DocumentModel comment)
            throws ClientException {
        String author = (String) comment.getProperty("comment", "author");
        if (author == null) {
            author = getCurrentUser(target);
        }
        Date creationDate = getQuoteTimeStamp(comment);
        return "COMMENT-" + author + '-'
                + timeFormat.format(creationDate.getTime());
    }

    private static Date getQuoteTimeStamp(DocumentModel comment) {
        Calendar creationDate;
        try {
            creationDate = (Calendar) comment.getProperty("dublincore",
                    "created");
        } catch (ClientException e) {
            creationDate = null;
        }
        if (creationDate == null) {
            creationDate = Calendar.getInstance();
        }
        return creationDate.getTime();
    }

    public void deleteQuote(DocumentModel docModel, DocumentModel comment)
            throws ClientException {
        LoginContext loginContext = null;
        CoreSession session = null;
        try {
            loginContext = Framework.login();
            session = openCoreSession(docModel.getRepositoryName());

            if (session == null) {
                throw new ClientException(
                        "Unable to acess repository for comment: "
                                + comment.getId());
            }
            DocumentRef ref = comment.getRef();
            if (!session.exists(ref)) {
                throw new ClientException("Comment Document does not exist: "
                        + comment.getId());
            }

            NuxeoPrincipal author = getAuthor(comment);
            session.removeDocument(ref);

            notifyEvent(session, docModel, QuoteEvents.COMMENT_REMOVED, null, comment,
                    author);

            session.save();

        } catch (Throwable e) {
            log.error("failed to delete comment", e);
            throw new ClientException("failed to delete comment", e);
        } finally {
            closeCoreSession(loginContext, session);
        }
    }

    public DocumentModel createQuote(DocumentModel docModel,
            DocumentModel parent, DocumentModel child) throws ClientException {
        LoginContext loginContext = null;
        CoreSession session = null;
        try {
            loginContext = Framework.login();
            session = openCoreSession(docModel.getRepositoryName());

            String author = updateAuthor(docModel, child);
            DocumentModel parentDocModel = session.getDocument(parent.getRef());
            DocumentModel newComment = internalCreateQuote(session, parentDocModel,
                    child, null);

            UserManager userManager = Framework.getService(UserManager.class);
            NuxeoPrincipal principal = userManager.getPrincipal(author);
            notifyEvent(session, docModel, QuoteEvents.COMMENT_ADDED, parent,
                    newComment, principal);

            session.save();
            return newComment;

        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            closeCoreSession(loginContext, session);
        }
    }

    private static NuxeoPrincipal getAuthor(DocumentModel docModel) {
        try {
            String[] contributors = (String[]) docModel.getProperty(
                    "dublincore", "contributors");
            UserManager userManager = Framework.getService(UserManager.class);
            return userManager.getPrincipal(contributors[0]);
        } catch (Exception e) {
            log.error("Error building principal for comment author", e);
            return null;
        }
    }

    public List<DocumentModel> getQuotes(DocumentModel docModel,
            DocumentModel parent) throws ClientException {
        try {
            //loginContext = Framework.login();
            //session = openCoreSession(docModel.getRepositoryName());
            //DocumentModel parentDocModel = session.getDocument(parent.getRef());
            return getQuotes(parent);
        } catch (Exception e) {
            throw new ClientException(e);
        }
    }

    public List<DocumentModel> getDocumentsForQuote(DocumentModel comment)
            throws ClientException {
        RelationManager relationManager;
        Map<String, Serializable> ctxMap = new HashMap<String, Serializable>();
        ctxMap.put(ResourceAdapter.CORE_SESSION_ID_CONTEXT_KEY, comment.getSessionId());
        try {
            relationManager = getRelationManager();
        } catch (Exception e) {
            throw new ClientException(e);
        }
        Resource commentResource = relationManager.getResource(
                config.commentNamespace, comment, ctxMap);
        if (commentResource == null) {
            throw new ClientException(
                    "Could not adapt document model to relation resource ; "
                            + "check the service relation adapters configuration");
        }
        Resource predicate = new ResourceImpl(config.predicateNamespace);
        Statement pattern = new StatementImpl(commentResource, predicate, null);

        List<Statement> statementList = relationManager.getStatements(
                config.graphName, pattern);
        // XXX AT: BBB for when repository name was not included in the resource
        // uri
        Resource oldDocResource = new QNameResourceImpl(
                config.commentNamespace, comment.getId());
        Statement oldPattern = new StatementImpl(oldDocResource, predicate, null);
        statementList.addAll(relationManager.getStatements(config.graphName,
                oldPattern));

        List<DocumentModel> docList = new ArrayList<DocumentModel>();
        for (Statement stmt : statementList) {
            QNameResourceImpl subject = (QNameResourceImpl) stmt.getObject();
            DocumentModel docModel = null;
            try {
                docModel = (DocumentModel) relationManager.getResourceRepresentation(
                        config.documentNamespace, subject, ctxMap);
            } catch (Exception e) {
                log.error("failed to retrieve documents from relations");
            }
            if (docModel == null) {
                log.warn("Could not adapt comment relation subject to a document "
                        + "model; check the service relation adapters configuration");
                continue;
            }
            docList.add(docModel);
        }
        return docList;

    }
    public DocumentModel createLocatedQuote(DocumentModel docModel,
            DocumentModel comment, String path) throws ClientException {
        LoginContext loginContext = null;
        CoreSession session = null;
        DocumentModel createdComment;
        try {
            loginContext = Framework.login();
            session = openCoreSession(docModel.getRepositoryName());
            createdComment = internalCreateQuote(session, docModel, comment, path);
            session.save();
        } catch (Exception e) {
            throw new ClientException(e);
        } finally {
            closeCoreSession(loginContext, session);
        }

        return createdComment;
    }

}
