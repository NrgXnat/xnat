package org.nrg.dicomtools.filters;

import com.fasterxml.jackson.core.type.TypeReference;
import org.dcm4che2.data.DicomObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The series import filter is used to determine the appropriate study modality of a specific {@link DicomObject
 * DICOM object}. The study modality is often the same as the DICOM object modality, but not always. This can be
 * used to filter out particular series of a DICOM study&mdash;for example, a project may not want combined PET-MR
 * studies, but only want the MR series to create a MR study and the PET series <i>plus</i> a single MR series that
 * includes the PET attenuation data to create a separate PET study&mdash;or to determine the study modality of a
 * particular object in the context of its project&mdash;for example, during the gradual DICOM import process, the
 * previously mentioned MR series containing PET attenuation data would actually be placed into the PET study.
 */
public interface SeriesImportFilter {

    /**
     * Indicates the last tag of the DICOM headers that should be processed. This can be used to improve performance
     * by discontinuing processing and parsing of the DICOM headers at a certain point, avoiding processing headers
     * and binary data that won't be used.
     */
    String KEY_PROJECT_ID = "projectId";
    String KEY_ENABLED = "enabled";
    String KEY_MODE = "mode";
    String KEY_STRICT = "strict";
    String KEY_EXCLUDED = "excluded";
    String KEY_LIST = "list";
    String KEY_SERIES_DESCRIPTION = "SeriesDescription";
    String KEY_DEFAULT_MODALITY = "defaultModality";
    TypeReference<LinkedHashMap<String, String>> MAP_TYPE_REFERENCE = new TypeReference<LinkedHashMap<String, String>>() {};

    String getProjectId();

    void setProjectId(String projectId);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    /**
     * Indicates the desired mode for the series import filter.
     * @return The series import filter mode.
     */
    SeriesImportFilterMode getMode();

    /**
     * Sets the mode for the series import filter.
     * @param mode    The mode for the series import filter.
     */
    void setMode(SeriesImportFilterMode mode);

    /**
     * Gets the currently configured modality match. This is used to determine which expression {@link DicomObject
     * DICOM objects} should be evaluated against.
     * @return The modality against which you want to match subsequent calls to {@link #shouldIncludeDicomObject(DicomObject)}.
     */
    String getModality();

    /**
     * Sets the modality match. This is used to determine which expression {@link DicomObject DICOM objects} should be
     * evaluated against.
     * @param modality    The modality against which you want to match subsequent calls to {@link #shouldIncludeDicomObject(DicomObject)}.
     */
    void setModality(String modality);

    /**
     * Evaluates the incoming {@link DicomObject DICOM object} and returns the first modality that matches the corresponding
     * expression.
     * @param dicomObject    The DICOM object to evaluate.
     * @return The modality key corresponding to the matching expression.
     */
    String findModality(DicomObject dicomObject);

    /**
     * Evaluates the incoming headers and returns the first modality that matches the corresponding expression. This is
     * functionally equivalent to {@link #findModality(DicomObject)}, but with the headers of the {@link DicomObject DICOM
     * object} decomposed to a map of strings.
     * @param headers    The headers to evaluate.
     * @return The modality key corresponding to the matching expression.
     */
    String findModality(Map<String, String> headers);

    /**
     * Evaluates the expression associated with the {@link #getModality() specified modality} to see if this {@link DicomObject
     * DICOM object} should be included in a study of the given modality.
     * @param dicomObject    The DICOM object to be evaluated.
     * @return <b>true</b> if the DICOM object matches the expression parameters, <b>false</b> otherwise.
     */
    boolean shouldIncludeDicomObject(DicomObject dicomObject);

    /**
     * Evaluates the expression associated with the target modality parameter to see if an object with the incoming headers
     * should be included in a study of the given modality. If the target modality parameter is blank, the value set for
     * the {@link #getModality()} method is used instead.
     * @param dicomObject    The DICOM object to be evaluated.
     * @param targetModality The modality to check against.
     * @return <b>true</b> if the DICOM object matches the expression parameters, <b>false</b> otherwise.
     */
    boolean shouldIncludeDicomObject(DicomObject dicomObject, String targetModality);

    /**
     * Evaluates the expression associated with the {@link #getModality() specified modality} to see if an object with the
     * incoming headers should be included in a study of the given modality.
     * @param headers    The headers to be evaluated.
     * @return <b>true</b> if the headers match the expression parameters, <b>false</b> otherwise.
     */
    boolean shouldIncludeDicomObject(Map<String, String> headers);

    /**
     * Evaluates the expression associated with the target modality parameter to see if an object with the incoming headers
     * should be included in a study of the given modality. If the target modality parameter is blank, the value set for
     * the {@link #getModality()} method is used instead.
     * @param headers           The headers to be evaluated.
     * @param targetModality    The modality to check against.
     * @return <b>true</b> if the headers match the expression parameters, <b>false</b> otherwise.
     */
    boolean shouldIncludeDicomObject(Map<String, String> headers, String targetModality);

    /**
     * Converts the series import filter to a map for persistence.
     * @return The series import filter reduced to a map.
     */
    LinkedHashMap<String, String> toMap();

    /**
     * The same as {@link #toMap()}, but prefixes each of the map keys with a qualifier. This is useful for situations where
     * common key names&mdash;e.g. mode, enabled, description&mdash;might clash with other items.
     * @return The series import filter reduced to a map, with each key prefixed with a qualifier.
     */
    LinkedHashMap<String, String> toQualifiedMap();
}
