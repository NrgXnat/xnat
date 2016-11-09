/*
 * core: org.nrg.xdat.turbine.utils.AdminUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tim
 *
 */
public class AdminUtils {
	private static final Logger  logger                 = LoggerFactory.getLogger(AdminUtils.class);

	private static       String  login_failure_message  = null;

	/**
     *
     */
	public AdminUtils() {
		super();
	}

	public static String GetLoginFailureMessage(){
		if(login_failure_message==null){
			try {
				login_failure_message=XDAT.getSiteConfigPreferences().getUiLoginFailureMessage();
                if (!StringUtils.isBlank(login_failure_message) && login_failure_message.contains("%d")) {
                    if (XDAT.getSiteConfigPreferences().getMaxFailedLogins() > 0) {
                        login_failure_message = String.format(login_failure_message, XDAT.getSiteConfigPreferences().getMaxFailedLogins());
                    } else {
                        logger.warn("Found login error message that contained a %d format placeholder, but the max failed login attempts is zero or less. Using the default login failure message instead.");
                        login_failure_message = "Login attempt failed. Please try again.";
                    }
                }
			} catch (Exception e) {
				logger.error("",e);
				login_failure_message="Login attempt failed. Please try again.";
			}
		}
		return login_failure_message;
	}

	public static void sendDisabledUserVerificationNotification(final UserI user, final Context context) throws Exception {
        context.put("time", Calendar.getInstance().getTime());
        context.put("server", TurbineUtils.GetFullServerPath());
        context.put("system", TurbineUtils.GetSystemName());
        context.put("user", user);

        final String body = populateVmTemplate(context, "/screens/email/DisabledUserVerification.vm");
        final String subject = "Disabled User Verified: " + user.getFirstname() + " " + user.getLastname();

		Map<String, Object> properties = new HashMap<>();
        properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
        properties.put(MailMessage.PROP_SUBJECT, subject);
        properties.put(MailMessage.PROP_HTML, body);
        XDAT.verifyNotificationType(NotificationType.Issue);
        XDAT.getNotificationService().createNotification(NotificationType.Issue.toString(), properties);

		sendAdminNotificationCopy(subject, body, NotificationType.Issue);
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

		boolean verificationRequired = XDAT.getSiteConfigPreferences().getEmailVerification();

		//If auto approval is false, send a notification to the administrator for each user we just verified.
		if(!XDAT.getSiteConfigPreferences().getUserRegistration()){
			// Send admin email
			AdminUtils.sendNewUserRequestNotification(username, firstName, lastName, email, comments, phone, organization, context);
		} else if((!verificationRequired) || user.isVerified()) {
			AdminUtils.sendNewUserCreationNotification(username, firstName, lastName, email, comments, phone, organization, context);
			AdminUtils.sendNewUserEmailMessage(username, email);
		}
	}

	public static void sendNewUserVerificationEmail(UserI user) throws Exception {
		// If the Item is null, don't continue.
		if (user == null) {
			throw new Exception("Unable to send verification email. Required User is null.");
		}
		sendNewUserVerificationEmail(user.getEmail(), user.getFirstname(), user.getLastname(), user.getLogin());
	}

