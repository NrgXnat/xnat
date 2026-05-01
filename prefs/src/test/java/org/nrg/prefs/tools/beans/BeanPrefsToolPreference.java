/*
 * prefs: org.nrg.prefs.tools.beans.BeanPrefsToolPreference
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.prefs.tools.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * For this test, this class represents any preference that might be set as a full object, i.e. as a bean. This
 * particular bean is based on the requirements for the DICOM SCP manager service, which manages multiple instances of
 * DICOM SCP receivers. The service manages instances of a worker that is configured via the property
 * values set in each bean.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(prefix = "_")
@Builder
public class BeanPrefsToolPreference {
    private String  _scpId;
    private String  _aeTitle;
    private int     _port;
    private String  _identifier;
    private String  _fileNamer;
    @Builder.Default
    private boolean _enabled = true;
}
