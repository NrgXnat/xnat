package org.nrg.xapi.authorization;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.xapi.exceptions.InsufficientPrivilegesException;
import org.nrg.xapi.exceptions.NotAuthenticatedException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.rest.*;
import org.nrg.xdat.security.helpers.AccessLevel;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

import static org.nrg.xdat.security.helpers.AccessLevel.*;

/**
 * Provides base functionality for XAPI request authorizers.
 */
@Slf4j
public abstract class AbstractXapiAuthorization implements XapiAuthorization {
    protected abstract boolean checkImpl(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) throws InsufficientPrivilegesException, NotFoundException;

    protected abstract boolean considerGuests();

    @Override
    public void check(final AccessLevel accessLevel, final JoinPoint joinPoint, final UserI user, final HttpServletRequest request) throws InsufficientPrivilegesException, NotAuthenticatedException, NotFoundException {
        // We can just cut everything off if the user is a guest: it can't be admin, auth, user, edit, coll, member, owner.
        // However, if accessLevel is something else, and considerGuests returns true, we should not cut things off
        final boolean isGuest = user.isGuest();
        //noinspection deprecation
        if ((!considerGuests() || accessLevel.equalsAny(Admin, Authenticated, DataAdmin, DataAccess, User, Edit, Delete, Collaborator, Member, Owner)) && isGuest) {
            throw new NotAuthenticatedException(request.getRequestURL().toString());
        }

        // If access level is administrator, all we need to do is check whether this user is an administrator.
        try {
            if (!checkImpl(accessLevel, joinPoint, user, request)) {
                if (isGuest) {
                    throw new NotAuthenticatedException(request.getRequestURI());
                }
                throw new InsufficientPrivilegesException(user.getUsername(), request.getRequestURI());
            }
        } catch (NotFoundException e) {
            if (isGuest) {
                throw new NotAuthenticatedException(request.getRequestURI());
            }
            if (!Groups.hasAllDataAccess(user)) {
                throw new InsufficientPrivilegesException(user.getUsername(), request.getRequestURI());
            }
            throw e;
        }
    }

    protected List<String> getUsernames(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, Username.class);
    }

    protected List<String> getProjects(final JoinPoint joinPoint) {
        @SuppressWarnings("deprecation") final List<String> projectIds = getAnnotatedParameters(joinPoint, ProjectId.class);
        if (!projectIds.isEmpty()) {
            log.warn("You are using a plugin that uses the deprecated @ProjectId annotation. Please check for an updated version of the plugin, as this deprecated annotation may not be supported in future versions of XNAT.");
            return new ArrayList<>(CollectionUtils.union(projectIds, getAnnotatedParameters(joinPoint, Project.class)));
        } else {
            // TODO: Eventually this should be the only call in here once we remove @ProjectId
            return getAnnotatedParameters(joinPoint, Project.class);
        }
    }

    protected List<String> getSubjects(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, Subject.class);
    }

    protected List<String> getExperiments(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, Experiment.class);
    }

    protected List<String> getGroups(final JoinPoint joinPoint) {
        return getAnnotatedParameters(joinPoint, UserGroup.class);
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

    @SuppressWarnings("unused")
    protected List<Class<?>> getParameterTypes(final JoinPoint joinPoint) {
        return Arrays.asList(((MethodSignature) joinPoint.getSignature()).getMethod().getParameterTypes());
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("WeakerAccess")
    protected List<String> getAnnotatedParameters(final JoinPoint joinPoint, final Class<? extends Annotation> annotation) {
        final int parameterIndex = getAnnotatedParameterIndex(((MethodSignature) joinPoint.getSignature()).getMethod(), annotation);
        if (parameterIndex == -1) {
            return NO_PARAMETERS;
        }
        final Object candidate = joinPoint.getArgs()[parameterIndex];
        if (candidate instanceof String) {
            return Collections.singletonList((String) candidate);
        }
        if (candidate instanceof List) {
            // TODO: This could eventually be a list of objects that need to be converted to IDs.
            //noinspection unchecked
            return (List<String>) candidate;
        }
        if (candidate instanceof XFTItem) {
            try {
                return Collections.singletonList(((XFTItem) candidate).getIDValue());
            } catch (XFTInitException e) {
                log.error("Error initializing XFT", e);
            } catch (ElementNotFoundException e) {
                log.error("Element not found: {}", e.ELEMENT);
            }
            return Collections.emptyList();
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

    public static int getAnnotatedParameterIndex(final Method method, final Class<? extends Annotation> annotation) {
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
}
