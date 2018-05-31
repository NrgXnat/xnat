/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyPassword
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
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
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.PasswordComplexityException;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.services.XdatUserAuthService;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.util.Date;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
public class ModifyPassword extends ModifyAction {
    public void doPerform(final RunData data, final Context context) throws Exception {
        setDataAndContext(data, context);

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
        } catch (UserFieldMappingException e) {
            redirect(false, e.getMessage());
            return;
        }

        final String login = found.getLogin();
        final UserI existing = found.getID() != null ? Users.getUser(found.getID()) : StringUtils.isNotBlank(login) ? Users.getUser(login) : null;

        if (existing == null) {
            redirect(false, "Unable to identify user for password modification.");
            return;
        }

        final String encodedPassword = existing.getPassword();
        final String currentPassword = data.getParameters().getString("current_password");
        final String updatedPassword = data.getParameters().getString("xdat:user.primary_password"); // the object in found will have run the password through escape character encoding, potentially altering it

        if (data.getSession().getAttribute("forgot") == null) {
            if (StringUtils.isBlank(encodedPassword) && !Roles.isSiteAdmin(user)) {
                redirect(false, "The user " + login + " is configured as a no-login user and can't have a password set. If you feel this is an error, please contact your site administrator.");
                return;
            }

            final boolean specifiedCurrentPassword = StringUtils.isNotBlank(currentPassword);
            final boolean hasCurrentPassword       = StringUtils.isNotBlank(user.getPassword());
            if ((!specifiedCurrentPassword && hasCurrentPassword) || StringUtils.isBlank(updatedPassword) || !Users.isPasswordValid(encodedPassword, currentPassword, existing.getSalt())) {
                //User correctly entered their old password or they forgot their old password
                final StringBuilder message = new StringBuilder("Your password was not updated: ");
                if (!specifiedCurrentPassword || StringUtils.isBlank(updatedPassword)) {
                    message.append("you must provide values for both your current and updated password.");
                } else if (!Users.isPasswordValid(encodedPassword, currentPassword, existing.getSalt())) {
                    message.append("you entered an incorrect value for your current password.");
                }
                redirect(false, message.toString());
                return;
            }
        }

        existing.setPassword(updatedPassword);

        final ValidationResultsI validate = Users.validate(existing);
        if (!validate.isValid()) {
            final String message;
            if (validate instanceof ValidationResults) {
                message = "<p>The submitted user information failed validation for the following reasons:</p><ul>" + ((ValidationResults) validate).toHTML();
            } else {
                message = "The submitted password seems to be invalid, but I have no further details.";
            }
            redirect(false, message);
        }

        try {
            if (StringUtils.isNotEmpty(updatedPassword)) {
                Users.save(existing, user, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Password"));

                //need to update password expiration
                final XdatUserAuth auth = XDAT.getXdatUserAuthService().getUserByNameAndAuth(existing.getUsername(), XdatUserAuthService.LOCALDB, "");
                auth.setPasswordUpdated(new Date());
                if(XDAT.getSiteConfigPreferences().getCanResetFailedLoginsWithForgotPassword()) {
                    auth.resetFailedLogins();
                }
                XDAT.getXdatUserAuthService().update(auth);

                final SchemaElementI se = SchemaElement.GetElement(Users.getUserDataType());

                if (se.getGenericXFTElement().getType().getLocalPrefix().equalsIgnoreCase("xdat")) {
                    ElementSecurity.refresh();
                }

                redirect(true, "Password changed.");
            } else {
                redirect(false, "Password unchanged.");
            }
        } catch (InvalidPermissionException e) {
            notifyAdmin(user, data, 403, "Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
        } catch (PasswordComplexityException e) {
            redirect(false, e.getMessage());
        } catch (Exception e) {
            logger.error("Error Storing User", e);
        }
    }

    @Override
    @Nonnull
    protected String getDefaultEditScreen() {
        return "XDATScreen_UpdateUser.vm";
    }

    private static final Logger logger = LoggerFactory.getLogger(ModifyPassword.class);
}
