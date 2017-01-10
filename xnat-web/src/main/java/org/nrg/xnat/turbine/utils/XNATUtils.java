/*
 * web: org.nrg.xnat.turbine.utils.XNATUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.CatEntryMetafieldBean;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.SecurityManager;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.XFTTable;
import org.nrg.xft.search.ItemSearch;
import org.nrg.xft.search.TableSearch;
import org.nrg.xft.security.UserI;
/**
 * @author Tim
 *
 */
public class XNATUtils {
	static Logger logger = Logger.getLogger(XNATUtils.class);
    public static String MAP_COLUMN_NAME="map";
    public static String LAB_COLUMN_NAME="lab_id";
    
    public static Hashtable getInvestigatorsForRead(String elementName, RunData data)
    {
        UserI tempUser = TurbineUtils.getUser(data);
        return getInvestigatorsForRead(elementName,tempUser);
    }
    
    public static Hashtable getInvestigatorsForRead(String elementName, UserI user)
    {
        Hashtable _return = new Hashtable();
        try {String login = null;
	        if (user != null)
	        {
	            login = user.getUsername();
	        }
            
            _return = ElementSecurity.GetDistinctIdValuesFor("xnat:investigatorData","default",login);
        } catch (Exception e) {
            logger.error("",e);
        }
        
        return _return;
    }
    
    public static Hashtable getInvestigatorsForCreate(String elementName, RunData data)
    {
        UserI tempUser = TurbineUtils.getUser(data);
        return getInvestigatorsForCreate(elementName,tempUser);
    }
    
    public static Hashtable getInvestigatorsForCreate(String elementName, UserI user)
    {
        Hashtable _return = new Hashtable();
        try {String login = null;
			if (user != null)
			{
			    login = user.getUsername();
			}
	            
	        _return = ElementSecurity.GetDistinctIdValuesFor("xnat:investigatorData","default",login);
        } catch (Exception e) {
            logger.error("",e);
        }
        
        return _return;
    }
    
  
  public static Hashtable getProjectsForCreate(String elementName, RunData data)
  {
      UserI tempUser = TurbineUtils.getUser(data);
      return getProjectsForCreate(elementName,tempUser);
  }
  
  public static Hashtable getProjectsForEdit(String elementName, RunData data)
  {
      UserI tempUser = TurbineUtils.getUser(data);
      return getProjectsForAction(elementName,tempUser,SecurityManager.EDIT);
  }
  
  public static Hashtable getProjectsForCreate(String elementName, UserI user)
  {
      return getProjectsForAction(elementName,user,SecurityManager.CREATE);
  }

  
  public static Hashtable getProjectsForAction(String elementName, UserI user, String action)
  {
      Hashtable _return = new Hashtable();
      try {String login = null;
        if (user != null)
        {
            login = user.getUsername();
        }
          
          if (ElementSecurity.IsSecureElement(elementName,action))
          {
              List<Object> permisionItems = Permissions.getAllowedValues(user,elementName,elementName +"/project",action);
              
              Hashtable temp = ElementSecurity.GetDistinctIdValuesFor("xnat:projectData","default",login);
              
              for(int i=0;i<permisionItems.size();i++){
                  String o=(String)permisionItems.get(i);
            	  if(temp.containsKey(o)){
                      _return.put(o,temp.get(o));
                  }
              } 
          }else{
              _return = ElementSecurity.GetDistinctIdValuesFor("xnat:projectData","default",login);
              
          }
      } catch (Exception e) {
          logger.error("",e);
      }
      
      return _return;
  }
  
