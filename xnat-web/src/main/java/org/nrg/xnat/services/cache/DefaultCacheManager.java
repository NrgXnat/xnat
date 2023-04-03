package org.nrg.xnat.services.cache;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.orm.DatabaseHelper;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.Initializing;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.cache.CacheManager;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.InvalidValueException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.XnatAppInfo;
import org.springframework.cache.Cache;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.nrg.xft.XFTItem.XDAT_META_ELEMENT;
import static org.nrg.xft.XFTItem.XDAT_META_ELEMENT_ID;
import static org.nrg.xft.XFTItem.XDAT_META_ELEMENT_NAME;
import static org.nrg.xft.XFTItem.XML_PATH_XDAT_META_ELEMENT_ID;
import static org.nrg.xft.XFTItem.XML_PATH_XDAT_META_ELEMENT_NAME;

@Service
@Slf4j
public class DefaultCacheManager implements CacheManager, Initializing {
    private static final String                           CACHE_NAME                         = "XdatMetaElementCache";
    private static final List<String>                     XSI_TYPES                          = Arrays.asList(XnatImagesessiondata.SCHEMA_ELEMENT_NAME, XnatImagescandata.SCHEMA_ELEMENT_NAME, XnatAbstractresource.SCHEMA_ELEMENT_NAME);
    private static final Predicate<GenericWrapperElement> PREDICATE_SESSION_OR_SCAN_DATATYPE = element -> XSI_TYPES.stream().anyMatch(element::instanceOf)
                                                                                                          && !StringUtils.equalsAny(element.getFullXMLName(), XnatImagesessiondata.SCHEMA_ELEMENT_NAME, XnatImagescandata.SCHEMA_ELEMENT_NAME);
    private static final String                           CACHE_ID_SEPARATOR                 = "/";
    private static final String                           PARAM_ELEMENT_NAMES                = "elementNames";
    private static final String                           QUERY_GET_META_ELEMENT_NAMES       = "SELECT " + XDAT_META_ELEMENT_NAME + " FROM xdat_meta_element";
    private static final String                           QUERY_GET_META_ELEMENTS            = "SELECT " + XDAT_META_ELEMENT_ID + ", " + XDAT_META_ELEMENT_NAME + " FROM xdat_meta_element WHERE " + XDAT_META_ELEMENT_NAME + " IN (:" + PARAM_ELEMENT_NAMES + ")";

    private final AtomicBoolean _initialized = new AtomicBoolean();

    private final Cache                      _cache;
    private final XnatAppInfo                _appInfo;
    private final XnatUserProvider           _primaryAdminUserProvider;
    private final DatabaseHelper             _helper;
    private final NamedParameterJdbcTemplate _template;

