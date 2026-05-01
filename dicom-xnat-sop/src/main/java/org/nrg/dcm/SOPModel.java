/*
 * dicom-xnat-sop: org.nrg.dcm.SOPModel
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.data.UID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@Slf4j
public final class SOPModel {
    public static final String XNAT_SCAN_COLUMN = "XNAT_SCAN_ID";

    private SOPModel() {}

    private static final String LEAD_SCAN_TYPE_R = "/lead-scan-types.txt";
    private static final String LEAD_SESSION_TYPE_R = "/lead-session-types.txt";
    private static final String LEAD_MODALITY_R = "/lead-modalities.txt";
    private static final String SOP_TO_SCAN_TYPE_R = "/series-scans.properties";
    private static final String SOP_TO_SESSION_TYPE_R = "/study-sessions.properties";
    private static final String MODALITY_TO_SESSION_TYPE_R = "/modality-sessions.properties";
    private static final String PRIMARY_SOP_UID_R = "/primary-sops.txt";
    private static final String UNKNOWN_UID_NAME = "Unknown UID Name:";

    private static final Function<String,String> uidForName = new Function<String,String>() {
        public String apply(final String name) {
            try {
                return UID.forName(name);
            } catch (IllegalArgumentException e) {
                if (StringUtils.startsWith(e.getMessage(), UNKNOWN_UID_NAME)) {
                    return name;
                }
                throw e;
            }
        }
    };

    private static final ImmutableMap<String, String> SOP_TO_SCAN_TYPE         = getStringMapFromResource(SOP_TO_SCAN_TYPE_R, uidForName);
    private static final ImmutableMap<String, String> SOP_TO_SESSION_TYPE      = getStringMapFromResource(SOP_TO_SESSION_TYPE_R, uidForName);
    private static final ImmutableMap<String, String> MODALITY_TO_SESSION_TYPE = getStringMapFromResource(MODALITY_TO_SESSION_TYPE_R, null);
    private static final Set<String>                  PRIMARY_SOP_UIDS         = ImmutableSet.copyOf(Lists.transform(getStringsFromResource(PRIMARY_SOP_UID_R), uidForName));
    private static final List<String>                 LEAD_SCAN_TYPES          = getStringsFromResource(LEAD_SCAN_TYPE_R);
    private static final List<String>                 LEAD_SESSION_TYPES       = getStringsFromResource(LEAD_SESSION_TYPE_R);
    private static final List<String>                 LEAD_MODALITIES          = getStringsFromResource(LEAD_MODALITY_R);

    private static ImmutableList<String> getStringsFromResource(final String path) {
        try (final InputStream in = SOPModel.class.getResourceAsStream(path)) {
            if (null == in) {
                throw new RuntimeException("resource " + PRIMARY_SOP_UID_R + " not found");
            }
            final ImmutableList.Builder<String> builder = ImmutableList.builder();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                final LineIterator lines = IOUtils.lineIterator(reader);
                while (lines.hasNext()) {
                    builder.add(StringUtils.trimToEmpty(lines.next()));
                }
                return builder.build();
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read resource " + path, e);
        }
    }

    private static ImmutableMap<String,String> getOldStringMapFromResource(final String path, final Function<String,String> keyXform) {
        try {
            final Properties p = new Properties();
            final InputStream in = SOPModel.class.getResourceAsStream(path);
            if (null == in) {
                throw new RuntimeException("resource " + path + " not found");
            }
            IOException ioexception = null;
            try {
                p.load(in);
            } catch (IOException e) {
                throw ioexception = e;
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    throw ioexception = null == ioexception ? e : ioexception;
                }
            }

            final ImmutableMap.Builder<String,String> builder = ImmutableMap.builder();
            for (final Map.Entry<Object,Object> me : p.entrySet()) {
                final String key = me.getKey().toString();
                final String k = null == keyXform ? key : keyXform.apply(key);
                builder.put(k, me.getValue().toString());
            }
            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize SOP to XNAT model conversion", e);
        }
    }

    private static ImmutableMap<String,String> getStringMapFromResource(final String path, final Function<String,String> keyXform) {
        final ImmutableMap<String, String> oldMap = getOldStringMapFromResource(path, keyXform);
        try {
            final Properties properties = new Properties();
            try (final InputStream in = SOPModel.class.getResourceAsStream(path)) {
                if (null == in) {
                    throw new RuntimeException("resource " + path + " not found");
                }
                properties.load(in);
            }

            for (final Map.Entry<Object,Object> me : properties.entrySet()) {
                final String key   = me.getKey().toString();
                final String k     = null == keyXform ? key : keyXform.apply(key);
                final String value = me.getValue().toString();
                log.info("Got property {} with value {}", k, value);
            }
            final ImmutableMap.Builder<String,String> builder = ImmutableMap.builder();
            for (final String property : properties.stringPropertyNames()) {
                final String key = null == keyXform ? property : keyXform.apply(property);
                assert key != null;
                builder.put(key, properties.getProperty(property));
            }
            return builder.build();
        } catch (IOException e) {
            throw new RuntimeException("Unable to initialize SOP to XNAT model conversion", e);
        }       
    }

    private static <T> T getLead(final Iterable<T> options, final Set<? extends T> values) {
        return Iterables.tryFind(options, new Predicate<T>() {
            @Override
            public boolean apply(final T option) {
                return values.contains(option);
            }
        }).orNull();
    }

    private static String getType(final Iterable<String> sopClassUIDs, final List<String> leadTypes, final Map<String,String> types) {
        return getLead(leadTypes, ImmutableSet.copyOf(Iterables.filter(Iterables.transform(sopClassUIDs, Functions.forMap(types, null)), Predicates.notNull())));
    }

    /**
     * Returns the XSD type of the XNAT scan model corresponding
     * to the given SOP Class UID
     * @param sopClassUID The SOP class UID.

     * @return XNAT scan model type (with no namespace prefix)
     */
    public static String getScanType(final String sopClassUID) {
        return SOP_TO_SCAN_TYPE.get(sopClassUID);
    }   

    /**
     * Returns the XSD type of the XNAT scan model for a series
     * containing the given SOP Class UIDs.
     * @param sopClassUIDs    The list of SOP class UIDs.
     * @return XNAT scan model type (with no namespace prefix)
     */
    public static String getScanType(final String...sopClassUIDs) {
        return getScanType(Arrays.asList(sopClassUIDs));
    }

    /**
     * Returns the XSD type of the XNAT scan model for a series
     * containing the given SOP Class UIDs.
     * @param sopClassUIDs    The list of SOP class UIDs.
     * @return XNAT scan model type (with no namespace prefix)
     */
    public static String getScanType(final Iterable<String> sopClassUIDs) {
        return getType(sopClassUIDs, LEAD_SCAN_TYPES, SOP_TO_SCAN_TYPE);	
    }

    /**
     * Returns the XSD type of the XNAT session model for a study
     * containing the given SOP Class UIDs.
     * @param sopClassUIDs    The list of SOP class UIDs.
     * @return XNAT session model type (with no namespace prefix)
     */
    public static String getSessionType(final Iterable<String> sopClassUIDs) {
        return getType(sopClassUIDs, LEAD_SESSION_TYPES, SOP_TO_SESSION_TYPE);
    }

    public static List<String> getSessionTypesFromSOPMap(){
        return new ArrayList<>(new HashSet<>(SOP_TO_SESSION_TYPE.values()));
    }

    /**
     * Is the given SOP class a primary imaging type?
     * @param sopClassUID    The SOP class UID to test.
     * @return true if the named SOP class is a primary imaging type
     */
    public static boolean isPrimaryImagingSOP(final String sopClassUID) {
        return PRIMARY_SOP_UIDS.contains(sopClassUID);
    }

    /**
     * Returns the DICOM modality identifier (as defined in PS 3.3, C.7.3.1.1.1)
     * that is dominant in XNAT among the provided modalities.
     * @param modalities Set of DICOM modality identifiers
     * @return DICOM modality that dominates in an XNAT representation
     */
    public static String getLeadModality(final Set<String> modalities) {
        return getLead(LEAD_MODALITIES, modalities);
    }

    /**
     * Returns the map from DICOM Modality string to XNAT session model type
     * (with no namespace prefix).
     * @return The map of DICOM modalities to XNAT session model types.
     */
    public static ImmutableMap<String,String> getModalityToSessionTypes() {
        return MODALITY_TO_SESSION_TYPE;
    }
    
    /**
     * Returns the map from SOP Class UID to XNAT session model type
     * (with no namespace prefix).
     * @return The map of SOP class UIDs to XNAT session model types.
     */
    public static ImmutableMap<String,String> getSOPClassToSessionTypes() {
        return SOP_TO_SESSION_TYPE;
    }

    /**
     * Function that returns the lead SOP UID&em;if any&em;from a set of SOP UIDs.
     */
    public final static Function<Set<String>, String> LEAD_SOP_EXTRACTOR = SOPModel::getSessionType;
}
