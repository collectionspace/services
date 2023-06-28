package org.collectionspace.services.IntegrationTests.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.testng.Assert.*;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
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
    public static final String USERNAME = "admin@core.collectionspace.org";
    public static final String PASSWORD = "Administrator";
    public static final String BASIC_AUTH_CREDS = Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes());
    public static final String BASE_URL = "http://" + HOST + ":" + PORT + "/cspace-services/";
    public static final String FILE_PATH = "test-data/json/";

    private HttpHost host = new HttpHost(HOST, PORT);

    private Executor restExecutor = Executor.newInstance()
        .auth(host, USERNAME, PASSWORD)
        .authPreemptive(host);

    private Executor authExecutor = Executor.newInstance(
        // Don't follow redirects.

        HttpClientBuilder.create()
            .setRedirectStrategy(new RedirectStrategy() {
                @Override
                public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
                        throws ProtocolException {
                    return false;
                }

                @Override
                public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
                        throws ProtocolException {
                    return null;
                }
            })
            .build()
    );

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
        Pair<String, String> loginFormResult = getLoginForm("login");

        String sessionCookie = loginFormResult.getLeft();
        String csrfToken = loginFormResult.getRight();

        String loggedInSessionCookie = postLoginForm("login", "username=" + USERNAME + "&password=" + PASSWORD + "&_csrf=" + csrfToken, sessionCookie);
        String authCode = getAuthCode("oauth2/authorize", loggedInSessionCookie);
        JsonNode jsonNode = postTokenGrant("oauth2/token", authCode);

        assertEquals(jsonNode.at("/token_type").asText(), "Bearer");
        assertTrue(StringUtils.isNotEmpty(jsonNode.at("/access_token").asText()));
    }

    private String postJson(String path, String filename) throws ClientProtocolException, IOException {
        return restExecutor.execute(Request.Post(getUrl(path))
            .addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType())
            .addHeader("Content-type", ContentType.APPLICATION_JSON.getMimeType())
            .addHeader("Authorization", "Basic " + BASIC_AUTH_CREDS)
            .bodyFile(getFile(filename), ContentType.APPLICATION_JSON))
            .handleResponse(new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine status = response.getStatusLine();
                    int statusCode = status.getStatusCode();

                    if (statusCode < 200 || statusCode > 299) {
                        throw new HttpResponseException(statusCode, status.getReasonPhrase());
                    }

                    return csidFromLocation(response);
                }
            });
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
            .handleResponse(new ResponseHandler<Integer>() {
                @Override
                public Integer handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine status = response.getStatusLine();
                    int statusCode = status.getStatusCode();

                    if (statusCode < 200 || statusCode > 299) {
                        throw new HttpResponseException(statusCode, status.getReasonPhrase());
                    }

                    return statusCode;
                }
            });
    }

    private Pair<String, String> getLoginForm(String path) throws ClientProtocolException, IOException {
        return authExecutor.execute(Request.Get(getUrl(path)))
            .handleResponse(new ResponseHandler<Pair<String, String>>() {
                @Override
                public Pair<String, String> handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine status = response.getStatusLine();
                    int statusCode = status.getStatusCode();

                    if (statusCode < 200 || statusCode > 299) {
                        throw new HttpResponseException(statusCode, status.getReasonPhrase());
                    }

                    HttpEntity entity = response.getEntity();

                    if (entity == null) {
                        throw new ClientProtocolException("response contains no content");
                    }

                    ContentType contentType = ContentType.getOrDefault(entity);
                    String mimeType = contentType.getMimeType();

                    if (!mimeType.equals(ContentType.TEXT_HTML.getMimeType())) {
                        throw new ClientProtocolException("unexpected content type: " + contentType);
                    }

                    return Pair.of(
                        sessionCookie(response),
                        csrfFromLoginForm(IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8.name()))
                    );
                }
            });
    }

    private String postLoginForm(String path, String values, String sessionCookie) throws ClientProtocolException, IOException {
        return authExecutor.execute(Request.Post(getUrl(path))
            .addHeader("Cookie", "JSESSIONID=" + sessionCookie)
            .addHeader("Content-type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
            .bodyString(values, ContentType.APPLICATION_FORM_URLENCODED))
            .handleResponse(new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine status = response.getStatusLine();
                    int statusCode = status.getStatusCode();

                    if (statusCode != 302) {
                        throw new HttpResponseException(statusCode, status.getReasonPhrase());
                    }

                    return sessionCookie(response);
                }
            });
    }

    private String getAuthCode(String path, String sessionCookie) throws ClientProtocolException, IOException {
        String queryString = "response_type=code&client_id=" + CLIENT_ID + "&scope=cspace.full&redirect_uri=/../cspace/core/authorized&code_challenge=Ngi8oeROpsTSaOttsCJgJpiSwLQrhrvx53pvoWw8koI&code_challenge_method=S256";

        return authExecutor.execute(Request.Get(getUrl(path) + "?" + queryString)
            .addHeader("Cookie", "JSESSIONID=" + sessionCookie))
            .handleResponse(new ResponseHandler<String>() {
                @Override
                public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
                    StatusLine status = response.getStatusLine();
                    int statusCode = status.getStatusCode();

                    if (statusCode != 302) {
                        throw new HttpResponseException(statusCode, status.getReasonPhrase());
                    }

                    return authCodeFromLocation(response);
                }
            });
    }

    private JsonNode postTokenGrant(String path, String authCode) throws ClientProtocolException, IOException {
        return authExecutor.execute(Request.Post(getUrl(path))
            .addHeader("Content-type", ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
            .bodyString("grant_type=authorization_code&redirect_uri=/../cspace/core/authorized&client_id=" + CLIENT_ID + "&code_verifier=xyz&code=" + authCode, ContentType.APPLICATION_FORM_URLENCODED))
            .handleResponse(new JsonBodyResponseHandler());
    }

    public class JsonBodyResponseHandler implements ResponseHandler<JsonNode> {

        @Override
        public JsonNode handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();

            if (statusCode < 200 || statusCode > 299) {
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

    private String csrfFromLoginForm(String formHtml) {
        Pattern pattern = Pattern.compile("\"token\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(formHtml);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String sessionCookie(HttpResponse response) {
        String value = response.getFirstHeader("Set-Cookie").getValue();
        Pattern pattern = Pattern.compile("JSESSIONID=(.*?);");
        Matcher matcher = pattern.matcher(value);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String csidFromLocation(HttpResponse response) {
        String location = response.getFirstHeader("Location").getValue();
        int index = location.lastIndexOf("/");

        return location.substring(index + 1);
    }

    private String authCodeFromLocation(HttpResponse response) {
        String location = response.getFirstHeader("Location").getValue();
        int index = location.lastIndexOf("code=");

        return location.substring(index + 5);
    }

    private String getUrl(String path) {
        return BASE_URL + path;
    }

    private File getFile(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();

        return new File(classLoader.getResource(FILE_PATH + fileName + ".json").getFile());
    }
}
