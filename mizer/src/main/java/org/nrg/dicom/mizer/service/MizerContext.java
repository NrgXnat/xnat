package org.nrg.dicom.mizer.service;

import java.util.Map;
import java.util.Properties;

/**
 * MizerContext encapsulates the additional information {@link Mizer Mizers} need to anonymize DICOM objects.
 *
 * Essentially a Map of named Objects.  {@link Mizer} will examine MizerContext and indicate their ability to handle
 * a particular context via {@link Mizer#understands(MizerContext)}.
 *
 * The {@link #getScriptId() script ID} property is used when populating the <b>DeidentificationMethodCodeSequence</b>
 * DICOM header value to record the DICOM's de-identification history. The meaning of the script ID is dependent on the
 * particular hosting system. If the script ID is set to <b>0L</b> or {@link #isRecord()} is <b>false</b>, the
 * de-identification metadata is not updated.
 */
public interface MizerContext {
    void setElement(String key, Object value);

    Object getElement(String key);

    Map<String, Object> getElements();

    void add(Properties properties);

    long getScriptId();

    void setScriptId(final long scriptId);

    /**
     * Indicates whether the DICOM de-identification method should be recorded by inserting information about the
     * particular mizer and scripts that were used to process the data. This defaults to true.
     *
     * @return Returns true if the DICOM deidentification method(s) should be recorded.
     */
    boolean isRecord();

    /**
     * Sets whether the DICOM de-identification method should be recorded by inserting information about the particular
     * mizer and scripts that were used to process the data.
     *
     * @param record Whether if the DICOM de-identification method(s) should be recorded.
     */
    void setRecord(boolean record);

    /**
     * Indicates whether the DICOM de-identification method should ignore calls to 'reject'
     *
     * @return Returns true if the reject clause should be ignored if it occurs.
     */
    boolean ignoreRejection();

    /**
     * Sets whether the DICOM de-identification method should ignore calls to 'reject'
     *
     * @param ignoreRejection Whether the reject clause should be ignored if it occurs.
     */
    void setIgnoreRejection(boolean ignoreRejection);
}
