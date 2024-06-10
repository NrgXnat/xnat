/*
 * web: org.nrg.xnat.utils.PetTracerListUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.nrg.config.entities.Configuration;
import org.nrg.config.services.ConfigService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatProjectdata;

public class PetTracerListUtils {
    final static Logger logger = Logger.getLogger(PetTracerListUtils.class);

    public static List<String> getPetTracerList(String project) {
        try {
            ConfigService configService = XDAT.getConfigService();
            Configuration projectConfig = configService.getConfig("tracers", "tracers", XnatProjectdata.getProjectInfoIdFromStringId(project));
            if (projectConfig != null && projectConfig.getStatus().equals("enabled")) {
                return Arrays.asList(projectConfig.getContents().split("\\s+"));
            }
            else {
                Configuration siteConfig = configService.getConfig("tracers", "tracers");
                if (siteConfig != null) {
                    return Arrays.asList(siteConfig.getContents().split("\\s+"));
                }
                else {
                    return new ArrayList<String>();
                }
            }
        } catch (Exception e) {
            logger.error(e);
            return new ArrayList<String>();
        }
    }
}
