/*
 * web: org.nrg.dcm.scp.DicomSCPManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import static org.nrg.dcm.scp.DicomSCPManager.TOOL_ID;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.inject.Provider;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.h2.Driver;
import org.json.JSONObject;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.id.CompositeDicomObjectIdentifier;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicatePropertiesException;
import org.nrg.dcm.scp.exceptions.DICOMReceiverWithDuplicateTitleAndPortException;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.prefs.annotations.NrgPreference;
import org.nrg.prefs.annotations.NrgPreferenceBean;
import org.nrg.prefs.events.PreferenceHandlerMethod;
import org.nrg.prefs.exceptions.InvalidPreferenceName;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.EventTriggeringAbstractPreferenceBean;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xdat.services.DataTypeAwareEventService;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.event.listeners.methods.AbstractXnatPreferenceHandlerMethod;
import org.nrg.xnat.processor.importer.ProcessorGradualDicomImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@NrgPreferenceBean(toolId = TOOL_ID, toolName = "DICOM SCP Manager", description = "Manages configuration of the various DICOM SCP endpoints on the XNAT system.")
public class DicomSCPManager extends EventTriggeringAbstractPreferenceBean implements PreferenceHandlerMethod {
    public static final String TOOL_ID = "dicomScpManager";

    @Autowired
    public DicomSCPManager(final ExecutorService executorService, final NrgPreferenceService preferenceService, final DataTypeAwareEventService eventService, final XnatUserProvider receivedFileUserProvider, final ApplicationContext context, final SiteConfigPreferences siteConfigPreferences, final ProcessorGradualDicomImporter importer, final DicomObjectIdentifier<XnatProjectdata> primaryDicomObjectIdentifier, final Map<String, DicomObjectIdentifier<XnatProjectdata>> dicomObjectIdentifiers) {
        super(preferenceService, eventService);

        _provider = receivedFileUserProvider;
        _context = context;

        _isEnableDicomReceiver = siteConfigPreferences.isEnableDicomReceiver();

        _importer = importer;

        String primaryBeanId = null;

        final List<String> sortedDicomObjectIdentifierBeanIds = new ArrayList<>();
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
        _template = new NamedParameterJdbcTemplate(getH2DataSource());
        _dicomSCPStore = new DicomSCPStore(executorService, this);
    }

    @PreDestroy
    public void shutdown() {
        log.debug("Handling pre-destroy actions, shutting down DICOM SCP receivers.");
        try {
            stop();
        } catch (DicomNetworkException e) {
            log.error("A DICOM network error occurred while trying to shut down", e);
        } catch (UnknownDicomHelperInstanceException e) {
            log.error("An unknown DICOM helper error occurred while trying to shut down", e);
        }
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

    public List<DicomSCPInstance> getDicomSCPInstancesList() {
        return _template.query(GET_ALL, DICOM_SCP_INSTANCE_ROW_MAPPER);
    }

    /**
     * Sets the full map of {@link DicomSCPInstance DICOM SCP instance} definitions.
     *
     * @param instances The DICOM SCP definitions to save.
     */
    public void setDicomSCPInstances(final Map<String, DicomSCPInstance> instances) throws DICOMReceiverWithDuplicatePropertiesException {
        log.debug("Setting DICOM SCP instances with IDs: {}", StringUtils.join(instances.keySet(), ", "));
        try {
            // Have to cache these first, since the database caching does double duty with generating IDs for any new
            // instances that are in the map.
            // Now use a new map from the cached instances so that the new IDs will be persisted.
            setMapValue(PREF_ID, instances);
            final List<DicomSCPInstance> cached = cacheInstances(instances);
            log.info("Cached {} DICOM SCP instances", cached.size());
        } catch (InvalidPreferenceName invalidPreferenceName) {
            log.error("Invalid preference name '{}': something is very wrong here.", PREF_ID, invalidPreferenceName);
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
     */
    public DicomSCPInstance saveDicomSCPInstance(final DicomSCPInstance instance) throws DICOMReceiverWithDuplicatePropertiesException, DicomNetworkException, UnknownDicomHelperInstanceException {
        final int instanceId = instance.getId();
        log.debug("Saving DicomScpInstance {}: {}", instanceId, instance);

        try {
            final DicomSCPInstance existingScp = getDicomSCPInstance(instanceId);

            // If existing and submitted are the same, then no change.
            if (existingScp.equals(instance)) {
                log.trace("No change found for existing DicomSCPInstance {}, just returning", instanceId);
                return instance;
            }
        } catch (NotFoundException e) {
            // This is okay: it's a new instance.
        }

        final String aeTitle = instance.getAeTitle();
        final int    port    = instance.getPort();

        try {
            final DicomSCPInstance instanceWithAeTitleAndPort = getDicomSCPInstance(aeTitle, port);
            if (instanceWithAeTitleAndPort.getId() != instanceId) {
                throw new DICOMReceiverWithDuplicateTitleAndPortException(aeTitle, port);
            }
        } catch (NotFoundException e) {
            // This is okay: it doesn't duplicate AE title and port.
        }

        final DicomSCPInstance persisted = instanceId == 0 ? cacheInstance(instance) : instance;
        log.debug("{} DicomSCPInstance {}: {}", instanceId == 0 ? "Saved new" : "Updated existing", persisted.getId(), persisted);

        try {
            set(new JSONObject(persisted.toMap()).toString(), "dicomSCPInstances:" + persisted.getId());
        } catch (InvalidPreferenceName invalidPreferenceName) {
            log.error("Invalid preference name '{}': something is very wrong here.", PREF_ID, invalidPreferenceName);
        }

        if (persisted.isEnabled()) {
            log.debug("DicomSCPInstance {} is enabled, cycling port {}", persisted.getId(), persisted.getPort());
            cycleDicomSCPPorts(Collections.singleton(persisted.getPort()));
        }

        return persisted;
    }

    public void deleteDicomSCPInstances(final Set<Integer> ids) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        log.debug("Got request to delete {} DicomSCPInstances: {}", ids.size(), StringUtils.join(ids, ", "));
        final Map<String, DicomSCPInstance> instances = getDicomSCPInstances();
        for (final int id : ids) {
            final DicomSCPInstance instance = instances.remove(Integer.toString(id));
            log.debug("Removed instance {}: {}", id, instance);
        }
        try {
            setDicomSCPInstances(instances);
        } catch (DICOMReceiverWithDuplicatePropertiesException ignored) {
            // Shouldn't happen: dupes would have been caught on previous, we're just deleting.
        }
        uncacheDicomScpInstances(ids);
    }

    public void deleteDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        deleteDicomSCPInstances(Collections.singleton(id));
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

    @Nonnull
    public DicomSCPInstance getDicomSCPInstance(final int id) throws NotFoundException {
        try {
            return _template.queryForObject(GET_INSTANCE_BY_ID, new MapSqlParameterSource("id", id), DICOM_SCP_INSTANCE_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("DicomSCPInstance(id: " + id + ")");
        }
    }

    @Nonnull
    public DicomSCPInstance getDicomSCPInstance(final String aeTitle, final int port) throws NotFoundException {
        try {
            return _template.queryForObject(GET_INSTANCE_BY_AE_TITLE_AND_PORT, new MapSqlParameterSource("aeTitle", aeTitle).addValue("port", port), DICOM_SCP_INSTANCE_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("DicomSCPInstance(aeTitle: " + aeTitle + ", port: " + port + ")");
        }
    }

    public List<DicomSCPInstance> getEnabledDicomSCPInstancesByPort(final int port) {
        try {
            return _template.query(GET_ENABLED_INSTANCES_BY_PORT, new MapSqlParameterSource("enabled", true).addValue("port", port), DICOM_SCP_INSTANCE_ROW_MAPPER);
        } catch (EmptyResultDataAccessException e) {
            return Collections.emptyList();
        }
    }

    public DicomSCPInstance enableDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException {
        log.debug("Enabling DicomSCPInstance {}", id);
        return toggleEnabled(true, id);
    }

    public DicomSCPInstance disableDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException {
        log.debug("Disabling DicomSCPInstance {}", id);
        return toggleEnabled(false, id);
    }

    /**
     * This starts all configured DICOM SCP instances, as long as the {@link SiteConfigPreferences#isEnableDicomReceiver()}
     * preference setting is set to true.
     */
    public List<Triple<String, Integer, Boolean>> start() throws UnknownDicomHelperInstanceException, DicomNetworkException {
        return _isEnableDicomReceiver ? cycleDicomSCPPorts(getPortsWithEnabledInstances()) : Collections.<Triple<String, Integer, Boolean>>emptyList();
    }

    public List<Triple<String, Integer, Boolean>> stop() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return _dicomSCPStore.stopAll();
    }

    public Map<String, String> getDicomObjectIdentifierBeans() {
        return Maps.asMap(_dicomObjectIdentifierBeanIds, _identifiersToMapFunction);
    }

    public Map<String, DicomObjectIdentifier<XnatProjectdata>> getDicomObjectIdentifiers() {
        return _dicomObjectIdentifiers;
    }

    @Nullable
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

    public ProcessorGradualDicomImporter getImporter() {
        return _importer;
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
        for (final DicomObjectIdentifier<XnatProjectdata> identifier : getDicomObjectIdentifiers().values()) {
            if (identifier instanceof CompositeDicomObjectIdentifier) {
                ((CompositeDicomObjectIdentifier) identifier).getProjectIdentifier().reset();
            }
        }
    }

    public Set<Integer> getPortsWithEnabledInstances() {
        return new HashSet<>(_template.queryForList(GET_PORTS_FOR_ENABLED_INSTANCES, EmptySqlParameterSource.INSTANCE, Integer.class));
    }

    /**
     * Post-processes preferences to provide mapping options for DicomSCPInstances to DicomSCP actual.
     */
    @Override
    protected void postProcessPreferences() {
        final Map<String, DicomSCPInstance> instances = getMapValue(PREF_ID);
        try {
            final List<DicomSCPInstance> cached = cacheInstances(instances);
            log.info("Cached {} DICOM SCP instances", cached.size());
        } catch (DICOMReceiverWithDuplicatePropertiesException e) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, e);
        }
    }

    protected Provider<UserI> getUserProvider() {
        return _provider;
    }

    protected DicomObjectIdentifier<XnatProjectdata> getIdentifier(final String identifier) throws UnknownDicomHelperInstanceException {
        //noinspection unchecked
        final DicomObjectIdentifier<XnatProjectdata> bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomObjectIdentifier.class) : _context.getBean(identifier, DicomObjectIdentifier.class);
        if (bean == null) {
            throw new UnknownDicomHelperInstanceException(identifier, DicomObjectIdentifier.class);
        }
        log.debug("Found bean of type {} for DICOM object identifier {}", bean.getClass().getName(), StringUtils.defaultIfBlank(identifier, "default"));
        return bean;
    }

    protected DicomFileNamer getDicomFileNamer(final String identifier) throws UnknownDicomHelperInstanceException {
        final DicomFileNamer bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomFileNamer.class) : _context.getBean(identifier, DicomFileNamer.class);
        if (bean == null) {
            throw new UnknownDicomHelperInstanceException(identifier, DicomFileNamer.class);
        }
        log.debug("Found bean of type {} for DICOM file namer {}", bean.getClass().getName(), StringUtils.defaultIfBlank(identifier, "default"));
        return bean;
    }

    @Nonnull
    private List<DicomSCPInstance> cacheInstances(final Map<String, DicomSCPInstance> instances) throws DICOMReceiverWithDuplicatePropertiesException {
        final List<DICOMReceiverWithDuplicatePropertiesException> badInstances = new ArrayList<>();

        clearCaches();

        final List<DicomSCPInstance> cached = Lists.newArrayList(Iterables.filter(Iterables.transform(instances.values(), new Function<DicomSCPInstance, DicomSCPInstance>() {
            @Override
            public DicomSCPInstance apply(final DicomSCPInstance instance) {
                try {
                    return cacheInstance(instance);
                } catch (DICOMReceiverWithDuplicatePropertiesException e) {
                    badInstances.add(e);
                    return null;
                }
            }
        }), Predicates.<DicomSCPInstance>notNull()));

        if (!badInstances.isEmpty()) {
            log.warn("While caching {} DicomSCPInstances, found {} valid instances but {} bad instances", instances.size(), cached.size(), badInstances.size());
            throw new DICOMReceiverWithDuplicatePropertiesException(badInstances);
        }

        // Could be slightly expensive to do the StringUtils.join() call, so check whether debug is enabled.
        if (log.isDebugEnabled()) {
            log.debug("Cached {} DicomSCPInstances: {}", cached.size(), StringUtils.join(cached, ", "));
        }
        return cached;
    }

    @Nonnull
    private DicomSCPInstance cacheInstance(final DicomSCPInstance instance) throws DICOMReceiverWithDuplicateTitleAndPortException {
        log.debug("{} DICOM SCP instance: {}", instance.getId() == 0 ? "Creating" : "Updating", instance);
        try {
            final Map<String, Object> properties = instance.toMap();
            if (instance.getId() == 0) {
                properties.put("id", null);
            }
            final int              updated   = _template.update(CREATE_OR_UPDATE_INSTANCE, properties);
            final DicomSCPInstance persisted = getDicomSCPInstance(instance.getAeTitle(), instance.getPort());
            log.debug("{} DICOM SCP instance with ID {}, affecting {} row: {}", instance.getId() == 0 ? "Created" : "Updated", persisted.getId(), updated, persisted);
            return persisted;
        } catch (DuplicateKeyException e) {
            log.info("The DICOM SCP instance {} uses an already existing title/port combination: {}:{}", instance.getId(), instance.getAeTitle(), instance.getPort());
            throw new DICOMReceiverWithDuplicateTitleAndPortException(instance);
        } catch (NotFoundException ignored) {
            // Shouldn't happen: we just stored it.
            return instance;
        }
    }

    private void clearCaches() {
        final int deleted = _template.update(DELETE_ALL_INSTANCES, EmptySqlParameterSource.INSTANCE);
        log.trace("Cleared DICOM SCP instances table, removed {} existing instances", deleted);
    }

    private void uncacheDicomScpInstances(final Set<Integer> ids) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        final Set<Integer> ports = getPortsForInstancesWithIds(ids);
        log.debug("Got request to uncache {} instances affecting {} ports", ids.size(), ports.size());

        final int updated = _template.update(DELETE_INSTANCES_BY_ID, new MapSqlParameterSource("ids", ids));
        log.debug("Deleted {} rows from the database for {} instances", updated, ids.size());
        if (updated > 0) {
            log.debug("Deleted {} rows, so cycling {} affected ports: {}", updated, ports.size(), StringUtils.join(ports, ", "));
            cycleDicomSCPPorts(ports);
        }
    }

    @Nonnull
    private DicomSCPInstance toggleEnabled(final boolean enabled, final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException {
        log.debug("Handling request to {} instance {}", enabled ? "enable" : "disable", id);
        final DicomSCPInstance instance = getDicomSCPInstance(id);
        instance.setEnabled(enabled);
        try {
            return saveDicomSCPInstance(instance);
        } catch (DICOMReceiverWithDuplicatePropertiesException e) {
            // Shouldn't happen: we just retrieved it and enabled doesn't count towards duplicate properties.
            return instance;
        } finally {
            cycleDicomSCPPorts(Collections.singleton(instance.getPort()));
        }
    }

    private Set<Integer> getPortsForInstancesWithIds(final Set<Integer> ids) {
        return new HashSet<>(_template.queryForList(GET_PORTS_FOR_INSTANCES_BY_ID, new MapSqlParameterSource("ids", ids), Integer.class));
    }

    private List<Triple<String, Integer, Boolean>> cycleDicomSCPPorts(final Set<Integer> updated) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        log.debug("I'm going to cycle {} ports that have been added or updated: {}", updated.size(), updated);
        return _dicomSCPStore.cycle(updated);
    }

    private static DataSource getH2DataSource() {
        final BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName(Driver.class.getName());
        dataSource.setUrl(DSCPM_DB_URL);
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
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

    private static final String PREF_ID      = "dicomSCPInstances";
    private static final String DSCPM_DB_URL = "jdbc:h2:mem:" + PREF_ID;

    // Read queries: no changes to DicomSCPs required.
    private static final String GET_ALL                           = "SELECT * FROM dicom_scp_instance";
    private static final String GET_INSTANCE_BY_ID                = "SELECT * FROM dicom_scp_instance WHERE id = :id";
    private static final String GET_ENABLED_INSTANCES_BY_PORT     = "SELECT * FROM dicom_scp_instance WHERE enabled = :enabled AND port = :port";
    private static final String DOES_INSTANCE_ID_EXIST            = "SELECT EXISTS(" + GET_INSTANCE_BY_ID + ")";
    private static final String GET_INSTANCE_BY_AE_TITLE_AND_PORT = "SELECT * FROM dicom_scp_instance WHERE ae_title = :aeTitle AND port = :port";
    private static final String GET_PORTS_FOR_ENABLED_INSTANCES   = "SELECT DISTINCT port FROM dicom_scp_instance WHERE enabled = TRUE";

    // Update queries: updating DicomSCPs required.
    private static final String CREATE_OR_UPDATE_INSTANCE     = "MERGE INTO dicom_scp_instance (id, ae_title, PORT, identifier, file_namer, enabled, custom_processing) KEY(id) VALUES(:id, :aeTitle, :port, :identifier, :fileNamer, :enabled, :customProcessing)";
    private static final String GET_PORTS_FOR_INSTANCES_BY_ID = "SELECT DISTINCT port FROM dicom_scp_instances WHERE id IN (:ids)";
    private static final String DELETE_INSTANCES_BY_ID        = "DELETE FROM dicom_scp_instance WHERE id IN (:ids)";
    private static final String DELETE_ALL_INSTANCES          = "DELETE FROM dicom_scp_instance";

    private final PreferenceHandlerMethod _handlerProxy = new AbstractXnatPreferenceHandlerMethod("enableDicomReceiver") {
        @Override
        protected void handlePreferenceImpl(final String preference, final String value) {
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
                                        resultSet.getBoolean("enabled"),
                                        resultSet.getBoolean("custom_processing"));
        }
    };

    private final XnatUserProvider              _provider;
    private final ApplicationContext            _context;
    private final String                        _primaryDicomObjectIdentifierBeanId;
    private final Set<String>                   _dicomObjectIdentifierBeanIds;
    private final IdentifiersToMapFunction      _identifiersToMapFunction;
    private final EmbeddedDatabase              _database;
    private final NamedParameterJdbcTemplate    _template;
    private final ProcessorGradualDicomImporter _importer;
    private final DicomSCPStore                 _dicomSCPStore;

    private boolean _isEnableDicomReceiver;

    private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _dicomObjectIdentifiers = new HashMap<>();
}
