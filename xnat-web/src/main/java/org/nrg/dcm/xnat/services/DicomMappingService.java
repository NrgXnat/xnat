package org.nrg.dcm.xnat.services;

import org.nrg.dcm.MutableAttrDefs;
import org.nrg.dcm.xnat.exceptions.InvalidEntityException;
import org.nrg.dcm.xnat.pojos.DicomMapping;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface DicomMappingService {

    /**
     * Get MutableAttrDefs for all "ad-hoc" attributes configured for this site and project (if not null)
     * @param project the project or null
     * @param schemaElement the schema element
     * @return attribute defs
     * @throws NotFoundException if no ad-hoc attributes have been configured
     */
    @Nonnull
    MutableAttrDefs getAddlAttributesForMapping(@Nullable String project, String schemaElement)
            throws NotFoundException;

    /**
     * Get a list of all DICOM mapping instances.
     * @return A list of all DICOM mapping instances.
     */
    List<DicomMapping> getAllPojos();

    void createOrUpdateFromPojo(final DicomMapping pojo, final UserI user) throws NotFoundException, XFTInitException, ElementNotFoundException, InvalidEntityException;

    void delete(long id);
}
