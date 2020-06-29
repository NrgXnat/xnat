/*
 * core: org.nrg.xdat.turbine.modules.screens.SecureScreen
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.modules.screens;

import org.nrg.framework.utilities.Reflection;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.modules.screens.VelocitySecureScreen;
import org.apache.turbine.services.velocity.TurbineVelocity;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.generic.EscapeTool;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.display.DisplayManager;
import org.nrg.xdat.entities.ThemeConfig;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.services.ThemeService;
import org.nrg.xdat.services.cache.GroupsAndPermissionsCache;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.AdminUtils;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader;
import org.nrg.xft.XFTTable;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import com.google.common.collect.Maps;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Tim
 */
public abstract class SecureScreen extends VelocitySecureScreen {
    protected final static Logger       logger       = LoggerFactory.getLogger(SecureScreen.class);
    private static         Pattern      _pattern     = Pattern.compile("\\A<!-- ([A-z_]+?): (.+) -->\\Z");
    private                List<String> _whitelistedIPs;
    protected              ThemeService themeService = XDAT.getThemeService();

    @SuppressWarnings("unused")
    public String getReason(RunData data) {
        return (String) TurbineUtils.GetPassedParameter(EventUtils.EVENT_REASON, data);
    }

    protected void error(Exception e, RunData data) {
        logger.error("", e);
        data.setScreenTemplate("Error.vm");
        data.getParameters().setString("exception", e.toString());
    }

    protected void preserveVariables(RunData data, Context context) {
        if (data.getParameters().containsKey("project")) {
            if (logger.isDebugEnabled()) {
                logger.debug(getClass().getName() + ": maintaining project '" + TurbineUtils.GetPassedParameter("project", data) + "'");
            }
            context.put("project", TurbineUtils.escapeParam(((String) TurbineUtils.GetPassedParameter("project", data))));
        }
    }

    public static void loadAdditionalVariables(RunData data, Context context) {
        checkForPopup(data, context);

        final UserI user = XDAT.getUserDetails();
        context.put("user", user);
        context.put("cacheLastModified", XDAT.getContextService().getBean(GroupsAndPermissionsCache.class).getUserLastUpdateTime(user));
        context.put("turbineUtils", TurbineUtils.GetInstance());
        context.put("displayManager", DisplayManager.GetInstance());
        context.put("systemName", TurbineUtils.GetSystemName());
        context.put("siteLogoPath", XDAT.getSiteLogoPath());
        context.put("esc", new EscapeTool());
        context.put("escUtils", new StringEscapeUtils());

        context.put("showReason", XDAT.getSiteConfigPreferences().getShowChangeJustification());
        context.put("requireReason", XDAT.getSiteConfigPreferences().getRequireChangeJustification());

        context.put("notifications", XDAT.getNotificationsPreferences());
        context.put("siteConfig", XDAT.getSiteConfigPreferences());

        context.put("configService", XDAT.getConfigService());
        context.put("contextService", XDAT.getContextService());
    }

    protected static void checkForPopup(final RunData data, final Context c) {
        if (TurbineUtils.GetPassedParameter("popup", data) != null) {
            if (((String) TurbineUtils.GetPassedParameter("popup", data)).equalsIgnoreCase("true")) {
                c.put("popup", "true");
            } else {
                c.put("popup", "false");
            }
        } else {
            c.put("popup", "false");
        }
    }

