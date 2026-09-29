package org.collectionspace.services.systeminfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/metrics")
public class MetricsResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response get() {
        return Response.ok(MeterRegistryProvider.getInstance().getRegistry().scrape()).build();
    }
}
