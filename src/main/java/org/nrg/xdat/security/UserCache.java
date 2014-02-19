/*
 * org.nrg.xdat.security.UserCache
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.security;

import org.apache.log4j.Logger;

/**
 * @author timo
 *
 */
public class UserCache {
	static org.apache.log4j.Logger logger = Logger.getLogger(UserCache.class);
//    private Hashtable users = new Hashtable();
//    private static UserCache CACHE = new UserCache();
//    /**
//     * 
//     */
//    public UserCache() {
//        super();
//    }
//    
//    public void clear(){
//        users = new Hashtable();
//    }
//    
//    public static void Clear(){
//    }
//    
//    private void closeUserSession(String session) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        if(this.users.containsKey(session))
//        {
//            synchronized(this){
//                users.remove(session);
//            }
//        }
//        maintain();
//        System.gc();
//    }
//    
//    private String createUserSession(String login, String password) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        XDATUser user = new XDATUser(login,password);
//        return createUserSession(user);
//    }
//    
//    private String refreshUserSession(String login, String password,String session_id) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        Object[] array = (Object[])users.get(session_id);
//        if (array!=null){
//            return session_id;
//        }else{
//            XDATUser user = new XDATUser(login,password);
//            return createUserSession(user,session_id);
//        }
//    }
//    
//    private String createUserSession(XDATUser user) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        Calendar creationDate = Calendar.getInstance();
//        String id = creationDate.getTimeInMillis() +user.getLogin();
//        return createUserSession(user,id);
//    }
//    
//    private String createUserSession(XDATUser user,String session_id) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        maintain();
//        if (user == null)
//        {
//            throw new Exception("Invalid User.");
//        }
//        
//        Calendar creationDate = Calendar.getInstance();
//        Hashtable hash = new Hashtable();
//        Object[] array = new Object[]{creationDate,user,hash};
//        synchronized(this){
//            this.users.put(session_id,array);
//        }
//        return session_id;
//    }
//    
//    public static void CloseUserSession(String session) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        CACHE.closeUserSession(session);
//    }
//    
//    public static String CreateUserSession(String login, String password) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        return CACHE.createUserSession(login,password);
//    }
//    
//    public static String RefreshUserSession(String login, String password,String session) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        return CACHE.refreshUserSession(login,password,session);
//    }
//    
//    public static String CreateUserSession(XDATUser user) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        return CACHE.createUserSession(user);
//    }
//    
//    public XDATUser getUser(String session_id) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,InvalidSessionException,Exception
//    {
//        Object[] array = (Object[])users.get(session_id);
//        if (array ==null)
//        {
//            throw new InvalidSessionException(session_id);
//        }
//        array[0]=Calendar.getInstance();
//
//        synchronized(this){
//            users.put(session_id,array);
//        }
//        return (XDATUser)array[1];
//    }
//    
//    public synchronized Hashtable getSessionHash(String session_id) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,InvalidSessionException,Exception
//    {
//        Object[] array = (Object[])users.get(session_id);
//        if (array ==null)
//        {
//            throw new InvalidSessionException(session_id);
//        }
//        array[0]=Calendar.getInstance();
//
//        synchronized(this){
//            users.put(session_id,array);
//        }
//        return (Hashtable)array[2];
//    }
//    
//    public static XDATUser GetUser(String session_id) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        return CACHE.getUser(session_id);
//    }
//    
//    public static Hashtable GetSessionHash(String session_id) throws XFTInitException, ElementNotFoundException, DBPoolException, java.sql.SQLException,FieldNotFoundException,FailedLoginException,Exception
//    {
//        return CACHE.getSessionHash(session_id);
//    }
//    
//    public synchronized void maintain(){
//        Calendar current= Calendar.getInstance();
//        current.add(Calendar.MINUTE,-15);
//        ArrayList toRemove= new ArrayList();
//        Enumeration keys = users.keys();
//        while (keys.hasMoreElements())
//        {
//            Object key = keys.nextElement();
//            Object[] array = (Object[]) users.get(key);
//            Calendar cal = (Calendar)array[0];
//            //System.out.println(current.getTime() + " " + cal.getTime());
//            if (cal.before(current))
//            {
//                toRemove.add(key);
//            }
//        }
//        
//        Iterator iter = toRemove.iterator();
//        while (iter.hasNext())
//        {
//            Object key = iter.next();
//            users.remove(key);
//        }
//        
//        if (XFT.VERBOSE)System.out.println(users.size() + " User(s) in cache.");
//        logger.info(users.size() + " User(s) in cache.");
//    }
//    
//
//	
//	public class InvalidSessionException extends Exception
//	{
//		public String FAILED_SESSION=null;
//		public InvalidSessionException(String session)
//		{
//			super("Invalid Session: " + session);
//			FAILED_SESSION=session;
//		}
//	};
}
