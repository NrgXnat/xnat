//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * XDAT eXtensible Data Archive Toolkit
 * Copyright (C) 2005 Washington University
 */
/*
 * Created on Nov 3, 2004
 */
package org.nrg.xft;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.nrg.config.exceptions.ConfigServiceException;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xft.db.DBPool;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperFactory;
import org.nrg.xft.schema.XFTManager;
import org.nrg.xft.schema.XFTSchema;
import org.nrg.xft.schema.design.SchemaFieldI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;

/**
 * @author Tim
 *
 * This class is used to initialize the settings for XFT tasks.
 */
public class XFT {
    private static String ADMIN_EMAIL = "nrgtech@nrg.wustl.edu";
    private static String ADMIN_EMAIL_HOST = "";
    private static String SITE_URL = "";
    private static String ARCHIVE_ROOT_PATH = "";
    private static String PREARCHIVE_PATH = "";
    private static String CACHE_PATH = "";
    static org.apache.log4j.Logger logger = Logger.getLogger(XFT.class);
    public static final String PREFIX = "xdat";
    public static final char PATH_SEPERATOR = '/';

    public static boolean VERBOSE = false;
    private static Boolean REQUIRE_REASON = null;
    private static Boolean SHOW_REASON = null;
    
    private static Boolean REQUIRE_EVENT_NAME = false;//used to configure whether event names are required on modifications

    public static void init() throws ElementNotFoundException, MalformedURLException, URISyntaxException {
        init(true);
    }

    /**
     * This method must be run before any XFT task is performed.
     * Using the InstanceSettings.xml document, it initializes the
     * XFT's settings and loads the schema.
     */
    public static void init(final boolean initLog4j) throws ElementNotFoundException, MalformedURLException, URISyntaxException {
        if (initLog4j) {
            initLog4j();
        }

        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();

        //XFT.LogCurrentTime("XFT INIT:2","ERROR");
        XFTManager.init();

        //XFT.LogCurrentTime("XFT INIT:3","ERROR");
        try {
            XFTMetaManager.init();

            //XFT.LogCurrentTime("XFT INIT:4","ERROR");
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

           // XFT.LogCurrentTime("XFT INIT:5","ERROR");
            XFTReferenceManager.init();

           //XFT.LogCurrentTime("XFT INIT:6","ERROR");
            schemas = XFTManager.GetSchemas().iterator();
            while (schemas.hasNext())
            {
                XFTSchema s = (XFTSchema)schemas.next();
            //	XFT.LogCurrentTime("XFT INIT " + s.getTargetNamespacePrefix() + ":1","ERROR");
                Iterator elements = s.getWrappedElementsSorted(GenericWrapperFactory.GetInstance()).iterator();
            //	XFT.LogCurrentTime("XFT INIT " + s.getTargetNamespacePrefix() + ":2 " ,"ERROR");
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
            //	XFT.LogCurrentTime("XFT INIT " + s.getTargetNamespacePrefix() + ":3","ERROR");
            }
        } catch (XFTInitException e) {
            e.printStackTrace();
        }
        if (XFT.VERBOSE)
         {
            System.out.print("");
       // XFT.LogCurrentTime("XFT INIT:7","ERROR");
        }
    }

