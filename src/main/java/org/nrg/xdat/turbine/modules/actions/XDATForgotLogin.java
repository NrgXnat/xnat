//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Dec 12, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.email.EmailUtils;
import org.nrg.xft.email.EmailerI;
import org.nrg.xft.search.ItemSearch;

public class XDATForgotLogin extends VelocitySecureAction {
    static Logger logger = Logger.getLogger(XDATForgotLogin.class);

    public void additionalProcessing(RunData data, Context context,XDATUser user) throws Exception{
    	
    }
    
    @Override
    public void doPerform(RunData data, Context context) throws Exception {
        String email = data.getParameters().getString("email");
        String username = data.getParameters().getString("username");
        if (email != null && !email.equals(""))
        {
            //check email
            ItemSearch search = new ItemSearch();
            search.setAllowMultiples(false);
            search.setElement("xdat:user");
            search.addCriteria("xdat:user.email",email);

            ItemI temp = search.exec().getFirst();
            if (temp==null){
                data.setMessage("Unknown email address.");
                data.setScreenTemplate("ForgotLogin.vm");
                return;
            }else{
                XDATUser newUser = new XDATUser(temp,false);
                
                try{
                	additionalProcessing(data, context, newUser);
                }catch(Exception e){
                    logger.error(e);
                }
            	
                try {
                	EmailerI sm = EmailUtils.getEmailer();
                    sm.setFrom(AdminUtils.getAdminEmailId());
                    InternetAddress ia = new InternetAddress(email);
                    ArrayList al = new ArrayList();
                    al.add(ia);
                    sm.setTo(al);
                    sm.setSubject(TurbineUtils.GetSystemName() +" Login Request");

                    
                    String url=TurbineUtils.GetFullServerPath() + "/app/template/Index.vm";
                    
                    String msgBody = "";
                    msgBody+= "<html><body>";
                    msgBody+="You requested your username, which is: ";
                    msgBody+=newUser.getUsername();
                    
                    msgBody+="<br><br><br>Please login to the site for additional user information <a href=\"" + url + "\">" + TurbineUtils.GetSystemName() + "</a>.";

                    msgBody+="</body></html>";
                    sm.setMsg(msgBody);

                    sm.send();

                    data.setMessage("The corresponding username for this email address has been emailed to your account.");
                    data.setScreenTemplate("Login.vm");
                } catch (MessagingException e) {
                    logger.error(e);
                    System.out.println("Error sending Email");


                    data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
                    data.setScreenTemplate("ForgotLogin.vm");
                    return;
                }
            }
        }else{
            //check user
            if (username != null && !username.equals(""))
            {
                //check email
                ItemSearch search = new ItemSearch();
                search.setAllowMultiples(false);
                search.setElement("xdat:user");
                search.addCriteria("xdat:user.login",username);

                ItemI temp = search.exec().getFirst();
                if (temp==null){
                    data.setMessage("Unknown username.");
                    data.setScreenTemplate("ForgotLogin.vm");
                    return;
                }else{
                    XDATUser newUser = new XDATUser(temp,false);

                    try{
                    	additionalProcessing(data, context, newUser);
                    }catch(Exception e){
                        logger.error(e);
                    }
                	
                    String newPassword = XFT.CreateRandomAlphaNumeric(10);
                    String tempPass = newUser.getStringProperty("primary_password");
                    newUser.setProperty("primary_password",XDATUser.EncryptString(newPassword,"SHA-256"));
                   	
                    newUser.save(null, true, false);
                    try {
                    	EmailerI sm = EmailUtils.getEmailer();
                        sm.setFrom(AdminUtils.getAdminEmailId());
                        InternetAddress ia = new InternetAddress(newUser.getEmail());
                        ArrayList<InternetAddress> al = new ArrayList<InternetAddress>();
                        al.add(ia);
                        sm.setTo(al);
                        sm.setSubject(TurbineUtils.GetSystemName() +" Login Request");
                        String msgBody = "";
                        msgBody+= "<html><body>";
                        msgBody+="Your password has been reset to:<br>";
                        msgBody+=newPassword;
                        
                        String url=TurbineUtils.GetFullServerPath() + "/app/action/XDATActionRouter/xdataction/MyXNAT";
                        
                        msgBody+="<br><br><br>Please login to the site and create a new password in the <a href=\"" + url + "\">account settings</a>.";

                        msgBody+="</body></html>";
                        sm.setMsg(msgBody);

                        sm.send();

                        data.setMessage("The password for " + username + " has been reset.  The new password has been emailed to your account. <br><br>Please use the new password to login to the site and change your password in the account settings.");

                        data.setScreenTemplate("Login.vm");
                    } catch (MessagingException e) {
                        logger.error("Unable to send mail",e);
                        System.out.println("Error sending Email");


                        data.setMessage("Due to a technical difficulty, we are unable to send you the email containing your information.  Please contact our technical support.");
                        data.setScreenTemplate("ForgotLogin.vm");
                        return;
                    }
                }
            }else{
                data.setScreenTemplate("ForgotLogin.vm");
                return;
            }
        }
    }

    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return true;
    }
}
