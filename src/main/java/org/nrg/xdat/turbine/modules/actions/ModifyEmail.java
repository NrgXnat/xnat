/*
 * core: org.nrg.xdat.turbine.modules.actions.ModifyEmail
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
import org.nrg.xdat.security.ElementSecurity;
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

@SuppressWarnings("unused")
public class ModifyEmail extends SecureAction {
    public void doPerform(final RunData data, final Context context) throws Exception {
        final UserI found;
        try {
            found = Users.createUser(TurbineUtils.GetDataParameterHash(data));
        } catch (UserFieldMappingException e1) {
            data.addMessage(e1.getMessage());
            if (TurbineUtils.GetPassedParameter("edit_screen", data) != null) {
                data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen", data)));
            }
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
            data.addMessage("Unable to identify user for email modification.");
            if (TurbineUtils.GetPassedParameter("edit_screen", data) != null) {
                data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen", data)));
            }
            return;
        }

        final String oldEmail = existing.getEmail();
        final String newEmail = found.getEmail();

        if (StringUtils.isBlank(newEmail) || StringUtils.equals(oldEmail, newEmail)) {
            data.addMessage("Email address unchanged.");
            if (TurbineUtils.GetPassedParameter("edit_screen", data) != null) {
                data.setScreenTemplate(((String) TurbineUtils.GetPassedParameter("edit_screen", data)));
            }
            return;
        }

        if (!newEmail.contains("@")) {
            data.setMessage("Please enter a valid email address.");
            data.setScreenTemplate("XDATScreen_UpdateUser.vm");
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

            AdminUtils.sendUserHTMLEmail("Email address changed.", "Your email address was successfully changed to " + existing.getEmail() + ".", true, new String[]{user.getEmail(), existing.getEmail()});

            context.put("success", true);
            data.setMessage("Email address changed.");
            // TODO: This should be included so that it stays on the update user page rather than going to the main page.
            // data.setScreenTemplate("XDATScreen_UpdateUser.vm");
        } catch (InvalidPermissionException e) {
            notifyAdmin(user, data, 403, "Possible Authorization Bypass event", "User attempted to modify a user account other then his/her own.  This typically requires tampering with the HTTP form submission process.");
        } catch (Exception e) {
            logger.error("Error Storing User", e);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ModifyEmail.class);
}
