/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.ModalityMapXnatImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.base.Function;
import org.nrg.dcm.SOPModel;

import java.util.Set;

import static org.nrg.dcm.NamedAttributes.Modality;

public class ModalityMapXnatImagesessiondataBeanFactory extends AttributeMapXnatImagesessiondataBeanFactory {
    private final static Function<Set<String>,String> LEAD_MODALITY = new Function<Set<String>,String>() {
        public String apply(final Set<String> modalities) {
            return SOPModel.getLeadModality(modalities);
        }
    };

    public ModalityMapXnatImagesessiondataBeanFactory() {
        super(Modality, SOPModel.getModalityToSessionTypes(), LEAD_MODALITY);
    }
}
