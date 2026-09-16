package org.collectionspace.services.systeminfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@Path("/metrics")
public class MetricsResource {

    private final PrometheusMeterRegistry registry;

    public MetricsResource(final PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    @GET
    public Response get() {
        return Response.ok(registry.scrape()).build();
    }
}
