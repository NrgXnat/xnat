/*
 * web: org.nrg.dcm.scp.DicomSCPManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.scp;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.id.*;
import org.nrg.dcm.scp.daos.DicomSCPInstanceService;
import org.nrg.dcm.scp.exceptions.*;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.event.listeners.methods.AbstractXnatPreferenceHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * DicomSCPManager
 */
@Service
@Slf4j
public class DicomSCPManager extends AbstractXnatPreferenceHandlerMethod {
    private final ApplicationContext _context;
    private final DicomSCPStore _dicomSCPStore;
    private final DicomSCPInstanceService _dicomSCPInstanceService;
    private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _dicomObjectIdentifierMap;
    private final String _primaryDicomObjectIdentifierBeanId;
    private final Set<String> _dicomObjectIdentifierBeanIds;
    private boolean _isEnableDicomReceiver;

    private static final Pattern AE_TITLE_PATTERN = Pattern.compile("(?=[^\\\\]*[^\\s\\\\]+$)(?=^[^\\s\\\\]+[^\\\\]*)[ -~]{1,16}");
    private static final String ENABLE_DICOM_RECEIVER_PREFERENCE = "enableDicomReceiver";

    @Autowired
    public DicomSCPManager(final ExecutorService executorService,
                           final DicomSCPInstanceService dicomSCPInstanceService,
                           final XnatUserProvider receivedFileUserProvider,
                           final ApplicationContext context,
                           final SiteConfigPreferences siteConfigPreferences,
                           final DicomObjectIdentifier<XnatProjectdata> primaryDicomObjectIdentifier,
                           final Map<String, DicomObjectIdentifier<XnatProjectdata>> dicomObjectIdentifiers) {
        super(receivedFileUserProvider, ENABLE_DICOM_RECEIVER_PREFERENCE);
        _dicomSCPInstanceService = dicomSCPInstanceService;
        _context = context;

        String primaryBeanId = null;

        _dicomObjectIdentifierMap = new HashMap<>();
        final List<String> sortedDicomObjectIdentifierBeanIds = new ArrayList<>();
        for (final String beanId : dicomObjectIdentifiers.keySet()) {
            final DicomObjectIdentifier<XnatProjectdata> identifier = dicomObjectIdentifiers.get(beanId);
            _dicomObjectIdentifierMap.put(beanId, identifier);
            if (identifier == primaryDicomObjectIdentifier) {
                primaryBeanId = beanId;
            } else {
                sortedDicomObjectIdentifierBeanIds.add(beanId);
                _dicomObjectIdentifierMap.put(beanId, identifier);
            }
        }

        Collections.sort(sortedDicomObjectIdentifierBeanIds);
        if (StringUtils.isNotBlank(primaryBeanId)) {
            _primaryDicomObjectIdentifierBeanId = primaryBeanId;
            sortedDicomObjectIdentifierBeanIds.add(0, _primaryDicomObjectIdentifierBeanId);
        } else {
            _primaryDicomObjectIdentifierBeanId = sortedDicomObjectIdentifierBeanIds.get(0);
        }

        _dicomObjectIdentifierBeanIds = sortedDicomObjectIdentifierBeanIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        _isEnableDicomReceiver = siteConfigPreferences.isEnableDicomReceiver();

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
    }

    @Transactional
    public Map<String, DicomSCPInstance> getDicomSCPInstances() {
        return _dicomSCPInstanceService.findAll().stream()
                .collect(Collectors.toMap(ds -> String.valueOf(ds.getId()), Function.identity()));
    }

    @Transactional
    public List<DicomSCPInstance> getDicomSCPInstancesList() {
        return _dicomSCPInstanceService.findAll();
    }

    /**
     * Sets the submitted {@link DicomSCPInstance DICOM SCP instance} definition. If the {@link DicomSCPInstance#getId()
     * instance ID} matches an existing DICOM SCP instance, that instance will be updated. If not, the {@link NotFoundException}
     * is thrown.
     *
     * @param instance The instance to be set.
     * @throws NotFoundException                               When an instance with the same ID does not already exist.
     * @throws DICOMReceiverWithDuplicateTitleAndPortException When the new instance is enabled and there's
     *                                                         already an enabled instance with the same AE title
     *                                                         and port.
     */
    @Transactional
    public DicomSCPInstance updateDicomSCPInstance(final DicomSCPInstance instance) throws NotFoundException, IOException {
        if (hasDicomSCPInstance(instance.getId())) {
            _dicomSCPInstanceService.saveOrUpdate(instance);
            return instance;
        }
        throw new NotFoundException("Could not find DICOM SCP instance with ID " + instance.getId());
    }

