/*
 * web: org.nrg.xnat.restlet.resources.SecureResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.collect.Maps;
import com.noelios.restlet.http.HttpConstants;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.TurbineException;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.turbine.utils.AccessLogger;
import org.nrg.xdat.turbine.utils.PopulateItem;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.db.DBAction;
import org.nrg.xft.db.MaterializedView;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.event.EventDetails;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.ItemJSONBuilder;
import org.nrg.xft.schema.Wrappers.XMLWrapper.SAXReader;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xft.utils.zip.ZipUtils;
import org.nrg.xnat.helpers.FileWriterWrapper;
import org.nrg.xnat.itemBuilders.WorkflowBasedHistoryBuilder;
import org.nrg.xnat.restlet.XnatItemRepresentation;
import org.nrg.xnat.restlet.XnatTableRepresentation;
import org.nrg.xnat.restlet.representations.*;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.*;
import org.restlet.util.Series;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.*;

@SuppressWarnings("deprecation")
public abstract class SecureResource extends Resource {
    private static final String COMPRESSION = "compression";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final String ACTION = "action";

    public static final String HANDLER = "handler";

    public static Logger logger = Logger.getLogger(SecureResource.class);

    public static List<Variant> STANDARD_VARIANTS = Arrays.asList(new Variant(MediaType.APPLICATION_JSON), new Variant(MediaType.TEXT_HTML), new Variant(MediaType.TEXT_XML));

    public Map<String, String> fieldMapping = new HashMap<>();

    // TODO: these should be proper extension types: application/x-xList, application/x-xcat+xml, application/x-xar
    public static final MediaType APPLICATION_XLIST = MediaType.register(
            "application/xList", "XNAT Listing");

    public static final MediaType APPLICATION_XCAT = MediaType.register(
            "application/xcat", "XNAT Catalog");

    public static final MediaType APPLICATION_XAR = MediaType.register(
            "application/xar", "XAR Archive");

    public static final MediaType APPLICATION_DICOM = MediaType.register(
            "application/dicom", "Digital Imaging and Communications in Medicine");


    public static final MediaType APPLICATION_XMIRC = MediaType.register(
            "application/x-mirc", "MIRC");

    public static final MediaType APPLICATION_XMIRC_DICOM = MediaType.register(
            "application/x-mirc-dicom", "MIRC DICOM");

    public static final MediaType TEXT_CSV = MediaType.register("text/csv", "CSV");


    protected List<String> actions = null;
    public String userName = null;
    private UserI user = null;
    public String requested_format = null;
    public String filepath = null;

    private final SerializerService _serializer;

    public SecureResource(Context context, Request request, Response response) {
        super(context, request, response);

        _serializer = XDAT.getContextService().getBean(SerializerService.class);
        if (null == _serializer) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "Serializer service was not properly initialized.");
            throw new NrgServiceRuntimeException("ERROR: Serializer service was not properly initialized.");
        }

        requested_format = getQueryVariable("format");

        // expects that the user exists in the session (either via traditional
        // session or set via the XnatSecureGuard
        user = XDAT.getUserDetails();
        filepath = getRequest().getResourceRef().getRemainingPart();
        if (filepath != null) {
            if (filepath.contains("?")) {
                filepath = filepath.substring(0, filepath.indexOf("?"));
            }
            if (filepath.startsWith("/")) {
                filepath = filepath.substring(1);
            }

            filepath = TurbineUtils.escapeParam(filepath);
        }
        logAccess();
    }

    public static Object getParameter(Request request, String key) {
        return TurbineUtils.escapeParam(request.getAttributes().get(key));
    }

    public static String getUrlEncodedParameter(Request request, String key) {
        try {
            String param = (String) request.getAttributes().get(key);
            return param == null ? null : TurbineUtils.escapeParam(URLDecoder.decode(param, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public void logAccess() {
        String url = getRequest().getResourceRef().toString();

        if (!(Method.GET.equals(getRequest().getMethod()) && url.contains("resources/SNAPSHOTS"))) {
            String login = "";
            if (user != null) {
                login = user.getLogin();
            }

            AccessLogger.LogServiceAccess(login, getRequest().getClientInfo().getAddress(), getRequest().getMethod() + " " + url, "");
        }
    }

    public MediaType getRequestedMediaType() {
        if (requested_format != null) {
            if (requested_format.equalsIgnoreCase("xml")) {
                return MediaType.TEXT_XML;
            } else if (requested_format.equalsIgnoreCase("json")) {
                return MediaType.APPLICATION_JSON;
            } else if (requested_format.equalsIgnoreCase("csv")) {
                return TEXT_CSV;
            } else if (requested_format.equalsIgnoreCase("txt")) {
                return MediaType.TEXT_PLAIN;
            } else if (requested_format.equalsIgnoreCase("html")) {
                return MediaType.TEXT_HTML;
            } else if (requested_format.equalsIgnoreCase("zip")) {
                return MediaType.APPLICATION_ZIP;
            } else if (requested_format.equalsIgnoreCase("tar.gz")) {
                return MediaType.APPLICATION_GNU_TAR;
            } else if (requested_format.equalsIgnoreCase("tar")) {
                return MediaType.APPLICATION_TAR;
            } else if (requested_format.equalsIgnoreCase("xList")) {
                return APPLICATION_XLIST;
            } else if (requested_format.equalsIgnoreCase("xcat")) {
                return APPLICATION_XCAT;
            } else if (requested_format.equalsIgnoreCase("xar")) {
                return APPLICATION_XAR;
            } else if (MediaType.valueOf(requested_format) != null) {
                return MediaType.valueOf(requested_format);
            }
        }
        return null;
    }

    public boolean isZIPRequest() {
        return isZIPRequest(getRequestedMediaType());
    }

    public static boolean isZIPRequest(MediaType mt) {
        return !(mt == null || !(mt.equals(MediaType.APPLICATION_ZIP) || mt.equals(MediaType.APPLICATION_TAR) || mt.equals(MediaType.APPLICATION_GNU_TAR)));
    }

    private Form f = null;

    /**
     * This method is used internally to get the Query form.  It should remain private so that all access to parameters are guaranteed to be properly escaped.
     *
     * @return The query variables as a Form object.
     */
    private Form getQueryVariableForm() {
        if (f == null) {
            f = getQueryVariableForm(getRequest());
        }
        return f;
    }

    private static Form getQueryVariableForm(Request request) {
        return request.getResourceRef().getQueryAsForm();
    }

    public Map<String, String> getQueryVariableMap() {
        return convertFormToMap(getQueryVariableForm());
    }

    protected SerializerService getSerializer() {
        return _serializer;
    }

    private Form _body;
    private MediaType _mediaType;

    /**
     * This method is used internally to get the Body form.  It should remain private so that all access to parameters are guaranteed to be properly escaped.
     *
     * @return The body variables as a Form object.
     */
    private Form getBodyAsForm() {
        if (_body == null) {
            Representation entity = getRequest().getEntity();

            if (entity != null && RequestUtil.isMultiPartFormData(entity) && entity.getSize() > 0) {
                _mediaType = entity.getMediaType();
                _body = new Form(entity);
            }
        }

        return _body;
    }

    protected MediaType getMediaType() {
        return _mediaType;
    }

    private static Map<String, String> convertFormToMap(Form q) {
        Map<String, String> map = Maps.newLinkedHashMap();
        if (q != null) {
            for (String s : q.getValuesMap().keySet()) {
                map.put(s, TurbineUtils.escapeParam(q.getFirstValue(s)));
            }
        }
        return map;
    }

    private static Map<String, List<String>> convertFormToMapOfLists(Form q) {
        Map<String, List<String>> map = Maps.newLinkedHashMap();
        if (q != null) {
            for (final String s : q.getValuesMap().keySet()) {
                final List<String> values = new ArrayList<>();
                final String[] all = q.getValuesArray(s);
                for (final String item : all) {
                    values.add(TurbineUtils.escapeParam(item));
                }
                map.put(s, values);
            }
        }
        return map;
    }

    /**
     * This creates an instance of the specified class and populates it from the mapped form data. Each key in the map
     * is treated as a property name. If a setter method for any of the specified properties is not found, an exception
     * will be thrown.
     *
     * @param clazz The type of the class you want to create.
     * @return The initialized instance of the specified class type.
     */
    @SuppressWarnings("unused")
    public <T> T createObjectFromFormData(final Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        final Map<String, List<String>> formData = getFullBodyFormAsMap();
        T instance = clazz.newInstance();
        BeanUtils.populate(instance, formData);
        return instance;
    }

    public Map<String, List<String>> getFullBodyFormAsMap() {
        return convertFormToMapOfLists(getBodyAsForm());
    }

    public Map<String, String> getBodyVariableMap() {
        return convertFormToMap(getBodyAsForm());
    }

    public String getBodyVariable(String key) {
        Form f = getBodyAsForm();
        if (f != null) {
            return TurbineUtils.escapeParam(f.getFirstValue(key));
        }
        return null;
    }

    private static String[] getVariablesFromForm(Form f, String key) {
        if (f != null) {
            String[] values = f.getValuesArray(key).clone();
            for (int i = 0; i < values.length; i++) {
                values[i] = TurbineUtils.escapeParam(values[i]);
            }
            return f.getValuesArray(key);
        }
        return null;
    }

    public boolean hasBodyVariable(String key) {
        return getBodyVariable(key) != null;
    }

    public Set<String> getBodyVariableKeys() {
        Form f = getBodyAsForm();
        if (f != null) {
            return f.getValuesMap().keySet();
        }
        return null;
    }

    public String getQueryVariable(String key) {
        return getQueryVariable(key, getRequest());
    }

    public String getQueryVariable(String key, String _default) {
        String s = getQueryVariable(key, getRequest());
        return (s == null) ? _default : s;
    }

    public static String getQueryVariable(String key, Request request) {
        Form f = getQueryVariableForm(request);
        if (f != null && f.getValuesMap().containsKey(key)) {
            return TurbineUtils.escapeParam(f.getFirstValue(key));
        }
        return null;
    }

    public boolean containsQueryVariable(String key) {
        return getQueryVariable(key) != null;
    }

    public boolean hasQueryVariable(String key) {
        return containsQueryVariable(key);
    }

    public Map<String, Object> getQueryVariablesAsMap() {
        Map<String, Object> params = new Hashtable<>();
        Form f = getQueryVariableForm();
        if (f != null) {
            for (Parameter p : f) {
                params.put(p.getName(), p.getValue());
            }
        }
        return params;
    }

    public boolean isQueryVariable(String key, String value, boolean caseSensitive) {
        if (getQueryVariable(key) != null) {
            if ((caseSensitive && getQueryVariable(key).equals(value)) || (!caseSensitive && getQueryVariable(key).equalsIgnoreCase(value))) {
                return true;
            }
        }
        return false;
    }

    public String[] getQueryVariables(String key) {
        return getVariablesFromForm(getQueryVariableForm(), key);
    }

    public Set<String> getQueryVariableKeys() {
        Form f = getQueryVariableForm();
        if (f != null) {
            return f.getValuesMap().keySet();
        }
        return null;
    }

    public MediaType overrideVariant(Variant v) {
        MediaType rmt = getRequestedMediaType();
        if (rmt != null) {
            return rmt;
        }

        if (v != null) {
            return v.getMediaType();
        } else {
            return MediaType.TEXT_XML;
        }
    }

    public String getReason() {
        return getQueryVariable(EventUtils.EVENT_REASON);
    }

    public Representation representTable(XFTTable table, MediaType mt, Hashtable<String, Object> params) {
        return representTable(table, mt, params, null);
    }

    @SuppressWarnings("unchecked")
    public Representation representTable(XFTTable table, MediaType mt, Hashtable<String, Object> params, Map<String, Map<String, String>> cp) {
        if (table != null) {
            if (getQueryVariable("sortBy") != null) {
                final String sortBy = getQueryVariable("sortBy");
                table.sort(Arrays.asList(StringUtils.split(sortBy, ',')));
                if (isQueryVariable("sortOrder", "DESC", false) && !mt.equals(APPLICATION_XLIST)) {
                    table.reverse();
                }
            }

            //try to map to an inserted implementation
            Class clazz = getExtensionTableRepresentations().get(mt.toString());
            if (clazz != null) {
                try {
                    Class[] parameterTypes = {XFTTable.class, Map.class, Hashtable.class, MediaType.class};
                    Object[] parameters = {table, cp, params, mt};
                    Constructor<OutputRepresentation> rep = clazz.getConstructor(parameterTypes);

                    return rep.newInstance(parameters);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            if (mt.equals(MediaType.TEXT_XML)) {
                return new XMLTableRepresentation(table, cp, params, MediaType.TEXT_XML);
            } else if (mt.equals(MediaType.APPLICATION_JSON)) {
                return new JSONTableRepresentation(table, cp, params, MediaType.APPLICATION_JSON);
            } else if (mt.equals(MediaType.APPLICATION_EXCEL) || mt.equals(TEXT_CSV)) {
                return new CSVTableRepresentation(table, cp, params, mt);
            } else if (mt.equals(APPLICATION_XLIST)) {
                Representation rep = new HTMLTableRepresentation(table, cp, params, MediaType.TEXT_HTML, false);
                rep.setMediaType(MediaType.TEXT_HTML);
                return rep;
            } else {
                if (mt.equals(MediaType.TEXT_HTML) && hasQueryVariable("requested_screen")) {
                    try {
                        for (String key : this.getQueryVariableKeys()) {
                            params.put(key, this.getQueryVariable(key));
                        }
                        params.put("table", table);
                        params.put("hideTopBar", isQueryVariableTrue("hideTopBar"));
                        return new StandardTurbineScreen(MediaType.TEXT_HTML, getRequest(), user, getQueryVariable("requested_screen"), params);
                    } catch (TurbineException e) {
                        logger.error("", e);
                        return new HTMLTableRepresentation(table, cp, params, MediaType.TEXT_HTML, true);
                    }
                } else {
                    return new HTMLTableRepresentation(table, cp, params, MediaType.TEXT_HTML, true);
                }
            }
        } else {
            Representation rep = new StringRepresentation("", mt);
            rep.setExpirationDate(Calendar.getInstance().getTime());
            return rep;
        }
    }

    public String getCurrentURI() {
        return getRequest().getResourceRef().getPath();
    }

    public void returnRepresentation(Representation representation) {
        getResponse().setEntity(representation);
        Representation selectedRepresentation = getResponse().getEntity();
        if (getRequest().getConditions().hasSome()) {
            final Status status = getRequest().getConditions().getStatus(getRequest().getMethod(), selectedRepresentation);

            if (status != null) {
                getResponse().setStatus(status);
                getResponse().setEntity(null);
            }
        }
    }

    public void returnDefaultRepresentation() {
        returnRepresentation(getRepresentation(getVariants().get(0)));
    }

    public Representation representItem(XFTItem item, MediaType mt, Hashtable<String, Object> metaFields, boolean allowDBAccess, boolean allowSchemaLocation) {
        if (item != null) {
            if (mt.equals(MediaType.TEXT_XML)) {
                return new XMLXFTItemRepresentation(item, MediaType.TEXT_XML, metaFields, allowDBAccess, allowSchemaLocation);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected <T> String toJson(final T instance) throws IOException {
        return getSerializer().toJson(instance);
    }

    public interface ItemHandlerI {
        String getHandlerString();

        Representation handle(XFTItem item, MediaType mt, SecureResource resource);
    }

    private static Map<String, ItemHandlerI> itemHandlers = null;

    public Representation representItem(XFTItem item, MediaType mt) {
        /* Load any classes we have placed in org.nrg.xnat.restlet.representations.item.extensions, and register them as "handlers".
         * If the REST request that returns an object specifies a handler as a query param ("handler=foo") then pass the
         * object and request to whatever item handler class has registered the name "foo".
        */
        if (itemHandlers == null) {
            itemHandlers = Maps.newConcurrentMap();

            try {
                List<Class<?>> handlerClasses = Reflection.getClassesForPackage("org.nrg.xnat.restlet.representations.item.extensions");

                for (Class handler : handlerClasses) {
                    if (ItemHandlerI.class.isAssignableFrom(handler)) {
                        ItemHandlerI instance = (ItemHandlerI) handler.newInstance();
                        itemHandlers.put(instance.getHandlerString(), instance);
                    }
                }
            } catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException e) {
                logger.error("", e);
            }
        }

        if (this.hasQueryVariable(HANDLER)) {
            if (itemHandlers.containsKey(this.getQueryVariable(HANDLER))) {
                ItemHandlerI handler = itemHandlers.get(this.getQueryVariable(HANDLER));
                return handler.handle(item, mt, this);
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid handler definition: " + this.getQueryVariable(HANDLER));
                return null;
            }
        }


        if (mt.equals(MediaType.TEXT_HTML)) {
            try {
                return new ItemHTMLRepresentation(item, MediaType.TEXT_HTML, getRequest(), user, getQueryVariable("requested_screen"), new Hashtable<String, Object>());
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return null;
            }
        } else if (mt.equals(MediaType.APPLICATION_JSON)) {
            try {
                FlattenedItemA.HistoryConfigI history = (isQueryVariableTrue("includeHistory")) ? FlattenedItemA.GET_ALL : new FlattenedItemA.HistoryConfigI() {
                    @Override
                    public boolean getIncludeHistory() {
                        return false;
                    }
                };
                return new JSONObjectRepresentation(MediaType.APPLICATION_JSON, (new ItemJSONBuilder()).call(item, history, isQueryVariableTrue("includeHeaders")));
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return null;
            }
        } else {
            return new ItemXMLRepresentation(item, MediaType.TEXT_XML, true, !isQueryVariableTrue("concealHiddenFields"));
        }
    }

    public MediaType buildMediaType(MediaType mt, String fName) {
        if (fName.endsWith(".txt")) {
            mt = MediaType.TEXT_PLAIN;
        } else if (fName.endsWith(".gif")) {
            mt = MediaType.IMAGE_GIF;
        } else if (fName.endsWith(".jpeg") || fName.endsWith(".jpg")) {
            mt = MediaType.IMAGE_JPEG;
        } else if (fName.endsWith(".xml")) {
            mt = MediaType.TEXT_XML;
        } else if (fName.endsWith(".png")) {
            mt = MediaType.IMAGE_PNG;
        } else if (fName.endsWith(".bmp")) {
            mt = MediaType.IMAGE_BMP;
        } else if (fName.endsWith(".tiff")) {
            mt = MediaType.IMAGE_TIFF;
        } else if (fName.endsWith(".html")) {
            mt = MediaType.TEXT_HTML;
        } else {
            if ((mt != null && mt.equals(MediaType.TEXT_XML)) && !fName.endsWith(".xml")) {
                mt = MediaType.ALL;
            } else {
                mt = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return mt;
    }

    public FileRepresentation representFile(File f, MediaType mt) {
        mt = buildMediaType(mt, f.getName());

        if (mt.getName().startsWith("APPLICATION") || !XDAT.getSiteConfigPreferences().getAllowHtmlResourceRendering()) {
            setContentDisposition(f.getName());
        }

        FileRepresentation fr = new FileRepresentation(f, mt);
        fr.setModificationDate(new Date(f.lastModified()));

        return fr;
    }

    public boolean allowDataDeletion() {
        return getQueryVariable("allowDataDeletion") != null && getQueryVariable("allowDataDeletion").equals("true");
    }

    public boolean populateFromDB() {
        return getQueryVariable("populateFromDB") == null || isQueryVariableTrue("populateFromDB");
    }

    protected boolean completeDocument = false;

    public XFTItem loadItem(String dataType, boolean parseFileItems) throws ClientException, ServerException {
        return loadItem(dataType, parseFileItems, null);
    }

    /**
     * Attempts to generate a XFTItem based on the contents of the submitted request
     * If the content-type is multi-part form data, the form entries will be reviewed for xml files that can be parsed.
     * If the content-type is text/xml OR req_format=xml OR inbody=true, then the body of the message will be parsed as an xml document.
     * If the req_format=form OR content-type is application_www_form, or multi-part_all, then the individual parameters of the submitted form will be reviewed as individual parameters for the XFTItem.
     * No matter which format is submitted, the query string parameters will be parsed to add individual parameters to the generated XFTItem.  These will override values from the previous methods.
     *
     * @param dataType       - xsi:type of object to be created.
     * @param parseFileItems - set to false if you are expecting something else to be in the body of the message, and don't want it parsed.
     * @param template       - item to add parameters to.
     * @return The {@link XFTItem} found in the request body, if any.
     * @throws ClientException - Client Side exception
     * @throws ServerException - Server Side exception
     */
    public XFTItem loadItem(String dataType, boolean parseFileItems, XFTItem template) throws ClientException, ServerException {
        XFTItem item = null;
        if (template != null && populateFromDB()) {
            item = template;
        }

        Representation entity = getRequest().getEntity();

        String req_format = getQueryVariable("req_format");
        if (req_format == null) {
            req_format = "";
        }

        if (parseFileItems) {
            if ((RequestUtil.hasContent(entity) && RequestUtil.compareMediaType(entity, MediaType.MULTIPART_FORM_DATA)) && !req_format.equals("form")) {
                //handle multi part form data (where xml is being submitted as a field in a multi part form)
                //req_format is checked to allow the body parsing to use the form method rather then file fields.
                try {
                    org.apache.commons.fileupload.DefaultFileItemFactory factory = new DefaultFileItemFactory();
                    org.restlet.ext.fileupload.RestletFileUpload upload = new RestletFileUpload(factory);

                    List<FileItem> items = upload.parseRequest(getRequest());

                    for (FileItem fi : items) {
                        if (fi.getName().endsWith(".xml")) {
                            SAXReader reader = new SAXReader(user);
                            if (item != null) {
                                reader.setTemplate(item);
                            }
                            try {
                                item = reader.parse(fi.getInputStream());

                                if (!reader.assertValid()) {
                                    throw reader.getErrors().get(0);
                                }
                                if (XFT.VERBOSE) {
                                    System.out.println("Loaded XML Item:" + item.getProperName());
                                }
                                if (item != null) {
                                    completeDocument = true;
                                }
                            } catch (SAXParseException e) {
                                logger.error("An error occurred parsing the XML", e);
                                getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "An error occurred parsing the XML: " + e.getMessage());
                                throw new ClientException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
                            } catch (IOException e) {
                                logger.error("An error occurred reading the XML", e);
                                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred reading the XML: " + e.getMessage());
                                throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
                            } catch (SAXException e) {
                                logger.error("An error occurred with the XML parser. Note that this doesn't mean that there is an issue with the XML itself.", e);
                                getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "An error occurred with the XML parser. Note that this doesn't mean that there is an issue with the XML itself: " + e.getMessage());
                                throw new ClientException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
                            } catch (Exception e) {
                                logger.error("An unknown error occurred", e);
                                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An unknown error occurred: " + e.getMessage());
                                throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
                            }
                        }
                    }
                } catch (FileUploadException e) {
                    logger.error("Error during file upload", e);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e, "Error during file upload");
                    throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
                }
            } else if (RequestUtil.hasContent(entity) && (RequestUtil.compareMediaType(entity, MediaType.TEXT_XML, MediaType.APPLICATION_XML) || req_format.equals("xml") || isQueryVariableTrue("inbody"))) {
                //handle straight xml data
                try {
                    Reader sax = entity.getReader();

                    SAXReader reader = new SAXReader(user);
                    if (item != null) {
                        reader.setTemplate(item);
                    }

                    item = reader.parse(sax);

                    if (!reader.assertValid()) {
                        throw reader.getErrors().get(0);
                    }
                    if (XFT.VERBOSE) {
                        System.out.println("Loaded XML Item:" + item.getProperName());
                    }
                    if (item != null) {
                        completeDocument = true;
                    }

                } catch (SAXParseException e) {
                    logger.error("An error occurred parsing the XML", e);
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "An error occurred parsing the XML: " + e.getMessage());
                    throw new ClientException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
                } catch (IOException e) {
                    logger.error("An error occurred reading the XML", e);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An error occurred reading the XML: " + e.getMessage());
                    throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
                } catch (SAXException e) {
                    logger.error("An error occurred with the XML parser. Note that this doesn't mean that there is an issue with the XML itself.", e);
                    getResponse().setStatus(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, "An error occurred with the XML parser. Note that this doesn't mean that there is an issue with the XML itself: " + e.getMessage());
                    throw new ClientException(Status.CLIENT_ERROR_UNPROCESSABLE_ENTITY, e);
                } catch (Exception e) {
                    logger.error("An unknown error occurred", e);
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "An unknown error occurred: " + e.getMessage());
                    throw new ServerException(Status.SERVER_ERROR_INTERNAL, e);
                }
            } else if (req_format.equals("form") || RequestUtil.isMultiPartFormData(entity)) {
                try {
                    Map<String, String> params = getBodyVariableMap();

                    params.putAll(getQueryVariableMap());

                    if (params.containsKey("ELEMENT_0")) {
                        dataType = params.get("ELEMENT_0");
                    }
                    if (params.containsKey("xsiType")) {
                        dataType = params.get("xsiType");
                    }

                    if (dataType == null) {
                        for (String key : params.keySet()) {
                            if (key.contains(":") && key.contains("/")) {
                                dataType = key.substring(0, key.indexOf("/"));
                                break;
                            }
                        }
                    }

                    if (dataType != null) {
                        PopulateItem populator = item != null ? PopulateItem.Populate(params, user, dataType, true, item) : PopulateItem.Populate(params, user, dataType, true);
                        item = populator.getItem();
                    }
                } catch (XFTInitException e) {
                    throw new ServerException(e);
                } catch (ElementNotFoundException e) {
                    throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e);
                } catch (FieldNotFoundException ignored) {
                    logger.debug("Didn't find fields for populating item from form data.");
                }
            }
        } else if (req_format.equals("form") || RequestUtil.isMultiPartFormData(entity)) {
            try {
                Map<String, String> params = getBodyVariableMap();

                params.putAll(getQueryVariableMap());

                if (params.containsKey("ELEMENT_0")) {
                    dataType = params.get("ELEMENT_0");
                }
                if (params.containsKey("xsiType")) {
                    dataType = params.get("xsiType");
                }

                if (dataType == null) {
                    for (String key : params.keySet()) {
                        if (key.contains(":") && key.contains("/")) {
                            dataType = key.substring(0, key.indexOf("/"));
                            break;
                        }
                    }
                }

                if (dataType != null) {
                    PopulateItem populator = item != null ? PopulateItem.Populate(params, user, dataType, true, item) : PopulateItem.Populate(params, user, dataType, true);
                    item = populator.getItem();
                }
            } catch (XFTInitException e) {
                throw new ServerException(e);
            } catch (ElementNotFoundException e) {
                throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e);
            } catch (FieldNotFoundException ignored) {
                logger.debug("Didn't find fields for populating item from form data.");
            }
        }

        try {
            Map<String, String> params = getQueryVariableMap();
            if (params.containsKey("ELEMENT_0")) {
                dataType = params.get("ELEMENT_0");
            }
            if (params.containsKey("xsiType")) {
                dataType = params.get("xsiType");
            }

            if (dataType == null) {
                for (String key : params.keySet()) {
                    if (key.contains(":") && key.contains("/")) {
                        dataType = key.substring(0, key.indexOf("/"));
                        break;
                    }
                }
            }

            if (fieldMapping.size() > 0) {
                for (String key : fieldMapping.keySet()) {
                    if (params.containsKey(key)) {
                        params.put(fieldMapping.get(key), params.get(key));
                    }
                }
            }

            PopulateItem populator = null;
            if (item != null) {
                populator = PopulateItem.Populate(params, user, dataType, true, item);
            } else if (dataType != null) {
                populator = PopulateItem.Populate(params, user, dataType, true);
            }

            if (populator != null) {
                item = populator.getItem();
            }
        } catch (XFTInitException e) {
            throw new ServerException(e);
        } catch (ElementNotFoundException e) {
            throw new ClientException(Status.CLIENT_ERROR_BAD_REQUEST, e);
        } catch (FieldNotFoundException ignored) {
            logger.debug("Didn't find fields for populating item from form data.");
        }
        return item;
    }

    public void returnSuccessfulCreateFromList(String newURI) {
        Reference ticket_ref = getRequest().getResourceRef().addSegment(newURI);
        getResponse().setLocationRef(ticket_ref);

        String targetRef = ticket_ref.getTargetRef().toString();
        if (targetRef.contains("?")) {
            targetRef = targetRef.substring(0, targetRef.indexOf("?"));
        }

        returnRepresentation(new StringRepresentation(targetRef));
    }

    public void returnString(String message, Status status) {
        returnRepresentation(new StringRepresentation(message), status);
    }

    public void returnString(String message, MediaType mt, Status st) {
        returnRepresentation(new StringRepresentation(message, mt), st);
    }

    public void returnRepresentation(Representation message, Status st) {
        getResponse().setEntity(message);
        getResponse().setStatus(st);
    }

    public void returnXML(XFTItem item) {
        returnRepresentation(representItem(item, MediaType.TEXT_XML));
    }

    protected void setResponseHeader(String key, String value) {
        Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");

        if (responseHeaders == null) {
            responseHeaders = new Form();
            getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
        }

        responseHeaders.add(key, value);
    }

    public boolean isQueryVariableTrue(String key) {
        return isQueryVariableTrueHelper(getQueryVariable(key));
    }

    protected static boolean isQueryVariableTrue(String key, Request request) {
        return isQueryVariableTrueHelper(getQueryVariable(key, request));
    }

    protected static boolean isQueryVariableTrueHelper(Object queryVariableObj) {
        if (queryVariableObj != null && queryVariableObj instanceof String) {
            String queryVariable = (String) queryVariableObj;
            return !(queryVariable.equalsIgnoreCase("false") || queryVariable.equalsIgnoreCase("0"));
        } else {
            return false;
        }
    }

    public boolean isQueryVariableFalse(String key) {
        return isQueryVariableFalseHelper(getQueryVariable(key));
    }

    private static boolean isQueryVariableFalseHelper(String queryVariable) {
        return queryVariable != null && (queryVariable.equalsIgnoreCase("false") || queryVariable.equalsIgnoreCase("0"));
    }

    protected boolean isFalse(Object value) {
        return value != null && Boolean.parseBoolean((String) value);
    }

    @Nonnull
    public UserI getUser() {
        try {
            return user == null ? Users.getGuest() : user;
        } catch (UserNotFoundException | UserInitException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.UserServiceError, "An error occurred retrieving the guest user.", e);
        }
    }

    public String getLabelForFieldMapping(String xPath) {
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(xPath)) {
                return entry.getKey();
            }
        }
        return null;
    }

    protected HttpServletRequest getHttpServletRequest() {
        return new RequestUtil().getHttpServletRequest(getRequest());
    }

    /**
     * Sets the Content-Disposition response header. The filename parameter indicates the name of the content.
     * This method specifies the content as an attachment. If you need to specify inline content (e.g. for MIME
     * content in email or embedded content situations), use {@link #setContentDisposition(String, boolean)}.
     * <p/>
     * <b>Note:</b> This differs from the {@link TurbineUtils#setContentDisposition(HttpServletResponse, String)}
     * version of this method in that it performs the header set in a "restlet-y" way. Both methods use the same
     * {@link TurbineUtils#createContentDispositionValue(String, boolean)} method to create the actual value set
     * for the response header.
     *
     * @param filename The suggested filename for downloaded content.
     */
    public void setContentDisposition(String filename) {
        setContentDisposition(filename, true);
    }

    /**
     * Sets the Content-Disposition response header. The filename parameter indicates the name of the content.
     * This method specifies the content as an attachment when the <b>isAttachment</b> parameter is set to true,
     * and as inline content when the <b>isAttachment</b> parameter is set to false. You can specify the content
     * as an attachment by default by calling {@link #setContentDisposition(String)}.
     * <p/>
     * <b>Note:</b> This differs from the {@link TurbineUtils#setContentDisposition(HttpServletResponse, String, boolean)}
     * version of this method in that it performs the header set in a "restlet-y" way. Both methods use the same
     * {@link TurbineUtils#createContentDispositionValue(String, boolean)} method to create the actual value set
     * for the response header.
     *
     * @param filename     The suggested filename for downloaded content.
     * @param isAttachment Indicates whether the content is an attachment or inline.
     */
    @SuppressWarnings("unchecked")
    public void setContentDisposition(String filename, boolean isAttachment) {
        final Map<String, Object> attributes = getResponse().getAttributes();
        if (attributes.containsKey(CONTENT_DISPOSITION)) {
            throw new IllegalStateException("A content disposition header has already been added to this response.");
        }
        Object oHeaders = attributes.get(HttpConstants.ATTRIBUTE_HEADERS);
        Series<Parameter> headers;
        if (oHeaders != null) {
            headers = (Series<Parameter>) oHeaders;
        } else {
            headers = new Form();
        }
        headers.add(new Parameter(CONTENT_DISPOSITION, TurbineUtils.createContentDispositionValue(filename, isAttachment)));
        attributes.put(HttpConstants.ATTRIBUTE_HEADERS, headers);
    }

    /**
     * Return the list of query string parameters value with the name 'action'.  List is created on first access, and cached for later access.
     *
     * @return Should never be null.
     */
    public List<String> getActions() {
        if (actions == null) {
            final String[] actionA = getQueryVariables(ACTION);
            if (actionA != null && actionA.length > 0) {
                actions = Arrays.asList(actionA);
            }

            if (actions == null) actions = new ArrayList<>();
        }
        return actions;
    }

    public boolean containsAction(final String name) {
        return getActions().contains(name);
    }

    public List<FileWriterWrapperI> getFileWriters() throws FileUploadException, ClientException {
        return getFileWritersAndLoadParams(getRequest().getEntity(), false);
    }

    public void handleParam(final String key, final Object value) throws ClientException {

    }

    public void loadQueryVariables() throws ClientException {
        loadParams(getQueryVariableForm());
    }

    public void loadBodyVariables() throws ClientException {
        loadParams(getBodyAsForm());
    }

    public void loadParams(Form f) throws ClientException {
        if (f != null) {
            for (final String key : f.getNames()) {
                for (String v : f.getValuesArray(key)) {
                    handleParam(key, TurbineUtils.escapeParam(v));
                }
            }
        }
    }

    public void loadParams(final String _json) throws ClientException {
        try {
            final JSONObject json = new JSONObject(_json);
            String[] keys = JSONObject.getNames(json);
            if (keys != null) {
                for (final String key : keys) {
                    handleParam(key, TurbineUtils.escapeParam(json.get(key)));
                }
            }
        } catch (JSONException e) {
            logger.error("invalid JSON message " + _json, e);
        } catch (NullPointerException e) {
            logger.error("", e);
        }
    }

    /**
     * Gets file writers and load parameters from the request entity. When <b>useFileFieldName</b> is <b>true</b>, this uses the
     * field name in the form as the name in the {@link FileWriterWrapperI} object. Otherwise, it uses the filename as the name
     * of the {@link FileWriterWrapperI} parameter. When form fields are encountered, the {@link #handleParam(String, Object)}
     * method is called to cache all of the standard form fields.
     *
     * @param entity           The request entity.
     * @param useFileFieldName Indicates whether the form field name should be used to identify the extracted files.
     * @return A list of any {@link FileWriterWrapperI} objects found in the request.
     * @throws FileUploadException When an error occurs uploading the file.
     * @throws ClientException When an invalid request or data are submitted.
     */
    public List<FileWriterWrapperI> getFileWritersAndLoadParams(final Representation entity, boolean useFileFieldName) throws FileUploadException, ClientException {
        final List<FileWriterWrapperI> wrappers = new ArrayList<>();
        if (isQueryVariableTrue("inbody") || RequestUtil.isFileInBody(entity)) {
            if (entity != null && entity.getMediaType() != null && entity.getMediaType().getName().equals(MediaType.MULTIPART_FORM_DATA.getName())) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_ACCEPTABLE, "In-body File posts must include the file directly as the body of the message (not as part of multi-part form data).");
                return null;
            } else {
                // NOTE: modified driveFileName here to return a name when content-type is null
                final String fileName = (filepath == null || filepath.equals("")) ? RequestUtil.deriveFileName("upload", entity, false) : filepath;

                if (fileName == null) {
                    throw new FileUploadException("In-body File posts must include the file directly as the body of the message.");
                }

                if (entity == null || entity.getSize() == -1 || entity.getSize() == 0) {
                    throw new FileUploadException("In-body File posts must include the file directly as the body of the message.");
                }

                wrappers.add(new FileWriterWrapper(entity, fileName));
            }
        } else if (RequestUtil.isMultiPartFormData(entity)) {
            final DefaultFileItemFactory factory = new DefaultFileItemFactory();
            final RestletFileUpload upload = new RestletFileUpload(factory);

            List<FileItem> items = upload.parseRequest(getRequest());

            for (final FileItem item : items) {
                if (item.isFormField()) {
                    // Load form field to passed parameters map
                    String fieldName = item.getFieldName();
                    String value = item.getString();
                    if (fieldName.equals("reference")) {
                        throw new FileUploadException("multi-part form posts may not be used to upload files via reference.");
                    } else {
                        handleParam(fieldName, TurbineUtils.escapeParam(value));
                    }
                    continue;
                }
                if (item.getName() == null) {
                    throw new FileUploadException("multi-part form posts must contain the file name of the uploaded file.");
                }

                String fileName = item.getName();
                if (fileName.indexOf('\\') > -1) {
                    fileName = fileName.substring(fileName.lastIndexOf('\\') + 1);
                }

                wrappers.add(new FileWriterWrapper(item, useFileFieldName ? item.getFieldName() : fileName));
            }
        } else {
            String name = entity.getDownloadName();
            logger.debug(name);
        }

        return wrappers;
    }

    public HttpSession getHttpSession() {
        return getHttpServletRequest().getSession();
    }

    public static String CONTEXT_PATH = null;

    public String getContextPath() {
        if (CONTEXT_PATH == null) {
            CONTEXT_PATH = TurbineUtils.GetRelativePath(getHttpServletRequest());
        }
        return CONTEXT_PATH;
    }

    public String wrapPartialDataURI(String uri) {
        return "/data" + uri;
    }

    public void setResponseStatus(final ActionException e) {
        getResponse().setStatus(e.getStatus(), e, e.getMessage());
    }

    public Integer identifyCompression(Integer defaultCompression) throws ActionException {
        try {
            if (containsQueryVariable(COMPRESSION)) {
                return Integer.valueOf(getQueryVariable(COMPRESSION));
            }
        } catch (NumberFormatException e) {
            throw new ClientException(e.getMessage());
        }

        if (defaultCompression != null) {
            return defaultCompression;
        } else {
            return ZipUtils.DEFAULT_COMPRESSION;
        }
    }

    @SuppressWarnings("unused")
    public Representation buildChangesets(XFTItem item, String key, MediaType mt) throws Exception {
        String files = getQueryVariable("includeFiles");
        String details = getQueryVariable("includeDetails");
        final boolean includeFiles = (StringUtils.isEmpty(files)) ? false : Boolean.valueOf(files);
        final boolean includeDetails = (StringUtils.isEmpty(details)) ? false : Boolean.valueOf(details);

        return new JSONObjectRepresentation(MediaType.APPLICATION_JSON, (new WorkflowBasedHistoryBuilder(item, key, user, includeFiles, includeDetails)).toJSON(getQueryVariable("dateFormat")));
    }

    public Integer getEventId() {
        final String id = getQueryVariable(EventUtils.EVENT_ID);
        if (id != null) {
            return Integer.valueOf(id);
        } else {
            return null;
        }
    }

    public EventUtils.TYPE getEventType() {
        final String id = getQueryVariable(EventUtils.EVENT_TYPE);
        if (id != null) {
            return EventUtils.getType(id, EventUtils.TYPE.WEB_SERVICE);
        } else {
            return EventUtils.TYPE.WEB_SERVICE;
        }
    }

    public String getAction() {
        return getQueryVariable(EventUtils.EVENT_ACTION);
    }

    public String getComment() {
        return getQueryVariable(EventUtils.EVENT_COMMENT);
    }

    public EventDetails newEventInstance(EventUtils.CATEGORY cat) {
        return EventUtils.newEventInstance(cat, getEventType(), getAction(), getReason(), getComment());
    }

    public EventDetails newEventInstance(EventUtils.CATEGORY cat, String action) {
        return EventUtils.newEventInstance(cat, getEventType(), (getAction() != null) ? getAction() : action, getReason(), getComment());
    }

    public void delete(ArchivableItem item, EventDetails event) throws Exception {
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, item.getXSIType(), item.getId(), item.getProject(), event);
        final EventMetaI ci = workflow.buildEvent();

        try {
            if (isQueryVariableTrue("removeFiles")) {
                final List<XFTItem> hash = item.getItem().getChildrenOfType("xnat:abstractResource");

                for (XFTItem resource : hash) {
                    ItemI om = BaseElement.GetGeneratedItem(resource);
                    if (om instanceof XnatAbstractresource) {
                        XnatAbstractresource resourceA = (XnatAbstractresource) om;
                        resourceA.deleteWithBackup(item.getArchiveRootPath(), user, ci);
                    }
                }
            }
            DBAction.DeleteItem(item.getItem().getCurrentDBVersion(), user, ci, false);

            WorkflowUtils.complete(workflow, ci);
        } catch (Exception e) {
            WorkflowUtils.fail(workflow, ci);
        }

        Users.clearCache(user);
        MaterializedView.deleteByUser(user);
    }

    public void delete(ArchivableItem parent, ItemI item, EventDetails event) throws Exception {
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, parent.getXSIType(), parent.getId(), parent.getProject(), event);
        final EventMetaI ci = workflow.buildEvent();

        try {
            if (isQueryVariableTrue("removeFiles")) {
                final List<XFTItem> hash = item.getItem().getChildrenOfType("xnat:abstractResource");

                for (XFTItem resource : hash) {
                    ItemI om = BaseElement.GetGeneratedItem(resource);
                    if (om instanceof XnatAbstractresource) {
                        XnatAbstractresource resourceA = (XnatAbstractresource) om;
                        resourceA.deleteWithBackup(parent.getArchiveRootPath(), user, ci);
                    }
                }
            }
            DBAction.DeleteItem(item.getItem().getCurrentDBVersion(), user, ci, false);

            WorkflowUtils.complete(workflow, ci);
        } catch (Exception e) {
            WorkflowUtils.fail(workflow, ci);
            throw e;
        }

        Users.clearCache(user);
        MaterializedView.deleteByUser(user);
    }

    @SuppressWarnings("unused")
    public void create(ArchivableItem parent, ItemI sub, boolean overwriteSecurity, boolean allowDataDeletion, EventDetails event) throws Exception {
        PersistentWorkflowI wrk = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, parent.getItem(), event);
        EventMetaI c = wrk.buildEvent();

        try {
            if (SaveItemHelper.authorizedSave(sub, user, false, false, c)) {
                WorkflowUtils.complete(wrk, c);
                MaterializedView.deleteByUser(user);
            }
        } catch (Exception e) {
            WorkflowUtils.fail(wrk, c);
            throw e;
        }
    }

    public void create(ArchivableItem sub, boolean overwriteSecurity, boolean allowDataDeletion, EventDetails event) throws Exception {
        PersistentWorkflowI wrk = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, sub.getItem(), event);
        EventMetaI c = wrk.buildEvent();

        try {
            if (SaveItemHelper.authorizedSave(sub, user, overwriteSecurity, allowDataDeletion, c)) {
                WorkflowUtils.complete(wrk, c);
                MaterializedView.deleteByUser(user);
            }
        } catch (Exception e) {
            WorkflowUtils.fail(wrk, c);
            throw e;
        }
    }

    protected static Representation returnStatus(ItemI i, MediaType mt) {
        try {
            if (i.needsActivation()) {
                return new StringRepresentation(ViewManager.QUARANTINE, mt);
            } else {
                return new StringRepresentation(ViewManager.ACTIVE, mt);
            }
        } catch (Exception e) {
            return new StringRepresentation(ViewManager.ACTIVE, mt);
        }
    }

    protected void postSaveManageStatus(ItemI i) throws ActionException {
        try {
            if (isQueryVariableTrue("activate")) {
                if (Permissions.canActivate(user, i.getItem())) {
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, i.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, "Activated"));
                    try {
                        i.activate(user);
                        WorkflowUtils.complete(wrk, wrk.buildEvent());
                    } catch (Exception e) {
                        logger.error("", e);
                        WorkflowUtils.fail(wrk, wrk.buildEvent());
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient activation privileges for experiments in this project.");
                }
            }

            if (isQueryVariableTrue(ViewManager.QUARANTINE)) {
                if (Permissions.canActivate(user, i.getItem())) {
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, i.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, "Quarantined"));
                    try {
                        i.quarantine(user);
                        WorkflowUtils.complete(wrk, wrk.buildEvent());
                    } catch (Exception e) {
                        logger.error("", e);
                        WorkflowUtils.fail(wrk, wrk.buildEvent());
                    }
                } else {
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient activation privileges for experiments in this project.");
                }
            }

            if (isQueryVariableTrue("_lock")) {
                if (Permissions.canActivate(user, i.getItem())) {
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, i.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, "Locked"));
                    try {
                        i.lock(user);
                        WorkflowUtils.complete(wrk, wrk.buildEvent());
                    } catch (Exception e) {
                        logger.error("", e);
                        WorkflowUtils.fail(wrk, wrk.buildEvent());
                    }
                } else {
                    throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient activation privileges for experiments in this project.", new Exception());
                }
            } else if (isQueryVariableTrue("_unlock")) {
                if (Permissions.canActivate(user, i.getItem())) {
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, i.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, "Unlocked"));
                    try {
                        i.activate(user);
                        WorkflowUtils.complete(wrk, wrk.buildEvent());
                    } catch (Exception e) {
                        logger.error("", e);
                        WorkflowUtils.fail(wrk, wrk.buildEvent());
                    }
                } else {
                    throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient activation privileges for experiments in this project.", new Exception());
                }
            } else if (isQueryVariableTrue("_obsolete")) {
                if (Permissions.canActivate(user, i.getItem())) {
                    PersistentWorkflowI wrk = PersistentWorkflowUtils.getOrCreateWorkflowData(getEventId(), user, i.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, "Obsoleted"));
                    try {
                        i.getItem().setStatus(user, ViewManager.OBSOLETE);
                        WorkflowUtils.complete(wrk, wrk.buildEvent());
                    } catch (Exception e) {
                        logger.error("", e);
                        WorkflowUtils.fail(wrk, wrk.buildEvent());
                    }
                } else {
                    throw new ClientException(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient activation privileges for experiments in this project.", new Exception());
                }
            }
        } catch (ActionException e) {
            throw e;
        } catch (Exception e) {
            logger.error("", e);
            throw new org.nrg.action.ServerException("Error modifying status", e);
        }
    }

    protected void respondToException(Exception exception, Status status) {
        logger.error("Transaction got a status: " + status, exception);
        getResponse().setStatus(status, exception.getMessage());
    }

    /**
     * This method walks the <b>org.nrg.xnat.restlet.extensions.item.extensions</b> package and attempts to find extensions for the
     * set of available REST table representations.
     */
    static Map<String, Class<?>> itemRepresentations = null;

    @SuppressWarnings("unused")
    private synchronized Map<String, Class<?>> getExtensionItemRepresentations() {
        if (itemRepresentations == null) {
            itemRepresentations = Maps.newHashMap();

            List<Class<?>> classes;
            try {
                classes = Reflection.getClassesForPackage("org.nrg.xnat.restlet.representations.item.extensions");
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(XnatItemRepresentation.class)) {
                    XnatItemRepresentation annotation = clazz.getAnnotation(XnatItemRepresentation.class);
                    boolean required = annotation.required();
                    if (!OutputRepresentation.class.isAssignableFrom(clazz)) {
                        String message = "You can only apply the XnatItemRepresentation annotation to classes that subclass the org.restlet.resource.Resource class: " + clazz.getName();
                        if (required) {
                            throw new NrgServiceRuntimeException(message);
                        } else {
                            logger.error(message);
                        }
                    } else {
                        if (MediaType.valueOf(annotation.mediaType()) != null) {
                            MediaType.register(annotation.mediaType(), annotation.mediaTypeDescription());
                        }
                        itemRepresentations.put(annotation.mediaType(), clazz);
                    }
                }
            }
        }

        return itemRepresentations;
    }


    /**
     * This method walks the <b>org.nrg.xnat.restlet.extensions.table.extensions</b> package and attempts to find extensions for the
     * set of available REST table representations.
     */

    static Map<String, Class<?>> tableRepresentations = null;

    private synchronized Map<String, Class<?>> getExtensionTableRepresentations() {
        if (tableRepresentations == null) {
            tableRepresentations = Maps.newHashMap();

            List<Class<?>> classes;
            try {
                classes = Reflection.getClassesForPackage("org.nrg.xnat.restlet.representations.table.extensions");
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(XnatTableRepresentation.class)) {
                    XnatTableRepresentation annotation = clazz.getAnnotation(XnatTableRepresentation.class);
                    boolean required = annotation.required();
                    if (!OutputRepresentation.class.isAssignableFrom(clazz)) {
                        String message = "You can only apply the XnatTableRepresentation annotation to classes that subclass the org.restlet.resource.Resource class: " + clazz.getName();
                        if (required) {
                            throw new NrgServiceRuntimeException(message);
                        } else {
                            logger.error(message);
                        }
                    } else {
                        if (MediaType.valueOf(annotation.mediaType()) != null) {
                            MediaType.register(annotation.mediaType(), annotation.mediaTypeDescription());
                        }
                        tableRepresentations.put(annotation.mediaType(), clazz);
                    }
                }
            }
        }

        return tableRepresentations;
    }

    protected boolean isWhitelisted() {
        return checkWhitelist(null);
    }

    protected boolean isWhitelisted(final String projectId) {
        return checkWhitelist(projectId);
    }

    private boolean checkWhitelist(final String projectId) {
        final ConfigService configService = XDAT.getConfigService();
        final String config = StringUtils.isNotBlank(projectId)
                ? configService.getConfigContents("user-resource-whitelist", "whitelist.json", Scope.Project, projectId)
                : configService.getConfigContents("user-resource-whitelist", "users/whitelist.json");

        if (StringUtils.isBlank(config)) {
            return false;
        }

        try {
            List<String> userResourceWhitelist = getSerializer().deserializeJson(config, SerializerService.TYPE_REF_LIST_STRING);
            if (userResourceWhitelist != null) {
                return userResourceWhitelist.contains(user.getUsername());
            }
        } catch (IOException e) {
            String message = "Error retrieving user list" + (projectId == null ? "" : " for project " + projectId);
            logger.error(message, e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, message + ": " + e.getMessage());
        }
        return false;
    }

    private static final Map<String, List<FilteredResourceHandlerI>> handlers = Maps.newConcurrentMap();
    private static final Object MUTEX_HANDLERS = new Object();

    /**
     * Get a list of the possible handlers.  This allows additional handlers to be injected at a later date or via a module.
     *
     * @return A list of possible handlers for the indicated package.
     * @throws InstantiationException When an error occurs creating one of the handler objects.
     * @throws IllegalAccessException When access levels are incorrect during access or creation.
     */
    public static List<FilteredResourceHandlerI> getHandlers(String _package, List<FilteredResourceHandlerI> defaultHandlers) throws InstantiationException, IllegalAccessException {
        if (!handlers.containsKey(_package)) {
            synchronized (MUTEX_HANDLERS) {
                final List<FilteredResourceHandlerI> handlerClasses = new ArrayList<>(defaultHandlers);
                //ordering here is important.  the last match wins
                try {
                    final List<Class<?>> classes = Reflection.getClassesForPackage(_package);
                    for (final Class<?> clazz : classes) {
                        if (FilteredResourceHandlerI.class.isAssignableFrom(clazz)) {
                            handlerClasses.add((FilteredResourceHandlerI) clazz.newInstance());
                        }
                    }
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
                handlers.put(_package, handlerClasses);
            }
        }

        return handlers.get(_package);
    }

    /**
     * Checks whether the resource URL is "clean", that is, has no remaining part (any string past the part of the URL
     * that matched the restlet URL) that is not a query string or URL fragment. For example, for the project resource
     * restlet, you can go to /data/projects/projectId/foo/bar. The "/projects/projectId" part of the URL will match the
     * mapped URL for that restlet and the remaining part is then "/foo/bar". If there is any remaining part, it's then
     * checked to see if that part is actually the beginning of query-string parameters by checking whether it starts
     * with the '?' character or if it's part of a URI fragment by checking whether it starts with the '#' character. If
     * the remaining part contains neither query-string parameters or a URI fragment, this method then sets the request
     * status code to 400 (bad request) and throws an exception.
     *
     * @param request  The restlet request.
     * @param response The restlet response.
     * @return Returns <b>true</b> if the URL is clean, <b>false</b> otherwise.
     */
    @SuppressWarnings("unused")
    protected boolean validateCleanUrl(final Request request, final Response response) {
        final String remaining = request.getResourceRef().getRemainingPart();
        if (StringUtils.isNotBlank(remaining) && !(remaining.startsWith("?") || remaining.startsWith("#"))) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No extra path information is permitted for this operation.");
            return false;
        }
        return true;
    }

    public interface FilteredResourceHandlerI {
        boolean canHandle(SecureResource resource);

        Representation handle(SecureResource resource, Variant variant) throws Exception;
    }
}
