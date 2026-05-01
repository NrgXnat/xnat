/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XnatPetmrImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import static org.nrg.dcm.Attributes.*;

import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.dcm4che2.data.UID;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomMetadataStore;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetmrsessiondataBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.nrg.xdat.preferences.HandlePetMr;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * Creates a PET/MR session if both MR and 2PET SOPs are present in the named study.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
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
                // If it's set to petmr or not set, but we've met the PET/MR criteria, then they'll get a PET/MR session bean.
                if (separatePetMr != HandlePetMr.Separate) {
                    return new XnatPetmrsessiondataBean();
                }
                // If it's set to separate, we have to check whether the MR series just contain PET attenuation data. In that case,
                // we're going to create a PET session bean.
                final Map<DicomAttributeIndex, String> constraints = new HashMap<>();
                constraints.put(Modality, "MR");
                final Map<DicomAttributeIndex, ConversionFailureException> failed = new HashMap<>();
                try {
                    final SetMultimap<DicomAttributeIndex, String> descriptions = store.getUniqueValuesGiven(constraints, Collections.singletonList(SeriesDescription), failed);
                    for (final DicomAttributeIndex attribute : descriptions.keySet()) {
                        for (final String value : descriptions.get(attribute)) {
                            // TODO: Use modality-map series import filters here to actually determine if the MR series really belong in a PET session or in a PET/MR session.
                            // TODO: See also line 719 in PrearcDatabase.java.
                            if (!value.contains("MRAC")) {
                                logger.info("Found a series description \"" + value + "\" that doesn't seem to be PET attenuation data, creating a PET/MR session.");
                                return new XnatPetmrsessiondataBean();
                            }
                        }
                    }
                } catch (SQLException | IOException e) {
                    logger.error("An error occurred trying to retrieve the series descriptions.", e);
                }
                return new XnatPetsessiondataBean();
            }
        }
        return null;
    }
}