    /**
     * Sets the submitted {@link DicomSCPInstance DICOM SCP instance} definition. If the {@link DicomSCPInstance#getId()
     * instance ID} matches an existing DICOM SCP instance, that instance will be updated.
     *
     * @param instance The instance to be set.
     * @throws DICOMReceiverWithDuplicateTitleAndPortException When the new instance is enabled and there's
     *                                                         already an enabled instance with the same AE title
     *                                                         and port.
     */
    @Transactional
    public DicomSCPInstance saveDicomSCPInstance(final DicomSCPInstance instance) throws DICOMReceiverWithDuplicatePropertiesException, DicomNetworkException, UnknownDicomHelperInstanceException, DicomScpInvalidWhitelistedItemException, DicomScpInvalidAeTitleException, DicomScpInvalidRoutingExpressionException {
        final long instanceId = instance.getId();
        log.debug("Saving DicomScpInstance {}: {}", instanceId, instance);

        final boolean isNewInstance = !_dicomSCPInstanceService.exists("id", instance.getId());

        // If existing and submitted are the same, then no change.
        if (!isNewInstance) {
            DicomSCPInstance existing = _dicomSCPInstanceService.findById(instanceId);
            if (existing.equals(instance)) {
                log.trace("No change found for existing DicomSCPInstance {}, just returning", instanceId);
                return instance;
            }
        }

        final String aeTitle = instance.getAeTitle();
        final int port = instance.getPort();

        if (!AE_TITLE_PATTERN.matcher(aeTitle).matches()) {
            throw new DicomScpInvalidAeTitleException("Invalid AE-title: " + aeTitle);
        }

        try {
            final DicomSCPInstance instanceWithAeTitleAndPort = getDicomSCPInstance(aeTitle, port);
            if (instanceWithAeTitleAndPort.getId() != instanceId) {
                throw new DICOMReceiverWithDuplicateTitleAndPortException(aeTitle, port);
            }
        } catch (NotFoundException e) {
            // This is okay: it doesn't duplicate AE title and port.
        }

        final Set<String> whitelist = instance.getWhitelist().stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());
        for (String item : whitelist) {
            final List<String> whitelistedItem = Arrays.asList(item.split("@"));
            if (whitelistedItem.size() == 2) {
                final String whitelistedAe = whitelistedItem.get(0);
                final String whitelistedIp = whitelistedItem.get(1);
                try {
                    new IpAddressMatcher(whitelistedIp);
                } catch (IllegalArgumentException e) {
                    throw new DicomScpInvalidWhitelistedItemException("Invalid Ip Address in whitelist: " + whitelistedIp, e);
                }
                if (!AE_TITLE_PATTERN.matcher(whitelistedAe).matches()) {
                    throw new DicomScpInvalidWhitelistedItemException("Invalid AE-title in whitelist: " + whitelistedAe);
                }
            } else if (whitelistedItem.size() == 1) {
                try {
                    new IpAddressMatcher(item);
                } catch (IllegalArgumentException e) {
                    if (!AE_TITLE_PATTERN.matcher(item).matches()) {
                        throw new DicomScpInvalidWhitelistedItemException("Invalid item in whitelist: " + item);
                    }
                }
            } else {
                throw new DicomScpInvalidWhitelistedItemException("Invalid item in whitelist: " + whitelistedItem);
            }
        }
        instance.setWhitelist(new ArrayList<>(whitelist));

        String routingExpressionErrors = _dicomSCPInstanceService.validate(instance);
        if (StringUtils.isNotEmpty(routingExpressionErrors)) {
            throw new DicomScpInvalidRoutingExpressionException(routingExpressionErrors);
        }

        _dicomSCPInstanceService.saveOrUpdate(instance);
        log.debug("{} DicomSCPInstance {}: {}", isNewInstance ? "Saved new" : "Updated existing", instance.getId(), instance);

        if (isNewInstance && !instance.isEnabled()) {
            log.debug("Created new DicomSCPInstance {}, but it's not enabled, so I'm not cycling its port {}", instance.getId(), instance.getPort());
        } else {
            log.debug("{} DicomSCPInstance {}, cycling port {}", isNewInstance ? "Created" : "Modified", instance.getId(), instance.getPort());
            cycleDicomSCPPorts(Collections.singleton(instance.getPort()));
        }

