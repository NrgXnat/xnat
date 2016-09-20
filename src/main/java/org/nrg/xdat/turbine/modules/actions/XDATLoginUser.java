/*
 * core: org.nrg.xdat.turbine.modules.actions.XDATLoginUser
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.actions;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.ActionLoader;
import org.apache.turbine.modules.actions.VelocityAction;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.security.TurbineSecurityException;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.Authenticator;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
/**
 * @author Tim
 *
 */
public class XDATLoginUser extends VelocityAction{

    private final static Logger logger       = LoggerFactory.getLogger(XDATLoginUser.class);
	/** CGI Parameter for the user name */
	public static final  String CGI_USERNAME = "username";

	/** CGI Parameter for the password */
	public static final String CGI_PASSWORD = "password";

	/** Logging */
	private static Log log = LogFactory.getLog(XDATLoginUser.class);

	/**
	 * Updates the user's LastLogin timestamp, sets their state to
	 * "logged in" and calls RunData.setUser() .  If the user cannot
	 * be authenticated (database error?) the user is assigned
	 * anonymous status and, if tr.props contains a TEMPLATE_LOGIN,
	 * the screenTemplate is set to this, otherwise the screen is set
	 * to SCREEN_LOGIN
	 *
	 * @param     data Turbine information.
	 * @exception TurbineSecurityException could not get instance of the
	 *            anonymous user
	 */
	public void doPerform(RunData data, Context context)
			throws TurbineSecurityException
	{
		//ScreenUtils.OutputDataParameters(data);
		//ScreenUtils.OutputContextParameters(TurbineVelocity.getContext(data));
		String username = (String)TurbineUtils.GetPassedParameter(CGI_USERNAME, data);
		String password = (String)TurbineUtils.GetPassedParameter(CGI_PASSWORD, data);
		if (StringUtils.isEmpty(username))
		{
			return;
		}else{
			if(username.contains("/")){
				username=username.substring(username.lastIndexOf("/")+1);
			}
			if(username.contains("\\")){
				username=username.substring(username.lastIndexOf("\\")+1);
			}
		}

		try
		{
			// Authenticate the user and get the object;.
			UserI user = Authenticator.Authenticate(new Authenticator.Credentials(username,password));

			try {
				XFTItem item = XFTItem.NewItem("xdat:user_login",user);
				java.util.Date today = java.util.Calendar.getInstance(java.util.TimeZone.getDefault()).getTime();
				item.setProperty("xdat:user_login.user_xdat_user_id",user.getID());
				item.setProperty("xdat:user_login.login_date",today);
				item.setProperty("xdat:user_login.ip_address",AccessLogger.GetRequestIp(data.getRequest()));
				item.setProperty("xdat:user_login.session_id", data.getSession().getId());  
				SaveItemHelper.authorizedSave(item,null,true,false,(EventMetaI)null);
			} catch (Exception e1) {
				logger.error("",e1);
			}

			XDAT.setUserDetails(user);
			

            AccessLogger.LogActionAccess(data, "Valid Login:"+user.getLogin());

            try{
            	doRedirect(data,context,user);
            }catch(Exception e){
                log.error("",e);
            }
		}
		catch (Exception e)
		{
            log.error("",e);

            AccessLogger.LogActionAccess(data, "Failed Login by '" + username +"': " +e.getMessage());
            
            if(username.toLowerCase().contains("script"))
            {
            	e= new Exception("Illegal username &lt;script&gt; usage.");
				AdminUtils.sendAdminEmail("Possible Cross-site scripting attempt blocked", StringEscapeUtils.escapeHtml4(username));
            	logger.error("",e);
                data.setScreenTemplate("Error.vm");
                data.getParameters().setString("exception", e.toString());
                return;
            }

				// Set Error Message and clean out the user.
            if(e instanceof SQLException){
				data.setMessage("An error has occurred.  Please contact a site administrator for assistance.");
            }else{
				data.setMessage(e.getMessage());
            }
            
			String loginTemplate =  org.apache.turbine.Turbine.getConfiguration().getString("template.login");

			if (StringUtils.isNotEmpty(loginTemplate))
			{
				// We're running in a templating solution
				data.setScreenTemplate(loginTemplate);
			}
			else
			{
				data.setScreen(org.apache.turbine.Turbine.getConfiguration().getString("screen.login"));
			}
		}
	}

	public void doRedirect(RunData data, Context context,UserI user) throws Exception{
		final String nextPage = (String) TurbineUtils.GetPassedParameter("nextPage", data);
		final String nextAction = (String) TurbineUtils.GetPassedParameter("nextAction", data);
		final boolean hasNextPage = StringUtils.isNotBlank(nextPage);
		final boolean hasNextAction = StringUtils.isNotBlank(nextAction);
		/*
		 * If the setPage("template.vm") method has not
		 * been used in the template to authenticate the
		 * user (usually Login.vm), then the user will
		 * be forwarded to the template that is specified
		 * by the "template.home" property as listed in
		 * TR.props for the webapp.
		 */
		if (hasNextAction && !nextAction.contains("XDATLoginUser") && !nextAction.equals(Turbine.getConfiguration().getString("action.login"))) {
			data.setAction(nextAction);
			VelocityAction action = (VelocityAction) ActionLoader.getInstance().getInstance(nextAction);
			action.doPerform(data, context);
		} else if (hasNextPage && !nextPage.equals(Turbine.getConfiguration().getString("template.home"))) {
			data.setScreenTemplate(nextPage);
		} else {
			data.setScreenTemplate("Index.vm");
		}

		if (data.getScreenTemplate().contains("Error.vm")) {
			data.setMessage("<b>Previous session expired.</b><br>If you have bookmarked this page, please redirect your bookmark to: " + TurbineUtils.GetFullServerPath());
			data.setScreenTemplate("Index.vm");
		}
	}

	public void doRegister(RunData data, Context context) throws Exception{
		data.setScreenTemplate("Register.vm");
	}

	public void doForgotLogin(RunData data, Context context) throws Exception{
		data.setScreenTemplate("ForgotLogin.vm");
	}
}

