/*
 * org.nrg.dcm.id.Xnat15DicomProjectIdentifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.dcm.id;

import com.google.common.collect.Lists;
import org.dcm4che2.data.Tag;
import org.nrg.xft.XFT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Xnat15DicomProjectIdentifier extends DbBackedProjectIdentifier {
    private static final String DICOM_PROJECT_RULES = "dicom-project.rules";

    Xnat15DicomProjectIdentifier() {
        super(getIdentifiers());
    }
    
    private static Logger slog() {
        return LoggerFactory.getLogger(Xnat15DicomProjectIdentifier.class);
    }
    
    private static List<DicomDerivedString> getIdentifiers() {
        final List<DicomDerivedString> identifiers = Lists.newArrayList();
        identifiers.add(new ContainedAssignmentDicomIdentifier(Tag.PatientComments, "Project", Pattern.CASE_INSENSITIVE));
        identifiers.add(new ContainedAssignmentDicomIdentifier(Tag.StudyComments, "Project", Pattern.CASE_INSENSITIVE));
        identifiers.add(new TextDicomIdentifier(Tag.StudyDescription));
        identifiers.add(new TextDicomIdentifier(Tag.AccessionNumber));
        loadFrom15Config(identifiers);
        return identifiers;
    }
    
    private static void loadFrom15Config(final Collection<DicomDerivedString> identifiers) {
        final File config = new File(XFT.GetConfDir(), DICOM_PROJECT_RULES);
        IOException ioexception = null;
        if (config.isFile()) {
            try {
                final BufferedReader reader = new BufferedReader(new FileReader(config));
                try {
                    String line;
                    while (null != (line = reader.readLine())) {
                        final DicomDerivedString extractor = parseRule(line);
                        if (null != extractor) {
                            identifiers.add(extractor);
                        }
                    }
                } catch (IOException e) {
                    throw ioexception = e;
                } finally {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        throw ioexception = null == ioexception ? e : ioexception;
                    }
                }
            } catch (Throwable t) {
                slog().error("Unable to load project identification rules from " + DICOM_PROJECT_RULES, t);
            }
        } else {
            slog().debug("custom project rules spec {} not found", DICOM_PROJECT_RULES);                
        }
    }
    
    private static final Pattern CUSTOM_RULE_PATTERN = Pattern.compile("\\((\\p{XDigit}{4})\\,(\\p{XDigit}{4})\\):(.+?)(?::(\\d+))?");


    private static final DicomDerivedString parseRule(final String rule) {
        final Matcher matcher = CUSTOM_RULE_PATTERN.matcher(rule);
        if (matcher.matches()) {
            final StringBuilder tagsb = new StringBuilder("0x");
            tagsb.append(matcher.group(1)).append(matcher.group(2));
            final int tag = Integer.decode(tagsb.toString());
            final String regexp = matcher.group(3);
            final String groupIdx = matcher.group(4);
            final int group = null == groupIdx ? 1 : Integer.parseInt(groupIdx);
            return new PatternDicomIdentifier(tag, Pattern.compile(regexp), group);
        } else {
            return null;
        }
    }
}
