/*
 * framework: org.nrg.framework.processors.NrgAbstractAnnotationProcessor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.processors;

import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.exceptions.NrgRuntimeException;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * This processor takes an annotation class and converts it into a properties file that is stored in the META-INF/xnat
 * resource folder.
 */
public abstract class NrgAbstractAnnotationProcessor<A extends Annotation> extends AbstractProcessor {

    protected NrgAbstractAnnotationProcessor() {
        final Annotation[] annotations = getClass().getAnnotations();
        if (annotations == null) {
            throw new NrgRuntimeException("The class " + getClass().getName() + " is supposed to be an annotation processor but has no annotations itself. It must have at least the @SupportedAnnotationType annotation to indicate the type of annotation(s) it wants to process.");
        }
        //noinspection unchecked
        _class = (Class<? extends A>) findSupportedAnnotationType(annotations);
    }

    /**
     * This processes the annotation of the parameterized type on the specified element, which should be an instantiable
     * class (i.e. not an interface or abstract class). The annotation is processed according to the logic provided in
     * the implementation of this method and converted into a properties object.
     *
     * Note that the map maintained by this processor is sorted based on order of insertion. If your subclass should
     * have a particular ordering, your implementation of this method should return an ordered map of some sort, for
     * example a LinkedHashMap.
     *
     * @param element    The annotated class element.
     * @param annotation The annotation.
     *
     * @return The attributes for the annotation converted into a map of strings.
     */
    protected abstract Map<String, String> processAnnotation(final TypeElement element, final A annotation);

    /**
     * Returns the name for the properties resource to be generated for the annotation instance. This can be as simple
     * as just the bare name of the properties bundle without a path or properties extension: these will be added if not
     * present. However you can also add folder hierarchies to the returned name if you want to further segregate the
     * generated properties files.
     *
     * @param element    The annotated class element.
     * @param annotation The annotation instance.
     *
     * @return The name for the properties resource.
     */
    protected abstract String getPropertiesName(final TypeElement element, final A annotation);

