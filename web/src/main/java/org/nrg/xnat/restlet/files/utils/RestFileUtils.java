/*
 * web: org.nrg.xnat.restlet.files.utils.RestFileUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.restlet.files.utils;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class RestFileUtils {

	public static String getRelativePath(String p,Map<String,String> _tokens){
		int i=-1;
		String _token=null;
		
		//replace by id
		for(Map.Entry<String,String> token:_tokens.entrySet()){
			_token=token.getKey();
			i=p.indexOf('/'+ _token + '/');
			if(i==-1){
				i=p.indexOf('/'+ _token);
				
				if(i==-1){
					i=p.indexOf(_token+'/');
					if(i>-1){
						i=(p.substring(0, i)).lastIndexOf('/') +1;
						break;
					}
				}else{
					i++;
					break;
				}
			}else{
				i++;
				break;
			}
		}
		
		//replace by label, only looks for exact matches.
		if(i==-1){
			for(Map.Entry<String,String> token:_tokens.entrySet()){
				_token=token.getValue();
				i=p.indexOf('/'+ _token + '/');
				if(i>-1){
					i++;
					break;
				}
			}
		}
		
		if(i==-1){
			if(p.indexOf(":")>-1){
				p=p.substring(p.indexOf(":"));
				p=p.substring(p.indexOf("/"));
				p=_token+p;
			}else{
				p=_token+p;
			}
		}else{
			p=p.substring(i);
		}
		
		for(Map.Entry<String,String> entry:_tokens.entrySet()){
			p=StringUtils.replace(p, entry.getKey(), entry.getValue());
		}
		
		return p;
	}

	public static String replaceResourceLabel(String path,Object id,String label){
		if(StringUtils.isEmpty(label) || id==null){
			return path;
		}else{
			int i=path.indexOf('/'+id.toString() +"/files/");
			if(i>-1){
				return StringUtils.replace(path, '/'+id.toString() +"/files/", '/'+label +"/files/");
			}else{
				return path;
			}
		}
	}

	public static String replaceInPath(String path,Object id,String newValue){
		if(StringUtils.isEmpty(newValue) || id==null){
			return path;
		}else{
			int i=path.indexOf('/'+id.toString() +'/');
			if(i>-1){
				return StringUtils.replace(path, '/'+id.toString() +'/', '/'+newValue +'/');
			}else{
				return path;
			}
		}
	}

	public static Map<String,String> getReMaps(List<XnatImagescandata> scans, List<XnatReconstructedimagedata> recons){
		final Map<String,String> valuesToReplaceInPath=new Hashtable<String,String>();
		
		if(scans!=null){
			for(final XnatImagescandata scan:scans){
				if(!StringUtils.isEmpty(scan.getType())){
					valuesToReplaceInPath.put(scan.getId(), scan.getId()+'-' + scan.getType().replaceAll( "\\W", "_").replaceAll(" ", "_"));
				}
			}
		}
		
		if(recons!=null){
			for(final XnatReconstructedimagedata scan:recons){
				if(!StringUtils.isEmpty(scan.getType())){
					valuesToReplaceInPath.put(scan.getId(), scan.getId()+'-' + scan.getType().replaceAll( "\\W", "_").replaceAll(" ", "_"));
				}
			}
		}
		
		return valuesToReplaceInPath;
	}
	
	public static String buildRelativePath(String uri,Map<String,String> session_mapping, Map<String,String> valuesToReplace, Object resourceID, String resourceLabel){
		String relative = RestFileUtils.getRelativePath(uri.replace('\\', '/'), session_mapping);
        
        relative=RestFileUtils.replaceResourceLabel(relative, resourceID, resourceLabel);
        
        for(Map.Entry<String, String> e:valuesToReplace.entrySet()){
            relative=RestFileUtils.replaceInPath(relative, e.getKey(), e.getValue());
        }
        
        return relative;
	}
	
	public static Map<String,String> getSessionMaps(List<XnatExperimentdata> assesseds,List<XnatExperimentdata> expts,XnatSubjectdata sub, XnatProjectdata proj){
		Map<String,String> session_ids=new Hashtable<String,String>();
		if(assesseds.size()>0){
			for(XnatExperimentdata session:assesseds){
				session_ids.put(session.getId(),session.getArchiveDirectoryName());
			}
		}else if(expts.size()>0){
			for(XnatExperimentdata session:expts){
				session_ids.put(session.getId(),session.getArchiveDirectoryName());
			}
		}else if(sub!=null){
			session_ids.put(sub.getId(),sub.getArchiveDirectoryName());
		}else if(proj!=null){
			session_ids.put(proj.getId(),proj.getId());
		}
		
		return session_ids;
	}
	
	// Uploading directories via linux (and likely Mac) will not fail due to "Everything is a file".  This is an initial
	// implementation of a check of files to see if they might be uploaded "directories".  These file representations directories
	// seem to be of a specific size and basically full of zero bytes.  It's possible this check could/should be improved over time.
	public static boolean isFileRepresentationOfDirectoryOrEmpty(File fl) {
		final long len = fl.length();
		// Is this the best check?
		if (len < 1024 || (len < (128*1024) && len%1024 == 0)) {
			try {
				final FileInputStream fis = new FileInputStream(fl);
				byte[] b = new byte[1024];
				while (fis.read(b)!=-1) {
					for (int i=0; i<b.length; i++) {
						if (b[i] != 0) {
							fis.close();
							return false;
						}
					}
				}
				fis.close();
				return true;
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
		}
		return false;
	}

}
