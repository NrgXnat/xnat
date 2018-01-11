/*
 * web: org.nrg.xnat.security.DisableInactiveUsers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;

import java.util.List;

@Slf4j
public class RefreshGuestUser implements Runnable {
    /**
     * Refreshes guest user object so information about what permissions it has are up to date.
     */
    @Override
    public void run() {
        try {
            final UserI                 guest           = Users.getGuest(true);
            final List<ElementSecurity> securedElements = ElementSecurity.GetSecureElements();

            final List<Object> allowedValues = Permissions.getAllowedValues(guest, "xnat:projectData", "xnat:projectData/ID", org.nrg.xdat.security.SecurityManager.READ);
            for (final Object project : allowedValues) {
                final String projectId = project != null ? project.toString() : "";
                if (Permissions.canReadProject(guest, projectId)) {
                    final EventMetaI event = EventUtils.ADMIN_EVENT(guest);
                    for (final ElementSecurity securedElement : securedElements) {
                        if (securedElement != null && securedElement.hasField(securedElement.getElementName() + "/project") && securedElement.hasField(securedElement.getElementName() + "/sharing/share/project")) {
                            Permissions.setPermissions(guest, Users.getAdminUser(), securedElement.getElementName(), securedElement.getElementName() + "/project", projectId, false, true, false, false, true, true, event);
                            Permissions.setPermissions(guest, Users.getAdminUser(), securedElement.getElementName(), securedElement.getElementName() + "/sharing/share/project", projectId, false, false, false, false, false, true, event);
                        }
                    }
                }
            }
        } catch (final UserInitException e) {
            // If this occurs, don't make a big fuss: it probably means that the system's starting up. If that's NOT
            // what it means, plenty more parts of the system will be complaining so we don't need to add to it.
            log.debug("Got a UserInitException while refreshing guest user. This probably just means that the system is still initializing: {}", e.getMessage());
        } catch (final Exception e) {
            log.error("An error occurred trying to refresh guest user.", e);
        }
    }
}

