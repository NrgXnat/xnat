/*
 * web: org.nrg.xnat.restlet.resources.SecureResource
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.resources;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.noelios.restlet.http.HttpConstants;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.TurbineException;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.action.ActionException;
import org.nrg.action.ClientException;
import org.nrg.action.ServerException;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.config.services.ConfigService;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.SerializerService;
import org.nrg.framework.utilities.Reflection;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatProjectdataI;
import org.nrg.xdat.om.*;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xdat.om.base.BaseXnatImagescandata;
import org.nrg.xdat.security.helpers.Features;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.security.helpers.Users;
import org.nrg.xdat.security.user.exceptions.UserInitException;
import org.nrg.xdat.security.user.exceptions.UserNotFoundException;
import org.nrg.xdat.services.cache.UserDataCache;
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
import org.nrg.xft.event.XftItemEvent;
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
import org.nrg.xft.utils.ValidationUtils.ValidationResults;
import org.nrg.xft.utils.XftStringUtils;
import org.nrg.xnat.archive.Rename;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.FileWriterWrapper;
import org.nrg.xnat.helpers.transactions.HTTPSessionStatusManagerQueue;
import org.nrg.xnat.helpers.transactions.PersistentStatusQueueManagerI;
import org.nrg.xnat.itemBuilders.WorkflowBasedHistoryBuilder;
import org.nrg.xnat.restlet.XnatItemRepresentation;
import org.nrg.xnat.restlet.XnatTableRepresentation;
import org.nrg.xnat.restlet.representations.*;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.restlet.util.RequestUtil;
import org.nrg.xnat.restlet.util.SecureResourceParameterMapper;
import org.nrg.xnat.status.StatusList;
import org.nrg.xnat.turbine.utils.ArchivableItem;
import org.nrg.xnat.turbine.utils.XNATUtils;
import org.nrg.xnat.utils.InteractiveAgentDetector;
import org.nrg.xnat.utils.WorkflowUtils;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.*;
import org.restlet.util.Series;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

import static org.nrg.xdat.preferences.SiteConfigPreferences.SITE_URL;
import static org.nrg.xft.event.XftItemEventI.DELETE;

@SuppressWarnings("deprecation")
public abstract class SecureResource extends Resource {

    private static final String COMPRESSION = "compression";

    private static final String CONTENT_DISPOSITION = "Content-Disposition";

    private static final String ACTION = "action";

    public static final String HANDLER = "handler";

    public static final Logger logger = Logger.getLogger(SecureResource.class);
    public static final long DEFAULT_PAGE_SIZE = 100;
    public static final long DEFAULT_PAGE_NUM = 0;

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

    public static final String HTTP_SESSION_LISTENER = "http-session-listener";

    protected    List<String> actions = null;
    public final String       userName;
    public       String       requested_format;
    public       String       filepath;

    public SecureResource(Context context, Request request, Response response) {
        super(context, request, response);

        _serializer = XDAT.getSerializerService();
        if (null == _serializer) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "Serializer service was not properly initialized.");
            throw new NrgServiceRuntimeException("ERROR: Serializer service was not properly initialized.");
        }
        _template = XDAT.getNamedParameterJdbcTemplate();
        if (_template == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "Named parameter JDBC template was not properly initialized.");
            throw new NrgServiceRuntimeException("ERROR: Named parameter JDBC template was not properly initialized.");
        }
        _userDataCache = XDAT.getContextService().getBean(UserDataCache.class);
        if (_userDataCache == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_FAILED_DEPENDENCY, "User data cache was not properly initialized.");
            throw new NrgServiceRuntimeException("ERROR: User data cache was not properly initialized.");
        }

        requested_format = getQueryVariable("format");

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

        try {
            // expects that the user exists in the session (either via traditional
            // session or set via the XnatSecureGuard
            _user = ObjectUtils.defaultIfNull(XDAT.getUserDetails(), Users.getGuest());
            userName = _user.getUsername();
            logAccess();
        } catch (UserNotFoundException | UserInitException e) {
            throw new RuntimeException("An error occurred where it really should not have occurred", e);
        }
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
        final String url = getRequest().getResourceRef().toString();
        AccessLogger.LogResourceAccess(getUser().getUsername(), getRequest(), url, "");
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

    public Map<String, String> getDecodedQueryVariableMap() {
        return convertFormToMapAndDecode(getQueryVariableForm());
    }

    protected SerializerService getSerializer() {
        return _serializer;
    }

    protected NamedParameterJdbcTemplate getTemplate() {
        return _template;
    }

    protected UserDataCache getUserDataCache() {
        return _userDataCache;
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
            final Representation entity = getRequest().getEntity();
            if (RequestUtil.isMultiPartFormData(entity) && entity.getSize() > 0) {
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

    private static Map<String, String> convertFormToMapAndDecode(Form q) {
        Map<String, String> map = Maps.newLinkedHashMap();
        if (q != null) {
            for (String s : q.getValuesMap().keySet()) {
                try {
                    String value = q.getFirstValue(s);
                    map.put(s, value == null ? null : URLDecoder.decode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
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
            return (caseSensitive && getQueryVariable(key).equals(value)) || (!caseSensitive && getQueryVariable(key).equalsIgnoreCase(value));
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

    public Representation representTable(XFTTable table, MediaType mt, Hashtable<String, Object> params, Map<String, Map<String, String>> columnProperties) {
        if (table != null) {
            if (getQueryVariable("sortBy") != null && !table.isSorted()) {
                final String sortBy = getQueryVariable("sortBy");
                table.sort(Arrays.asList(StringUtils.split(sortBy, ',')));
                if (isQueryVariable("sortOrder", "DESC", false) && !mt.equals(APPLICATION_XLIST)) {
                    table.reverse();
                }
            }

            //try to map to an inserted implementation
            final Class<?> clazz = getExtensionTableRepresentations().get(mt.toString());
            if (clazz != null) {
                try {
                    return clazz.asSubclass(OutputRepresentation.class).getConstructor(OBJECT_REPRESENTATION_CTOR_PARAM_TYPES).newInstance(table, columnProperties, params, mt);
                } catch (Exception e) {
                    logger.error("", e);
                }
            }

            if (mt.equals(MediaType.TEXT_XML)) {
                return new XMLTableRepresentation(table, columnProperties, params, MediaType.TEXT_XML);
            } else if (mt.equals(MediaType.APPLICATION_JSON)) {
                return new JSONTableRepresentation(table, columnProperties, params, MediaType.APPLICATION_JSON);
            } else if (mt.equals(MediaType.APPLICATION_EXCEL) || mt.equals(TEXT_CSV)) {
                return new CSVTableRepresentation(table, columnProperties, params, mt);
            } else if (mt.equals(APPLICATION_XLIST)) {
                Representation rep = new HTMLTableRepresentation(table, columnProperties, params, MediaType.TEXT_HTML, false);
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
                        return new StandardTurbineScreen(MediaType.TEXT_HTML, getRequest(), getUser(), getQueryVariable("requested_screen"), params);
                    } catch (TurbineException e) {
                        logger.error("", e);
                        return new HTMLTableRepresentation(table, columnProperties, params, MediaType.TEXT_HTML, true);
                    }
                } else {
                    return new HTMLTableRepresentation(table, columnProperties, params, MediaType.TEXT_HTML, true);
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
        setStatusBasedOnConditions();
    }

    protected void setStatusBasedOnConditions() {
        final Representation selectedRepresentation = getResponse().getEntity();
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

                for (final Class<?> handler : handlerClasses) {
                    if (ItemHandlerI.class.isAssignableFrom(handler)) {
                        final ItemHandlerI instance = (ItemHandlerI) handler.newInstance();
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
                return new ItemHTMLRepresentation(item, MediaType.TEXT_HTML, getRequest(), getUser(), getQueryVariable("requested_screen"), new Hashtable<>());
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return null;
            }
        } else if (mt.equals(MediaType.APPLICATION_JSON)) {
            try {
                FlattenedItemA.HistoryConfigI history = (isQueryVariableTrue("includeHistory")) ? FlattenedItemA.GET_ALL : () -> false;
                return new JSONObjectRepresentation(MediaType.APPLICATION_JSON, (new ItemJSONBuilder()).call(item, history, isQueryVariableTrue("includeHeaders")));
            } catch (Exception e) {
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
                return null;
            }
        } else {
            return new ItemXMLRepresentation(item, MediaType.TEXT_XML, true, !isQueryVariableTrue("concealHiddenFields"));
        }
    }

    public MediaType buildMediaType(final MediaType mediaType, final String filename) {
        final String extension = FilenameUtils.getExtension(filename);
        switch (extension) {
            case "bmp":
                return MediaType.IMAGE_BMP;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "html":
                return MediaType.TEXT_HTML;
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "pdf":
                return MediaType.APPLICATION_PDF;
            case "png":
                return MediaType.IMAGE_PNG;
            case "svg":
                return MediaType.IMAGE_SVG;
            case "tiff":
                return MediaType.IMAGE_TIFF;
            case "txt":
                return MediaType.TEXT_PLAIN;
            case "xml":
                return MediaType.TEXT_XML;
        }
        return mediaType != null && mediaType.equals(MediaType.TEXT_XML) ? MediaType.ALL : MediaType.APPLICATION_OCTET_STREAM;
    }

    public FileRepresentation representFile(final File file, final MediaType incoming) {
        final MediaType mediaType = buildMediaType(incoming, file.getName());

        if (forceDownload(file, mediaType)) {
            setContentDisposition(file.getName());
        }

        final FileRepresentation representation = new FileRepresentation(file, mediaType);
        representation.setModificationDate(new Date(file.lastModified()));
        return representation;
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

        final UserI user = getUser();
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
                    
                    // Add custom parameter mappings here
                    // Define new classes of type SecureResourceParameterMapper to have them picked up here
                    Collection<SecureResourceParameterMapper> mappers = XDAT.getContextService().getBeansOfType(SecureResourceParameterMapper.class).values();
                    for(SecureResourceParameterMapper m : mappers) {
                        params = m.mapParams(params, dataType); 
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
    
    public void returnSuccessfulCreateFromList(final String newURI) {
        final Reference ticketRef = getSiteUrlResolvedReference().addSegment(newURI);
        getResponse().setLocationRef(ticketRef);

        String targetRef = ticketRef.getTargetRef().toString();
        if (targetRef.contains("?")) {
            targetRef = targetRef.substring(0, targetRef.indexOf("?"));
        }

        returnRepresentation(new StringRepresentation(targetRef));
    }

    /**
     * Takes the request reference and modifies the protocol and server to use the configured site URL value. This works around the fact that,
     * when XNAT sits behind a front-end proxy such as nginx or Apache HTTPD that provides SSL, XNAT sees the URL as using http, since the
     * connection between the proxy and Tomcat is unencrypted. As a result, the URL returned by XNAT specifies http even though that may be
     * unreachable for any clients trying to use the resulting URL.
     *
     * Note that, if any errors occur retrieving or parsing the URL specified for the site URL preference, this method logs that error then
     * returns the original unmodified reference value.
     *
     * @return A reference that uses the site URL setting for the protocol and server values.
     */
    protected Reference getSiteUrlResolvedReference() {
        final Reference reference = getRequest().getResourceRef();
        try {
            // If the reference starts with the processing URL, we'll assume that that's what they really meant and
            // just return that without translating it to the siteUrl.
            final String processingUrl = XDAT.getSiteConfigurationProperty("processingUrl", null);
            if (StringUtils.startsWithIgnoreCase(reference.toString(), processingUrl)) {
                return reference;
            }
            final String siteUrlProperty = XDAT.getSiteConfigurationProperty(SITE_URL);
            try {
            	final String path = reference.getPath();
            	final String remainingPart = reference.getRemainingPart(false,false);
            	final String basePath = (remainingPart.length()> 0 && path.contains(remainingPart)) ? path.substring(0,path.lastIndexOf(remainingPart)) : path;
                final URL siteUrl = new URL(siteUrlProperty);
                reference.setProtocol(new Protocol(siteUrl.getProtocol()));
                reference.setAuthority(siteUrl.getAuthority());
                reference.setBaseRef(reference.getScheme() + "://" + reference.getAuthority() + basePath);
            } catch (MalformedURLException e) {
                logger.warn("An error occurred trying to convert the site URL value " + siteUrlProperty + " to a URL object: " + e.getMessage());
            }
        } catch (ConfigServiceException e) {
            logger.warn("An error occurred trying to retrieve the site URL from the configuration service", e);
        }
        return reference;
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

    @SuppressWarnings("SameParameterValue")
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

    protected static boolean isQueryVariableTrueHelper(final Object queryVariableObj) {
        if (queryVariableObj == null) {
            return false;
        }
        if (queryVariableObj instanceof String) {
            return !(StringUtils.equalsAnyIgnoreCase((String) queryVariableObj, "false", "0"));
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
            return ObjectUtils.defaultIfNull(_user, Users.getGuest());
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
     * This method tests whether the current user is a guest. If so, it then tests whether the request came from an
     * interactive agent such as a browser. If neither of these is true, the response status is set to 404 Not Found.
     * If so, the HTTP request is stored in the session request cache and a redirect to the login page is added to the
     * response. On successful authentication, the client is redirected to the page requested in the original request.
     */
    protected void setGuestDataResponse() {
        setGuestDataResponse(null);
    }

    /**
     * This method tests whether the current user is a guest. If so, it then tests whether the request came from an
     * interactive agent such as a browser. If neither of these is true, the response status is set to 404 Not Found.
     * If so, the HTTP request is stored in the session request cache and a redirect to the login page is added to the
     * response. On successful authentication, the client is redirected to the page requested in the original request.
     *
     * @param message The message to add to the response status. Can be null or empty.
     */
    protected void setGuestDataResponse(final String message) {
        final UserI user = getUser();
        final Response response = getResponse();
        if (user.isGuest()) {
            final HttpServletRequest httpServletRequest = getHttpServletRequest();
            if (XDAT.getContextService().getBean(InteractiveAgentDetector.class).isInteractiveAgent(httpServletRequest)) {
                final RequestCache cache = new HttpSessionRequestCache();
                cache.saveRequest(httpServletRequest, null);
                response.redirectTemporary("/app/template/Login.vm");
            }
        } else {
            if (StringUtils.isBlank(message)) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            } else {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND, message);
            }
        }
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

            if (actions == null) {
                actions = new ArrayList<>();
            }
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

    public void loadParams(final String json) throws ClientException {
        try {
            final JSONObject jsonObject = new JSONObject(json);
            final String[]   keys = JSONObject.getNames(jsonObject);
            if (keys != null) {
                for (final String key : keys) {
                    final Object jsonValue = jsonObject.get(key);
                    handleParam(key, TurbineUtils.escapeParam(jsonValue != null ? jsonValue.toString() : null));
                }
            }
        } catch (JSONException e) {
            logger.error("invalid JSON message: " + json, e);
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
                final String fileName = StringUtils.defaultIfBlank(filepath, RequestUtil.deriveFileName("upload", entity, false));

                if (StringUtils.isBlank(fileName)) {
                    throw new FileUploadException("In-body File posts must include the file directly as the body of the message. In this case, there is no filename specified.");
                }
                if (entity == null) {
                    throw new FileUploadException("In-body File posts must include the file directly as the body of the message. In this case, the request entity is null.");
                }
                if (entity.getSize() < 1 && !entity.getMediaType().equals(MediaType.APPLICATION_ZIP)) {
                    throw new FileUploadException("In-body File posts must include the file directly as the body of the message. In this case, the request entity size is " + entity.getSize() + " but the media type is not application/zip (i.e. streaming compressed upload).");
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
        return uri.startsWith("/xapi/") ? uri : StringUtils.prependIfMissing(uri, "/data");
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
            return XDAT.getSiteConfigPreferences().getZipCompressionMethod();
        }
    }

    @SuppressWarnings("unused")
    public Representation buildChangesets(XFTItem item, String key, MediaType mt) throws Exception {
        String files = getQueryVariable("includeFiles");
        String details = getQueryVariable("includeDetails");
        final boolean includeFiles = !StringUtils.isEmpty(files) && Boolean.parseBoolean(files);
        final boolean includeDetails = !StringUtils.isEmpty(details) && Boolean.parseBoolean(details);

        return new JSONObjectRepresentation(MediaType.APPLICATION_JSON, (new WorkflowBasedHistoryBuilder(item, key, getUser(), includeFiles, includeDetails)).toJSON(getQueryVariable("dateFormat")));
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
        final UserI               user     = getUser();
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, item.getXSIType(), item.getId(), item.getProject(), event);
        final EventMetaI          ci       = workflow.buildEvent();

        final XftItemEvent.Builder builder = XftItemEvent.builder();
        try {
            if (isQueryVariableTrue("removeFiles")) {
                final List<XFTItem> hash = item.getItem().getChildrenOfType("xnat:abstractResource");

                for (XFTItem resource : hash) {
                    ItemI om = BaseElement.GetGeneratedItem(resource);
                    if (om instanceof XnatAbstractresource) {
                        XnatAbstractresource resourceA = (XnatAbstractresource) om;
                        resourceA.deleteWithBackup(item.getArchiveRootPath(), item.getProject(), user, ci);
                        builder.item(resource);
                    }
                }
            }
            DBAction.DeleteItem(item.getItem().getCurrentDBVersion(), user, ci, false);
            if (BaseElement.class.isAssignableFrom(item.getClass())) {
                builder.element((BaseElement) item);
            } else {
                builder.xsiType(item.getXSIType()).id(item.getId());
            }
            XDAT.triggerEvent(builder.build());
            WorkflowUtils.complete(workflow, ci);
        } catch (Exception e) {
            WorkflowUtils.fail(workflow, ci);
        }

        Users.clearCache(user);
    }

    public void delete(ArchivableItem parent, ItemI item, EventDetails event) throws Exception {
        final UserI               user     = getUser();
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, parent.getXSIType(), parent.getId(), parent.getProject(), event);
        final EventMetaI          ci       = workflow.buildEvent();

        try {
            XNATUtils.delete(parent, item, ci, isQueryVariableTrue("removeFiles"));
            WorkflowUtils.complete(workflow, ci);
        } catch (Exception e) {
            WorkflowUtils.fail(workflow, ci);
            throw e;
        }

        Users.clearCache(user);
    }

    @SuppressWarnings("unused")
    public boolean create(final ArchivableItem parent, final ItemI sub, final boolean overwriteSecurity, final boolean allowDataDeletion, final EventDetails event) throws Exception {
        final UserI               user     = getUser();
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), user, parent.getItem(), event);
        final EventMetaI          meta     = workflow.buildEvent();

        try {
            if (SaveItemHelper.authorizedSave(sub, user, false, false, meta)) {
                WorkflowUtils.complete(workflow, meta);
                Users.clearCache(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            WorkflowUtils.fail(workflow, meta);
            throw e;
        }
    }

    public boolean create(final ArchivableItem item, boolean overwriteSecurity, boolean allowDataDeletion, EventDetails event) throws Exception {
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), getUser(), item.getItem(), event);
        final EventMetaI meta = workflow.buildEvent();
        return create(item, overwriteSecurity, allowDataDeletion, workflow, meta);
    }

    public boolean create(final ArchivableItem item, boolean overwriteSecurity, boolean allowDataDeletion, final PersistentWorkflowI workflow, final EventMetaI meta) throws Exception {
        return createOrUpdateImpl(true, item, overwriteSecurity, allowDataDeletion, workflow, meta);
    }

    public boolean update(final ArchivableItem item, boolean overwriteSecurity, boolean allowDataDeletion, final EventDetails event) throws Exception {
        final PersistentWorkflowI workflow = WorkflowUtils.getOrCreateWorkflowData(getEventId(), getUser(), item.getItem(), event);
        final EventMetaI meta = workflow.buildEvent();
        return update(item, overwriteSecurity, allowDataDeletion, workflow, meta);
    }

    public boolean update(final ArchivableItem item, boolean overwriteSecurity, boolean allowDataDeletion, final PersistentWorkflowI workflow, final EventMetaI meta) throws Exception {
        return createOrUpdateImpl(false, item, overwriteSecurity, allowDataDeletion, workflow, meta);
    }

    public boolean isSharingAllowed(final UserI user, final String projectId) {
        if (!Features.checkRestrictedFeature(user, projectId, Features.PROJECT_SHARING_FEATURE)) {
            this.getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Sharing is not allowed.");
            return false;
        }
        return true;
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
            final UserI user = getUser();
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

    protected Representation representProjectsForArchivableItem(final String label, final XnatProjectdata project, final Map<XnatProjectdataI, String> projects, final MediaType mediaType) {
        final XFTTable table = new XFTTable();
        table.initTable(new ArrayList<>(Arrays.asList("label", "ID", "Secondary_ID", "Name")));

        Object[] row = new Object[4];
        row[0] = label;
        row[1] = project.getId();
        row[2] = project.getSecondaryId();
        row[3] = project.getName();
        table.rows().add(row);

        for (final XnatProjectdataI key : projects.keySet()) {
            table.rows().add(ArrayUtils.toArray(projects.get(key), key.getId(), key.getSecondaryId(), key.getName()));
        }

        return representTable(table, mediaType, new Hashtable<>());
    }

    protected void changeExperimentPrimaryProject(final XnatExperimentdata experiment, final XnatProjectdata source, final XnatProjectdata destination, final String newLabel, final XnatExperimentdataShare share, final int index) throws Exception {
        final UserI user = getUser();
        if (!Permissions.canDelete(user, experiment)) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, "Specified user account has insufficient privileges for experiments in this project.");
            return;
        }

        if (experiment.getProject().equals(destination.getId())) {
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Already assigned to project: " + destination.getId());
            return;
        }

        final String workingLabel = StringUtils.defaultIfBlank(newLabel, StringUtils.defaultIfBlank(experiment.getLabel(), experiment.getId()));

        final XnatExperimentdata match = XnatExperimentdata.GetExptByProjectIdentifier(destination.getId(), workingLabel, user, false);

        if (match != null) {
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, "Specified label is already in use.");
        }

        final List<String> assessorList = StringUtils.isNotBlank(getQueryVariable("moveAssessors")) ? Arrays.asList(getQueryVariable("moveAssessors").split(",")) : null;

        final EventMetaI meta = BaseXnatExperimentdata.ChangePrimaryProject(user, experiment, destination, workingLabel, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.MODIFY_PROJECT), assessorList);
        XDAT.triggerXftItemEvent(experiment, XftItemEvent.MOVE, ImmutableMap.<String, Object>of("origin", source.getId(), "target", destination.getId()));

        if (share != null) {
            SaveItemHelper.authorizedRemoveChild(experiment.getItem(), "xnat:experimentData/sharing/share", share.getItem(), user, meta);
            experiment.removeSharing_share(index);
        }
    }

    @SuppressWarnings("unused")
    protected void shareExperimentToProject(final UserI user, final XnatProjectdata newProject, final XnatExperimentdata experiment) throws Exception {
        shareExperimentToProject(user, newProject, experiment, null);
    }

    protected void shareExperimentToProject(final UserI user, final XnatProjectdata newProject, final XnatExperimentdata experiment, final String newLabel) throws Exception {
        shareExperimentToProject(user, newProject, experiment, new XnatExperimentdataShare(user), newLabel);
    }

    protected void shareExperimentToProject(final UserI user, final XnatProjectdata newProject, final XnatExperimentdata experiment, final XnatExperimentdataShare shared, final String newLabel) throws Exception {
        shareExperimentToProject(user, newProject, experiment, shared, newLabel, true);
    }

    protected void shareExperimentToProject(final UserI user, final XnatProjectdata newProject, final XnatExperimentdata experiment, final XnatExperimentdataShare shared, final String newLabel, boolean shareAllScans) throws Exception {
        final String newProjectId = newProject.getId();

        shared.setProject(newProjectId);
        shared.setProperty("sharing_share_xnat_experimentda_id", experiment.getId());
        if(StringUtils.isNotBlank(newLabel)) {
            shared.setLabel(newLabel);
        }
        if (shareAllScans) {
            if (experiment instanceof XnatImagesessiondata) {
                for (XnatImagescandataI scan : ((XnatImagesessiondata) experiment).getScans_scan()) {
                    shareScanToProject(user, newProject, (XnatImagescandata) scan);
                }
            }
        }
        BaseXnatExperimentdata.SaveSharedProject(shared, experiment, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.CONFIGURED_PROJECT_SHARING));
        XDAT.triggerXftItemEvent(experiment, XftItemEvent.SHARE, ImmutableMap.<String, Object>of("target", newProjectId));
    }

    protected void shareScanToProject(final UserI user, final XnatProjectdata newProject, final XnatImagescandata scan)
            throws Exception {
        XnatImagescandataShare shared = new XnatImagescandataShare(user);
        final String newProjectId = newProject.getId();

        shared.setProject(newProjectId);
        shared.setProperty("sharing_share_xnat_imagescandat_xnat_imagescandata_id", scan.getXnatImagescandataId());
        shared.setLabel(scan.getId());
        BaseXnatImagescandata.SaveSharedProject(shared, scan, user, newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.CONFIGURED_PROJECT_SHARING));
        XDAT.triggerXftItemEvent(scan, XftItemEvent.SHARE, ImmutableMap.<String, Object>of("target", newProjectId));
    }

    protected void deleteItem(final XnatProjectdata proj, final BaseElement item) {
        if (!ArchivableItem.class.isAssignableFrom(item.getClass())) {
            throw new IllegalArgumentException("The BaseElement item must also implement the ArchivableItem interface, but the class " + item.getClass().getName() + " doesn't.");
        }

        try {
            final UserI               user       = getUser();
            final XnatProjectdata     newProject = getProjectFromFilePath(proj, (ArchivableItem) item);
            final PersistentWorkflowI wrk        = WorkflowUtils.buildOpenWorkflow(user, item.getItem(), newEventInstance(EventUtils.CATEGORY.DATA, EventUtils.getDeleteAction(item.getXSIType())));
            final EventMetaI          c          = wrk.buildEvent();

            try {
                final boolean                      removeFiles = isQueryVariableTrue("removeFiles");
                final XnatProjectdata              project     = (newProject != null) ? newProject : proj;
                final Class<? extends BaseElement> itemType    = item.getClass();

                final String message;
                if (XnatPvisitdata.class.isAssignableFrom(itemType)) {
                    message = ((XnatPvisitdata) item).delete(project, user, removeFiles, c);
                } else if (XnatImagesessiondata.class.isAssignableFrom(itemType)) {
                    message = ((XnatImagesessiondata) item).delete(project, user, removeFiles, c);
                } else if (XnatSubjectdata.class.isAssignableFrom(itemType)) {
                    message = ((XnatSubjectdata) item).delete(project, user, removeFiles, c);
                } else if (XnatExperimentdata.class.isAssignableFrom(itemType)) {
                    message = ((XnatExperimentdata) item).delete(project, user, removeFiles, c);
                } else {
                    message = null;
                }
                if (message != null) {
                    WorkflowUtils.fail(wrk, c);
                    getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, message);
                } else {
                    if(StringUtils.equals(project.getId(),((ArchivableItem) item).getProject())) {
                        //only issue DELETE event if it was being deleted from the root projecct
                        XDAT.triggerXftItemEvent(item, DELETE, ImmutableMap.of("target", project.getId()));
                    }
                    WorkflowUtils.complete(wrk, c);
                }
            } catch (Exception e) {
                try {
                    WorkflowUtils.fail(wrk, c);
                } catch (Exception e1) {
                    logger.error("", e1);
                }
                logger.error("", e);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
            }
        } catch (PersistentWorkflowUtils.EventRequirementAbsent e) {
            logger.error("Forbidden: " + e.getMessage(), e);
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN, e.getMessage());
        } catch (NotFoundException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "Unable to identify project: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }

    protected XnatProjectdata getProjectFromFilePath(final XnatProjectdata project, final ArchivableItem item) throws NotFoundException {
        if (StringUtils.isNotBlank(filepath)) {
            if (!StringUtils.startsWith(filepath, "projects/")) {
                throw new IllegalArgumentException("Illegal file path '" + filepath + "' does not start with 'projects/'.");
            }
            final String projectId = StringUtils.removeStart(filepath, "projects/");
            return getProjectById(projectId);
        }
        return project != null ? project : getProjectById(item.getProject());
    }

    protected boolean rename(final XnatProjectdata proj, final ArchivableItem existing, final String label, final UserI user) {
        try {
            new Rename(proj, existing, label, user, getReason(), getEventType()).call();
        } catch (Rename.ProcessingInProgress e) {
            final String message = "Specified session is being processed (" + e.getPipelineName() + ").";
            logger.error(message, e);
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, message);
            return false;
        } catch (Rename.DuplicateLabelException | Rename.LabelConflictException e) {
            final String message = "Specified label " + label + " is already in use.";
            logger.error(message, e);
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, message);
            return false;
        } catch (Rename.FolderConflictException e) {
            final String message = "File system destination contains pre-existing files";
            logger.error(message, e);
            getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT, message);
            return false;
        } catch (InvalidArchiveStructure | URISyntaxException e) {
            final String message = "Non-standard archive structure in existing experiment directory.";
            logger.error(message, e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, message);
            return false;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,e.getMessage());
            return false;
        }
        return true;
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
                return userResourceWhitelist.contains(getUser().getUsername());
            }
        } catch (IOException e) {
            String message = "Error retrieving user list" + (projectId == null ? "" : " for project " + projectId);
            logger.error(message, e);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, message + ": " + e.getMessage());
        }
        return false;
    }

    private boolean forceDownload(final File file, final MediaType mediaType) {
        return StringUtils.startsWith(mediaType.getName(), "APPLICATION") ||
               !XDAT.getSiteConfigPreferences().getAllowHtmlResourceRendering() ||
               !CollectionUtils.containsAny(XDAT.getSiteConfigPreferences().getHtmlResourceRenderingWhitelist().stream().map(String::toLowerCase).collect(Collectors.toList()),
                                            Arrays.asList("*", StringUtils.lowerCase(FilenameUtils.getExtension(file.getName()))));
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
    @SuppressWarnings("RedundantThrows")
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

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean validateSubject(final XnatSubjectdata subject) throws Exception {
        if (StringUtils.isNotBlank(subject.getLabel()) && !XftStringUtils.isValidId(subject.getId())) {
            getResponse().setStatus(Status.CLIENT_ERROR_EXPECTATION_FAILED, "Invalid character in subject label.");
            return false;
        }

        final ValidationResults results = subject.validate();
        if (results != null && !results.isValid()) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, results.toFullString());
            return false;
        }

        return true;
    }

    public interface FilteredResourceHandlerI {
        boolean canHandle(SecureResource resource);

        Representation handle(SecureResource resource, Variant variant) throws Exception;
    }

    private boolean createOrUpdateImpl(final boolean isCreate, final ArchivableItem item, final boolean overwriteSecurity, final boolean allowDataDeletion, final PersistentWorkflowI workflow, final EventMetaI meta) throws Exception {
        try {
            final UserI user = getUser();
            if (SaveItemHelper.authorizedSave(item, user, overwriteSecurity, allowDataDeletion, meta)) {
                if (isCreate) {
                    final XFTItem xftItem = item.getItem();
                    if (xftItem.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME) || xftItem.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME) || xftItem.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                        XDAT.triggerXftItemEvent(xftItem, XftItemEvent.CREATE);
                    }
                }
                WorkflowUtils.complete(workflow, meta);
                Users.clearCache(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            WorkflowUtils.fail(workflow, meta);
            throw e;
        }
    }

    protected void storeStatusList(final String transaction_id, final StatusList sl) throws IllegalArgumentException {
        retrieveSQManager().storeStatusQueue(transaction_id, sl);
    }

    protected PersistentStatusQueueManagerI retrieveSQManager() {
        return new HTTPSessionStatusManagerQueue(this.getHttpSession());
    }

    private XnatProjectdata getProjectById(final String projectId) throws NotFoundException {
        return Optional.ofNullable(XnatProjectdata.getXnatProjectdatasById(projectId, getUser(),false)).orElseThrow(() -> new NotFoundException(projectId));
    }

    protected String buildOffsetFromParams(){
        //inject paging
        Long offset = null;
        Long limit = null;
        if(this.hasQueryVariable("limit") || this.hasQueryVariable("offset")) {
            final String offsetS = this.getQueryVariable("offset");
            if (offsetS == null) {
                offset = DEFAULT_PAGE_NUM;
            }else if (StringUtils.equals("*", offsetS)) {
                offset = null;
            }else{
                offset = Long.valueOf(offsetS);
            }

            final String rowsS= this.getQueryVariable("limit");
            if(rowsS == null){
                limit = DEFAULT_PAGE_SIZE;
            }else if (StringUtils.equals("*", rowsS)) {
                limit = null;
            }else{
                limit = Long.valueOf(rowsS);
            }
        }

        if(offset != null && limit != null && (offset > 0 || limit > 0)){
            hasOffset=true;
            return " LIMIT "+ limit + " OFFSET " + offset;
        }else{
            return "";
        }
    }

    private static final Class<?>[] OBJECT_REPRESENTATION_CTOR_PARAM_TYPES = {XFTTable.class, Map.class, Hashtable.class, MediaType.class};

    protected boolean hasOffset = false;
    private final UserI                      _user;
    private final SerializerService          _serializer;
    private final NamedParameterJdbcTemplate _template;
    private final UserDataCache              _userDataCache;
}
