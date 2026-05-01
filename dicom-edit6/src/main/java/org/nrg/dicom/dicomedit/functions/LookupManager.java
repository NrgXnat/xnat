/*
 * DicomEdit: FunctionManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import org.nrg.dicom.dicomedit.LookupTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Provide API to all Lookup functions.
 *
 * Singleton class that manages lookup table functions.  *
 */
public class LookupManager {
    private static final Logger logger = LoggerFactory.getLogger(LookupManager.class);
    private static LookupManager lookupManager;
    private static LookupTable lookupTable;

    /**
     * Construct the LookupManager.
     *
     * This is private constructor. Use <code>LookupManager.getInstance()</code> to get the singleton instance.
     */
    private LookupManager() {
        lookupTable = new LookupTable();
        lookupManager = this;
    }

    public static void setLookupTable( LookupTable lt) {
        LookupManager.getInstance().lookupTable = lt;
    }

    public void add( String keyType, String key, String value) {
        LookupManager.getInstance().lookupTable.add( keyType, key, value);
    }

    /**
     * Get the value or null.
     *
     * @param keyType
     * @param key
     * @return
     */
    public String lookup( String keyType, String key) {
        return LookupManager.getInstance().lookupTable.lookup( keyType, key);
    }

    /**
     * Return the singleton instance of the LookupManager.
     *
     * @return LookupManager
     */
    public static LookupManager getInstance() {
        if( lookupManager == null) {
            lookupManager = new LookupManager();
        }
        return lookupManager;
    }

    public void load( String line) {
        LookupManager.getInstance().lookupTable.load( line);
    }

    public void load( File file) throws IOException {
        LookupManager.getInstance().lookupTable.load( file);
    }

    public void load( String[] lines) throws IOException {
        LookupManager.getInstance().lookupTable.load( lines);
    }
}
