/*
 * org.nrg.xdat.om.base.BaseXnatMrsessiondata
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.WrkWorkflowdata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatMrsessiondata;
import org.nrg.xdat.om.base.auto.AutoXnatMrsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;


/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatMrsessiondata extends AutoXnatMrsessiondata {

    public BaseXnatMrsessiondata(ItemI item) {
        super(item);
    }

    public BaseXnatMrsessiondata(UserI user) {
        super(user);
    }

    public BaseXnatMrsessiondata() {
    }

    public BaseXnatMrsessiondata(Hashtable properties, UserI user) {
        super(properties, user);
    }

    public static Comparator GetScannerDelayComparator()
    {
	return (new BaseXnatMrsessiondata()).getScannerDelayComparator();
    }

    public Comparator getScannerDelayComparator()
    {
	return new ScannerDelayComparator();
    }

    public class ScannerDelayComparator implements Comparator{
	public ScannerDelayComparator()
	{
	}
	public int compare(Object o1, Object o2) {
	    BaseXnatMrsessiondata  value1 = (BaseXnatMrsessiondata)(o1);
	    BaseXnatMrsessiondata value2 = (BaseXnatMrsessiondata)(o2);

	    if (value1 == null){
		if (value2 == null)
		{
		    return 0;
		}else{
		    return -1;
		}
	    }
	    if (value2== null)
	    {
		return 1;
	    }

	    int i = Compare(value1.getScanner(),value2.getScanner());

	    if (i == 0)
	    {
		return Compare(value1.getDelay(),value2.getDelay());
	    }else{
		return i;
	    }
	}
    }

    private static int Compare(final Comparable o1, final Comparable o2) {
	if (o1 == null) {
	    return null == o2 ? 0 : -1;
	} else {
	    return null == o2 ? 1 : o1.compareTo(o2);
	}
    }

    public String getWorkflowStatus()
    {
        WrkWorkflowdata wkdata = null;

        CriteriaCollection cc= new CriteriaCollection("AND");
        cc.addClause("wrk:workFlowData.ID",getId());
        ArrayList al =WrkWorkflowdata.getWrkWorkflowdatasByField(cc, getUser(), false);
        if (al.size()>0){
            wkdata= (WrkWorkflowdata)al.get(al.size()-1);
            if (wkdata.getPipelineName().toLowerCase().indexOf("transfer")!=-1){
                if(wkdata.getStatus().equalsIgnoreCase("In Progress"))
                {
                    if (wkdata.getCurrentStepId().equalsIgnoreCase("Store"))
                        return "storing";
                    else
                        return "archiving";
                }if(wkdata.getStatus().equalsIgnoreCase("Running"))
                {
                    if (wkdata.getCurrentStepId().equalsIgnoreCase("Store"))
                        return "storing";
                    else
                        return "archiving";
                }else if (wkdata.getStatus().equalsIgnoreCase("Complete")){
                    return "uploaded";
                }else if (wkdata.getStatus().equalsIgnoreCase("Queued")){
                    return "queued";
                }else{
                    return  wkdata.getPipelineName() + " " + wkdata.getStatus();
                }
            }else{
                if(wkdata.getStatus().equalsIgnoreCase("Running"))
                {
                    return "processing";
                }else if (wkdata.getStatus().equalsIgnoreCase("Complete")){
                    return "processed";
                }else if (wkdata.getStatus().equalsIgnoreCase("Awaiting Action")){
                    return "waiting";
                }else if (wkdata.getStatus().equalsIgnoreCase("Queued")){
                    return "queued";
                }else{
                    return wkdata.getPipelineName() + " " + wkdata.getStatus();
                }
            }
        }

        if (this.getScanner()!=null)
        {
            return "uploading";
        }else{
            return "";
        }


    }
    
    /**
     * 
     * @param otherImageSession
     * @throws Exception
     */
    public void copyValuesFrom(final XnatImagesessiondata otherImageSession) throws Exception {
	super.copyValuesFrom(otherImageSession);
        
    	if(otherImageSession instanceof XnatMrsessiondata){
	final XnatMrsessiondata otherMR = (XnatMrsessiondata)otherImageSession;
	
        if (null != otherMR.getStabilization()){
            this.setStabilization(otherMR.getStabilization());
        }
        
        if (null != otherMR.getMarker()){
            this.setMarker(otherMR.getMarker());
        }
        
        if (null != otherMR.getCoil()){
            this.setCoil(otherMR.getCoil());
        }
    }
    }
        
    public String getDefaultIdentifier(){
        return this.getDcmpatientname();
    }

    public ArrayList getUnionOfScansByType(String csvType) {
        ArrayList _return = new ArrayList();
        String[] types = csvType.split(",");
        if (types != null && types.length > 0) {
        	for(int i = 0; i < types.length; i++) {
        	    ArrayList rtn = getScansByType(types[i].trim());
        	    if (rtn.size() > 0 )_return.addAll(rtn);
        	}
        }
        _return.trimToSize();
        return _return;
    }

    public ArrayList getUnionOfScansByType(String csvType, boolean chronological) {
        ArrayList _return = new ArrayList();
        if (chronological) {
            String[] types = csvType.split(",");
            Hashtable scanTypes = new Hashtable();
            if (types != null && types.length > 0) {
            	for(int i = 0; i < types.length; i++) {
            		scanTypes.put(types[i].trim(), "");
            	}
            }
            for(XnatImagescandataI scan :  this.getScans_scan()){
            	if (scan.getType() != null && scanTypes.containsKey(scan.getType())) {
            		_return.add(scan);
            	}
            }
            _return.trimToSize();
            return _return;
        }else
        	return getUnionOfScansByType(csvType);
    }

    public ArrayList getUnionOfScansByType(String csvType, String chronological) {
    	return getUnionOfScansByType(csvType, new Boolean(chronological).booleanValue());
    }
    
    public Map<String,String> getCustomScanFields(String project){
    	Map<String,String> customheaders= super.getCustomScanFields(project);
    	
    	customheaders.put("parameters/tr","");
    	customheaders.put("parameters/te","");
    	customheaders.put("parameters/ti","");
    	customheaders.put("parameters/flip","");
    	customheaders.put("parameters/sequence","");
    	customheaders.put("parameters/imageType","");
    	customheaders.put("parameters/scanSequence","");
    	customheaders.put("parameters/seqVariant","");
    	customheaders.put("parameters/scanOptions","");
    	customheaders.put("parameters/acqType","");
    	
    	return customheaders;
    }
    
}
