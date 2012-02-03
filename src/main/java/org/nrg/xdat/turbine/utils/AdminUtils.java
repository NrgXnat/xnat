//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on Jun 28, 2005
 *
 */
package org.nrg.xdat.turbine.utils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.email.EmailUtils;
import org.nrg.xft.email.EmailerI;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.StringUtils;

/**
 * @author Tim
 *
 */
public class AdminUtils {
	static Logger logger = Logger.getLogger(AdminUtils.class);
    private static String authorizerEmailAddress = null;
    private static boolean NEW_USER_REGISTRATIONS=true;
    private static boolean PAGE_EMAIL =true;
    /**
     * 
     */
    public AdminUtils() {
        super();
    }

    public static void SetNewUserRegistrationsEmail(boolean b){
        NEW_USER_REGISTRATIONS=b;
    }

    public static void SetPageEmail(boolean b){
        PAGE_EMAIL=b;
    }
    
    public static boolean GetNewUserRegistrationsEmail(){
        return NEW_USER_REGISTRATIONS;
    }
    
    public static boolean GetPageEmail(){
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
		* @return Email id   
		*/	
	 public static String getAuthorizerEmailId() {
			if (authorizerEmailAddress ==null) {
				try {
                    ItemCollection items = ItemSearch.GetItems("xdat:user.assigned_roles.assigned_role.role_name","Bossman",null,false);
                    
                    if (items.size()>0)
                    {
                        int count = 0;
                        Iterator iter = items.getItemIterator();
                        while (iter.hasNext())
                        {
                            if (count++ == 0)
                                authorizerEmailAddress = ((ItemI)iter.next()).getStringProperty("email");
                            else{
                                authorizerEmailAddress += "," + ((ItemI)iter.next()).getStringProperty("email");
                            }
                        }
                    }else{
                        authorizerEmailAddress = getAdminEmailId();
                    }
                } catch (XFTInitException e) {
                    logger.error("",e);
                    authorizerEmailAddress = getAdminEmailId();
                } catch (ElementNotFoundException e) {
                    logger.error("",e);
                    authorizerEmailAddress = getAdminEmailId();
                } catch (FieldNotFoundException e) {
                    logger.error("",e);
                    authorizerEmailAddress = getAdminEmailId();
                } catch (Exception e) {
                    logger.error("",e);
                    authorizerEmailAddress = getAdminEmailId();
                }
                
			}
			return authorizerEmailAddress;
		 }


	 public static String getNewUserEmailBody(String UserName,RunData data,Context context) throws Exception{
	 	context.put("username",UserName);
        context.put("server",TurbineUtils.GetFullServerPath());
        context.put("system",TurbineUtils.GetSystemName());
        context.put("admin_email",AdminUtils.getAdminEmailId());
        StringWriter sw = new StringWriter();
        Template template =Velocity.getTemplate("/screens/email/WelcomeNewUser.vm");
        template.merge(context,sw);
	 	return sw.toString();
	 }
	
	/** 
	 * Constructs the body of the email sent to a  user once Authorization is complete. Invoked for welcome emails
	 * 
	 * @return body of welcome email
	 */

	 public static String getUserAuthorizedEmailBody(String UserName, RunData data, Context context) throws Exception {
		 context.put("username",UserName);
        context.put("server",TurbineUtils.GetFullServerPath());
        context.put("system",TurbineUtils.GetSystemName());
        context.put("admin_email",AdminUtils.getAdminEmailId());
        StringWriter sw = new StringWriter();
        Template template =Velocity.getTemplate("/screens/email/user_authorization.vm");
        template.merge(context,sw);
	 	return sw.toString();
	 }
		 
//	public static String GetServer()
//	{
//		String s = "";
//
//		s = Turbine.getServerScheme() + "://" + Turbine.getServerName() + ":" + Turbine.getServerPort() + Turbine.getContextPath();
//
//		if (XFT.VERBOSE)
//	        System.out.println("Server:" + s);
//		return s;
//	}
	
	
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
      * @param UserName
      * @param Email
      */
     
     public static void sendNewUserRequestEmailMessage(String UserName,String firstname, String lastname, String Email,String comments,String phone, String lab) {
        String msgBody = getNewUserRequestEmailBody(UserName,firstname,lastname,Email,comments,phone,lab);

        try {
        	EmailerI sm = EmailUtils.getEmailer();
            sm.setFrom(getAdminEmailId());
            InternetAddress ia = new InternetAddress(getAdminEmailId());
            ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
            al.add(ia);
            sm.setTo(al);
            sm.setSubject(TurbineUtils.GetSystemName() +" New User Request: " + firstname + " " + lastname);
            sm.setMsg(msgBody);
            
            sm.send();
        } catch (Exception e) {
                logger.error("Unable to send mail",e);
                System.out.println("Error sending Email");
        }
     }
	 
	 /**
	  * Sends the Welcome email to a new User 
	  * 
	  * @param UserName
	  * @param Email
	  */
	 
	 public static void sendNewUserEmailMessage(String UserName, String Email,RunData data,Context context) throws Exception {
		String msgBody = getNewUserEmailBody(UserName,data,context);

		try {
        	EmailerI sm = EmailUtils.getEmailer();
			sm.setFrom(getAdminEmailId());
			InternetAddress ia = new InternetAddress(Email);
			ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
			al.add(ia);
			sm.setTo(al);

			if (AdminUtils.GetNewUserRegistrationsEmail()) {
	            ia = new InternetAddress(getAdminEmailId());
	            al = new ArrayList<InternetAddress>();
	            al.add(ia);
	            sm.setCc(al);
			}
				 
//            ia = new InternetAddress(getAdminEmailId());
//            al = new ArrayList();
//            al.add(ia);
//            sm.setBcc(al);
			sm.setSubject("Welcome to " + TurbineUtils.GetSystemName());
			sm.setMsg(msgBody);
			
             sm.send();
         } catch (Exception e) {
			    logger.error("Unable to send mail",e);
			    System.out.println("Error sending Email");
         }
	 }
     
        public static String getNewUserRequestEmailBody(String username, String firstname, String lastname,String email, String comments,String phone, String lab)
        {
            String msg = "";
            msg +="<html><body>";
            msg +="<b>New User Request</b><br><br>";
            Date d = Calendar.getInstance().getTime();
            msg +="<b>Date:</b> " + d + "<br>";
            msg +="<b>Site:</b> " + TurbineUtils.GetSystemName() + "<br>";
            msg +="<b>Host:</b> " + TurbineUtils.GetFullServerPath() + "<br>";
            msg +="<b>Username:</b> " + username + "<br>";
            msg +="<b>Firstname:</b> " + firstname + "<br>";
            msg +="<b>Lastname:</b> " + lastname + "<br>";
            msg +="<b>Phone:</b> " + phone + "<br>";
            msg +="<b>Lab:</b> " + lab + "<br>";
            msg +="<b>Email:</b> " + email + "<br><br><br>";
            msg +="This user's account has been created but will be disabled until you enable the account.<br><br><br>";
            
            msg +="<a href=\"" + TurbineUtils.GetFullServerPath() + "/app/action/DisplayItemAction/search_value/" + username + "/search_element/xdat:user/search_field/xdat:user.login\">Review and Enable</a><br><br><br>";

            msg +="<b>User Comments:</b> " + comments + "<br><br><br>";
            msg +=d + "," + username + "," + firstname + "," + lastname + "," + phone + "," + lab + "," + email;
            msg +="</body></html>";
            return msg;
        }
	

	/** 
	 * Constructs the body of the email sent to an Authorizer
	 * 
	 * @return body of authorization email
	 */
	 
	 public static String getAuthorizeRequestEmailBody(String UserName_AwaitingAuthorization, String login, RunData data) {
		String msg = "Authorization for new or updated access privilege has been requested for <b>" + UserName_AwaitingAuthorization +"</b>";
		msg += "<br><br> This user will not be able to access the requested resources until you have completed authorization. Please review the privileges <a href=\""+ TurbineUtils.GetFullServerPath()+"/app/action/DisplayItemAction/search_element/xdat:user/search_field/xdat:user.login/search_value/"+ login +"/\">here</a>.";
		msg += "<br><br> For help, contact  <a href=\"mailto:" + getAdminEmailId() + "?subject=" + TurbineUtils.GetSystemName() + " Assistance\">" + TurbineUtils.GetSystemName() + " Management </A>";
		return msg;
	 }

	/**
	 * Sends the Authorization Request to Authorizer 
	 * 
	 * @param UserId_AwaitingAuthrization
	 */
	 
	public static void sendAuthorizationEmailMessage(XDATUser user,RunData data) {

		String msgBody = getAuthorizeRequestEmailBody(user.getFirstname() + " "+ user.getLastname(),user.getUsername(),data);
		try {
        	EmailerI sm = EmailUtils.getEmailer();
		sm.setFrom(getAdminEmailId());
		
		//BUILD ADDRESS ARRAY
		ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
		Iterator<String> iter = StringUtils.CommaDelimitedStringToArrayList(getAuthorizerEmailId()).iterator();
		while (iter.hasNext())
		{
		    String s = iter.next();
		    InternetAddress ia = new InternetAddress();
			ia.setAddress(s);
			al.add(ia);
		}
		sm.setTo(al);
		
//		BUILD ADDRESS ARRAY
		al = new ArrayList<InternetAddress>();

        if(AdminUtils.GetNewUserRegistrationsEmail()){
    		iter = StringUtils.CommaDelimitedStringToArrayList(getAdminEmailId()).iterator();
    		while (iter.hasNext())
    		{
    		    String s = (String)iter.next();
    		    InternetAddress ia = new InternetAddress();
    			ia.setAddress(s);
    			al.add(ia);
    		}
    		sm.setCc(al);
        }
		sm.setSubject(TurbineUtils.GetSystemName() + ": Authorization Request");
		sm.setMsg(msgBody);
		
            sm.send();
        } catch (Exception e) {
		    logger.error("Unable to send mail",e);
		    System.out.println("Error sending Email");
        }	
	}
	 
	/**
		 * Sends the User an email saying Authorization complete and the user can log on to system 
		 * 
		 * @param UserId_AwaitingAuthrization
		 */
	 
		public static void sendUserAuthorizedEmailMessage(XDATUser user,RunData data,Context context) throws Exception {

			if (user.getEmail()!=null && user.getEmail()!="") {
			String msgBody = getUserAuthorizedEmailBody(user.getUsername(),data,context);

			try {
            	EmailerI sm = EmailUtils.getEmailer();
				sm.setFrom(getAdminEmailId());
				InternetAddress ia = new InternetAddress(user.getEmail());
				List<InternetAddress> al = new ArrayList<InternetAddress>();
				al.add(ia);
				sm.setTo(al);
				sm.setSubject(TurbineUtils.GetSystemName() + ": Authorization Complete");
				sm.setMsg(msgBody);
				
	             sm.send();
	         } catch (Exception e) {
				    logger.error("Unable to send mail",e);
				    System.out.println("Error sending Email");
	         }
			}		
		}
        
        public static boolean sendUserHTMLEmail(String subject, String msg, boolean ccAdmin, RunData data)
        {
            boolean successful = false;
            XDATUser user =TurbineUtils.getUser(data);
            if (user.getEmail()!=null && user.getEmail()!="") {
                try {
                	EmailerI sm = EmailUtils.getEmailer();
                    sm.setFrom(getAdminEmailId());
                    InternetAddress ia = new InternetAddress(user.getEmail());
                    ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
                    al.add(ia);
                    sm.setTo(al);
                    if (ccAdmin){
                        ia = new InternetAddress(getAdminEmailId());
                        al = new ArrayList<InternetAddress>();
                        al.add(ia);
                        sm.setCc(al);
                    }
                    sm.setSubject(subject);
                    sm.setMsg(msg);
                    
                     sm.send();
                     successful = true;
                 } catch (Exception e) {
                        logger.error("Unable to send mail",e);
                        System.out.println("Error sending Email");
                        successful = false;
                 }
             }else{
                 successful = false;
             }
            
            return successful;
        }

        public static void sendErrorEmail(RunData data, String e){
            XDATUser user = TurbineUtils.getUser(data);
            if (user.getEmail()!=null && user.getEmail()!="") {

                try {
                	EmailerI sm = EmailUtils.getEmailer();
                    sm.setFrom(getAdminEmailId());
                    InternetAddress ia = new InternetAddress(getAdminEmailId());
                    ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
                    al.add(ia);
                    sm.setTo(al);
                    sm.setSubject(TurbineUtils.GetSystemName() + ": Error Thrown");
                    
                    StringBuffer sb = new StringBuffer();
                    sb.append("<B>Error Report</B><BR>");
                    sb.append("HOST: ").append(TurbineUtils.GetFullServerPath()).append("<BR>");
                    sb.append("USER: ").append(user.getLogin()).append("(").append(user.getFirstname()).append(" ").append(user.getLastname()).append(")").append("<BR>");
                    sb.append("TIME: ").append(java.util.Calendar.getInstance().getTime()).append("<BR>");
                    sb.append("ERROR: ").append(e.toString()).append("<BR>");
                    
                    sm.setMsg(sb.toString());
                    
                     sm.send();
                 } catch (Exception e1) {
                        logger.error("Unable to send mail",e1);
                        System.out.println("Error sending Email");
                 }
                }   
        }
        
        public static void sendAdminEmail(UserI user, String subject, String message){
                try {
                	EmailerI sm = EmailUtils.getEmailer();
                    sm.setFrom(getAdminEmailId());
                    InternetAddress ia = new InternetAddress(getAdminEmailId());
                    ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
                    al.add(ia);
                    sm.setTo(al);
                    sm.setSubject(TurbineUtils.GetSystemName() + ": " +subject);
                    
                    StringBuffer sb = new StringBuffer();
                    sb.append("HOST: ").append(TurbineUtils.GetFullServerPath()).append("<BR>");
                    if(user!=null)sb.append("USER: ").append(user.getUsername()).append("(").append(user.getFirstname()).append(" ").append(user.getLastname()).append(")").append("<BR>");
                    sb.append("TIME: ").append(java.util.Calendar.getInstance().getTime()).append("<BR>");
                    sb.append("MESSAGE: ").append(message).append("<BR>");
                    
                    sm.setMsg(sb.toString());
                    
                     sm.send();
                 } catch (Exception e1) {
                        logger.error("Unable to send mail",e1);
                        System.out.println("Error sending Email");
                 }
        }
        
        public static void sendAdminEmail(String subject, String message){

                try {
                	EmailerI sm = EmailUtils.getEmailer();
                    sm.setFrom(getAdminEmailId());
                    InternetAddress ia = new InternetAddress(getAdminEmailId());
                    ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
                    al.add(ia);
                    sm.setTo(al);
                    sm.setSubject(TurbineUtils.GetSystemName() + ": " +subject);
                    
                    StringBuffer sb = new StringBuffer();
                    sb.append("HOST: ").append(TurbineUtils.GetFullServerPath()).append("<BR>");
                    sb.append("TIME: ").append(java.util.Calendar.getInstance().getTime()).append("<BR>");
                    sb.append("MESSAGE: ").append(message).append("<BR>");
                    
                    sm.setMsg(sb.toString());
                    
                     sm.send();
                 } catch (Exception e1) {
                        logger.error("Unable to send mail",e1);
                        System.out.println("Error sending Email");
                 }
        }
}
