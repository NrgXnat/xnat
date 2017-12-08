package org.nrg.xnat.security.conditions;

import com.google.common.base.Joiner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xnat.security.provider.AuthenticationProviderConfigurationLocator;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Properties;

@Slf4j
public abstract class AuthProvidersDefinedCondition implements Condition {
    protected AuthProvidersDefinedCondition(final String providerType) {
        assert StringUtils.isNotBlank(providerType) : "You must declare a valid provider type for this condition.";
        _providerType = providerType;
    }

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final ListableBeanFactory                        factory = context.getBeanFactory();
        final AuthenticationProviderConfigurationLocator locator = BeanFactoryUtils.beanOfType(factory, AuthenticationProviderConfigurationLocator.class);
        if (ObjectUtils.isEmpty(locator)) {
            log.debug("Didn't find an auth provider configuration locator, nothing to do");
            return false;
        }
        final Map<String, Properties> definitions = locator.getProviderDefinitions(_providerType);
        if (ObjectUtils.isEmpty(locator)) {
            log.debug("Found an auth provider configuration locator, but it doesn't have any LDAP providers, nothing to do");
            return false;
        }
        log.debug("Found locator bean with {} LDAP providers defined: {}", definitions.size(), Joiner.on(", ").join(definitions.keySet()));
        return true;
    }

    private final String _providerType;
}

