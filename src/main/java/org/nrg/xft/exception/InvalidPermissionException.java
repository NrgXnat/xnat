/*
 * core: org.nrg.xft.exception.InvalidPermissionException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.exception;

import org.apache.commons.lang.StringUtils;

/**
 * @author Tim
 */
@SuppressWarnings("serial")
public class InvalidPermissionException extends XftItemException {
    public InvalidPermissionException(final String error) {
        super("This user has insufficient privileges for the data type '" + error + "'.");
    }
    public InvalidPermissionException(final String username, final String dataType) {
        super("The user " + username + " has insufficient privileges for the data type '" + dataType + "'.");
    }
    public InvalidPermissionException(final String username, final String dataType, final String itemId) {
        super("The user " + username + " has insufficient privileges for the '" + dataType + "' item with ID '" + itemId + "'.");
    }
    public InvalidPermissionException(final String username, final String action, final String dataType, final String itemId) {
        super("The user " + username + " has insufficient privileges to " + action + " the '" + dataType + (itemId != null ? "' item with ID '" + itemId + "'." : ""));
    }
    public InvalidPermissionException(final String username, final String action, final String dataType, final String itemId, final String message) {
        super("The user " + username + " has insufficient privileges to " + action + " the '" + dataType + (StringUtils.isNotBlank(itemId) ? "' item with ID '" + itemId + "'." : "") + (StringUtils.isNotBlank(message) ? ": " + message : ""));
    }
}
