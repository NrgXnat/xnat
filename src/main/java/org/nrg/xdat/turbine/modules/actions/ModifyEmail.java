/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyEmail
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
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.messaging.MessagingException;

import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("unused")
public class ModifyEmail extends ModifyAction {

    public static final String EMAIL_ADDRESS_CHANGED = "Email address changed.";

    public void doPerform(final RunData data, final Context context) throws Exception {
        setDataAndContext(data, context);

        final UserI found;
        try {
            found = Users.createUser(TurbineUtils.GetDataParameterHash(data));
        } catch (UserFieldMappingException e) {
            redirect(false, e.getMessage());
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
            redirect(false,"Unable to identify user for email modification.");
            return;
        }

        final String oldEmail = existing.getEmail();
        final String newEmail = found.getEmail();

        if (StringUtils.isBlank(newEmail) || StringUtils.equals(oldEmail, newEmail)) {
            redirect(false, "Email address unchanged.");
            return;
        }

        if (!newEmail.contains("@")) {
            redirect(false, "Please enter a valid email address.");
            return;
        }

        // Only admins can set an email address that's already being used.
        if (!Roles.isSiteAdmin(XDAT.getUserDetails()) && Users.getUsersByEmail(newEmail).size() > 0) {
            redirect(false, "The email address you've specified is already in use.");
            return;
        }

        existing.setEmail(newEmail);

        final ValidationResultsI validation = Users.validate(existing);

        if (!validation.isValid()) {
            TurbineUtils.SetEditItem(found, data);
            context.put("vr", validation);
            if (TurbineUtils.GetPassedParameter("edit_screen", data) != null) {
                data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen", data)));
            }
            return;
        }

        final UserI user = XDAT.getUserDetails();
        assert user != null;

        try {
            Users.save(existing, user, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Email"));
            ElementSecurity.refresh();

            // Update the email address for the user principal in the application session.
            user.setEmail(existing.getEmail());

            final String message = "Your email address was successfully changed to " + existing.getEmail() + ".";
            try {
                AdminUtils.sendUserHTMLEmail(EMAIL_ADDRESS_CHANGED, message, true, new String[]{user.getEmail(), existing.getEmail()});
            } catch (MailException e) {
                logger.error("An error occurred trying to send an email to the administrator and the following addresses: " + user.getEmail() + ", " + existing.getEmail() + ".\nSubject: \"" + EMAIL_ADDRESS_CHANGED + "\"\nMessage:\n" + message, e);
            }

            redirect(true, "Email address changed.");
        } catch (InvalidPermissionException e) {
            notifyAdmin(user, data, 403, "Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
        } catch (Exception e) {
            logger.error("Error Storing User", e);
        }
    }

    @Override
    @Nonnull
    protected String getDefaultEditScreen() {
        return "XDATScreen_UpdateUser.vm";
    }

    private static final Logger logger = LoggerFactory.getLogger(ModifyEmail.class);
}
