package org.collectionspace.hello.services;

import org.collectionspace.hello.services.nuxeo.NuxeoRESTClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.collectionspace.hello.*;


import org.collectionspace.hello.People.PeopleItem;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.restlet.resource.Representation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/persons")
@Consumes("application/xml")
@Produces("application/xml")
public class PersonDocResource {

    final Logger logger = LoggerFactory.getLogger(PersonDocResource.class);

    public PersonDocResource() {
    }

    @GET
    public People getPeople(@Context UriInfo ui) {
        People p = new People();
        try {
            List<People.PeopleItem> list = p.getPeopleItem();
            NuxeoRESTClient nxClient = getClient();

            List<String> pathParams = new ArrayList<String>();
            Map<String, String> queryParams = new HashMap<String, String>();
            //browse default repository for People
            //For sanjay, People repository id is f084243e-4b81-42a1-9a05-518e974facbd
            pathParams = Arrays.asList("default", "f084243e-4b81-42a1-9a05-518e974facbd", "browse");
            Representation res = nxClient.get(pathParams, queryParams);
            SAXReader reader = new SAXReader();
            Document document = reader.read(res.getStream());
            Element root = document.getRootElement();
            for (Iterator i = root.elementIterator(); i.hasNext();) {
                Element element = (Element) i.next();
                PeopleItem pli = new PeopleItem();
                pli.setTitle(element.attributeValue("title"));
                pli.setUri(element.attributeValue("url"));
                pli.setId(element.attributeValue("id"));
                list.add(pli);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return p;
    }


//    private void getQueryModel() throws IOException {
//        NuxeoRESTClient nxClient = getClient();
//
//        List<String> pathParams = new ArrayList<String>();
//        Map<String, String> queryParams = new HashMap<String, String>();
//
//        //query model for user documents
//        pathParams = Arrays.asList("execQueryModel", "USER_DOCUMENTS");
//        queryParams.put("QP1", "Administrator");
//        queryParams.put("format", "XML");
//
//
//        Representation res = nxClient.get(pathParams, queryParams);
//        String resStr = res.getText();
//        verbose("getQueryModel:" + resStr);
//
//    }
//
//    private void getVocabulary() throws IOException {
//        NuxeoRESTClient nxClient = getClient();
//
//        List<String> pathParams = new ArrayList<String>();
//        Map<String, String> queryParams = new HashMap<String, String>();
//        //get vocabulary
//        pathParams = Arrays.asList("vocabulary", "continent_country");
//        queryParams.put("lang", "en");
//
//        Representation res = nxClient.get(pathParams, queryParams);
//        String resStr = res.getText();
//        verbose("getVocabulary:" + resStr);
//
//    }
    private NuxeoRESTClient getClient() {
        NuxeoRESTClient nxClient = new NuxeoRESTClient("http://127.0.0.1:8080/nuxeo");
        nxClient.setAuthType(NuxeoRESTClient.AUTH_TYPE_BASIC);
        nxClient.setBasicAuthentication("Administrator", "Administrator");
        return nxClient;
    }

    private void verbose(String msg) {
        System.out.println("PersonDocResource: " + msg);
    }
}
