/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.AttributeMapXnatImagesessiondataBeanFactory
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.nrg.dcm.DicomAttributeIndex;
import org.nrg.dcm.DicomMetadataStore;
import org.nrg.xdat.bean.XnatImagesessiondataBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.nrg.dcm.NamedAttributes.StudyInstanceUID;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class AttributeMapXnatImagesessiondataBeanFactory extends XnatImagesessiondataBeanFactory {
    private final DicomAttributeIndex attribute;
    private final Map<String,Class<? extends XnatImagesessiondataBean>> classes;
    private final Function<Set<String>,String> leadExtractor;
    
    public AttributeMapXnatImagesessiondataBeanFactory(final DicomAttributeIndex attribute, final Map<String,String> keysToDataTypes, final Function<Set<String>,String> leadExtractor) {
        this.attribute = attribute;
        this.leadExtractor = leadExtractor;
        this.classes = ImmutableMap.copyOf(Maps.transformValues(keysToDataTypes, XnatClassMapping.forBaseClass(XnatImagesessiondataBean.class)));
    }

    public AttributeMapXnatImagesessiondataBeanFactory(final DicomAttributeIndex attribute, final List<String> sessionTypes, final Function<Set<String>,String> leadExtractor) {
        this.attribute = attribute;
        this.leadExtractor = leadExtractor;

        final XnatClassMapping<XnatImagesessiondataBean> classMapping = XnatClassMapping.forBaseClass(XnatImagesessiondataBean.class);
        this.classes = sessionTypes.stream().collect(Collectors.toMap(java.util.function.Function.identity(), classMapping::apply));
    }

    private static <K,C> C getInstance(final K key, final Map<? extends K,Class<? extends C>> classes) {
        if (null == key) {
            return null;
        }
        final Class<? extends C> clazz = classes.get(key);
        if (null == clazz) {
            return null;
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.nrg.dcm.xnat.XnatImagesessiondataBeanFactory#create(org.nrg.dcm.DicomMetadataStore, java.lang.String)
     */
    @Override
    public XnatImagesessiondataBean create(DicomMetadataStore store, String studyInstanceUID) {
        return create(store, studyInstanceUID, null);
    }

    @Override
    public XnatImagesessiondataBean create(final DicomMetadataStore store, final String studyInstanceUID, final Map<String, String> parameters) {
        final Pair<Set<String>, XnatImagesessiondataBean> attributeValuesAndBean = getAttributeValuesAndBean(store, studyInstanceUID);
        if (attributeValuesAndBean.equals(ImmutablePair.<Set<String>, XnatImagesessiondataBean>nullPair())) {
            return null;
        }
        return attributeValuesAndBean.getValue();
    }

    protected Pair<Set<String>, XnatImagesessiondataBean> getAttributeValuesAndBean(final DicomMetadataStore store, final String studyInstanceUID) {
        // Next try simple SOP class mapping
        final SetMultimap<DicomAttributeIndex, String> attributeValues = getValues(store, ImmutableMap.of(StudyInstanceUID, studyInstanceUID), Collections.singleton(attribute));
        if (null == attributeValues) {
            return ImmutablePair.nullPair();
        }
        final Set<String> values = attributeValues.get(attribute);
        if (null == values || values.isEmpty()) {
            return ImmutablePair.nullPair();
        }
        return Pair.of(values, getInstance(leadExtractor.apply(values), classes));
    }
}