    public DefaultCacheManager(final org.springframework.cache.CacheManager cacheManager, final XnatAppInfo appInfo, final XnatUserProvider primaryAdminUserProvider, final DatabaseHelper helper, final NamedParameterJdbcTemplate template) {
        _cache                    = cacheManager.getCache(CACHE_NAME);
        _appInfo                  = appInfo;
        _primaryAdminUserProvider = primaryAdminUserProvider;
        _helper                   = helper;
        _template                 = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCacheName() {
        return CACHE_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInitialize() {
        // If not initialized yet, we can't count on anything.
        if (!_appInfo.isInitialized()) {
            log.debug("Application not initialized, can't initialize the xdat:meta_element cache yet");
            return false;
        }
        // Don't initialize the primary node until the xdat_meta_element table exists. Only initialize non-primary nodes
        // if the xdat_meta_element table has entries and those entries match the list of datatypes from XFT. Once those
        // match, that implies that the primary node has finished populating the xdat_meta_element table.
        return _appInfo.isPrimaryNode() ? verifyXdatMetaElementTable() : verifyXdatMetaElementEntries();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Future<Boolean> initialize() {
        final UserI user = _primaryAdminUserProvider.get();
        if (user == null) {
            log.warn("Couldn't load the primary admin user, deferring initialization");
            return new AsyncResult<>(false);
        }

        final Optional<GenericWrapperElement> optionalXdatMetaElementElement = getXdatMetaElementElement();
        if (!optionalXdatMetaElementElement.isPresent()) {
            log.warn("Couldn't find the schema element for xdat:meta_element, deferring initialization");
            return new AsyncResult<>(false);
        }

        final GenericWrapperElement metaElement = optionalXdatMetaElementElement.get();

        final Optional<List<ItemI>> optionalElements;
        if (_appInfo.isPrimaryNode()) {
            // Only initialize the table if this is the primary node.
            optionalElements = initializeXdatMetaElements(user, metaElement);
            if (!optionalElements.isPresent()) {
                log.warn("Tried to initialize the xdat:meta_element table but that didn't happen. Please check other logs for any errors, deferring initialization");
                return new AsyncResult<>(false);
            }
        } else {
            optionalElements = retrieveXdatMetaElements(user, metaElement);
            if (!optionalElements.isPresent()) {
                log.warn("Tried to retrieve the list of xdat:meta_element items but didn't find anything, deferring initialization");
                return new AsyncResult<>(false);
            }
        }

        final List<ItemI> elements = optionalElements.get();
        log.debug("Found {} xdat:meta_element objects, preparing to cache them", elements.size());

        final List<ItemI> failed = new ArrayList<>();
        elements.forEach(element -> {
            try {
                put(XDAT_META_ELEMENT, element.getStringProperty(XDAT_META_ELEMENT_NAME), element);
            } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                log.error("An error occurred trying to get the element name of an xdat:meta_element entry: {}", element, e);
                failed.add(element);
            }
        });
        if (!failed.isEmpty()) {
            log.warn("Failed to cache {} items, please check earlier log entries for more info", failed.size());
        }

        log.info("Completed caching {} xdat:meta_element objects", elements.size() - failed.size());
        _initialized.set(true);
        return new AsyncResult<>(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInitialized() {
        return _initialized.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getInitializationStatus() {
        return ImmutableMap.of("initialized", _initialized.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearAll() {
        if (isInitialized()) {
            log.info("I'm being asked to clear all cached data");
            _cache.clear();
        } else {
            log.warn("I'm being asked to clear all cached data but I haven't completed initialization yet so I'm not going to do that");
        }
    }

    @Override
    public void clearXsiType(final String xsiType) {
        final String  prefix = xsiType + CACHE_ID_SEPARATOR;
        final List<?> keys   = getNativeCache().getKeysNoDuplicateCheck();
        keys.stream().filter(key -> key instanceof String && StringUtils.startsWith((String) key, prefix)).forEach(_cache::evict);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object retrieve(final String xsiType, final Object id) {
        if (isInvalid(xsiType, id)) {
            return null;
        }

        final String cacheId = getCacheId(xsiType, id);
        log.debug("Going to try to retrieve an object with the cache ID {}", cacheId);

        final Cache.ValueWrapper wrapper = _cache.get(cacheId);
        if (wrapper == null) {
            log.info("Someone tried to retrieve something from the cache, but the requested cache ID {} wasn't found", cacheId);
            return null;
        }

        log.info("Someone requested cached data for cache ID {}", cacheId);
        return wrapper.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Object put(String xsiType, Object id, Object item) {
        if (isInvalid(xsiType, id, item)) {
            return null;
        }

        final String cacheId = getCacheId(xsiType, id);
        log.debug("Caching an object with the cache ID {}", cacheId);

        final Cache.ValueWrapper wrapper = _cache.putIfAbsent(cacheId, item);
        if (wrapper == null) {
            log.debug("Cached object with ID {}", cacheId);
            return item;
        }

        log.debug("Tried to cache object with ID {} but it looks like that's already cached, returning existing cached item", cacheId);
        return wrapper.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Object remove(String xsiType, Object id) {
        if (isInvalid(xsiType, id)) {
            return null;
        }
        final String cacheId = getCacheId(xsiType, id);
        log.debug("Going to try to remove an object with the cache ID {}", cacheId);

        final Object cached = _cache.get(cacheId);
        if (cached != null) {
            _cache.evict(cacheId);
        }
        return cached;
    }

    /**
     * Gets the underlying native cache implementation to allow some advanced operations. Cache access should be done
     * with the Spring cache abstraction wherever possible!
     *
     * @return The native cache implementation.
     */
    private net.sf.ehcache.Cache getNativeCache() {
        return (net.sf.ehcache.Cache) _cache.getNativeCache();
    }

    private boolean verifyXdatMetaElementTable() {
        try {
            return _helper.tableExists("xdat_meta_element") && GenericWrapperElement.GetElement(XDAT_META_ELEMENT) != null;
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("An error has occurred trying to check for cache manager initialization", e);
        } catch (SQLException e) {
            log.error("An error has occurred trying to check whether the xdat_meta_element table exists", e);
        }
        return false;
    }

    /**
     * Verifies that the <pre>xdat_meta_element</pre> table exists and contains all the schema elements existing in XFT.
     *
     * @return Returns true if <pre>xdat_meta_element</pre> exists and contains all schemas in XFT, false otherwise.
     */
    private boolean verifyXdatMetaElementEntries() {
        if (!verifyXdatMetaElementTable()) {
            log.info("The xdat_meta_element table does not yet exist");
            return false;
        }
        final Optional<List<String>> optional = getAllXdatMetaElementNames();
        if (!optional.isPresent()) {
            log.info("Couldn't load xdat:meta_element names from XFT");
            return false;
        }
        final List<String> allElements = optional.get();
        if (allElements.isEmpty()) {
            log.info("Loaded xdat:meta_element names from XFT but nothing was found");
            return false;
        }
        return new HashSet<>(getProvisionedXdatMetaElementNames()).containsAll(allElements);
    }

    /**
     * Gets the element names stored in the <pre>xdat_meta_element</pre> table.
     *
     * @return A list of the element names stored in the <pre>xdat_meta_element</pre> table.
     */
    private List<String> getProvisionedXdatMetaElementNames() {
        return _template.queryForList(QUERY_GET_META_ELEMENT_NAMES, EmptySqlParameterSource.INSTANCE, String.class);
    }

    /**
     * Initializes the <pre>xdat_meta_elements</pre> table with session, scan, and resource datatypes if not already
     * populated. This should only be run on the primary node.
     */
    private Optional<List<ItemI>> initializeXdatMetaElements(final UserI user, final GenericWrapperElement metaElement) {
        final Optional<List<String>> optional = getAllXdatMetaElementNames();
        if (!optional.isPresent()) {
            log.warn("Tried to get all xdat:meta_element names but nothing was found");
            return Optional.empty();
        }

        final List<String> allElements   = optional.get();
        final List<String> existing      = getProvisionedXdatMetaElementNames();
        final List<String> unprovisioned = allElements.stream().filter(elementName -> !existing.contains(elementName)).collect(Collectors.toList());
        if (log.isDebugEnabled()) {
            log.debug("Found {} image session or scan datatypes total, {} datatypes already provisioned in the xdat_meta_element table, preparing to initialize datatype cache with {} new elements: {}", allElements.size(), existing.size(), unprovisioned.size(), String.join(", ", unprovisioned));
        } else {
            log.info("Found {} image session or scan datatypes total, {} datatypes already provisioned in the xdat_meta_element table, preparing to initialize datatype cache with {} new elements", allElements.size(), existing.size(), unprovisioned.size());
        }

        final Map<Boolean, Map<String, ItemI>> results = unprovisioned.stream()
                                                                      .map(elementName -> Pair.of(elementName, createXdatMetaElement(user, metaElement, elementName)))
                                                                      .collect(Collectors.partitioningBy(pair -> pair.getValue() != null,
                                                                                                         Collectors.toMap(Pair::getKey, Pair::getValue)));

        final Set<String> failed = results.get(false).keySet();
        if (!failed.isEmpty()) {
            log.warn("Failed to create {} xdat:meta_element objects: {}", failed.size(), String.join(", ", failed));
            // TODO: Check and retry here...
        }

        log.info("Created and cached {} xdat:meta_element items, returning {} previously existing xdat:meta_element items", results.get(true).size(), existing.size());
        return Optional.of(_template.query(QUERY_GET_META_ELEMENTS, new MapSqlParameterSource(PARAM_ELEMENT_NAMES, existing), (result, index) -> {
            final int    metaElementId   = result.getInt(XDAT_META_ELEMENT_ID);
            final String metaElementName = result.getString(XDAT_META_ELEMENT_NAME);
            try {
                return XFTItem.NewItem(metaElement, ImmutableMap.of(XML_PATH_XDAT_META_ELEMENT_ID, metaElementId, XML_PATH_XDAT_META_ELEMENT_NAME, metaElementName), false, _primaryAdminUserProvider.get());
            } catch (ElementNotFoundException | FieldNotFoundException | InvalidValueException e) {
                log.error("An error occurred trying to create an XFTItem for meta element with ID {} and element name {}", metaElementId, metaElementName, e);
                return null;
            }
        }).stream().filter(Objects::nonNull).collect(Collectors.toList()));
    }

    /**
     * Gets a list of items representing the various <pre>xdat:meta_element</pre> objects on the system.
     *
     * @param user        The user to use for search operation
     * @param metaElement The schema element for the search
     *
     * @return A list of <pre>xdat:meta_element</pre> items
     */
    private Optional<List<ItemI>> retrieveXdatMetaElements(final UserI user, final GenericWrapperElement metaElement) {
        try {
            return Optional.of(ItemSearch.GetAllItems(metaElement.getFullXMLName(), user, false).getItems());
        } catch (Exception e) {
            log.error("An error occurred trying to retrieve the list of xdat:meta_element objects", e);
            return Optional.empty();
        }
    }

    @Nullable
    private ItemI createXdatMetaElement(final UserI user, final GenericWrapperElement metaElement, final String elementName) {
        try {
            XFTItem item = XFTItem.NewItem(metaElement, ImmutableMap.of(XML_PATH_XDAT_META_ELEMENT_NAME, elementName), false, user);
            SaveItemHelper.authorizedSave(item, user, false, false, EventUtils.ADMIN_EVENT(user));
            log.info("Created xdat:meta_element object for datatype {}", elementName);
            return item;
        } catch (Exception e) {
            log.error("Got an error trying to create the xdat:meta_element object for datatype {}", elementName, e);
            return null;
        }
    }

    /**
     * Checks whether the XSI type has a value and whether other objects like ID and object to be cached are not null.
     *
     * @param xsiType The XSI type to validate
     * @param objects The objects to check for null
     *
     * @return Returns false if XSI type is not blank and none of the other objects are null.
     */
    private static boolean isInvalid(final String xsiType, final Object... objects) {
        if (StringUtils.isBlank(xsiType) || ObjectUtils.anyNull(objects)) {
            log.info("Someone tried to retrieve something from the cache, but xsiType ({}), ID ({}), or item was null or blank", xsiType, objects[0]);
            return true;
        }
        return false;
    }

    /**
     * Formats the XSI type and ID for use as a cache ID.
     *
     * @param xsiType The XSI type to format
     * @param id      The ID to format
     *
     * @return A formatted cache ID.
     */
    private static String getCacheId(final String xsiType, final Object id) {
        return xsiType + CACHE_ID_SEPARATOR + id;
    }

    /**
     * Gets the schema element object for xdat:meta_element. If an error is encountered trying to retrieve the schema
     * element, the returned optional is empty.
     *
     * @return Returns the schema element object for xdat:meta_element.
     */
    private static Optional<GenericWrapperElement> getXdatMetaElementElement() {
        try {
            return Optional.ofNullable(GenericWrapperElement.GetElement(XDAT_META_ELEMENT));
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("An error occurred trying to get the element object for xdat:meta_element", e);
            return Optional.empty();
        }
    }

    /**
     * Gets a list of the names of all schema elements that extend the following datatypes:
     *
     * <ul>
     *     <li><pre>xnat:imageSessionData</pre></li>
     *     <li><pre>xnat:imageScanData</pre></li>
     *     <li><pre>xnat:abstractResource</pre></li>
     * </ul>
     *
     * Note that <pre>xnat:imageSessionData</pre> and <pre>xnat:imageScanData</pre> are <em>not</em> included in the
     * returned list. If an error is encountered trying to retrieve the schema elements, the returned optional is empty.
     *
     * @return A list of the names of all schema elements for sessions, scans, and resources.
     */
    private static Optional<List<String>> getAllXdatMetaElementNames() {
        try {
            return Optional.of(GenericWrapperElement.GetAllElements(false).stream()
                                                    .filter(PREDICATE_SESSION_OR_SCAN_DATATYPE)
                                                    .map(GenericWrapperElement::getFullXMLName)
                                                    .collect(Collectors.toList()));
        } catch (XFTInitException | ElementNotFoundException e) {
            log.error("An error occurred trying to get the element objects for image sessions and scans", e);
            return Optional.empty();
        }
    }
}
