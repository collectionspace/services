package org.collectionspace.services.IntegrationTests.test;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Integration tests for the GET /health endpoint. The endpoint is unauthenticated
 * and returns JSON with overall status and component checks (database, nuxeo, elasticsearch).
 * Requires a running CollectionSpace services server at {@link #BASE_URL}. If the server
 * is not reachable, tests are skipped (e.g. when running in CI without a deployed server).
 */
public class HealthEndpointIntegrationTest {

    public static final String HOST = "localhost";
    public static final int PORT = 8180;
    public static final String BASE_URL = "http://" + HOST + ":" + PORT + "/cspace-services/";
    public static final String HEALTH_PATH = "health";

    private String getUrl(String path) {
        return BASE_URL + path;
    }

    /**
     * Performs GET on the health endpoint. Skips the test if the server is not reachable
     * (Connection refused), so that mvn test can pass when no server is running.
     */
    private HttpResponse getHealthResponse() throws IOException {
        try {
            return Request.Get(getUrl(HEALTH_PATH))
                .addHeader("Accept", "application/json")
                .execute()
                .returnResponse();
        } catch (HttpHostConnectException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ConnectException && "Connection refused".equals(cause.getMessage())) {
                throw new SkipException(
                    "CollectionSpace server not running at " + BASE_URL + " - skip integration test");
            }
            throw e;
        }
    }

    @Test
    public void testHealthReturnsOkOrServiceUnavailable() throws IOException {
        HttpResponse response = getHealthResponse();
        int status = response.getStatusLine().getStatusCode();
        Assert.assertTrue(status == 200 || status == 503,
            "Expected 200 or 503, got " + status);
        String body = EntityUtils.toString(response.getEntity());
        JsonNode root = new ObjectMapper().readTree(body);
        Assert.assertTrue(root.has("status"), "Response should have 'status' field");
        String statusVal = root.get("status").asText();
        Assert.assertTrue(
            "pass".equals(statusVal) || "warn".equals(statusVal) || "fail".equals(statusVal),
            "status should be pass, warn, or fail; got: " + statusVal);
    }

    @Test
    public void testHealthResponseStructure() throws IOException {
        HttpResponse response = getHealthResponse();
        int status = response.getStatusLine().getStatusCode();
        Assert.assertTrue(status == 200 || status == 503,
            "Expected 200 or 503, got " + status);
        String body = EntityUtils.toString(response.getEntity());
        JsonNode root = new ObjectMapper().readTree(body);
        Assert.assertTrue(root.has("status"), "Response should have 'status' field");
        Assert.assertTrue(root.has("checks"), "Response should have 'checks' object");
        JsonNode checks = root.get("checks");
        Assert.assertTrue(checks.isObject(), "checks should be an object");

        Assert.assertTrue(checks.has("database:cspace"),
            "checks should contain 'database:cspace'");
        assertValidCheckStatus(checks.get("database:cspace").get("status").asText(), "database:cspace");

        Assert.assertTrue(checks.has("database:csadmin"),
            "checks should contain 'database:csadmin'");
        assertValidCheckStatus(checks.get("database:csadmin").get("status").asText(), "database:csadmin");

        // database:nuxeo: either single "database:nuxeo" (warn when no tenants) or "database:nuxeo:<repoName>" per repo
        boolean hasNuxeoCheck = checks.has("database:nuxeo");
        if (!hasNuxeoCheck) {
            Iterator<String> keys = checks.fieldNames();
            while (keys.hasNext()) {
                if (keys.next().startsWith("database:nuxeo:")) {
                    hasNuxeoCheck = true;
                    break;
                }
            }
        }
        Assert.assertTrue(hasNuxeoCheck,
            "checks should contain 'database:nuxeo' or at least one 'database:nuxeo:*'");

        // Every database:* check must have status one of pass, warn, fail, disabled
        Iterator<String> keyIt = checks.fieldNames();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            if (key.startsWith("database:")) {
                Assert.assertTrue(checks.get(key).has("status"),
                    "check '" + key + "' should have 'status'");
                String checkStatus = checks.get(key).get("status").asText();
                assertValidCheckStatus(checkStatus, key);
            }
        }
    }

    private static void assertValidCheckStatus(String status, String checkKey) {
        assertTrue(
            "pass".equals(status) || "warn".equals(status) || "fail".equals(status) || "disabled".equals(status),
            "check " + checkKey + " status should be pass, warn, fail, or disabled; got: " + status);
    }

    @Test
    public void testHealthNoAuthRequired() throws IOException {
        HttpResponse response = getHealthResponse();
        int status = response.getStatusLine().getStatusCode();
        Assert.assertTrue(status == 200 || status == 503,
            "Health endpoint should be accessible without auth (200 or 503); got " + status);
    }
}
