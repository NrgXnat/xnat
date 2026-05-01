/*
 * DicomEdit: org.nrg.dicom.dicomedit.functions.Function
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit.functions;

import com.google.common.base.Joiner;
import org.nrg.dicom.dicomedit.FindKnownValueVisitor;
import org.nrg.dicom.dicomedit.TagPathFactory;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for all script functions
 */
public abstract class BaseFunction {
    protected static final String DEFAULT_NAMESPACE = "";

    private String nameSpace;
    private String name;
    private String usage;
    private String description;
    private String fqn;
    // context required by this function from the external context, not supplied through function arguments.
    protected Map<String, Object> internalContext;

    private static final Logger logger = LoggerFactory.getLogger(BaseFunction.class);

    public static BaseFunction createInstance(Properties properties) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        final String fqn = properties.getProperty("class");
        final Class<? extends BaseFunction> functionClass = Class.forName(fqn).asSubclass(BaseFunction.class);
        final Constructor<? extends BaseFunction> constructor = functionClass.getConstructor(Properties.class);
        return constructor.newInstance(properties);
    }

    public BaseFunction(String name, String nameSpace, String usage, String description) {
        this.name = name;
        this.nameSpace = nameSpace;
        this.usage = usage;
        this.description = description;
        this.fqn = (DEFAULT_NAMESPACE.equals(nameSpace)) ? name : nameSpace + "_" + name;
        this.internalContext = new HashMap<>();
    }

     public abstract Value apply(List<Value> values, Map<String, Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException;

    public BaseFunction(Properties properties) {
        name = properties.getProperty("name");
        if (name == null) {
            String msg = "Failed to create script function. Required property \'name\' is missing.";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        nameSpace = properties.getProperty("namespace");
        if (nameSpace == null) {
            String msg = "Failed to create script function. Required property \'namespace\' is missing.";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        usage = "";
        description = "";
        this.fqn = (DEFAULT_NAMESPACE.equals(nameSpace)) ? name : nameSpace + "_" + name;
    }

    protected List<Value> flattenValueList(List<Value> values) {
        List<Value> flattenedValues = new ArrayList<>();
        for (Value v : values) {
            if (v.isScalar()) {
                flattenedValues.addAll(flattenValueList(v.asValueList()));
            } else {
                flattenedValues.add(v);
            }
        }
        return flattenedValues;
    }

    protected List<String> asStrings(List<Value> values, DicomObjectI dicomObject) {
        List<String> valuesAsStrings = new ArrayList<>();
        // separate tagPaths
        List<Value> valueList = flattenValueList(values);
        Map<Boolean, List<Value>> groups = valueList.stream()
                .collect(Collectors.partitioningBy(v -> v.asObject() instanceof TagPath));
        // add non-tagPath values to list of strings.
        groups.get(false).forEach(v -> valuesAsStrings.add(v.asString()));
        // add the string values matching tagPaths.
        List<TagPath> tagPaths = groups.get(true).stream().map(v -> (TagPath)v.asObject()).collect(Collectors.toList());
        FindKnownValueVisitor valueVisitor = new FindKnownValueVisitor(tagPaths);
        valuesAsStrings.addAll( valueVisitor.getKnownValues(dicomObject).values());

        return valuesAsStrings;
    }

    /**
     * Reduce the list of values to a flat list of TagPaths.
     *
     * @param values list of values, may contain list values.
     * @return flat list of TagPaths
     * @throws ScriptEvaluationRuntimeException if any of the specified values is not a TagPath.
     */
    protected List<TagPath> getTagPaths(List<Value> values) {
        List<TagPath> tagPaths = new ArrayList<>();
        for (Value v : flattenValueList(values)) {
            if (v.isScalar()) {
                tagPaths.addAll(getTagPaths(v.asValueList()));
            } else {
                tagPaths.add(getTagPathFromValue(v));
            }
        }
        return tagPaths;
    }

    protected TagPath getTagPathFromValue(Value value) {
        if (value.asObject() instanceof TagPath) {
            return (TagPath) value.asObject();
        } else if (value.asObject() instanceof String) {
            // backward compatability. look for tagPaths in arguments as strings.
            try {
                return TagPathFactory.createDE6Instance( value.asString());
            } catch (ScriptEvaluationRuntimeException e) {
                throw new ScriptEvaluationRuntimeException(String.format("Argument %s is not a valid tagPath.", value.asString()));
            }
        } else {
            throw new ScriptEvaluationRuntimeException("Arguments must be tagpaths : " + value.asString());
        }
    }

    public Map<String,Object> getInternalContext() {
        return internalContext;
    }
    public void setInternalContext(String name, Object value) {
        internalContext.put(name, value);
    }

    public String getName() {
        return name;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public String getUsage() {
        return usage;
    }

    public String getDescription() {
        return description;
    }

    public String getFQN() {
        return fqn;
    }

    protected String getFormattedErrorMessage(final List<Value> values) {
        return "Error executing function " + getName() + "\nArguments: " + Joiner.on(", ").join(values) + "\n" + getUsage();
    }

}
