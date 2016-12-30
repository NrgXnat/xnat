/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyPassword
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tim
 *
 */
@SuppressWarnings("unused")
public class ModifyPassword extends SecureAction {
    public void doPerform(RunData data, Context context) throws Exception {
        if (data.getSession().getAttribute("forgot") != null && ((Boolean) data.getSession().getAttribute("forgot"))) {
            context.put("forgot", true);
        }

        final UserI user = XDAT.getUserDetails();
        if (user == null) {
            error(new Exception("User 'null' cannot change password."), data);
            return;
        }

        if (user.getUsername().equals("guest")) {
            error(new Exception("Guest account password must be managed in the administration section."), data);
            return;
        }

        final UserI found;
        try {
            found = Users.createUser(TurbineUtils.GetDataParameterHash(data));
        } catch (UserFieldMappingException e1) {
            data.addMessage(e1.getMessage());
            redirect(data, false);
            return;
        }

        UserI existing = null;
        if (found.getID() != null) {
            existing = Users.getUser(found.getID());
        }

        if (existing == null && found.getLogin() != null) {
            existing = Users.getUser(found.getLogin());
        }

        if (existing == null) {
            data.addMessage("Unable to identify user for password modification.");
            redirect(data, false);
            return;
        }

        final String newPassword     = data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it
        final String oldPassword     = existing.getPassword();
        final String currentPassword = data.getParameters().getString("current_password");

        if ((StringUtils.isBlank(oldPassword) || StringUtils.isBlank(currentPassword) || Users.isPasswordValid(oldPassword, currentPassword, existing.getSalt())) && data.getSession().getAttribute("forgot") == null) {
            //User correctly entered their old password or they forgot their old password
            data.setMessage("Incorrect current password. Password unchanged.");
            redirect(data, false);
            return;
        }

        existing.setPassword(newPassword);

        if (!Users.validate(existing).isValid()) {
            redirect(data, false);
        }

        final UserI authenticatedUser = XDAT.getUserDetails();
        try {
            if (StringUtils.isNotEmpty(newPassword)) {
                Users.save(existing, authenticatedUser, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Password"));

                //need to update password expiration
                final XdatUserAuth auth = XDAT.getXdatUserAuthService().getUserByNameAndAuth(existing.getUsername(), XdatUserAuthService.LOCALDB, "");
                auth.setPasswordUpdated(new java.util.Date());
                XDAT.getXdatUserAuthService().update(auth);

                final SchemaElementI se = SchemaElement.GetElement(Users.getUserDataType());

                if (se.getGenericXFTElement().getType().getLocalPrefix().equalsIgnoreCase("xdat")) {
                    ElementSecurity.refresh();
                }

                data.setMessage("Password changed.");
                redirect(data, true);
            } else {
                data.setMessage("Password unchanged.");
                redirect(data, false);
            }
        } catch (InvalidPermissionException e) {
            notifyAdmin(authenticatedUser, data, 403, "Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
        } catch (PasswordComplexityException e) {
            data.setMessage(e.getMessage());
            redirect(data, false);
        } catch (Exception e) {
            logger.error("Error Storing User", e);
        }
    }

    private void redirect(RunData data, boolean changed){
        if(changed) {
            Boolean expired = (Boolean) data.getSession().getAttribute("expired");
            Boolean forgot = (Boolean) data.getSession().getAttribute("forgot");
            String loginTemplate = org.apache.turbine.Turbine.getConfiguration().getString("template.login");
            String homepageTemplate = org.apache.turbine.Turbine.getConfiguration().getString("template.homepage");
            if ((forgot != null && forgot)) {
                //User forgot their password. They must log in again.
                if (StringUtils.isNotEmpty(loginTemplate)) {
                    // We're running in a templating solution
                    data.setScreenTemplate(loginTemplate);
                } else {
                    data.setScreen(org.apache.turbine.Turbine.getConfiguration().getString("screen.login"));
                }
            }else if ((expired != null && expired)) {
                //They just updated expired password.
                data.getSession().setAttribute("expired", Boolean.FALSE);//New password is not expired
                if (StringUtils.isNotEmpty(homepageTemplate)) {
                    // We're running in a templating solution
                    data.setScreenTemplate(homepageTemplate);
                } else {
                    data.setScreen(org.apache.turbine.Turbine.getConfiguration().getString("screen.homepage"));
                }
            } else {
                data.setScreenTemplate("XDATScreen_UpdateUser.vm");
            }
        }
        else{
            data.setScreenTemplate("XDATScreen_UpdateUser.vm");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ModifyPassword.class);
}
