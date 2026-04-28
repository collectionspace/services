package org.collectionspace.services.systeminfo;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;

import javax.naming.NamingException;

import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.ConfigUtils;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.storage.JDBCTools;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;
import org.collectionspace.services.nuxeo.client.java.NuxeoConnectorEmbedded;
import org.nuxeo.elasticsearch.ElasticSearchComponent;
import org.nuxeo.elasticsearch.api.ElasticSearchService;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JAX-RS resource for the /health endpoint. Returns JSON health status
 * suitable for load balancers and Kubernetes (200/503, IETF draft health format).
 */
@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    private static final Logger logger = LoggerFactory.getLogger(HealthResource.class);
    private static final String DESCRIPTION = "CollectionSpace Services";

    @GET
    public Response get() {
        Map<String, Object> root = new LinkedHashMap<>();
        Map<String, Object> checks = new LinkedHashMap<>();
        root.put("checks", checks);

        try {
            String cspaceInstanceId = getServiceId();

            root.put("description", DESCRIPTION);
            root.put("version", getApplicationVersion());
            root.put("releaseId", readGitCommitId());
            root.put("serviceId", cspaceInstanceId);

            List<String> repoNames = resolveAllRepositoryNames();
            String firstRepoName = repoNames.isEmpty() ? null : repoNames.get(0);
            boolean requiredFailed = false;
            boolean hasWarn = false;

            // Check: database:cspace (required)
            Map<String, Object> cspaceCheck = checkDatabaseByDatasource(JDBCTools.CSPACE_DATASOURCE_NAME, false, null,
                    null);
            checks.put("database:cspace", cspaceCheck);
            if ("fail".equals(cspaceCheck.get("status"))) {
                requiredFailed = true;
            }

            // Check: database:csadmin (optional)
            Map<String, Object> csadminCheck = checkDatabaseByDatasource(JDBCTools.CSADMIN_DATASOURCE_NAME, true, null,
                    null);
            checks.put("database:csadmin", csadminCheck);
            if ("fail".equals(csadminCheck.get("status"))) {
                hasWarn = true;
            }

            // Check: database:nuxeo per repository (required for each)
            if (repoNames.isEmpty()) {
                Map<String, Object> warnCheck = new LinkedHashMap<>();
                warnCheck.put("status", "warn");
                warnCheck.put("output", "no tenants configured");
                checks.put("database:nuxeo", warnCheck);
                hasWarn = true;
            } else {
                for (String repoName : repoNames) {
                    Map<String, Object> nuxeoDbCheck = checkDatabaseByDatasource(JDBCTools.NUXEO_DATASOURCE_NAME, false,
                            repoName, cspaceInstanceId);
                    checks.put("database:nuxeo:" + repoName, nuxeoDbCheck);
                    if ("fail".equals(nuxeoDbCheck.get("status"))) {
                        requiredFailed = true;
                    }
                }
            }

            // Check: nuxeo:repository (first repo only, required)
            if (firstRepoName != null) {
                Map<String, Object> repoCheck = checkNuxeoRepository(firstRepoName);
                checks.put("nuxeo:repository", repoCheck);
                if ("fail".equals(repoCheck.get("status"))) {
                    requiredFailed = true;
                }
            } else {
                Map<String, Object> warnCheck = new LinkedHashMap<>();
                warnCheck.put("status", "warn");
                warnCheck.put("output", "no tenants configured");
                checks.put("nuxeo:repository", warnCheck);
                hasWarn = true;
            }

            // Check: database:nuxeo_reader per repository (optional)
            for (String repoName : repoNames) {
                Map<String, Object> readerCheck = checkDatabaseByDatasource(JDBCTools.NUXEO_READER_DATASOURCE_NAME,
                        true, repoName, cspaceInstanceId);
                checks.put("database:nuxeo_reader:" + repoName, readerCheck);
                if ("fail".equals(readerCheck.get("status"))) {
                    hasWarn = true;
                }
            }

            // Check: database:csadmin_nuxeo per repository (optional)
            for (String repoName : repoNames) {
                Map<String, Object> csadminNuxeoCheck = checkDatabaseByDatasource(
                        JDBCTools.CSADMIN_NUXEO_DATASOURCE_NAME, true, repoName, cspaceInstanceId);
                checks.put("database:csadmin_nuxeo:" + repoName, csadminNuxeoCheck);
                if ("fail".equals(csadminNuxeoCheck.get("status"))) {
                    hasWarn = true;
                }
            }

            // Check: elasticsearch (optional)
            Map<String, Object> esCheck = checkElasticsearch();
            checks.put("elasticsearch", esCheck);
            if ("fail".equals(esCheck.get("status"))) {
                hasWarn = true;
            }

            // Overall status
            String overall;
            if (requiredFailed) {
                overall = "fail";
                root.put("output", firstFailingCheckMessage(checks));
            } else if (hasWarn) {
                overall = "warn";
            } else {
                overall = "pass";
            }
            root.put("status", overall);

            String json = new ObjectMapper().writeValueAsString(root);
            int statusCode = "fail".equals(overall) ? 503 : 200;
            return Response.status(statusCode).entity(json).type(MediaType.APPLICATION_JSON).build();

        } catch (Throwable t) {
            logger.error("Error building health response", t);
            Map<String, Object> failRoot = new LinkedHashMap<>();
            failRoot.put("status", "fail");
            failRoot.put("description", DESCRIPTION);
            failRoot.put("output", t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName());
            try {
                String json = new ObjectMapper().writeValueAsString(failRoot);
                return Response.status(503).entity(json).type(MediaType.APPLICATION_JSON).build();
            } catch (Exception e) {
                return Response.status(503).entity("{\"status\":\"fail\",\"output\":\"Error serializing response\"}")
                    .type(MediaType.APPLICATION_JSON).build();
            }
        }
    }

    private String getApplicationVersion() {
        String v = SystemInfoResource.class.getPackage().getImplementationVersion();
        return v != null ? v : "unknown";
    }

    private String readGitCommitId() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("git.properties")) {
            if (in == null) {
                return "unknown";
            }
            Properties props = new Properties();
            props.load(in);
            String abbrev = props.getProperty("git.commit.id.abbrev");
            return abbrev != null ? abbrev : "unknown";
        } catch (IOException e) {
            return "unknown";
        }
    }

    private String getServiceId() {
        try {
            return ServiceMain.getInstance().getCspaceInstanceId();
        } catch (Exception e) {
            return "unknown";
        }
    }

    /**
     * Returns all repository names from tenant bindings (deduplicated).
     */
    private List<String> resolveAllRepositoryNames() {
        List<String> result = new ArrayList<>();
        try {
            TenantBindingConfigReaderImpl reader = ServiceMain.getInstance().getTenantBindingConfigReader();
            Hashtable<String, TenantBindingType> bindings = reader.getTenantBindings();
            if (bindings == null || bindings.isEmpty()) {
                return result;
            }
            for (TenantBindingType tenant : bindings.values()) {
                List<String> repoList = ConfigUtils.getRepositoryNameList(tenant);
                if (repoList != null) {
                    for (String repoName : repoList) {
                        if (repoName != null && !result.contains(repoName)) {
                            result.add(repoName);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Could not resolve repository names", e);
        }
        return result;
    }

    /**
     * Runs a database health check: obtains a connection via the supplier,
     * executes SELECT 1, reads product/version from metadata, and returns
     * a check map with status pass/fail/disabled. When {@code namingExceptionAsDisabled}
     * is true, NamingException results in status "disabled" instead of "fail".
     */
    private Map<String, Object> checkDatabase(Callable<Connection> connectionSupplier,
            boolean namingExceptionAsDisabled) {
        Map<String, Object> check = new LinkedHashMap<>();
        check.put("componentType", "datastore");
        Connection conn = null;
        try {
            conn = connectionSupplier.call();
            try (Statement stmt = conn.createStatement()) {
                stmt.executeQuery("SELECT 1");
            }
            DatabaseMetaData meta = conn.getMetaData();
            String product = meta.getDatabaseProductName();
            String version = meta.getDatabaseProductVersion();
            check.put("status", "pass");
            check.put("observedValue", product + " " + version);
        } catch (NamingException e) {
            if (namingExceptionAsDisabled) {
                check.put("status", "disabled");
                check.put("output", "datasource not configured");
            } else {
                check.put("status", "fail");
                check.put("output", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            }
        } catch (Exception e) {
            check.put("status", "fail");
            check.put("output", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    logger.trace("Error closing connection", e);
                }
            }
        }
        return check;
    }

    private Map<String, Object> checkDatabaseByDatasource(String datasourceName, boolean namingExceptionAsDisabled,
            String repositoryName, String cspaceInstanceId) {
        return checkDatabase(() -> getConnectionForDatasource(datasourceName, repositoryName, cspaceInstanceId),
                namingExceptionAsDisabled);
    }

    private Connection getConnectionForDatasource(String datasourceName, String repositoryName, String cspaceInstanceId)
            throws Exception {
        if (repositoryName == null || cspaceInstanceId == null) {
            DataSource ds = JDBCTools.getDataSource(datasourceName);
            return ds.getConnection();
        }
        return JDBCTools.getConnection(datasourceName, repositoryName, cspaceInstanceId);
    }

    private Map<String, Object> checkNuxeoRepository(String repoName) {
        Map<String, Object> check = new LinkedHashMap<>();
        check.put("componentType", "component");
        CoreSessionInterface session = null;
        try {
            NuxeoConnectorEmbedded connector = NuxeoConnectorEmbedded.getInstance();
            session = connector.getClient().openRepository(repoName, ServiceContext.DEFAULT_TX_TIMEOUT);
            session.getRepositoryName();
            check.put("status", "pass");
            check.put("observedValue", repoName);
        } catch (Exception e) {
            check.put("status", "fail");
            check.put("output", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        } finally {
            if (session != null) {
                try {
                    NuxeoConnectorEmbedded.getInstance().releaseRepositorySession(session);
                } catch (Exception e) {
                    logger.trace("Error releasing repository session", e);
                }
            }
        }
        return check;
    }

    private Map<String, Object> checkElasticsearch() {
        Map<String, Object> check = new LinkedHashMap<>();
        if (!Framework.isBooleanPropertyTrue("elasticsearch.enabled")) {
            check.put("status", "disabled");
            return check;
        }
        try {
            ElasticSearchComponent es = (ElasticSearchComponent) Framework.getService(ElasticSearchService.class);
            Object client = es != null ? es.getClient() : null;
            if (client != null) {
                check.put("status", "pass");
            } else {
                check.put("status", "fail");
                check.put("output", "Elasticsearch client not available");
            }
        } catch (Exception e) {
            check.put("status", "fail");
            check.put("output", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
        return check;
    }

    private String firstFailingCheckMessage(Map<String, Object> checks) {
        for (Map.Entry<String, Object> entry : checks.entrySet()) {
            if (entry.getValue() instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> c = (Map<String, Object>) entry.getValue();
                if ("fail".equals(c.get("status"))) {
                    return entry.getKey() + " check failed";
                }
            }
        }
        return "health check failed";
    }
}
