/*
 * web: org.nrg.xnat.turbine.modules.actions.LoadImageData
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.actions;

import org.apache.log4j.Logger;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.turbine.modules.actions.SecureAction;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase;
import org.nrg.xnat.turbine.utils.XNATSessionPopulater;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
public class LoadImageData extends SecureAction {
    private final static String PREARC_PAGE = "XDATScreen_prearchives.vm";
    
//    private final static class SessionTypeParams {
//	int loads = 0;
//	final String tag;
//	final String viewTemplate;
//	
//	SessionTypeParams(final String tag, final String viewTemplate) {
//	    this.tag = tag;
//	    this.viewTemplate = viewTemplate;
//	}
//	
//	String makeTag() {
//	    return tag + loads;
//	}
//    }
//    
//    private final static Map<String,SessionTypeParams> SESSION_TYPES = new HashMap<String,SessionTypeParams>();
//    static {
//	SESSION_TYPES.put("xnat:mrsessiondata", new SessionTypeParams("MR",
//								"XDATScreen_dcm_xnat_mrSessionData.vm"));
//	SESSION_TYPES.put("xnat:ctsessiondata", new SessionTypeParams("CT",
//								"XDATScreen_dcm_xnat_ctSessionData.vm"));
//	SESSION_TYPES.put("xnat:petsessiondata", new SessionTypeParams("PET",
//								"XDATScreen_ecat_xnat_petSessionData.vm")); 
//    }
    
    private final org.apache.log4j.Logger logger = Logger.getLogger(LoadImageData.class);

    public XnatImagesessiondata getSession(UserI user, File xml, String project,boolean nullifySubject) throws IOException,SAXException{
    	return (new XNATSessionPopulater(user, xml, project, nullifySubject)).populate();
    }
    
    /* (non-Javadoc)
     * @see org.apache.turbine.modules.actions.VelocityAction#doPerform(org.apache.turbine.util.RunData, org.apache.velocity.context.Context)
     */
    public void doPerform(final RunData data, final Context context) throws Exception {
        final String folder = (String)TurbineUtils.GetPassedParameter("folder",data);
        String root = (String)TurbineUtils.GetPassedParameter("root",data);
        if(root==null){
        	root=(String)TurbineUtils.GetPassedParameter("timestamp",data);
        }
        final UserI user = TurbineUtils.getUser(data);
        
        final String project = (String)TurbineUtils.GetPassedParameter("project",data);	// can we final this?
        if (null == folder || null == root || null == project) {
            data.setMessage("Unknown folder: " + folder);
            data.setScreenTemplate(PREARC_PAGE);
            return;
        }

        String prearchive_path= PrearcDatabase.projectPath(project);
        //LOAD FOLDER
        final File dir = new File("NONE".equals(root) ? prearchive_path : (Paths.get(prearchive_path, root).toString()));

        final Collection<String> folders;
        if (dir.exists()) {
            folders = new HashSet<String>(Arrays.asList(dir.list()));
        } else {
            folders = new ArrayList<String>(0);
        }
        
        final File xml;
        if (folders.contains(folder)) {
            final File sessdir = new File(dir, folder);
            assert(sessdir.exists());
            xml = new File(sessdir.getAbsolutePath() + ".xml");
        } else {
            data.setMessage("Unknown folder: " + folder);
            data.setScreenTemplate(PREARC_PAGE);
            return;
        }
        
        if (!xml.canRead()) {
            logger.error("Unable to load xml document.");
            data.setMessage("Unable to load xml document.");
            data.setScreenTemplate(PREARC_PAGE);
            return;
        }
        
        XnatImagesessiondata imageSessionData = this.getSession(TurbineUtils.getUser(data), xml,project,false);                     

            final String tag = root+"_"+folder;
            data.getSession().setAttribute(tag, imageSessionData);
            data.getParameters().add("tag", tag);
        data.getParameters().add("src","/prearchive/projects/"+ project+"/"+ root +"/"+ folder);
            data.setScreenTemplate("XDATScreen_uploaded_xnat_imageSessionData.vm");
    }

}
