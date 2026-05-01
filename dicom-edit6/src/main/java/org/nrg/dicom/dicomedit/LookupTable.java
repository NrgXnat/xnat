/*
 * DicomEdit: FunctionManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulate Lookup Tables.
 *
 */
public class LookupTable {
    private static final Logger logger = LoggerFactory.getLogger(LookupTable.class);
    private Map<String,Map<String, String>> lookupMaps;

    public LookupTable() {
        lookupMaps = new HashMap<>();
    }

    /**
     * Construct the LookupTable from a file.
     *
     */
    public LookupTable( File file) throws IOException {
        load( new FileReader( file));
    }

    /**
     * Construct the LookupTable from the specified reader.
     *
     * @param reader can be null to indicate non-existent table.
     * @throws IOException
     */
    public LookupTable( Reader reader) throws IOException {
        this();
        if( reader != null) {
            load(reader);
        }
    }

    /**
     * Construct the LookupTable from the specified InputStream.
     *
     * @param inputStream can be null to indicate non-existent table.
     * @throws IOException
     */
    public LookupTable( InputStream inputStream) throws IOException {
        this();
        if( inputStream != null) {
            load(new InputStreamReader(inputStream));
        }
    }

    public void add( String keyType, String key, String value) {
        if( ! lookupMaps.containsKey( keyType)) {
            lookupMaps.put( keyType, new HashMap<String, String>());
        }
        lookupMaps.get(keyType).put( key, value);
    }

    /**
     * Get the value or null.
     *
     * @param keyType
     * @param key
     * @return
     */
    public String lookup( String keyType, String key) {
        String value = null;
        if( lookupMaps.containsKey( keyType)) {
            value = lookupMaps.get(keyType).get(key);
            if( value == null) {
                logger.debug("Unknown keyType/key: " + keyType + "/" + key);
            }
        }
        else {
            logger.debug("Unknown keyType: " + keyType);
        }
        return value;
    }

    public void load( String line) {
        if( ! line.isEmpty() && ! line.trim().startsWith("//")) {
            String[] tokens = line.split("=");
            if( tokens.length == 2) {
                String keys = tokens[0].trim();
                String value = tokens[1].trim();
                String[] keyTokens = keys.split("/");
                if( keyTokens.length == 2) {
                    String keyType = keyTokens[0].trim();
                    String key = keyTokens[1];
                    add( keyType, key, value);
                }
                else {
                    logger.debug("Unrecognized key pair '" + keys + "' in line " + line);
                }
            }
            else {
                logger.debug("Unrecognized line: " + line);
            }
        }
    }

    public void load( File file ) throws IOException {
        load( new FileReader( file));
    }

    public void load( Reader reader) throws IOException {
        try (BufferedReader r = new BufferedReader( reader)) {
            String line;
            while( (line = r.readLine()) != null) {
                load( line);
            }
        }
    }

    public void load( String[] lines) throws IOException {
        for( String line: lines) {
            load( line);
        }
    }
}
