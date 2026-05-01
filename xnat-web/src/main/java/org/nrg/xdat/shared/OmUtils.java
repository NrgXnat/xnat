package org.nrg.xdat.shared;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RegExUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourceseries;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class OmUtils {
    private static final Pattern CHARS_TO_CLEAN = Pattern.compile("[`~@#$%^&*()+=\\[\\]{}|\\\\/?:;',.<> ]");

    private OmUtils() {
        // Nothing here.
    }

    public static void validateXnatAbstractResources(final String expectedPath, final List<XnatAbstractresourceI> resources) throws InvalidValueException {
        for (final XnatAbstractresourceI resource : resources) {
            final String uri;
            if (resource instanceof XnatResource) {
                uri = ((XnatResource) resource).getUri();
            } else if (resource instanceof XnatResourceseries) {
                uri = ((XnatResourceseries) resource).getPath();
            } else {
                continue;
            }
            FileUtils.ValidateUriAgainstRoot(uri, expectedPath, "URI references data outside of the project:" + uri);
        }
    }

    public static void deleteResourceFiles(final UserI user, final String rootPath, final String projectId, final List<XnatAbstractresourceI> resources, final EventMetaI event) throws Exception {
        deleteResourceFiles(user, rootPath, projectId, null, resources, event);
    }

    public static void deleteResourceFiles(final UserI user, final String rootPath, final String projectId, final File archivePath, final List<XnatAbstractresourceI> resources, final EventMetaI event) throws Exception {
        // If there are no resources to delete, then skip the rest of this.
        if (resources == null || resources.isEmpty()) {
            log.warn("User {} tried to delete resource files in project {} with root path {} and archive path {}, but no resources were specified", user != null ? user.getUsername() : "<null>", projectId, rootPath, archivePath);
            return;
        }

        if (log.isInfoEnabled()) {
            log.info("User {} is deleting resource files in project {} with root path {} and archive path {}, resources: {}", user != null ? user.getUsername() : "<null>", projectId, rootPath, archivePath, resources.stream().map(XnatAbstractresourceI::getXnatAbstractresourceId).map(Object::toString).collect(Collectors.joining(", ")));
        }

        final String timestamp = FileUtils.getMsTimestamp();

        for (final XnatAbstractresourceI resource : resources) {
            ((XnatAbstractresource) resource).deleteWithBackup(rootPath, projectId, user, event, timestamp);
        }

        if (archivePath != null && archivePath.exists()) {
            FileUtils.MoveToCache(archivePath, timestamp);
        }
    }

    /**
     * Converts invalid characters in the submitted value to "_".
     *
     * @param value The string to be cleaned
     *
     * @return The cleaned string
     */
    public static String cleanValue(final String value) {
        return RegExUtils.replaceAll(value, CHARS_TO_CLEAN, "_");
    }
}
