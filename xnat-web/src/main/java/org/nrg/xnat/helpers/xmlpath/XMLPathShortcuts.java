/*
 * web: org.nrg.xnat.helpers.xmlpath.XMLPathShortcuts
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.xmlpath;

import org.nrg.xdat.om.*;
import org.nrg.xnat.restlet.util.XNATRestConstants;

import java.util.*;

public class XMLPathShortcuts implements XMLPathShortcutsI {
    private static XMLPathShortcuts instance = null;

    public static final String IMAGE_SCAN_DATA = XnatImagescandata.SCHEMA_ELEMENT_NAME;
    public static final String EXPERIMENT_DATA = XnatExperimentdata.SCHEMA_ELEMENT_NAME;
    public static final String SUBJECT_DATA    = XnatSubjectdata.SCHEMA_ELEMENT_NAME;
    public static final String RECON_DATA      = XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME;
    public static final String DERIVED_DATA    = XnatDeriveddata.SCHEMA_ELEMENT_NAME;
    public static final String PROJECT_DATA    = XnatProjectdata.SCHEMA_ELEMENT_NAME;
    public static final String VISIT_DATA      = XnatPvisitdata.SCHEMA_ELEMENT_NAME;

    private final Map<String, Map<String, String>> shortcuts             = new Hashtable<>();
    private final Map<String, Map<String, String>> readonly              = new Hashtable<>();
    private final Map<String, Map<String, String>> embeddedTypeShortcuts = new Hashtable<>();

    private XMLPathShortcuts() {
        addShortCut(IMAGE_SCAN_DATA, "ID", "xnat:imageScanData/ID");
        addShortCut(IMAGE_SCAN_DATA, "type", "xnat:imageScanData/type");
        addShortCut(IMAGE_SCAN_DATA, "UID", "xnat:imageScanData/UID");
        addShortCut(IMAGE_SCAN_DATA, "note", "xnat:imageScanData/note");
        addShortCut(IMAGE_SCAN_DATA, "quality", "xnat:imageScanData/quality");
        addShortCut(IMAGE_SCAN_DATA, "condition", "xnat:imageScanData/condition");
        addShortCut(IMAGE_SCAN_DATA, "series_description", "xnat:imageScanData/series_description");
        addShortCut(IMAGE_SCAN_DATA, "documentation", "xnat:imageScanData/documentation");
        addShortCut(IMAGE_SCAN_DATA, "scanner", "xnat:imageScanData/scanner");
        addShortCut(IMAGE_SCAN_DATA, "modality", "xnat:imageScanData/modality");
        addShortCut(IMAGE_SCAN_DATA, "frames", "xnat:imageScanData/frames");
        addShortCut(IMAGE_SCAN_DATA, "validation_method", "xnat:imageScanData/validation/method");
        addShortCut(IMAGE_SCAN_DATA, "validation_status", "xnat:imageScanData/validation/status");
        addShortCut(IMAGE_SCAN_DATA, "validation_date", "xnat:imageScanData/validation/date");
        addShortCut(IMAGE_SCAN_DATA, "validation_notes", "xnat:imageScanData/validation/notes");

        addShortCut(IMAGE_SCAN_DATA, "xnat_imagescandata_id", "xnat:imageScanData/xnat_imagescandata_id", true);

        addShortCut(IMAGE_SCAN_DATA, "scanTime", "xnat:imageScanData/startTime");
        addShortCut(IMAGE_SCAN_DATA, "coil", "xnat:mrScanData/coil");
        addShortCut(IMAGE_SCAN_DATA, "fieldStrength", "xnat:mrScanData/fieldStrength");
        addShortCut(IMAGE_SCAN_DATA, "marker", "xnat:mrScanData/marker");
        addShortCut(IMAGE_SCAN_DATA, "stabilization", "xnat:mrScanData/stabilization");

        addShortCut(IMAGE_SCAN_DATA, "orientation", "xnat:petScanData/parameters/orientation");
        addShortCut(IMAGE_SCAN_DATA, "originalFileName", "xnat:petScanData/parameters/originalFileName");
        addShortCut(IMAGE_SCAN_DATA, "systemType", "xnat:petScanData/parameters/systemType");
        addShortCut(IMAGE_SCAN_DATA, "fileType", "xnat:petScanData/parameters/fileType");
        addShortCut(IMAGE_SCAN_DATA, "transaxialFOV", "xnat:petScanData/parameters/transaxialFOV");
        addShortCut(IMAGE_SCAN_DATA, "acqType", "xnat:petScanData/parameters/acqType");
        addShortCut(IMAGE_SCAN_DATA, "facility", "xnat:petScanData/parameters/facility");
        addShortCut(IMAGE_SCAN_DATA, "numPlanes", "xnat:petScanData/parameters/numPlanes");
        addShortCut(IMAGE_SCAN_DATA, "numFrames", "xnat:petScanData/parameters/frames/numFrames");
        addShortCut(IMAGE_SCAN_DATA, "numGates", "xnat:petScanData/parameters/numGates");
        addShortCut(IMAGE_SCAN_DATA, "planeSeparation", "xnat:petScanData/parameters/planeSeparation");
        addShortCut(IMAGE_SCAN_DATA, "binSize", "xnat:petScanData/parameters/binSize");
        addShortCut(IMAGE_SCAN_DATA, "dataType", "xnat:petScanData/parameters/dataType");

        addShortCut(IMAGE_SCAN_DATA, "insert_date", "xnat:imageScanData/meta/insert_date", true);
        addShortCut(IMAGE_SCAN_DATA, "insert_user", "xnat:imageScanData/meta/insert_user/login", true);
        addShortCut(IMAGE_SCAN_DATA, "last_modified", "xnat:imageScanData/meta/last_modified", true);

        addShortCut(IMAGE_SCAN_DATA, "xsiType", "xnat:imageScanData/extension_item/element_name", true);


        //experiments
        addShortCut(EXPERIMENT_DATA, "ID", "xnat:experimentdata/id");
        addShortCut(EXPERIMENT_DATA, "visit_id", "xnat:experimentdata/visit_id");
        addShortCut(EXPERIMENT_DATA, "visit", "xnat:experimentdata/visit");
        addShortCut(EXPERIMENT_DATA, "date", "xnat:experimentdata/date");
        addShortCut(EXPERIMENT_DATA, "time", "xnat:experimentdata/time");
        addShortCut(EXPERIMENT_DATA, "note", "xnat:experimentdata/note");
        addShortCut(EXPERIMENT_DATA, "pi_firstname", "xnat:experimentdata/investigator/firstname");
        addShortCut(EXPERIMENT_DATA, "pi_lastname", "xnat:experimentdata/investigator/lastname");
        addShortCut(EXPERIMENT_DATA, "validation_method", "xnat:experimentdata/validation/method");
        addShortCut(EXPERIMENT_DATA, "validation_status", "xnat:experimentdata/validation/status");
        addShortCut(EXPERIMENT_DATA, "validation_date", "xnat:experimentdata/validation/date");
        addShortCut(EXPERIMENT_DATA, "validation_notes", "xnat:experimentdata/validation/notes");
        addShortCut(EXPERIMENT_DATA, "project", "xnat:experimentdata/project");
        addShortCut(EXPERIMENT_DATA, "label", "xnat:experimentdata/label");

        addShortCut(EXPERIMENT_DATA, "scanner", "xnat:imagesessiondata/scanner");
        addShortCut(EXPERIMENT_DATA, "operator", "xnat:imagesessiondata/operator");
        addShortCut(EXPERIMENT_DATA, "dcmAccessionNumber", "xnat:imagesessiondata/dcmaccessionnumber");
        addShortCut(EXPERIMENT_DATA, "dcmPatientId", "xnat:imagesessiondata/dcmpatientid");
        addShortCut(EXPERIMENT_DATA, "dcmPatientName", "xnat:imagesessiondata/dcmpatientname");
        addShortCut(EXPERIMENT_DATA, "session_type", "xnat:imagesessiondata/session_type");
        addShortCut(EXPERIMENT_DATA, "modality", "xnat:imagesessiondata/modality");
        addShortCut(EXPERIMENT_DATA, "UID", "xnat:imagesessiondata/uid");
        addShortCut(EXPERIMENT_DATA, "studyInstanceUID", "xnat:imagesessiondata/uid");

        addShortCut(EXPERIMENT_DATA, "coil", "xnat:mrsessiondata/coil");
        addShortCut(EXPERIMENT_DATA, "fieldStrength", "xnat:mrsessiondata/fieldstrength");
        addShortCut(EXPERIMENT_DATA, "marker", "xnat:mrsessiondata/marker");
        addShortCut(EXPERIMENT_DATA, "stabilization", "xnat:mrsessiondata/stabilization");

        addShortCut(EXPERIMENT_DATA, "studyType", "xnat:petsessiondata/studytype");
        addShortCut(EXPERIMENT_DATA, "patientID", "xnat:petsessiondata/patientid");
        addShortCut(EXPERIMENT_DATA, "patientName", "xnat:petsessiondata/patientname");
        addShortCut(EXPERIMENT_DATA, "stabilization", "xnat:petsessiondata/stabilization");
        addShortCut(EXPERIMENT_DATA, "scan_start_time", "xnat:petsessiondata/start_time_scan");
        addShortCut(EXPERIMENT_DATA, "injection_start_time", "xnat:petsessiondata/start_time_injection");
        addShortCut(EXPERIMENT_DATA, "tracer_name", "xnat:petsessiondata/tracer/name");
        addShortCut(EXPERIMENT_DATA, "tracer_startTime", "xnat:petsessiondata/tracer/starttime");
        addShortCut(EXPERIMENT_DATA, "tracer_dose", "xnat:petsessiondata/tracer/dose");
        addShortCut(EXPERIMENT_DATA, "tracer_sa", "xnat:petsessiondata/tracer/specificactivity");
        addShortCut(EXPERIMENT_DATA, "tracer_totalmass", "xnat:petsessiondata/tracer/totalmass");
        addShortCut(EXPERIMENT_DATA, "tracer_intermediate", "xnat:petsessiondata/tracer/intermediate");
        addShortCut(EXPERIMENT_DATA, "tracer_isotope", "xnat:petsessiondata/tracer/isotope");
        addShortCut(EXPERIMENT_DATA, "tracer_isotope", "xnat:petsessiondata/tracer/isotope/half-life");
        addShortCut(EXPERIMENT_DATA, "tracer_transmissions", "xnat:petsessiondata/tracer/transmissions");
        addShortCut(EXPERIMENT_DATA, "tracer_transmissions_start", "xnat:petsessiondata/tracer/transmissions_starttime");

        addShortCut(EXPERIMENT_DATA, "subject_ID", "xnat:subjectassessordata/subject_id");
        addShortCut(EXPERIMENT_DATA, "subject_label", "xnat:subjectdata/label", true);
        addShortCut(EXPERIMENT_DATA, "subject_project", "xnat:subjectdata/project", true);

        addShortCut(EXPERIMENT_DATA, "session_ID", "xnat:imagesessiondata/id", true);
        addShortCut(EXPERIMENT_DATA, "session_label", "xnat:imagesessiondata/label", true);
        addShortCut(EXPERIMENT_DATA, "session_project", "xnat:imagesessiondata/project", true);

        addShortCut(EXPERIMENT_DATA, "insert_date", "xnat:experimentData/meta/insert_date", true);
        addShortCut(EXPERIMENT_DATA, "insert_user", "xnat:experimentData/meta/insert_user/login", true);
        addShortCut(EXPERIMENT_DATA, "last_modified", "xnat:experimentData/meta/last_modified", true);
        addShortCut(EXPERIMENT_DATA, "xsiType", "xnat:experimentData/extension_item/element_name", true);


        //subjects
        addShortCut(SUBJECT_DATA, "project", "xnat:subjectData/project");
        addShortCut(SUBJECT_DATA, "label", "xnat:subjectData/label");
        addShortCut(SUBJECT_DATA, "ID", "xnat:subjectData/ID");

        addShortCut(SUBJECT_DATA, "group", "xnat:subjectData/group");
        addShortCut(SUBJECT_DATA, "src", "xnat:subjectData/src");
        addShortCut(SUBJECT_DATA, "pi_firstname", "xnat:subjectData/investigator/firstname");
        addShortCut(SUBJECT_DATA, "pi_lastname", "xnat:subjectData/investigator/lastname");
        addShortCut(SUBJECT_DATA, "dob", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/dob");
        addShortCut(SUBJECT_DATA, "yob", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/yob");
        addShortCut(SUBJECT_DATA, "age", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/age");
        addShortCut(SUBJECT_DATA, "gender", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gender");
        addShortCut(SUBJECT_DATA, "handedness", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/handedness");
        addShortCut(SUBJECT_DATA, "ses", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/ses");
        addShortCut(SUBJECT_DATA, "education", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/education");
        addShortCut(SUBJECT_DATA, "educationDesc", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/educationDesc");
        addShortCut(SUBJECT_DATA, "race", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/race");
        addShortCut(SUBJECT_DATA, "ethnicity", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/ethnicity");
        addShortCut(SUBJECT_DATA, "weight", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/weight");
        addShortCut(SUBJECT_DATA, "height", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/height");
        addShortCut(SUBJECT_DATA, "gestational_age", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/gestational_age");
        addShortCut(SUBJECT_DATA, "post_menstrual_age", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/post_menstrual_age");
        addShortCut(SUBJECT_DATA, "birth_weight", "xnat:subjectData/demographics[@xsi:type=xnat:demographicData]/birth_weight");

        addShortCut(SUBJECT_DATA, "insert_date", "xnat:subjectData/meta/insert_date", true);
        addShortCut(SUBJECT_DATA, "insert_user", "xnat:subjectData/meta/insert_user/login", true);
        addShortCut(SUBJECT_DATA, "last_modified", "xnat:subjectData/meta/last_modified", true);

        //recon
        addShortCut(RECON_DATA, "ID", "xnat:reconstructedImageData/ID");
        addShortCut(RECON_DATA, "type", "xnat:reconstructedImageData/type");
        addShortCut(RECON_DATA, "baseScanType", "xnat:reconstructedImageData/baseScanType");
        addShortCut(RECON_DATA, "xnat_reconstructedimagedata_id", "xnat:reconstructedImageData/xnat_reconstructedimagedata_id", true);
        addShortCut(RECON_DATA, "xsiType", "xnat:reconstructedImageData/extension_item/element_name",true);
        
        //assessor
        addShortCut(DERIVED_DATA, "ID", "xnat:experimentdata/ID");
        addShortCut(DERIVED_DATA, "visit_id", "xnat:experimentdata/visit_id");
        addShortCut(DERIVED_DATA, "date", "xnat:experimentdata/date");
        addShortCut(DERIVED_DATA, "time", "xnat:experimentdata/time");
        addShortCut(DERIVED_DATA, "note", "xnat:experimentdata/note");
        addShortCut(DERIVED_DATA, "pi_firstname", "xnat:experimentdata/investigator/firstname");
        addShortCut(DERIVED_DATA, "pi_lastname", "xnat:experimentdata/investigator/lastname");
        addShortCut(DERIVED_DATA, "validation_method", "xnat:experimentdata/validation/method");
        addShortCut(DERIVED_DATA, "validation_status", "xnat:experimentdata/validation/status");
        addShortCut(DERIVED_DATA, "validation_date", "xnat:experimentdata/validation/date");
        addShortCut(DERIVED_DATA, "validation_notes", "xnat:experimentdata/validation/notes");
        addShortCut(DERIVED_DATA, "project", "xnat:experimentdata/project");
        addShortCut(DERIVED_DATA, "label", "xnat:experimentdata/label");

        addShortCut(DERIVED_DATA, "session_ID", "xnat:imagesessiondata/id", true);
        addShortCut(DERIVED_DATA, "session_label", "xnat:imagesessiondata/label", true);
        addShortCut(DERIVED_DATA, "session_project", "xnat:imagesessiondata/project", true);

        addShortCut(DERIVED_DATA, "insert_date", "xnat:experimentData/meta/insert_date", true);
        addShortCut(DERIVED_DATA, "insert_user", "xnat:experimentData/meta/insert_user/login", true);
        addShortCut(DERIVED_DATA, "last_modified", "xnat:experimentData/meta/last_modified", true);
        addShortCut(DERIVED_DATA, "xsiType", "xnat:experimentData/extension_item/element_name", true);

        //project
        addShortCut(PROJECT_DATA, "ID", "xnat:projectData/ID");
        addShortCut(PROJECT_DATA, "secondary_ID", "xnat:projectData/secondary_ID");
        addShortCut(PROJECT_DATA, "name", "xnat:projectData/name");
        addShortCut(PROJECT_DATA, "description", "xnat:projectData/description");
        addShortCut(PROJECT_DATA, "keywords", "xnat:projectData/keywords");
        addShortCut(PROJECT_DATA, "alias", "xnat:projectData/aliases/alias/alias");
        addShortCut(PROJECT_DATA, "pi_firstname", "xnat:projectData/PI/firstname");
        addShortCut(PROJECT_DATA, "pi_lastname", "xnat:projectData/PI/lastname");
        addShortCut(PROJECT_DATA, "note", "xnat:projectData/fields/field[name=note]/field");

        //visit
        addShortCut(VISIT_DATA, "ID", "xnat:pvisitData/ID");
        addShortCut(VISIT_DATA, "visit_type", "xnat:pvisitData/visit_type");
        addShortCut(VISIT_DATA, "visit_name", "xnat:pvisitData/visit_name");
        addShortCut(VISIT_DATA, "closed", "xnat:pvisitData/closed");
        addShortCut(VISIT_DATA, "project", "xnat:pvisitdata/project");
        addShortCut(VISIT_DATA, "date", "xnat:pvisitdata/date");
        addShortCut(VISIT_DATA, "xsiType", "xnat:pvisitdata/extension_item/element_name", true);
        addShortCut(VISIT_DATA, "label", "xnat:pvisitdata/label");
        addShortCut(VISIT_DATA, "insert_date", "xnat:pvisitdata/meta/insert_date", true);
        addShortCut(VISIT_DATA, "insert_user", "xnat:pvisitdata/meta/insert_user/login", true);
        addShortCut(VISIT_DATA, "last_modified", "xnat:pvisitdata/meta/last_modified", true);
        addShortCut(VISIT_DATA, "subject_ID", "xnat:pvisitdata/subject_id");

    }

    public void addShortCut(final String xsiType, final String key, final String path) {
        addShortCut(xsiType, key, path, false);
    }

    public void addShortCut(final String xsiType, final String key, final String path, boolean readOnly) {
        if (!shortcuts.containsKey(xsiType)) {
            shortcuts.put(xsiType.intern(), new Hashtable<String, String>());
            readonly.put(xsiType.intern(), new Hashtable<String, String>());
            embeddedTypeShortcuts.put(xsiType.intern(), new Hashtable<String, String>());
        }

        if (readOnly) {
            readonly.get(xsiType).put(key.intern(), path.intern());
        } else {
            shortcuts.get(xsiType).put(key.intern(), path.intern());
            if (path.matches("^.*\\[@xsi:type=[A-z0-9:]+\\].*$")) {
                embeddedTypeShortcuts.get(xsiType.intern()).put(path.replaceAll("\\[@xsi:type=[A-z0-9:]+\\]", ""), path.intern());
            }
        }
    }

    public static synchronized XMLPathShortcutsI getInstance() {
        if (instance == null) {
            instance = new XMLPathShortcuts();
        }
        return instance;
    }

    /**
     * Return parameters with keys that start with XSI:TYPEs or match shortcuts
     *
     * @param params    The parameters to check.
     * @param type      The data type to check.
     * @param readOnly  Whether read-only fields should be considered
     * @return The usable fields from the list of parameters.
     */
    public static Map<String, Object> identifyUsableFields(final Map<String, Object> params, final String type, boolean readOnly) {
        return XMLPathShortcuts.getInstance().identifyFields(params, type, readOnly);
    }

    @SuppressWarnings("serial")
    final static List<String> REGEXP = new ArrayList<String>() {{
        add(XNATRestConstants.XML_PATH_REGEXP);
        add(XNATRestConstants.XML_PATH_REGEXP2);
    }};

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> identifyFields(final Map<String, Object> params, final String type, boolean readOnly) {
        final Map<String, Object> relevant = new HashMap<>();

        if (params != null) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                for (final String reg : REGEXP) {
                    if ((entry.getKey()).matches(reg)) {
                        relevant.put(entry.getKey(), entry.getValue());
                    }
                }
                if (shortcuts.get(type).containsKey(entry.getKey())) {
                    relevant.put(shortcuts.get(type).get(entry.getKey()), entry.getValue());
                }

                if (readOnly) {
                    if (readonly.get(type).containsKey(entry.getKey())) {
                        relevant.put(readonly.get(type).get(entry.getKey()), entry.getValue());
                    }
                }
            }
        }

        return relevant;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getShortcuts(String type, boolean readOnly) {
        final Map<String, String> temp = new HashMap<>(shortcuts.get(type));
        if (readOnly) {
            temp.putAll(readonly.get(type));
        }
        return temp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getEmbeddedTypeShortcuts(String type) {
        return embeddedTypeShortcuts.get(type);
    }
}
