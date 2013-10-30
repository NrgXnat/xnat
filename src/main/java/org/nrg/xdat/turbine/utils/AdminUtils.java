//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 28, 2005
 *
 */
package org.nrg.xdat.turbine.utils;

import java.io.StringWriter;
import java.util.*;

import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.mail.api.MailMessage;
import org.nrg.xdat.XDAT;
import org.nrg.mail.api.NotificationSubscriberProvider;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.entities.AliasToken;
import org.nrg.xdat.entities.UserRegistrationData;
import org.nrg.xdat.om.XdatUser;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.services.AliasTokenService;
import org.nrg.xdat.services.UserRegistrationDataService;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.AuthUtils;

/**
 * @author Tim
 * 
 */
public class AdminUtils {
	static Logger logger = Logger.getLogger(AdminUtils.class);
	private static String authorizerEmailAddress = null;
	private static boolean NEW_USER_REGISTRATIONS = true;
	private static boolean PAGE_EMAIL = true;
    private static NotificationSubscriberProvider _provider;

    private static String login_failure_message=null;
    
	/**
     * 
     */
	public AdminUtils() {
		super();
	}
	
	public static String GetLoginFailureMessage(){
		if(login_failure_message==null){
			try {
				login_failure_message=XDAT.getSiteConfigurationProperty("UI.login_failure_message", "Login attempt failed. Please try again.");
                if (!StringUtils.isBlank(login_failure_message) && login_failure_message.contains("%d")) {
                    if (AuthUtils.MAX_FAILED_LOGIN_ATTEMPTS > 0) {
                        login_failure_message = String.format(login_failure_message, AuthUtils.MAX_FAILED_LOGIN_ATTEMPTS);
                    } else {
                        logger.warn("Found login error message that contained a %d format placeholder, but the max failed login attempts is zero or less. Using the default login failure message instead.");
                        login_failure_message = "Login attempt failed. Please try again.";
                    }
                }
			} catch (ConfigServiceException e) {
				logger.error("",e);
				login_failure_message="Login attempt failed. Please try again.";
			}
		}
		return login_failure_message;
	}

	public static void SetNewUserRegistrationsEmail(boolean b) {
		NEW_USER_REGISTRATIONS = b;
	}

	public static void SetPageEmail(boolean b) {
		PAGE_EMAIL = b;
	}

	public static boolean GetNewUserRegistrationsEmail() {
		return NEW_USER_REGISTRATIONS;
	}

	public static boolean GetPageEmail() {
		return PAGE_EMAIL;
	}

	/**
	 * Gets the administrator's email address.
	 * 
	 * @return The administrator's email address.
	 */
	public static String getAdminEmailId() {
		return XFT.GetAdminEmail();
	}

	/**
	 * Gets the Authorizer Email Id
	 * 
		* @return Email id   
	 */
	public static String getAuthorizerEmailId() {
		if (authorizerEmailAddress == null) {
			try {
				ItemCollection items = ItemSearch.GetItems("xdat:user.assigned_roles.assigned_role.role_name", "Bossman", null, false);

				if (items.size() > 0) {
					int count = 0;
					@SuppressWarnings("rawtypes")
					Iterator iterator = items.getItemIterator();
					while (iterator.hasNext()) {
						if (count++ == 0)
							authorizerEmailAddress = ((ItemI) iterator.next()).getStringProperty("email");
						else {
							authorizerEmailAddress += "," + ((ItemI) iterator.next()).getStringProperty("email");
						}
					}
				} else {
					authorizerEmailAddress = getAdminEmailId();
				}
			} catch (XFTInitException e) {
				logger.error("", e);
				authorizerEmailAddress = getAdminEmailId();
			} catch (ElementNotFoundException e) {
				logger.error("", e);
				authorizerEmailAddress = getAdminEmailId();
			} catch (FieldNotFoundException e) {
				logger.error("", e);
				authorizerEmailAddress = getAdminEmailId();
			} catch (Exception e) {
				logger.error("", e);
				authorizerEmailAddress = getAdminEmailId();
			}

		}
		return authorizerEmailAddress;
	}

	/**
	 * Gets the MailServer to be used
	 * 
	 * @return MailServer
	 */

