package org.nrg.dcm.xnat;

import static org.nrg.dcm.Attributes.SOPClassUID;
import static org.nrg.dcm.SOPModel.LEAD_SOP_EXTRACTOR;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.dcm.DicomMetadataStore;
import org.nrg.dcm.SOPModel;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatOtherdicomsessiondataBean;

import java.util.Map;
import java.util.Set;

@Slf4j
public class DefaultXnatImagesessiondataBeanFactory extends AttributeMapXnatImagesessiondataBeanFactory {
    public DefaultXnatImagesessiondataBeanFactory() {
        // Same as SOPMapXnatImagesessiondataBeanFactory
        super(SOPClassUID, SOPModel.getSessionTypesFromSOPMap(), LEAD_SOP_EXTRACTOR);
    }

    @Override
    public XnatImagesessiondataBean create(final DicomMetadataStore store, final String studyInstanceUID, final Map<String, String> parameters) {
        final Pair<Set<String>, XnatImagesessiondataBean> attributeValuesAndBean = getAttributeValuesAndBean(store, studyInstanceUID);
        if (attributeValuesAndBean.equals(ImmutablePair.<Set<String>, XnatImagesessiondataBean>nullPair())) {
            return null;
        }
        final XnatImagesessiondataBean bean = attributeValuesAndBean.getValue();
        if (bean != null) {
            return bean;
        }
        final Set<String> sopClassUids = attributeValuesAndBean.getKey();
        if (sopClassUids == null || sopClassUids.isEmpty()) {
            return null;
        }
        final String scanBean = SOPModel.getScanType(sopClassUids);
        if (StringUtils.isNotBlank(scanBean)) {
            log.info("Did not find a DICOM session type but did find scan type \"{}\" for SOP class UIDs, returning generic session bean: {}", scanBean, sopClassUids);
            return new XnatOtherdicomsessiondataBean();
        }
        log.info("Did not find a DICOM session or scan type for SOP class UIDs, returning null: {}", sopClassUids);
        return null;
    }
}
