package org.nrg.xnat.micrometer.utils;

import org.springframework.core.env.Environment;

import static org.nrg.xnat.services.XnatAppInfo.PROPERTY_XNAT_METRICS_ENABLED;

public class MetricsUtils {

    public static  boolean isMetricsEnabled(final Environment environment) {
        return environment != null ? Boolean.parseBoolean(environment.getProperty(PROPERTY_XNAT_METRICS_ENABLED, "false")) : false;
    }

}