	public static String getMailServer() {
		return XFT.GetAdminEmailHost();
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
     * @throws Exception
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

        String body = populateVmTemplate(context, "/screens/email/NewUserRequest.vm");
        String subject = TurbineUtils.GetSystemName() + " New User Request: " + first + " " + last;

        AdminUtils.sendAdminEmail(subject, body);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(MailMessage.PROP_FROM, getAdminEmailId());
        properties.put(MailMessage.PROP_SUBJECT, subject);
        properties.put(MailMessage.PROP_HTML, body);
        XDAT.verifyNotificationType(NotificationType.NewUser);
        XDAT.getNotificationService().createNotification(NotificationType.NewUser.toString(), properties);
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
     * @throws Exception
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

        String body = populateVmTemplate(context, "/screens/email/NewUserNotification.vm");
        String subject = TurbineUtils.GetSystemName() + " New User Created: " + first + " " + last;

        AdminUtils.sendAdminEmail(subject, body);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(MailMessage.PROP_FROM, getAdminEmailId());
        properties.put(MailMessage.PROP_SUBJECT, subject);
        properties.put(MailMessage.PROP_HTML, body);
        XDAT.verifyNotificationType(NotificationType.NewUser);
        XDAT.getNotificationService().createNotification(NotificationType.NewUser.toString(), properties);
	}

    public static void sendNewUserNotification(final XDATUser user, final Context context) throws Exception {
        UserRegistrationData regData = XDAT.getContextService().getBean(UserRegistrationDataService.class).getUserRegistrationData(user);
        String comments = "";
        String phone = "";
        String organization = "";

        // regData will be null if the user was created by an admin (via admin > users > add user)
        if(null != regData){
            phone = regData.getPhone();
            organization = regData.getOrganization();
            comments = regData.getComments();
        }
        sendNewUserNotification(user, comments, phone, organization, context);
    }

    public static void sendNewUserNotification(final XDATUser user, final String comments, final String phone, final String organization, final Context context) throws Exception {
        final String username = user.getUsername();
        final String firstName = user.getFirstname();
        final String lastName = user.getLastname();
        final String email = user.getEmail();

        //If auto approval is false, send a notification to the administrator for each user we just verified.
        if(!XFT.GetUserRegistration()){
            // Send admin email
            AdminUtils.sendNewUserRequestNotification(username, firstName, lastName, email, comments, phone, organization, context);
        } else {
            AdminUtils.sendNewUserCreationNotification(username, firstName, lastName, email, comments, phone, organization, context);
        }
    }

   public static void sendNewUserVerificationEmail(XdatUser user) throws Exception {
      // If the Item is null, don't continue.
      if(user == null){ throw new Exception("Unable to send verification email. Required User is null."); }
      sendNewUserVerificationEmail(user.getEmail(), user.getFirstname(), user.getLastname(), user.getLogin());
   }
   
   public static void sendNewUserVerificationEmail(ItemI i) throws Exception {
      // If the Item is null, don't continue.
      if(i == null){ throw new Exception("Unable to send verification email. Required Item is null."); }
      sendNewUserVerificationEmail((String)i.getProperty("email"), (String)i.getProperty("firstName"), 
                                   (String)i.getProperty("lastName"), (String)i.getProperty("login"));
   }
   
   public static void sendNewUserVerificationEmail(String email, String firstName, String lastName, String userName) throws Exception{

       if((email == null || email.equals("")) || (firstName == null || firstName.equals("")) ||
          (lastName == null || lastName.equals("")) || (userName == null || userName.equals("")))
       {
          throw new Exception("Unable to send verification email. One or more required fields is empty.");
       }
       
       AliasToken token = XDAT.getContextService().getBean(AliasTokenService.class).issueTokenForUser(userName);
       Context context = new VelocityContext();
       context.put("name", firstName + lastName);
       context.put("verifyEmailLink", TurbineUtils.GetFullServerPath() + "/app/template/VerifyEmail.vm?a=" + token.getAlias() + "&s=" + token.getSecret());

       String subject = TurbineUtils.GetSystemName() + " Email Verification";
       String text = populateVmTemplate(context, "/screens/email/NewUserVerification.vm");
       XDAT.getMailService().sendHtmlMessage(AdminUtils.getAdminEmailId(), email, subject, text);
   }

    /**
	 * Sends the Welcome email to a new User
	 * 
	 * @param username    The username of the new user.
	 * @param email       The email  of the new user.
	 */

	public static void sendNewUserEmailMessage(String username, String email, Context context) throws Exception {
        context.put("username", username);
        context.put("server", TurbineUtils.GetFullServerPath());
        context.put("system", TurbineUtils.GetSystemName());
        context.put("admin_email", AdminUtils.getAdminEmailId());

        String body = populateVmTemplate(context, "/screens/email/WelcomeNewUser.vm");
        String subject = "Welcome to " + TurbineUtils.GetSystemName();

        if (AdminUtils.GetNewUserRegistrationsEmail()) {
			XDAT.getMailService().sendHtmlMessage(getAdminEmailId(), new String[] { email }, new String[] { getAdminEmailId() }, null, subject, body);
		} else {
			XDAT.getMailService().sendHtmlMessage(getAdminEmailId(), email, subject, body);
		}
	}

