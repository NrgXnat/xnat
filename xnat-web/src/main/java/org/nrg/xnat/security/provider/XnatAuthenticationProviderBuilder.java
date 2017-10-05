package org.nrg.xnat.security.provider;

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.reflections.ReflectionUtils.*;

public class XnatAuthenticationProviderBuilder {
    private final Class<? extends XnatAuthenticationProvider> _target;
    private       String                                      _providerId;
    private       String                                      _authMethod;
    private       Object[]                                    _args;
    private       String                                      _name;
    private       Integer                                     _order;
    private       Boolean                                     _visible;

    public static XnatAuthenticationProviderBuilder builder(final Class<? extends XnatAuthenticationProvider> target) {
        return new XnatAuthenticationProviderBuilder(target);
    }

    private XnatAuthenticationProviderBuilder(final Class<? extends XnatAuthenticationProvider> target) {
        _target = target;
    }

    public XnatAuthenticationProviderBuilder providerId(final String providerId) {
        _providerId = providerId;
        return this;
    }

    public XnatAuthenticationProviderBuilder authMethod(final String authMethod) {
        _authMethod = authMethod;
        return this;
    }

    /**
     * Specifies arguments to be passed to the constructor for the authentication provider. Note that these arguments
     * should include the {@link #providerId(String) provider ID} and {@link #authMethod(String) authentication method}
     * for the provider unless those values are determined by the provider implementation somehow. The builder doesn't
     * provide access to set those values as they are immutable once the provider is instantiated.
     *
     * @param args The arguments to pass to the authentication provider's constructor.
     *
     * @return The builder instance.
     */
    public XnatAuthenticationProviderBuilder args(final Object... args) {
        _args = args;
        return this;
    }

    public XnatAuthenticationProviderBuilder name(final String name) {
        _name = name;
        return this;
    }

    public XnatAuthenticationProviderBuilder order(final int order) {
        _order = order;
        return this;
    }

    public XnatAuthenticationProviderBuilder visible(final boolean visible) {
        _visible = visible;
        return this;
    }

    public XnatAuthenticationProvider build() {
        if (StringUtils.isAnyBlank(_providerId, _authMethod)) {
            throw new IllegalStateException("You must specify both provider ID and authentication method in order to build an authentication provider.");
        }

        final Object[]   args    = _args == null ? new Object[]{_name, _providerId} : _args;
        final Class<?>[] classes = getClassesForArgs(args);
        try {
            @SuppressWarnings("unchecked") final Set<Constructor> constructors = getConstructors(_target, withParametersAssignableTo(classes));

            final Constructor<? extends XnatAuthenticationProvider> constructor;
            switch (constructors.size()) {
                case 0:
                    // No constructors found for that mess, panic!
                    throw new IllegalStateException("No constructor for authentication provider " + _target.getName() + " with arguments matching: " + Joiner.on(", ").join(classes));

                case 1:
                    // We found a match, continue.
                    //noinspection unchecked
                    constructor = (Constructor<? extends XnatAuthenticationProvider>) constructors.iterator().next();
                    break;

                default:
                    // There were multiple matches considering superclasses, so try to get an exact match.
                    @SuppressWarnings("unchecked") final Set<Constructor> withoutSuperclasses = getConstructors(_target, withParameters(classes));
                    switch (withoutSuperclasses.size()) {
                        case 0:
                            // No exact match, can't figure it out, panic.
                            throw new IllegalStateException("There are multiple constructors for authentication provider " + _target.getName() + " with arguments matching: " + Joiner.on(", ").join(classes) + " but none that match exactly. Can't determine the appropriate constructor to call.");

                        case 1:
                            // Exact match, use this one.
                            //noinspection unchecked
                            constructor = withoutSuperclasses.iterator().next();
                            break;

                        default:
                            // Multiple exact matches. I don't think this can actually happen, but never say never...
                            throw new IllegalStateException("There are multiple constructors for authentication provider " + _target.getName() + " with arguments exactly matching: " + Joiner.on(", ").join(classes) + ". That's weird, I think, but I can't determine the appropriate constructor to call.");
                    }
            }

            final XnatAuthenticationProvider provider = constructor.newInstance(args);

            if (StringUtils.isNotBlank(_name)) {
                provider.setName(_name);
            }
            if (_order != null) {
                provider.setOrder(_order);
            }
            if (_visible != null) {
                provider.setVisible(_visible);
            }

            return provider;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            final String message = _args == null ? "provider ID " + _providerId + " and auth method " + _authMethod: "the arguments " + Joiner.on(", ").join(_args);
            throw new IllegalStateException("An error occurred trying to build the authentication provider " + _target.getName() + " with " + message);
        }
    }

    private Class<?>[] getClassesForArgs(final Object[] args) {
        final List<Class<?>> classes = new ArrayList<>();
        for (final Object arg : args) {
            classes.add(arg.getClass());
        }
        return classes.toArray(new Class<?>[args.length]);
    }
}
