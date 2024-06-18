/*
 * core: org.nrg.xdat.turbine.utils.TurbineUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.turbine.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.preferences.DisplayedUserIdentifierType;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.db.PoolDBUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.schema.design.SchemaElementI;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.XftStringUtils;
import org.restlet.data.Status;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader.safeJoin;

/**
 * @author Tim
 */
@Slf4j
public class TurbineUtils {
    public static final  String       EDIT_ITEM = "edit_item";
    public static final String        BASE_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final Logger       logger    = Logger.getLogger(TurbineUtils.class);
    private              XdatSecurity _security = null;

    private static TurbineUtils INSTANCE = null;

    private TurbineUtils() {
        init();
    }

    private void init() {

    }

    public static TurbineUtils GetInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TurbineUtils();
        }

        return INSTANCE;
    }

    private XdatSecurity getSecurityObject() {
        if (_security == null) {
            final List<XdatSecurity> al = XdatSecurity.getAllXdatSecuritys(null, false);
            if (al.size() > 0) {
                _security = al.get(0);
            }
        }

        return _security;
    }

    public Integer getSecurityID() {
        final XdatSecurity sec = this.getSecurityObject();
        if (sec != null) {
            try {
                return sec.getIntegerProperty("xdat:security.xdat_security_id");
            } catch (FieldNotFoundException | ElementNotFoundException e) {
                logger.error("", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static Integer GetSystemID() {
        return TurbineUtils.GetInstance().getSecurityID();
    }

    public static String GetSystemName() {
        final String site_id = XDAT.getSiteConfigPreferences().getSiteId();
        if (site_id == null || StringUtils.isEmpty(site_id)) {
            return "XNAT";
        } else {
            return site_id;
        }
    }

    @SuppressWarnings("unused")
    public boolean loginRequired() {
        return XDAT.getSiteConfigPreferences().getRequireLogin();
    }

    /**
     * Gets a bean with the indicated name. If no bean with that name is found, this method throws {@link
     * NoSuchBeanDefinitionException}.
     *
     * @param name The name of the bean to be retrieved.
     *
     * @return An object from the context.
     *
     * @throws NoSuchBeanDefinitionException When a bean of the indicated type can't be found.
     */
    public Object getBean(final String name) throws NoSuchBeanDefinitionException {
        return XDAT.getContextService().getBean(name);
    }

    /**
     * Gets a bean of the indicated type. If no bean of that type is found, this method throws {@link
     * NoSuchBeanDefinitionException}.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     *
     * @throws NoSuchBeanDefinitionException When a bean of the indicated type can't be found.
     */
    public <T> T getBean(final Class<T> type) throws NoSuchBeanDefinitionException {
        return XDAT.getContextService().getBean(type);
    }

    /**
     * Gets a bean of the indicated type. If no bean of that type is found, null is returned.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     */
    @SuppressWarnings("unused")
    public <T> T getBeanSafely(final Class<T> type) {
        try {
            return getBean(type);
        } catch (NoSuchBeanDefinitionException ignored) {
            // This is OK, just means the bean doesn't exist in the current context. Carry on.
        }
        // If we didn't find a valid bean of the type, return null.
        return null;
    }

    /**
     * Gets the bean with the indicated name and type.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param name The name of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     *
     * @throws NoSuchBeanDefinitionException When a bean of the indicated type can't be found.
     */
    public <T> T getBean(final String name, final Class<T> type) throws NoSuchBeanDefinitionException {
        return XDAT.getContextService().getBean(name, type);
    }

    /**
     * Gets the bean with the indicated name and type. If no bean with that name and type is found, null is returned.
     *
     * @param <T>  The type of the bean to be retrieved.
     * @param name The name of the bean to be retrieved.
     * @param type The class of the bean to be retrieved.
     *
     * @return An object of the type.
     */
    @SuppressWarnings("unused")
    public <T> T getBeanSafely(final String name, final Class<T> type) {
        try {
            return getBean(name, type);
        } catch (NoSuchBeanDefinitionException ignored) {
            // This is OK, just means the bean doesn't exist in the current context. Carry on.
        }
        // If we didn't find a valid bean of the type, return null.
        return null;
    }

    /**
     * Gets all beans with the indicated type.
     *
     * @param type The class of the bean to be retrieved.
     * @param <T>  The parameterized class of the bean to be retrieved.
     *
     * @return An object of the type.
     */
    @SuppressWarnings("unused")
    public <T> Map<String, T> getBeansOfType(final Class<T> type) {
        return XDAT.getContextService().getBeansOfType(type);
    }

    @SuppressWarnings("unused")
    public static boolean LoginRequired() {
        return XDAT.getSiteConfigPreferences().getRequireLogin();
    }

    public static SchemaElementI GetSchemaElementBySearch(RunData data) {
        if (data == null) {
            return null;
        }

        //TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
        final String searchField   = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
        final String searchElement = TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
        if (searchElement != null) {
            try {
                return GenericWrapperElement.GetElement(searchElement);
            } catch (XFTInitException e) {
                logger.info("Error initializing XFT", e);
            } catch (ElementNotFoundException e) {
                logger.info("Couldn't find element " + e.ELEMENT, e);
            }
        }

        if (searchField != null) {
            try {
                return XftStringUtils.GetRootElement(searchField);
            } catch (ElementNotFoundException e) {
                logger.info("Couldn't find element " + e.ELEMENT, e);
            }
        }

        return null;
    }

    public static XFTItem GetItemBySearch(RunData data) throws Exception {
        return (XFTItem) GetItemBySearch(data, null);
    }

    public static ItemI GetItemBySearch(RunData data, boolean preLoad) throws Exception {
        return GetItemBySearch(data, Boolean.valueOf(preLoad));
    }

    public static ItemI GetItemBySearch(RunData data, Boolean preload) throws Exception {
        if (data == null) {
            return null;
        }

        final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
        final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
        if (searchField == null || searchValue == null) {
            return null;
        }

        final ItemSearch search = new ItemSearch();
        search.setUser(XDAT.getUserDetails());

        final String elementName = XftStringUtils.GetRootElementName(searchField);

        search.setElement(elementName);
        search.addCriteria(searchField, searchValue);

        search.setAllowMultiples(ObjectUtils.getIfNull(preload, () -> isPreload(elementName)));

        final ItemCollection items = search.exec();
        if (items.size() > 0) {
            return items.getFirst();
        } else {
            return null;
        }
    }

    public static void SetEditItem(Object item, RunData data) {
        if (data != null) {
            data.getSession().setAttribute(EDIT_ITEM, item);
        }
    }

    public static Object GetEditItem(RunData data) {
        if (data == null) {
            return null;
        }

        final ItemI edit_item = (ItemI) data.getSession().getAttribute(EDIT_ITEM);
        data.getSession().removeAttribute(EDIT_ITEM);
        return edit_item;
    }

    public static void SetParticipantItem(ItemI item, RunData data) {
        if (data != null) {
            data.getSession().setAttribute("participant", item);
        }
    }

    public static ItemI GetParticipantItem(RunData data) {
        if (data == null) {
            return null;
        }

        final ItemI edit_item = (ItemI) data.getSession().getAttribute("participant");
        if (edit_item == null) {
            String s = TurbineUtils.escapeParam(data.getParameters().getString("part_id"));
            if (s != null) {
                try {
                    final ItemCollection items = ItemSearch.GetItems("xnat:subjectData.ID", s, XDAT.getUserDetails(), false);
                    if (items.size() > 0) {
                        return items.getFirst();
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            } else {
                s = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
                if (s != null) {
                    if (s.equalsIgnoreCase("xnat:subjectData.ID")) {
                        try {
                            return TurbineUtils.GetItemBySearch(data);
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }
            }
        }
        if (edit_item != null) {
            data.getSession().removeAttribute("participant");
        }
        return edit_item;
    }

    public static String GetSearchElement(RunData data) {
        if (data == null) {
            return null;
        }

        String s = TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
        if (s == null) {
            s = TurbineUtils.escapeParam(data.getParameters().getString("element"));
        }
        return s;
    }


    /**
     * Returns server and context as specified in the Turbine object model (taken from the first login url).
     *
     * @return The full server path.
     */
    public static String GetFullServerPath() {
        String siteUrl = XDAT.getSiteConfigPreferences().getSiteUrl();
        if (siteUrl.endsWith("/")) {
            siteUrl = siteUrl.substring(0, siteUrl.length() - 1);
        }
        if (StringUtils.isNotBlank(siteUrl)) {
            return siteUrl;
        }
        String contextPath = Turbine.getContextPath();
        if (contextPath.endsWith("/")) {
            contextPath = contextPath.substring(0, contextPath.length() - 1);
        }
        return Turbine.getServerScheme() + "://" + Turbine.getServerName() + (!Turbine.getServerPort().equals("80") ? ":" + Turbine.getServerPort() : "") + contextPath;
    }

    /**
     * Returns server and context as specified in user request object.
     *
     * @param data The run data for the context.
     *
     * @return The relative server path.
     */
    public static String GetRelativeServerPath(RunData data) {
        if (data == null) {
            return null;
        }

        return GetRelativePath(data.getRequest());
    }

    public static String GetRelativePath(HttpServletRequest req) {
        if (req.getContextPath() != null && !req.getContextPath().equals("")) {
            return req.getContextPath();
        } else {
            return "";
        }
    }

    /**
     * Returns server and context as specified in user request object.
     *
     * @param req Servlet request
     *
     * @return The full server path.
     */
    public static String GetFullServerPath(HttpServletRequest req) {
        if (StringUtils.isNotBlank(XDAT.getSiteConfigPreferences().getSiteUrl())) {
            return XDAT.getSiteConfigPreferences().getSiteUrl();
        }

        String s      = req.getRequestURL().toString();
        String server = null;

        if (req.getContextPath() != null && !req.getContextPath().equals("")) {
            final String path = req.getContextPath() + "/";
            if (s.contains(path)) {
                final int breakIndex = s.indexOf(path) + (path.length());
                server = s.substring(0, breakIndex);
            }
        }

        if (server == null) {
            final String port = (new Integer(req.getServerPort())).toString();
            if (s.contains(port)) {
                final int breakIndex = s.indexOf(port) + port.length();
                server = s.substring(0, breakIndex);
            }
        }

        if (server == null) {
            server = req.getScheme() + "://" + req.getServerName();
        }

        return server;
    }

    public static String GetContext() {
        return Turbine.getContextPath();
    }

    /**
     * Sets the user details.
     *
     * @param data The request run data.
     *
     * @deprecated Use {@link XDAT#getUserDetails()} instead.
     */
    @Deprecated
    public static UserI getUser(RunData data) {
        return data == null ? null : XDAT.getUserDetails();
    }

    /**
     * Sets the user details.
     *
     * @param data The request run data.
     * @param user The user object.
     *
     * @throws Exception When something goes wrong.
     * @deprecated Use {@link XDAT#setUserDetails(UserI)} instead.
     */
    @SuppressWarnings("UnusedParameters")
    @Deprecated
    public static void setUser(RunData data, UserI user) throws Exception {
        XDAT.setUserDetails(user);
    }

    public boolean checkRole(final UserI user, final String role) {
        return user != null && StringUtils.isNotBlank(role) && Roles.checkRole(user, role);
    }

    public boolean isSiteAdmin(final UserI user) {
        return user != null && Roles.isSiteAdmin(user);
    }

    public boolean isGuest(final UserI user) {
        return user == null || user.isGuest();
    }

    public boolean canEdit(final UserI user, final XFTItem item) throws Exception {
        return Permissions.canEdit(user, item);
    }

    public boolean canEdit(final UserI user, final String xmlPath, final Object item) throws Exception {
        return Permissions.canEdit(user, xmlPath, item);
    }

    public boolean canDelete(final UserI user, final XFTItem item) throws Exception {
        return Permissions.canDelete(user, item);
    }

    /**
     * @param data The run data for the context.
     *
     * @return The display search found in the context.
     */
    public static DisplaySearch getSearch(RunData data) {
        if (data == null) {
            return null;
        }

        if (data.getParameters().get("search_xml") != null || data.getParameters().get("search_id") != null) {
            return TurbineUtils.getDSFromSearchXML(data);
        } else {
            DisplaySearch ds = (DisplaySearch) data.getSession().getAttribute("search");
            if (ds == null) {
                String displayElement = TurbineUtils.escapeParam(data.getParameters().getString("search_element"));
                if (displayElement == null) {
                    displayElement = TurbineUtils.escapeParam(data.getParameters().getString("element"));
                }

                if (displayElement == null) {
                    return null;
                }

                try {
                    ds = UserHelper.getSearchHelperService().getSearchForUser(XDAT.getUserDetails(), displayElement, "listing");

                    final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
                    final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
                    if (searchField != null && searchValue != null) {
                        SearchCriteria criteria = new SearchCriteria();
                        criteria.setFieldWXMLPath(searchField);
                        criteria.setValue(searchValue);
                        ds.addCriteria(criteria);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            return ds;
        }
    }

    /**
     * Findbugs says DisplaySearch should be serializable.  That is a good idea, but this code is only used
     * in legacy code and should be remove at some point.  Unable to suppress warnings to support 1.5.
     *
     * @param data   The run data for the context.
     * @param search The display search.
     *
     * @deprecated We should get rid of this.
     */
    @Deprecated
    public static void setSearch(RunData data, DisplaySearch search) {
        if (data != null) {
            data.getSession().setAttribute("search", search);
        }
    }

    public static DisplaySearch getDSFromSearchXML(RunData data) {
        if (data == null) {
            return null;
        }

        final UserI user = XDAT.getUserDetails();

        if (user != null) {
            if (data.getParameters().get("search_xml") != null) {
                try {
                    String search_xml = data.getParameters().getString("search_xml");
                    search_xml = search_xml.replaceAll("%", "%25");
                    search_xml = URLDecoder.decode(search_xml, "UTF-8");
                    search_xml = StringUtils.replace(search_xml, ".close.", "/");

                    final StringReader     sr     = new StringReader(search_xml);
                    final InputSource      is     = new InputSource(sr);
                    final SAXReader        reader = new SAXReader(user);
                    final XFTItem          item   = reader.parse(is);
                    final XdatStoredSearch search = new XdatStoredSearch(item);
                    final DisplaySearch    ds     = search.getCSVDisplaySearch(user);
                    data.getParameters().remove("search_xml");
                    return ds;

                } catch (Exception e) {
                    logger.error("", e);
                }
            } else if (data.getParameters().get("search_id") != null) {
                try {
                    final String search_id = data.getParameters().get("search_id");

                    final String search_xml = PoolDBUtils.RetrieveLoggedCustomSearch(user.getUsername(), user.getDBName(), search_id);

                    if (search_xml != null) {
                        final StringReader     sr     = new StringReader(search_xml);
                        final InputSource      is     = new InputSource(sr);
                        final SAXReader        reader = new SAXReader(user);
                        final XFTItem          item   = reader.parse(is);
                        final XdatStoredSearch search = new XdatStoredSearch(item);
                        final DisplaySearch    ds     = search.getDisplaySearch(user);
                        data.getParameters().remove("search_id");
                        return ds;
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            } else if (data.getRequest().getAttribute("xss") != null) {
                try {
                    final XdatStoredSearch search = (XdatStoredSearch) data.getRequest().getAttribute("xss");
                    if (search != null) {
                        final DisplaySearch ds = search.getDisplaySearch(user);
                        data.getParameters().remove("search_id");
                        return ds;
                    }
                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }
        return null;
    }

    public static RunData SetSearchProperties(RunData data, ItemI item) {
        data.getParameters().setString("search_element", item.getXSIType());
        try {
            final SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
            final SchemaField    sf = (SchemaField) se.getAllPrimaryKeys().get(0);
            data.getParameters().setString("search_field", StringUtils.replace(StringUtils.replace(sf.getXMLPathString(se.getFullXMLName()), "/", "."), "@", "."));
            final Object o = item.getProperty(sf.getId());
            data.getParameters().setString("search_value", o.toString());
        } catch (Exception e) {
            logger.error("", e);
        }
        return data;
    }

    public static void SetSearchProperties(Context context, ItemI item) {
        context.put("search_element", item.getXSIType());
        try {
            final SchemaElementI se = SchemaElement.GetElement(item.getXSIType());
            final SchemaField    sf = (SchemaField) se.getAllPrimaryKeys().get(0);
            context.put("search_field", StringUtils.replace(StringUtils.replace(sf.getXMLPathString(se.getFullXMLName()), "/", "."), "@", "."));
            final Object o = item.getProperty(sf.getId());
            context.put("search_value", o.toString());
        } catch (Exception e) {
            logger.error("", e);
        }
    }

    public static boolean isAccessibleItem(final UserI user, final ItemI item) throws Exception {
        return ElementSecurity.IsSecureElement(item.getXSIType()) && Permissions.canRead(user, item) || Groups.hasAllDataAccess(user);
    }

    public static void denyAccess(final RunData data) {
        final HttpServletResponse response = data.getResponse();
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN.getCode());
        data.setStatusCode(Status.CLIENT_ERROR_FORBIDDEN.getCode());
        data.setMessage("You don't have access to the requested item.");
        data.setScreenTemplate("Index.vm");
    }

    public static ItemI getDataItem(RunData data) {
        final ItemI item = (ItemI) data.getSession().getAttribute("data_item");
        data.getSession().removeAttribute("data_item");
        return item;
    }

    public static RunData setDataItem(RunData data, ItemI item) {
        data.getSession().setAttribute("data_item", item);
        return data;
    }

    public static void OutputDataParameters(RunData data) {
        if (data != null) {
            logger.debug("\n\nData Parameters");
            final List<String> parameters = GetDataParameterList(data);
            for (String parameter : parameters) {
                logger.debug("KEY: " + parameter + " VALUE: " + data.getParameters().get(parameter.toLowerCase()));
            }
        }
    }

    public static List<String> GetDataParameterList(RunData data) {
        final List<String> parameters = new ArrayList<>();
        for (final Object parameter : data.getParameters().getKeys()) {
            parameters.add(parameter.toString());
        }
        Collections.sort(parameters);
        return parameters;
    }

    @SuppressWarnings({"deprecation", "unchecked"})
    public static Map<String, String> GetDataParameterHash(RunData data) {
        //TurbineUtils.OutputDataParameters(data);
        final Map<String, String> hash  = new Hashtable<>();
        ParameterParser           pp    = data.getParameters();
        Enumeration<Object>       penum = pp.keys();
        while (penum.hasMoreElements()) {
            final String key   = penum.nextElement().toString();
            final Object value = TurbineUtils.escapeParam(data.getParameters().get(key));
            if (value != null && !value.equals("")) {
                hash.put(TurbineUtils.escapeParam(key), value.toString());
            }
        }
        return hash;
    }

    @SuppressWarnings("unused")
    public static Map<String, String> GetContextParameterHash(Context context) {
        final Map<String, String> hash = new Hashtable<>();
        for (final Object key : context.getKeys()) {
            final Object value = context.get((String) key);
            if (value != null && !value.equals("")) {
                hash.put((String) key, value.toString());
            }
        }
        return hash;
    }

    @SuppressWarnings("unused")
    public static Map<String, String> GetTurbineParameters(RunData data, Context context) {
        final Map<String, String> hash;
        if (data != null) {
            hash = GetDataParameterHash(data);
        } else {
            hash = new Hashtable<>();
        }
        if (context != null) {
            hash.putAll(GetContextParameterHash(context));
        }
        return hash;
    }

    /**
     * Debugging method used in actions to display all fields in an Intake Group.
     *
     * @param group The group to display.
     */
    @SuppressWarnings("unused")
    public static void OutputGroupFields(Group group) {
        logger.debug("\n\nGroup Parameters");
        for (int i = 0; i < group.getFieldNames().length; i++) {
            try {
                logger.debug("FIELD: " + group.getFieldNames()[i] + " VALUE: " + group.get(group.getFieldNames()[i]).getValue() + " DISPLAY NAME: " + group.get(group.getFieldNames()[i]).getDisplayName() + " KEY: " + group.get(group.getFieldNames()[i]).getKey() + " Initial: " + group.get(group.getFieldNames()[i]).getInitialValue() + " DEFAULT: " + group.get(group.getFieldNames()[i]).getDefaultValue() + " TEST: " + group.get(group.getFieldNames()[i]).getTestValue());
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Debugging method used in actions to display all parameters in the Context object
     *
     * @param context The context to display.
     */
    public static void OutputContextParameters(Context context) {
        if (context != null) {
            logger.debug("\n\nContext Parameters");
            for (int i = 0; i < context.getKeys().length; i++) {
                logger.debug("KEY: " + context.getKeys()[i].toString() + " VALUE: " + context.get(context.getKeys()[i].toString()));
            }
        }
    }

    public static void OutputSessionParameters(RunData data) {
        if (data != null) {
            logger.debug("\n\nSession Parameters");
            final Enumeration<String> enumer = data.getSession().getAttributeNames();
            while (enumer.hasMoreElements()) {
                final String key = enumer.nextElement();
                final Object o   = data.getSession().getAttribute(key);
                logger.debug("KEY: " + key + " VALUE: " + o.getClass());
            }
        }
    }

    public static void OutputPassedParameters(RunData data, Context context, String name) {
        logger.debug("\n\n" + name);
        TurbineUtils.OutputDataParameters(data);
        TurbineUtils.OutputContextParameters(context);
        TurbineUtils.OutputSessionParameters(data);
    }

    public static boolean HasPassedParameter(String s, RunData data) {
        if (data.getParameters().get(s.toLowerCase()) != null) {
            final Object o = TurbineUtils.escapeParam(data.getParameters().get(s.toLowerCase()));
            return !o.toString().equalsIgnoreCase("");
        } else {
            return false;
        }
    }

    public static Object GetPassedParameter(String s, RunData data) {
        return GetPassedParameter(s.toLowerCase(), data, null);
    }

    public static Boolean GetPassedBoolean(String s, RunData data) {
        return data.getParameters().getBoolean(s);
    }

    public static Integer GetPassedInteger(String s, RunData data) {
        return TurbineUtils.GetPassedInteger(s, data, null);
    }

    public static Integer GetPassedInteger(String s, RunData data, Integer defualt) {
        if (data.getParameters().get(s.toLowerCase()) != null) {
            final Object o = TurbineUtils.escapeParam(data.getParameters().getInt(s.toLowerCase()));
            if (o.toString().equalsIgnoreCase("")) {
                return defualt;
            } else {
                return (Integer) o;
            }
        } else {
            return defualt;
        }
    }

    public static Object[] GetPassedObjects(String s, RunData data) {
        final Object[] v = data.getParameters().getObjects(s);
        if (v != null) {
            for (int i = 0; i < v.length; i++) {
                v[i] = TurbineUtils.escapeParam(v[i]);
            }
        }
        return v;
    }

    public static Collection<String> GetPassedStrings(String s, RunData data) {
        final Collection<String> _ret = new ArrayList<>();
        final String[]           v    = data.getParameters().getStrings(s);
        if (v != null) {
            for (final String aV : v) {
                if (StringUtils.isNotBlank(aV) && StringUtils.isNotBlank(TurbineUtils.escapeParam(aV))) {
                    _ret.add(TurbineUtils.escapeParam(aV));
                }
            }
        }
        return _ret;
    }

    public static Object GetPassedParameter(String s, RunData data, Object defualt) {
        if (data == null) {
            return defualt;
        }
        final ParameterParser parameters = data.getParameters();
        if (parameters != null) {
            if (parameters.get(s.toLowerCase()) != null) {
                final Object o = TurbineUtils.escapeParam(parameters.get(s.toLowerCase()));
                if (StringUtils.isNotBlank(o.toString())) {
                    return o;
                }
            }
        }
        return defualt;
    }

    public static void InstanciatePassedItemForScreenUse(RunData data, Context context) {
        try {
            final ItemI o = TurbineUtils.GetItemBySearch(data);

            if (o != null) {
                TurbineUtils.setDataItem(data, o);

                context.put("item", o);
                context.put("element", SchemaElement.GetElement(o.getXSIType()));
                context.put("search_element", TurbineUtils.escapeParam(data.getParameters().getString("search_element")));
                context.put("search_field", TurbineUtils.escapeParam(data.getParameters().getString("search_field")));
                context.put("search_value", TurbineUtils.escapeParam(data.getParameters().getString("search_value")));

            } else {
                logger.error("No Item Found.");
                data.setScreenTemplate("DefaultReport.vm");
            }
        } catch (Exception e) {
            logger.error("", e);
            data.setMessage(e.getMessage());
            data.setScreenTemplate("DefaultReport.vm");
        }
    }

    public Boolean toBoolean(String s) {
        return Boolean.valueOf(s);
    }

    public List<String> toList(final String concatenated) {
        return ToList(concatenated);
    }

    public static List<String> ToList(final String concatenated) {
        return Arrays.asList(concatenated.split("\\s*,\\s*"));
    }

    public boolean isNonEmptyCollection(final Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean IsNonEmptyCollection(final Collection<?> collection) {
        return GetInstance().isNonEmptyCollection(collection);
    }

    public String formatDate(Date d, String pattern) {
        final SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(d);
    }

    public String formatDate(Date d) {
        synchronized (getDateFormatter()) {
            return getDateFormatter().format(d);
        }
    }

    private static SimpleDateFormat default_date_format = null;

    public static SimpleDateFormat getDateFormatter() {
        if (default_date_format == null) {
            try {
                default_date_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("uiDateFormat", "yyyy-MM-dd"));
            } catch (ConfigServiceException e) {
                default_date_format = new SimpleDateFormat("yyyy-MM-dd");
            }
        }
        return default_date_format;
    }

    @SuppressWarnings("unused")
    public String formatDateTime(Date d) {
        synchronized (getDateTimeFormatter()) {
            return getDateTimeFormatter().format(d);
        }
    }

    private static SimpleDateFormat default_date_time_format = null;

    public static SimpleDateFormat getDateTimeFormatter() {
        if (default_date_time_format == null) {
            try {
                default_date_time_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("uiDateTimeFormat", BASE_DATE_TIME_FORMAT));
            } catch (ConfigServiceException e) {
                default_date_time_format = new SimpleDateFormat(BASE_DATE_TIME_FORMAT);
            }
        }
        return default_date_time_format;
    }

    public String formatLocalDateTime(LocalDateTime dateTime) {
        synchronized (getLocalDateTimeFormatter()) {
            return getLocalDateTimeFormatter().format(dateTime);
        }
    }

    private static DateTimeFormatter default_local_date_time_formatter = null;

    public static DateTimeFormatter getLocalDateTimeFormatter() {
        if (default_local_date_time_formatter == null) {
            try {
                default_local_date_time_formatter = DateTimeFormatter.ofPattern(XDAT.getSiteConfigurationProperty("uiDateTimeFormat", BASE_DATE_TIME_FORMAT));
            } catch (ConfigServiceException e) {
                default_local_date_time_formatter = DateTimeFormatter.ofPattern(BASE_DATE_TIME_FORMAT);
            }
        }
        return default_local_date_time_formatter;
    }

    @SuppressWarnings("unused")
    public String formatDateTimeSeconds(Date d) {
        synchronized (getDateTimeSecondsFormatter()) {
            return getDateTimeSecondsFormatter().format(d);
        }
    }

    private static SimpleDateFormat default_date_time_seconds_format = null;

    public static SimpleDateFormat getDateTimeSecondsFormatter() {
        if (default_date_time_seconds_format == null) {
            try {
                default_date_time_seconds_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("uiDateTimeSecondsFormat", "MM/dd/yyyy HH:mm:ss.SSS"));
            } catch (ConfigServiceException e) {
                default_date_time_seconds_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            }
        }
        return default_date_time_seconds_format;
    }

    @SuppressWarnings("unused")
    public String formatTime(Date d) {
        synchronized (getTimeFormatter()) {
            return getTimeFormatter().format(d);
        }
    }

    private static SimpleDateFormat default_time_format = null;

    public static SimpleDateFormat getTimeFormatter() {
        if (default_time_format == null) {
            try {
                default_time_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("uiTimeFormat", "HH:mm:ss"));
            } catch (ConfigServiceException e) {
                default_time_format = new SimpleDateFormat("HH:mm:ss");
            }
        }
        return default_time_format;
    }

    public boolean resourceExists(String screen) {
        return Velocity.resourceExists(screen);
    }

    public String validateTemplate(String screen, String project) {
        if (StringUtils.isNotBlank(project)) {
            final String key = (screen.endsWith(".vm") ? screen.substring(0, screen.length() - 3) : screen) + "_" + project + ".vm";
            if (isValidResourceKey(key)) {
                return key;
            }
        }
        final String key = screen.endsWith(".vm") ? screen : screen + ".vm";
        return isValidResourceKey(key) ? key : null;
    }

    /**
     * Object type disambiguation helper.
     *
     * @param objectModel The object model.
     * @param projectId   The project ID.
     *
     * @return The display ID if it can be determined.
     */
    public String getProjectDisplayID(final Object objectModel, final String projectId) {
        if (objectModel == null) {
            return "";
        }
        // Can we call the getProject(String, boolean) method on this object? If so, then call the getDisplayID() method
        // on the resulting project object.
        try {
            final Object project = getProject(objectModel, projectId);
            if (project != null) {
                final Method getDisplayID = project.getClass().getMethod("getDisplayID");
                return (String) getDisplayID.invoke(project);
            }
        } catch (NoSuchMethodException ignored) {
            // If this doesn't exist, we'll just move onto the next one.
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something went wrong invoking the project getDisplayID() method.", e);
        }
        // OK, if not that, then let's see if there's a getDisplayID() method (i.e. this is a project itself). We can
        // use that directly to get the display ID.
        try {
            final Method getDisplayID = objectModel.getClass().getMethod("getDisplayID");
            return (String) getDisplayID.invoke(objectModel);
        } catch (NoSuchMethodException e) {
            //
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something went wrong invoking the getDisplayID() method.", e);
        }
        return projectId;
    }

    /**
     * Object type disambiguation helper.
     *
     * @param objectModel The object model.
     * @param projectId   The project ID.
     *
     * @return The display ID if it can be determined.
     */
    public String getProjectName(final Object objectModel, final String projectId) {
        if (objectModel == null) {
            return "";
        }
        // Can we call the getProject(String, boolean) method on this object? If so, then call the getDisplayID() method
        // on the resulting project object.
        try {
            final Object project = getProject(objectModel, projectId);
            if (project != null) {
                final Method getName = project.getClass().getMethod("getName");
                return (String) getName.invoke(project);
            }
        } catch (NoSuchMethodException ignored) {
            // If this doesn't exist, we'll just move onto the next one.
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something went wrong invoking the project getName() method.", e);
        }
        // OK, if not that, then let's see if there's a getDisplayID() method (i.e. this is a project itself). We can
        // use that directly to get the display ID.
        try {
            final Method getName = objectModel.getClass().getMethod("getName");
            return (String) getName.invoke(objectModel);
        } catch (NoSuchMethodException e) {
            //
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something went wrong invoking the getName() method.", e);
        }
        return projectId;
    }

    public Object getProject(final Object objectModel, final String projectId) {
        try {
            final Method getProject = objectModel.getClass().getMethod("getProject", String.class, Boolean.class);
            return getProject.invoke(objectModel, projectId, false);
        } catch (NoSuchMethodException ignored) {
            // If this doesn't exist, just return null.
            return null;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException("Something went wrong invoking the project getDisplayID() method.", e);
        }
    }

    public String getConfigValue(final String project, final String toolName, final String path, final boolean inherit, final String defaultValue) {
        return XDAT.getConfigValue(project, toolName, path, inherit, defaultValue);
    }

    public boolean getBooleanConfigValue(String project, String toolName, String path, boolean inherit, boolean _default) {
        return XDAT.getBooleanConfigValue(project, toolName, path, inherit, _default);
    }

    public String getPreferenceValue(final String toolId, final String path) {
        return XDAT.getPreferenceValue(toolId, path);
    }

    public boolean getBooleanPreferenceValue(final String toolId, final String path, final boolean defaultValue) {
        return XDAT.getBooleanPreferenceValue(toolId, path, defaultValue);
    }

    public String getConfigValue(String project, String toolName, String path, boolean inherit, String key, String _default) {
        String thevalue = TurbineUtils.GetInstance().getConfigValue(project, toolName, path, inherit, _default);
        if (StringUtils.equals(_default, thevalue)) {
            return _default;
        }
        try {
            JSONObject obj = new JSONObject(thevalue);
            if (StringUtils.isEmpty(key)) {
                return thevalue;
            } else {

                return obj.getString(key);
            }
        } catch (JSONException ex) {
            return _default;
        }
    }

    public String getTemplateName(String module, String dataType, String project) {
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
            String                      temp = validateTemplate(safeJoin("/screens", root.getSQLName(), root.getSQLName() + module), project);
            if (temp != null) {
                return temp;
            }

            temp = validateTemplate(safeJoin("/screens", root.getSQLName(), module), project);
            if (temp != null) {
                return temp;
            }

            for (ArrayList primary : root.getExtendedElements()) {
                GenericWrapperElement p = ((SchemaElementI) primary.get(0)).getGenericXFTElement();
                temp = validateTemplate(Paths.get("/screens", p.getSQLName(), p.getSQLName() + module).toString(), project);
                if (temp != null) {
                    return temp;
                }

                temp = validateTemplate(Paths.get("/screens", p.getSQLName(), module).toString(), project);
                if (temp != null) {
                    return temp;
                }
            }
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("", e);
        }

        return null;
    }

    protected final Map<String, List<Properties>> cachedVMS = new Hashtable<>();

    /**
     * Note: much of this was copied from SecureScreen.  This version looks at the other templates directories (not just templates).  We may want to merge the two impls.
     *
     * @param subFolder Like topBar/admin
     *
     * @return The properties extracted from the template.
     */
    public List<Properties> getTemplates(String subFolder) {
        //first see if the props have been cached.
        List<Properties> screens         = cachedVMS.get(subFolder);
        List<String>     _defaultScreens = new ArrayList<>();
        if (screens == null) {
            synchronized (this) {
                //synchronized so that two calls don't overwrite each other.  I only synchronized this chunk in hopes that when the screens list is cached, the block wouldn't occur.
                //need to build the list of props.
                screens = new ArrayList<>();
                List<String> exists = new ArrayList<>();

                for (final String path : CustomClasspathResourceLoader.TEMPLATE_PATHS) {
                    String forwardSlashSubFolder = subFolder;
                    if (forwardSlashSubFolder != null) {
                        forwardSlashSubFolder = subFolder.replace("\\", "/");
                    }
                    List<URL> uris = CustomClasspathResourceLoader.findVMsByClasspathDirectory("screens" + "/" + forwardSlashSubFolder);
                    if (uris.size() > 0) {
                        for (final URL url : uris) {
                            final String fileName = FilenameUtils.getBaseName(url.toString()) + "." + FilenameUtils.getExtension(url.toString());
                            final String resolved = safeJoin(forwardSlashSubFolder, fileName);
                            if (!exists.contains(resolved)) {
                                try {
                                    // TODO: It looks like the critical test is whether the input stream is null.
                                    SecureScreen.addProps(fileName, CustomClasspathResourceLoader.getInputStream("screens/" + resolved), screens, _defaultScreens, resolved);
                                    exists.add(resolved);
                                } catch (FileNotFoundException e) {
                                    //this shouldn't happen
                                } catch (ResourceNotFoundException e) {
                                    logger.error("", e);
                                }
                            }
                        }
                    }
                    final File screensFolder = XDAT.getScreenTemplateFolder(path);
                    if (screensFolder.exists()) {
                        File subFile = new File(screensFolder, subFolder);
                        if (subFile.exists()) {
                            final File[] files = subFile.listFiles((folder, name) -> name.endsWith(".vm"));

                            if (files != null) {
                                for (File f : files) {
                                    String subpath             = Paths.get(subFolder, f.getName()).toString();
                                    String forwardSlashSubPath = subpath;
                                    if (forwardSlashSubPath != null) {
                                        forwardSlashSubPath = subpath.replace("\\", "/");
                                    }
                                    if (!exists.contains(forwardSlashSubPath)) {  // ...so that it matches the resolved path string above and doesn't add duplicates
                                        try {
                                            SecureScreen.addProps(f, screens, _defaultScreens, subpath);
                                            exists.add(forwardSlashSubPath);
                                        } catch (FileNotFoundException e) {
                                            //this shouldn't happen
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                screens.sort(PROPERTIES_COMPARATOR);
                cachedVMS.put(subFolder, screens);
            }
        }
        return screens;
    }

    private boolean containsPropByProperty(final List<Properties> props, final Properties prop, final String property) {
        return prop.containsKey(property) && (CollectionUtils.find(props, arg0 -> ((Properties) arg0).getProperty(property) != null && Objects.equals(prop.getProperty(property), ((Properties) arg0).getProperty(property))) != null);
    }

    private void mergePropsNoOverwrite(final List<Properties> props, final List<Properties> add, final String property) {
        for (final Properties p : add) {
            if (!containsPropByProperty(props, p, property)) {
                props.add(p);
            }
        }
    }

    /**
     * Looks for templates in the give subFolder underneath the give dataType in the xdat-templates, xnat-templates, or templates.
     * dataType/subFolder
     *
     * @param dataType  The data type for which to search.
     * @param subFolder The subfolder to search.
     *
     * @return The properties for the templates.
     */
    @SuppressWarnings("unchecked")
    public List<Properties> getTemplates(String dataType, String subFolder) {
        List<Properties> props = new ArrayList<>();
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);

            final String           subFolderPath = safeJoin(root.getSQLName(), subFolder);
            final List<Properties> templates     = getTemplates(subFolderPath);
            props.addAll(templates);
            mergePropsNoOverwrite(props, templates, "fileName");

            for (final List<Object> primary : root.getExtendedElements()) {
                final GenericWrapperElement p = ((SchemaElementI) primary.get(0)).getGenericXFTElement();
                mergePropsNoOverwrite(props, getTemplates(safeJoin(p.getSQLName(), subFolder)), "fileName");
            }

            props.sort(PROPERTIES_COMPARATOR);
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("", e);
        }
        return props;
    }

    @SuppressWarnings("unused")
    public boolean validateClasspathTemplate(String screen) {
        if (screen != null) {
            final String    modScreen        = screen.replace("\\", "/");
            final String    templateFileName = screen.substring(modScreen.lastIndexOf('/') + 1);
            final String    forwardSlashDir  = modScreen.substring(0, modScreen.lastIndexOf('/'));
            final List<URL> uris             = CustomClasspathResourceLoader.findVMByClasspathDirectoryAndFileName(forwardSlashDir, templateFileName);
            return uris.size() > 0;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public String getTemplateName(String module, String dataType, String project, String subFolder) {
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
            String                      temp = validateTemplate(safeJoin("/screens", root.getSQLName(), subFolder, root.getSQLName() + module), project);
            if (temp != null) {
                return temp;
            }

            temp = validateTemplate(safeJoin("/screens", root.getSQLName(), subFolder, module), project);
            if (temp != null) {
                return temp;
            }

            for (List<Object> primary : root.getExtendedElements()) {
                final GenericWrapperElement p = ((SchemaElementI) primary.get(0)).getGenericXFTElement();
                temp = validateTemplate(safeJoin("/screens", p.getSQLName(), subFolder, p.getSQLName() + module), project);
                if (temp != null) {
                    return temp;
                }

                temp = validateTemplate(safeJoin("/screens", p.getSQLName(), subFolder, module), project);
                if (temp != null) {
                    return temp;
                }
            }
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("", e);
        }

        return null;
    }

    public String formatDate(long d, String pattern) {
        return formatDate(new Date(d), pattern);
    }

    @SuppressWarnings("unused")
    public String formatNumber(Object o, int roundTo) {
        final NumberFormat formatter = NumberFormat.getInstance();
        if (o == null) {
            return "";
        }
        if (o instanceof String) {
            try {
                o = formatter.parse((String) o);
            } catch (ParseException e) {
                logger.error("", e);
                return o.toString();
            }
        }

        if (o instanceof Number) {
            final Number n = (Number) o;
            formatter.setGroupingUsed(false);
            formatter.setMaximumFractionDigits(roundTo);
            formatter.setMinimumFractionDigits(roundTo);
            return formatter.format(n);
        } else {
            return o.toString();
        }
    }

    public static String escapeParam(String o) {
        return (o == null) ? null : StringEscapeUtils.escapeXml11(o);
    }

    public static Object escapeParam(Object o) {
        if (o instanceof String) {
            return escapeParam((String) o);
        } else {
            return o;
        }
    }

    public static String unescapeParam(String o) {
        return (o == null) ? null : StringEscapeUtils.unescapeXml(o);
    }

    /**
     * If a value is placed into a form field via JavaScript, it must be unescaped first,
     * otherwise the value will be XML-encoded, and it will be double-encoded on re-transmission to the server.
     * (e.g. "&quot;" will become "&amp;quot;").
     * This is not necessary for form fields populated via HTML, as the browser will automatically decode the entities.
     *
     * @param param The parameter to be unescaped.
     *
     * @return The input string, with any XML entities decoded.
     */
    public static Object unescapeParam(Object param) {
        if (param instanceof String) {
            return unescapeParam((String) param);
        } else {
            return param;
        }
    }

    public String escapeHTML(String o) {
        return (o == null) ? null : StringEscapeUtils.escapeHtml4(o);
    }

    public String escapeJS(String o) {
        return (o == null) ? null : StringEscapeUtils.escapeEcmaScript(o);
    }

    public static void setBannerMessage(final RunData data, final String message) {
        data.getSession().setAttribute("bannerMessage", message);
    }

    public String getBannerMessage(final RunData data) {
        final HttpSession session = data.getSession();
        try {
            return StringUtils.replace(StringUtils.defaultIfBlank((String) session.getAttribute("bannerMessage"), ""), "'", "\\'");
        } finally {
            session.removeAttribute("bannerMessage");
        }
    }

    public int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * Redirects to the page indicated by the <b>template.login</b> Turbine property.
     *
     * @param data The data object for the screen or action class.
     */
    public static void redirectToLogin(final RunData data) {
        final String loginTemplate = Turbine.getConfiguration().getString("template.login");
        data.setScreenTemplate(StringUtils.defaultIfBlank(loginTemplate, Turbine.getConfiguration().getString("screen.login")));
    }

    /**
     * Sets the Content-Disposition response header. The filename parameter indicates the name of the content.
     * This method specifies the content as an attachment. If you need to specify inline content (e.g. for MIME
     * content in email or embedded content situations), use {@link #setContentDisposition(HttpServletResponse, String, boolean)}.
     *
     * @param response The servlet response on which the header should be set.
     * @param filename The suggested filename for downloaded content.
     */
    public static void setContentDisposition(HttpServletResponse response, String filename) {
        setContentDisposition(response, filename, true);
    }

    /**
     * Sets the Content-Disposition response header. The filename parameter indicates the name of the content.
     * This method specifies the content as an attachment when the <b>isAttachment</b> parameter is set to true,
     * and as inline content when the <b>isAttachment</b> parameter is set to false. You can specify the content
     * as an attachment by default by calling {@link #setContentDisposition(HttpServletResponse, String)}.
     *
     * @param response     The servlet response on which the header should be set.
     * @param filename     The suggested filename for downloaded content.
     * @param isAttachment Indicates whether the content is an attachment or inline.
     */
    public static void setContentDisposition(HttpServletResponse response, String filename, boolean isAttachment) {
        if (response.containsHeader(CONTENT_DISPOSITION)) {
            throw new IllegalStateException("A content disposition header has already been added to this response.");
        }
        response.addHeader(CONTENT_DISPOSITION, createContentDispositionValue(filename, isAttachment));
    }

    /**
     * Creates the value to be set for a content disposition header.
     *
     * @param filename     The filename for the header.
     * @param isAttachment Whether the content is an attachment or inline.
     *
     * @return The value to be set for the content disposition header.
     */
    public static String createContentDispositionValue(final String filename, final boolean isAttachment) {
        return String.format("%s; filename=\"%s\";", isAttachment ? "attachment" : "inline", filename);
    }

    public static boolean isAuthorized(final RunData data, final UserI user, final boolean allowGuestAccess) throws Exception {
        if (user == null) {
            XDAT.setGuestUserDetails();
            final String destination = data.getTemplateInfo().getScreenTemplate();
            data.getParameters().add("nextPage", destination);
            data.getParameters().add("nextAction", StringUtils.defaultIfBlank(data.getAction(), Turbine.getConfiguration().getString("action.login")));
            return allowGuestAccess;
        }
        return !(!allowGuestAccess && user.isGuest());
    }

    public boolean isNullObject(final Object object) {
        return object == null;
    }

    public boolean isBlankString(final String string) {
        return StringUtils.isBlank(string);
    }

    public boolean equals(final String string1, final String string2) {
        return StringUtils.equals(string1, string2);
    }

    public boolean equalsIgnoreCase(final String string1, final String string2) {
        return StringUtils.equalsIgnoreCase(string1, string2);
    }

    public List<Object> sortByName(final List<Object> items) {
        if (null == items) {
            return Collections.emptyList();
        }
        try {
            return items.stream().sorted(Comparator.comparing(o -> {
                try {
                    return o.getClass().getMethod("getName").invoke(o).toString();
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            })).collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.error(e.getMessage(), e);
            return items; // return unsorted list if something happens.
        }
    }

    public String getDisplayedUserIdentifier(UserI user) {
        return ObjectUtils.defaultIfNull(XDAT.getSiteConfigPreferences().getDisplayedUserIdentifierType(),
                DisplayedUserIdentifierType.USERNAME).format(user);
    }

    private static Boolean isPreload(final String elementName) {
        try {
            return SchemaElement.GetElement(elementName).isPreLoad();
        } catch (XFTInitException e) {
            logger.error(XDAT.XFT_INIT_EXCEPTION_MESSAGE, e);
        } catch (ElementNotFoundException e) {
            log.error(XDAT.ELEMENT_NOT_FOUND_MESSAGE, e.ELEMENT, e);
        }
        return false;
    }

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final Comparator<Properties> PROPERTIES_COMPARATOR = (arg0, arg1) -> {
        if (arg0.containsKey("Sequence") && arg1.containsKey("Sequence")) {
            try {
                return Integer.compare(Integer.parseInt(arg0.getProperty("Sequence")), Integer.parseInt(arg1.getProperty("Sequence")));
            } catch (NumberFormatException e) {
                logger.error("Illegal sequence format.", e);
                return 0;
            }
        }
        if (arg0.containsKey("Sequence")) {
            return -1;
        }
        if (arg1.containsKey("Sequence")) {
            return 1;
        }
        return 0;
    };

    private boolean isValidResourceKey(final String key) {
        final boolean isDebugMode = Boolean.parseBoolean(XDAT.safeSiteConfigProperty("debugMode", "false"));

        if (isDebugMode) {
            return resourceExists(key);
        }

        if (!_templates.containsKey(key)) {
            _templates.put(key, resourceExists(key));
        }

        return _templates.get(key);
    }

    private static final Map<String, Boolean> _templates = new ConcurrentHashMap<>();
}
