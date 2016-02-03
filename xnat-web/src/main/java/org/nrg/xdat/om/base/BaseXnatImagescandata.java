/*
 * org.nrg.xdat.om.base.BaseXnatImagescandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 12/13/13 1:12 PM
 */
package org.nrg.xdat.om.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.nrg.xdat.model.CatCatalogI;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatQcscandataI;
import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatDicomseries;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.XnatResourceseries;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xdat.om.base.auto.AutoXnatImagescandata;
import org.nrg.xdat.security.helpers.Permissions;
import org.nrg.xft.ItemI;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xft.utils.StringUtils;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.scanType.ImageScanTypeMapping;
import org.nrg.xnat.helpers.scanType.ScanTypeMappingI;
import org.nrg.xnat.utils.CatalogUtils;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatImagescandata extends AutoXnatImagescandata {

	public BaseXnatImagescandata(ItemI item) {
		super(item);
	}

	public BaseXnatImagescandata(UserI user) {
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatImagescandata(UserI user)
	 **/
	public BaseXnatImagescandata()
	{}

	public BaseXnatImagescandata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public String getPreCount()
    {
        try {
            String temp = this.getId();
            if (temp != null)
            {
                if (temp.contains("-"))
                {
                    return temp.substring(0,temp.indexOf("-"));
                }else{
                    return temp;
                }
            }else{
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public String getPostCount()
    {
        try {
            String temp = this.getId();
            if (temp != null)
            {
                if (temp.contains("-"))
                {
                    return temp.substring(temp.indexOf("-")+1);
                }else{
                    return "";
                }
            }else{
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }

    public Integer getPreCountI()
    {
        String s = getPreCount();
        try{
            if (s.equals(""))
            {
                return 0;
            }else{
                return new Integer(s);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Integer getPostCountI()
    {
        String s = getPostCount();
        try {
            if (s.equals(""))
            {
                return 0;
            }else{
                return new Integer(s);
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isInRAWDirectory(){
        boolean hasRAW=false;
        for (XnatAbstractresourceI xnatAbstractresourceI : getFile()) {
            XnatAbstractresource file = (XnatAbstractresource) xnatAbstractresourceI;
            if (file.isInRAWDirectory()) {
                hasRAW=true;
                break;
            }
        }
        return hasRAW;
    }




    public void deleteFilesFromFileSystem(String rootPath,UserI u, EventMetaI ci) throws Exception{
        List<XnatAbstractresourceI> files = this.getFile();
        if (!files.isEmpty())
        for (XnatAbstractresourceI resource:files){
        	((XnatAbstractresource)resource).deleteWithBackup(rootPath, u,ci);
        }
    }

    public ArrayList<java.io.File> getJavaFiles(String rootPath){
        ArrayList<File> jFiles = new ArrayList<File>();
        List scanFiles= this.getFile();
        if (scanFiles.size()>0)
        {
            for (Object scanFile : scanFiles) {
                XnatAbstractresource xnatFile = (XnatAbstractresource) scanFile;
                jFiles.addAll(xnatFile.getCorrespondingFiles(rootPath));
            }
        }
        return jFiles;
    }

    public static Comparator GetComparator() {
        return (new BaseXnatImagescandata()).getComparator();
    }

    public Comparator getComparator()
    {
        return new ImageScanComparator();
    }
    
    public class ImageScanComparator implements Comparator{
        public ImageScanComparator() {}

        public int compare(Object o1, Object o2) {
            BaseXnatImagescandata  value1 = (BaseXnatImagescandata)(o1);
            BaseXnatImagescandata value2 = (BaseXnatImagescandata)(o2);

            if (value1 == null){
                if (value2 == null)
                {
                    return 0;
                }else{
                    return -1;
                }
	    }
            if (value2== null) {
		return 1;
	    }

            if (value1.getPreCountI().equals(value2.getPreCountI())) {
                return value1.getPostCountI().compareTo(value2.getPostCountI());
            }else{
                Integer i1 = value1.getPreCountI();
                Integer i2 = value2.getPreCountI();
                return i1.compareTo(i2);
            }
        }
    }
    private String scan_dir = null;
    public String deriveScanDir(){
        if (scan_dir == null) {
            String rootPath;
			try {
				rootPath = this.getImageSessionData().getArchiveRootPath();
			} catch (UnknownPrimaryProjectException e) {
				rootPath=null;
			}
            String last_dir = null;
            for (XnatAbstractresourceI xnatFile : this.getFile()) {
                    if (xnatFile instanceof org.nrg.xdat.om.XnatResource){
                        XnatResource resource = (XnatResource)xnatFile;
                        String uri =resource.getFullPath(rootPath);
                        if (last_dir==null){
                            last_dir= uri;
                            int index = last_dir.toUpperCase().indexOf("/RAW/");
                            if (index!=-1){
                            	if(last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)>-1){
                                    scan_dir = last_dir.substring(0,last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)+this.getId().length()+1);
                            	} else {
                            		scan_dir = last_dir.substring(0,index+4);
                            	}
                                return scan_dir;
                            }else{
                            	index = last_dir.toUpperCase().indexOf("/SCANS/");
                                if (index!=-1){
                                    if(last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)>-1){
                                        scan_dir = last_dir.substring(0,last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)+this.getId().length()+1);
                                	}else{
                                		scan_dir = last_dir.substring(0,index+6);
                                	}
                                    return scan_dir;
                                }
                            }
                        }
                    }else if(xnatFile instanceof org.nrg.xdat.om.XnatDicomseries){
                        XnatDicomseries resource = (XnatDicomseries)xnatFile;
                        String uri =resource.getFullPath(rootPath);
                        if (last_dir==null){
                            last_dir= uri;
                            int index = last_dir.toUpperCase().indexOf("/RAW/");
                            if (index!=-1){
                            	if(last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)>-1){
                                    scan_dir = last_dir.substring(0,last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)+this.getId().length()+1);
                            	}else{
                            		scan_dir = last_dir.substring(0,index+4);
                            	}
                                return scan_dir;
                            }else{
                            	index = last_dir.toUpperCase().indexOf("/SCANS/");
                                if (index!=-1){
                                    if(last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)>-1){
                                        scan_dir = last_dir.substring(0,last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)+this.getId().length()+1);
                                	}else{
                                		scan_dir = last_dir.substring(0,index+6);
                                	}
                                    return scan_dir;
                                }
                            }
                        }
                    }else if(xnatFile instanceof org.nrg.xdat.om.XnatResourceseries){
                        XnatResourceseries resource = (XnatResourceseries)xnatFile;
                        String uri =resource.getFullPath(rootPath);
                        if (last_dir==null){
                            last_dir= uri;
                            int index = last_dir.toUpperCase().indexOf("/RAW/");
                            if (index!=-1){
                            	if(last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)>-1){
                                    scan_dir = last_dir.substring(0,last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)+this.getId().length()+1);
                            	}else{
                            		scan_dir = last_dir.substring(0,index+4);
                            	}
                                return scan_dir;
                            }else{
                            	index = last_dir.toUpperCase().indexOf("/SCANS/");
                                if (index!=-1){
                                    if(last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)>-1){
                                        scan_dir = last_dir.substring(0,last_dir.toUpperCase().indexOf("/"+this.getId().toUpperCase()+"/",index)+this.getId().length()+1);
                                	}else{
                                		scan_dir = last_dir.substring(0,index+6);
                                	}
                                    return scan_dir;
                                }
                            }
                        }
                    }
	    }

            if (scan_dir == null) {
                scan_dir = this.getImageSessionData().deriveRawDir();
	}
    }


        return scan_dir;
    }

    private XnatImagesessiondata mr = null;

    public XnatImagesessiondata getImageSessionData()
    {
        if (mr==null)
        {
            ArrayList al = XnatImagesessiondata.getXnatImagesessiondatasByField("xnat:imageSessionData/ID",this.getImageSessionId(),this.getUser(),false);
            if (al.size()>0)
            {
                mr = (XnatImagesessiondata)al.get(0);
            }
        }

        return mr;
    }
    
    public void setImageSessionData(XnatImagesessiondata ses){
    	mr=ses;
    }
    
    Snapshot snapshot=null;
    public Snapshot getSnapshot(){
    	if(snapshot!=null){
    		return snapshot;
    	}
    	
		if(this.getFile().size()>0){
			snapshot=new Snapshot();
		    XnatImagesessiondata ses=this.getImageSessionData();
		    String label=ses.getIdentifier(ses.getProject(),false);
		    String labelImg=label + "_"+this.getId() + "_qc";

		    String path=null;
		    for(XnatAbstractresourceI res: this.getFile()){
				if(res.getLabel()!=null && res.getLabel().equalsIgnoreCase("SNAPSHOTS")){
				    path="/data/experiments/"+ses.getId() + "/scans/"+this.getId() + "/resources/SNAPSHOTS/files/";
				    if(res instanceof XnatResourcecatalog){
						CatCatalogI cat=((XnatResourcecatalog)res).getCleanCatalog(ses.getProjectData().getRootArchivePath(), false,null,null);
						for(CatEntryI entry:cat.getEntries_entry()){
							if(entry.getContent().equalsIgnoreCase("THUMBNAIL")){
							    snapshot.setThumbnail(path + entry.getUri());
							}
							if(entry.getContent().equalsIgnoreCase("ORIGINAL")){
							    snapshot.setFull(path + entry.getUri());
							}
						}
				    }else{
					    snapshot.setFull(path + labelImg + ".gif");
					    snapshot.setThumbnail(path + labelImg + "_t.gif");
				    }
				} 
		    }
	
		    if(path==null){
		    	path="/app/template/QualityControl.vm?project=" + ses.getProject() + "&session_label=" + label + "&session_id=" + ses.getId() + "&scan="+ this.getId();
		    	snapshot.setFull(path + "&extension=.gif");
		    	snapshot.setThumbnail(path + "&extension=_t.gif");
			}
		    
		    return snapshot;
		}else{
		    return null;
		}
    }
    
    public class Snapshot{
	private String thumbnail=null;
	private String full=null;
	
	public String getFull() {
	    return full;
	}
	public void setFull(String full) {
	    this.full = full;
	}
	public String getThumbnail() {
	    return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
	    this.thumbnail = thumbnail;
	}
	
	
    }
    
    public void copyValuesFrom(final XnatImagescandata other) throws Exception {
    	if (null != other.getType())
		    this.setType(other.getType());
		if (null != other.getQuality())
			this.setQuality(other.getQuality());
		if (null != other.getNote())
			this.setNote(other.getNote());
	}

	public XnatQcscandataI getManualQC() {
		final XnatImagesessiondata session = getImageSessionData();
		if (session.getManualQC() != null && session.getManualQC().getScans_scan() != null) {
			for (XnatQcscandataI qc : session.getManualQC().getScans_scan()) {
				if (getId().equals(qc.getImagescanId())) {
					return qc;
				}
			}
		}
		return null;
	}

	public File getExpectedSessionDir() throws InvalidArchiveStructure, UnknownPrimaryProjectException{
		return this.getImageSessionData().getExpectedSessionDir();
	}

	@Override
	public void preSave() throws Exception{
		super.preSave();

		if(this.getImageSessionData()==null){
			throw new Exception("Unable to identify image session for:" + this.getImageSessionId());
		}

		final String expectedPath=this.getExpectedSessionDir().getAbsolutePath().replace('\\', '/');
		
		validate(expectedPath);
	}
	
	public void validate(String expectedPath) throws Exception{
		
		if(StringUtils.IsEmpty(this.getId())){
			throw new IllegalArgumentException();
		}	
		
		if(!StringUtils.IsAlphaNumericUnderscore(getId())){
			throw new IllegalArgumentException("Identifiers cannot use special characters.");
		}
		
		for(final XnatAbstractresourceI res: this.getFile()){
			final String uri;
			if(res instanceof XnatResource){
				uri=((XnatResource)res).getUri();
			}else if(res instanceof XnatResourceseries){
				uri=((XnatResourceseries)res).getPath();
			}else{
				continue;
			}
			
			FileUtils.ValidateUriAgainstRoot(uri,expectedPath,"URI references data outside of the project:" + uri);
		}
	}
	
	
	public static List<XnatImagescandata> getScansByIdORType(final String scanID,
			final XnatImagesessiondata session,UserI user, boolean preLoad) {
		final CriteriaCollection cc = new CriteriaCollection("OR");
		CriteriaCollection subcc = new CriteriaCollection("AND");
		subcc.addClause("xnat:imageScanData/image_session_ID", session.getId());
		if (!(scanID.equals("*") || scanID.equals("ALL")) && !scanID.contains(",")) {
			subcc.addClause("xnat:imageScanData/ID", scanID);
		} else {
			final CriteriaCollection subsubcc = new CriteriaCollection("OR");
			for (final String s : StringUtils.CommaDelimitedStringToArrayList(
					scanID, true)) {
				subsubcc.addClause("xnat:imageScanData/ID", s);
			}
			subcc.add(subsubcc);
		}
		cc.add(subcc);

		subcc = new CriteriaCollection("AND");
		subcc.addClause("xnat:imageScanData/image_session_ID", session.getId());
        if (!(scanID.equals("*") || scanID.equals("ALL")) && !scanID.contains(",")) {
			if (scanID.equals("NULL")) {
				CriteriaCollection subsubcc = new CriteriaCollection("OR");
				subsubcc.addClause("xnat:imageScanData/type", "", " IS NULL ",
						true);
				subsubcc.addClause("xnat:imageScanData/type", "");
				subcc.add(subsubcc);
			} else {
				subcc.addClause("xnat:imageScanData/type", scanID);
			}
		} else {
			CriteriaCollection subsubcc = new CriteriaCollection("OR");
			for (String s : StringUtils.CommaDelimitedStringToArrayList(scanID,
					true)) {
				if (s.equals("NULL")) {
					subsubcc.addClause("xnat:imageScanData/type", "",
							" IS NULL ", true);
					subsubcc.addClause("xnat:imageScanData/type", "");
				} else {
					subsubcc.addClause("xnat:imageScanData/type", s);
				}
			}
			subcc.add(subsubcc);
		}
		cc.add(subcc);

		return XnatImagescandata.getXnatImagescandatasByField(cc, user,
				preLoad);
	}
	
	public ScanTypeMappingI getScanTypeMapping(String project, String dbName){
		return new ImageScanTypeMapping(project, dbName);
	}
	

	Map<String, Number> stats = null;
	
	public Map<String, Number> getPrearchiveFileStats(){
		if(stats==null){
			stats = new HashMap<String, Number>();
			int totalCount = 0;
			long totalSize = 0;
	        for (XnatAbstractresourceI resource : this.getFile()) {
	            String prearcPath = null;
	            try{
	               // Try to retrieve the scan path within the prearchive.
	               prearcPath =  this.getParent().getStringProperty("prearchivepath");
	               if(null == prearcPath || prearcPath.equals("")){
	                  throw new Exception("Unable to determine the prearchive path for scan");
	               }
	            }catch(Exception e){
	               // If the prearchive path is null or empty, count and size should be 0
	               logger.error("Unable to retrieve file counts.", e);
	               continue;
	            }
	               
	            if(resource instanceof XnatResourcecatalogI){
	               // Get the rootPath and FileStats
	               final String rootPath=CatalogUtils.getCatalogFile(prearcPath, ((XnatResourcecatalogI)resource)).getParentFile().getAbsolutePath();
	               CatalogUtils.Stats c_stats=CatalogUtils.getFileStats(CatalogUtils.getCleanCatalog(prearcPath, (XnatResourcecatalogI)resource, false), rootPath);
	               if(null != c_stats){
	                  totalSize += c_stats.size;
	                  totalCount += c_stats.count;
	               }else{
	                  logger.error("Unable to retrieve file counts.", new Exception("Unable to retrieve CatalogUtils.Stats for the given resource."));
	               }
	            }else{
	               logger.error("Unable to retrieve file counts.", new Exception("Prearchive resource is not an instance of XnatResourcecatalogI. Skipping..."));
	            }
	         }
	        stats.put("size", Long.valueOf(totalSize));
	        stats.put("count", Integer.valueOf(totalCount));
		}
        return stats;
	}
	
    public List<String> getReadableFileStats() {
        List<String> stats = new ArrayList<String>();
        
        // If the scan is in the prearchive, retrieve the count & file sizes from CatalogUtils.getFileStats()
        if(this.getImageSessionId() == null){
           Map<String,Number> prearcStats = this.getPrearchiveFileStats();
           stats.add(CatalogUtils.formatFileStats("TOTAL", prearcStats.get("count").intValue(), prearcStats.get("size").longValue()));
        }else{ // If this scan is already archived, retrieve the count & file sizes from the resource
           int totalCount = 0;
           long totalSize = 0;
           for (XnatAbstractresourceI resource : getFile()) {
              // Get the label, if it is null or a snapshot skip it.
              String label = resource.getLabel();
              if (label != null && label.equals("SNAPSHOTS")) {
                 continue;
              }
              
              int count;
              long size;
              Integer rawCount = resource.getFileCount();
              if (rawCount != null) {
                  count = rawCount;
              } else {
                  count = 0;
              }
              Object rawFileSize = resource.getFileSize();
              
              if (rawFileSize != null) {
                 if (rawFileSize instanceof Integer) {
                    size = (Integer) rawFileSize;
                 } else if (rawFileSize instanceof Long) {
                    size = (Long) rawFileSize;
                 } else {
                    size = Long.parseLong(rawFileSize.toString());
                 }
              } else {
                 size = 0;
              }
              totalSize += size;
              totalCount += count;
              stats.add(CatalogUtils.formatFileStats(label, count, size));
           }
           // Add totalCount and totalSize to the stats list
           stats.add(0, CatalogUtils.formatFileStats("TOTAL", totalCount, totalSize));
        }

        return stats;
    }

    /**
     * It's annoying to do this, but in some cases it's easier to get this formatted display directly from the scan
     * object rather than trying to manage it in the Velocity template.
     * @return A formatted string that will display the scans in a tip text format.
     */
    public String getFormattedReadableFileStats(UserI user) throws Exception {
        if (getFile().size() > 0) {
            List<String> stats = getReadableFileStats();
            if(stats == null || stats.size() == 0 || stats.get(0) == null || stats.get(0).equals("") || stats.get(0).equals("0 B in 0 files")) {
                return getEmptyResourceCount(user);
            } else if (stats.size() == 1) {
                return stats.get(0);
            } else {
                return getListAsTipText(stats);
           }
        } else {
            return getEmptyResourceCount(user);
        }
    }

    private String getEmptyResourceCount(UserI user) throws Exception {
        if (Permissions.canEdit(user,this)) {
            return "<a onclick=\"window.viewer.init(true);\">Show Counts</a>";
        }
        return getListAsTipText(EMPTY_RESOURCE_LIST);
    }

    static public String getListAsTipText(List<String> list) {
        if (list == null || list.size() == 0 || list.get(0) == null || list.get(0).trim().equals("")) {
            return "<span class=\"tip_text\">No items found.<span style=\"top:20px;white-space:nowrap;left:-10;width:auto;\" class=\"tip shadowed\">No items were found: Check your data to be sure you specified a valid system object.</span></span>";
        }
        StringBuilder buffer = new StringBuilder("<span class=\"tip_text\">").append(list.get(0)).append("<span style=\"top:20px;white-space:nowrap;left:-10;width:auto;\" class=\"tip shadowed\">");
        for (int index = 1; index < list.size(); index++) {
            buffer.append(list.get(index));
            if (index < list.size() - 1) {
                buffer.append("<br/>");
            }
        }
        buffer.append("</span></span>");
        return buffer.toString();
    }

    private static final List<String> EMPTY_RESOURCE_LIST = new ArrayList<String>() {{
        add("0 B in 0 files");
        add("No files were found associated with this scan. Contact a member or owner of this project to generate the file count data.");
    }};
}
