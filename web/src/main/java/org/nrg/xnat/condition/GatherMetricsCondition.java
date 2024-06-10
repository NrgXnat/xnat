package org.nrg.xnat.condition;

import org.nrg.xnat.micrometer.utils.MetricsUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.nrg.xnat.services.XnatAppInfo.PROPERTY_XNAT_METRICS_ENABLED;

public class GatherMetricsCondition implements Condition {

    @Override
    // @see org.nrg.xnat.services.XnatAppInfo
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        return MetricsUtils.isMetricsEnabled(context.getEnvironment());
    }
}
