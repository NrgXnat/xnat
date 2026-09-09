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
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.nrg.dcm.DicomFileNamer;
import org.nrg.dcm.id.*;
import org.nrg.dcm.scp.exceptions.*;
import org.nrg.dcm.scp.services.DicomSCPInstanceService;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.security.user.XnatUserProvider;
import org.nrg.xnat.DicomObjectIdentifier;
import org.nrg.xnat.event.EventListener;
import org.nrg.xnat.event.listeners.methods.AbstractXnatPreferenceHandlerMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.bus.Event;
import reactor.fn.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * DicomSCPManager
 */
@Service
@Transactional
@EventListener
@Slf4j
public class DicomSCPManager extends AbstractXnatPreferenceHandlerMethod implements Consumer<Event<DicomSCPEvent>> {
    private final ApplicationContext                                  _context;
    private final Executor                                            _executor;
    private final DicomSCPStore                                       _dicomSCPStore;
    private final DicomSCPInstanceService                             _dicomSCPInstanceService;
    private final Map<String, DicomObjectIdentifier<XnatProjectdata>> _dicomObjectIdentifierMap;
    private final String                                              _primaryDicomObjectIdentifierBeanId;
    private final Set<String>                                         _dicomObjectIdentifierBeanIds;
    private final AtomicBoolean                                       _isEnableDicomReceiver;

    private static final Pattern AE_TITLE_PATTERN                 = Pattern.compile("(?=[^\\\\]*[^\\s\\\\]+$)(?=^[^\\s\\\\]+[^\\\\]*)[ -~]{1,16}");
    private static final String  ENABLE_DICOM_RECEIVER_PREFERENCE = "enableDicomReceiver";

    @Autowired
    public DicomSCPManager(final DicomScpExecutor dicomScpExecutor,
                           final DicomSCPInstanceService dicomSCPInstanceService,
                           final XnatUserProvider receivedFileUserProvider,
                           final ApplicationContext context,
                           final SiteConfigPreferences siteConfigPreferences,
                           final DicomObjectIdentifier<XnatProjectdata> primaryDicomObjectIdentifier,
                           final Map<String, DicomObjectIdentifier<XnatProjectdata>> dicomObjectIdentifiers) {
        super(receivedFileUserProvider, ENABLE_DICOM_RECEIVER_PREFERENCE);
        _executor                = dicomScpExecutor;
        _dicomSCPInstanceService = dicomSCPInstanceService;
        _context                 = context;

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
            sortedDicomObjectIdentifierBeanIds.addFirst(_primaryDicomObjectIdentifierBeanId);
        } else {
            _primaryDicomObjectIdentifierBeanId = sortedDicomObjectIdentifierBeanIds.getFirst();
        }

        _dicomObjectIdentifierBeanIds = sortedDicomObjectIdentifierBeanIds.stream().filter(StringUtils::isNotBlank).collect(Collectors.toSet());

        _isEnableDicomReceiver = new AtomicBoolean(siteConfigPreferences.isEnableDicomReceiver());

