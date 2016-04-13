/*
 * org.nrg.xdat.turbine.modules.screens.SecureScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 7:54 PM
 */

package org.nrg.xdat.turbine.modules.screens;

import com.google.common.base.Joiner;
import org.apache.commons.io.FileUtils;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.generic.EscapeTool;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author Tim
 */
public abstract class SecureScreen extends VelocitySecureScreen {
	public final static Logger logger = LoggerFactory.getLogger(SecureScreen.class);
    private static Pattern _pattern = Pattern.compile("\\A<!-- ([A-z_]+?): (.+) -->\\Z");
    List<String> _whitelistedIPs;
    protected ThemeService themeService = XDAT.getContextService().getBean(ThemeService.class);

    @SuppressWarnings("unused")
    public String getReason(RunData data){
    	return (String)TurbineUtils.GetPassedParameter(EventUtils.EVENT_REASON, data);
    }

    protected void error(Exception e,RunData data){
        logger.error("",e);
        data.setScreenTemplate("Error.vm");
        data.getParameters().setString("exception", e.toString());
    }

    protected void preserveVariables(RunData data, Context context){
        if (data.getParameters().containsKey("project")){
        	if(XFT.VERBOSE)System.out.println(this.getClass().getName() + ": maintaining project '" + TurbineUtils.GetPassedParameter("project",data) +"'");
            context.put("project", TurbineUtils.escapeParam(((String)TurbineUtils.GetPassedParameter("project",data))));
        }
    }
    
    public static void loadAdditionalVariables(RunData data, Context c){
    	if (TurbineUtils.GetPassedParameter("popup",data) !=null){
            if(((String)TurbineUtils.GetPassedParameter("popup",data)).equalsIgnoreCase("true")){
                c.put("popup","true");
            }else{
                c.put("popup","false");
            }
        }else{
            c.put("popup","false");
        }

        c.put("user", XDAT.getUserDetails());
        c.put("turbineUtils",TurbineUtils.GetInstance());
    	c.put("displayManager", DisplayManager.GetInstance());
        c.put("systemName",TurbineUtils.GetSystemName());
        c.put("esc", new EscapeTool());

        c.put("showReason", XFT.getShowChangeJustification());
        c.put("requireReason", XFT.getRequireChangeJustification());
        
        try{
        	c.put("siteConfig", XDAT.getSiteConfiguration());
        }catch(ConfigServiceException ignored){
        	
        }
    }

