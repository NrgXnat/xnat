/*
 * org.nrg.xnat.turbine.modules.screens.XDATScreen_MyXNAT
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.turbine.modules.screens.SecureScreen;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;

public class XDATScreen_MyXNAT extends SecureScreen {
	
    protected void doBuildTemplate(RunData data, Context context) throws Exception {
    	/*String folder = org.nrg.xnat.turbine.utils.ArcSpecManager.GetInstance().getGlobalCachePath() +  TurbineUtils.getUser(data).getLogin();
    	File userFolder = new File(folder);
    	Map folderListing  = new Hashtable();
    	Long count =new Long(0);
    	if (userFolder.exists()) {
    		File[] children = userFolder.listFiles();
    		if (children != null) {
    			for (int i=0; i<children.length; i++) {
        				String childPath = children[i].getName();
        				ArrayList folderDetails = new ArrayList();
        				List<File> subFolders = new ArrayList<File>();

        				//0th location: Folder Name 
        				//1st Location: Size of the folder in MB
        				//2nd location: Display name
        				//3rd Location: Arraylist of subfolders in this folder

        				folderDetails.add(childPath);
        				folderDetails.add(size(children[i]));
        				Long orderBy=null;
	    	                try {
	    	                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
	    	                    Date d = sdf.parse(children[i].getName());
	    	                    orderBy = d.getTime();
	    	                    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat ("MM/dd/yyyy HH:mm:ss");
	    	                    folderDetails.add(formatter.format(d));
	    	                } catch (Exception e) {
	    	                    //logger.error("",e);
	    	                    folderDetails.add(children[i].getName());
	    	                }
	    	                if (orderBy==null)
	                            orderBy = count++;
	    	                
	    					if (!children[i].isDirectory()) {
	    						subFolders.add(children[i]);
	    					}else {
		        				File[] files=children[i].listFiles();
		        				subFolders =  Arrays.asList(files);
	    					}

	    	                folderDetails.add(subFolders);
	    	                folderListing.put(orderBy, folderDetails);
	    			}
    			}
    		}
		TreeMap sort = new TreeMap(folderListing);
        List folders =new ArrayList();
        folders.addAll(sort.values());
        Collections.reverse(folders);
        context.put("folders",folders);*/
    	try {
            UserI item = TurbineUtils.getUser(data);
            if (item == null)
            {
                data.setMessage("Invalid Search Parameters: No Data Item Found.");
                data.setScreen("Index");
                TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
            } else{
                try {
                    context.put("item",item);
                } catch (Exception e) {
                    e.printStackTrace();
                    data.setMessage("Invalid Search Parameters: No Data Item Found.");
                    data.setScreen("Index");
                    TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
                }
            }
    	} catch (Exception e) {
            e.printStackTrace();
            data.setMessage("Invalid Search Parameters: No Data Item Found.");
            data.setScreen("Index");
            TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
        }

    }
    
    /*private long size(File file) {
    	if (isSoftLink(file)) return 0;
    	if (file.isFile())
    	    return bytesToMeg(file.length());
    	  File[] files = file.listFiles();
    	  long size = 0;
    	  if (files != null) {
    	    for (int i = 0; i < files.length; i++)
    	      size += size(files[i]);
    	  }
    	  return size;
   }*/

    /*private boolean isSoftLink(File f) {
    	boolean rtn = false;
    	try {
	    	if (!f.getCanonicalFile().equals(f.getAbsoluteFile())) {
	    		rtn = true;
	    	}
    	}catch(Exception e) {}
    	return rtn;
    }*/
    
	//private static final long  MEGABYTE = 1024L * 1024L;

	/*private long bytesToMeg(long bytes) {
  		return bytes / MEGABYTE ;
 	}*/

    
    static org.apache.log4j.Logger logger = Logger.getLogger(XDATScreen_MyXNAT.class);

}
