package org.nrg.xdat.preferences;

import com.fasterxml.jackson.databind.JavaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xft.security.UserI;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.nrg.xdat.security.helpers.Users.AUTHORITY_ADMIN;

@Slf4j
public abstract class AbstractJsonBasedPreferenceAccess implements PreferenceAccess {

    private static final ResourceLoader RESOURCE_LOADER = new DefaultResourceLoader();

    private final Map<String, List<String>> accessLevels;

    protected AbstractJsonBasedPreferenceAccess(final SerializerService serializer, final String resource) throws IOException {
        final Resource accessProperties = RESOURCE_LOADER.getResource(resource);
        try (final InputStream inputStream = accessProperties.getInputStream()) {
            final JavaType mapType = serializer.getMapStringListString();
            accessLevels = StringUtils.endsWithAny(resource, ".yml", ".yaml")
                           ? serializer.deserializeYaml(inputStream, mapType)
                           : serializer.deserializeJson(inputStream, mapType);
        }
        Stream.of(KEY_PUBLIC, KEY_AUTHENTICATED, KEY_ADMIN).forEach(level -> {
            if (!accessLevels.containsKey(level)) {
                accessLevels.put(level, Collections.emptyList());
            }
            log.debug("Found {} entries for access level {}", accessLevels.get(level).size(), level);
        });
        accessLevels.get(KEY_AUTHENTICATED).addAll(accessLevels.get(KEY_PUBLIC));
        accessLevels.get(KEY_ADMIN).addAll(accessLevels.get(KEY_AUTHENTICATED));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract String getPreferenceTool();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRead(final UserI user, final String preference) {
        // Admin can access them all so no need to look further.
        if (user.getAuthorities().contains(AUTHORITY_ADMIN)) {
            return true;
        }

        return user.isGuest() ? accessLevels.get(KEY_PUBLIC).contains(preference) : accessLevels.get(KEY_AUTHENTICATED).contains(preference);
    }
}
