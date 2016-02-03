/*
 * org.nrg.xdat.om.base.BaseXnatPetsessiondata
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
import org.nrg.xdat.model.XnatPetscandataI;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatPetsessiondata;
import org.nrg.xdat.om.base.auto.AutoXnatPetsessiondata;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author XDAT
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BaseXnatPetsessiondata extends AutoXnatPetsessiondata {
	public BaseXnatPetsessiondata(ItemI item)
	{
		super(item);
	}

	public BaseXnatPetsessiondata(UserI user)
	{
		super(user);
	}

	/**
	 * @deprecated Use BaseXnatPetsessiondata(UserI user)
	 **/
	public BaseXnatPetsessiondata()
	{}

	public BaseXnatPetsessiondata(Hashtable properties, UserI user)
	{
		super(properties,user);
	}


    public XnatPetscandataI getDynamicEmissionScan()
    {
        ArrayList scans = this.getScansByType("Dynamic emission");

        if (scans.size()>0)
        {
            return (XnatPetscandataI)scans.get(0);
        }else{
            return null;
        }
    }


    public XnatPetscandataI getTransmissionScan()
    {
        ArrayList scans = this.getScansByType("Transmission");

        if (scans.size()>0)
        {
            return (XnatPetscandataI)scans.get(0);
        }else{
            return null;
        }
    }



    public String getDefaultIdentifier(){
        return this.getPatientname();
    }


    public void copyValuesFrom(XnatImagesessiondata otherImage) throws Exception {
    	super.copyValuesFrom(otherImage);
    	if(otherImage instanceof XnatPetsessiondata){
		final XnatPetsessiondata tempPET = (XnatPetsessiondata)otherImage;
		if (null != tempPET.getTracer_dose())
		    this.setTracer_dose(tempPET.getTracer_dose());
		if (null != tempPET.getTracer_dose_units())
		    this.setTracer_dose_units(tempPET.getTracer_dose_units());
		if (null != tempPET.getTracer_intermediate_units())
		    this.setTracer_intermediate_units(tempPET.getTracer_intermediate_units());
		if (null != tempPET.getTracer_intermediate())
		    this.setTracer_intermediate(tempPET.getTracer_intermediate());
		if (null != tempPET.getTracer_isotope_halfLife())
		    this.setTracer_isotope_halfLife(tempPET.getTracer_isotope_halfLife());
		if (null != tempPET.getTracer_isotope())
		    this.setTracer_isotope(tempPET.getTracer_isotope());
		if (null != tempPET.getTracer_name())
		    this.setTracer_name(tempPET.getTracer_name());
		if (null != tempPET.getTracer_specificactivity())
		    this.setTracer_specificactivity(tempPET.getTracer_specificactivity());
		if (null != tempPET.getTracer_transmissions())
		    this.setTracer_transmissions(tempPET.getTracer_transmissions());
		if (null != tempPET.getTracer_totalmass())
		    this.setTracer_totalmass(tempPET.getTracer_totalmass());
		if (null != tempPET.getTracer_totalmass_units())
		    this.setTracer_totalmass_units(tempPET.getTracer_totalmass_units());
		if (null != tempPET.getStartTimeInjection())
		    this.setStartTimeInjection(tempPET.getStartTimeInjection());
		if (null != tempPET.getStartTimeScan())
		    this.setStartTimeScan(tempPET.getStartTimeScan());
    	}

    }

    public Map<String,String> getCustomScanFields(String project){
    	Map<String,String> customheaders= super.getCustomScanFields(project);

    	customheaders.put("Original File Name","parameters/originalFileName");
    	customheaders.put("System Type","parameters/systemType");
    	customheaders.put("File Type","parameters/fileType");
    	customheaders.put("Transaxial FOV","parameters/transaxialFOV");
    	customheaders.put("Acq Type","parameters/acqType");
    	customheaders.put("Facility","parameters/facility");
    	customheaders.put("Num Planes","parameters/numPlanes");
    	customheaders.put("Num Gates","parameters/numGates");
    	customheaders.put("Plane Separation","parameters/planeSeparation");
    	customheaders.put("Bin Size","parameters/binSize");
    	customheaders.put("Data Type","parameters/dataType");
    	customheaders.put("Dimensions x","parameters/dimensions/x");
    	customheaders.put("Dimensions y","parameters/dimensions/y");
    	customheaders.put("Dimensions z","parameters/dimensions/z");
    	customheaders.put("Dimensions num","parameters/dimensions/num");
    	customheaders.put("Offset x","parameters/offset/x");
    	customheaders.put("Offset y","parameters/offset/y");
    	customheaders.put("Offset z","parameters/offset/z");
    	customheaders.put("Recon Zoom","parameters/reconZoom");
    	customheaders.put("Pixel Size x","parameters/pixelSize/x");
    	customheaders.put("Pixel Size y","parameters/pixelSize/y");
    	customheaders.put("Pixel Size z","parameters/pixelSize/z");
    	customheaders.put("Filter Code","parameters/filterCode");
    	customheaders.put("Resolution x","parameters/resolution/x");
    	customheaders.put("Resolution y","parameters/resolution/y");
    	customheaders.put("Resolution z","parameters/resolution/z");
    	customheaders.put("Num RElements","parameters/numRElements");
    	customheaders.put("Num Angles","parameters/numAngles");
    	customheaders.put("ZRotation Angle","parameters/ZRotationAngle");
    	customheaders.put("Processing Code","parameters/processingCode");
    	customheaders.put("Gate Duration","parameters/gateDuration");
    	customheaders.put("rWave Offset","parameters/rWaveOffset");
    	customheaders.put("Num Accepted Beats","parameters/numAcceptedBeats");
    	customheaders.put("Filter cutoff","parameters/filter/cutoff");
    	customheaders.put("Annotation","parameters/annotation");
    	customheaders.put("MT_1_1","parameters/MT_1_1");
    	customheaders.put("MT_1_2","parameters/MT_1_2");
    	customheaders.put("MT_1_3","parameters/MT_1_3");
    	customheaders.put("MT_1_4","parameters/MT_1_4");
    	customheaders.put("MT_2_1","parameters/MT_2_1");
    	customheaders.put("MT_2_2","parameters/MT_2_2");
    	customheaders.put("MT_2_3","parameters/MT_2_3");
    	customheaders.put("MT_2_4","parameters/MT_2_4");
    	customheaders.put("MT_3_1","parameters/MT_3_1");
    	customheaders.put("MT_3_2","parameters/MT_3_2");
    	customheaders.put("MT_3_3","parameters/MT_3_3");
    	customheaders.put("MT_3_4","parameters/MT_3_4");
    	customheaders.put("RFilter cutoff","parameters/RFilter/cutoff");
    	customheaders.put("RFilter resolution","parameters/RFilter/resolution");
    	customheaders.put("RFilter code","parameters/RFilter/code");
    	customheaders.put("RFilter order","parameters/RFilter/order");
    	customheaders.put("ZFilter cutoff","parameters/ZFilter/cutoff");
    	customheaders.put("ZFilter resolution","parameters/ZFilter/resolution");
    	customheaders.put("ZFilter code","parameters/ZFilter/code");
    	customheaders.put("ZFilter order","parameters/ZFilter/order");
    	customheaders.put("scatter Type","parameters/scatterType");
    	customheaders.put("recon Type","parameters/reconType");
    	customheaders.put("recon Views","parameters/reconViews");
    	customheaders.put("bed Position","parameters/bedPosition");
    	customheaders.put("ecat Calibration Factor","parameters/ecatCalibrationFactor");
    	customheaders.put("ecat Validation","ecatValidation");
    	customheaders.put("ecat Validation status","ecatValidation/status");


    	return customheaders;
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
}
