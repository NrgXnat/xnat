/*
 * core: org.nrg.xdat.security.helpers.FeatureDefinitionI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.security.helpers;

public interface FeatureDefinitionI {
    /**
     * What is the key which identifies this feature
     *
     * @return The feature key.
     */
    String getKey();

    /**
     * What is the human readable name of this feature
     *
     * @return The feature name.
     */
    String getName();

    /**
     * What is the description of this feature for human readability?
     *
     * @return The feature description.
     */
    String getDescription();

    /**
     * Indicates whether this feature is banned on this server.
     *
     * @return Returns true if the feature is banned, false otherwise.
     */
    boolean isBanned();

    /**
     * Indicates whether this feature is on by default.
     *
     * @return Returns true if the feature is on by default, false otherwise.
     */
    boolean isOnByDefault();
}
