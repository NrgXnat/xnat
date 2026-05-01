/*
 * core: org.nrg.xdat.turbine.utils.AdminUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.framework.generics.GenericUtils;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserChangeRequest;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserChangeRequestService;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xft.security.UserI;
import org.springframework.mail.MailException;

import javax.mail.MessagingException;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Tim
 *
 */
@Slf4j
public class AdminUtils {
    public static final String DEFAULT_LOGIN_FAILURE_MESSAGE = "Login attempt failed. Please try again.";

    /**
     *
     */
	public AdminUtils() {
		super();
	}

    public static String GetLoginFailureMessage() {
        if (StringUtils.isBlank(loginFailureMessage)) {
            loginFailureMessage = resolveLoginFailureMessage();
        }
        return loginFailureMessage;
    }

    public static void sendDisabledUserVerificationNotification(final UserI user, final Context context) throws Exception {
        final String subject = "Expired User reverified: " + user.getFirstname() + " " + user.getLastname();

		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			String body = XDAT.getNotificationsPreferences().getEmailMessageDisabledUserVerification();
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);

			Date date = Calendar.getInstance().getTime();
			String dateString = TurbineUtils.GetInstance().formatDateTime(date);
			body = body.replaceAll("DATE_INPUT", dateString);
			body = body.replaceAll("USER_LOGIN", user.getLogin());

			String url = TurbineUtils.GetFullServerPath() + "/app/action/DisplayItemAction/search_value/" + user.getLogin() + "/search_element/xdat:user/search_field/xdat:user.login";

			String loginLink = "<a href=\"" + url + "\">verified user account " + user.getLogin() + "</a>";

			String enabledMessage;

			if (user.isEnabled()) {
				enabledMessage = "<p>After being disabled due to inactivity, the owner of this account has completed the email verification process to show that they are still the proper account owner. This user account is now no longer disabled due to inactivity and they can access the site again.\n</p>" +
						"<p> The " + loginLink + " has been enabled.</p>";
			} else {
				enabledMessage = "<p>After being disabled due to inactivity, the owner of this account has completed the email verification process to show that they are still the proper account owner. This user account is now no longer disabled due to inactivity, but their account has not yet been manually enabled by a site administrator. You, or another admin, will need to do this before they can access the site. </p>" +
						"<p>You can review and enable the " + loginLink + ".</p>";
			}
			body = body.replaceAll("ENABLED_MESSAGE", enabledMessage);