    public static String getLastSessionIdForParticipant(String id,UserI user)
    {
        String login = null;
		if (user != null)
		{
		    login = user.getUsername();
		}
        String query = "SELECT mr.id FROM xnat_mrSessionData mr LEFT JOIN xnat_subjectAssessorData sad ON mr.ID=sad.ID LEFT JOIN xnat_experimentData ed ON sad.ID=ed.ID WHERE subject_id='" + id +"' ORDER BY date DESC LIMIT 1";
        try {
            XFTTable table = TableSearch.Execute(query,user.getDBName(),login);
            if (table.size()>0)
            {
                table.resetRowCursor();
                Object mr_id = null;
                while(table.hasMoreRows())
                {
                    mr_id = table.nextRowHash().get("id");
                    if (mr_id !=null)
                    {
                        break;
                    }
                }
                
                return (String)mr_id;
            }

            return null;
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
    }
    
    public static XnatMrsessiondata getLastSessionForParticipant(String id,UserI user)
    {
        try {
            String mr_id = XNATUtils.getLastSessionIdForParticipant(id,user);
            if (mr_id == null)
            {
                return null;
            }
            
            ItemI mr = ItemSearch.GetItem("xnat:mrSessionData.ID",mr_id,user,false);
            if (mr == null)
            {
                return null;
            }
            
            return new XnatMrsessiondata(mr);
        } catch (Exception e) {
            logger.error("",e);
            return null;
        }
    }
//    
//    public String getCurrentArchiveFolder() throws org.nrg.xnat.exceptions.UndefinedArchive,org.nrg.xnat.exceptions.InvalidArchiveStructure,IOException{
//        String arcpath = XFT.GetArchiveRootPath();
//        if (arcpath==null || arcpath.equals("")){
//            throw new UndefinedArchive();
//        }
//       
//        File f = new File(arcpath);
//        
//        if (!f.exists()){
//            f.mkdir();
//        }
//        
//        String curA =System.getProperty("CURRENT_ARC");
//        if (curA ==null)
//            curA=System.getenv("CURRENT_ARC");
//        
//        //Map m = System.getenv();
//        if (curA!=null)
//        {
//            if (!curA.endsWith("\\") && !curA.endsWith("/")){
//                curA += File.separator;
//            }
//            
//            if (FileUtils.IsAbsolutePath(curA))
//            {
//                File currentArc = new File(curA);
//                if (!currentArc.exists()){
//                    currentArc.mkdirs();
//                }
//                            
//                int index = curA.indexOf(f.getName());
//                if (index ==-1 )
//                {
//                    throw new org.nrg.xnat.exceptions.InvalidArchiveStructure(f.getName() + " does not exist in " + curA);
//                }else{
//                    curA = curA.substring(index + f.getName().length() + 1);
//                    
//                    return curA;
//                }
//            }else{
//                File currentArc = new File(arcpath + curA);
//                if (!currentArc.exists()){
//                    currentArc.mkdirs();
//                }
//                            
//                return curA;
//            }
//        }else{
//            return null;
//        }
//    }
//    
//    public static String GetCurrentArchiveFolder() throws org.nrg.xnat.exceptions.UndefinedArchive,org.nrg.xnat.exceptions.InvalidArchiveStructure,IOException{
//        return GetInstance().getCurrentArchiveFolder();
//    }
    

    public static void populateCatalogBean(CatCatalogBean cat, String header,File f){
    	if (f.isDirectory()){
    		if (f.listFiles()!=null && f.listFiles().length>0)
    		for (File child : f.listFiles()){
    			populateCatalogBean(cat, header + f.getName() + "/", child);
    		}
    	}else{
    		CatEntryBean entry = new CatEntryBean();
    		entry.setUri(header + f.getName());
    		cat.addEntries_entry(entry);
    	}
    }
    
    public static CatalogSet getCatalogBean(RunData data,ItemI input){
        XnatProjectdata project = null;
        ItemI thisOM=null;
        XFTItem item=null;
        
        if (input instanceof XFTItem){
            thisOM = BaseElement.GetGeneratedItem(input);
            item = (XFTItem)input;
        }else{
            thisOM = input;
            item = input.getItem();
        }
        CatalogSet catalog_set = null;

        final String server = TurbineUtils.GetFullServerPath();
        
        final String url = server + "/app/template/GetFile.vm/search_element/" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_element",data)) + "/search_field/" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_field",data)) + "/search_value/" + ((String)org.nrg.xdat.turbine.utils.TurbineUtils.GetPassedParameter("search_value",data));
        
        try {
            Class c = thisOM.getClass();
            Class[] pClasses = new Class[]{String.class};
            Method m= c.getMethod("getCatalogBean", pClasses);
            if(m!=null){
                Object [] objects = new Object[]{url};
                catalog_set = (CatalogSet)m.invoke(thisOM, objects);
            }
        } catch (IllegalArgumentException e) {
            logger.error("",e);
        } catch (IllegalAccessException e) {
            logger.error("",e);
        }  catch (InvocationTargetException e) {
            logger.error("",e);
        } catch (NoSuchMethodException e) {
            logger.error("",e);
        }
        
        if (catalog_set!=null){
            return catalog_set;
        }
        
        if (thisOM instanceof XnatExperimentdata){
            project = ((XnatExperimentdata)thisOM).getPrimaryProject(false);
        }else if(thisOM instanceof XnatSubjectdata){
            project = ((XnatSubjectdata)thisOM).getPrimaryProject(false);
        }else if(thisOM instanceof XnatProjectdata){
            project = ((XnatProjectdata)thisOM);
        }
        List<XFTItem> hash = (item).getChildrenOfType("xnat:abstractResource");
        