    public static void initLog4j()
    {
        PropertyConfigurator.configure(XDAT.getContextService().getConfigurationStream("log4j.properties"));

        logger.info("");
        Logger.getLogger("org.nrg.xft.db.PoolDBUtils").error("");

        LoggerRepository lr = logger.getLoggerRepository();
        Enumeration enum1 = lr.getCurrentLoggers();
        while (enum1.hasMoreElements())
        {
            Logger l = (Logger)enum1.nextElement();
            Enumeration e2 = l.getAllAppenders();
            while (e2.hasMoreElements())
            {
                Appender a = (Appender)e2.nextElement();
                if (a instanceof FileAppender)
                {
                    FileAppender fa = (FileAppender)a;
                    String s = fa.getFile();
                    if (s != null)
                    {
                        File f = new File(s);
                        if (f.exists())
                        {
                            try {
                                Runtime.getRuntime().exec("ls -l " + s + " > " + s + ".info1");
                                Runtime.getRuntime().exec("chmod 777 " + s);
                                Runtime.getRuntime().exec("ls -l " + s + " > " + s + ".info2");
                            } catch (Exception e1) {
                            }
                        }else{
                            try {
                               // FileUtils.OutputToFile("",s);
                                Runtime.getRuntime().exec("touch " + s);
                                Runtime.getRuntime().exec("ls -l " + s + " > " + s + ".info1");
                                Runtime.getRuntime().exec("chmod 777 " + s);
                                Runtime.getRuntime().exec("ls -l " + s + " > " + s + ".info2");
                            } catch (Exception e1) {
                            }
                        }
                    }
                }
            }
        }
    }

    public static boolean IsInitialized()
    {
        try {
            XFTManager.GetInstance();
            return true;
        } catch (XFTInitException e) {
            return false;
        }
    }

    public static void closeConnections() throws SQLException
    {
       DBPool.GetPool().closeConnections();
    }

    public static String buildLogFileName(ItemI item) throws XFTInitException, ElementNotFoundException, FieldNotFoundException{
        // MIGRATE: This is iffy but I'm not sure it's ever used here.
        String s = XDAT.getContextService().getAppRelativeLocation("logs").getPath();
        if(!(new File(s)).exists())
        {
            (new File(s)).mkdir();
        }

        s += "inserts/";
        if(!(new File(s)).exists())
        {
            (new File(s)).mkdir();
        }

        String fileName = item.getItem().getProperName();

        Iterator iter = item.getItem().getGenericSchemaElement().getAllPrimaryKeys().iterator();
        while (iter.hasNext())
        {
            SchemaFieldI sf = (SchemaFieldI)iter.next();
            Object pk = item.getProperty(sf.getXMLPathString(item.getXSIType()));

            fileName += "_" + pk;
        }

        if ((new File(s + fileName + ".sql")).exists())
        {
            int counter = 0;
            while ((new File(s + fileName + "_" + counter + ".sql")).exists())
            {
                counter ++;
            }
            fileName = fileName+ "_" + counter;
        }

        fileName += ".sql";

        return fileName;
    }

    public static void LogInsert(String message, String fileName)
    {
        if (!fileName.startsWith("xdat:"))
        {
            try {
                // MIGRATE: This is iffy but I'm not sure it's ever used here.
                String s = XDAT.getContextService().getAppRelativeLocation("logs").getPath();
                 if(!(new File(s)).exists())
                 {
                     (new File(s)).mkdir();
                 }

                 s += "inserts/";
                 if(!(new File(s)).exists())
                 {
                     (new File(s)).mkdir();
                 }

                 if ((new File(s + fileName + ".sql")).exists())
                 {
                     int counter = 0;
                     while ((new File(s + fileName + "_" + counter + ".sql")).exists())
                     {
                         counter ++;
                     }
                     fileName = fileName+ "_" + counter;
                 }

                 fileName += ".sql";

                 FileUtils.OutputToFile(message,s + fileName);
             } catch (Exception e) {
                 logger.error("",e);
             }
        }
    }

    public static void LogInsert(String message, ItemI item)
    {
        if (!item.getItem().getXSIType().startsWith("xdat:") && !item.getItem().getXSIType().startsWith("wrk:"))
        {
            try {
                // MIGRATE: This is iffy but I'm not sure it's ever used here.
                 String s = XDAT.getContextService().getAppRelativeLocation("logs").getPath();
                 if(!(new File(s)).exists())
                 {
                     (new File(s)).mkdir();
                 }

                 s += "inserts/";
                 if(!(new File(s)).exists())
                 {
                     (new File(s)).mkdir();
                 }

                 String fileName = item.getItem().getProperName();

                 Iterator iter = item.getItem().getGenericSchemaElement().getAllPrimaryKeys().iterator();
                 while (iter.hasNext())
                 {
                     SchemaFieldI sf = (SchemaFieldI)iter.next();
                     Object pk = item.getProperty(sf.getXMLPathString(item.getXSIType()));

                     fileName += "_" + pk;
                 }

                 fileName=fileName.replace(":", "_");

                 if ((new File(s + fileName + ".sql")).exists())
                 {
                     int counter = 0;
                     while ((new File(s + fileName + "_" + counter + ".sql")).exists())
                     {
                         counter ++;
                     }
                     fileName = fileName+ "_" + counter;
                 }

                 fileName += ".sql";

                 FileUtils.OutputToFile(message,s + fileName);
             } catch (Exception e) {
                 logger.error("",e);
             }
        }
    }

