/*
 * web: org.nrg.dcm.id.Xnat15DicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.id;

import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.Tag;
import org.nrg.dcm.ContainedAssignmentExtractor;
import org.nrg.dcm.Extractor;
import org.nrg.dcm.TextExtractor;
import org.nrg.framework.configuration.ConfigPaths;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xnat.services.cache.UserProjectCache;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class Xnat15DicomProjectIdentifier extends DbBackedProjectIdentifier {
    public Xnat15DicomProjectIdentifier(final UserProjectCache userProjectCache) {
        super(userProjectCache);
    }

    @Override
    protected List<Extractor> getIdentifiers() {
        final List<Extractor> identifiers = Arrays.asList(new ContainedAssignmentExtractor(Tag.PatientComments, PROJECT, Pattern.CASE_INSENSITIVE),
                new ContainedAssignmentExtractor(Tag.StudyComments, PROJECT, Pattern.CASE_INSENSITIVE),
                new ContainedAssignmentExtractor(Tag.AdditionalPatientHistory, PROJECT, Pattern.CASE_INSENSITIVE),
                new TextExtractor(Tag.StudyDescription),
                new TextExtractor(Tag.AccessionNumber));
        loadFrom15Config(identifiers);
        return identifiers;
    }

    @Override
    protected List<Extractor> getDynamicExtractors() {
        return XnatDefaultDicomObjectIdentifier.getExtractorsFromConfig(CompositeDicomObjectIdentifier.ExtractorType.PROJECT);
    }

    private static void loadFrom15Config(final Collection<Extractor> identifiers) {
        final ConfigPaths   paths         = XDAT.getContextService().getBeanSafely(ConfigPaths.class);
        final Path          confDir       = Paths.get(XFT.GetConfDir());
        if (!paths.contains(confDir)) {
            paths.add(confDir);
        }
        final List<File> configs = paths.findFiles(DICOM_PROJECT_RULES);
        if (configs.isEmpty()) {
            return;
        }
        File cfgFile = configs.get(0);
        if (configs.size() > 1) {
            log.warn("Multiple project identifier config files found {}, using {}", configs, cfgFile);
        }
        try {
            List<Extractor> extractorsFromFile = XnatDefaultDicomObjectIdentifier.parseAsExtractors(new FileReader(cfgFile));
            identifiers.addAll(extractorsFromFile);
        } catch (IOException e) {
            log.error("An error occurred trying to parse the {} configuration", cfgFile, e);
        }
    }

    private static final String  DICOM_PROJECT_RULES = "dicom-project.rules";
    private static final String  PROJECT             = "Project";
}
