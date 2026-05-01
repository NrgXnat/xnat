package org.nrg.dcm.xnat.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.nrg.attr.EvaluableAttrDef;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.MutableAttrDefs;
import org.nrg.dcm.xnat.XnatAttrDef;
import org.nrg.dcm.xnat.entities.DicomMappingEntity;
import org.nrg.dcm.xnat.exceptions.InvalidEntityException;
import org.nrg.dcm.xnat.pojos.DicomMapping;
import org.nrg.dcm.xnat.services.DicomMappingEntityService;
import org.nrg.dcm.xnat.services.DicomMappingService;
import org.nrg.dcm.xnat.utils.DicomMappingUtils;
import org.nrg.framework.constants.Scope;
import org.nrg.framework.exceptions.NotFoundException;
import org.nrg.xdat.bean.XnatAddfieldBean;
import org.nrg.xdat.bean.XnatImagescandataBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.base.auto.AutoXnatProjectdata;
import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xft.security.UserI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DicomMappingServiceImpl implements DicomMappingService {
    private final DicomMappingEntityService dicomMappingEntityService;

    @Autowired
    public DicomMappingServiceImpl(final DicomMappingEntityService dicomMappingEntityService) {
        this.dicomMappingEntityService = dicomMappingEntityService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public MutableAttrDefs getAddlAttributesForMapping(@Nullable String project, String schemaElement)
            throws NotFoundException {
        List<DicomMappingEntity> entities = dicomMappingEntityService.find(project, schemaElement);
        MutableAttrDefs attrDefs = new MutableAttrDefs();
        for (DicomMappingEntity entity : entities) {
            int dicomTag = Integer.decode(entity.getDicomTag());
            String fieldName = entity.getFieldName();
            EvaluableAttrDef<DicomAttributeIndex, ?, ?> attr;
            switch (entity.getFieldType()) {
                case INTEGER:
                    attr = new XnatAttrDef.Int(fieldName, dicomTag);
                    break;
                case FLOAT:
                    attr = new XnatAttrDef.Real(fieldName, dicomTag);
                    break;
                case BOOLEAN:
                    attr = new XnatAttrDef.BooleanAttr(fieldName, dicomTag);
                    break;
                case DATE:
                    attr = new XnatAttrDef.Date(fieldName, dicomTag);
                    break;
                case TIME:
                    attr = new XnatAttrDef.Time(fieldName, dicomTag);
                    break;
                case STRING:
                default:
                    attr = new XnatAttrDef.Text(fieldName, dicomTag);
            }
            if (entity.isAddParamAttribute()) {
                attrDefs.add(new XnatAttrDef.AddParam<>(attr));
            } else {
                attrDefs.add(new XnatAttrDef.AddField<>(attr));
            }
        }
        return attrDefs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DicomMapping> getAllPojos() {
        List<DicomMapping> pojos = new ArrayList<>();
        for (DicomMappingEntity entity : dicomMappingEntityService.getAll()) {
            pojos.add(entity.toPojo());
        }
        return pojos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createOrUpdateFromPojo(DicomMapping pojo, UserI user)
            throws NotFoundException, XFTInitException, InvalidEntityException {
        long id = pojo.getId();
        DicomMappingEntity entity;
        if (id == 0L) {
            // creating
            entity = new DicomMappingEntity();
            setParamsFromPojoAndValidate(entity, pojo, user);
            DicomMappingEntity created = dicomMappingEntityService.create(entity);
            if (created == null) {
                throw new HibernateException("Unable to store entity");
            }
        } else {
            entity = dicomMappingEntityService.get(id);
            setParamsFromPojoAndValidate(entity, pojo, user);
            dicomMappingEntityService.update(entity);
        }
    }

    /**
     * Validate the entity:
     * 1. Ensure that schema element is valid XNAT element that extends xnatImageSession or xnatImageScan and if the
     * latter, has an addParam method (also set addParamAttribute field)
     * 2. Ensure that DICOM tag is valid hex integer
     *
     * @param entity the entity
     * @param user the user
     * @throws XFTInitException if XNAT isn't initialized
     * @throws InvalidEntityException if the entity is not valid per above
     */
    private void validate(DicomMappingEntity entity, UserI user)
            throws XFTInitException, InvalidEntityException {

        // Validate schema element
        String schemaElement = entity.getSchemaElement();
        try {
            // validate that schema element matches an xnat element
            Class<?> elementBeanClass = SchemaElement.GetElement(schemaElement).getCorrespondingJavaBeanClass();
            if (XnatImagescandataBean.class.isAssignableFrom(elementBeanClass)) {
                entity.setAddParamAttribute(true);
                try {
                    DicomMappingUtils.checkBeanHasAddParamMethod(elementBeanClass, schemaElement);
                } catch (NoSuchMethodException | ElementNotFoundException | ClassNotFoundException e) {
                    throw new InvalidEntityException("schemaElement " + schemaElement + " does not include a " +
                            "parameters/addParam field", e);
                }
            } else if (XnatImagesessiondataBean.class.isAssignableFrom(elementBeanClass)) {
                entity.setAddParamAttribute(false);
            } else {
                throw new InvalidEntityException("schemaElement " + schemaElement + " must either extend " +
                        XnatImagesessiondata.SCHEMA_ELEMENT_NAME + " or " + XnatImagescandata.SCHEMA_ELEMENT_NAME);
            }
        } catch (ElementNotFoundException | ClassNotFoundException e) {
            throw new InvalidEntityException("schemaElement " + schemaElement + " is not part of XNAT", e);
        }

        // Validate DICOM tag
        String dicomTag = entity.getDicomTag();
        try {
            // validate that dicomTag is valid hex integer
            Integer tagInt = Integer.decode(dicomTag);
            // Skip the below - just try to extract whatever is provided and throw exception downstream if invalid
            // validate that tag is known by dcm4che
            //String tagName = ElementDictionary.getDictionary().nameOf(tagInt);
            //if (tagName == null || ElementDictionary.getUnkown().equals(tagName)) {
            //    throw new InvalidEntityException("dicomTag " + dicomTag + " (int: " + tagInt + ") is not recognized " +
            //            "by the dcm4che library");
            //}
        } catch (NumberFormatException e) {
            throw new InvalidEntityException("dicomTag " + dicomTag + " is not a valid hexadecimal integer", e);
        }

        // Validate scope
        Scope scope = entity.getScope();
        switch (scope) {
            case Site:
                break;
            case Project:
                String projectId = entity.getScopeObjectId();
                XnatProjectdata project = AutoXnatProjectdata.getXnatProjectdatasById(projectId, user, false);
                if (project == null) {
                    throw new InvalidEntityException("No such project " + projectId + ", please use scope " +
                            Scope.Site + " or provide a valid project");
                }
                break;
            default:
                throw new InvalidEntityException("scope " + scope + " is not supported for DICOM mapping");
        }
    }

    /**
     * Set entity params from pojo and then validate the entity
     * @param entity the entity
     * @param pojo the pojo
     * @param user the user
     * @throws XFTInitException if XNAT not initialized
     * @throws InvalidEntityException in entity invalid
     */
    private void setParamsFromPojoAndValidate(DicomMappingEntity entity, DicomMapping pojo, UserI user)
            throws XFTInitException, InvalidEntityException {
        entity.setSchemaElement(pojo.getSchemaElement());
        entity.setScope(pojo.getScope());
        entity.setScopeObjectId(pojo.getScopeObjectId());
        entity.setFieldName(pojo.getFieldName());
        entity.setFieldType(pojo.getFieldType());
        entity.setDicomTag(pojo.getDicomTag());
        validate(entity, user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(long id) {
        dicomMappingEntityService.delete(id);
    }
}