        _dicomSCPStore = new DicomSCPStore(this);
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
        } catch (GeneralSecurityException e) {
            log.error("An unknown General Security error occurred while trying to shut down", e);
        }
    }

    @Override
    protected void handlePreferenceImpl(final String preference, final String value) {
        final boolean enabled = Boolean.parseBoolean(value);
        _isEnableDicomReceiver.set(enabled);
        try {
            if (enabled) {
                start();
            } else {
                stop();
            }
        } catch (UnknownDicomHelperInstanceException | DicomNetworkException | GeneralSecurityException e) {
            log.error("Error globally {} all Dicom SCP Receivers.", enabled ? "starting" : "stopping", e);
        }
    }

    @Override
    public void accept(final Event<DicomSCPEvent> event) {
        final DicomSCPEvent data    = event.getData();
        final long          id      = data.getId();
        final String        action  = data.getAction();
        final String        aeTitle = data.getAeTitle();
        final int           port    = data.getPort();

        log.info("Received DICOM SCP event: entity {} at {}:{} operation {}", id, aeTitle, port, action);
        switch (action) {
            case "INSERT":
                handleInsert(id);
                break;
            case "UPDATE":
                handleUpdate(id, aeTitle, port);
                break;
            case "DELETE":
                handleDelete(aeTitle, port);
                break;
        }
    }

    public Executor getExecutor() {
        return _executor;
    }

    public Map<String, DicomSCPInstance> getDicomSCPInstances() {
        return _dicomSCPInstanceService.getAllWithDisabled().stream()
                                       .collect(Collectors.toMap(ds -> String.valueOf(ds.getId()), Function.identity()));
    }

    public List<DicomSCPInstance> getDicomSCPInstancesList() {
        return _dicomSCPInstanceService.getAllWithDisabled();
    }

    /**
     * Sets the submitted {@link DicomSCPInstance DICOM SCP instance} definition. If the {@link DicomSCPInstance#getId()
     * instance ID} matches an existing DICOM SCP instance, that instance will be updated. If not, the {@link NotFoundException}
     * is thrown.
     *
     * @param instance The instance to be set.
     *
     * @throws NotFoundException When an instance with the same ID does not already exist.
     */
    @SuppressWarnings("unused")
    public DicomSCPInstance updateDicomSCPInstance(final DicomSCPInstance instance) throws NotFoundException, DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        if (hasDicomSCPInstance(instance.getId())) {
            _dicomSCPInstanceService.update(instance);
            cycleDicomSCPPort(instance.getPort());
            return instance;
        }
        throw new NotFoundException("Could not find DICOM SCP instance with ID " + instance.getId());
    }

    /**
     * Updates the submitted {@link DicomSCPInstance DICOM SCP instance} definition. If the {@link DicomSCPInstance#getId()
     * instance ID} matches an existing DICOM SCP instance, that instance will be updated. If not, the {@link NotFoundException}
     * <p>
     * Enabled field is ignored while updating unlike the updateDicomSCPInstance method
     *
     * @param instance The instance to be set.
     *
     * @throws NotFoundException When an instance with the same ID does not already exist.
     *
     */
    @SuppressWarnings("unused")
    public DicomSCPInstance update(final DicomSCPInstance instance, final boolean lookup) throws NotFoundException, DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        if (instance == null) {
            throw new NotFoundException("Instance is null");
        }
        if (lookup) {
            DicomSCPInstance foundInstance = findById(instance.getId());
            if (foundInstance == null) {
                throw new NotFoundException("Could not find DICOM SCP instance with ID " + instance.getId());
            }
        }
        log.debug("Updating Dicom SCP Instance {}", instance.getId());
        _dicomSCPInstanceService.update(instance);
        cycleDicomSCPPort(instance.getPort());
        return instance;
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
    public DicomSCPInstance saveDicomSCPInstance(final DicomSCPInstance instance) throws DICOMReceiverWithDuplicatePropertiesException, DicomNetworkException, UnknownDicomHelperInstanceException, DicomScpInvalidWhitelistedItemException, DicomScpInvalidAeTitleException, DicomScpInvalidRoutingExpressionException, DicomScpUnsupportedRoutingExpressionException, DicomScpUnknownDOIException, GeneralSecurityException {
        final long instanceId = instance.getId();
        log.debug("Saving DicomScpInstance {}: {}", instanceId, instance);

        if (!hasKnownDicomObjectIdentifier(instance)) {
            throw new DicomScpUnknownDOIException(instance);
        }
        if (isCustomRoutingRequestedButPrevented(instance)) {
            throw new DicomScpUnsupportedRoutingExpressionException(instance);
        }

        final boolean isNewInstance = !_dicomSCPInstanceService.exists("id", instance.getId());

        // If existing and submitted are the same, then no change.
        if (!isNewInstance) {
            DicomSCPInstance existing = _dicomSCPInstanceService.retrieve(instanceId);
            if (existing.equals(instance)) {
                log.trace("No change found for existing DicomSCPInstance {}, just returning", instanceId);
                return instance;
            }
        }

        final String aeTitle = instance.getAeTitle();
        final int    port    = instance.getPort();

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
                final String whitelistedAe = whitelistedItem.getFirst();
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
            cycleDicomSCPPort(instance.getPort());
        }

        return instance;
    }

    private boolean hasKnownDicomObjectIdentifier(DicomSCPInstance instance) {
        return _dicomObjectIdentifierMap.get(instance.getIdentifier()) != null;
    }

    /**
     * Don't allow routing expressions to be enabled on a DOI that does not support them.
     * True if custom routing expressions are not supported by the instance but custom routing expressions are enabled.
     *
     * @param instance The DICOM SCP instance to test
     *
     * @return true if custom routing configuration should be prevented.
     */
    private boolean isCustomRoutingRequestedButPrevented(DicomSCPInstance instance) {
        return instance.isRoutingExpressionsEnabled() && !_dicomObjectIdentifierMap.get(instance.getIdentifier()).isCustomRoutingSupported();
    }

    public void deleteDicomSCPInstances(final Set<Integer> ids) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException, GeneralSecurityException {
        log.debug("Got request to delete {} DicomSCPInstances: {}", ids.size(), StringUtils.join(ids, ", "));
        final Map<String, DicomSCPInstance> instances  = getDicomSCPInstances();
        final Set<String>                   stringIds  = ids.stream().map(id -> Integer.toString(id)).collect(Collectors.toSet());
        final Set<String>                   invalidIds = stringIds.stream().filter(stringId -> !instances.containsKey(stringId)).collect(Collectors.toSet());
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

    public void deleteDicomSCPInstance(final int id) throws DicomNetworkException, UnknownDicomHelperInstanceException, NotFoundException, GeneralSecurityException {
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
     *
     * @return Returns true if the instance exists, false otherwise.
     */
    public boolean hasDicomSCPInstance(final long id) {
        return _dicomSCPInstanceService.exists("id", id);
    }

    @Nonnull
    public DicomSCPInstance getDicomSCPInstance(final long id) throws NotFoundException {
        DicomSCPInstance entity = _dicomSCPInstanceService.retrieve(id);
        if (entity == null) {
            throw new NotFoundException("DicomSCPInstance(id: " + id + ")");
        }
        // TODO: Huh. entity returned by retrieve is a proxy object. Dunno why.
        // Do a useless read of a property so lazy loading happens in the context of this session.
        log.debug("Retrieved DICOM SCP instance with ID {}, AE title is {} and port is {}", id, entity.getAeTitle(), entity.getPort());
        return entity;
    }

    /**
     * Finds a Dicom SCP Instance by ID
     *
     * @param id - id of the Dicom SCP Instance to be found
     *
     * @return the DicomSCPInstance with the passed id or NotFoundException
     *
     * @throws NotFoundException if no instance with the passed id exists
     */
    public DicomSCPInstance findById(final long id) throws NotFoundException {
        DicomSCPInstance entity = _dicomSCPInstanceService.findById(id);
        if (entity == null) {
            throw new NotFoundException("DicomSCPInstance(id: " + id + ")");
        }
        log.debug("Found DICOM SCP instance with ID {}, AE title is {} and port is {}", id, entity.getAeTitle(), entity.getPort());
        return entity;
    }

    @Nonnull
    public DicomSCPInstance getDicomSCPInstance(final String aeTitle, final int port) throws NotFoundException {
        return _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port)
                                       .orElseThrow(() -> new NotFoundException("No such instance with aeTitle '%s' and port %d".formatted(aeTitle, port)));
    }

    public List<DicomSCPInstance> getEnabledDicomSCPInstancesByPort(final int port) {
        return _dicomSCPInstanceService.findAllByPort(port);
    }

    public DicomSCPInstance enableDicomSCPInstance(final int id) throws NotFoundException {
        log.debug("Enabling DicomSCPInstance {}", id);
        return toggleEnabled(true, id);
    }

    public DicomSCPInstance disableDicomSCPInstance(final int id) throws NotFoundException {
        log.debug("Disabling DicomSCPInstance {}", id);
        return toggleEnabled(false, id);
    }

    /**
     * This starts all configured DICOM SCP instances, as long as the {@link SiteConfigPreferences#isEnableDicomReceiver()}
     * preference setting is set to true.
     */
    public List<Triple<String, Integer, Boolean>> start() throws UnknownDicomHelperInstanceException, DicomNetworkException, GeneralSecurityException {
        return _isEnableDicomReceiver.get() ? cycleDicomSCPPorts(_dicomSCPInstanceService.getPortsWithEnabledInstances()) : Collections.emptyList();
    }

    public List<Triple<String, Integer, Boolean>> stop() throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        return _dicomSCPStore.stopAll();
    }

    /**
     * isCustomProcessing
     * Cache this because CStore asks this a lot.
     *
     * @param aeTitle The AE title of the instance to check
     * @param port    The port of the instance to check
     *
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
     * @param aeTitle The AE title of the instance to check
     * @param port    The port of the instance to check
     *
     * @return false if SCP instance is unknown
     */
    public boolean isDirectArchive(String aeTitle, int port) {
        Optional<DicomSCPInstance> instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port);
        return instance.map(DicomSCPInstance::isDirectArchive).orElse(false);
    }

    /**
     * getDirectArchiveOverwrite
     *
     * @param aeTitle The AE title of the instance to check
     * @param port    The port of the instance to check
     *
     * @return the overwrite mode (null, "append", or "delete"), or null if SCP instance is unknown
     */
    public String getDirectArchiveOverwrite(String aeTitle, int port) {
        Optional<DicomSCPInstance> instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port);
        return instance.map(DicomSCPInstance::getDirectArchiveOverwrite).orElse(null);
    }

    /**
     * isAnonymizationEnabled
     * Cache this because CStore asks this a lot.
     *
     * @param aeTitle The AE title of the instance to check
     * @param port    The port of the instance to check
     *
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
     * @param aeTitle The AE title of the instance to check
     * @param port    The port of the instance to check
     *
     * @return a DOI for the specified instance or null if the instance does not exist.
     */
    @Nullable
    public DicomObjectIdentifier<XnatProjectdata> getDicomObjectIdentifier(final String aeTitle, int port) {
        DicomSCPInstance instance = _dicomSCPInstanceService.findByAETitleAndPort(aeTitle, port)
                                                            .orElseThrow(() -> new IllegalArgumentException("Unknown DicomSCPInstances with aeTitle '%s' and port %d".formatted(aeTitle, port)));
        DicomObjectIdentifier<XnatProjectdata> doi = _dicomObjectIdentifierMap.get(instance.getIdentifier());
        return doi instanceof ReceiverAwareIdentifier<?> rai ?
                rai.forInstance(instance) :
                doi;
    }

    // for API
    public void resetDicomObjectIdentifier() {
        final DicomObjectIdentifier<XnatProjectdata> objectIdentifier = getDefaultDicomObjectIdentifier();
        if (objectIdentifier instanceof CompositeDicomObjectIdentifier identifier) {
            identifier.getProjectIdentifier().reset();
        }
    }

    // for API
    public void resetDicomObjectIdentifier(final String beanId) {
        final DicomObjectIdentifier<XnatProjectdata> identifier = getDicomObjectIdentifier(beanId);
        if (identifier instanceof CompositeDicomObjectIdentifier objectIdentifier) {
            objectIdentifier.getProjectIdentifier().reset();
        }
    }

    // for API
    public void resetDicomObjectIdentifierBeans() {
        for (final DicomObjectIdentifier<XnatProjectdata> identifier : getDicomObjectIdentifiers().values()) {
            if (identifier instanceof CompositeDicomObjectIdentifier objectIdentifier) {
                objectIdentifier.getProjectIdentifier().reset();
            }
        }
    }

    public Set<Integer> getPortsWithEnabledInstances() {
        return _dicomSCPInstanceService.getPortsWithEnabledInstances();
    }

    protected DicomObjectIdentifier<XnatProjectdata> getIdentifier(final String identifier) throws UnknownDicomHelperInstanceException {
        //noinspection unchecked
        final DicomObjectIdentifier<XnatProjectdata> bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomObjectIdentifier.class) : _context.getBean(identifier, DicomObjectIdentifier.class);
        log.debug("Found bean of type {} for DICOM object identifier {}", bean.getClass().getName(), StringUtils.defaultIfBlank(identifier, "default"));
        return bean;
    }

    protected DicomFileNamer getDicomFileNamer(final String identifier) {
        final DicomFileNamer bean = StringUtils.isBlank(identifier) ? _context.getBean(DicomFileNamer.class) : _context.getBean(identifier, DicomFileNamer.class);
        log.debug("Found bean of type {} for DICOM file namer {}", bean.getClass().getName(), StringUtils.defaultIfBlank(identifier, "default"));
        return bean;
    }

    protected XnatUserProvider getUserProvider() {
        return super.getUserProvider();
    }

    private void handleInsert(final long id) {
        final DicomSCPInstance instance = _dicomSCPInstanceService.findById(id);
        if (instance == null) {
            log.warn("Could not find DICOM SCP instance with ID {}, cannot handle INSERT event", id);
        } else {
            log.debug("Handling INSERT event for DICOM SCP instance {} with AE title {} and port {}", id, instance.getAeTitle(), instance.getPort());
            try {
                final Triple<String, Integer, Boolean> triple = cycleDicomSCPPort(instance.getPort());
                log.debug("Cycled DICOM SCP instance {} with AE title {} and port {}, result was: {}", id, triple.getLeft(), triple.getMiddle(), triple.getRight());
            } catch (DicomNetworkException e) {
                log.error("A DICOM network error occurred while trying to cycle DICOM SCP port {}", instance.getPort(), e);
            } catch (UnknownDicomHelperInstanceException e) {
                log.error("An unknown DICOM helper error occurred while trying to cycle port {}", instance.getPort(), e);
            } catch (GeneralSecurityException e) {
                log.error("An unknown General Security error occurred while trying to shut down", e);
            }
        }
    }

    private void handleUpdate(final long id, final String aeTitle, final int port) {
        log.debug("Handling UPDATE event for DICOM SCP instance {} with AE title {} and port {}", id, aeTitle, port);
        final DicomSCPInstance instance = _dicomSCPInstanceService.findById(id);
        if (instance == null) {
            log.warn("Could not find DICOM SCP instance with ID {}, cannot handle UPDATE event", id);
        } else {
            log.debug("Found DICOM SCP instance {} with AE title {} and port {}", id, instance.getAeTitle(), instance.getPort());
            if (port > 0 && port != instance.getPort()) {
                try {
                    cycleDicomSCPPorts(Stream.of(port, instance.getPort()).collect(Collectors.toSet()));
                } catch (DicomNetworkException e) {
                    log.error("A DICOM network error occurred while trying to cycle DICOM SCP ports {} and {}", port, instance.getPort(), e);
                } catch (UnknownDicomHelperInstanceException e) {
                    log.error("An unknown DICOM helper error occurred while trying to cycle ports {} and {}", port, instance.getPort(), e);
                } catch (GeneralSecurityException e) {
                    log.error("An unknown General Security error occurred while trying to shut down", e);
                }
            } else {
                try {
                    cycleDicomSCPPort(instance.getPort());
                } catch (DicomNetworkException e) {
                    log.error("A DICOM network error occurred while trying to cycle DICOM SCP port {}", port, e);
                } catch (UnknownDicomHelperInstanceException e) {
                    log.error("An unknown DICOM helper error occurred while trying to cycle port {}", port, e);
                } catch (GeneralSecurityException e) {
                    log.error("An unknown General Security error occurred while trying to shut down", e);
                }
            }
        }
    }

    private void handleDelete(final String aeTitle, final int port) {
        log.debug("Handling DELETE event for DICOM SCP instance with AE title {} and port {}", aeTitle, port);
        try {
            cycleDicomSCPPort(port);
        } catch (DicomNetworkException e) {
            log.error("A DICOM network error occurred while trying to cycle DICOM SCP port {}", port, e);
        } catch (UnknownDicomHelperInstanceException e) {
            log.error("An unknown DICOM helper error occurred while trying to cycle port {}", port, e);
        } catch (GeneralSecurityException e) {
            log.error("An unknown General Security error occurred while trying to shut down", e);
        }
    }

    @Nonnull
    private DicomSCPInstance toggleEnabled(final boolean enabled, final int id) throws NotFoundException {
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
        } catch (NrgServiceException | GeneralSecurityException e) {
            // Shouldn't happen: we just retrieved it and enabled doesn't count towards duplicate properties.
            return instance;
        }
    }

    private Triple<String, Integer, Boolean> cycleDicomSCPPort(final int updated) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        final List<Triple<String, Integer, Boolean>> triples = cycleDicomSCPPorts(Collections.singleton(updated));
        if (triples.isEmpty()) {
            log.warn("No cycle dicom SCP ports found for port {}", updated);
            return ImmutableTriple.nullTriple();
        }
        return triples.getFirst();
    }

    private List<Triple<String, Integer, Boolean>> cycleDicomSCPPorts(final Set<Integer> updated) throws DicomNetworkException, UnknownDicomHelperInstanceException, GeneralSecurityException {
        log.debug("I'm going to cycle {} ports that have been added or updated: {}", updated.size(), updated);
        return _dicomSCPStore.cycle(updated);
    }
}
