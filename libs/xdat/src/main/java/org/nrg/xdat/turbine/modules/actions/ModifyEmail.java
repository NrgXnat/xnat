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
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserFieldMappingException;
import org.nrg.xdat.services.UserChangeRequestService;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.InvalidPermissionException;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.ValidationUtils.ValidationResultsI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ModifyEmail extends ModifyAction {

    public static final String EMAIL_ADDRESS_CHANGED = "Email address changed.";

    public void doPerform(final RunData data, final Context context) throws Exception {
        final String method = data.getRequest().getMethod();
        UserChangeRequestService userChangeRequestService = XDAT.getContextService().getBean(UserChangeRequestService.class);

        if (!StringUtils.equalsIgnoreCase("post", method)) {
            throw new Exception("The only valid method for this action is POST.");
        }

        final String submittedCsrf = data.getParameters().getString("XNAT_CSRF");
        final String sessionCsrf = (String) data.getSession().getAttribute("XNAT_CSRF");
        if (!StringUtils.equals(submittedCsrf, sessionCsrf)) {
            SecureAction.handleCsrfTokenError(data.getRequest());
        }
        
        setDataAndContext(data, context);

        final boolean cancelRequest = data.getParameters().getBoolean("cancelRequest");
        final boolean confirmationToken = data.getParameters().getBoolean("confirmationToken");
        if(cancelRequest){
            userChangeRequestService.cancelRequest(XDAT.getUserDetails().getUsername(), "email");
            redirect(true, "Email change request canceled.");
        }
        else {
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
                redirect(false, "Unable to identify user for email modification.");
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

            final UserI user = XDAT.getUserDetails();
            assert user != null;
            SiteConfigPreferences preferences = XDAT.getSiteConfigPreferences();
            if(preferences.getEmailVerification() && !Roles.isSiteAdmin(XDAT.getUserDetails()) ) {
                //User can only create an email change request. They must verify their email for it to take effect
                boolean sent;
                if (!AdminUtils.issueEmailChangeRequest(user, newEmail)) {
                    logger.error("Unable to send email change verification");
                    redirect(false, "Email address change request could not be submitted. Please coordinate with site admin.");
                    return;
                }

                redirect(true, "Email address change request submitted. An email was sent to the new email you submitted. Once you have clicked the link from that email to verify that it is your account, your email will be changed.");
            }
            else {
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

                try {
                    Users.save(existing, user, false, EventUtils.newEventInstance(EventUtils.CATEGORY.SIDE_ADMIN, EventUtils.TYPE.WEB_FORM, "Modified User Email"));
                    ElementSecurity.refresh();

                    // Update the email address for the user principal in the application session.
                    user.setEmail(existing.getEmail());

                    if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                        String body = XDAT.getNotificationsPreferences().getEmailMessageAddressChanged();
                        body = body.replaceAll("NEW_EMAIL", existing.getEmail());
                        try {
                            AdminUtils.sendUserHTMLEmail(EMAIL_ADDRESS_CHANGED, body, true, new String[]{user.getEmail(), existing.getEmail()});
                        } catch (MailException e) {
                            logger.error("An error occurred trying to send an email to the administrator and the following addresses: " + user.getEmail() + ", " + existing.getEmail() + ".\nSubject: \"" + EMAIL_ADDRESS_CHANGED + "\"\nMessage:\n" + body, e);
                        }
                    }


                    redirect(true, "Email address changed.");
                } catch (InvalidPermissionException e) {
                    if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
                        String body = XDAT.getNotificationsPreferences().getEmailMessageUnauthorizedDataAttempt();
                        String type = "attempted. User attempted to modify a user account other than thier own. This typically requires tampering with the HTTP form submission process.";
                        body = body.replaceAll("TYPE", type);
                        body = body.replaceAll("USER_DETAILS", "");
                        notifyAdmin(user, data, 403, "Possible Authorization Bypass event", body);
                    }
                } catch (Exception e) {
                    logger.error("Error Storing User", e);
                }
            }

        }
    }

    @Override
    @Nonnull
    protected String getDefaultEditScreen() {
        return "XDATScreen_UpdateUser.vm";
    }

    private static final Logger logger = LoggerFactory.getLogger(ModifyEmail.class);
}