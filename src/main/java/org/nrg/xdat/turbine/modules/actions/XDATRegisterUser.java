//Copyright Washington University School of Medicine All Rights Reserved
/*
 * Created on Dec 11, 2006
 *
 */
package org.nrg.xdat.turbine.modules.actions;

import java.util.UUID;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.modules.actions.VelocitySecureAction;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.entities.XDATUserDetails;
import org.nrg.xdat.entities.XdatUserAuth;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xdat.security.PasswordValidator;
import org.nrg.xdat.security.RegExpValidator;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.search.ItemSearch;
import org.springframework.security.authentication.AuthenticationManager;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

public class XDATRegisterUser extends VelocitySecureAction {
    static Logger logger = Logger.getLogger(XDATRegisterUser.class);

    @Override
    public void doPerform(RunData data, Context context) throws Exception {
		try {
        	PopulateItem populater = PopulateItem.Populate(data,org.nrg.xft.XFT.PREFIX + ":user",true);
        	ItemI found = populater.getItem();
            ItemSearch search = new ItemSearch();
            search.setAllowMultiples(false);
            search.setElement("xdat:user");
            search.addCriteria("xdat:user.login",found.getProperty("login"));
            
            ItemI temp = search.exec().getFirst();
            
            if (temp==null)
            {
            	if(!found.getStringProperty("email").equals(AdminUtils.getAdminEmailId()))
            	{
                    search = new ItemSearch();
                    search.setAllowMultiples(false);
                    search.setElement("xdat:user");
                    search.addCriteria("xdat:user.email",found.getProperty("email"));

                    temp = search.exec().getFirst();
            	}

                if (temp==null)
                {
	                String tempPass = found.getStringProperty("primary_password");
	                PasswordValidator validator = XDAT.getContextService().getBean(PasswordValidator.class);
	                if(validator.isValid(tempPass)){
	                
		             // NEW USER
	                    found.setProperty("primary_password",XDATUser.EncryptString(tempPass,"SHA-256"));
	
		                boolean autoApproval=autoApproval(data,context);	       
		                
		                if (autoApproval)
		                {
		                    found.setProperty("enabled","true");
		                }else{
		                    found.setProperty("enabled","false");
		                }
		                
		                found.setProperty("xdat:user.assigned_roles.assigned_role[0].role_name","SiteUser");
	                    found.setProperty("xdat:user.assigned_roles.assigned_role[1].role_name","DataManager");
		                
		                XDATUser newUser = new XDATUser(found);
		               // newUser.initializePermissions();
		                
		                SaveItemHelper.authorizedSave(newUser, TurbineUtils.getUser(data),true,false,true,false);
		                TurbineUtils.setUser(data,newUser);
		                
		                XdatUserAuth newUserAuth = new XdatUserAuth((String)found.getProperty("login"), "localdb");
	                    XDAT.getXdatUserAuthService().create(newUserAuth);
	
		                if (autoApproval)
		                {
		
		                    HttpSession session = data.getSession();
		                    session.setAttribute("user",newUser);
		                    session.setAttribute("loggedin",true);
		                    data.setMessage("User registration complete.");
		                    
		                    session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
		                    
		                    String sub = "New User Created: " + newUser.getUsername();
		                    String msg = this.getAutoApprovalTextMsg(data,newUser);
		                    
		                    
		                    if (AdminUtils.GetNewUserRegistrationsEmail())
		                        AdminUtils.sendAdminEmail(sub, msg);
		
		                    XFTItem item = XFTItem.NewItem("xdat:user_login",newUser);
		                    java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
		                    item.setProperty("xdat:user_login.user_xdat_user_id",newUser.getID());
		                    item.setProperty("xdat:user_login.login_date",today);
		                    item.setProperty("xdat:user_login.ip_address",data.getRemoteAddr());
		                    item.setProperty("login_date",today);
		                    item.setProperty("ip_address",data.getRemoteAddr());	                    
		                    SaveItemHelper.authorizedSave(item,null,true,false);
		                    
							Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
		                    grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
		    		    	Authentication authentication = new UsernamePasswordAuthenticationToken((String)found.getProperty("login"), tempPass, grantedAuthorities);
		    		    	SecurityContext securityContext = SecurityContextHolder.getContext();
		    		    	securityContext.setAuthentication(authentication);
							
		                    try{
		                    	directRequest(data,context,newUser);
		                    }catch(Exception e){
		                        logger.error(e);
		                    }
		                }else{
		                    
		                    try{
		                    	directRequest(data,context,newUser);
		                    }catch(Exception e){
		                        logger.error(e);
		                    }
		                	
		                    String comments = "";
		                    if (TurbineUtils.HasPassedParameter("comments", data))
		                        comments = (String)TurbineUtils.GetPassedParameter("comments", data);
		                              
		                    String phone = "";
		                    if (TurbineUtils.HasPassedParameter("phone", data))
		                        phone = (String)TurbineUtils.GetPassedParameter("phone", data);
		                              
		                    String lab = "";
		                    if (TurbineUtils.HasPassedParameter("lab", data))
		                        lab = (String)TurbineUtils.GetPassedParameter("lab", data);
		                    
		                    AdminUtils.sendNewUserRequestEmailMessage(newUser.getUsername(), newUser.getFirstname(), newUser.getLastname(), newUser.getEmail(), comments, phone, lab, context);
		                    
		                    data.setRedirectURI(null);
		                    data.setScreenTemplate("PostRegister.vm");
		                }
	                }else{
		            	handleInvalidPassword(data, context, found, validator.getMessage());
	                }
	            }else{
	            	handleDuplicateEmail(data, context, found);
                }
            }else{
            	handleDuplicateLogin(data, context, found);
            }
        } catch (Exception e) {
            logger.error("Error Storing User",e);
        }
        
        
    }
    