	/**
     * This method overrides the method in {@link VelocitySecureScreen#doBuildTemplate(RunData)} to perform a security
     * check first and store the popup status in the context.
     *
     * @param data Turbine information.
     * @throws Exception When something goes wrong.
     */
	protected void doBuildTemplate(RunData data) throws Exception {
	    try {
	    	attemptToPreventBrowserCachingOfHTML(data.getResponse());
            Context c = TurbineVelocity.getContext(data);
            loadAdditionalVariables(data, c);

            String themedStyle = themeService.getThemePage("theme", "style");
            if(themedStyle != null) {
                c.put("themedStyle", themedStyle);
            }
            String themedScript = themeService.getThemePage("theme", "script");
            if(themedScript != null) {
                c.put("themedScript", themedScript);
            }

            c.put("XNAT_CSRF", data.getSession().getAttribute("XNAT_CSRF"));
            preserveVariables(data,c);
            
            if (isAuthorized(data)) {
                SessionRegistry sessionRegistry = XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class);
                
                if(sessionRegistry != null){
                    List<String> uniqueIPs = new ArrayList<>();
                    List<String> sessionIds = new ArrayList<>();
                    for (SessionInformation session : sessionRegistry.getAllSessions(TurbineUtils.getUser(data), false)) {
                        sessionIds.add(session.getSessionId());
                    }

                    if (sessionIds.size() > 0) {
                        String query = "SELECT session_id, ip_address FROM xdat_user_login WHERE session_id in ('" + Joiner.on("','").join(sessionIds) + "')";
       
                		UserI user = TurbineUtils.getUser(data);
                        _whitelistedIPs = XDAT.getWhitelistedIPs(user);

                		try {
                			XFTTable table = TableSearch.Execute(query, user.getDBName(), user.getUsername());
                			table.resetRowCursor();
                            while (table.hasMoreRows()) {
                            	final Hashtable row = table.nextRowHash();
                                String ipAddress = (String) row.get("ip_address");
                                if (!uniqueIPs.contains(ipAddress)) {
                                    if (!isExcludedIp(ipAddress)) {
                                        uniqueIPs.add(ipAddress);
                                    } else {
                                        // If the session is at an excluded IP...
                                        for (String sessionId : sessionIds) {
                                            // Then we need to disregard the session ID as well.
                                            if (sessionId.equals(row.get("session_id"))) {
                                                sessionIds.remove(sessionId);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                		} catch (Exception e){
                			logger.error("problem looking for concurrent session IP addresses.", e);
                		}
                	}
                	//if(sessionCount > 100 || (sessionCount > 1 && ip.size() > 1 && ! TurbineUtils.getUser(data).getLogin().equals("guest"))){
                	if(! TurbineUtils.getUser(data).getLogin().equals("guest")){
                        StringBuilder sessionWarning = new StringBuilder(); //"WARNING: Your account currently has ").append(sessionCount).append(" login sessions open from ").append(ip.size()).append(" distinct IP addresses. If you believe this is incorrect, please take corrective action. The IP addresses are:");
                        for (String i : uniqueIPs) {
                			sessionWarning.append(i).append(", ");
                		}
                		//trim that last comma
                		if(sessionWarning.length() > 2){
                			sessionWarning.delete(sessionWarning.length() - 2, sessionWarning.length());
                		}
                        c.put("sessionCount", sessionIds.size());
                        c.put("sessionIpCount", uniqueIPs.size());
                		c.put("sessionIpCsv", sessionWarning.toString());
                	}
                }
                doBuildTemplate(data, c);
            }else{
                if (!XFT.GetRequireLogin()) {
                    data.setScreenTemplate("Login.vm");
                }
            }
            
        } catch (ConfigServiceException e) {
            logger.error("An error occurred accessing the configuration service", e);
            data.setScreenTemplate("Error.vm");
        } catch (RuntimeException e) {
            logger.error("",e);
            data.setScreenTemplate("Error.vm");
        }
	}

    private boolean isExcludedIp(final String newIP) throws ConfigServiceException {
        for (String ip : _whitelistedIPs) {
            if (newIP.startsWith(ip)) {
                return true;
            }
        }
        return false;
    }

	/**
	 * Override this method to perform the security check needed.
	 *
	 * @param data Turbine information.
	 * @return True if the user is authorized to access the screen.
     * @throws Exception When something goes wrong.
	 */
    protected boolean isAuthorized(RunData data) throws Exception {
        if (XFT.GetRequireLogin() || TurbineUtils.HasPassedParameter("par", data)) {
	        logger.debug("isAuthorized() Login Required:true");
            TurbineVelocity.getContext(data).put("logout","true");
			data.getParameters().setString("logout","true");
			boolean isAuthorized = false;

			UserI user = TurbineUtils.getUser(data);
            if (user == null) {
		        //logger.debug("isAuthorized() Login Required:true user:null");
				String Destination = data.getTemplateInfo().getScreenTemplate();
				data.getParameters().add("nextPage", Destination);
				if (!data.getAction().equalsIgnoreCase(""))
					data.getParameters().add("nextAction",data.getAction());
				else
					data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
				//System.out.println("nextPage::" + ((String)TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");
				doRedirect(data,org.apache.turbine.Turbine.getConfiguration().getString("template.login"));

            } else {

		        //logger.debug("isAuthorized() Login Required:true user:found");
				isAuthorized = true;
				if (TurbineUtils.GetPassedParameter("popup",data) != null){
					if (((String)TurbineUtils.GetPassedParameter("popup",data)).equalsIgnoreCase("true")){
						data.getTemplateInfo().setLayoutTemplate("/Popup.vm");
					}
                } else {
					data.getParameters().setString("popup","false");
				}

				logAccess(data);
			}

			return isAuthorized;
		}else{
            boolean isAuthorized = true;
	        logger.debug("isAuthorized() Login Required:false");
			UserI user = TurbineUtils.getUser(data);
            if (user == null) {
                if (!allowGuestAccess())isAuthorized=false;

                HttpSession session = data.getSession();
                session.removeAttribute("loggedin");
                UserI guest=Users.getGuest();
                if (guest!=null) {
					TurbineUtils.setUser(data,guest);
					session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
                    String Destination = data.getTemplateInfo().getScreenTemplate();
                    data.getParameters().add("nextPage", Destination);
                    if (!data.getAction().equalsIgnoreCase(""))
                        data.getParameters().add("nextAction",data.getAction());
                    else
                        data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
                    //System.out.println("nextPage::" + ((String)TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");

				}
			}else{
			    if (!allowGuestAccess() && user.getLogin().equals("guest")){
                    isAuthorized=false;
                }
            }

            if (TurbineUtils.GetPassedParameter("popup",data) != null){
                if (((String)TurbineUtils.GetPassedParameter("popup",data)).equalsIgnoreCase("true")){
                    data.getTemplateInfo().setLayoutTemplate("/Popup.vm");
                }
            } else {
                data.getParameters().setString("popup","false");
            }

            logAccess(data);

            if (!isAuthorized){
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase(""))
                    data.getParameters().add("nextAction",data.getAction());
                else
                    data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
                //System.out.println("nextPage::" + ((String)TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");
                doRedirect(data,org.apache.turbine.Turbine.getConfiguration().getString("template.login"));

            }
			return isAuthorized;
		}
	}

    public void logAccess(RunData data) {
		AccessLogger.LogScreenAccess(data);
	}

    public void logAccess(RunData data, String message) {
        AccessLogger.LogScreenAccess(data,message);
    }

    public boolean allowGuestAccess(){
        return true;
    }

    @SuppressWarnings("unused")
    protected void setDefaultTabs(String... defaultTabs) {
        _defaultTabs = Arrays.asList(defaultTabs);
    }

    @SuppressWarnings("unused")
    protected void cacheTabs(Context context, String subfolder) throws FileNotFoundException {
        List<Properties> tabs = findTabs(subfolder);
        if (tabs != null && tabs.size() > 0) {
            context.put("tabs", tabs);
        }
    }

    protected List<Properties> findTabs(String subfolder) throws FileNotFoundException {
        List<Properties> tabs = new ArrayList<>();
        File tabsFolder = XDAT.getScreenTemplatesSubfolder(subfolder);
        if (tabsFolder!=null && tabsFolder.exists()) {
            File[] files = tabsFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File folder, String name) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Testing the name: " + name + " in folder: " + folder.getAbsolutePath());
                    }
                    return name.endsWith(".vm");
                }
            });

