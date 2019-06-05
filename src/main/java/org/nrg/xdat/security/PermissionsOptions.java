package org.nrg.xdat.security;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Provides a way to assign different permissions to different data types when creating groups.
 */
@Data
@RequiredArgsConstructor
@Accessors(prefix = "_")
@Slf4j
public class PermissionsOptions {
    public PermissionsOptions(final String pattern, final boolean read, final boolean edit, final boolean create, final boolean delete, final boolean active) {
        this(Pattern.compile(pattern), read, edit, create, delete, active);
    }

    private final Pattern _pattern;
    private final boolean _read;
    private final boolean _edit;
    private final boolean _create;
    private final boolean _delete;
    private final boolean _active;
}
