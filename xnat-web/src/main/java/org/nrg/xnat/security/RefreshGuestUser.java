/*
 * web: org.nrg.xnat.security.DisableInactiveUsers
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.security;

import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.utils.WorkflowUtils;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RefreshGuestUser implements Runnable {

    public RefreshGuestUser() {
    }

    /**
     * Refreshes guest user object so information about what permissions it has are up to date.
     */
    @Override
    public void run() {
        try {
            UserI guest = Users.getGuest(true);
            ArrayList<ElementSecurity> securedElements = ElementSecurity.GetSecureElements();

            final List<Object> ps = Permissions.getAllowedValues(guest, "xnat:projectData", "xnat:projectData/ID", org.nrg.xdat.security.SecurityManager.READ);
            for(Object proj : ps) {
                String projectId = "";
                if(proj!=null){
                    projectId = proj.toString();
                }
                final EventMetaI c = EventUtils.ADMIN_EVENT(guest);

                for (ElementSecurity es : securedElements) {
                    if (es!=null && es.hasField(es.getElementName() + "/project") && es.hasField(es.getElementName() + "/sharing/share/project")) {
                        Permissions.setPermissions(guest, AdminUtils.getAdminUser(), es.getElementName(), es.getElementName() + "/project", projectId, false, true, false, false, true, true, c);
                        Permissions.setPermissions(guest, AdminUtils.getAdminUser(), es.getElementName(), es.getElementName() + "/sharing/share/project", projectId, false, false, false, false, false, true, c);
                    }
                }
            }
        } catch (final UserInitException e) {
            // If this occurs, don't make a big fuss: it probably means that the system's starting up. If that's NOT
            // what it means, plenty more parts of the system will be complaining so we don't need to add to it.
            logger.debug("Got a UserInitException while refreshing guest user. This probably just means that the system is still initializing: {}", e.getMessage());
        } catch (final Exception e) {
            logger.error("An error occurred trying to refresh guest user.", e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RefreshGuestUser.class);

}

