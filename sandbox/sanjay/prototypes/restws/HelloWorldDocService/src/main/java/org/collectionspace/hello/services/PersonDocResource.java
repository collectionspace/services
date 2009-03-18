package org.collectionspace.hello.services;

import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.hello.Person;
import org.collectionspace.hello.Persons;

import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/persons")
@Consumes("application/xml")
@Produces("application/xml")
public class PersonDocResource {

    final Logger logger = LoggerFactory.getLogger(PersonDocResource.class);
    private Map<Long, Person> personDB = new ConcurrentHashMap<Long, Person>();
    private AtomicLong idCounter = new AtomicLong();

    public PersonDocResource() {
    }

    @GET
    public Persons getPersons(@Context UriInfo ui) {
        try {
            getRepository();
            getQueryModel();
            getVocabulary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Persons persons = new Persons();
        return persons;
    }

    private void getRepository() throws IOException {
        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        //browse default repository
        pathParams = Arrays.asList("default", "*", "browse");
        Representation res = nxClient.get(pathParams, queryParams);
        String resStr = res.getText(); 
        verbose("getRepository:" + resStr);
    }

    private void getQueryModel() throws IOException {
        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();

        //query model for user documents
        pathParams = Arrays.asList("execQueryModel", "USER_DOCUMENTS");
        queryParams.put("QP1", "Administrator");
        queryParams.put("format", "XML");


        Representation res = nxClient.get(pathParams, queryParams);
        String resStr = res.getText();
        verbose("getQueryModel:" + resStr);

    }

    private void getVocabulary() throws IOException {
        NuxeoRESTClient nxClient = getClient();

        List<String> pathParams = new ArrayList<String>();
        Map<String, String> queryParams = new HashMap<String, String>();
        //get vocabulary
        pathParams = Arrays.asList("vocabulary", "continent_country");
        queryParams.put("lang", "en");

        Representation res = nxClient.get(pathParams, queryParams);
        String resStr = res.getText();
        verbose("getVocabulary:" + resStr);

    }

    private NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient("http://127.0.0.1:8080/nuxeo");
        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");
        return nxClient;
    }

//        @GET
//    public Persons getPersons(@Context UriInfo ui) {
//        String repoName = "default";// = (String) req.getAttributes().get("repoName");
//        String docid = "";// = (String) req.getAttributes().get("docid");
//
//        //fixme: how the heck navigationContext is initialized
////        NavigationContext navigationContext = null;
//        CoreSession session = null;
//
//        DOMDocumentFactory domfactory = new DOMDocumentFactory();
//        DOMDocument result = (DOMDocument) domfactory.createDocument();
//        // Element root = result.createElement("browse");
//        // result.setRootElement((org.dom4j.Element) root);
//
////        if (repoName == null || repoName.equals("*")) {
//        try {
//            getQueryModel();
////            login();
//
//        // Connect to default repoName
////        NuxeoClient.getInstance().connect("localhost", 62474);
////        session = Framework.getService(RepositoryManager.class).getDefaultRepository().open();
//
////            RepositoryManager rm = getRepositoryManager();
////            String repoURI = rm.getRepository(repoName).getRepositoryUri();
////            Repository repo = rm.getRepository(repoName);
////            if (repoURI == null) {
////                repoURI = repo.getName();
////            }
////
////            session = getSession();
////            String sid = session.connect(repoURI, new HashMap<String, Serializable>());
////
////            // get the root
////            DocumentModel root = session.getRootDocument();
////            System.out.print(root.getRef());
////
////            // get workspace root (expect default repoName layout)
////            DocumentModel ws = session.getDocument(new PathRef("/default-domain/workspaces"));
////            String title = ws.getTitle();
////
////
//
////                RepositoryManager repmanager = Framework.getService(RepositoryManager.class);
////                Collection<Repository> repos = repmanager.getRepositories();
////
////                Element serversNode = result.createElement("availableServers");
////                result.setRootElement((org.dom4j.Element) serversNode);
////
////                for (Repository availableRepo : repos) {
////                    Element server = result.createElement("server");
////                    server.setAttribute("title", availableRepo.getName());
////                    server.setAttribute("url", getRelURL(availableRepo.getName(), "*"));
////                    serversNode.appendChild(server);
////                }
//        } catch (Exception e) {
////                handleError(result, res, e);
////                return;
//            e.printStackTrace();
//        }
////        } else {
////            DocumentModel dm = null;
////            try {
////                //how t
////                navigationContext.setCurrentServerLocation(new RepositoryLocation(
////                        repoName));
////                session = navigationContext.getOrCreateDocumentManager();
////                if (docid == null || docid.equals("*")) {
////                    dm = session.getRootDocument();
////                } else {
////                    dm = session.getDocument(new IdRef(docid));
////                }
////            } catch (ClientException e) {
////                //handleError(result, res, e);
////                //return;
////                e.printStackTrace();
////            }
////
////            Element current = result.createElement("document");
////            try {
////                current.setAttribute("title", dm.getTitle());
////            } catch (DOMException e1) {
////                //handleError(res, e1);
////                e1.printStackTrace();
////            } catch (ClientException e1) {
////                //handleError(res, e1);
////                e1.printStackTrace();
////            }
////            current.setAttribute("type", dm.getType());
////            current.setAttribute("id", dm.getId());
////            current.setAttribute("url", getRelURL(repoName, dm.getRef().toString()));
////            result.setRootElement((org.dom4j.Element) current);
////
////            if (dm.isFolder()) {
////                // Element childrenElem = result.createElement("children");
////                // root.appendChild(childrenElem);
////
////                DocumentModelList children = null;
////                try {
////                    children = session.getChildren(dm.getRef());
////                } catch (ClientException e) {
////                    //handleError(result, res, e);
////                    //return;
////                    e.printStackTrace();
////                }
////
////                for (DocumentModel child : children) {
////                    Element el = result.createElement("document");
////                    try {
////                        el.setAttribute("title", child.getTitle());
////                    } catch (DOMException e) {
////                        //handleError(res, e);
////                        e.printStackTrace();
////                    } catch (ClientException e) {
////                        ///handleError(res, e);
////                        e.printStackTrace();
////                    }
////                    el.setAttribute("type", child.getType());
////                    el.setAttribute("id", child.getId());
////                    el.setAttribute("url", getRelURL(repoName, child.getRef().toString()));
////                    current.appendChild(el);
////                }
////            }
////        }
//        Persons persons = new Persons();
//        return persons;
//    }
//    private void login() throws LoginException {
//        CallbackHandler handler = new NuxeoCallbackHandler("Administrator",
//                "Administrator");
//        LoginContext lc = NuxeoLoginContextFactory.getLoginContext(handler);
//        try {
//            lc.login();
//        } catch (LoginException le) {
//            System.out.print("Unable to login :" + le);
//        }
//    }
//
//    private static RepositoryManager getRepositoryManager()
//            throws NamingException {
//        String beanRemoteLocation = "nuxeo/RepositoryManagerBean/remote";
//        javax.naming.Context ctx = getInitialContext();
//        Object proxy = ctx.lookup(beanRemoteLocation);
//        return (RepositoryManager) proxy;
//    }
//
//    private static CoreSession getSession()
//            throws Exception {
//
//        String beanRemoteLocation = "nuxeo/DocumentManagerBean/remote";
//        javax.naming.Context ctx = getInitialContext();
//        Object proxy = ctx.lookup(beanRemoteLocation);
//        return (CoreSession) proxy;
//
//    }
//
//    private static javax.naming.Context getInitialContext() throws NamingException {
////        Hashtable env = new Hashtable();
////        env.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
////        env.put(javax.naming.Context.PROVIDER_URL, "jnp://localhost:1099");
////        env.put(javax.naming.Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
//        return new InitialContext();
//
//    }
//
//    private static String getRelURL(String repo, String uuid) {
//        return '/' + repo + '/' + uuid;
//    }
    private void verbose(String msg) {
        System.out.println("PersonDocResource: " + msg);
    }
}
