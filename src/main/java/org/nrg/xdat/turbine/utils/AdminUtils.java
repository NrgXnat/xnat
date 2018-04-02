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
import org.nrg.mail.api.MailMessage;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xft.security.UserI;

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
        context.put("time", Calendar.getInstance().getTime());
        context.put("server", TurbineUtils.GetFullServerPath());
        context.put("siteLogoPath", XDAT.getSiteLogoPath());
        context.put("system", TurbineUtils.GetSystemName());
        context.put("user", user);

        final String body = populateVmTemplate(context, "/screens/email/DisabledUserVerification.vm");
        final String subject = "Expired User reverified: " + user.getFirstname() + " " + user.getLastname();

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

			final String subject = TurbineUtils.GetSystemName() + " Email Verification";
			final String emailText = StringUtils.defaultIfBlank(XDAT.getSiteConfigPreferences().getEmailVerificationMessage(), populateVmTemplate(context, "/screens/email/NewUserVerification.vm"));
			XDAT.getMailService().sendHtmlMessage(XDAT.getSiteConfigPreferences().getAdminEmail(),
												  email,
												  subject,
												  emailText.replaceAll("FULL_NAME", fullName)
														   .replaceAll("VERIFICATION_URL", verificationUrl)
														   .replaceAll("SITE_NAME", TurbineUtils.GetSystemName()));
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
		if (!StringUtils.isBlank(email)) {
		    context.put("time", Calendar.getInstance().getTime());
		    context.put("system", TurbineUtils.GetSystemName());
			context.put("siteLogoPath", XDAT.getSiteLogoPath());
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
	 * @param username    The username of the new user.
	 * @param email       The email of the new user.
	 * @param context     The data context.
	 * @throws Exception When something goes wrong.
	 * @see #sendNewUserCreationNotification(String, String, String, String, String, String, String, org.apache.velocity.context.Context)
	 */
	private static void sendNewUserRequestNotification(String username, String first, String last, String email, String comments, String phone, String lab, Context context) throws Exception {
		context.put("time", Calendar.getInstance().getTime());
		context.put("server", TurbineUtils.GetFullServerPath());
		context.put("siteLogoPath", XDAT.getSiteLogoPath());
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
		context.put("siteLogoPath", XDAT.getSiteLogoPath());
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
}
