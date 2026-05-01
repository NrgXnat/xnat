/*
 * framework: org.nrg.framework.processors.XnatMixInAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import org.kohsuke.MetaInfServices;
import org.nrg.framework.annotations.XnatMixIn;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@inheritDoc}
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.annotations.XnatMixIn")
public class XnatMixInAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatMixIn> {
    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatMixIn mixIn) {
        final List<String> values         = getTypeElementValues(element, "value");
        final List<String> targets        = getTypeElementValues(element, "targets");
        final String       annotatedClass = element.getQualifiedName().toString();

        final boolean hasValue   = values != null && !values.isEmpty();
        final boolean hasTargets = targets != null && !targets.isEmpty();

        if (!hasValue && !hasTargets) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You must specify at least a mix-in class with the value attribute or .");
        }

        if (hasValue && values.size() > 1) {
            throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "You can only specify a single mix-in class with the value attribute.");
        }

        // If there's a value specified for the value attribute, then that's the mix-in class. Otherwise the annotated
        // class is the mix-in class. There's only one mix-in class that can be set for each instance of this annotation
        // so we can set it once and not have to deal with it any more.
        final String mixInClass = hasValue ? values.get(0) : annotatedClass;

        // If there's a value specified for the targets attribute, then that specifies the classes targeted by the
        // mix-in class. If there's not any targets specified, then the "targets" is a list of one: the annotated class.
        final List<String> targetClasses = hasTargets ? targets : Collections.singletonList(annotatedClass);

        final Map<String, String> properties = new LinkedHashMap<>();
        for (final String targetClass : targetClasses) {
            properties.put(targetClass, mixInClass);
        }
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropertiesName(final TypeElement element, final XnatMixIn mixIn) {
        return String.format("META-INF/xnat/serializers/%s-mixin.properties", element.getQualifiedName().toString());
    }
}
