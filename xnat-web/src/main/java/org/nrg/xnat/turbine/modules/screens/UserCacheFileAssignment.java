/*
 * web: org.nrg.xnat.turbine.modules.screens.UserCacheFileAssignment
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.turbine.modules.screens.SecureReport;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.utils.UserUtils;

import java.io.File;
import java.util.List;

public class UserCacheFileAssignment extends SecureReport {

	@Override
	public void finalProcessing(RunData data, Context context) {
		XnatExperimentdata expt=((XnatExperimentdata)om);
		String userPath = org.nrg.xdat.security.helpers.Users.getUserCacheUploadsPath(TurbineUtils.getUser(data));
		File dir = new File (userPath,expt.getId());
		
		JSONObject parent= new JSONObject();
		try {
			parent.put("type", "text");
			parent.put("label", "Uploads");
		} catch (JSONException e) {
			logger.error("",e);
		}
		
		for(File f: dir.listFiles()){
			convertDirToJSON(parent,f,"/");
		}
		context.put("srcFiles",parent);
		
		
		try {
			context.put("destFiles",convertOMtoJSON(expt,expt.getPrimaryProject(false)));
		} catch (JSONException e) {
			logger.error("",e);
		}
		
		context.put("user_path", String.format("/data/user/cache/resources/%s/files",expt.getId()));
		
//		List<PathInfo> paths=Lists.newArrayList();
//		
//		List<File> fileList = Lists.newArrayList();
//		fileList.addAll(FileUtils.listFiles(dir,null,true));
//		for(File f:fileList){
//			String path=UserCacheResource.constructPath(f).substring(1);
//			
//			paths.add(new PathInfo(path,f.length()));
//		}
//		
//		context.put("files", paths);
		
	}
	
	public JSONObject convertOMtoJSON(XnatExperimentdata expt,XnatProjectdata proj) throws JSONException{
		final JSONObject dest=createTextNode(expt.getIdentifier(expt.getProject()));
		
		final List<XnatAbstractresource> res=expt.getResources_resource();
		if(!res.isEmpty()){
			final JSONObject r1=createTextNode("Session Resources");
			addNode(dest,r1);
			for(final XnatAbstractresource abst:res){
				final JSONObject r=createTextNode(abst.getLabel());
				r.put("dest", String.format("/data/archive/experiments/%s/resources/%s/files",expt.getId(),abst.getXnatAbstractresourceId()));
				r.put("labelStyle", "icon-of");
				addNode(r1,r);
				final List<File> files=abst.getCorrespondingFiles(proj.getRootArchivePath());
				if(files.size()>0){
					r.put("file_count",files.size());
				}
			}
		}
		
		if(expt instanceof XnatImagesessiondata){
			final XnatImagesessiondata session=(XnatImagesessiondata)expt;
			
			final List<XnatImagescandataI> scans=session.getScans_scan();
			if(scans.size()>0){
				final JSONObject s1=createTextNode("Scans");
				addNode(dest,s1);
				
				for(final XnatImagescandataI scan:scans){
					final JSONObject s=createTextNode(scan.getId());
					s.put("scan_type",scan.getType());
					s.put("condition", scan.getCondition());
					
					addNode(s1,s);

					final List<XnatAbstractresource> s_ress=scan.getFile();
					for(final XnatAbstractresource abst:s_ress){
						final JSONObject s_res=createTextNode(abst.getLabel());
						s_res.put("dest", String.format("/data/archive/experiments/%s/scans/%s/resources/%s/files",expt.getId(),scan.getId(),abst.getXnatAbstractresourceId()));
						addNode(s,s_res);
						
						s_res.put("labelStyle", "icon-of");
						
						final List<File> files=abst.getCorrespondingFiles(proj.getRootArchivePath());
						if(files.size()>0){
							s_res.put("file_count",files.size());
						}
					}
				}
			}
		}
		
		return dest;
	}
	
	public static JSONObject createTextNode(String lbl) throws JSONException{
		JSONObject o=new JSONObject();
		o.put("type", "text");
		o.put("label", lbl);
		return o;
	}
	
	public static void addNode(JSONObject parent,JSONObject child) throws JSONException{
		JSONArray children;
		try {
			children = parent.getJSONArray("children");
		} catch (JSONException e) {
			children=new JSONArray();
			parent.put("children", children);	
		}
		
		children.put(child);
		
		try {
			parent.get("expanded");
		} catch (JSONException e) {
			parent.put("expanded", true);
		}
		
	}
	
	public void convertDirToJSON(JSONObject parent,File f,String header){
		if(f.isDirectory()&& !FileUtils.HasFiles(f)){
			return;
		}
		
		try {			
			JSONObject o=createTextNode(f.getName());
			o.put("fpath", header+f.getName());
			addNode(parent,o);
			
			if(f.isDirectory()){
				o.put("labelStyle", "icon-of");
				for(final File c:f.listFiles()){
					convertDirToJSON(o, c,header+f.getName()+"/");
				}
			}else{
				//file
			}
		} catch (JSONException e) {
			logger.error("",e);
		}
		
	}
	
//	public class PathInfo{
//		public String getPath() {
//			return path;
//		}
//		public long getSize() {
//			return size;
//		}
//		final String path;
//		final long size;
//		public PathInfo(String path,long size){
//			this.path=path;
//			this.size=size;
//		}
//		
//		public String toString(){
//			return path + ":" +size;
//		}
//		
//	}
}
