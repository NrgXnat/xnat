package org.nrg.dcm.xnat;

import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.nrg.attr.ConversionFailureException;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomMetadataStore;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Map;

public abstract class XnatBeanFactory {
    protected Logger logger;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected SetMultimap<DicomAttributeIndex, String> getValues(final DicomMetadataStore store,
                                                                 Map<DicomAttributeIndex, String> idMap,
                                                                 Collection<DicomAttributeIndex> targetAttributes) {
        final Map<DicomAttributeIndex, ConversionFailureException> failures = Maps.newLinkedHashMap();
        SetMultimap<DicomAttributeIndex, String> values;
        try {
            values = store.getUniqueValuesGiven(idMap, targetAttributes, failures);
        } catch (Throwable t) {
            logger.error("unable to convert attributes " + targetAttributes, t);
            return null;
        }

        for (final Map.Entry<DicomAttributeIndex,ConversionFailureException> fme : failures.entrySet()) {
            logger.error("unable to convert " + fme.getKey(), fme.getValue());
        }

        return values;
    }
}
