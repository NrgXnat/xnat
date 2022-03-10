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
import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.dcm.scp.DicomSCPManager;
import org.nrg.dcm.scp.exceptions.DicomNetworkException;
import org.nrg.dcm.scp.exceptions.UnknownDicomHelperInstanceException;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.prefs.services.NrgPreferenceService;
import org.nrg.prefs.services.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;

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

    private static final String TOOL_ID      = "dicomScpManager";
    private static final String PREF_ID      = "dicomSCPInstances";

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
            Properties properties = _preferenceService.getToolProperties(TOOL_ID);
            if( !properties.isEmpty()) {
                log.debug(String.format("Migrate existing DicomSCPInstance configuration."));
                processProperties(properties);
                deleteToolIfEmpty();
                cycleRecievers();
            } else {
                log.debug(String.format("Preferences for tool %s do not exist.", TOOL_ID));
                if( _dicomSCPManager.getDicomSCPInstancesList().isEmpty()) {
                    log.debug(String.format("No DicomSCPInstances found. Add default.", TOOL_ID));
                    _dicomSCPManager.saveDicomSCPInstance( DicomSCPInstance.createDefault());
                }
            }
        }
        catch (Exception e) {
            throw new InitializingTaskException(InitializingTaskException.Level.Error, "An error occurred migrating DicomSCPInstance configurations.", e);
        }
    }

    private void processProperties( Properties properties) throws IOException, NrgServiceException {
        for( String key: properties.stringPropertyNames()) {
            if( key.startsWith( PREF_ID)) {
                String value = properties.getProperty( key);
                DicomSCPInstance instance = _mapper.readValue( value, DicomSCPInstance.class);
                instance.setId(0);
                Date now = new Date();
                instance.setCreated( now);
                instance.setTimestamp( now);
                log.debug( String.format("Migrating '%s':'%s' = %s", TOOL_ID, key, value));
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
//        _dicomSCPManager.initDicomObjectIdentifiers();
        _dicomSCPManager.start();
    }
}
