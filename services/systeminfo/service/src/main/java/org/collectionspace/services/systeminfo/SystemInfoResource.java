package org.collectionspace.services.systeminfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Properties;
import java.util.TimeZone;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.authorization.AuthZ;
import org.collectionspace.services.authorization.CSpaceResource;
import org.collectionspace.services.authorization.URIResourceImpl;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.CSWebApplicationException;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.context.RemoteServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.security.UnauthorizedException;

@Path(SystemInfoClient.SERVICE_PATH)
@Produces({"application/xml"})
@Consumes({"application/xml"})
public class SystemInfoResource extends AbstractCollectionSpaceResourceImpl<SystemInfoCommon, SystemInfoCommon> {

    private static final String VER_DISPLAY_NAME = "CollectionSpace Services v%s.%s";
    private static final int MAJOR_IDX = 0;
    private static final int MINOR_IDX = 1;
    private static final int PATCH_IDX = 2;

    @Override
    public Class<?> getCommonPartClass() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServiceName() {
        return SystemInfoClient.SERVICE_NAME;
    }

    @Override
    protected String getVersionString() {
        // TODO Auto-generated method stub
        return null;
    }

    //
    // API Endpoints
    //

    @GET
    public SystemInfoCommon get(@Context UriInfo ui) {
        SystemInfoCommon result;

        try {
            String packageVersion = SystemInfoResource.class.getPackage().getImplementationVersion();
            String[] versionParts = packageVersion.split("\\.");

            result = new SystemInfoCommon();
            result.setInstanceId(ServiceMain.getInstance().getCspaceInstanceId());
            result.setDisplayName(String.format(VER_DISPLAY_NAME, versionParts[MAJOR_IDX], versionParts[MINOR_IDX]));

            Version ver = new Version();
            ver.setMajor(versionParts[MAJOR_IDX]);
            ver.setMinor(versionParts[MINOR_IDX]);
            ver.setPatch(versionParts[PATCH_IDX]);
            ver.setBuild(readGitCommitId());
            result.setVersion(ver);

            result.setHostTimezone(TimeZone.getDefault().getID());
            result.setHostLocale(Locale.getDefault().toLanguageTag());
            result.setHostCharset(Charset.defaultCharset().name());

            // To get the full set of the system information, we required the user be authenticated *and* have "DELETE"
            // privs on the "systeminfo" resource
            try {
                ServiceContext<SystemInfoCommon, SystemInfoCommon> ctx = createServiceContext(getServiceName(), ui);
                CSpaceResource res = new URIResourceImpl(ctx.getTenantId(), SystemInfoClient.SERVICE_NAME,
                                                         HttpMethod.DELETE);
                if (AuthZ.get().isAccessAllowed(res)) {
                    // TODO: Stop hardcoding this!
                    // result.setNuxeoVersionString("7.10-HF17");
                    result.setHost(String.format("Architecture:%s Name:%s Version:%s",
                                                 System.getProperty("os.arch"),
                                                 System.getProperty("os.name"),
                                                 System.getProperty("os.version")));
                    result.setJavaVersionString(System.getProperty("java.version"));
                    // TODO: Stop hardcoding this!
                    // result.setPostgresVersionString("9.5.7");
                }
            } catch (UnauthorizedException e) {
                logger.trace(e.getMessage(), e);
            }

        } catch (Exception e) {
            logger.error("Error generating system info", e);
            Response response = Response.status(Response.Status.BAD_REQUEST)
                                        .entity(e.getMessage())
                                        .type("text/plain").build();
            throw new CSWebApplicationException(response);
        }

        return result;
    }

    private String readGitCommitId() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("git.properties"));
        return properties.getProperty("git.commit.id.abbrev");
    }

    @Override
    public ServiceContextFactory<SystemInfoCommon, SystemInfoCommon> getServiceContextFactory() {
        return (ServiceContextFactory<SystemInfoCommon, SystemInfoCommon>) RemoteServiceContextFactory.get();
    }
}
