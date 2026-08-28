/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XnatPetmrImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che3.data.UID;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomMetadataStore;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.preferences.HandlePetMr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.nrg.dcm.NamedAttributes.Modality;
import static org.nrg.dcm.NamedAttributes.SOPClassUID;
import static org.nrg.dcm.NamedAttributes.SeriesDescription;
import static org.nrg.dcm.NamedAttributes.StudyInstanceUID;

/**
 * Creates a PET/MR session if both MR and 2PET SOPs are present in the named study.
 */
@Slf4j
public final class XnatPetmrImagesessiondataBeanFactory extends XnatImagesessiondataBeanFactory {
    private static final Iterable<String>  PET_SOPs = Arrays.asList(UID.PositronEmissionTomographyImageStorage, UID.EnhancedPETImageStorage);
    private static final Iterable<String>  MR_SOPs  = Arrays.asList(UID.MRImageStorage, UID.EnhancedMRImageStorage);
    private static final List<Set<String>> SOP_COMBINATIONS;

    static {
        final ImmutableList.Builder<Set<String>> builder = ImmutableList.builder();
        for (final String petSOP : PET_SOPs) {
            for (final String mrSOP : MR_SOPs) {
                builder.add(ImmutableSet.of(petSOP, mrSOP));
            }
        }
        SOP_COMBINATIONS = builder.build();
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.xnat.XnatImagesessiondataBeanFactory#create(org.nrg.dcm.DicomMetadataStore, java.lang.String)
     */
    public XnatImagesessiondataBean create(final DicomMetadataStore store, final String studyInstanceUID) {
        return create(store, studyInstanceUID, null);
    }

    @Override
    public XnatImagesessiondataBean create(final DicomMetadataStore store, final String studyInstanceUID, final Map<String, String> parameters) {
        final SetMultimap<DicomAttributeIndex, String> values = getValues(store, ImmutableMap.of(StudyInstanceUID, studyInstanceUID),
                                                                          Collections.unmodifiableCollection(Arrays.asList(SOPClassUID, SeriesDescription, Modality)));

        if (null == values || values.isEmpty()) {
            return null;
        }

        final Set<String> sopClassUIDs = values.get(SOPClassUID);
        if (null == sopClassUIDs || sopClassUIDs.isEmpty()) {
            return null;
        }
        final HandlePetMr separatePetMr = HandlePetMr.getParameter(parameters);
        for (final Set<String> sops : SOP_COMBINATIONS) {
            if (sopClassUIDs.containsAll(sops)) {
                // If it's set to pet, we always create a PET session bean, even if it is a PET/MR, so there's no need to inspect it.
                if (separatePetMr == HandlePetMr.Pet) {
                    return new XnatPetsessiondataBean();
                }
                // Otherwise, we've met the PET/MR criteria, so they'll get a PET/MR session bean.
                return new XnatPetmrsessiondataBean();
            }
        }
        return null;
    }
}