	/**
	 * Constructs the body of the email sent to an authorizer
	 * 
	 * @return body of authorization email
	 */

	public static String getAuthorizeRequestEmailBody(String UserName_AwaitingAuthorization, String login) {
		String msg = "Authorization for new or updated access privilege has been requested for <b>" + UserName_AwaitingAuthorization + "</b>";
		msg += "<br><br> This user will not be able to access the requested resources until you have completed authorization. Please review the privileges <a href=\"" + TurbineUtils.GetFullServerPath()
				+ "/app/action/DisplayItemAction/search_element/xdat:user/search_field/xdat:user.login/search_value/" + login + "/\">here</a>.";
		msg += "<br><br> For help, contact  <a href=\"mailto:" + getAdminEmailId() + "?subject=" + TurbineUtils.GetSystemName() + " Assistance\">" + TurbineUtils.GetSystemName() + " Management </A>";
		return msg;
	}

	/**
	 * Sends the Authorization Request to authorizer
	 * 
	 * @param user    The user to be authorized.
	 */

	public static void sendAuthorizationEmailMessage(XDATUser user) {

		String from = getAdminEmailId();
		String[] tos = StringUtils.split(getAuthorizerEmailId(), ", ");
		String[] ccs = AdminUtils.GetNewUserRegistrationsEmail() ? new String[] { from } : null;
		String subject = TurbineUtils.GetSystemName() + ": Authorization Request";
		String body = getAuthorizeRequestEmailBody(user.getFirstname() + " " + user.getLastname(), user.getUsername());
		try {
			XDAT.getMailService().sendHtmlMessage(from, tos, ccs, null, subject, body);
		} catch (MessagingException exception) {
			logger.error("Unable to send mail", exception);
		}
	}

    public static boolean sendUserHTMLEmail(String subject, String message, boolean ccAdmin, String[] email_addresses) {
		boolean successful = false;
		if (email_addresses.length>0) {
			String from = getAdminEmailId();
			try {
				XDAT.getMailService().sendHtmlMessage(from, email_addresses, ccAdmin ? new String[] { from } : null, null, subject, message);
			} catch (MessagingException exception) {
				logger.error("Unable to send mail", exception);
				successful = false;
			}
		} else {
			successful = false;
		}

		return successful;
	}

	public static void sendErrorNotification(RunData data, String message, Context context) throws Exception {
		XDATUser user = TurbineUtils.getUser(data);
		String email = user.getEmail();
		if (!StringUtils.isBlank(email)) {
		    context.put("time", Calendar.getInstance().getTime());
		    context.put("system", TurbineUtils.GetSystemName());
		    context.put("server", TurbineUtils.GetFullServerPath());
		    context.put("user", user.getLogin() + " (" + user.getFirstname() + " " + user.getLastname() + ")");
			context.put("error", message);

			String body = populateVmTemplate(context, "/screens/email/ErrorReport.vm");

			try {
				// XDAT.getMailService().sendHtmlMessage(getAdminEmailId(), getErrorEmailIds(), TurbineUtils.GetSystemName() + ": Error Thrown", body);
                Map<String, Object> properties = new HashMap<String, Object>();
                properties.put(MailMessage.PROP_FROM, getAdminEmailId());
                properties.put(MailMessage.PROP_SUBJECT, TurbineUtils.GetSystemName() + ": Error Thrown");
                properties.put(MailMessage.PROP_HTML, body);
                XDAT.verifyNotificationType(NotificationType.Error);
                XDAT.getNotificationService().createNotification(NotificationType.Error.toString(), properties);
            } catch (Exception e1) {
				logger.error("Unable to send mail", e1);
			}
		}
	}

	public static void sendAdminEmail(UserI user, String subject, String message) {
		String admin = getAdminEmailId();
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

	public static void sendAdminEmail(String subject, String message) {
		sendAdminEmail(null, subject, message);
	}
	
	public static NotificationSubscriberProvider getNotificationSubscriberProvider() {
	    return _provider;
	}
	
	public static void setNotificationSubscriberProvider(NotificationSubscriberProvider provider) {
	    _provider = provider;
	}

    public static String populateVmTemplate(Context context, String templatePath) throws Exception {
        StringWriter writer = new StringWriter();
        Template template = Velocity.getTemplate(templatePath);
        template.merge(context, writer);
        return writer.toString();
    }
}
