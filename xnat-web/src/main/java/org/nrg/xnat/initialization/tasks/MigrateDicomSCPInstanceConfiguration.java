/*
 * web: org.nrg.xnat.initialization.tasks.MigrateDatabaseTables
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import static org.nrg.dcm.scp.DicomSCPManager.TOOL_ID;

/**
 * MigrateDicomSCPInstanceConfiguration: Initializing task to move DicomSCP configuration from Preferences to their own Entity.
 */
@Component
@Slf4j
public class MigrateDicomSCPInstanceConfiguration extends AbstractInitializingTask {
    private final NrgPreferenceService    _preferenceService;
    private final ToolService             _toolService;
    private final DicomSCPManager         _dicomSCPManager;
    private final ObjectMapper            _mapper;

    private static final String PREF_ID      = "dicomSCPInstances";
    private static final String DEFAULT_DOI_LABEL = "dicomObjectIdentifier";

    @Autowired
    public MigrateDicomSCPInstanceConfiguration(final NrgPreferenceService preferenceService, final ToolService toolService, final DicomSCPManager dicomSCPManager) {
        super();
        _preferenceService = preferenceService;
        _toolService = toolService;
        _dicomSCPManager = dicomSCPManager;
        _mapper = new ObjectMapper();
    }

    @Override
    public String getTaskName() {
        return "Migrate DicomSCP config";
    }

    @Override
    @Transactional
    protected void callImpl() throws InitializingTaskException {
        try {
            log.debug( "Look for tool {} preferences.", TOOL_ID);
            Properties properties = _preferenceService.getToolProperties(TOOL_ID);
            if( !properties.isEmpty()) {
                log.debug( "Migrate existing DicomSCPInstance configuration.");
                processProperties(properties);
                deleteToolIfEmpty();
                cycleRecievers();
                if( log.isDebugEnabled()) {
                    List<DicomSCPInstance> instances = _dicomSCPManager.getDicomSCPInstancesList();
                    log.debug( "Migrated {} instances.", instances.size());
                    instances.stream().forEach(i -> log.debug("Migrated instance: {}", i));
                }
            } else {
                log.debug( "Preferences for tool {} do not exist.", TOOL_ID);
                if( _dicomSCPManager.getDicomSCPInstancesList().isEmpty()) {
                    log.debug( "No DicomSCPInstances found. Add default.");
                    _dicomSCPManager.saveDicomSCPInstance( DicomSCPInstance.createDefault());
                }
            }
        }
        catch (Exception e) {
            log.error( "An error occurred migrating DicomSCPInstance configuration: {}", e.getMessage(), e);
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred migrating DicomSCPInstance configurations.", e);
        }
    }

    private void processProperties( Properties properties) throws IOException, NrgServiceException {
        for( String key: properties.stringPropertyNames()) {
            if( key.startsWith( PREF_ID)) {
                String value = properties.getProperty( key);
                DicomSCPInstance instance = _mapper.readValue( value, DicomSCPInstance.class);
                instance.setId(0);
                if( StringUtils.isBlank( instance.getIdentifier())) {
                    instance.setIdentifier( DEFAULT_DOI_LABEL);
                }
                Date now = new Date();
                instance.setCreated( now);
                instance.setTimestamp( now);
                log.debug( "Migrating '{}':'{}' = {} as {}", TOOL_ID, key, value, instance);
                _dicomSCPManager.saveDicomSCPInstance( instance);
                _preferenceService.deletePreference( TOOL_ID, key);
            }
        }
    }

    private void deleteToolIfEmpty() {
        Properties properties = _preferenceService.getToolProperties(TOOL_ID);
        if (properties.isEmpty()) {
            _toolService.delete(_preferenceService.getTool(TOOL_ID));
        }
    }

    private void cycleRecievers() throws Exception {
        _dicomSCPManager.stop();
        _dicomSCPManager.start();
    }
}
