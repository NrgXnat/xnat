/*
 * web: org.nrg.dcm.scp.DicomSCPManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.h2.Driver;
import org.jetbrains.annotations.NotNull;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicateTitleAndPortException;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.dcm.scp.exceptions.UnknownDicomScpInstanceException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.services.NrgEventService;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.event.listeners.methods.AbstractScopedSiteConfigPreferenceHandlerMethod;
import org.nrg.xnat.utils.XnatUserProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;

import static org.nrg.dcm.scp.DicomSCPManager.TOOL_ID;

@SuppressWarnings("SqlResolve")
@Service
@NrgPreferenceBean(toolId = TOOL_ID, toolName = "DICOM SCP Manager", description = "Manages configuration of the various DICOM SCP endpoints on the XNAT system.")
public class DicomSCPManager extends EventTriggeringAbstractPreferenceBean implements PreferenceHandlerMethod {
    public static final String TOOL_ID = "dicomScpManager";
    public static final String PREF_ID = "dicomSCPInstances";

    @Autowired
    public DicomSCPManager(final NrgPreferenceService preferenceService, final NrgEventService eventService, final XnatUserProvider receivedFileUserProvider, final ApplicationContext context, final SiteConfigPreferences siteConfigPreferences, final DicomObjectIdentifier<XnatProjectdata> primaryDicomObjectIdentifier, final Map<String, DicomObjectIdentifier<XnatProjectdata>> dicomObjectIdentifiers) {
        super(preferenceService, eventService);

        _provider = receivedFileUserProvider;
        _context = context;

        _isEnableDicomReceiver = siteConfigPreferences.isEnableDicomReceiver();

        String primaryBeanId = null;

        final List<String> sortedDicomObjectIdentifierBeanIds = Lists.newArrayList();
        for (final String beanId : dicomObjectIdentifiers.keySet()) {
            final DicomObjectIdentifier<XnatProjectdata> identifier = dicomObjectIdentifiers.get(beanId);
            _dicomObjectIdentifiers.put(beanId, identifier);
            if (identifier == primaryDicomObjectIdentifier) {
                primaryBeanId = beanId;
            } else {
                sortedDicomObjectIdentifierBeanIds.add(beanId);
                _dicomObjectIdentifiers.put(beanId, identifier);
            }
        }

        Collections.sort(sortedDicomObjectIdentifierBeanIds);
        if (StringUtils.isNotBlank(primaryBeanId)) {
            _primaryDicomObjectIdentifierBeanId = primaryBeanId;
            sortedDicomObjectIdentifierBeanIds.add(0, _primaryDicomObjectIdentifierBeanId);
        } else {
            _primaryDicomObjectIdentifierBeanId = sortedDicomObjectIdentifierBeanIds.get(0);
        }

        _dicomObjectIdentifierBeanIds = ImmutableSet.copyOf(sortedDicomObjectIdentifierBeanIds);
        _identifiersToMapFunction = new IdentifiersToMapFunction(_dicomObjectIdentifiers);

        _database = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName(PREF_ID)
                .addScript("META-INF/xnat/scripts/init-dicom-scp-db.sql")
                .build();
        _template = new NamedParameterJdbcTemplate(new BasicDataSource() {{
            setDriverClassName(Driver.class.getName());
            setUrl(DSCPM_DB_URL);
            setUsername("sa");
            setPassword("");
        }});
    }

    @PreDestroy
    public void shutdown() {
        _log.debug("Handling pre-destroy actions, shutting down DICOM SCP receivers.");
        stop();
        _database.shutdown();
    }

    /**
     * Pass-through method to dispatch calls to {@link PreferenceHandlerMethod#getToolIds()} onto the internal handler
     * proxy object. The method handles updates to the {@link SiteConfigPreferences#isEnableDicomReceiver() enable DICOM
     * receiver} preference.
     *
     * @return Returns tool ID for {@link SiteConfigPreferences}.
     */
    @Override
    public List<String> getToolIds() {
        return _handlerProxy.getToolIds();
    }

    /**
     * Pass-through method to dispatch calls to {@link PreferenceHandlerMethod#getHandledPreferences()} onto the
     * internal handler proxy object.
     *
     * @return Returns the preference ID for {@link SiteConfigPreferences#isEnableDicomReceiver()}.
     */
    @Override
    public List<String> getHandledPreferences() {
        return _handlerProxy.getHandledPreferences();
    }

    /**
     * Pass-through method to dispatch calls to {@link PreferenceHandlerMethod#findHandledPreferences(Collection)} onto
     * the internal handler proxy object.
     *
     * @return Returns the preference ID for {@link SiteConfigPreferences#isEnableDicomReceiver()}.
     */
    @Override
    public Set<String> findHandledPreferences(final Collection<String> preferences) {
        return _handlerProxy.findHandledPreferences(preferences);
    }

    /**
     * Pass-through method to dispatch calls to {@link PreferenceHandlerMethod#handlePreferences(Map)} onto the internal
     * handler proxy object. Calls the {@link PreferenceHandlerMethod#handlePreference(String, String)} for the relevant
     * preference.
     */
    @Override
    public void handlePreferences(final Map<String, String> values) {
        _handlerProxy.handlePreferences(values);
    }

    /**
     * Pass-through method to dispatch calls to {@link PreferenceHandlerMethod#handlePreference(String, String)} onto
     * the internal handler proxy object. Updates the internal flag that tracks whether DICOM receivers should be
     * enabled on the system.
     */
    @Override
    public void handlePreference(final String preference, final String value) {
        _handlerProxy.handlePreference(preference, value);
    }

    @NrgPreference(defaultValue = "{'1': {'id': '1', 'aeTitle': 'XNAT', 'port': 8104, 'enabled': true}}", key = "id")
    public Map<String, DicomSCPInstance> getDicomSCPInstances() {
        return getMapValue(PREF_ID);
    }

    /**
     * Sets the full map of {@link DicomSCPInstance DICOM SCP instance} definitions.
     *
     * @param instances The DICOM SCP definitions to save.
     */
    @SuppressWarnings("unused")
    public List<DicomSCPInstance> setDicomSCPInstances(final Map<String, DicomSCPInstance> instances) throws DICOMReceiverWithDuplicateTitleAndPortException, DicomNetworkException, UnknownDicomHelperInstanceException {
        try {
            // Have to cache these first, since the database caching does double duty with generating IDs for any new
            // instances that are in the map.
            final List<DicomSCPInstance> cached = cacheDicomScpInstances(instances.values());

            // Now use a new map from the cached instances so that the new IDs will be persisted.
            setMapValue(PREF_ID, Maps.uniqueIndex(cached, new Function<DicomSCPInstance, String>() {
                public String apply(DicomSCPInstance instance) {
                    return Integer.toString(instance.getId());
                }
            }));

            return cached;
        } catch (InvalidPreferenceName invalidPreferenceName) {
            _log.error("Invalid preference name '" + PREF_ID + "': something is very wrong here.", invalidPreferenceName);
            return Collections.emptyList();
        }
    }

    /**
     * Sets the submitted {@link DicomSCPInstance DICOM SCP instance} definition. If the {@link DicomSCPInstance#getId()
     * instance ID} matches an existing DICOM SCP instance, that instance will be updated.
     *
     * @param instance The instance to be set.
     *
     * @throws DICOMReceiverWithDuplicateTitleAndPortException When the new instance is enabled and there's
     *                                                         already an enabled instance with the same AE title
     *                                                         and port.
     * @throws DicomNetworkException                           When an error occurs during DICOM communication
     *                                                         operations.
     */
    public DicomSCPInstance setDicomSCPInstance(final DicomSCPInstance instance) throws DICOMReceiverWithDuplicateTitleAndPortException, DicomNetworkException, UnknownDicomHelperInstanceException {
        try {
            final List<DicomSCPInstance> instances = setDicomSCPInstances(new HashMap<String, DicomSCPInstance>() {{
                put(Integer.toString(instance.getId()), instance);
            }});
            return instances.isEmpty() ? null : instances.get(0);
        } catch (DuplicateKeyException e) {
            throw new DICOMReceiverWithDuplicateTitleAndPortException(instance.getAeTitle(), instance.getPort());
        }
    }

    @SuppressWarnings("unused")
    public void deleteDicomSCPInstances(final Integer... ids) throws DicomNetworkException, UnknownDicomHelperInstanceException, DICOMReceiverWithDuplicateTitleAndPortException {
        for (final int id : ids) {
            if (!hasDicomSCPInstance(id)) {
                continue;
            }
            try {
                delete(PREF_ID, Integer.toString(id));
            } catch (InvalidPreferenceName invalidPreferenceName) {
                _log.info("Got an invalidate preference name error trying to delete DICOM SCP instance " + id);
            }
        }
        uncacheDicomScpInstances(ids);
    }

    public void deleteDicomSCPInstance(final int id) throws UnknownDicomScpInstanceException, UnknownDicomHelperInstanceException, DicomNetworkException, DICOMReceiverWithDuplicateTitleAndPortException {
        deleteDicomSCPInstances(id);
    }

    /**
     * Indicates whether a {@link DicomSCPInstance DICOM SCP instance} with the indicated ID exists.
     *
     * @param id The ID of the DICOM SCP instance to check.
     *
     * @return Returns true if the instance exists, false otherwise.
     */
    public boolean hasDicomSCPInstance(final int id) {
        return _template.queryForObject(DOES_INSTANCE_ID_EXIST, new MapSqlParameterSource("id", id), Boolean.class);
    }

    /**
     * Indicates whether a {@link DicomSCPInstance DICOM SCP instance} with the indicated AE title and port exists.
     *
     * @param aeTitle The AE title to search for.
     * @param port    The port to search for.
     *
     * @return Returns true if an instance with the indicated AE title and port exists, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean hasDicomSCPInstance(final String aeTitle, final String port) {
        return _template.queryForObject(DOES_INSTANCE_AE_TITLE_AND_PORT_EXIST,
                                        new HashMap<String, Object>() {{
                                            put("aeTitle", aeTitle);
                                            put("port", port);
                                        }},
                                        Boolean.class);
    }

    public DicomSCPInstance getDicomSCPInstance(final int id) {
        return _template.queryForObject(GET_INSTANCE_BY_ID, new MapSqlParameterSource("id", id), DICOM_SCP_INSTANCE_ROW_MAPPER);
    }

    public DicomSCPInstance getDicomSCPInstance(final String aeTitle, final int port) {
        return _template.queryForObject(GET_INSTANCE_BY_AE_TITLE_AND_PORT, new HashMap<String, Object>() {{
            put("aeTitle", aeTitle);
            put("port", port);
        }}, DICOM_SCP_INSTANCE_ROW_MAPPER);
    }

    public List<DicomSCPInstance> getEnabledDicomSCPInstancesByPort(final int port) {
        return _template.query(GET_ENABLED_INSTANCES_BY_PORT, new HashMap<String, Object>() {{
            put("enabled", true);
            put("port", port);
        }}, DICOM_SCP_INSTANCE_ROW_MAPPER);
    }

    public void enableDicomSCPInstance(final int id) throws DICOMReceiverWithDuplicateTitleAndPortException, DicomNetworkException, UnknownDicomHelperInstanceException {
        enableDicomSCPInstances(id);
    }

    public void enableDicomSCPInstances(final int... ids) throws DICOMReceiverWithDuplicateTitleAndPortException, DicomNetworkException, UnknownDicomHelperInstanceException {
        toggleEnabled(true, Arrays.asList(ArrayUtils.toObject(ids)));
    }

    public void disableDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        disableDicomSCPInstances(id);
    }

    public void disableDicomSCPInstances(final int... ids) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        toggleEnabled(false, Arrays.asList(ArrayUtils.toObject(ids)));
    }

    /**
     * This starts all configured DICOM SCP instances, as long as the {@link SiteConfigPreferences#isEnableDicomReceiver()}
     * preference setting is set to true.
     */
    public List<String> start() throws UnknownDicomHelperInstanceException, DicomNetworkException {
        return _isEnableDicomReceiver ? updateDicomSCPs(getPortsWithEnabledInstances()) : Collections.<String>emptyList();
    }

    public List<String> stop() {
        final List<String> stopped = new ArrayList<>();
        for (final DicomSCP dicomSCP : _dicomSCPs.values()) {
            final List<String> aeTitles = dicomSCP.stop();
            for (final String aeTitle : aeTitles) {
                stopped.add(DicomSCPInstance.formatDicomSCPInstanceKey(aeTitle, dicomSCP.getPort()));
            }
        }
        return stopped;
    }

    /**
     * Returns each DICOM SCP definition along with that definition's enabled status. Note that the enabled status is
     * taken from the saved DICOM SCP definition and is <i>not</i> affected by the system-wide {@link
     * SiteConfigPreferences#isEnableDicomReceiver()} setting.
     *
     * @return A map of each definition and its enabled status.
     */
    public Map<DicomSCPInstance, Boolean> areDicomSCPInstancesStarted() {
        final Map<DicomSCPInstance, Boolean> statuses = new HashMap<>();
        for (final DicomSCPInstance instance : getDicomSCPInstances().values()) {
            statuses.put(instance, false);
        }
        for (final int port : _dicomSCPs.keySet()) {
            final DicomSCP dicomSCP = _dicomSCPs.get(port);
            for (final String aeTitle : dicomSCP.getAeTitles()) {
                final DicomSCPInstance instance = getDicomSCPInstance(aeTitle, port);
                statuses.put(instance, instance.isEnabled());
            }
        }
        return statuses;
    }

    public Map<String, String> getDicomObjectIdentifierBeans() {
        return Maps.asMap(_dicomObjectIdentifierBeanIds, _identifiersToMapFunction);
    }

    public Map<String, DicomObjectIdentifier<XnatProjectdata>> getDicomObjectIdentifiers() {
        return _dicomObjectIdentifiers;
    }

    public DicomObjectIdentifier<XnatProjectdata> getDicomObjectIdentifier(final String beanId) {
        return StringUtils.isBlank(beanId)
               ? getDefaultDicomObjectIdentifier()
               : _dicomObjectIdentifierBeanIds.contains(beanId)
                 ? getDicomObjectIdentifiers().get(beanId)
                 : null;
    }

    public DicomObjectIdentifier<XnatProjectdata> getDefaultDicomObjectIdentifier() {
        return getDicomObjectIdentifiers().get(_primaryDicomObjectIdentifierBeanId);
    }

    public void resetDicomObjectIdentifier() {
        final DicomObjectIdentifier<XnatProjectdata> objectIdentifier = getDefaultDicomObjectIdentifier();
        if (objectIdentifier instanceof CompositeDicomObjectIdentifier) {
            ((CompositeDicomObjectIdentifier) objectIdentifier).getProjectIdentifier().reset();
        }
    }

    public void resetDicomObjectIdentifier(final String beanId) {
        final DicomObjectIdentifier<XnatProjectdata> identifier = getDicomObjectIdentifier(beanId);
        if (identifier instanceof CompositeDicomObjectIdentifier) {
            ((CompositeDicomObjectIdentifier) identifier).getProjectIdentifier().reset();
        }
    }

    public void resetDicomObjectIdentifierBeans() {
        for (final DicomObjectIdentifier identifier : getDicomObjectIdentifiers().values()) {
            if (identifier instanceof CompositeDicomObjectIdentifier) {
                ((CompositeDicomObjectIdentifier) identifier).getProjectIdentifier().reset();
            }
        }
    }

    /**
     * Post-processes preferences to provide mapping options for DicomSCPInstances to DicomSCP actual.
     */
    @Override
    protected void postProcessPreferences() {
        final Map<String, DicomSCPInstance> value = getMapValue(PREF_ID);
        try {
            cacheDicomScpInstances(value.values());
        } catch (DICOMReceiverWithDuplicateTitleAndPortException | DicomNetworkException | UnknownDicomHelperInstanceException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, e);
        }
    }

    protected Provider<UserI> getUserProvider() {
        return _provider;
    }

    protected DicomObjectIdentifier<XnatProjectdata> getIdentifier(final String identifier) throws UnknownDicomHelperInstanceException {
        final DicomObjectIdentifier bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomObjectIdentifier.class) : _context.getBean(identifier, DicomObjectIdentifier.class);
        if (bean == null) {
            throw new UnknownDicomHelperInstanceException(identifier, DicomObjectIdentifier.class);
        }
        //noinspection unchecked
        return (DicomObjectIdentifier<XnatProjectdata>) bean;
    }

    protected DicomFileNamer getDicomFileNamer(final String identifier) throws UnknownDicomHelperInstanceException {
        final DicomFileNamer bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomFileNamer.class) : _context.getBean(identifier, DicomFileNamer.class);
        if (bean == null) {
            throw new UnknownDicomHelperInstanceException(identifier, DicomFileNamer.class);
        }
        return bean;
    }

    private List<DicomSCPInstance> cacheDicomScpInstances(final Collection<DicomSCPInstance> instances) throws DICOMReceiverWithDuplicateTitleAndPortException, DicomNetworkException, UnknownDicomHelperInstanceException {
        final List<DicomSCPInstance>                                cached       = new ArrayList<>();
        final List<DICOMReceiverWithDuplicateTitleAndPortException> badInstances = new ArrayList<>();
        final List<Integer>                                         ids          = new ArrayList<>();
        for (final DicomSCPInstance instance : instances) {
            try {
                _template.update(CREATE_OR_UPDATE_INSTANCE, instance.toMap());
                final DicomSCPInstance retrieved = getDicomSCPInstance(instance.getAeTitle(), instance.getPort());
                ids.add(retrieved.getId());
                cached.add(retrieved);
            } catch (DuplicateKeyException e) {
                badInstances.add(new DICOMReceiverWithDuplicateTitleAndPortException(instance));
            }
        }
        if (!badInstances.isEmpty()) {
            throw new DICOMReceiverWithDuplicateTitleAndPortException(badInstances);
        }

        updateDicomSCPs(getPortsForIds(ids));
        return cached;
    }

    private void uncacheDicomScpInstances(final Integer... ids) throws DICOMReceiverWithDuplicateTitleAndPortException, DicomNetworkException, UnknownDicomHelperInstanceException {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("ids", Arrays.asList(ids));
        final List<Integer>         ports      = getPortsForIds(Arrays.asList(ids));
        final int                   updated    = _template.update(DELETE_INSTANCES_BY_ID, parameters);
        if (updated > 0) {
            updateDicomSCPs(ports);
        }
    }

    private void toggleEnabled(final boolean enabled, final Collection<Integer> ids) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        final int updated = _template.update(ENABLE_OR_DISABLE_INSTANCES_BY_ID, new HashMap<String, Object>() {{
            put("enabled", enabled);
            put("ids", ids);
        }});
        if (updated > 0) {
            updateDicomSCPs(getPortsForIds(ids));
        }
    }

    private List<Integer> getPortsForIds(final Collection<Integer> ids) {
        return _template.queryForList(GET_PORTS_BY_IDS, new MapSqlParameterSource("ids", ids), Integer.class);
    }

    private Collection<Integer> getPortsWithEnabledInstances() {
        return _template.queryForList(GET_PORTS_FOR_ENABLED_INSTANCES, Collections.<String, Object>emptyMap(), Integer.class);
    }

    private List<String> updateDicomSCPs(final Collection<Integer> ports) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        final List<String> aggregated = new ArrayList<>();
        for (final int port : ports) {
            final DicomSCP dicomSCP;
            if (_dicomSCPs.containsKey(port)) {
                dicomSCP = _dicomSCPs.get(port);
                if (dicomSCP.isStarted()) {
                    dicomSCP.stop();
                }
            } else {
                dicomSCP = DicomSCP.create(this, Executors.newCachedThreadPool(), port);
                _dicomSCPs.put(port, dicomSCP);
            }
            final List<String> started = dicomSCP.start();
            _log.info("Started {} listeners on port {}: {}", started.size(), port, Joiner.on(", ").join(started));
            aggregated.addAll(Lists.transform(started, new Function<String, String>() {
                @NotNull
                @Override
                public String apply(@Nullable final String aeTitle) {
                    return aeTitle + ":" + port;
                }
            }));
        }
        return aggregated;
    }

    private static class IdentifiersToMapFunction implements Function<String, String> {
        IdentifiersToMapFunction(final Map<String, DicomObjectIdentifier<XnatProjectdata>> identifiers) {
            _identifiers = identifiers;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String beanId) {
            if (StringUtils.isBlank(beanId)) {
                return null;
            }
            final DicomObjectIdentifier<XnatProjectdata> identifier = _identifiers.get(beanId);
            if (identifier instanceof CompositeDicomObjectIdentifier) {
                return ((CompositeDicomObjectIdentifier) identifier).getName();
            }
            return beanId;
        }

        private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _identifiers;
    }

    private static final String ENABLE_DICOM_RECEIVER_PREF = "enableDicomReceiver";
    private static final String DSCPM_DB_URL               = "jdbc:h2:mem:" + PREF_ID;

    // Read queries: no changes to DicomSCPs required.
    private static final String GET_INSTANCE_BY_ID                    = "SELECT * FROM dicom_scp_instance WHERE id = :id";
    private static final String GET_ENABLED_INSTANCES_BY_PORT         = "SELECT * FROM dicom_scp_instance WHERE enabled = :enabled AND port = :port";
    private static final String DOES_INSTANCE_ID_EXIST                = "SELECT EXISTS(" + GET_INSTANCE_BY_ID + ")";
    private static final String GET_INSTANCE_BY_AE_TITLE_AND_PORT     = "SELECT * FROM dicom_scp_instance WHERE ae_title = :aeTitle AND port = :port";
    private static final String DOES_INSTANCE_AE_TITLE_AND_PORT_EXIST = "SELECT EXISTS(" + GET_INSTANCE_BY_AE_TITLE_AND_PORT + ")";
    private static final String GET_PORTS_BY_IDS                      = "SELECT DISTINCT port FROM dicom_scp_instance WHERE id IN (:ids)";
    private static final String GET_PORTS_FOR_ENABLED_INSTANCES       = "SELECT DISTINCT port FROM dicom_scp_instance WHERE enabled = TRUE";

    // Update queries: updating DicomSCPs required.
    private static final String CREATE_OR_UPDATE_INSTANCE         = "MERGE INTO dicom_scp_instance (id, ae_title, PORT, identifier, file_namer, enabled) KEY(id) VALUES(:id, :aeTitle, :port, :identifier, :fileNamer, :enabled)";
    private static final String ENABLE_OR_DISABLE_INSTANCES_BY_ID = "UPDATE dicom_scp_instance SET enabled = :enabled WHERE id IN (:ids)";
    private static final String DELETE_INSTANCES_BY_ID            = "DELETE FROM dicom_scp_instance WHERE id IN (:ids)";

    private static final Logger _log = LoggerFactory.getLogger(DicomSCPManager.class);

    private final PreferenceHandlerMethod _handlerProxy = new AbstractScopedSiteConfigPreferenceHandlerMethod(ENABLE_DICOM_RECEIVER_PREF) {
        @Override
        public void handlePreference(final String preference, final String value) {
            _isEnableDicomReceiver = Boolean.parseBoolean(value);
        }
    };

    private static final RowMapper<DicomSCPInstance> DICOM_SCP_INSTANCE_ROW_MAPPER = new RowMapper<DicomSCPInstance>() {
        @Override
        public DicomSCPInstance mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
            return new DicomSCPInstance(resultSet.getInt("id"),
                                        resultSet.getString("ae_title"),
                                        resultSet.getInt("port"),
                                        resultSet.getString("identifier"),
                                        resultSet.getString("file_namer"),
                                        resultSet.getBoolean("enabled"));
        }
    };

    private final XnatUserProvider           _provider;
    private final ApplicationContext         _context;
    private final String                     _primaryDicomObjectIdentifierBeanId;
    private final Set<String>                _dicomObjectIdentifierBeanIds;
    private final IdentifiersToMapFunction   _identifiersToMapFunction;
    private final EmbeddedDatabase           _database;
    private final NamedParameterJdbcTemplate _template;

    private boolean _isEnableDicomReceiver;

    private final Map<Integer, DicomSCP>                              _dicomSCPs              = new HashMap<>();
    private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _dicomObjectIdentifiers = new HashMap<>();
}
