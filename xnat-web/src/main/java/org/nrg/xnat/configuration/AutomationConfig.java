/*
 * web: org.nrg.xnat.configuration.AutomationConfig
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.configuration;

import org.nrg.automation.runners.ScriptRunnerOutputAdapter;
import org.nrg.automation.services.ScriptRunnerService;
import org.nrg.automation.services.ScriptService;
import org.nrg.automation.services.ScriptTriggerService;
import org.nrg.automation.services.impl.DefaultScriptRunnerService;
import org.nrg.framework.exceptions.NrgServiceException;
import org.nrg.framework.orm.hibernate.HibernateEntityPackageList;
import org.nrg.xnat.utils.BuildDirScriptRunnerOutputAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan({"org.nrg.automation.services.impl.hibernate", "org.nrg.automation.repositories"})
public class AutomationConfig {

    @Bean
    public ScriptRunnerService scriptRunnerService(final ScriptService scriptService, final ScriptTriggerService triggerService) {
        final DefaultScriptRunnerService service = new DefaultScriptRunnerService(scriptService, triggerService);
        service.setRunnerPackages(Collections.singletonList("org.nrg.automation.runners"));
        return service;
    }

    @Bean
    public HibernateEntityPackageList nrgAutomationEntityPackages() {
        return new HibernateEntityPackageList(Collections.singletonList("org.nrg.automation.entities"));
    }

    @Bean
    public ScriptRunnerOutputAdapter scriptRunnerOutputAdapter() throws NrgServiceException {
        return new BuildDirScriptRunnerOutputAdapter();
    }

    @Bean
    public String populateEventsQuery() {
        return "SELECT event_id, event_label, total\n" +
                "FROM (SELECT DISTINCT\n" +
                "    CASE pipeline_name\n" +
                "        WHEN 'Transfer'::text\n" +
                "        THEN 'Archive'::text\n" +
                "        ELSE\n" +
                "            CASE xs_lastposition('/'::text, pipeline_name::text)\n" +
                "                WHEN 0\n" +
                "                THEN pipeline_name\n" +
                "                ELSE substring(substring(pipeline_name::text, xs_lastposition('/'::text, pipeline_name::text) + 1), 1, xs_lastposition('.'::text, substring(pipeline_name::text, xs_lastposition('/'::text, pipeline_name::text) + 1)) - 1)\n" +
                "            END\n" +
                "    END AS event_label,\n" +
                "    pipeline_name AS event_id,\n" +
                "    count(*) AS total\n" +
                "    FROM (SELECT *\n" +
                "        FROM wrk_workflowData\n" +
                "        WHERE externalid !='ADMIN' AND\n" +
                "              externalid !='' AND\n" +
                "              externalid IS NOT NULL AND\n" +
                "              launch_time > now() - '1 year'::interval) AS current\n" +
                "    GROUP BY event_id, event_label) AS events\n" +
                "WHERE total > 1";
    }

    @Bean
    public String eventSpecCriteria() {
        return " AND event_id LIKE '%%%s%%'";
    }

    @Bean
    public Map<String, String> defaultEvents() {
        // MIGRATION: This stuff needs to go away and be replaced by new events in event service.
        final Map<String, String> defaultEvents = new HashMap<>();
        defaultEvents.put("Added CT Session", "Added CT Session");
        defaultEvents.put("Added Freesurfer", "Added Freesurfer");
        defaultEvents.put("Added Manual QC", "Added Manual QC");
        defaultEvents.put("Added MR Session", "Added MR Session");
        defaultEvents.put("Added PET Session", "Added PET Session");
        defaultEvents.put("Added PET/MR Session", "Added PET/MR Session");
        defaultEvents.put("Added Project", "Added Project");
        defaultEvents.put("Added Subject", "Added Subject");
        defaultEvents.put("Added QC", "Added QC");
        defaultEvents.put("Added Radiology Read-new", "Added Radiology Read-new");
        defaultEvents.put("Added Subject", "Added Subject");
        defaultEvents.put("Added Visit", "Added Visit");
        defaultEvents.put("Added user to project", "Added user to project");
        defaultEvents.put("Catalog(s) Refreshed", "Catalog(s) Refreshed");
        defaultEvents.put("Configured project sharing", "Configured project sharing");
        defaultEvents.put("Created resource", "Created resource");
        defaultEvents.put("Deleted", "Deleted");
        defaultEvents.put("File Deleted", "File Deleted");
        defaultEvents.put("File(s) uploaded", "File(s) uploaded");
        defaultEvents.put("Folder Created", "Folder Created");
        defaultEvents.put("Folder Deleted", "Folder Deleted");
        defaultEvents.put("Initialized permissions", "Initialized permissions");
        defaultEvents.put("Merged", "Merged");
        defaultEvents.put("Modified CT Session", "Modified CT Session");
        defaultEvents.put("Modified Freesurfer", "Modified Freesurfer");
        defaultEvents.put("Modified MR Session", "Modified MR Session");
        defaultEvents.put("Modified Manual QC", "Modified Manual QC");
        defaultEvents.put("Modified PET Session", "Modified PET Session");
        defaultEvents.put("Modified PET/MR Session", "Modified PET/MR Session");
        defaultEvents.put("Modified Project", "Modified Project");
        defaultEvents.put("Modified Subject", "Modified Subject");
        defaultEvents.put("Modified configured pipelines", "Modified configured pipelines");
        defaultEvents.put("Modified project", "Modified project");
        defaultEvents.put("Modified subject", "Modified subject");
        defaultEvents.put("Removed Resource Catalog", "Removed Resource Catalog");
        defaultEvents.put("Removed Subject", "Removed Subject");
        defaultEvents.put("Removed scan", "Removed scan");
        defaultEvents.put("Renamed", "Renamed");
        defaultEvents.put("Stored XML", "Stored XML");
        defaultEvents.put("Transferred", "Transferred");
        defaultEvents.put("Uploaded File", "Uploaded File");
        defaultEvents.put("Upload complete", "Upload complete");
        defaultEvents.put("unknown action", "unknown action");
        defaultEvents.put("xnat_tools/AutoRun.xml", "AutoRun");
        return defaultEvents;
    }

}
