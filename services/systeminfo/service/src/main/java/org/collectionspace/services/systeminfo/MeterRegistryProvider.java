package org.collectionspace.services.systeminfo;

import java.util.Collections;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.binder.tomcat.TomcatMetrics;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.apache.catalina.Manager;
import org.jspecify.annotations.Nullable;

/**
 * A singleton for a micrometer MeterRegistry. The instance is lazily created in the event metrics are disabled.
 *
 * @since 9.1
 */
public class MeterRegistryProvider {

    private static class MeterRegistryProviderInstance {
        private static final MeterRegistryProvider INSTANCE = new MeterRegistryProvider();
    }

    private final PrometheusMeterRegistry registry;
    private final JvmGcMetrics jvmGcMetrics;
    private TomcatMetrics tomcatMetrics;

    private MeterRegistryProvider() {
        this.registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT,
                                                    PrometheusRegistry.defaultRegistry,
                                                    Clock.SYSTEM);

        // configure our histogram config for 0.5 (median), 0.9, and 0.95 quantiles
        registry.config().meterFilter(
            new MeterFilter() {
                @Override
                public @Nullable DistributionStatisticConfig configure(Meter.Id id,
                                                                       DistributionStatisticConfig config) {
                    if (id.getType() == Meter.Type.TIMER) {
                        return DistributionStatisticConfig.builder()
                                                          .percentilesHistogram(true)
                                                          .percentiles(0.50, 0.90, 0.95)
                                                          .build().merge(config);
                    }
                    return MeterFilter.super.configure(id, config);
                }
            });

        new JvmThreadMetrics().bindTo(registry);
        new JvmMemoryMetrics().bindTo(registry);
        new ProcessorMetrics().bindTo(registry);
        new UptimeMetrics().bindTo(registry);

        // store the jvm gv metrics so we can close it later
        jvmGcMetrics = new JvmGcMetrics();
        jvmGcMetrics.bindTo(registry);

        Metrics.addRegistry(registry);
    }

    public static MeterRegistryProvider getInstance() {
        return MeterRegistryProviderInstance.INSTANCE;
    }

    public PrometheusMeterRegistry getRegistry() {
        return registry;
    }

    public static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty("cspace.metrics.enabled"));
    }

    public void registerTomcatMetrics(Manager tomcatManager) {
        this.tomcatMetrics = new TomcatMetrics(tomcatManager, Collections.emptyList());
        tomcatMetrics.bindTo(MeterRegistryProviderInstance.INSTANCE.getRegistry());
    }

    public void close() {
        if (tomcatMetrics != null) {
            tomcatMetrics.close();
        }

        jvmGcMetrics.close();
        registry.close();
    }
}
