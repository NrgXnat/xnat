package org.nrg.xdat.preferences;

import org.nrg.xft.security.UserI;

public interface PreferenceAccess {
    String KEY_PUBLIC        = "public";
    String KEY_AUTHENTICATED = "authenticated";
    String KEY_ADMIN         = "admin";

    /**
     * Indicates the tool to which this bean controls access.
     *
     * @return The tool ID
     */
    String getPreferenceTool();

    /**
     * Indicates whether the user can read the specified preference for the {@link #getPreferenceTool() configured tool}.
     *
     * @param user       The user object to check
     * @param preference The name of the preference to check
     *
     * @return Returns true if the user can read the indicated preference, false otherwise.
     */
    boolean canRead(final UserI user, final String preference);
}
