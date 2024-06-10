/*
 * web: org.nrg.xnat.ajax.GrantProjectAccess
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.ajax;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.turbine.services.rundata.RunDataService;
import org.apache.turbine.services.rundata.TurbineRunDataFacade;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.turbine.utils.ProjectAccessRequest;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;

public class GrantProjectAccess {
	public void invite(HttpServletRequest req, HttpServletResponse response,ServletConfig sc) throws Exception{
        String email = (String)req.getParameter("email");
        String projectID = (String)req.getParameter("project");
        String level = (String)req.getParameter("level");

        String requestingUser = req.getRemoteUser();
        UserI userObject = Users.getUser(requestingUser);


        if (!(Permissions.isProjectOwner(userObject, projectID) || Roles.isSiteAdmin(userObject))) {
            //The requesting user does not have sufficient rights to add this user to this project.
            response.setStatus(403);
            response.setContentType("text/html");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().write("RED");
        }
		else {
            RunDataService rundataService = null;
            rundataService = TurbineRunDataFacade.getService();
            try {
                if (rundataService == null) {
                    throw new TurbineException(
                            "No RunData Service configured!");
                }
                RunData data = rundataService.getRunData(req, response, sc);
                UserI user = XDAT.getUserDetails();

                XnatProjectdata project = XnatProjectdata.getXnatProjectdatasById(projectID, user, false);


                //SEND email to user
                ProjectAccessRequest.InviteUser(project, email, user, user.getFirstname() + " " + user.getLastname()
                        + " has invited you to join the " + project.getName() + " "
                        + DisplayManager.GetInstance().getSingularDisplayNameForProject().toLowerCase() + ".", level);
            } catch (Exception e) {

            }

            response.setContentType("text/html");
            response.setHeader("Cache-Control", "no-cache");
            response.getWriter().write("GREEN");
        }
        
    }
}
