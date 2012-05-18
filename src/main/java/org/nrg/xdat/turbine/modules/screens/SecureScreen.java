//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT – Extensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Jan 17, 2005
 *
 */
package org.nrg.xdat.turbine.modules.screens;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Hashtable;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTTable;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * @author Tim
 *
 */
public abstract class SecureScreen extends VelocitySecureScreen {
	public final static Logger logger = Logger.getLogger(SecureScreen.class);
    private static Pattern _pattern = Pattern.compile("\\A<!-- ([A-z_]+?): (.+) -->\\Z");

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
        	if(XFT.VERBOSE)System.out.println(this.getClass().getName() + ": maintaining project '" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data)) +"'");
            context.put("project", TurbineUtils.escapeParam(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("project",data))));
        }
    }
    
    public static void loadAdditionalVariables(RunData data, Context c){
    	if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data))!=null){
            if(((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)).equalsIgnoreCase("true")){
                c.put("popup","true");
            }else{
                c.put("popup","false");
            }
        }else{
            c.put("popup","false");
        }

        String systemName = TurbineUtils.GetSystemName();
        c.put("turbineUtils",TurbineUtils.GetInstance());
        c.put("systemName",systemName);
        
        c.put("showReason", XFT.SHOW_REASON);
        c.put("requireReason", XFT.REQUIRE_REASON);
        
        c.put("configProps", XFT.PROPS);        
    }

	/**
     * This method overrides the method in VelocitySecureScreen to
     * perform a security check first and store the popup status in the context.
     *
     * @param data Turbine information.
     * @throws Exception, a generic exception.
     */
	protected void doBuildTemplate(RunData data)
            throws Exception {
	    try {
            Context c = TurbineVelocity.getContext(data);
            loadAdditionalVariables(data, c);

            
            c.put("XNAT_CSRF", data.getSession().getAttribute("XNAT_CSRF"));
            preserveVariables(data,c);
            
            if (isAuthorized(data)) {
                SessionRegistry sessionRegistry = null;
                sessionRegistry = XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class);
                
                if(sessionRegistry != null){
                	int sessionCount = 0;
            		Set<String> ip = new HashSet<String>();

                	List<SessionInformation> l = sessionRegistry.getAllSessions(TurbineUtils.getUser(data), false);
                	if(l != null){
                		StringBuffer in = new StringBuffer("'");
                		sessionCount = l.size();
                		for(SessionInformation i:l){
                			in.append(i.getSessionId()).append("','");
                		}
                		//notice the lazy hack to finish out the query.
                		String query = "SELECT DISTINCT(ip_address) FROM xdat_user_login WHERE session_id in (" + in + "thisIsALazyHack')";
       
                		XDATUser user = TurbineUtils.getUser(data);
                		try {
                			PoolDBUtils dbUtils = new PoolDBUtils();
                			XFTTable table = TableSearch.Execute(query, user.getDBName(), user.getUsername());
                			table.resetRowCursor();
                            while (table.hasMoreRows())
                            {
                            	final Hashtable row = table.nextRowHash();
                                ip.add( (String)row.get("ip_address") );
                            }
                		} catch (Exception e){
                			logger.error("problem looking for concurrent session IP addresses.", e);
                		}
                	}
                	if(sessionCount > 100 || (sessionCount > 1 && ip.size() > 1 && ! TurbineUtils.getUser(data).getLogin().equals("guest"))){
                		StringBuffer sessionWarning = new StringBuffer("WARNING: Your account currently has ").append(sessionCount).append(" login sessions open from ").append(ip.size()).append(" distinct IP addresses. If you believe this is incorrect, please take corrective action. The IP addresses are:");
                		for(String i:ip){
                			sessionWarning.append(i).append(" ");
                		}
                		c.put("sessionWarning", sessionWarning.toString() );
                		
                	}
                }
                doBuildTemplate(data, c);
            }else{

                if (XFT.GetRequireLogin()) {
                }else{
                    data.setScreenTemplate("Login.vm");
                }
            }
            
        } catch (RuntimeException e) {
            logger.error("",e);
            data.setScreenTemplate("Error.vm");
            return;
        }
	}

	/**
	 * Overide this method to perform the security check needed.
	 *
	 * @param data Turbine information.
	 * @return True if the user is authorized to access the screen.
     * @throws Exception, a generic exception.
	 */
    protected boolean isAuthorized(RunData data) throws Exception {
		
//		String method = data.getRequest().getMethod();
//	There are places where a secure screen gets a POST or a PUT. that's a problem. uncomment this to see where that happens.
//    	if("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)){
//    		if(!"XDATLoginUser".equalsIgnoreCase(data.getAction())){
//    			System.out.println("SOME HOW WE'RE GETTING A POST/PUT IN SECURE SSCRRREEENNNNNN!!!!!\nSOME HOW WE'RE GETTING A POST/PUT IN SECURE SSCRRREEENNNNNN!!!!!\nSOME HOW WE'RE GETTING A POST/PUT IN SECURE SSCRRREEENNNNNN!!!!!\nSOME HOW WE'RE GETTING A POST/PUT IN SECURE SSCRRREEENNNNNN!!!!!\nSOME HOW WE'RE GETTING A POST/PUT IN SECURE SSCRRREEENNNNNN!!!!!\nSOME HOW WE'RE GETTING A POST/PUT IN SECURE SSCRRREEENNNNNN!!!!!\n it was in:" + data.getAction());
//    		}
//    	}
		
	    //TurbineUtils.OutputDataParameters(data);
        if (XFT.GetRequireLogin() || TurbineUtils.HasPassedParameter("par", data)) {
	        logger.debug("isAuthorized() Login Required:true");
            TurbineVelocity.getContext(data).put("logout","true");
			data.getParameters().setString("logout","true");
			boolean isAuthorized = false;

			XDATUser user = TurbineUtils.getUser(data);
            if (user == null) {
		        //logger.debug("isAuthorized() Login Required:true user:null");
				String Destination = data.getTemplateInfo().getScreenTemplate();
				data.getParameters().add("nextPage", Destination);
				if (!data.getAction().equalsIgnoreCase(""))
					data.getParameters().add("nextAction",data.getAction());
				else
					data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
				//System.out.println("nextPage::" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");
				doRedirect(data,org.apache.turbine.Turbine.getConfiguration().getString("template.login"));

            } else {

		        //logger.debug("isAuthorized() Login Required:true user:found");
				isAuthorized = true;
				if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)) != null){
					if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)).equalsIgnoreCase("true")){
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
			XDATUser user = TurbineUtils.getUser(data);
            if (user == null) {
                if (!allowGuestAccess())isAuthorized=false;

                HttpSession session = data.getSession();
                session.removeAttribute("loggedin");
				ItemSearch search = new ItemSearch();
				SchemaElementI e = SchemaElement.GetElement(XDATUser.USER_ELEMENT);
				search.setElement(e.getGenericXFTElement());
				search.addCriteria(XDATUser.USER_ELEMENT +"/login", "guest");
				ItemCollection items = search.exec(true);
                if (items.size() > 0) {
                    Iterator iter = items.iterator();
                    while (iter.hasNext()){
                        ItemI o = (ItemI)iter.next();
                        XDATUser temp = new XDATUser(o);
                        if (temp.getUsername().equalsIgnoreCase("guest")) {
                            user = temp;
                        }
                    }
                    if (user == null){
                        ItemI o = items.getFirst();
                        user = new XDATUser(o);
                    }
					TurbineUtils.setUser(data,user);
					session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());
                    String Destination = data.getTemplateInfo().getScreenTemplate();
                    data.getParameters().add("nextPage", Destination);
                    if (!data.getAction().equalsIgnoreCase(""))
                        data.getParameters().add("nextAction",data.getAction());
                    else
                        data.getParameters().add("nextAction",org.apache.turbine.Turbine.getConfiguration().getString("action.login"));
                    //System.out.println("nextPage::" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");

				}
			}else{
			    if (!allowGuestAccess() && user.getLogin().equals("guest")){
                    isAuthorized=false;
                }
            }

            if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)) != null){
                if (((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("popup",data)).equalsIgnoreCase("true")){
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
                //System.out.println("nextPage::" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");
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

    protected void setDefaultTabs(String... defaultTabs) {
        _defaultTabs = Arrays.asList(defaultTabs);
    }

    protected void cacheTabs(Context context, String subfolder) throws FileNotFoundException {
        List<Properties> tabs = findTabs(subfolder);
        if (tabs != null && tabs.size() > 0) {
            context.put("tabs", tabs);
        }
    }

    protected List<Properties> findTabs(String subfolder) throws FileNotFoundException {
        List<Properties> tabs = new ArrayList<Properties>();
        File tabsFolder = XDAT.getScreenTemplatesSubfolder(subfolder);
        if (tabsFolder!=null && tabsFolder.exists()) {
            File[] files = tabsFolder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File folder, String name) {
                    if (_log.isDebugEnabled()) {
                        _log.debug("Testing the name: " + name + " in folder: " + folder.getAbsolutePath());
                    }
                    return name.endsWith(".vm");
                }
            });

            for (File file: files) {
                addProps(file,tabs,_defaultTabs,subfolder+"/"+file.getName());
            }
        }
        return tabs;
    }
    
    public static void addProps(File file,List<Properties> screens, List<String> _defaultScreens, final String path) throws FileNotFoundException{
    	String fileName = file.getName();
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

            if(file.exists()){
                Scanner scanner = new Scanner(file);
                try {
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        Matcher matcher = _pattern.matcher(line);
                        if (matcher.matches()) {
                            String key = matcher.group(1);
                            String value = matcher.group(2);
                            if (key.equalsIgnoreCase("ignore") && value.equalsIgnoreCase("true")) {
                                if (_log.isDebugEnabled()) {
                                    _log.debug("Found ignore = true in file: " + file.getName());
                                }
                                include = false;
                                break;
                            }
                            metadata.setProperty(key, value);
                            if (_log.isDebugEnabled()) {
                                _log.debug("Came up with " + key + "[" + value + "] from file: " + file.getName());
                            }
                        }
                    }
                } finally {
                    if (scanner != null) {
                        scanner.close();
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

    private static final Log _log = LogFactory.getLog(SecureScreen.class);

    private List<String> _defaultTabs;
}

