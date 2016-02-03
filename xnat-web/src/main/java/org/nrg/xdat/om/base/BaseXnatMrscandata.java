/*
 * org.nrg.xdat.om.base.BaseXnatMrscandata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatMrqcscandataI;
import org.nrg.xdat.model.XnatMrscandataI;
import org.nrg.xdat.om.base.auto.AutoXnatMrscandata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.scanType.AbstractScanTypeMapping;
import org.nrg.xnat.helpers.scanType.ScanTypeMappingI;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatMrscandata extends AutoXnatMrscandata {

	public BaseXnatMrscandata(ItemI item) {
		super(item);
	}

	public BaseXnatMrscandata(UserI user) {
		super(user);
	}

	public BaseXnatMrscandata() {
	}

	public BaseXnatMrscandata(Hashtable properties, UserI user) {
		super(properties, user);
	}

	public XnatMrqcscandataI getManualQC() {
		return (XnatMrqcscandataI) super.getManualQC();
	}
	
	public ScanTypeMappingI getScanTypeMapping(final String project, final String dbName){
		return new MRScanTypeMapping(project,dbName);
	}
	
    private static class ScanTypeHistory{
        private final List<ScanType> types = Lists.newArrayList();
        
        public void add(String t, String it, Integer f){
            types.add(new ScanType(t,it,f));
        }
        
        public String match(String desc,String imgtype,Integer frames){
            if(types.size()==1){
                return types.get(0).getType();
            }
            //match by imgtype
            if(imgtype!=null && !imgtype.equals("")){
                for(ScanType st: types){
                    if(imgtype.equalsIgnoreCase(st.getImgtype())){
                        return st.getType();
                    }
                }
            }
            
            //match by frames
            if(frames!=null){
                for(ScanType st: types){
                    if(frames.equals(st.getFrames())){
                        return st.getType();
                    }
                }
            }
            
            if(imgtype==null){
                ScanType candidate = null;
                for (ScanType scanType : types) {
                    if (scanType.getImgtype() == null) {
                        if (candidate == null || (candidate.getType() == null && scanType.getType() != null)) {
                            candidate = scanType;
                    }
                }
            }
                if (candidate != null) {
                    return candidate.getType();
                }
            }
            
            if(frames==null){
                for(ScanType st: types){
                    if(st.getFrames()==null){
                        return st.getType();
                    }
                }
            }
            
            return types.get(0).getType();
        }
    }
    
    private static class ScanType{
        private final String _type;
        private final String _imgtype;
        private final Integer _frames;
        
        public ScanType(final String t, final String it, final Integer f) {
            _type=t;
            _imgtype=it;
            _frames=f;
        }
        
        public Integer getFrames() {
            return _frames;
        }

        public String getImgtype() {
            return _imgtype;
        }

        public String getType() {
            return _type;
        }
     }

    /**
     * For MR, we also use the DICOM image type parameters for matching scan types.
     *
     */
	public static class MRScanTypeMapping extends AbstractScanTypeMapping<ScanTypeHistory> implements ScanTypeMappingI {	    
	    public MRScanTypeMapping(String project,String dbName){
	        super(project, dbName, buildSelectSql(project));
	    }
		
	    private static final String buildSelectSql(final String project) {
            if (Strings.isNullOrEmpty(project)) {
                return null;
            } else {
	            return "SELECT DISTINCT REPLACE(REPLACE(REPLACE(REPLACE(UPPER(scan.series_description),' ',''),'_',''),'-',''),'*','') AS series_description,scan.type,UPPER(parameters_imagetype) AS parameters_imagetype,frames FROM xnat_imagescandata scan LEFT JOIN xnat_mrscandata mr ON scan.xnat_imagescandata_id=mr.xnat_imagescandata_id LEFT JOIN xnat_experimentData isd ON scan.image_session_id=isd.id WHERE scan.series_description IS NOT NULL AND isd.project='" + project + "';";
	        }
        }
	    	    
        /*
         * (non-Javadoc)
         * @see org.nrg.xnat.helpers.scanType.AbstractScanTypeMapping#getMappedType(org.nrg.xdat.model.XnatImagescandataI, java.util.Map)
         */
        protected String getMappedType(XnatImagescandataI scan, Map<String,ScanTypeHistory> histories) {
            final String seriesDescription = scan.getSeriesDescription();
            final String formatted = AbstractScanTypeMapping.standardizeFormat(seriesDescription);
            final String imgType = ((XnatMrscandataI)scan).getParameters_imagetype();

            final ScanTypeHistory history = histories.get(formatted);
            return null == history ? null : history.match(seriesDescription, imgType, scan.getFrames());
        }

        /*
         * (non-Javadoc)
         * @see org.nrg.xnat.helpers.scanType.AbstractScanTypeMapping#newScanHistory()
         */
        protected ScanTypeHistory newScanHistory() { return new ScanTypeHistory(); }
 
        /*
         * (non-Javadoc)
         * @see org.nrg.xnat.helpers.scanType.AbstractScanTypeMapping#update(java.lang.Object, java.util.Hashtable)
         */
        protected void update(final ScanTypeHistory h, final Hashtable<?,?> row) {
            h.add((String)row.get("type"),(String)row.get("parameters_imagetype"),(Integer)row.get("frames"));
        }
	}
}
