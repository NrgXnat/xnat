/*
 * framework: org.nrg.framework.processors.XnatTaskAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import com.google.common.base.Joiner;
import org.kohsuke.MetaInfServices;
import org.nrg.framework.task.XnatTask;

import javax.annotation.processing.Processor;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Processes the {@link XnatTask} annotation.  This annotation notates a class that might require node-specific handling
 * (e.g. that task should execute only on a single node in a multi-node environment). 
 */
@MetaInfServices(Processor.class)
@SupportedAnnotationTypes("org.nrg.framework.task.XnatTask")
public class XnatTaskAnnotationProcessor extends NrgAbstractAnnotationProcessor<XnatTask> {

    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#processAnnotation(javax.lang.model.element.TypeElement, java.lang.annotation.Annotation)
     */
    @Override
    protected Map<String, String> processAnnotation(final TypeElement element, final XnatTask task) {
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(XnatTask.CLASS, element.getQualifiedName().toString());
        properties.put(XnatTask.TASK_ID, task.taskId());
        properties.put(XnatTask.DESCRIPTION, task.description());
        properties.put(XnatTask.DEFAULT_EXECUTION_RESOLVER, task.defaultExecutionResolver());
        properties.put(XnatTask.EXECUTION_RESOLVER_CONFIGURABLE, String.valueOf(task.executionResolverConfigurable()));
        properties.put(XnatTask.ALLOWED_EXECUTION_RESOLVERS, Joiner.on(",").join(task.allowedExecutionResolvers()));
        properties.put("javaClass", element.getQualifiedName().toString());
        return properties;
    }

    /* (non-Javadoc)
     * @see org.nrg.framework.processors.NrgAbstractAnnotationProcessor#getPropertiesName(java.lang.annotation.Annotation)
     */
    @Override
    protected String getPropertiesName(final TypeElement element, final XnatTask task) {
    	final String taskId = task.taskId();
        return String.format("META-INF/xnat/task/%s-xnat-task.properties", taskId);
    }
}
