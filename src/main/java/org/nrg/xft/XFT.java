/*
 * org.nrg.xft.XFT
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 1/3/14 12:24 PM
 */


package org.nrg.xft;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class XFT {
    private static String ADMIN_EMAIL      = "nrgtech@nrg.wustl.edu";
    private static String ADMIN_EMAIL_HOST = "";

    private static String CONF_DIR = null;

    private static       String SITE_URL          = "";
    private static       String ARCHIVE_ROOT_PATH = "";
    private static       String PREARCHIVE_PATH   = "";
    private static       String CACHE_PATH        = "";
    private static final Logger logger            = LoggerFactory.getLogger(XFT.class);
    public static final  String PREFIX            = "xdat";
    public static final  char   PATH_SEPERATOR    = '/';
    private static       String WEBAPP_NAME       = null;

    public static  boolean VERBOSE        = false;
    private static Boolean REQUIRE_REASON = null;
    private static Boolean SHOW_REASON    = null;


    private static Boolean REQUIRE_EVENT_NAME = false;//used to configure whether event names are required on modifications

    /**
     * This method must be run before any XFT task is performed.
     * Using the InstanceSettings.xml document, it initializes the
     * XFT's settings and loads the schema.
     *
     * @param location (Directory which includes the InstanceSettings.xml document)
     */
    public static void init(String location) throws ElementNotFoundException {

        if (!location.endsWith(File.separator)) {
            location = location + File.separator;
        }

        if (XFT.VERBOSE) {
            System.out.println("SETTINGS LOCATION: " + location);
        }

        CONF_DIR = location;

        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();

        XFTManager.init(location);

        try {
            XFTMetaManager.init();

            Iterator schemas = XFTManager.GetSchemas().iterator();
            while (schemas.hasNext()) {
                XFTSchema s        = (XFTSchema) schemas.next();
                Iterator  elements = s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
                while (elements.hasNext()) {
                    GenericWrapperElement input = (GenericWrapperElement) elements.next();
                    if (input.isExtension()) {
                        GenericWrapperElement e = GenericWrapperElement.GetElement(input.getExtensionType());
                        e.initializeExtendedField();
                    }
                }
            }

            XFTReferenceManager.init();

            schemas = XFTManager.GetSchemas().iterator();
            while (schemas.hasNext()) {
                XFTSchema s        = (XFTSchema) schemas.next();
                Iterator  elements = s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
                while (elements.hasNext()) {
                    GenericWrapperElement input = (GenericWrapperElement) elements.next();
                    if (!input.hasUniqueIdentifiers() && !input.ignoreWarnings()) {
                        if (XFT.VERBOSE) {
                            System.out.println("WARNING: Data Type:" + input.getFullXMLName() + " has no columns which uniquely identify it.");
                        }
                        logger.info("WARNING: Data Type:" + input.getFullXMLName() + " has no columns which uniquely identify it.");
                    }

                    ArrayList sqlColumnNames = new ArrayList();
                    Iterator  iter           = input.getAllFieldNames().iterator();
                    while (iter.hasNext()) {
                        Object[] field   = (Object[]) iter.next();
                        String   sqlName = (String) field[0];
                        if (!sqlColumnNames.contains(sqlName.toLowerCase())) {
                            sqlColumnNames.add(sqlName.toLowerCase());
                        } else {
                            System.out.println("ERROR: Duplicate SQL column names in data type:" + input.getFullXMLName() + " column:" + sqlName);
                            logger.info("ERROR: Duplicate SQL column names in data type:" + input.getFullXMLName() + " column:" + sqlName);
                        }

                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("", e);
        }
        if (XFT.VERBOSE) {
            System.out.print("");
        }
    }

    public static boolean IsInitialized() {
        try {
            XFTManager.GetInstance();
            return true;
        } catch (XFTInitException e) {
            return false;
        }
    }

    public static Character CreateRandomCharacter(Random randGen) {
        int i = 111;
        while (i == 111) {
            i = randGen.nextInt(25) + 97;
        }
        return (char) i;
    }

    public static Integer CreateRandomNumber(Random randGen) {
        return randGen.nextInt(8) + 1;
    }

    public static String CreateRandomAlphaNumeric(int length) {
        Random  randGen = new Random();
        String  temp    = "";
        boolean b       = true;
        for (int i = 0; i < length; i++) {
            if (b) {
                b = false;
                temp += CreateRandomCharacter(randGen);
            } else {
                b = true;
                temp += CreateRandomNumber(randGen);
            }
        }
        return temp;
    }

    private static String SITE_ID = "";

    public static String GetSiteID() {
        return SITE_ID;
    }

    public static void SetSiteID(String s) {
        SITE_ID = s;
    }

    /**
     * Returns ID as SITE ID + incremented number.
     *
     * @return The generated ID.
     * @throws Exception When something goes wrong.
     */
    public static String CreateId(int digits, String column, String tableName, String dbname, String login) throws Exception {
        return CreateIDFromBase(GetSiteID(), digits, column, tableName, dbname, login);
    }

    public static String CreateIDFromBase(String base, int digits, String column, String tableName, String dbname, String login) throws Exception {
        if (base == null) {
            throw new NullPointerException();
        }
        final String identifier = StringUtils.replace(StringUtils.replace(StringUtils.replace(StringUtils.replace(base, " ", ""), "-", "_"), "\"", ""), "'", "");
        return IncrementID(identifier, digits, column, tableName, dbname, login);
    }

    private static String IncrementID(String s, int digits, String column, String tableName, String dbname, String login) throws Exception {
        String tempId;

        if (s == null) {
            throw new NullPointerException();
        }

        XFTTable  table = org.nrg.xft.search.TableSearch.Execute("SELECT " + column + " FROM " + tableName + " WHERE " + column + " LIKE '" + s + "%';", dbname, login);
        ArrayList al    = table.convertColumnToArrayList("id");

        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(digits);
        if (al.size() > 0) {
            int    count = al.size() + 1;
            String full  = StringUtils.replace(nf.format(count), ",", "");
            tempId = s + full;

            while (al.contains(tempId)) {
                count++;
                full = StringUtils.replace(nf.format(count), ",", "");
                tempId = s + full;
            }

            return tempId;
        } else {
            int    count = 1;
            String full  = StringUtils.replace(nf.format(count), ",", "");
            tempId = s + full;
            return tempId;
        }
    }


    public static String GetAllSchemaLocations(String location) {
        StringBuffer sb      = new StringBuffer();
        Iterator     schemas = XFTManager.GetSchemas().iterator();
        int          counter = 0;
        while (schemas.hasNext()) {
            try {
                XFTSchema s = (XFTSchema) schemas.next();
                if (counter++ > 0) {
                    sb.append(" ");
                }

                if (location == null) {
                    sb.append(s.getTargetNamespaceURI()).append(" ").append(s.getDataModel().getResource().getFile().getPath());
                } else {
                    sb.append(s.getTargetNamespaceURI()).append(" ").append(XDAT.getSiteConfigPreferences().getSiteUrl()).append("/schemas/").append(s.getDataModel().getFileName());
                }
            } catch (IOException e) {
                logger.error("", e);
            }
        }

        return sb.toString();
    }

    public static String GetConfDir() {
        return CONF_DIR;
    }
}

