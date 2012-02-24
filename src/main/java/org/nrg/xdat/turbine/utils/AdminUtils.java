//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 28, 2005
 *
 */
package org.nrg.xdat.turbine.utils;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Iterator;

import javax.mail.MessagingException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.mail.api.NotificationSubscriberProvider;
import org.nrg.mail.api.NotificationType;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;

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

	/**
     * 
     */
	public AdminUtils() {
		super();
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
	 * Gets the Admin's Email Address.
	 * 
	 * @return admin email address
	 */
	public static String getAdminEmailId() {
		return XFT.GetAdminEmail();
	}

	/**
	 * Gets the Authorizer Email Id
	 * 
	 * @return Randy Buckner Email id
	 */
	public static String getAuthorizerEmailId() {
		if (authorizerEmailAddress == null) {
			try {
				ItemCollection items = ItemSearch.GetItems("xdat:user.assigned_roles.assigned_role.role_name", "Bossman", null, false);

				if (items.size() > 0) {
					int count = 0;
					@SuppressWarnings("rawtypes")
					Iterator iter = items.getItemIterator();
					while (iter.hasNext()) {
						if (count++ == 0)
							authorizerEmailAddress = ((ItemI) iter.next()).getStringProperty("email");
						else {
							authorizerEmailAddress += "," + ((ItemI) iter.next()).getStringProperty("email");
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
	 * Sends the Welcome email to a new User
	 * 
	 * @param username
	 * @param email
	 * @param context 
	 * @throws Exception 
	 */
	public static void sendNewUserRequestEmailMessage(String username, String firstname, String lastname, String email, String comments, String phone, String lab, Context context) throws Exception {
        context.put("time", Calendar.getInstance().getTime());
        context.put("server", TurbineUtils.GetFullServerPath());
        context.put("system", TurbineUtils.GetSystemName());
        context.put("username", username);
        context.put("firstname", firstname);
        context.put("lastname", lastname);
        context.put("email", email);
        context.put("comments", comments);
        context.put("phone", phone);
        context.put("lab", lab);
        
        String body = populateVmTemplate(context, "/screens/email/NewUserRequest.vm");

        String subject = TurbineUtils.GetSystemName() + " New User Request: " + firstname + " " + lastname;
		try {
			XDAT.getMailService().sendHtmlMessage(getAdminEmailId(), getNewUserEmailIds(), subject, body);
		} catch (MessagingException exception) {
			logger.error("Error sending new user request email", exception);
		}
	}

    /**
	 * Sends the Welcome email to a new User
	 * 
	 * @param username
	 * @param email
	 */

	public static void sendNewUserEmailMessage(String username, String email, RunData data, Context context) throws Exception {
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
	 * Constructs the body of the email sent to an Authorizer
	 * 
	 * @return body of authorization email
	 */

	public static String getAuthorizeRequestEmailBody(String UserName_AwaitingAuthorization, String login, RunData data) {
		String msg = "Authorization for new or updated access privilege has been requested for <b>" + UserName_AwaitingAuthorization + "</b>";
		msg += "<br><br> This user will not be able to access the requested resources until you have completed authorization. Please review the privileges <a href=\"" + TurbineUtils.GetFullServerPath()
				+ "/app/action/DisplayItemAction/search_element/xdat:user/search_field/xdat:user.login/search_value/" + login + "/\">here</a>.";
		msg += "<br><br> For help, contact  <a href=\"mailto:" + getAdminEmailId() + "?subject=" + TurbineUtils.GetSystemName() + " Assistance\">" + TurbineUtils.GetSystemName() + " Management </A>";
		return msg;
	}

	/**
	 * Sends the Authorization Request to Authorizer
	 * 
	 * @param UserId_AwaitingAuthrization
	 */

	public static void sendAuthorizationEmailMessage(XDATUser user, RunData data) {

		String from = getAdminEmailId();
		String[] tos = StringUtils.split(getAuthorizerEmailId(), ", ");
		String[] ccs = AdminUtils.GetNewUserRegistrationsEmail() ? new String[] { from } : null;
		String subject = TurbineUtils.GetSystemName() + ": Authorization Request";
		String body = getAuthorizeRequestEmailBody(user.getFirstname() + " " + user.getLastname(), user.getUsername(), data);
		try {
			XDAT.getMailService().sendHtmlMessage(from, tos, ccs, null, subject, body);
		} catch (MessagingException exception) {
			logger.error("Unable to send mail", exception);
		}
	}

	/**
	 * Sends an email to the user saying Authorization complete and the user can
	 * log on to system.
	 * 
	 * @param user
	 * @param data
	 * @param context
	 * @throws Exception
	 */
	public static void sendUserAuthorizedEmailMessage(XDATUser user, RunData data, Context context) throws Exception {

		String email = user.getEmail();
		if (!StringUtils.isBlank(email)) {
			String from = getAdminEmailId();
			String subject = TurbineUtils.GetSystemName() + ": Authorization Complete";
		    context.put("username", user.getUsername());
	        context.put("server", TurbineUtils.GetFullServerPath());
	        context.put("system", TurbineUtils.GetSystemName());
	        context.put("admin_email", AdminUtils.getAdminEmailId());
	        String body = populateVmTemplate(context, "/screens/email/user_authorization.vm");
			XDAT.getMailService().sendHtmlMessage(from, email, subject, body);
		}
	}

	public static boolean sendUserHTMLEmail(String subject, String message, boolean ccAdmin, RunData data) {
		boolean successful = false;
		String email = TurbineUtils.getUser(data).getEmail();
		if (StringUtils.isBlank(email)) {
			String from = getAdminEmailId();
			try {
				XDAT.getMailService().sendHtmlMessage(from, new String[] { email }, ccAdmin ? new String[] { from } : null, null, subject, message);
			} catch (MessagingException exception) {
				logger.error("Unable to send mail", exception);
				successful = false;
			}
		} else {
			successful = false;
		}

		return successful;
	}

	public static void sendErrorEmail(RunData data, String e, Context context) throws Exception {
		XDATUser user = TurbineUtils.getUser(data);
		String email = user.getEmail();
		if (!StringUtils.isBlank(email)) {
		    context.put("time", Calendar.getInstance().getTime());
		    context.put("system", TurbineUtils.GetSystemName());
		    context.put("server", TurbineUtils.GetFullServerPath());
		    context.put("user", user.getLogin() + " (" + user.getFirstname() + " " + user.getLastname() + ")");
			context.put("error", e.toString());

			String body = populateVmTemplate(context, "/screens/email/ErrorReport.vm");

			try {
				XDAT.getMailService().sendHtmlMessage(getAdminEmailId(), getErrorEmailIds(), TurbineUtils.GetSystemName() + ": Error Thrown", body);
			} catch (Exception e1) {
				logger.error("Unable to send mail", e1);
			}
		}
	}

	public static void sendAdminEmail(UserI user, String subject, String message) {
		String admin = getAdminEmailId();
		String qualifiedSubject = TurbineUtils.GetSystemName() + ": " + subject;

		StringBuffer formattedMessage = new StringBuffer();
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

    private static String populateVmTemplate(Context context, String templatePath) throws ResourceNotFoundException, ParseErrorException, Exception, MethodInvocationException {
        StringWriter writer = new StringWriter();
        Template template = Velocity.getTemplate(templatePath);
        template.merge(context, writer);
        return writer.toString();
    }

    private static String[] getErrorEmailIds() {
        return getNotificationTypeAddresses(NotificationType.Error);
    }

    private static String[] getIssueEmailIds() {
        return getNotificationTypeAddresses(NotificationType.Issue);
    }
    
    private static String[] getNewUserEmailIds() {
        return getNotificationTypeAddresses(NotificationType.NewUser);
    }
    
    private static String[] getUpdateEmailIds() {
        return getNotificationTypeAddresses(NotificationType.Update);
    }
    
    private static String[] getNotificationTypeAddresses(NotificationType type) {
        if (_provider == null) {
            return new String[] { getAdminEmailId() };
        }
        String[] addresses = _provider.getSubscribers(type);
        return ArrayUtils.isEmpty(addresses) ? new String[] { getAdminEmailId() } : addresses;
    }
}