        CatCatalogBean catalog = new CatCatalogBean();
        Hashtable<String,Object> fileMap = new Hashtable<String,Object>();
                
        int counter = 0;
        
        catalog.setId(((XFTItem)item).getPK().toString());
        if (project!=null){
            for (XFTItem resource : hash){
                ItemI om = BaseElement.GetGeneratedItem(resource);
                if (om instanceof XnatAbstractresource){
                    XnatAbstractresource resourceA = (XnatAbstractresource)om;
                    ArrayList<File> files = resourceA.getCorrespondingFiles(project.getRootArchivePath());
                    for (int i=0;i<files.size();i++){
                        File f = files.get(i);
                        //String xPath= item.getXSIType() + "[" + ((XFTItem)item).getPKString() + "]/" + key;
                        //xPath = xPath.replace('/', '.');
                        
                        CatEntryBean entry = new CatEntryBean();
                        entry.setUri(url + "/file/" + counter);
                        
                        fileMap.put("/file/" + counter++, f);
                        
                        String path = f.getAbsolutePath();
                        if (path.indexOf(File.separator + project.getId())!=-1){
                            path = path.substring(path.indexOf(File.separator + project.getId()) + 1);
                        }else{
                            if (path.indexOf(File.separator + ((XFTItem)item).getPK())!=-1){
                                path = path.substring(path.indexOf(File.separator + ((XFTItem)item).getPK()) + 1);
                            }
                        }
                        
                        entry.setCachepath(path);
                        entry.setName(f.getName());
                        
                        CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                        meta.setMetafield(path);
                        meta.setName("RELATIVE_PATH");
                        entry.addMetafields_metafield(meta);
                        
                        meta = new CatEntryMetafieldBean();
                        meta.setMetafield(new Long(f.length()).toString());
                        meta.setName("SIZE");
                        entry.addMetafields_metafield(meta);

                        catalog.addEntries_entry(entry);
                        
                    }
                    if (om instanceof XnatResourcecatalog){
                        File f = ((XnatResourcecatalog)om).getCatalogFile(project.getRootArchivePath());
                        CatEntryBean entry = new CatEntryBean();
                        
                        entry.setUri(url + "/file/" + counter);
                        
                        fileMap.put("/file/" + counter++, f);
                        
                        String path = f.getAbsolutePath();
                        if (path.indexOf(File.separator + project.getId())!=-1){
                            path = path.substring(path.indexOf(File.separator + project.getId()) + 1);
                        }else{
                            if (path.indexOf(File.separator + ((XFTItem)item).getPK())!=-1){
                                path = path.substring(path.indexOf(File.separator + ((XFTItem)item).getPK()) + 1);
                            }
                        }
                        
                        entry.setCachepath(path);
                        entry.setName(f.getName());
                        
                        CatEntryMetafieldBean meta = new CatEntryMetafieldBean();
                        meta.setMetafield(path);
                        meta.setName("RELATIVE_PATH");
                        entry.addMetafields_metafield(meta);
                        
                        meta = new CatEntryMetafieldBean();
                        meta.setMetafield(new Long(f.length()).toString());
                        meta.setName("SIZE");
                        entry.addMetafields_metafield(meta);

                        catalog.addEntries_entry(entry);
                    }
                    
                }
            }
        }
        
        return new CatalogSet(catalog,fileMap);
    }


    protected static File GetFileOnLocalFileSystem(String fullPath) {
        File f = new File(fullPath);
        if (!f.exists()){
            if (!fullPath.endsWith(".gz")){
            	f= new File(fullPath + ".gz");
            	if (!f.exists()){
            		return null;
            	}
            }else{
                return null;
            }
        }
        
        return f;
    }
    
    public static boolean isNull(String s){
    	if(s==null){
    		return true;
    	}else if(s.equals("NULL")){
    		return true;
    	}else{
    		return false;
}
    }
    
    public static boolean hasValue(String s){
    	if(isNull(s)){
    		return false;
    	}else{
    		if(StringUtils.isEmpty(s)){
    			return false;
    		}
    	}
    	
    	return true;
    }
	
    public static Object getFirstOf(final Iterator<?> i) {
		while (i.hasNext()) {
			final Object o = i.next();
			if (null != o) {
				return o;
			}
		}
		return null;
	}
	
    public static Object getFirstOf(final MultiMap m, final Object key) {
		final Collection<?> vals = (Collection<?>)m.get(key);
		return null == vals ? null : getFirstOf(vals.iterator());
	}
	
	public static boolean isNullOrEmpty(final String s) {
		return null == s || "".equals(s);
	}
}
