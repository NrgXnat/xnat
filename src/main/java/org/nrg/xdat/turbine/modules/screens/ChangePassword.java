// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xdat.turbine.modules.screens;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.TemporaryTokenStore;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.utils.SaveItemHelper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class ChangePassword extends VelocitySecureScreen {


	@Override
	protected void doBuildTemplate(RunData data) throws Exception {
		Context c = TurbineVelocity.getContext(data);
        SecureScreen.loadAdditionalVariables(data, c);
        doBuildTemplate(data, c);
	}

	@Override
	protected void doBuildTemplate(final RunData data, final Context context) {
		try {
			if(data!=null && TurbineUtils.getUser(data)!=null && TurbineUtils.getUser(data).getUsername()!=null){
				context.put("login", TurbineUtils.getUser(data).getUsername());
				context.put("topMessage", "Your password has expired. Please choose a new one.");
			}
			else{
				String alias = (String)TurbineUtils.GetPassedParameter("a", data);
				String secret = (String)TurbineUtils.GetPassedParameter("s", data);
				
				context.put("a", alias);
				context.put("s", secret);
				context.put("topMessage", "Please choose a new password.");
				
				//String login = TemporaryTokenStore.getLoginByToken(token);
				
				//context.put("login", login);
				//context.put("topMessage", "Please choose a new password.");
				
				//log user in if token is valid
				/*XDATUser user = new XDATUser(login);
                TurbineUtils.setUser(data,user);

                HttpSession session = data.getSession();
                session.setAttribute("user",user);
                session.setAttribute("loggedin",true);
                session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());

                XFTItem item = XFTItem.NewItem("xdat:user_login",user);
                java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
                item.setProperty("xdat:user_login.user_xdat_user_id",user.getID());
                item.setProperty("xdat:user_login.login_date",today);
                item.setProperty("xdat:user_login.ip_address",data.getRemoteAddr());
                item.setProperty("login_date",today);
                item.setProperty("ip_address",data.getRemoteAddr());	                    
                SaveItemHelper.authorizedSave(item,null,true,false);
                
				Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
                grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
		    	Authentication authentication = new UsernamePasswordAuthenticationToken(login, "", grantedAuthorities);
		    	SecurityContext securityContext = SecurityContextHolder.getContext();
		    	securityContext.setAuthentication(authentication);*/
			
				
				
			}

			
		} catch (Exception e) {
		    log.error(e);
		    e.printStackTrace();
		}
	    }

	@Override
	protected boolean isAuthorized(RunData arg0) throws Exception {
		return false;
	}


}