    public void handleInvalidPassword(RunData data,Context context,ItemI found, String message){
    	try {
			String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
			String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);
			String par = (String)TurbineUtils.GetPassedParameter("par",data);
			
			if(!StringUtils.isEmpty(par)){
				context.put("par", par);
			}
			if (!StringUtils.isEmpty(nextAction) && nextAction.indexOf("XDATLoginUser")==-1 && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
				context.put("nextAction", nextAction);
			}else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
				context.put("nextPage", nextPage);
			}
			// OLD USER
			data.setMessage(message);
		} catch (Exception e) {
            logger.error("Error adding user without complex enough password",e);
			data.setMessage(message);
		}finally{
			data.setScreenTemplate("Register.vm");
		}
    }
    
    public void handleDuplicateEmail(RunData data,Context context,ItemI found){
    	try {
			String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
			String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);
			String par = (String)TurbineUtils.GetPassedParameter("par",data);
			
			if(!StringUtils.isEmpty(par)){
				context.put("par", par);
			}
			if (!StringUtils.isEmpty(nextAction) && nextAction.indexOf("XDATLoginUser")==-1 && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
				context.put("nextAction", nextAction);
			}else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
				context.put("nextPage", nextPage);
			}
			// OLD USER
			data.setMessage("Email (" + found.getProperty("email") + ") already exists.");
		} catch (Exception e) {
            logger.error("Error handling duplicate login",e);
			data.setMessage("Email already exists.");
		}finally{
			data.setScreenTemplate("Register.vm");
		}
    }
    
    public void handleDuplicateLogin(RunData data,Context context,ItemI found){
    	try {
			String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
			String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);
			String par = (String)TurbineUtils.GetPassedParameter("par",data);
			
			if(!StringUtils.isEmpty(par)){
				context.put("par", par);
			}
			if (!StringUtils.isEmpty(nextAction) && nextAction.indexOf("XDATLoginUser")==-1 && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
				context.put("nextAction", nextAction);
			}else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
				context.put("nextPage", nextPage);
			}
			// OLD USER
			data.setMessage("Username (" + found.getProperty("login") + ") already exists.");
		} catch (Exception e) {
            logger.error("Error handling duplicate login",e);
			data.setMessage("Username already exists.");
		}finally{
			data.setScreenTemplate("Register.vm");
		}
    }
    
    public String getAutoApprovalTextMsg(RunData data, XDATUser newUser){
    	String msg="New User Created: " + newUser.getUsername();
        msg +="<br>Firstname: " + newUser.getFirstname();
        msg +="<br>Lastname: " + newUser.getLastname();
        msg +="<br>Email: " + newUser.getEmail();
        if (TurbineUtils.HasPassedParameter("comments", data))
            msg +="<br>Comments: " + TurbineUtils.GetPassedParameter("comments", data);
        
        if (TurbineUtils.HasPassedParameter("phone", data))
            msg +="<br>Phone: " + TurbineUtils.GetPassedParameter("phone", data);
        
        if (TurbineUtils.HasPassedParameter("lab", data))
            msg +="<br>Lab: " + TurbineUtils.GetPassedParameter("lab", data);
        
        return msg;
    }
    
    public boolean autoApproval(RunData data, Context context)throws Exception{
    	return XFT.GetUserRegistration();
    }
    
    public void directRequest(RunData data,Context context,XDATUser user) throws Exception{
		String nextPage = (String)TurbineUtils.GetPassedParameter("nextPage",data);
		String nextAction = (String)TurbineUtils.GetPassedParameter("nextAction",data);

        data.setScreenTemplate("Index.vm");
        
        if (XFT.GetUserRegistration()){
         if (!StringUtils.isEmpty(nextAction) && nextAction.indexOf("XDATLoginUser")==-1 && !nextAction.equals(org.apache.turbine.Turbine.getConfiguration().getString("action.login"))){
			data.setAction(nextAction);
            VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance(nextAction);
            action.doPerform(data, context);
		 }else if (!StringUtils.isEmpty(nextPage) && !nextPage.equals(org.apache.turbine.Turbine.getConfiguration().getString("template.home")) ) {
			data.setScreenTemplate(nextPage);
		 }
        }
    }

    @Override
    protected boolean isAuthorized(RunData data) throws Exception {
        return true;
    }

}
