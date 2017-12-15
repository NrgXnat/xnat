package org.nrg.xapi.authorization;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotAuthenticatedException;
import org.nrg.xapi.rest.ProjectId;
import org.nrg.xapi.rest.RestUserGroup;
import org.nrg.xapi.rest.Username;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xft.security.UserI;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.nrg.xdat.security.helpers.AccessLevel.*;

/**
 * Provides base functionality for XAPI request authorizers.
 */
public abstract class AbstractXapiAuthorization implements XapiAuthorization {
    protected abstract boolean checkImpl();

    protected abstract boolean considerGuests();

    @Override
    public void check(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) throws InsufficientPrivilegesException, NotAuthenticatedException {
        // We can just cut everything off if the user is a guest: it can't be admin, auth, user, edit, coll, member, owner.
        // However, if accessLevel is something else, and considerGuests returns true, we should not cut things off
        if ((!considerGuests() || accessLevel.equalsAny(Admin, Authenticated, User, Edit, Collaborator, Member, Owner)) && user.isGuest()) {
            throw new NotAuthenticatedException(request.getRequestURL().toString());
        }

        _accessLevel = accessLevel;
        _joinPoint = joinPoint;
        _user = user;
        _username = user.getUsername();
        _request = request;
        _requestMethod = request.getMethod();

        // If access level is administrator, all we need to do is check whether this user is an administrator.
        if (!checkImpl()) {
            throw new InsufficientPrivilegesException(getUsername(), getRequestPath());
        }
    }

    public AccessLevel getAccessLevel() {
        return _accessLevel;
    }

    protected JoinPoint getJoinPoint() {
        return _joinPoint;
    }

    protected UserI getUser() {
        return _user;
    }

    protected String getUsername() {
        return _username;
    }

    protected HttpServletRequest getRequest() {
        return _request;
    }

    protected String getRequestPath() {
        return _requestMethod;
    }

    protected List<String> getUsernames(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, Username.class);
    }

    protected List<String> getProjectIds(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, ProjectId.class);
    }

    protected List<String> getGroups(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, RestUserGroup.class);
    }

    protected <T> List<? extends T> getParameters(final JoinPoint joinPoint, final Class<T> superclass) {
        final List<T> parameters = Lists.newArrayList();
        for (final Object parameter : joinPoint.getArgs()) {
            if (superclass.isAssignableFrom(parameter.getClass())) {
                parameters.add(superclass.cast(parameter));
            }
        }
        return parameters;
    }

    protected List<Class<?>> getParameterTypes(final JoinPoint joinPoint) {
        return Arrays.asList(((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes());
    }

    protected <T> List<Class<? extends T>> getParameterTypes(final JoinPoint joinPoint, final Class<T> superclass) {
        final Method                   method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        final List<Class<? extends T>> types  = Lists.newArrayList();
        for (final Class<?> clazz : method.getParameterTypes()) {
            if (superclass.isAssignableFrom(clazz)) {
                types.add(clazz.asSubclass(superclass));
            }
        }
        return types;
    }

    protected List<String> getAnnotatedParameters(final JoinPoint joinPoint, final Class<? extends Annotation> annotation) {
        final int    parameterIndex = getAnnotatedParameterIndex(((MethodSignature) joinPoint.getSignature()).getMethod(), annotation);
        if (parameterIndex == -1) {
            return NO_PARAMETERS;
        }
        final Object candidate = joinPoint.getArgs()[parameterIndex];
        if (candidate instanceof String) {
            return Collections.singletonList((String) candidate);
        }
        if (candidate instanceof List) {
            //noinspection unchecked
            return (List<String>) candidate;
        }

        final String singular = StringUtils.uncapitalize(annotation.getSimpleName());
        final String plural   = singular + "s";

        if (candidate instanceof Map) {
            final Map map = (Map) candidate;
            if (map.containsKey(plural)) {
                //noinspection unchecked
                return (List<String>) map.get(plural);
            }
            if (map.containsKey(singular)) {
                return Collections.singletonList((String) map.get(singular));
            }
        }
        throw new RuntimeException("Found parameter " + parameterIndex + " annotated with @" + annotation.getSimpleName() + " for the method " + joinPoint.getSignature().getName() + " but the annotated parameter is not a String, List of strings, or a map containing a key named " + singular + " or " + plural + ".");
    }

    protected static int getAnnotatedParameterIndex(final Method method, final Class<? extends Annotation> annotation) {
        final Annotation[][] annotations = method.getParameterAnnotations();
        for (int parameterIndex = 0; parameterIndex < annotations.length; parameterIndex++) {
            final Annotation[] parameterAnnotations = annotations[parameterIndex];
            if (parameterAnnotations.length == 0) {
                continue;
            }
            for (final Annotation instance : parameterAnnotations) {
                if (instance.annotationType() == annotation) {
                    return parameterIndex;
                }
            }
        }
        return -1;
    }

    private static final List<String> NO_PARAMETERS = Collections.emptyList();

    private AccessLevel        _accessLevel;
    private JoinPoint          _joinPoint;
    private UserI              _user;
    private HttpServletRequest _request;
    private String             _username;
    private String             _requestMethod;
}
