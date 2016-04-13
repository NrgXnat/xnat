/*
 * org.nrg.xdat.turbine.utils.TurbineUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 11/18/13 9:36 AM
 */
package org.nrg.xdat.turbine.utils;

import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.Turbine;
import org.apache.turbine.services.intake.model.Group;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.parser.ParameterParser;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XdatSecurity;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xdat.schema.SchemaField;
import org.nrg.xdat.search.DisplaySearch;
import org.nrg.xdat.security.XdatStoredSearch;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
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
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLDecoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Tim
 */
public class TurbineUtils {
    public static final String EDIT_ITEM = "edit_item";
    private static final Logger logger = Logger.getLogger(TurbineUtils.class);
    private XdatSecurity _security = null;

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
            } catch (FieldNotFoundException e) {
                logger.error("", e);
                return null;
            } catch (ElementNotFoundException e) {
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
        final String site_id = XFT.GetSiteID();
        if (site_id == null || StringUtils.isEmpty(site_id)) {
            return "XNAT";
        } else {
            return site_id;
        }
    }

    @SuppressWarnings("unused")
    public boolean loginRequired() {
        return XFT.GetRequireLogin();
    }

    @SuppressWarnings("unused")
    public static boolean LoginRequired() {
        return XFT.GetRequireLogin();
    }

    public static ItemI GetItemBySearch(RunData data, boolean preLoad) throws Exception {
        if (data == null) {
            return null;
        }

        //TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
        final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
        final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
        if (searchField != null && searchValue != null) {
            final ItemSearch search = new ItemSearch();
            search.setUser(XDAT.getUserDetails());

            final String elementName = XftStringUtils.GetRootElementName(searchField);

            search.setElement(elementName);
            search.addCriteria(searchField, searchValue);
            search.setAllowMultiples(preLoad);

            final ItemCollection items = search.exec();
            if (items.size() > 0) {
                return items.getFirst();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static SchemaElementI GetSchemaElementBySearch(RunData data) {
        if (data == null) {
            return null;
        }

        //TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
        final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
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
        if (data == null) {
            return null;
        }

        //TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
        final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
        final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
        if (searchField != null && searchValue != null) {
            final ItemSearch search = new ItemSearch();
            search.setUser(XDAT.getUserDetails());

            final String elementName = XftStringUtils.GetRootElementName(searchField);

            final SchemaElementI gwe = SchemaElement.GetElement(elementName);
            search.setElement(elementName);
            search.addCriteria(searchField, searchValue);

            search.setAllowMultiples(gwe.isPreLoad());

            final ItemCollection items = search.exec();
            if (items.size() > 0) {
                ItemI o = items.getFirst();
                //o.extend();
                return (XFTItem) o;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static ItemI GetItemBySearch(RunData data, Boolean preload) throws Exception {
        if (data == null) {
            return null;
        }

        //TurbineUtils.OutputPassedParameters(data,null,"GetItemBySearch()");
        final String searchField = TurbineUtils.escapeParam(data.getParameters().getString("search_field"));
        final Object searchValue = TurbineUtils.escapeParam(data.getParameters().getObject("search_value"));
        if (searchField != null && searchValue != null) {
            final ItemSearch search = new ItemSearch();
            search.setUser(XDAT.getUserDetails());

            final String elementName = XftStringUtils.GetRootElementName(searchField);

            final SchemaElementI gwe = SchemaElement.GetElement(elementName);
            search.setElement(elementName);
            search.addCriteria(searchField, searchValue);

            boolean b;
            if (preload == null) {
                b = gwe.isPreLoad();
            } else {
                b = preload;
            }
            search.setAllowMultiples(b);

            final ItemCollection items = search.exec();
            if (items.size() > 0) {
                return items.getFirst();
            } else {
                return null;
            }
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
        if (XFT.GetSiteURL() == null || XFT.GetSiteURL().equals("")) {
            String s = Turbine.getServerScheme() + "://" + Turbine.getServerName();
            if (!Turbine.getServerPort().equals("80"))
                s += ":" + Turbine.getServerPort();
            s += Turbine.getContextPath();
            return s;
        } else {
            return XFT.GetSiteURL();
        }
    }

    /**
     * Returns server and context as specified in user request object.
     *
     * @param data The run data for the context.
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
     * @return The full server path.
     */
    public static String GetFullServerPath(HttpServletRequest req) {
        if (XFT.GetSiteURL() == null || XFT.GetSiteURL().equals("")) {
            String s = req.getRequestURL().toString();
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
        } else {
            return XFT.GetSiteURL();
        }
    }

    public static String GetContext() {
        return Turbine.getContextPath();
    }

    /**
     * Sets the user details.
     * @param data    The request run data.
     * @deprecated Use {@link XDAT#getUserDetails()} instead.
     */
    @Deprecated
    public static UserI getUser(RunData data) {
        return data == null ? null : XDAT.getUserDetails();
    }

    /**
     * Sets the user details.
     * @param data    The request run data.
     * @param user    The user object.
     * @throws Exception When something goes wrong.
     * @deprecated Use {@link XDAT#setUserDetails(UserI)} instead.
     */
    @SuppressWarnings("UnusedParameters")
    @Deprecated
    public static void setUser(RunData data, UserI user) throws Exception {
        XDAT.setUserDetails(user);
    }

    /**
     * Sets user details for a new user.
     * @param data    The request run data.
     * @param user    The user object.
     * @param context The request context.
     * @throws Exception When something goes wrong.
     * @deprecated Use {@link XDAT#setNewUserDetails(UserI, RunData, Context)} instead.
     */
    public static void setNewUser(RunData data, UserI user, Context context) throws Exception {
        XDAT.setNewUserDetails(user, data, context);
    }

    public boolean checkRole(final UserI user, final String role) {
        return user != null && StringUtils.isNotBlank(role) && Roles.checkRole(user, role);
    }

    public boolean isSiteAdmin(final UserI user) {
        return user != null && Roles.isSiteAdmin(user);
    }

    public boolean isGuest(final UserI user) {
        return user == null || StringUtils.equals("guest", user.getLogin());
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

                    final StringReader sr = new StringReader(search_xml);
                    final InputSource is = new InputSource(sr);
                    final SAXReader reader = new SAXReader(user);
                    final XFTItem item = reader.parse(is);
                    final XdatStoredSearch search = new XdatStoredSearch(item);
                    final DisplaySearch ds = search.getCSVDisplaySearch(user);
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
                        final StringReader sr = new StringReader(search_xml);
                        final InputSource is = new InputSource(sr);
                        final SAXReader reader = new SAXReader(user);
                        final XFTItem item = reader.parse(is);
                        final XdatStoredSearch search = new XdatStoredSearch(item);
                        final DisplaySearch ds = search.getDisplaySearch(user);
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
            final SchemaField sf = (SchemaField) se.getAllPrimaryKeys().get(0);
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
            final SchemaField sf = (SchemaField) se.getAllPrimaryKeys().get(0);
            context.put("search_field", StringUtils.replace(StringUtils.replace(sf.getXMLPathString(se.getFullXMLName()), "/", "."), "@", "."));
            final Object o = item.getProperty(sf.getId());
            context.put("search_value", o.toString());
        } catch (Exception e) {
            logger.error("", e);
        }
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
        final Map<String, String> hash = new Hashtable<>();
        ParameterParser pp = data.getParameters();
        Enumeration<Object> penum = pp.keys();
        while (penum.hasMoreElements()) {
            final String key = penum.nextElement().toString();
            final Object value = TurbineUtils.escapeParam(data.getParameters().get(key));
            if (value != null && !value.equals(""))
                hash.put(TurbineUtils.escapeParam(key), value.toString());
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
     * @param group    The group to display.
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
     * @param context    The context to display.
     */
    public static void OutputContextParameters(Context context) {
        if (context != null) {
            logger.debug("\n\nContext Parameters");
            for (int i = 0; i < context.getKeys().length; i++) {
                logger.debug("KEY: " + context.getKeys()[i].toString() + " VALUE: " + context.get(context.getKeys()[i].toString()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static void OutputSessionParameters(RunData data) {
        if (data != null) {
            logger.debug("\n\nSession Parameters");
            final Enumeration<String> enumer = data.getSession().getAttributeNames();
            while (enumer.hasMoreElements()) {
                final String key = enumer.nextElement();
                final Object o = data.getSession().getAttribute(key);
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
        final Collection<String> _ret = Lists.newArrayList();
        final String[] v = data.getParameters().getStrings(s);
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
        if (data.getParameters().get(s.toLowerCase()) != null) {
            final Object o = TurbineUtils.escapeParam(data.getParameters().get(s.toLowerCase()));
            if (o.toString().equalsIgnoreCase("")) {
                return defualt;
            } else {
                return o;
            }
        } else {
            return defualt;
        }
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

    @SuppressWarnings("unused")
    public String[] toList(String s) {
        return s.split(",");
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
                default_date_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("UI.date-format", "MM/dd/yyyy"));
            } catch (ConfigServiceException e) {
                default_date_format = new SimpleDateFormat("MM/dd/yyyy");
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
                default_date_time_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("UI.date-time-format", "MM/dd/yyyy HH:mm:ss"));
            } catch (ConfigServiceException e) {
                default_date_time_format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            }
        }
        return default_date_time_format;
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
                default_date_time_seconds_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("UI.date-time-seconds-format", "MM/dd/yyyy HH:mm:ss.SSS"));
            } catch (ConfigServiceException e) {
                default_date_time_seconds_format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");
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
                default_time_format = new SimpleDateFormat(XDAT.getSiteConfigurationProperty("UI.time-format", "HH:mm:ss"));
            } catch (ConfigServiceException e) {
                default_time_format = new SimpleDateFormat("HH:mm:ss");
            }
        }
        return default_time_format;
    }

    public boolean resourceExists(String screen){
    	return Velocity.resourceExists(screen);
    }
    
    public String validateTemplate(String screen, String project) {
        if (screen.endsWith(".vm")) {
            screen = screen.substring(0, screen.length() - 3);
        }

        if (project != null && Velocity.resourceExists(screen + "_" + project + ".vm")) {
            return screen + "_" + project + ".vm";
        } else {
            if (Velocity.resourceExists(screen + ".vm")) {
                return screen + ".vm";
            } else {
                return null;
            }
        }
    }

    /**
     * Object type disambiguation helper.
     *
     * @param objectModel The object model.
     * @param projectId   The project ID.
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

    public String getTemplateName(String module, String dataType, String project) {
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
            String temp = validateTemplate("/screens/" + root.getSQLName() + "/" + root.getSQLName() + module, project);
            if (temp != null) {
                return temp;
            }

            temp = validateTemplate("/screens/" + root.getSQLName() + "/" + module, project);
            if (temp != null) {
                return temp;
            }

            for (ArrayList primary : root.getExtendedElements()) {
                GenericWrapperElement p = ((SchemaElementI) primary.get(0)).getGenericXFTElement();
                temp = validateTemplate("/screens/" + p.getSQLName() + "/" + p.getSQLName() + module, project);
                if (temp != null) {
                    return temp;
                }

                temp = validateTemplate("/screens/" + p.getSQLName() + "/" + module, project);
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
     * @return The properties extracted from the template.
     */
    public List<Properties> getTemplates(String subFolder) {
        //first see if the props have been cached.
        List<Properties> screens = cachedVMS.get(subFolder);
        List<String> _defaultScreens = new ArrayList<>();
        if (screens == null) {
            synchronized (this) {
                //synchronized so that two calls don't overwrite eachother.  I only synchronized this chunk in hopes that when the screens list is cached, the block woudn't occur.
                //need to build the list of props.
                screens = new ArrayList<>();
                List<String> exists = new ArrayList<>();
                List<File> screensFolders = XDAT.getScreenTemplateFolders();
                for (File screensFolder : screensFolders) {
                    if (screensFolder.exists()) {
                        File subFile = new File(screensFolder, subFolder);
                        if (subFile.exists()) {
                            File[] files = subFile.listFiles(new FilenameFilter() {
                                @Override
                                public boolean accept(File folder, String name) {
                                    return name.endsWith(".vm");
                                }
                            });

                            if (files != null) {
                                for (File f : files) {
                                    String path = subFolder + "/" + f.getName();
                                    if (!exists.contains(path)) {
                                        try {
                                            SecureScreen.addProps(f, screens, _defaultScreens, path);
                                            exists.add(path);
                                        } catch (FileNotFoundException e) {
                                            //this shouldn't happen
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                //add paths for files on the classpath
                List<URL> uris = CustomClasspathResourceLoader.findVMsByClasspathDirectory("screens" + "/" + subFolder);
                for (URL url : uris) {
                    String fileName = FilenameUtils.getBaseName(url.toString()) + "." + FilenameUtils.getExtension(url.toString());
                    String path = CustomClasspathResourceLoader.safeJoin("/", subFolder, fileName);
                    if (!exists.contains(path)) {
                        try {
                            SecureScreen.addProps(fileName, CustomClasspathResourceLoader.getInputStream("screens/" + path), screens, _defaultScreens, path);
                            exists.add(path);
                        } catch (FileNotFoundException e) {
                            //this shouldn't happen
                        } catch (ResourceNotFoundException e) {
                            logger.error("", e);
                        }
                    }
                }

                Collections.sort(screens, PROPERTIES_COMPARATOR);

                cachedVMS.put(subFolder, screens);
            }
        }
        return screens;
    }

    private boolean containsPropByProperty(final List<Properties> props, final Properties prop, final String property) {
        return prop.containsKey(property) && (CollectionUtils.find(props, new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return ((Properties) arg0).getProperty(property) != null && Objects.equals(prop.getProperty(property), ((Properties) arg0).getProperty(property));
            }
        }) != null);
    }

    private void mergePropsNoOverwrite(final List<Properties> props, final List<Properties> add, final String property) {
        for (final Properties p : add) {
            if (!containsPropByProperty(props, p, property)) {
                props.add(p);
            }
        }
    }

    /**
     * Looks for templates in the give subFolder underneath the give dataType in the xdat-templatea, xnat-templates, or templates.
     * dataType/subFolder
     *
     * @param dataType     The data type for which to search.
     * @param subFolder    The subfolder to search.
     * @return The properties for the templates.
     */
    @SuppressWarnings("unchecked")
    public List<Properties> getTemplates(String dataType, String subFolder) {
        List<Properties> props = Lists.newArrayList();
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);

            props.addAll(getTemplates(root.getSQLName() + "/" + subFolder));
            mergePropsNoOverwrite(props, getTemplates(root.getSQLName() + "/" + subFolder), "fileName");

            for (final List<Object> primary : root.getExtendedElements()) {
                final GenericWrapperElement p = ((SchemaElementI) primary.get(0)).getGenericXFTElement();
                mergePropsNoOverwrite(props, getTemplates(p.getSQLName() + "/" + subFolder), "fileName");
            }

            Collections.sort(props, PROPERTIES_COMPARATOR);
        } catch (XFTInitException | ElementNotFoundException e) {
            logger.error("", e);
        }
        return props;
    }

    @SuppressWarnings("unchecked")
    public String getTemplateName(String module, String dataType, String project, String subFolder) {
        try {
            final GenericWrapperElement root = GenericWrapperElement.GetElement(dataType);
            String temp = validateTemplate("/screens/" + root.getSQLName() + "/" + subFolder + "/" + root.getSQLName() + module, project);
            if (temp != null) {
                return temp;
            }

            temp = validateTemplate("/screens/" + root.getSQLName() + "/" + subFolder + "/" + module, project);
            if (temp != null) {
                return temp;
            }

            for (List<Object> primary : root.getExtendedElements()) {
                final GenericWrapperElement p = ((SchemaElementI) primary.get(0)).getGenericXFTElement();
                temp = validateTemplate("/screens/" + p.getSQLName() + "/" + subFolder + "/" + p.getSQLName() + module, project);
                if (temp != null) {
                    return temp;
                }

                temp = validateTemplate("/screens/" + p.getSQLName() + "/" + subFolder + "/" + module, project);
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
        if (o instanceof String)
            return escapeParam((String) o);
        else
            return o;
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
     * @param param    The parameter to be unescaped.
     * @return The input string, with any XML entities decoded.
     */
    public static Object unescapeParam(Object param) {
        if (param instanceof String)
            return unescapeParam((String) param);
        else
            return param;
    }

    public String escapeHTML(String o) {
        return (o == null) ? null : StringEscapeUtils.escapeHtml4(o);
    }

    public String escapeJS(String o) {
        return (o == null) ? null : StringEscapeUtils.escapeEcmaScript(o);
    }

    public int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
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
    @SuppressWarnings("unchecked")
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
     * @return The value to be set for the content disposition header.
     */
    public static String createContentDispositionValue(final String filename, final boolean isAttachment) {
        return String.format("%s; filename=\"%s\";", isAttachment ? "attachment" : "inline", filename);
    }

    public static boolean isAuthorized(final RunData data, final UserI user, final boolean allowGuestAccess) throws Exception {
        if (user ==null) {
            HttpSession session = data.getSession();
            session.removeAttribute("loggedin");

            UserI guest= Users.getGuest();
            if (guest!=null) {
                XDAT.setUserDetails(guest);
                session.setAttribute("XNAT_CSRF", UUID.randomUUID().toString());

                String Destination = data.getTemplateInfo().getScreenTemplate();
                data.getParameters().add("nextPage", Destination);
                if (!data.getAction().equalsIgnoreCase("")) {
                    data.getParameters().add("nextAction", data.getAction());
                } else {
                    data.getParameters().add("nextAction", Turbine.getConfiguration().getString("action.login"));
                }
            }
            return allowGuestAccess;
        } else {
            return !(!allowGuestAccess && user.getLogin().equals("guest"));
        }
    }

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final Comparator<Properties> PROPERTIES_COMPARATOR = new Comparator<Properties>() {
        @Override
        public int compare(Properties arg0, Properties arg1) {
            if (arg0.containsKey("Sequence") && arg1.containsKey("Sequence")) {
                try {
                    Integer sequence1 = Integer.parseInt(arg0.getProperty("Sequence"));
                    Integer sequence2 = Integer.parseInt(arg1.getProperty("Sequence"));
                    return sequence1.compareTo(sequence2);
                } catch (NumberFormatException e) {
                    logger.error("Illegal sequence format.", e);
                    return 0;
                }
            } else if (arg0.containsKey("Sequence")) {
                return -1;
            } else if (arg1.containsKey("Sequence")) {
                return 1;
            } else {
                return 0;
            }
        }
    };
}
