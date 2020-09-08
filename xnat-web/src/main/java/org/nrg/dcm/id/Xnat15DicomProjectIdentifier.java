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
import org.nrg.config.entities.Configuration;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Xnat15DicomProjectIdentifier extends DbBackedProjectIdentifier {
    public static final String DICOM_ROUTING_TOOL        = "dicom";
    public static final String PROJECT_ROUTING_RULES_CFG = "projectRules";

    public Xnat15DicomProjectIdentifier(final UserProjectCache userProjectCache) {
        super(userProjectCache);
    }

    protected List<DicomDerivedString> getIdentifiers() {
        final List<DicomDerivedString> identifiers = Arrays.asList(new ContainedAssignmentDicomIdentifier(Tag.PatientComments, PROJECT, Pattern.CASE_INSENSITIVE),
                                                                   new ContainedAssignmentDicomIdentifier(Tag.StudyComments, PROJECT, Pattern.CASE_INSENSITIVE),
                                                                   new ContainedAssignmentDicomIdentifier(Tag.AdditionalPatientHistory, PROJECT, Pattern.CASE_INSENSITIVE),
                                                                   new TextDicomIdentifier(Tag.StudyDescription),
                                                                   new TextDicomIdentifier(Tag.AccessionNumber));
        loadFrom15Config(identifiers);
        return identifiers;
    }

    private static void loadFrom15Config(final Collection<DicomDerivedString> identifiers) {
        try {
            final Reader        source;
            final Configuration configuration = XDAT.getConfigService().getConfig(DICOM_ROUTING_TOOL, PROJECT_ROUTING_RULES_CFG);
            final ConfigPaths   paths         = XDAT.getContextService().getBeanSafely(ConfigPaths.class);
            final Path          confDir       = Paths.get(XFT.GetConfDir());
            if (!paths.contains(confDir)) {
                paths.add(confDir);
            }
            final List<File> configs = paths.findFiles(DICOM_PROJECT_RULES);
            if (configuration != null && configuration.isEnabled() && org.apache.commons.lang.StringUtils.isNotBlank(configuration.getContents())) {
                source = new StringReader(configuration.getContents());
            } else if (configs.size() > 0) {
                source = new FileReader(configs.get(0));
            } else {
                source = null;
            }

            if (source != null) {
                try (final BufferedReader reader = new BufferedReader(source)) {
                    String line;
                    while (null != (line = reader.readLine())) {
                        final DicomDerivedString extractor = parseRule(line);
                        if (null != extractor) {
                            identifiers.add(extractor);
                        }
                    }
                }
            }
        } catch (FileNotFoundException ignored) {
            //
        } catch (IOException e) {
            log.error("An error occurred trying to open the DICOM project rules configuration", e);
        }
    }

    private static DicomDerivedString parseRule(final String rule) {
        final Matcher matcher = CUSTOM_RULE_PATTERN.matcher(rule);
        if (matcher.matches()) {
            final int    tag      = Integer.decode("0x" + matcher.group(1) + matcher.group(2));
            final String regexp   = matcher.group(3);
            final String groupIdx = matcher.group(4);
            final int    group    = null == groupIdx ? 1 : Integer.parseInt(groupIdx);
            return new PatternDicomIdentifier(tag, Pattern.compile(regexp), group);
        } else {
            return null;
        }
    }

    private static final Pattern CUSTOM_RULE_PATTERN = Pattern.compile("\\((\\p{XDigit}{4}),(\\p{XDigit}{4})\\):(.+?)(?::(\\d+))?");
    private static final String  DICOM_PROJECT_RULES = "dicom-project.rules";
    private static final String  PROJECT             = "Project";
}