	public static void sendNewUserVerificationEmail(String email, String firstName, String lastName, String userName) throws Exception {
		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			if ((email == null || email.equals("")) || (firstName == null || firstName.equals("")) ||
				(lastName == null || lastName.equals("")) || (userName == null || userName.equals(""))) {
				throw new Exception("Unable to send verification email. One or more required fields is empty.");
			}

			AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(userName);
			Context context = new VelocityContext();
			String fullName = firstName + " " + lastName;
			String verificationUrl = TurbineUtils.GetFullServerPath() + "/app/template/VerifyEmail.vm?a=" + token.getAlias() + "&s=" + token.getSecret();
			context.put("name", fullName);
			context.put("verifyEmailLink", verificationUrl);

			String subject = TurbineUtils.GetSystemName() + " Email Verification";
			String emailText = XDAT.getSiteConfigPreferences().getEmailVerificationMessage();
			if (emailText == null) {
				emailText = populateVmTemplate(context, "/screens/email/NewUserVerification.vm");
			}
			emailText = emailText.replaceAll("FULL_NAME", fullName);
			emailText = emailText.replaceAll("VERIFICATION_URL", verificationUrl);

			XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), email, subject, emailText);
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
			body = body.replaceAll("USER_USERNAME",username);
			body = body.replaceAll("SITE_URL",TurbineUtils.GetFullServerPath());
			body = body.replaceAll("SITE_NAME",TurbineUtils.GetSystemName());
			body = body.replaceAll("ADMIN_EMAIL",XDAT.getSiteConfigPreferences().getAdminEmail());

			UserI user = Users.getUser(username);
			body = body.replaceAll("USER_FIRSTNAME", user.getFirstname());
			body = body.replaceAll("USER_LASTNAME",user.getLastname());
			body = body.replaceAll("HELP_EMAIL",XDAT.getNotificationsPreferences().getHelpContactInfo());

			String subject = "Welcome to " + TurbineUtils.GetSystemName();

			XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), new String[] { email }, new String[] { XDAT.getSiteConfigPreferences().getAdminEmail() }, null, subject, body);
		}
	}

	public static boolean sendUserHTMLEmail(final String subject, final String message, final boolean ccAdmin, final String[] addresses) {
		if (XDAT.getNotificationsPreferences().getSmtpEnabled()) {
			if (addresses.length > 0) {
				final String from = XDAT.getSiteConfigPreferences().getAdminEmail();
				try {
					XDAT.getMailService().sendHtmlMessage(from, addresses, ccAdmin ? new String[]{from} : null, null, subject, message);
					return true;
				} catch (MessagingException exception) {
					logger.error("Unable to send mail", exception);
				}
			}
		}
		return false;
	}

	public static void sendErrorNotification(String message, Context context) throws Exception {
		UserI user = XDAT.getUserDetails();
		assert user != null;
		String email = user.getEmail();
		if (!StringUtils.isBlank(email)) {
		    context.put("time", Calendar.getInstance().getTime());
		    context.put("system", TurbineUtils.GetSystemName());
		    context.put("server", TurbineUtils.GetFullServerPath());
		    context.put("user", user.getLogin() + " (" + user.getFirstname() + " " + user.getLastname() + ")");
			context.put("error", message);

			final String subject = TurbineUtils.GetSystemName() + ": Error Thrown";
			final String body = populateVmTemplate(context, "/screens/email/ErrorReport.vm");

			try {
				// XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(), getErrorEmailIds(), TurbineUtils.GetSystemName() + ": Error Thrown", body);
				Map<String, Object> properties = new HashMap<>();
				properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
				properties.put(MailMessage.PROP_SUBJECT, subject);
                properties.put(MailMessage.PROP_HTML, body);
                XDAT.verifyNotificationType(NotificationType.Error);
                XDAT.getNotificationService().createNotification(NotificationType.Error.toString(), properties);
            } catch (Exception e1) {
				logger.error("Unable to send mail", e1);
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
			logger.error("Unable to send mail", exception);
		}
	}
	}

	public static void sendAdminEmail(String subject, String message) {
		sendAdminEmail(null, subject, message);
	}

	public static String populateVmTemplate(Context context, String templatePath) throws Exception {
        StringWriter writer = new StringWriter();
        Template template = Velocity.getTemplate(templatePath);
        template.merge(context, writer);
        return writer.toString();
    }

	public static UserI getAdminUser() {
		final SiteConfigPreferences preferences = XDAT.getSiteConfigPreferences();
		final String adminEmail = preferences != null ? preferences.getAdminEmail() : null;
		UserI fallback = null;
		for (final String login : Users.getAllLogins()) {
			try {
				final UserI user = Users.getUser(login);
				if (Roles.isSiteAdmin(user)) {
					if (adminEmail == null) {
						return user;
					}
					if (StringUtils.equalsIgnoreCase(user.getEmail(), adminEmail)) {
						return user;
					}
					fallback = user;
				}
			} catch (Exception ignored){

			}
		}
		return fallback;
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
	 * @param username    The username of the new user.
	 * @param email       The email of the new user.
	 * @param context     The data context.
	 * @throws Exception When something goes wrong.
	 * @see #sendNewUserCreationNotification(String, String, String, String, String, String, String, org.apache.velocity.context.Context)
	 */
	private static void sendNewUserRequestNotification(String username, String first, String last, String email, String comments, String phone, String lab, Context context) throws Exception {
		context.put("time", Calendar.getInstance().getTime());
		context.put("server", TurbineUtils.GetFullServerPath());
		context.put("system", TurbineUtils.GetSystemName());
		context.put("username", username);
		context.put("first", first);
		context.put("last", last);
		context.put("email", email);
		context.put("comments", comments);
		context.put("phone", phone);
		context.put("lab", lab);

		final String body = populateVmTemplate(context, "/screens/email/NewUserRequest.vm");
		final String subject = TurbineUtils.GetSystemName() + " New User Request: " + first + " " + last;

		final Map<String, Object> properties = new HashMap<>();
		properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
		properties.put(MailMessage.PROP_SUBJECT, subject);
		properties.put(MailMessage.PROP_HTML, body);
		XDAT.verifyNotificationType(NotificationType.NewUser);
		XDAT.getNotificationService().createNotification(NotificationType.NewUser.toString(), properties);

		sendAdminNotificationCopy(subject, body, NotificationType.NewUser);
	}

	/**
	 * Sends an email to subscribers for the {@link NotificationType#NewUser new user event} indicating that a new user
	 * account registration has been created. This notification is sent when a new user is created but auto-enable is
	 * turned on.
	 *
	 *
	 * @param username    The username of the new user.
	 * @param email       The email of the new user.
	 * @param context     The data context.
	 * @throws Exception When something goes wrong.
	 */
	private static void sendNewUserCreationNotification(String username, String first, String last, String email, String comments, String phone, String lab, Context context) throws Exception {
		context.put("time", Calendar.getInstance().getTime());
		context.put("server", TurbineUtils.GetFullServerPath());
		context.put("system", TurbineUtils.GetSystemName());
		context.put("username", username);
		context.put("first", first);
		context.put("last", last);
		context.put("email", email);
		context.put("comments", comments);
		context.put("phone", phone);
		context.put("lab", lab);

		final String body = populateVmTemplate(context, "/screens/email/NewUserNotification.vm");
		final String subject = "New User Created: " + first + " " + last;

		final Map<String, Object> properties = new HashMap<>();
		properties.put(MailMessage.PROP_FROM, XDAT.getSiteConfigPreferences().getAdminEmail());
		properties.put(MailMessage.PROP_SUBJECT, subject);
		properties.put(MailMessage.PROP_HTML, body);
		XDAT.verifyNotificationType(NotificationType.NewUser);
		XDAT.getNotificationService().createNotification(NotificationType.NewUser.toString(), properties);

		sendAdminNotificationCopy(subject, body, NotificationType.NewUser);
	}
}
