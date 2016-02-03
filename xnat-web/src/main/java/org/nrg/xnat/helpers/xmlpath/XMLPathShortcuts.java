/*
 * org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.xmlpath;

import org.nrg.xdat.om.*;
import org.nrg.xnat.restlet.util.XNATRestConstants;

import java.util.*;


public class XMLPathShortcuts implements XMLPathShortcutsI{
	private static XMLPathShortcuts instance=null;
	
	public static final String IMAGE_SCAN_DATA=XnatImagescandata.SCHEMA_ELEMENT_NAME;
	public static final String EXPERIMENT_DATA=XnatExperimentdata.SCHEMA_ELEMENT_NAME;
	public static final String SUBJECT_DATA=XnatSubjectdata.SCHEMA_ELEMENT_NAME;
	public static final String RECON_DATA=XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME;
	public static final String DERIVED_DATA=XnatDeriveddata.SCHEMA_ELEMENT_NAME;
	public static final String PROJECT_DATA=XnatProjectdata.SCHEMA_ELEMENT_NAME;
	public static final String VISIT_DATA=XnatPvisitdata.SCHEMA_ELEMENT_NAME;
	
	private Map<String,Map<String,String>> shortcuts=new Hashtable<String,Map<String,String>>();
	private Map<String,Map<String,String>> readonly=new Hashtable<String,Map<String,String>>();
	
	private XMLPathShortcuts(){
		this.addShortCut(IMAGE_SCAN_DATA, "ID", "xnat:imageScanData/ID");
		this.addShortCut(IMAGE_SCAN_DATA, "type", "xnat:imageScanData/type");
		this.addShortCut(IMAGE_SCAN_DATA, "UID", "xnat:imageScanData/UID");
		this.addShortCut(IMAGE_SCAN_DATA, "note", "xnat:imageScanData/note");
		this.addShortCut(IMAGE_SCAN_DATA, "quality", "xnat:imageScanData/quality");
		this.addShortCut(IMAGE_SCAN_DATA, "condition", "xnat:imageScanData/condition");
		this.addShortCut(IMAGE_SCAN_DATA, "series_description", "xnat:imageScanData/series_description");
		this.addShortCut(IMAGE_SCAN_DATA, "documentation", "xnat:imageScanData/documentation");
		this.addShortCut(IMAGE_SCAN_DATA, "scanner", "xnat:imageScanData/scanner");
		this.addShortCut(IMAGE_SCAN_DATA, "modality", "xnat:imageScanData/modality");
		this.addShortCut(IMAGE_SCAN_DATA, "frames", "xnat:imageScanData/frames");
		this.addShortCut(IMAGE_SCAN_DATA, "validation_method", "xnat:imageScanData/validation/method");
		this.addShortCut(IMAGE_SCAN_DATA, "validation_status", "xnat:imageScanData/validation/status");
		this.addShortCut(IMAGE_SCAN_DATA, "validation_date", "xnat:imageScanData/validation/date");
		this.addShortCut(IMAGE_SCAN_DATA, "validation_notes", "xnat:imageScanData/validation/notes");

		this.addShortCut(IMAGE_SCAN_DATA, "xnat_imagescandata_id", "xnat:imageScanData/xnat_imagescandata_id",true);

		this.addShortCut(IMAGE_SCAN_DATA, "scanTime","xnat:imageScanData/startTime");
		this.addShortCut(IMAGE_SCAN_DATA, "coil", "xnat:mrScanData/coil");
		this.addShortCut(IMAGE_SCAN_DATA, "fieldStrength", "xnat:mrScanData/fieldStrength");
		this.addShortCut(IMAGE_SCAN_DATA, "marker", "xnat:mrScanData/marker");
		this.addShortCut(IMAGE_SCAN_DATA, "stabilization", "xnat:mrScanData/stabilization");

		this.addShortCut(IMAGE_SCAN_DATA, "orientation","xnat:petScanData/parameters/orientation");
		this.addShortCut(IMAGE_SCAN_DATA, "originalFileName","xnat:petScanData/parameters/originalFileName");
		this.addShortCut(IMAGE_SCAN_DATA, "systemType","xnat:petScanData/parameters/systemType");
		this.addShortCut(IMAGE_SCAN_DATA, "fileType","xnat:petScanData/parameters/fileType");
		this.addShortCut(IMAGE_SCAN_DATA, "transaxialFOV","xnat:petScanData/parameters/transaxialFOV");
		this.addShortCut(IMAGE_SCAN_DATA, "acqType","xnat:petScanData/parameters/acqType");
		this.addShortCut(IMAGE_SCAN_DATA, "facility","xnat:petScanData/parameters/facility");
		this.addShortCut(IMAGE_SCAN_DATA, "numPlanes","xnat:petScanData/parameters/numPlanes");
		this.addShortCut(IMAGE_SCAN_DATA, "numFrames","xnat:petScanData/parameters/frames/numFrames");
		this.addShortCut(IMAGE_SCAN_DATA, "numGates","xnat:petScanData/parameters/numGates");
		this.addShortCut(IMAGE_SCAN_DATA, "planeSeparation","xnat:petScanData/parameters/planeSeparation");
		this.addShortCut(IMAGE_SCAN_DATA, "binSize","xnat:petScanData/parameters/binSize");
		this.addShortCut(IMAGE_SCAN_DATA, "dataType","xnat:petScanData/parameters/dataType");

		this.addShortCut(IMAGE_SCAN_DATA, "insert_date", "xnat:imageScanData/meta/insert_date",true);
		this.addShortCut(IMAGE_SCAN_DATA, "insert_user", "xnat:imageScanData/meta/insert_user/login",true);
		this.addShortCut(IMAGE_SCAN_DATA, "last_modified", "xnat:imageScanData/meta/last_modified",true);

		this.addShortCut(IMAGE_SCAN_DATA, "xsiType", "xnat:imageScanData/extension_item/element_name",true);
		
		
		//experiments
		this.addShortCut(EXPERIMENT_DATA, "ID", "xnat:experimentdata/id");
		this.addShortCut(EXPERIMENT_DATA, "visit_id", "xnat:experimentdata/visit_id");
		this.addShortCut(EXPERIMENT_DATA, "visit", "xnat:experimentdata/visit");
		this.addShortCut(EXPERIMENT_DATA, "date", "xnat:experimentdata/date");
		this.addShortCut(EXPERIMENT_DATA, "time", "xnat:experimentdata/time");
		this.addShortCut(EXPERIMENT_DATA, "note", "xnat:experimentdata/note");
		this.addShortCut(EXPERIMENT_DATA, "pi_firstname", "xnat:experimentdata/investigator/firstname");
		this.addShortCut(EXPERIMENT_DATA, "pi_lastname", "xnat:experimentdata/investigator/lastname");
		this.addShortCut(EXPERIMENT_DATA, "validation_method", "xnat:experimentdata/validation/method");
		this.addShortCut(EXPERIMENT_DATA, "validation_status", "xnat:experimentdata/validation/status");
		this.addShortCut(EXPERIMENT_DATA, "validation_date", "xnat:experimentdata/validation/date");
		this.addShortCut(EXPERIMENT_DATA, "validation_notes", "xnat:experimentdata/validation/notes");
		this.addShortCut(EXPERIMENT_DATA, "project", "xnat:experimentdata/project");
		this.addShortCut(EXPERIMENT_DATA, "label", "xnat:experimentdata/label");
		
		this.addShortCut(EXPERIMENT_DATA, "scanner", "xnat:imagesessiondata/scanner");
		this.addShortCut(EXPERIMENT_DATA, "operator", "xnat:imagesessiondata/operator");
		this.addShortCut(EXPERIMENT_DATA, "dcmAccessionNumber", "xnat:imagesessiondata/dcmaccessionnumber");
		this.addShortCut(EXPERIMENT_DATA, "dcmPatientId", "xnat:imagesessiondata/dcmpatientid");
		this.addShortCut(EXPERIMENT_DATA, "dcmPatientName", "xnat:imagesessiondata/dcmpatientname");
		this.addShortCut(EXPERIMENT_DATA, "session_type", "xnat:imagesessiondata/session_type");
		this.addShortCut(EXPERIMENT_DATA, "modality", "xnat:imagesessiondata/modality");
		this.addShortCut(EXPERIMENT_DATA, "UID", "xnat:imagesessiondata/uid");
		this.addShortCut(EXPERIMENT_DATA, "studyInstanceUID", "xnat:imagesessiondata/uid");

		this.addShortCut(EXPERIMENT_DATA, "coil", "xnat:mrsessiondata/coil");
		this.addShortCut(EXPERIMENT_DATA, "fieldStrength", "xnat:mrsessiondata/fieldstrength");
		this.addShortCut(EXPERIMENT_DATA, "marker", "xnat:mrsessiondata/marker");
		this.addShortCut(EXPERIMENT_DATA, "stabilization", "xnat:mrsessiondata/stabilization");

		this.addShortCut(EXPERIMENT_DATA, "studyType", "xnat:petsessiondata/studytype");
		this.addShortCut(EXPERIMENT_DATA, "patientID", "xnat:petsessiondata/patientid");
		this.addShortCut(EXPERIMENT_DATA, "patientName", "xnat:petsessiondata/patientname");
		this.addShortCut(EXPERIMENT_DATA, "stabilization", "xnat:petsessiondata/stabilization");
		this.addShortCut(EXPERIMENT_DATA, "scan_start_time", "xnat:petsessiondata/start_time_scan");
		this.addShortCut(EXPERIMENT_DATA, "injection_start_time", "xnat:petsessiondata/start_time_injection");
		this.addShortCut(EXPERIMENT_DATA, "tracer_name", "xnat:petsessiondata/tracer/name");
		this.addShortCut(EXPERIMENT_DATA, "tracer_startTime", "xnat:petsessiondata/tracer/starttime");
		this.addShortCut(EXPERIMENT_DATA, "tracer_dose", "xnat:petsessiondata/tracer/dose");
		this.addShortCut(EXPERIMENT_DATA, "tracer_sa", "xnat:petsessiondata/tracer/specificactivity");
		this.addShortCut(EXPERIMENT_DATA, "tracer_totalmass", "xnat:petsessiondata/tracer/totalmass");
		this.addShortCut(EXPERIMENT_DATA, "tracer_intermediate", "xnat:petsessiondata/tracer/intermediate");
		this.addShortCut(EXPERIMENT_DATA, "tracer_isotope", "xnat:petsessiondata/tracer/isotope");
		this.addShortCut(EXPERIMENT_DATA, "tracer_isotope", "xnat:petsessiondata/tracer/isotope/half-life");
		this.addShortCut(EXPERIMENT_DATA, "tracer_transmissions", "xnat:petsessiondata/tracer/transmissions");
		this.addShortCut(EXPERIMENT_DATA, "tracer_transmissions_start", "xnat:petsessiondata/tracer/transmissions_starttime");

		
		this.addShortCut(EXPERIMENT_DATA, "subject_ID", "xnat:subjectassessordata/subject_id");
		this.addShortCut(EXPERIMENT_DATA, "subject_label", "xnat:subjectdata/label",true);
		this.addShortCut(EXPERIMENT_DATA, "subject_project", "xnat:subjectdata/project",true);
		
		this.addShortCut(EXPERIMENT_DATA, "session_ID", "xnat:imagesessiondata/id",true);
		this.addShortCut(EXPERIMENT_DATA, "session_label", "xnat:imagesessiondata/label",true);
		this.addShortCut(EXPERIMENT_DATA, "session_project", "xnat:imagesessiondata/project",true);
		
		this.addShortCut(EXPERIMENT_DATA, "insert_date", "xnat:experimentData/meta/insert_date",true);
		this.addShortCut(EXPERIMENT_DATA, "insert_user", "xnat:experimentData/meta/insert_user/login",true);
		this.addShortCut(EXPERIMENT_DATA, "last_modified", "xnat:experimentData/meta/last_modified",true);
		this.addShortCut(EXPERIMENT_DATA, "xsiType", "xnat:experimentData/extension_item/element_name",true);
		
		
		//subjects
		this.addShortCut(SUBJECT_DATA, "project", "xnat:subjectData/project");
		this.addShortCut(SUBJECT_DATA, "label", "xnat:subjectData/label");
		this.addShortCut(SUBJECT_DATA, "ID", "xnat:subjectData/ID");

		this.addShortCut(SUBJECT_DATA, "group", "xnat:subjectData/group");
		this.addShortCut(SUBJECT_DATA, "src", "xnat:subjectData/src");
		this.addShortCut(SUBJECT_DATA, "pi_firstname", "xnat:subjectData/investigator/firstname");
		this.addShortCut(SUBJECT_DATA, "pi_lastname", "xnat:subjectData/investigator/lastname");
		this.addShortCut(SUBJECT_DATA, "dob", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/dob");
		this.addShortCut(SUBJECT_DATA, "yob", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/yob");
		this.addShortCut(SUBJECT_DATA, "age", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/age");
		this.addShortCut(SUBJECT_DATA, "gender", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gender");
		this.addShortCut(SUBJECT_DATA, "handedness", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/handedness");
		this.addShortCut(SUBJECT_DATA, "ses", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/ses");
		this.addShortCut(SUBJECT_DATA, "education", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/education");
		this.addShortCut(SUBJECT_DATA, "educationDesc", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/educationDesc");
		this.addShortCut(SUBJECT_DATA, "race", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/race");
		this.addShortCut(SUBJECT_DATA, "ethnicity", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/ethnicity");
		this.addShortCut(SUBJECT_DATA, "weight", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/weight");
		this.addShortCut(SUBJECT_DATA, "height", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/height");
		this.addShortCut(SUBJECT_DATA, "gestational_age", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gestational_age");
		this.addShortCut(SUBJECT_DATA, "post_menstrual_age", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/post_menstrual_age");
		this.addShortCut(SUBJECT_DATA, "birth_weight", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/birth_weight");
		
		this.addShortCut(SUBJECT_DATA, "insert_date", "xnat:subjectData/meta/insert_date",true);
		this.addShortCut(SUBJECT_DATA, "insert_user", "xnat:subjectData/meta/insert_user/login",true);
		this.addShortCut(SUBJECT_DATA, "last_modified", "xnat:subjectData/meta/last_modified",true);
		
		//recon
		this.addShortCut(RECON_DATA, "ID", "xnat:reconstructedImageData/ID");
		this.addShortCut(RECON_DATA, "type", "xnat:reconstructedImageData/type");
		this.addShortCut(RECON_DATA, "baseScanType", "xnat:reconstructedImageData/baseScanType");
		this.addShortCut(RECON_DATA, "xnat_reconstructedimagedata_id", "xnat:reconstructedImageData/xnat_reconstructedimagedata_id",true);
		
		//assessor
		this.addShortCut(DERIVED_DATA, "ID", "xnat:experimentdata/ID");
		this.addShortCut(DERIVED_DATA, "visit_id", "xnat:experimentdata/visit_id");
		this.addShortCut(DERIVED_DATA, "date", "xnat:experimentdata/date");
		this.addShortCut(DERIVED_DATA, "time", "xnat:experimentdata/time");
		this.addShortCut(DERIVED_DATA, "note", "xnat:experimentdata/note");
		this.addShortCut(DERIVED_DATA, "pi_firstname", "xnat:experimentdata/investigator/firstname");
		this.addShortCut(DERIVED_DATA, "pi_lastname", "xnat:experimentdata/investigator/lastname");
		this.addShortCut(DERIVED_DATA, "validation_method", "xnat:experimentdata/validation/method");
		this.addShortCut(DERIVED_DATA, "validation_status", "xnat:experimentdata/validation/status");
		this.addShortCut(DERIVED_DATA, "validation_date", "xnat:experimentdata/validation/date");
		this.addShortCut(DERIVED_DATA, "validation_notes", "xnat:experimentdata/validation/notes");
		this.addShortCut(DERIVED_DATA, "project", "xnat:experimentdata/project");
		this.addShortCut(DERIVED_DATA, "label", "xnat:experimentdata/label");
		
		this.addShortCut(DERIVED_DATA, "session_ID", "xnat:imagesessiondata/id",true);
		this.addShortCut(DERIVED_DATA, "session_label", "xnat:imagesessiondata/label",true);
		this.addShortCut(DERIVED_DATA, "session_project", "xnat:imagesessiondata/project",true);
		
		this.addShortCut(DERIVED_DATA, "insert_date", "xnat:experimentData/meta/insert_date",true);
		this.addShortCut(DERIVED_DATA, "insert_user", "xnat:experimentData/meta/insert_user/login",true);
		this.addShortCut(DERIVED_DATA, "last_modified", "xnat:experimentData/meta/last_modified",true);
		this.addShortCut(DERIVED_DATA, "xsiType", "xnat:experimentData/extension_item/element_name",true);
		
		//project
		this.addShortCut(PROJECT_DATA, "ID", "xnat:projectData/ID");
		this.addShortCut(PROJECT_DATA, "secondary_ID", "xnat:projectData/secondary_ID");
		this.addShortCut(PROJECT_DATA, "name", "xnat:projectData/name");
		this.addShortCut(PROJECT_DATA, "description", "xnat:projectData/description");
		this.addShortCut(PROJECT_DATA, "keywords", "xnat:projectData/keywords");
		this.addShortCut(PROJECT_DATA, "alias", "xnat:projectData/aliases/alias/alias");
		this.addShortCut(PROJECT_DATA, "pi_firstname", "xnat:projectData/PI/firstname");
		this.addShortCut(PROJECT_DATA, "pi_lastname", "xnat:projectData/PI/lastname");
		this.addShortCut(PROJECT_DATA, "note", "xnat:projectData/fields/field[name=note]/field");
		
		//visit
		this.addShortCut(VISIT_DATA, "ID", "xnat:pvisitData/ID");
		this.addShortCut(VISIT_DATA, "visit_type", "xnat:pvisitData/visit_type");
		this.addShortCut(VISIT_DATA, "visit_name", "xnat:pvisitData/visit_name");
		this.addShortCut(VISIT_DATA, "closed", "xnat:pvisitData/closed");
		this.addShortCut(VISIT_DATA, "project", "xnat:pvisitdata/project");
		this.addShortCut(VISIT_DATA, "date", "xnat:pvisitdata/date");
		this.addShortCut(VISIT_DATA, "xsiType", "xnat:pvisitdata/extension_item/element_name",true);
		this.addShortCut(VISIT_DATA, "label", "xnat:pvisitdata/label");
		this.addShortCut(VISIT_DATA, "insert_date", "xnat:pvisitdata/meta/insert_date",true);
		this.addShortCut(VISIT_DATA, "insert_user", "xnat:pvisitdata/meta/insert_user/login",true);
		this.addShortCut(VISIT_DATA, "last_modified", "xnat:pvisitdata/meta/last_modified",true);
		this.addShortCut(VISIT_DATA, "subject_ID", "xnat:pvisitdata/subject_id");
		
	}
	
	public void addShortCut(final String xsiType,final String key, final String path){
		this.addShortCut(xsiType, key, path, false);
	}
	
	public void addShortCut(final String xsiType,final String key, final String path,boolean readOnly){
		if(!shortcuts.containsKey(xsiType)){
			shortcuts.put(xsiType.intern(), new Hashtable<String,String>());
			readonly.put(xsiType.intern(), new Hashtable<String,String>());
		}
		
		if(readOnly){
			readonly.get(xsiType).put(key.intern(), path.intern());
		}else{
		shortcuts.get(xsiType).put(key.intern(), path.intern());
	}
	}
	
	public static synchronized XMLPathShortcutsI getInstance(){
		if(instance==null){
			instance=new XMLPathShortcuts();
		}
		return instance;
	}
	


	/**
	 * Return parameters with keys that start with XSI:TYPEs or match shortcuts
	 * @param params
	 * @return
	 */
	public static Map<String,Object> identifyUsableFields(final Map<String,Object> params, final String TYPE,boolean readOnly){
		 return XMLPathShortcuts.getInstance().identifyFields(params,TYPE,readOnly);
		
		
	}
	
	@SuppressWarnings("serial")
	final static List<String> REGEXP=new ArrayList<String>(){{
		add(XNATRestConstants.XML_PATH_REGEXP);
		add(XNATRestConstants.XML_PATH_REGEXP2);}};
	
	public Map<String,Object> identifyFields(final Map<String,Object> params, final String TYPE,boolean readOnly){
		final Map<String,Object> relevant=new HashMap<String,Object>();
		
		if(params!=null){
			for(Map.Entry<String,Object> entry:params.entrySet()){
				for(final String reg:REGEXP){
					if((entry.getKey()).matches(reg)){
					relevant.put(entry.getKey(),entry.getValue());
						continue;
					}
				}
				if(shortcuts.get(TYPE).containsKey(entry.getKey())){
					relevant.put(shortcuts.get(TYPE).get(entry.getKey()),entry.getValue());
				}
				
				if(readOnly){
					if(readonly.get(TYPE).containsKey(entry.getKey())){
						relevant.put(readonly.get(TYPE).get(entry.getKey()),entry.getValue());
					}
				}
			}
		}
		
		return relevant;
	}

	@Override
	public Map<String, String> getShortcuts(String type, boolean readOnly) {
		Map<String,String> temp=new HashMap<String,String>(this.shortcuts.get(type));
		if(readOnly)temp.putAll(this.readonly.get(type));
		return temp;
	}

}
