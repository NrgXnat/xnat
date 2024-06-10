package org.nrg.xnat.services.messaging.archive;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xft.event.XftItemEventI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.entities.ResourceSurveyRequest;
import org.nrg.xnat.services.archive.impl.hibernate.ResourceMitigationHelper;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Value
@Slf4j
public class ResourceMitigationEventProperties {
    public ResourceMitigationEventProperties(final XftItemEventI event) {
        id             = event.getId();
        xsiType        = event.getXsiType();
        parameters     = event.getProperties();
        deleted        = GenericUtils.convertToTypedList((Iterable<?>) parameters.get(ResourceMitigationHelper.DELETED), Path.class).stream().map(Objects::toString).collect(Collectors.toList());
        moved          = GenericUtils.convertToTypedMap((Map<?, ?>) parameters.get(ResourceMitigationHelper.UPDATED), Path.class, Path.class).entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), entry -> entry.getValue().toString()));
        request        = (ResourceSurveyRequest) parameters.get(ResourceMitigationHelper.REQUEST);
        username       = request.getRequester();
        requester      = getRequester(username);
        totalFileCount = deleted.size() + moved.size();
    }

    private UserI getRequester(final String username) {
        try {
            return Users.getUser(username);
        } catch (UserInitException e) {
            log.error("An error occurred trying to retrieve the user object for username {}, will return admin user as default", username, e);
        } catch (UserNotFoundException e) {
            log.error("Could not find an account for username {}, will return admin user as default", username, e);
        }
        return Users.getAdminUser();
    }

    String                id;
    String                xsiType;
    Map<String, ?>        parameters;
    List<String>          deleted;
    Map<String, String>   moved;
    String                username;
    ResourceSurveyRequest request;
    UserI                 requester;
    int                   totalFileCount;
}
