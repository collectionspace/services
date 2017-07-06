package org.collectionspace.services.IntegrationTests.test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests for sending and receiving JSON-formatted payloads.
 */
public class JsonIntegrationTest {
    public static final String HOST = "localhost";
    public static final int PORT = 8180;
    public static final String CLIENT_ID = "cspace-ui";
    public static final String CLIENT_SECRET = "";
    public static final String USERNAME = "admin@core.collectionspace.org";
    public static final String PASSWORD = "Administrator";
    public static final String BASE_URL = "http://" + HOST + ":" + PORT + "/cspace-services/";
    public static final String FILE_PATH = "test-data/json/";
    
    private Executor restExecutor = Executor.newInstance()
            .auth(new HttpHost(HOST, PORT), USERNAME, PASSWORD);

    private Executor authExecutor = Executor.newInstance()
            .auth(new HttpHost(HOST, PORT), CLIENT_ID, CLIENT_SECRET);
    
    private ObjectMapper mapper = new ObjectMapper();
    private JsonFactory jsonFactory = mapper.getFactory();

    @Test
    public void testRecord() throws ClientProtocolException, IOException {
        JsonNode jsonNode;
        
        String csid = postJson("collectionobjects", "collectionobject1");
        
        jsonNode = getJson("collectionobjects/" + csid);
        
        assertEquals(jsonNode.at("/document/ns2:collectionspace_core/createdBy").asText(), USERNAME);
        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/objectNumber").asText(), "TEST2000.4.5");
        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/objectNameList/objectNameGroup/objectName").asText(), "Test Object");
        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment/0").asText(), "line 1\nline 2");
        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment/1").asText(), "åéîøü");

        jsonNode = putJson("collectionobjects/" + csid, "collectionobject2");

        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/objectNumber").asText(), "TEST2000.4.5-updated");
        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment").asText(), "™£•");
        assertTrue(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment/0").isMissingNode());
        assertTrue(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment/1").isMissingNode());

        jsonNode = getJson("collectionobjects/" + csid);

        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/objectNumber").asText(), "TEST2000.4.5-updated");
        assertEquals(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment").asText(), "™£•");
        assertTrue(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment/0").isMissingNode());
        assertTrue(jsonNode.at("/document/ns2:collectionobjects_common/comments/comment/1").isMissingNode());

        delete("collectionobjects/" + csid);
    }
    
    @Test
    public void testAuth() throws ClientProtocolException, IOException {
        JsonNode jsonNode;
        
        jsonNode = postAuthForm("oauth/token", "grant_type=password&username=" + USERNAME + "&password=" + PASSWORD);

        assertEquals(jsonNode.at("/token_type").asText(), "bearer");
        assertTrue(StringUtils.isNotEmpty(jsonNode.at("/access_token").asText()));
    }
    
    private String postJson(String path, String filename) throws ClientProtocolException, IOException {
        return restExecutor.execute(Request.Post(getUrl(path))
            .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
            .addHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType())
            .bodyFile(getFile(filename), ContentType.APPLICATION_JSON))
            .handleResponse(new CsidFromLocationResponseHandler());
    }
    
    private JsonNode getJson(String path) throws ClientProtocolException, IOException {
        return restExecutor.execute(Request.Get(getUrl(path))
            .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType()))
            .handleResponse(new JsonBodyResponseHandler());
    }
    
    private JsonNode putJson(String path, String filename) throws ClientProtocolException, IOException {
        return restExecutor.execute(Request.Put(getUrl(path))
            .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
            .addHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType())
            .bodyFile(getFile(filename), ContentType.APPLICATION_JSON))
            .handleResponse(new JsonBodyResponseHandler());
    }
    
    private void delete(String path) throws ClientProtocolException, IOException {
        restExecutor.execute(Request.Delete(getUrl(path)))
            .handleResponse(new CheckStatusResponseHandler());
    }

    private JsonNode postAuthForm(String path, String values) throws ClientProtocolException, IOException {
        return authExecutor.execute(Request.Post(getUrl(path))
                .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
                .addHeader("Content-type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                .bodyString(values, ContentType.APPLICATION_FORM_URLENCODED))
                .handleResponse(new JsonBodyResponseHandler());
    }

    public class CsidFromLocationResponseHandler implements ResponseHandler<String> {

        @Override
        public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            
            if (statusCode< 200 || statusCode > 299) {
                throw new HttpResponseException(statusCode, status.getReasonPhrase());
            }
            
            return csidFromLocation(response.getFirstHeader("Location").getValue());
        }
    }
    
    public class JsonBodyResponseHandler implements ResponseHandler<JsonNode> {

        @Override
        public JsonNode handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            
            if (statusCode< 200 || statusCode > 299) {
                throw new HttpResponseException(statusCode, status.getReasonPhrase());
            }
            
            HttpEntity entity = response.getEntity();
            
            if (entity == null) {
                throw new ClientProtocolException("response contains no content");
            }

            ContentType contentType = ContentType.getOrDefault(entity);
            String mimeType = contentType.getMimeType();
            
            if (!mimeType.equals(ContentType.APPLICATION_JSON.getMimeType())) {
                throw new ClientProtocolException("unexpected content type: " + contentType);
            }

            return jsonFactory.createParser(entity.getContent()).readValueAsTree();
        }
    }

    public class CheckStatusResponseHandler implements ResponseHandler<Integer> {

        @Override
        public Integer handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            
            if (statusCode< 200 || statusCode > 299) {
                throw new HttpResponseException(statusCode, status.getReasonPhrase());
            }
            
            return statusCode;
        }
    }

    private String csidFromLocation(String location) {
        int index = location.lastIndexOf("/");
        
        return location.substring(index + 1);
    }
    
    private String getUrl(String path) {
        return BASE_URL + path;
    }
    
    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        
        return new File(classLoader.getResource(FILE_PATH + fileName + ".json").getFile());
    }
}
