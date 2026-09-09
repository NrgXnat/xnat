/*
 * web: org.nrg.xnat.restlet.util.XnatWebDavStatus
 * XNAT http://www.xnat.org
 *
 * Restlet 2.6 removed the WebDAV status constants from org.restlet.data.Status
 * (2.5.x still had them). XNAT's REST layer uses 422/423/424 as general-purpose
 * client errors, so they are pinned here with the same reason phrases.
 */

package org.nrg.xnat.restlet.util;

import org.restlet.data.Status;

public final class XnatWebDavStatus {
    public static final Status SUCCESS_MULTI_STATUS              = new Status(207, "Multi-Status", "Provides status for multiple independent operations", null);
    public static final Status SERVER_ERROR_INSUFFICIENT_STORAGE = new Status(507, "Insufficient Storage", "The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request", null);
    public static final Status CLIENT_ERROR_UNPROCESSABLE_ENTITY = new Status(422, "Unprocessable Entity", "The server understands the content type of the request entity and the syntax of the request entity is correct but was unable to process the contained instructions", null);
    public static final Status CLIENT_ERROR_LOCKED               = new Status(423, "Locked", "The source or destination resource of a method is locked", null);
    public static final Status CLIENT_ERROR_FAILED_DEPENDENCY    = new Status(424, "Failed Dependency", "The method could not be performed on the resource because the requested action depended on another action and that action failed", null);

    private XnatWebDavStatus() {
    }
}