    public static void LogError(Object message)
    {
        logger.error(message);
    }

    public static void LogCurrentTime(Object message)
    {
        logger.debug(message + "--\t\t" + Calendar.getInstance().get(Calendar.MINUTE)+":" + Calendar.getInstance().get(Calendar.SECOND) +":" + Calendar.getInstance().get(Calendar.MILLISECOND));
    }

    public static void LogCurrentTime(Object message, String level)
    {
        if (level.equalsIgnoreCase("ERROR"))
        {
            logger.error(message + "--\t\t" + Calendar.getInstance().get(Calendar.MINUTE)+":" + Calendar.getInstance().get(Calendar.SECOND) +":" + Calendar.getInstance().get(Calendar.MILLISECOND));
        }else if (level.equalsIgnoreCase("INFO"))
        {
            logger.info(message + "--\t\t" + Calendar.getInstance().get(Calendar.MINUTE)+":" + Calendar.getInstance().get(Calendar.SECOND) +":" + Calendar.getInstance().get(Calendar.MILLISECOND));
        }else
        {
            logger.debug(message + "--\t\t" + Calendar.getInstance().get(Calendar.MINUTE)+":" + Calendar.getInstance().get(Calendar.SECOND) +":" + Calendar.getInstance().get(Calendar.MILLISECOND));
        }

    }
//    /**
//     * @return Returns the wEBAPP_NAME.
//     */
//    public static String getWEBAPP_NAME() {
//        return WEBAPP_NAME;
//    }
//
//    public static void setWEBAPP_NAME(String s){
//        WEBAPP_NAME= s;
//        if (WEBAPP_NAME.startsWith("/")){
//            WEBAPP_NAME=WEBAPP_NAME.substring(1);
//        }
//    }

    public static Character CreateRandomCharacter(Random randGen){
        int i = 111;
        while (i==111)
        {
            i= randGen.nextInt(25) + 97;
        }
        return new Character((char)i);
    }

    public static Integer CreateRandomNumber(Random randGen){
        return randGen.nextInt(8)+1;
    }

    public static String CreateRandomAlphaNumeric(int length){
        Random randGen= new Random();
        String temp ="";
        boolean b=true;
        for(int i=0;i<length;i++){
            if(b){
                b=false;
                temp +=CreateRandomCharacter(randGen);
            }else{
                b=true;
                temp +=CreateRandomNumber(randGen);
            }
        }
        return temp;
    }
//
//    public static String CreateGenericID()
//    {
//        String s = new String();
//        Random randGen = new Random();
//
//        int i = 111;
//        while (i==111)
//        {
//            i= randGen.nextInt(25) + 97;
//        }
//        s += new Character((char)i);
//
//        i = 111;
//        while (i==111)
//        {
//            i= randGen.nextInt(25) + 97;
//        }
//        s += new Character((char)i);
//
//        i = 111;
//        while (i==111)
//        {
//            i= randGen.nextInt(25) + 97;
//        }
//        s += new Character((char)i);
//
//        i= randGen.nextInt(8)+1;
//        s += i;
//
//        i= randGen.nextInt(8)+1;
//        s += i;
//
//        i= randGen.nextInt(8)+1;
//        s += i;
//
//        return s;
//    }
//
//    public static String CreateGenericID(String table, String id, String dbName, String login, String header){
//        String newID= null;
//        String query = "SELECT count(" + id + ") AS id_count FROM " + table +" WHERE " + id + "='";
//        Long newIDCounter = null;
//        try {
//            Long idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery("SELECT count(" + id + ") AS id_count FROM " + table +";", "id_count", dbName,login);
//            newIDCounter=idCOUNT;
//            newID= header + idCOUNT;
//            idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + newID + "';", "id_count", dbName,login);
//            while (idCOUNT > 0){
//                newIDCounter++;
//                newID= header + idCOUNT;
//                idCOUNT= (Long)PoolDBUtils.ReturnStatisticQuery(query + newID + "';", "id_count", dbName,login);
//            }
//        } catch (Exception e) {
//            logger.error("",e);
//        }
//
//        return newID.toString();
//    }