            for (File file: files) {
                try {
					addProps(file,tabs,_defaultTabs,subfolder+"/"+file.getName());
				} catch (IOException e) {
					logger.error("",e);
				}
            }
        }
        return tabs;
    }
    
    public static void addProps(File file,List<Properties> screens, List<String> _defaultScreens, final String path) throws FileNotFoundException{
        if(file.exists()){
        	InputStream stm=null;
            try {
				stm=FileUtils.openInputStream(file);
				addProps(file.getName(),stm,screens,_defaultScreens,path);
			} catch (IOException e) {
				logger.error("",e);
			}finally{
				if(stm!=null){
					try {
						stm.close();
					} catch (IOException e) {
						logger.error("",e);
					}
				}
			}
        }
    }
    
    public static void addProps(String fileName,InputStream file,List<Properties> screens, List<String> _defaultScreens, final String path) throws FileNotFoundException{
    	String divName = fileName.substring(0, fileName.length() - 3);

        // If there are no default tabs or if the defaultTabs doesn't exclude this divName...
        if (_defaultScreens == null || !_defaultScreens.contains(divName)) {
            Properties metadata = new Properties();

            
            // Set default divName and title properties to start. These can be overridden during mix-in processing.
            metadata.setProperty("fileName", fileName);
            metadata.setProperty("path", path);
            metadata.setProperty("divName", divName);
            metadata.setProperty("title", divName);

            boolean include = true;

            if(file!=null){
                try (final Scanner scanner = new Scanner(file)) {
                    while (scanner.hasNextLine()) {
                        String  line    = scanner.nextLine();
                        Matcher matcher = _pattern.matcher(line);
                        if (matcher.matches()) {
                            String key   = matcher.group(1);
                            String value = matcher.group(2);
                            if (key.equalsIgnoreCase("ignore") && value.equalsIgnoreCase("true")) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Found ignore = true in file: " + fileName);
                                }
                                include = false;
                                break;
                            }
                            metadata.setProperty(key, value);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Came up with " + key + "[" + value + "] from file: " + fileName);
                            }
                        }
                    }
                }

                if (include) {
                	screens.add(metadata);
                }
            }
        }
    }

    /**
     * Searches for the parameters contained in the <b>parameters</b> array. If the parameter is present with any of the
     * names in the array, it will be pulled and stored in the context using the first parameter name in the array.
     *
     * @param data          The run data.
     * @param context       The Velocity context object.
     * @param parameters    An array of parameter names to be evaluated.
     * @return <b>true</b> if the parameter was found in the run data, <b>false</b> otherwise.
     */
    protected static boolean storeParameterIfPresent(final RunData data, final Context context, final String... parameters) {
        for (String parameter : parameters) {
            if (TurbineUtils.HasPassedParameter(parameter, data)) {
                context.put(parameters[0], TurbineUtils.GetPassedParameter(parameter, data));
                return true;
            }
        }
        return false;
    }

    private void attemptToPreventBrowserCachingOfHTML(ServletResponse resp) {
    	if(resp != null && resp instanceof HttpServletResponse) {
    		HttpServletResponse response = (HttpServletResponse) resp;
	        response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
	        response.setHeader("Last-Modified", new Date().toString());
	        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	        response.setHeader("Pragma", "no-cache");
    	}
    }
    
    private List<String> _defaultTabs;
}