			Map<String, Object> properties = new HashMap<>();
			properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
			properties.put(MailMessage.PROP_SUBJECT, subject);
			properties.put(MailMessage.PROP_HTML, body);
			XDAT.verifyNotificationType(NotificationType.Issue);
			XDAT.getNotificationService().createNotification(NotificationType.Issue.toString(), properties);
			sendAdminNotificationCopy(subject, body, NotificationType.Issue);
		}
	}

	public static void sendNewUserNotification(final UserI user, final Context context) throws Exception {
		final UserRegistrationData regData = XDAT.getContextService().getBean(UserRegistrationDataService.class).getUserRegistrationData(user);
		final String comments = regData == null ? "" : regData.getComments();
		final String phone = regData == null ? "" : regData.getPhone();
		final String organization = regData == null ? "" : regData.getOrganization();
		sendNewUserNotification(user, comments, phone, organization, context);
	}

	public static void sendNewUserNotification(final UserI user, final String comments, final String phone, final String organization, final Context context) throws Exception {
		final String username = user.getUsername();
		final String firstName = user.getFirstname();
		final String lastName = user.getLastname();
		final String email = user.getEmail();

		// If user isn't enabled, send a notification to the administrator for each user we just verified.
		if (!user.isEnabled()) {
			// Send admin email
			AdminUtils.sendNewUserRequestNotification(user, comments, phone, organization, context);
		} else {
			AdminUtils.sendNewUserCreationNotification(user, comments, phone, organization, context);
			AdminUtils.sendNewUserEmailMessage(username, email);
		}
	}

	public static void sendNewUserVerificationEmail(final UserI user) throws Exception {
		// If the Item is null, don't continue.
		if (user == null) {
			throw new Exception("Unable to send verification email. Required User is null.");
		}
		sendNewUserVerificationEmail(user, user.isEnabled());
	}


	public static void sendNewUserVerificationEmail(UserI user, final boolean isEnabled) throws Exception {
		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			final AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(user.getLogin());
			String body = XDAT.getNotificationsPreferences().getEmailMessageNewUserVerification();
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
			String enabledMessage;
			if (isEnabled) {
				enabledMessage = "After verifying your email address, you will be able to immediately log in and start using " + TurbineUtils.GetSystemName() + ".";
			} else {
				enabledMessage = "Note: After verifying your email address, you will need to wait for a " +  TurbineUtils.GetSystemName() + " administrator to enable your account before you can log in. ";
			}
			body = body.replaceAll("ENABLED_MESSAGE", enabledMessage);

			final String verificationUrl = TurbineUtils.GetFullServerPath() + "/app/template/VerifyEmail.vm?a=" + token.getAlias() + "&s=" + token.getSecret();

			String verifyUrl = "<a href=\"" + verificationUrl + "\">Verify your email address on XNAT</a>";

			final String resendVerificationUrl = TurbineUtils.GetFullServerPath() + "/app/template/ForgotLogin.vm";

			String forgotLoginUrl = "<a href=\"" + resendVerificationUrl + "\">Request a new email verification link from XNAT</a>";

			body = body.replaceAll("VERIFY_URL", verifyUrl);
			body = body.replaceAll("FORGOT_LOGIN_URL", forgotLoginUrl);

			String subject = "Verify Your Email Address For " + TurbineUtils.GetSystemName();

			XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), new String[] { user.getEmail() }, new String[] { XDAT.getSiteConfigPreferences().getAdminEmail() }, null, subject, body);
		}
	}

    /**
	 * Sends the Welcome email to a new User
	 *
	 * @param username    The username of the new user.
	 * @param email       The email  of the new user.
	 */
	public static void sendNewUserEmailMessage(String username, String email) throws Exception {
		if(XDAT.getNotificationsPreferences().getSmtpEnabled()){
			String body = XDAT.getNotificationsPreferences().getEmailMessageUserRegistration();
			UserI user = Users.getUser(username);
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);

			body = body.replaceAll("HELP_EMAIL",XDAT.getNotificationsPreferences().getHelpContactInfo());

			body = XDAT.getNotificationsPreferences().replaceBackwardsCompatibleEmailInconsistencies(body);

			String subject = "Welcome to " + TurbineUtils.GetSystemName();

			XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), new String[] { email }, new String[] { XDAT.getSiteConfigPreferences().getAdminEmail() }, null, subject, body);
		}
	}

	@SuppressWarnings("UnusedReturnValue")
	public static boolean sendUserHTMLEmail(final String subject, final String message, final boolean ccAdmin, final String[] addresses) {
		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			if (addresses.length > 0) {
				final String from = XDAT.getSiteConfigPreferences().getAdminEmail();
				try {
					XDAT.getMailService().sendHtmlMessage(from, addresses, ccAdmin ? new String[]{from} : null, null, subject, message);
					return true;
				} catch (MessagingException exception) {
					log.error("Unable to send mail", exception);
				}
			}
		}
		return false;
	}

	public static void sendErrorNotification(String message, Context context) {
		UserI user = XDAT.getUserDetails();
		assert user != null;
		String email = user.getEmail();
		if (!StringUtils.isBlank(email) && XDAT.getNotificationsPreferences().getSmtpEnabled()) {


			final String subject = TurbineUtils.GetSystemName() + ": Error Thrown";

			String body = XDAT.getNotificationsPreferences().getEmailMessageErrorMessage();
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
			body = body.replaceAll("USER_LOGIN", user.getLogin());

			Date date = Calendar.getInstance().getTime();
			String dateString = TurbineUtils.GetInstance().formatDateTime(date);
			body = body.replaceAll("ERROR_TIME", dateString);
			body = body.replaceAll("ERROR_MESSAGE", message);

			try {
				// XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), getErrorEmailIds(), TurbineUtils.GetSystemName() + ": Error Thrown", body);
				Map<String, Object> properties = new HashMap<>();
				properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
				properties.put(MailMessage.PROP_SUBJECT, subject);
                properties.put(MailMessage.PROP_HTML, body);
                XDAT.verifyNotificationType(NotificationType.Error);
                XDAT.getNotificationService().createNotification(NotificationType.Error.toString(), properties);
            } catch (Exception e1) {
				log.error("Unable to send mail", e1);
			}

			sendAdminNotificationCopy(subject, body, NotificationType.Error);
		}
	}

	public static void sendAdminEmail(UserI user, String subject, String message) {
		if(XDAT.getNotificationsPreferences().getSmtpEnabled()){
		String admin = XDAT.getSiteConfigPreferences().getAdminEmail();
		String qualifiedSubject = TurbineUtils.GetSystemName() + ": " + subject;

		StringBuilder formattedMessage = new StringBuilder();
		formattedMessage.append("HOST: ").append(TurbineUtils.GetFullServerPath()).append("<BR>");
		if (user != null)
			formattedMessage.append("USER: ").append(user.getUsername()).append("(").append(user.getFirstname()).append(" ").append(user.getLastname()).append(")").append("<BR>");
		formattedMessage.append("TIME: ").append(java.util.Calendar.getInstance().getTime()).append("<BR>");
		formattedMessage.append("MESSAGE: ").append(message).append("<BR>");

		try {
			XDAT.getMailService().sendHtmlMessage(admin, admin, qualifiedSubject, formattedMessage.toString());
		} catch (Exception exception) {
			log.error("Unable to send mail", exception);
		}
	}
	}

	public static void emailAllAdmins(UserI user, String subject, String message) {

		ArrayList<String> alreadyEmailed = new ArrayList<>();
		if(XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			//first send an email to the sit admin email
			sendAdminEmail(user, subject, message);
			alreadyEmailed.add(XDAT.getSiteConfigPreferences().getAdminEmail());

			//then email all administrators
			List<? extends UserI> allUsers = Users.getUsers();
			if (allUsers != null && allUsers.size() > 0) {
				for (UserI u : allUsers) {
					if (u!=null && Roles.isSiteAdmin(u)) {
						String currEmail = u.getEmail();
						if(!alreadyEmailed.contains(currEmail)){
							String qualifiedSubject = TurbineUtils.GetSystemName() + ": " + subject;

							StringBuilder formattedMessage = new StringBuilder();
							formattedMessage.append("HOST: ").append(TurbineUtils.GetFullServerPath()).append("<BR>");
							formattedMessage.append("USER: ").append(u.getUsername()).append("(").append(u.getFirstname()).append(" ").append(u.getLastname()).append(")").append("<BR>");
							formattedMessage.append("TIME: ").append(java.util.Calendar.getInstance().getTime()).append("<BR>");
							formattedMessage.append("MESSAGE: ").append(message).append("<BR>");

							try {
								XDAT.getMailService().sendHtmlMessage(currEmail, currEmail, qualifiedSubject, formattedMessage.toString());
							} catch (Exception exception) {
								log.error("Unable to send mail", exception);
							}

							alreadyEmailed.add(currEmail);
						}
					}

				}
			}
		}
	}

	public static void sendAdminEmail(String subject, String message) {
		sendAdminEmail(null, subject, message);
	}

	public static void emailAllAdmins(String subject, String message) {
		emailAllAdmins(null, subject, message);
	}

	public static String populateVmTemplate(Context context, String templatePath) {
        StringWriter writer = new StringWriter();
        Template template = Velocity.getTemplate(templatePath);
        template.merge(context, writer);
        return writer.toString();
    }

	/**
	 * Gets the primary administrator user object.
	 *
	 * @return The primary administrator user object.
	 * @deprecated Use the {@link Users#getAdminUser()} method instead. In Spring-based code, you should just autowire the
	 * {@link XnatUserProvider} instance for "primaryAdminUserProvider".
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated
	public static UserI getAdminUser() {
		return Users.getAdminUser();
	}

	private static void sendAdminNotificationCopy(final String subject, final String body, final NotificationType event) {
		final String email = XDAT.getSiteConfigPreferences().getAdminEmail();
		if (XDAT.getNotificationsPreferences().getCopyAdminOnNotifications() &&
			!StringUtils.contains(XDAT.getSubscriberEmailsListAsString(event), email)) {
			AdminUtils.sendUserHTMLEmail(subject, body, false, new String[] { email });
		}
	}

	/**
	 * Sends an email to subscribers for the {@link NotificationType#NewUser new user event} indicating that a new user
	 * account registration has been requested. This notification is sent when a new user is created but auto-enable is
	 * turned off.
	 *
	 *
	 * @param context     The data context.
	 * @throws Exception When something goes wrong.
	 */
	private static void sendNewUserRequestNotification(UserI user, String comments, String phone, String lab, Context context) throws Exception {

		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			String body = XDAT.getNotificationsPreferences().getEmailMessageNewUserRequest();
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
			Date date = Calendar.getInstance().getTime();
			String dateString = TurbineUtils.GetInstance().formatDateTime(date);
			body = body.replaceAll("TIME", dateString);
			body = body.replaceAll("USER_PHONE", phone);
			body = body.replaceAll("LAB_NAME", lab);
			body = body.replaceAll("USER_COMMENTS", comments);

			String projectAccess = "";
			if (context.containsKey("pars")) {
				List<String> pars = GenericUtils.convertToTypedList((List<?>) context.get("pars"), String.class);
				if (pars.size() > 0) {
					projectAccess =  "The account has open project access requests for the following projects: ";
					projectAccess = projectAccess + "<ul><li>" + String.join("</li><li>", pars) + "</li></ul>";
					projectAccess = projectAccess + "\n";

				}
			}
			body = body.replaceAll("PROJECT_ACCESS_REQUESTS", projectAccess);

			String url = TurbineUtils.GetFullServerPath()+ "/app/action/DisplayItemAction/search_value/" + user.getUsername() + "/search_element/xdat:user/search_field/xdat:user.login";

			String reviewLink = "<a href=\"" + url + "\">Review and Enable " + user.getUsername() + "</a>";
			body = body.replaceAll("REVIEW_LINK", reviewLink);

			final String subject = TurbineUtils.GetSystemName() + " New User Request: " + user.getFirstname() + " " + user.getLastname();

			final Map<String, Object> properties = new HashMap<>();
			properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
			properties.put(MailMessage.PROP_SUBJECT, subject);
			properties.put(MailMessage.PROP_HTML, body);
			XDAT.verifyNotificationType(NotificationType.NewUser);
			XDAT.getNotificationService().createNotification(NotificationType.NewUser.toString(), properties);

			sendAdminNotificationCopy(subject, body, NotificationType.NewUser);
		}
	}

	/**
	 * Sends an email to subscribers for the {@link NotificationType#NewUser new user event} indicating that a new user
	 * account registration has been created. This notification is sent when a new user is created but auto-enable is
	 * turned on.
	 *
	 *
	 * @param user    The new user.
	 * @param context     The data context.
	 * @throws Exception When something goes wrong.
	 */
	private static void sendNewUserCreationNotification(UserI user, String comments, String phone, String lab, Context context) throws Exception {

		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			String body = XDAT.getNotificationsPreferences().getEmailMessageNewUserNotification();
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
			Date date = Calendar.getInstance().getTime();
			String dateString = TurbineUtils.GetInstance().formatDateTime(date);
			body = body.replaceAll("TIME", dateString);
			body = body.replaceAll("USER_PHONE", phone);
			body = body.replaceAll("LAB_NAME", lab);
			body = body.replaceAll("USER_COMMENTS", comments);
			String projectAccess = "";

			if (context.containsKey("pars")) {
				List<String> pars = GenericUtils.convertToTypedList((List<?>) context.get("pars"), String.class);
				if (pars.size() > 0) {
					projectAccess =  "<p>The account has open project access requests for the following projects: ";
					projectAccess = projectAccess + "<ul><li>" + String.join("</li><li>", pars) + "</li></ul>";
					projectAccess = projectAccess + "</p>\n";

				}
			}
			body = body.replaceAll("PROJECT_ACCESS_REQUESTS", projectAccess);

			String url = TurbineUtils.GetFullServerPath()+ "/app/action/DisplayItemAction/search_value/" + user.getUsername() + "/search_element/xdat:user/search_field/xdat:user.login";

			String reviewLink = "<a href=\"" + url + "\">Review the new user account <b>" + user.getUsername() + "</b></a>";

			body = body.replaceAll("REVIEW_LINK", reviewLink);

			final String subject = "New User Created: " + user.getFirstname() + " " + user.getLastname();

			final Map<String, Object> properties = new HashMap<>();
			properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
			properties.put(MailMessage.PROP_SUBJECT, subject);
			properties.put(MailMessage.PROP_HTML, body);
			XDAT.verifyNotificationType(NotificationType.NewUser);
			XDAT.getNotificationService().createNotification(NotificationType.NewUser.toString(), properties);

			sendAdminNotificationCopy(subject, body, NotificationType.NewUser);
		}

	}

    private static String resolveLoginFailureMessage() {
        final String prefMessage = XDAT.getSiteConfigPreferences().getUiLoginFailureMessage();
        if (!StringUtils.isBlank(prefMessage)) {
            if (!prefMessage.contains("%d")) {
                return prefMessage;
            }
            if (XDAT.getSiteConfigPreferences().getMaxFailedLogins() > 0) {
                return String.format(prefMessage, XDAT.getSiteConfigPreferences().getMaxFailedLogins());
            }
            log.warn("Found login error message that contained a %d format placeholder, but the max failed login attempts is zero or less. Using the default login failure message instead.");
        } else {
            log.info("No value was set for the login failure message. Using the default login failure message instead.");
        }
        return DEFAULT_LOGIN_FAILURE_MESSAGE;
    }

    private static String loginFailureMessage = null;

	public static boolean issueEmailChangeRequest(UserI user, String newEmail) {
		if (!XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			return false;
		}

		String guid = UUID.randomUUID().toString();
		getUserChangeRequestService().create(new UserChangeRequest(user.getUsername(), "email", newEmail, guid));

		boolean sent;
		try {
			String body = XDAT.getNotificationsPreferences().getEmailMessageEmailAddressChangeRequest();
			body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
			body = body.replaceAll("NEW_EMAIL", newEmail);
			sent = AdminUtils.sendUserHTMLEmail("Email Change Request Submitted", body, false,
					new String[]{user.getEmail()});

			if (sent) {
				body = XDAT.getNotificationsPreferences().getEmailMessageVerifyEmailChangeRequest();
				body = XDAT.getNotificationsPreferences().replaceCommonAnchorTags(body, user);
				String link = TurbineUtils.GetFullServerPath() + "/app/template/XDATScreen_UpdateUser.vm?confirmationToken=" + guid;
				String changeEmailLink = "<a href=\"" + link + "\">this link</a>";
				body = body.replaceAll("CHANGE_EMAIL_LINK", changeEmailLink);
				AdminUtils.sendUserHTMLEmail("Verify Email Address Change Request", body, false,
						new String[]{newEmail});
			}
		} catch (Exception e) {
			log.error("Unable to send email change request verification for {} to {}", user.getEmail(), newEmail, e);
			sent = false;
		}

		if (!sent) {
			getUserChangeRequestService().cancelRequest(user.getUsername(), "email");
		}

		return sent;
	}

	public static UserChangeRequestService getUserChangeRequestService() {
		if (userChangeRequestService == null) {
			userChangeRequestService = XDAT.getContextService().getBean(UserChangeRequestService.class);
		}
		return userChangeRequestService;
	}

	private static UserChangeRequestService userChangeRequestService;
}