    private static String SITE_ID ="";

    public static String GetSiteID(){
        return SITE_ID;
    }

    public static void SetSiteID(String s){
        SITE_ID=s;
    }

    /**
     * Returns ID as SITE ID + incremented number.
     * @return
     * @throws Exception
     */
    public static String CreateId(int digits, String column, String tableName, String dbname, String login) throws Exception{
        return CreateIDFromBase(GetSiteID(), digits, column, tableName,dbname,login);
    }

    public static String CreateIDFromBase(String base, int digits, String column, String tableName, String dbname, String login) throws Exception{
        String identifier = "";

        if (base!=null)
        {
            identifier = base;
            identifier = StringUtils.ReplaceStr(identifier, " ", "");
            identifier = StringUtils.ReplaceStr(identifier, "-", "_");
            identifier = StringUtils.ReplaceStr(identifier, "\"", "");
            identifier = StringUtils.ReplaceStr(identifier, "'", "");

            identifier= IncrementID(identifier,digits,column,tableName,dbname,login);
        }else{
            throw new NullPointerException();
        }

        return identifier;
    }

    private static String IncrementID(String s,int digits, String column, String tableName, String dbname, String login) throws Exception{
        String temp_id = null;

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
            String full = StringUtils.ReplaceStr(nf.format(count), ",", "");
            temp_id = s+ full;

            while (al.contains(temp_id)){
                count++;
                full =StringUtils.ReplaceStr(nf.format(count), ",", "");
                temp_id = s+ full;
            }

            return temp_id;
        }else{
            int count =1;
            String full = StringUtils.ReplaceStr(nf.format(count), ",", "");
            temp_id = s+ full;
            return temp_id;
        }
    }


    public static String GetAllSchemaLocations(String location)
    {
        StringBuffer sb = new StringBuffer();
        Iterator schemas = XFTManager.GetSchemas().iterator();
        int counter = 0;
        while (schemas.hasNext())
        {
            XFTSchema s = (XFTSchema)schemas.next();
            if (counter++==0)
            {
                if (location==null)
                {
                    sb.append(s.getTargetNamespaceURI()).append(" ").append(StringUtils.ReplaceStr(s.getDataModel().getFullFileSpecification(),"\\","/"));
                }else{
                    sb.append(s.getTargetNamespaceURI()).append(" ").append(StringUtils.ReplaceStr(location,"\\","/") + StringUtils.ReplaceStr(s.getDataModel().getFolderName(),"\\","/") + "/" + s.getDataModel().getFileName());
                }
            }else{
                if (location==null)
                {
                    sb.append(" ").append(s.getTargetNamespaceURI()).append(" ").append(StringUtils.ReplaceStr(s.getDataModel().getFullFileSpecification(),"\\","/"));
                }else{
                    sb.append(" ").append(s.getTargetNamespaceURI()).append(" ").append(StringUtils.ReplaceStr(location,"\\","/") + StringUtils.ReplaceStr(s.getDataModel().getFolderName(),"\\","/") + "/" + s.getDataModel().getFileName());
                }
            }
        }

        return sb.toString();
    }
    public static String GetSiteURL()
    {
        return XFT.SITE_URL;
    }

    public static void SetSiteURL(String s)
    {
        XFT.SITE_URL=s;
    }
    public static String GetAdminEmail()
    {
        return XFT.ADMIN_EMAIL;
    }

    public static void SetAdminEmail(String s)
    {
        XFT.ADMIN_EMAIL=s;
    }

    public static String GetAdminEmailHost()
    {
        return XFT.ADMIN_EMAIL_HOST;
    }

    public static void SetAdminEmailHost(String s)
    {
        if (!s.equals("%SMTP_SERVER%")) {
            XFT.ADMIN_EMAIL_HOST=s;
        }
    }

    public static String GetArchiveRootPath()
    {
        if (!XFT.ARCHIVE_ROOT_PATH.endsWith(File.separator) && !XFT.ARCHIVE_ROOT_PATH.endsWith("/"))
        {
            XFT.ARCHIVE_ROOT_PATH = XFT.ARCHIVE_ROOT_PATH + File.separator;
        }
        return XFT.ARCHIVE_ROOT_PATH;
    }

    public static void SetArchiveRootPath(String s)
    {
        XFT.ARCHIVE_ROOT_PATH=s.replace('\\', '/');
    }

    public static String GetPrearchivePath()
    {
        if (!XFT.PREARCHIVE_PATH.endsWith(File.separator) && !XFT.PREARCHIVE_PATH.endsWith("/"))
        {
            XFT.PREARCHIVE_PATH = XFT.PREARCHIVE_PATH + File.separator;
        }
        return XFT.PREARCHIVE_PATH;
    }

    public static void SetPrearchivePath(String s)
    {
        XFT.PREARCHIVE_PATH=s.replace('\\', '/');;
    }

    public static String GetCachePath()
    {
        if (!XFT.CACHE_PATH.endsWith(File.separator) && !XFT.CACHE_PATH.endsWith("/"))
        {
            XFT.CACHE_PATH = XFT.CACHE_PATH + File.separator;
        }
        return XFT.CACHE_PATH;
    }

    public static File GetCacheDir()
    {
        return new File(GetCachePath());
    }

    public static void SetCachePath(String s)
    {
        XFT.CACHE_PATH=s.replace('\\', '/');;
    }
    /*
    private static String THUMBNAIL_LOCATION = "";
    public static String GetThumbnailPath()
    {
        if (!XFT.THUMBNAIL_LOCATION.endsWith(File.separator) && !XFT.THUMBNAIL_LOCATION.endsWith("/"))
        {
            XFT.THUMBNAIL_LOCATION = XFT.THUMBNAIL_LOCATION + File.separator;
        }
        return XFT.THUMBNAIL_LOCATION;
    }

    public static void SetThumbnailPath(String s)
    {
        XFT.THUMBNAIL_LOCATION=s;
    }

    private static String LORES_LOCATION = "";
    public static String GetLoResPath()
    {
        if (!XFT.LORES_LOCATION.endsWith(File.separator) && !XFT.LORES_LOCATION.endsWith("/"))
        {
            XFT.LORES_LOCATION = XFT.LORES_LOCATION + File.separator;
        }
        return XFT.LORES_LOCATION;
    }

    public static void SetLoResPath(String s)
    {
        XFT.LORES_LOCATION=s;
    }

    */
    private static String PIPELINE_LOCATION = "";
    public static String GetPipelinePath()
    {
        if (!XFT.PIPELINE_LOCATION.endsWith(File.separator) && !XFT.PIPELINE_LOCATION.endsWith("/"))
        {
            XFT.PIPELINE_LOCATION = XFT.PIPELINE_LOCATION + File.separator;
        }
        return XFT.PIPELINE_LOCATION;
    }

    public static void SetPipelinePath(String s)
    {
        XFT.PIPELINE_LOCATION=s;
    }

    /**
     * FTP Location
     */
    private static String FTP_LOCATION = "";
    public static String getFtpPath()
    {
        if (!XFT.FTP_LOCATION.endsWith(File.separator) && !XFT.FTP_LOCATION.endsWith("/"))
        {
            XFT.FTP_LOCATION = XFT.FTP_LOCATION + File.separator;
        }
        return XFT.FTP_LOCATION;
    }

    public static void setFtpPath(String s)
    {
        XFT.FTP_LOCATION=s;
    }

    /**
     * FTP Location
     */
    private static String BUILD_LOCATION = "";
    public static String getBuildPath()
    {
        if (!XFT.BUILD_LOCATION.endsWith(File.separator) && !XFT.BUILD_LOCATION.endsWith("/"))
        {
            XFT.BUILD_LOCATION = XFT.BUILD_LOCATION + File.separator;
        }
        return XFT.BUILD_LOCATION;
    }

    public static void setBuildPath(String s)
    {
        XFT.BUILD_LOCATION=s;
    }

    private static String RequireLogin = "";
    public static boolean GetRequireLogin()
    {
        if(XFT.RequireLogin==null){
            return true;
        }else if(XFT.RequireLogin.equalsIgnoreCase("false") || XFT.RequireLogin.equalsIgnoreCase("1"))
        {
            return false;
        }else{
            return true;
        }
    }

    public static void SetRequireLogin(String s)
    {
        XFT.RequireLogin=s;
    }

    private static String EmailVerification = "";
    public static boolean GetEmailVerification()
    {
        if(XFT.EmailVerification==null){
            return false;
        }else if(XFT.EmailVerification.equalsIgnoreCase("false") || XFT.EmailVerification.equalsIgnoreCase("1"))
        {
            return false;
        }else{
            return true;
        }
    }

    public static void SetEmailVerification(String s)
    {
        XFT.EmailVerification=s;
    }    

    private static String UserRegistration = "";
    public static boolean GetUserRegistration()
    {
        return !(XFT.UserRegistration != null && (XFT.UserRegistration.equalsIgnoreCase("false") || XFT.UserRegistration.equalsIgnoreCase("1")));
    }

    public static void SetUserRegistration(String s)
    {
        XFT.UserRegistration=s;
    }

    private static String EnableCsrfToken = "";
    public static boolean GetEnableCsrfToken()
    {
        return XFT.EnableCsrfToken == null || !(XFT.EnableCsrfToken.equalsIgnoreCase("false") || XFT.EnableCsrfToken.equalsIgnoreCase("1"));
    }

    public static void SetEnableCsrfToken(String s)
    {
        XFT.EnableCsrfToken=s;
    }
    
    public static boolean getBooleanProperty(String key, String _default){
        try {
            Object item = XDAT.getSiteConfigurationProperty(key);
            if (item == null) {
                return Boolean.valueOf(_default);
            } else {
                return Boolean.valueOf(item.toString());
            }
        } catch (ConfigServiceException e) {
            throw new RuntimeException("Error accessing site configuration", e);
        }
    }
    
    public static boolean getBooleanProperty(String key, boolean _default){
    	return getBooleanProperty(key, Boolean.toString(_default));
    }

    private static final String STR_REQUIRE_EVENT_NAME = "audit.require_event_name";
    private static final String REQUIRE_CHANGE_JUSTIFICATION = "audit.require_change_justification";
    private static final String SHOW_CHANGE_JUSTIFICATION = "audit.show_change_justification";
    
    public static boolean getShowChangeJustification(){
    	if(SHOW_REASON==null){
    		SHOW_REASON=getBooleanProperty(SHOW_CHANGE_JUSTIFICATION,false);
    	}
    	return SHOW_REASON;
    }
    public static boolean getRequireChangeJustification(){
    	if(REQUIRE_REASON==null){
    		REQUIRE_REASON=getBooleanProperty(REQUIRE_CHANGE_JUSTIFICATION,false);
    	}
    	return REQUIRE_REASON;
    }
    public static boolean getRequireEventName(){
    	if(REQUIRE_EVENT_NAME==null){
    		REQUIRE_EVENT_NAME=getBooleanProperty(STR_REQUIRE_EVENT_NAME,false);
    	}
    	return REQUIRE_EVENT_NAME;
    }
    
}