    /**
     * This method overrides the method in {@link VelocitySecureScreen#doBuildTemplate(RunData)} to perform a security
     * check first and store the popup status in the context.
     *
     * @param data Turbine information.
     *
     * @throws Exception When something goes wrong.
     */
    protected void doBuildTemplate(final RunData data) throws Exception {
        try {
            attemptToPreventBrowserCachingOfHTML(data.getResponse());
            final Context context = TurbineVelocity.getContext(data);
            loadAdditionalVariables(data, context);
            if (UserHelper.getUserHelper(data) == null && !XDAT.getSiteConfigPreferences().getRequireLogin()) {
                UserHelper.setGuestUserHelper(data);
            }

            final ThemeConfig themeConfig = themeService.getTheme();
            if (themeConfig != null) {
                context.put("theme", themeConfig.getName());
                final String themedStyle = themeService.getThemePage("theme", "style");
                if (StringUtils.isNotBlank(themedStyle)) {
                    context.put("themedStyle", themedStyle);
                }
                final String themedScript = themeService.getThemePage("theme", "script");
                if (StringUtils.isNotBlank(themedScript)) {
                    context.put("themedScript", themedScript);
                }
            }

            context.put("XNAT_CSRF", data.getSession().getAttribute("XNAT_CSRF"));
            preserveVariables(data, context);

            if (isAuthorized(data)) {
                final SessionRegistry sessionRegistry = XDAT.getContextService().getBean("sessionRegistry", SessionRegistryImpl.class);

                if (sessionRegistry != null) {
                    final List<String> uniqueIPs = new ArrayList<>();

                    final UserI user = XDAT.getUserDetails();
                    final List<String> sessionIds = user != null && !user.isGuest() ? Lists.transform(sessionRegistry.getAllSessions(user, false), new Function<SessionInformation, String>() {
                        @Override
                        public String apply(final SessionInformation session) {
                            return session.getSessionId();
                        }
                    }) : Collections.<String>emptyList();

                    assert user != null;
                    if (!sessionIds.isEmpty()) {
                        _whitelistedIPs = XDAT.getWhitelistedIPs(user);

                        try {
                            final String query = "SELECT session_id, ip_address FROM xdat_user_login WHERE session_id in ('" + Joiner.on("','").join(sessionIds) + "')";
                            final XFTTable table = TableSearch.Execute(query, user.getDBName(), user.getUsername());
                            table.resetRowCursor();
                            while (table.hasMoreRows()) {
                                final Hashtable row       = table.nextRowHash();
                                final String ipAddress = (String) row.get("ip_address");
                                if (!uniqueIPs.contains(ipAddress)) {
                                    if (!isExcludedIp(ipAddress)) {
                                        uniqueIPs.add(ipAddress);
                                    } else {
                                        // If the session is at an excluded IP...
                                        for (final String sessionId : sessionIds) {
                                            // Then we need to disregard the session ID as well.
                                            if (sessionId.equals(row.get("session_id"))) {
                                                sessionIds.remove(sessionId);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("problem looking for concurrent session IP addresses.", e);
                        }
                    }
                    //if(sessionCount > 100 || (sessionCount > 1 && ip.size() > 1 && ! TurbineUtils.getUser(data).getLogin().equals("guest"))){
                    if (!user.isGuest()) {
                        context.put("sessionCount", sessionIds.size());
                        context.put("sessionIpCount", uniqueIPs.size());
                        context.put("sessionIpCsv", Joiner.on(", ").join(uniqueIPs));
                    }
                }
                doBuildTemplate(data, context);
            } else {
                if (!XDAT.getSiteConfigPreferences().getRequireLogin()) {
                    data.setScreenTemplate("Login.vm");
                }
            }

            dynamicVariableLoad("org.nrg.xnat.extensions.screens."+ getClass().getName(),data,context);
        } catch (ConfigServiceException e) {
            logger.error("An error occurred accessing the configuration service", e);
            data.setScreenTemplate("Error.vm");
        } catch (RuntimeException e) {
            logger.error("", e);
            data.setScreenTemplate("Error.vm");
        }
    }

    public static void dynamicVariableLoad(final String packageName, final RunData data, final Context context) throws Exception{
        if(Reflection.getClassesForPackage(packageName).size()>0){
            Map<String,Object> params= Maps.newHashMap();
            params.put("context",context);
            params.put("data",data);

            Reflection.injectDynamicImplementations(packageName, false, params);
        }
    }

    /*******************************
     * Place custom extensions of the ScreenAdditionalVariables class in your
     * plugin in the org.nrg.extensions.screens.<ScreenJavaClassName>
     */
    public abstract class ScreenAdditionalVariables implements Reflection.InjectableI {
        public void execute(Map<String, Object> params)
        {
            try{
                RunData data= (RunData) params.get("data");
                Context context= (Context) params.get("context");
                this.loadAdditionalVariables(data, context);
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        public abstract void loadAdditionalVariables(final RunData data, final Context context) throws Exception;
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
     *
     * @return True if the user is authorized to access the screen.
     *
     * @throws Exception When something goes wrong.
     */
    protected boolean isAuthorized(final RunData data) throws Exception {
        return isAuthorizedInternal(data);
    }

    protected boolean isAuthorizedAdmin(final RunData data) throws Exception {
        if (isAuthorizedInternal(data) && Roles.isSiteAdmin(XDAT.getUserDetails())) {
            return true;
        }
        recordUnauthorizedAccess(data);
        return false;
    }

    /**
     * Provided so that this class can check authorization without calling isAuthorized() and getting subclass
     * implementation of isAuthorized() and likely ending up with a stack overflow.
     *
     * @param data The request data.
     *
     * @return Returns true if the user is authorized, false otherwise.
     *
     * @throws Exception When an error occurs.
     */
    private boolean isAuthorizedInternal(final RunData data) throws Exception {
        if (XDAT.getSiteConfigPreferences().getRequireLogin() || TurbineUtils.HasPassedParameter("par", data)) {
            logger.debug("isAuthorized() Login Required:true");
            TurbineVelocity.getContext(data).put("logout", "true");
            data.getParameters().setString("logout", "true");
            boolean isAuthorized = false;

            final UserI user = XDAT.getUserDetails();
            if (user == null || user.isGuest()) {
                //logger.debug("isAuthorized() Login Required:true user:null");
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase("")) {
                    data.getParameters().add("nextAction", data.getAction());
                } else {
                    data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
                }
                //System.out.println("nextPage::" + ((String)TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");
                doRedirect(data, Turbine.getConfiguration().getString("template.login"));
            } else {
                //logger.debug("isAuthorized() Login Required:true user:found");
                isAuthorized = true;
                if (TurbineUtils.GetPassedParameter("popup", data) != null) {
                    if (((String) TurbineUtils.GetPassedParameter("popup", data)).equalsIgnoreCase("true")) {
                        data.getTemplateInfo().setLayoutTemplate("/Popup.vm");
                    }
                } else {
                    data.getParameters().setString("popup", "false");
                }

                logAccess(data);
            }

            return isAuthorized;
        } else {
            boolean isAuthorized = true;
            logger.debug("isAuthorized() Login Required:false");
            final UserI user = XDAT.getUserDetails();
            if (user == null || user.isGuest()) {
                if (!allowGuestAccess()) {
                    isAuthorized = false;
                }

                XDAT.setGuestUserDetails();

                data.getParameters().add("nextPage", data.getTemplateInfo().getScreenTemplate());
                if (!data.getAction().equalsIgnoreCase("")) {
                    data.getParameters().add("nextAction", data.getAction());
                } else {
                    data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
                }
            } else {
                if (!allowGuestAccess() && user.getLogin().equals("guest")) {
                    isAuthorized = false;
                }
            }

            if (TurbineUtils.GetPassedParameter("popup", data) != null) {
                if (((String) TurbineUtils.GetPassedParameter("popup", data)).equalsIgnoreCase("true")) {
                    data.getTemplateInfo().setLayoutTemplate("/Popup.vm");
                }
            } else {
                data.getParameters().setString("popup", "false");
            }

            logAccess(data);

            if (!isAuthorized) {
                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase("")) {
                    data.getParameters().add("nextAction", data.getAction());
                } else {
                    data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
                }
                //System.out.println("nextPage::" + ((String)TurbineUtils.GetPassedParameter("nextPage",data)) + "::nextAction" + ((String)TurbineUtils.GetPassedParameter("nextAction",data)) + "\n");
                doRedirect(data, Turbine.getConfiguration().getString("template.login"));

            }
            return isAuthorized;
        }
    }

    public void logAccess(RunData data) {
        AccessLogger.LogScreenAccess(data);
    }

    public void logAccess(RunData data, String message) {
        AccessLogger.LogScreenAccess(data, message);
    }

    public boolean allowGuestAccess() {
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
        List<Properties> tabs                  = new ArrayList<>();
        File             tabsFolder;
        String           forwardSlashSubFolder = subfolder;
        if (forwardSlashSubFolder != null) {
            forwardSlashSubFolder = subfolder.replace("\\", "/");
        }
        List<URL> uris = CustomClasspathResourceLoader.findVMsByClasspathDirectory("screens" + "/" + forwardSlashSubFolder);
        if (uris.size() > 0) {
            final URL url      = uris.get(0);
            String    fileName = FilenameUtils.getBaseName(url.toString()) + "." + FilenameUtils.getExtension(url.toString());
            String    resolved = CustomClasspathResourceLoader.safeJoin("/", forwardSlashSubFolder, fileName);
            try {
                //addProps(cpTabFile, tabs, _defaultTabs, forwardSlashSubFolder + "/" + cpTabFile.getName());
                addProps(fileName, CustomClasspathResourceLoader.getInputStream("screens/" + resolved), tabs, _defaultTabs, resolved);
            } catch (IOException e) {
                logger.error("", e);
            }
        } else {
            tabsFolder = XDAT.getScreenTemplatesSubfolder(subfolder);
            if (tabsFolder != null && tabsFolder.exists()) {
                File[] files = tabsFolder.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File folder, String name) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Testing the name: " + name + " in folder: " + folder.getAbsolutePath());
                        }
                        return name.endsWith(".vm");
                    }
                });

                if (files != null) {
                    for (File file : files) {
                        try {
                            addProps(file, tabs, _defaultTabs, subfolder + "/" + file.getName());
                        } catch (IOException e) {
                            logger.error("", e);
                        }
                    }
                }
            }
        }
        return tabs;
    }

    protected void recordUnauthorizedAccess(final RunData data) throws IOException {
        data.setMessage("Unauthorized access.  Please login to gain access to this page.");
        logAccess(data, "Unauthorized access.");
        logger.error("Unauthorized Access to an Admin Screen (prevented).");
        AdminUtils.sendAdminEmail(XDAT.getUserDetails(), "Unauthorized Admin Access Attempt", "Unauthorized Access to an Admin Screen (" + data.getScreen() + ") prevented.");
        data.getResponse().sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    public static void addProps(File file, List<Properties> screens, List<String> _defaultScreens, final String path) throws FileNotFoundException {
        if (file.exists()) {
            InputStream stm = null;
            try {
                stm = FileUtils.openInputStream(file);
                addProps(file.getName(), stm, screens, _defaultScreens, path);
            } catch (IOException e) {
                logger.error("", e);
            } finally {
                if (stm != null) {
                    try {
                        stm.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            }
        }
    }

    public static void addProps(String fileName, InputStream file, List<Properties> screens, List<String> _defaultScreens, final String path) throws FileNotFoundException {
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

            if (file != null) {
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
     * @param data       The run data.
     * @param context    The Velocity context object.
     * @param parameters An array of parameter names to be evaluated.
     *
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
        if (resp != null && resp instanceof HttpServletResponse) {
            HttpServletResponse response = (HttpServletResponse) resp;
            response.setHeader("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
            response.setHeader("Last-Modified", new Date().toString());
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
            response.setHeader("Pragma", "no-cache");
        }
    }

    private List<String> _defaultTabs;
}

