/*
 * prefs: org.nrg.prefs.exceptions.InvalidPreferenceName
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.exceptions;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceException;

@Getter
@Accessors(prefix = "_")
public class InvalidPreferenceName extends NrgServiceException {
    /**
     * Creates an exception with the specified message.
     *
     * @param message The message to set.
     *
     * @deprecated Use any of the other constructors specifying the tool ID and preference name instead.
     */
    @Deprecated
    public InvalidPreferenceName(final String message) {
        this(null, null, Scope.getDefaultScope(), "", message);
    }

    /**
     * Creates an exception indicating that the specified preference is not a valid name for the specified tool.
     *
     * @param toolId     The ID of the tool containing the preference.
     * @param preference The ID of the preference.
     */
    public InvalidPreferenceName(final String toolId, final String preference) {
        this(toolId, preference, Scope.getDefaultScope(), "", null);
    }

    /**
     * Creates an exception indicating that the specified preference is not a valid name for the specified tool, along
     * with the specified message.
     *
     * @param toolId     The ID of the tool containing the preference.
     * @param preference The ID of the preference.
     * @param scope      The scope for the associated entity.
     * @param entityId   The ID of the specific entity.
     */
    public InvalidPreferenceName(final String toolId, final String preference, final Scope scope, final String entityId) {
        this(toolId, preference, scope, entityId, null);
    }

    /**
     * Creates an exception indicating that the specified preference is not a valid name for the specified tool, along
     * with the specified message.
     *
     * @param toolId     The ID of the tool containing the preference.
     * @param preference The ID of the preference.
     * @param scope      The scope for the associated entity.
     * @param entityId   The ID of the specific entity.
     * @param message    The message to set.
     */
    public InvalidPreferenceName(final String toolId, final String preference, final Scope scope, final String entityId, final String message) {
        super(NrgServiceError.ConfigurationError, message);
        _toolId = toolId;
        _preference = preference;
        _scope = scope;
        _entityId = entityId;
    }

    private final String _toolId;
    private final String _preference;
    private final Scope  _scope;
    private final String _entityId;
}
