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
import org.apache.log4j.Logger;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;

public class XFT {
    private static String CONF_DIR=null;

    private static final Logger logger         = Logger.getLogger(XFT.class);
    public static final String  PREFIX         = "xdat";
    public static final char    PATH_SEPARATOR = '/';

    public static boolean VERBOSE = false;

    /**
     * This method must be run before any XFT task is performed.
     * Using the InstanceSettings.xml document, it initializes the
     * XFT's settings and loads the schema.
     * @param location (Directory which includes the InstanceSettings.xml document)
     */
    public static void init(String location) throws ElementNotFoundException {
        if (! location.endsWith(File.separator))
        {
            location = location + File.separator;
        }

        if (XFT.VERBOSE) {
            System.out.println("SETTINGS LOCATION: " + location);
        }

        CONF_DIR=location;

        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();

        XFTManager.init(location);

        try {
            XFTMetaManager.init();

            Iterator schemas = XFTManager.GetSchemas().iterator();
            while (schemas.hasNext())
            {
                XFTSchema s = (XFTSchema)schemas.next();
                Iterator elements = s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
                while (elements.hasNext())
                {
                    GenericWrapperElement input = (GenericWrapperElement)elements.next();
                    if (input.isExtension())
                    {
                        GenericWrapperElement e = GenericWrapperElement.GetElement(input.getExtensionType());
                        e.initializeExtendedField();
                    }
                }
            }

            XFTReferenceManager.init();

            schemas = XFTManager.GetSchemas().iterator();
            while (schemas.hasNext())
            {
                XFTSchema s = (XFTSchema)schemas.next();
                Iterator elements = s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
                while (elements.hasNext())
                {
                    GenericWrapperElement input = (GenericWrapperElement)elements.next();
                    if (!input.hasUniqueIdentifiers() && !input.ignoreWarnings())
                    {
                        if (XFT.VERBOSE) {
                            System.out.println("WARNING: Data Type:" + input.getFullXMLName() + " has no columns which uniquely identify it.");
                        }
                        logger.info("WARNING: Data Type:" + input.getFullXMLName() + " has no columns which uniquely identify it.");
                    }

                    ArrayList sqlColumnNames= new ArrayList();
                    Iterator iter = input.getAllFieldNames().iterator();
                    while (iter.hasNext())
                    {
                        Object[] field = (Object[])iter.next();
                        String sqlName = (String)field[0];
                        if (!sqlColumnNames.contains(sqlName.toLowerCase()))
                        {
                            sqlColumnNames.add(sqlName.toLowerCase());
                        }else{
                            System.out.println("ERROR: Duplicate SQL column names in data type:" + input.getFullXMLName() + " column:" + sqlName);
                            logger.info("ERROR: Duplicate SQL column names in data type:" + input.getFullXMLName() + " column:" + sqlName);
                        }

                    }
                }
            }
        } catch (XFTInitException e) {
            logger.error("",e);
        }
        if (XFT.VERBOSE)
         {
            System.out.print("");
        }
    }

    public static String CreateIDFromBase(String base, int digits, String column, String tableName, String dbname, String login) throws Exception{
        String identifier;

        if (base!=null)
        {
            identifier = base;
            identifier = StringUtils.replace(identifier, " ", "");
            identifier = StringUtils.replace(identifier, "-", "_");
            identifier = StringUtils.replace(identifier, "\"", "");
            identifier = StringUtils.replace(identifier, "'", "");

            identifier= IncrementID(identifier,digits,column,tableName,dbname,login);
        }else{
            throw new NullPointerException();
        }

        return identifier;
    }

    private static String IncrementID(String s,int digits, String column, String tableName, String dbname, String login) throws Exception{
        String temp_id;

        if(s == null)
        {
            throw new NullPointerException();
        }

        XFTTable table = org.nrg.xft.search.TableSearch.Execute("SELECT " + column + " FROM " + tableName + " WHERE " + column + " LIKE '" + s + "%';", dbname, login);
        ArrayList al =table.convertColumnToArrayList("id");

        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setMinimumIntegerDigits(digits);
        if (al.size()>0){
            int count =al.size()+1;
            String full = StringUtils.replace(nf.format(count), ",", "");
            temp_id = s+ full;

            while (al.contains(temp_id)){
                count++;
                full = StringUtils.replace(nf.format(count), ",", "");
                temp_id = s+ full;
            }

            return temp_id;
        }else{
            int count =1;
            String full = StringUtils.replace(nf.format(count), ",", "");
            temp_id = s+ full;
            return temp_id;
        }
    }


    public static String GetAllSchemaLocations(String location)
    {
        StringBuilder sb      = new StringBuilder();
        Iterator      schemas = XFTManager.GetSchemas().iterator();
        int           counter = 0;
        while (schemas.hasNext())
        {
            try {
            XFTSchema s = (XFTSchema)schemas.next();
				if (counter++>0)
                {
					sb.append(" ");
                }

                if (location==null)
                {
                    sb.append(s.getTargetNamespaceURI()).append(" ");
                    try {
                        final String path = s.getDataModel().getResource().getFile().getPath();
                        sb.append(path);
                    } catch (FileNotFoundException e) {
                        sb.append(s.getDataModel().getResource().getURI().toString());
                    }
                }else{
				    sb.append(s.getTargetNamespaceURI()).append(" ").append(XDAT.getSiteConfigPreferences().getSiteUrl()).append("/schemas/").append(s.getDataModel().getFileName());
                }
			} catch (IOException e) {
				logger.error("",e);
            }
        }

        return sb.toString();
    }

    public static String GetConfDir(){
        return CONF_DIR;
    }
}