    /**
     * {@inheritDoc}
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        try {
            return SourceVersion.valueOf("RELEASE_8");
        } catch (IllegalArgumentException ignored) {
        }
        try {
            return SourceVersion.valueOf("RELEASE_7");
        } catch (IllegalArgumentException ignored) {
        }
        return SourceVersion.RELEASE_6;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Messager messager = processingEnv.getMessager();
        if (roundEnv.processingOver()) {
            return false;
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "Beginning processing for the " + _class.getName() + " annotation.");
        final Map<String, Map<String, String>> outputs = new LinkedHashMap<>();
        for (final Element element : roundEnv.getElementsAnnotatedWith(_class)) {
            if (!(element instanceof TypeElement)) {
                continue;
            }
            final TypeElement typeElement = (TypeElement) element;
            messager.printMessage(Diagnostic.Kind.NOTE, "Found the " + typeElement.toString() + " class.");

            final A annotation = typeElement.getAnnotation(_class);
            if (annotation == null) {
                continue;
            }
            if (!typeElement.getKind().isClass() && !typeElement.getKind().isInterface()) {
                continue;
            }

            messager.printMessage(Diagnostic.Kind.NOTE, "Processing the " + typeElement.toString() + " class.");
            outputs.put(xnatize(getPropertiesName(typeElement, annotation)), processAnnotation(typeElement, annotation));
        }

        final Filer filer = processingEnv.getFiler();

        for (final String propertiesPath : outputs.keySet()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Writing resource to " + propertiesPath);

            try (final PrintWriter writer = new PrintWriter(new OutputStreamWriter(filer.createResource(StandardLocation.CLASS_OUTPUT, "", propertiesPath).openOutputStream(), "UTF-8"))) {
                final Map<String, String> properties = outputs.get(propertiesPath);
                for (final String property : properties.keySet()) {
                    writer.println(property + "=" + properties.get(property));
                }
            } catch (IOException x) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write service definition files: " + x);
            }
        }

        return false;
    }

    /**
     * Returns the attribute value for the parameterized annotation on the given type element as a type element. This is
     * used to get back a class when the annotation attribute specifies a class. This returns only a single class name
     * and will throw an exception if multiple classes are specified, as in an array of types. If you want to retrieve a
     * list of class names, you should use the {@link #getTypeElementValues(TypeElement, String)} method instead.
     *
     * @param element The element (that is, the annotated class) to inspect.
     * @param key     The name of the attribute to retrieve.
     *
     * @return The type element for the specified class if found, or null if not found.
     *
     * @throws IllegalArgumentException When there is more than one class specified for the attribute value.
     */
    @SuppressWarnings("unused")
    @Nullable
    protected String getTypeElementValue(final TypeElement element, String key) {
        final List<String> list = getTypeElementValues(element, key);
        if (list == null) {
            return null;
        }
        if (list.size() > 1) {
            throw new IllegalArgumentException("You should only specify a single class for the " + key + " attribute for " + _class.getName() + " annotations.");
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Returns the attribute value or values for the parameterized annotation on the given type element as a class name.
     * This is used to get back a list of class names when the annotation attribute specifies an array of classes. This
     * method returns multiple class names if specified as the attribute value. If you want to limit the number of
     * classes to a single class and prohibit specifying multiple classes, you should use the
     * {@link #getTypeElementValue(TypeElement, String)} method instead.
     *
     * @param element The element (that is, the annotated class) to inspect.
     * @param key     The name of the attribute to retrieve.
     *
     * @return The type element(s) for the specified class(es) if found, or null if not found.
     */
    @Nullable
    protected List<String> getTypeElementValues(final TypeElement element, String key) {
        final AnnotationMirror mirror = getAnnotationMirror(element);
        if (mirror == null) {
            return null;
        }
        final AnnotationValue annotationValue = getAnnotationValue(mirror, key);
        if (annotationValue == null) {
            return null;
        }
        final List<String> elements = new ArrayList<>();
        final Object value = annotationValue.getValue();
        if (value instanceof List) {
            final List list = (List) value;
            if (list.size() == 0) {
                return null;
            }

            for (final Object object : list) {
                final TypeElement typeElement = convertAnnotationValueToTypeElement((AnnotationValue) object);
                if (typeElement != null) {
                    elements.add(typeElement.toString());
                }
            }
        } else if (value instanceof AnnotationValue) {
            final TypeElement typeElement = convertAnnotationValueToTypeElement((AnnotationValue) value);
            if (typeElement != null) {
                elements.add(typeElement.toString());
            }
        } else {
            elements.add(value.toString());
        }
        return elements.size() > 0 ? elements : null;
    }

    /**
     * This method finds the @SupportedAnnotationTypes annotation on the implementing class definition and extracts the
     * annotation to be processed from there. This method will throw exceptions if:
     * <p/>
     * <ul>
     * <li>The @SupportedAnnotationTypes annotation isn't found on the implementing class definition</li>
     * <li>Multiple annotation types are specified for the @SupportedAnnotationTypes annotation</li>
     * <li>The class for the specified annotation type can't be found</li>
     * </ul>
     *
     * @param annotations The annotations on the implementing class definition.
     *
     * @return The class for the supported annotation type.
     */
    @Nullable
    private Class<? extends Annotation> findSupportedAnnotationType(final Annotation[] annotations) {
        for (final Annotation annotation : annotations) {
            if (annotation.annotationType().equals(SupportedAnnotationTypes.class)) {
                final String[] types = ((SupportedAnnotationTypes) annotation).value();
                if (types.length > 1) {
                    throw new NrgRuntimeException("NRG annotation processors do not currently support handling multiple annotations with a single processor.");
                }
                final Class<?> found;
                try {
                    found = Class.forName(types[0]);
                } catch (ClassNotFoundException e) {
                    throw new NrgRuntimeException(types[0] + " was specified as a supported annotation type on the " + getClass().getName() + " class, but that annotation class can't be found on the classpath.");
                }
                if (!found.isAnnotation()) {
                    throw new NrgRuntimeException("Found the class " + found.getName() + " as a supported annotation type for the " + getClass().getName() + " class, but that class is not an annotation!");
                }
                return found.asSubclass(Annotation.class);
            }
        }
        throw new NrgRuntimeException("Did not find an instance of the @SupportedAnnotationTypes annotation on the " + getClass().getName() + " class definition.");
    }

    /**
     * This "xnatizes" the name of the properties resource to be generated. This means it is prefaced with META-INF/xnat
     * (specifying another root path, e.g. /META-INF/foo, will result in an exception) and ended with ".properties".
     *
     * @param propertiesName The name for the generated properties resource.
     *
     * @return The full path and name for the generated properties resource.
     */
    private String xnatize(final String propertiesName) {
        if (StringUtils.isEmpty(propertiesName)) {
            throw new NrgRuntimeException("You must specify a valid name for the generated properties file for the " + _class.getName() + " annotation processing.");
        }
        if (propertiesName.startsWith("/") && !propertiesName.startsWith("/META-INF/xnat")) {
            throw new NrgRuntimeException("An invalid root path has been specified: " + propertiesName + ". All XNAT properties bundles are generated into the META-INF/xnat folder, although you can specify subfolders to that.");
        }
        final StringBuilder buffer = new StringBuilder();
        if (propertiesName.startsWith("META-INF/xnat") || propertiesName.startsWith("/META-INF/xnat")) {
            buffer.append(propertiesName);
        } else {
            buffer.append("META-INF/xnat/").append(propertiesName);
        }
        if (!propertiesName.endsWith(".properties")) {
            buffer.append(".properties");
        }
        return buffer.toString();
    }

    @Nullable
    private TypeElement convertAnnotationValueToTypeElement(final AnnotationValue value) {
        final TypeMirror typeMirror = (TypeMirror) value.getValue();
        if (typeMirror == null) {
            return null;
        }
        return (TypeElement) processingEnv.getTypeUtils().asElement(typeMirror);
    }

    @Nullable
    private AnnotationMirror getAnnotationMirror(final TypeElement typeElement) {
        for (final AnnotationMirror mirror : typeElement.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(_class.getName())) {
                return mirror;
            }
        }
        return null;
    }

    @Nullable
    private static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
        for (final Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private final Class<? extends A> _class;
}