        return instance;
    }

    @Transactional
    public void deleteDicomSCPInstances(final Set<Integer> ids) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException {
        log.debug("Got request to delete {} DicomSCPInstances: {}", ids.size(), StringUtils.join(ids, ", "));
        final Map<String, DicomSCPInstance> instances = getDicomSCPInstances();
        final Set<String> stringIds = ids.stream().map(id -> Integer.toString(id)).collect(Collectors.toSet());
        final Set<String> invalidIds = stringIds.stream().filter(stringId -> !instances.containsKey(stringId)).collect(Collectors.toSet());
        if (!invalidIds.isEmpty()) {
            throw new NotFoundException("Got request to delete DICOM SCP instances with ID(s): " + String.join(", ", stringIds) + ". The following IDs are invalid identifiers: " + String.join(", ", invalidIds));
        }
        final Set<Integer> ports = new HashSet<>();
        for (final int id : ids) {
            final DicomSCPInstance instance = instances.remove(Integer.toString(id));
            ports.add(instance.getPort());
            _dicomSCPInstanceService.delete(instance);
            log.debug("Removed instance {}: {}", id, instance);
        }

        log.debug("Deleted {} DICOM SCP instances affecting {} ports, so cycling each of those: {}", ids.size(), ports.size(), StringUtils.join(ports, ", "));
        cycleDicomSCPPorts(ports);
    }

    @Transactional
    public void deleteDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException {
        try {
            deleteDicomSCPInstances(Collections.singleton(id));
        } catch (NotFoundException e) {
            throw new NotFoundException("Could not find DICOM SCP instance with ID " + id);
        }
    }

    /**
     * Indicates whether a {@link DicomSCPInstance DICOM SCP instance} with the indicated ID exists.
     *
     * @param id The ID of the DICOM SCP instance to check.
     * @return Returns true if the instance exists, false otherwise.
     */
    @Transactional
    public boolean hasDicomSCPInstance(final long id) {
        return _dicomSCPInstanceService.exists("id", id);
    }

    @Nonnull
    @Transactional
    public DicomSCPInstance getDicomSCPInstance(final long id) throws NotFoundException {
        DicomSCPInstance entity = _dicomSCPInstanceService.findById(id);
        if (entity == null) {
            throw new NotFoundException("DicomSCPInstance(id: " + id + ")");
        }
        // TODO: Huh. entity returned by findById is a proxy object. Dunno why.
        // Do a useless read of a property so lazy loading happens in the context of this session.
        String title = entity.getAeTitle();
        return entity;
    }

    @Nonnull
    @Transactional
    public DicomSCPInstance getDicomSCPInstance(final String aeTitle, final int port) throws NotFoundException {
        return _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port)
                .orElseThrow(() -> new NotFoundException(String.format("No such instance with aeTitle '%s' and port %d", aeTitle, port)));
    }

    @Transactional
    public List<DicomSCPInstance> getEnabledDicomSCPInstancesByPort(final int port) {
        return _dicomSCPInstanceService.findAllEnabled().stream().filter(ds -> ds.getPort() == port).collect(Collectors.toList());
    }

    @Transactional
    public DicomSCPInstance enableDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException, IOException {
        log.debug("Enabling DicomSCPInstance {}", id);
        return toggleEnabled(true, id);
    }

    @Transactional
    public DicomSCPInstance disableDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException, IOException {
        log.debug("Disabling DicomSCPInstance {}", id);
        return toggleEnabled(false, id);
    }

    /**
     * This starts all configured DICOM SCP instances, as long as the {@link SiteConfigPreferences#isEnableDicomReceiver()}
     * preference setting is set to true.
     */
    @Transactional
    public List<Triple<String, Integer, Boolean>> start() throws UnknownDicomHelperInstanceException, DicomNetworkException {
        return _isEnableDicomReceiver ? cycleDicomSCPPorts(_dicomSCPInstanceService.getPortsWithEnabledInstances()) : Collections.emptyList();
    }

    public List<Triple<String, Integer, Boolean>> stop() throws DicomNetworkException, UnknownDicomHelperInstanceException {
        return _dicomSCPStore.stopAll();
    }

    /**
     * isCustomProcessing
     * Cache this because CStore asks this a lot.
     *
     * @param aeTitle
     * @param port
     * @return false if SCP instance is unknown
     */
    public boolean isCustomProcessing(String aeTitle, int port) {
        Optional<DicomSCPInstance> instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port);
        return instance.map(DicomSCPInstance::isCustomProcessing).orElse(false);
    }

    /**
     * isDirectArchive
     * Cache this because CStore asks this a lot.
     *
     * @param aeTitle
     * @param port
     * @return false if SCP instance is unknown
     */
    public boolean isDirectArchive(String aeTitle, int port) {
        Optional<DicomSCPInstance> instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port);
        return instance.map(DicomSCPInstance::isDirectArchive).orElse(false);
    }

    /**
     * isAnonymizationEnabled
     * Cache this because CStore asks this a lot.
     *
     * @param aeTitle
     * @param port
     * @return false if SCP instance is unknown
     */
    public boolean isAnonymizationEnabled(String aeTitle, int port) {
        Optional<DicomSCPInstance> instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port);
        return instance.map(DicomSCPInstance::isAnonymizationEnabled).orElse(false);
    }

    // for API
    public Map<String, String> getDicomObjectIdentifierBeans() {
        return _dicomObjectIdentifierBeanIds.stream()
                .filter(_dicomObjectIdentifierMap::containsKey)
                .collect(Collectors.toMap(java.util.function.Function.identity(),
                        beanId -> _dicomObjectIdentifierMap.get(beanId) instanceof CompositeDicomObjectIdentifier ? ((CompositeDicomObjectIdentifier) _dicomObjectIdentifierMap.get(beanId)).getName() : beanId));
    }

    public Map<String, DicomObjectIdentifier<XnatProjectdata>> getDicomObjectIdentifiers() {
        return _dicomObjectIdentifierMap;
    }

    // for API
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

    /**
     * getDicomObjectIdentifier
     *
     * @param aeTitle
     * @param port
     * @return a DOI for the specified instance or null if the instance does not exist.
     */
    @Nullable
    public DicomObjectIdentifier<XnatProjectdata> getDicomObjectIdentifier(final String aeTitle, int port) {
        DicomSCPInstance instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown DicomSCPInstances with aeTitle '%s' and port %d", aeTitle, port)));
        DicomObjectIdentifier<XnatProjectdata> doi = _dicomObjectIdentifierMap.get(instance.getIdentifier());
        if (doi instanceof AeTitleAndPortAware) {
            AeTitleAndPortAware aware = (AeTitleAndPortAware) doi;
            aware.setAeTitle(aeTitle);
            aware.setPort(port);
        }
        return doi;
    }


    // for API
    public void resetDicomObjectIdentifier() {
        final DicomObjectIdentifier<XnatProjectdata> objectIdentifier = getDefaultDicomObjectIdentifier();
        if (objectIdentifier instanceof CompositeDicomObjectIdentifier) {
            ((CompositeDicomObjectIdentifier) objectIdentifier).getProjectIdentifier().reset();
        }
    }

    // for API
    public void resetDicomObjectIdentifier(final String beanId) {
        final DicomObjectIdentifier<XnatProjectdata> identifier = getDicomObjectIdentifier(beanId);
        if (identifier instanceof CompositeDicomObjectIdentifier) {
            ((CompositeDicomObjectIdentifier) identifier).getProjectIdentifier().reset();
        }
    }

    // for API
    public void resetDicomObjectIdentifierBeans() {
        for (final DicomObjectIdentifier<XnatProjectdata> identifier : getDicomObjectIdentifiers().values()) {
            if (identifier instanceof CompositeDicomObjectIdentifier) {
                ((CompositeDicomObjectIdentifier) identifier).getProjectIdentifier().reset();
            }
        }
    }

    @Transactional
    public Set<Integer> getPortsWithEnabledInstances() {
        return _dicomSCPInstanceService.findAllEnabled().stream().map(DicomSCPInstance::getPort).collect(Collectors.toSet());
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

    protected XnatUserProvider getUserProvider() {
        return super.getUserProvider();
    }

    @Nonnull
    private DicomSCPInstance toggleEnabled(final boolean enabled, final int id) throws NotFoundException, IOException {
        log.debug("Handling request to {} instance {}", enabled ? "enable" : "disable", id);
        final DicomSCPInstance instance = getDicomSCPInstance(id);
        if (enabled == instance.isEnabled()) {
            return instance;
        }
        try {
            instance.setEnabled(enabled);
            if (enabled) {
                _dicomSCPStore.start(instance.getPort());
            } else {
                _dicomSCPStore.stop(instance.getPort());
            }
            return saveDicomSCPInstance(instance);
        } catch (NrgServiceException e) {
            // Shouldn't happen: we just retrieved it and enabled doesn't count towards duplicate properties.
            return instance;
        }
    }

    private List<Triple<String, Integer, Boolean>> cycleDicomSCPPorts(final Set<Integer> updated) throws DicomNetworkException, UnknownDicomHelperInstanceException {
        log.debug("I'm going to cycle {} ports that have been added or updated: {}", updated.size(), updated);
        return _dicomSCPStore.cycle(updated);
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        _isEnableDicomReceiver = Boolean.parseBoolean(value);
        try {
            if (_isEnableDicomReceiver) {
                start();
            } else {
                stop();
            }
        } catch (UnknownDicomHelperInstanceException | DicomNetworkException e) {
            log.error("Error globally {} all Dicom SCP Receivers.", _isEnableDicomReceiver ? "starting" : "stopping", e);
        }
    }
}
