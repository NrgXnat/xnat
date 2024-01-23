package org.nrg.xnat.condition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static org.nrg.xnat.services.XnatAppInfo.PROPERTY_XNAT_PRIMARY_NODE;


@Slf4j
public class OnPrimaryNodeCondition implements Condition {

    @Override
    // @see org.nrg.xnat.services.XnatAppInfo
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        Environment environment = context.getEnvironment();
        final boolean     isPrimaryNode        = environment != null && Boolean.parseBoolean(environment.getProperty(PROPERTY_XNAT_PRIMARY_NODE, "true"));
        return isPrimaryNode;
    }
}
