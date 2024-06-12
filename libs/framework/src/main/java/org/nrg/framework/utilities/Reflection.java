/*
 * framework: org.nrg.framework.utilities.Reflection
 * XNAT http://www.xnat.org
 * Copyright (c) 2018-2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import static org.reflections.ReflectionUtils.withParametersCount;
import static org.reflections.ReflectionUtils.withPattern;
import static org.reflections.ReflectionUtils.withReturnType;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XnatPlugin;
import org.nrg.framework.exceptions.NotConcreteTypeException;
import org.nrg.framework.exceptions.NotParameterizedTypeException;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.framework.orm.hibernate.exceptions.InvalidDirectParameterizedClassUsageException;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;

@SuppressWarnings({"WeakerAccess"})
@Slf4j
public class Reflection {
    public static final String                          CAPITALIZED_NAME          = "[A-Z][A-z0-9_]*";
    public static final String                          REGEX_REFL_GETTER         = "^public.*\\.(get|is)%s\\(\\).*$";
    public static final List<Predicate<? super Method>> PREDICATES_ANY_GETTER     = getGetterPredicate();
    public static final String                          REGEX_REFL_SETTER         = "^public.*\\.set(%s)\\(.+\\).*$";
    public static final List<Predicate<? super Method>> PREDICATES_ANY_SETTER     = getSetterPredicate();
    public static final String                          REGEX_BOOL_GETTER         = "^is(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                          REGEX_OBJECT_GETTER       = "^get(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                          REGEX_GETTER              = "^(is|get)(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                          REGEX_SETTER              = "^set(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                          REGEX_PROPERTY            = "^(?<prefix>is|get|set)(?<property>[A-Z][A-z0-9_]*)$";
    public static final Pattern                         PATTERN_OBJECT_GETTER     = Pattern.compile(REGEX_OBJECT_GETTER);
    public static final Pattern                         PATTERN_BOOL_GETTER       = Pattern.compile(REGEX_BOOL_GETTER);
    @SuppressWarnings("unused")
    public static final Pattern                         PATTERN_GETTER            = Pattern.compile(REGEX_GETTER);
    public static final Pattern                         PATTERN_SETTER            = Pattern.compile(REGEX_SETTER);
    @SuppressWarnings("unused")
    public static final Pattern                         PATTERN_PROPERTY          = Pattern.compile(REGEX_PROPERTY);
    public static       Map<String, List<Class<?>>>     CACHED_CLASSES_BY_PACKAGE = new HashMap<>();

    /**
     * Returns the class object for the specified class name by calling <b>Class.forName()</b>. Unlike that method, If the
     * class name can't be found, this method returns <b>null</b> instead of throwing a <b>ClassNotFoundException</b>.
     * 
     * @param className The name of the class to find.
     *                  
     * @return The class object for the specified class name if found, otherwise <b>null</b>.
     */
    public static Class<?> getClass(final String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Returns the hierarchy for the submitted class parameter. This includes only primary superclasses and terminates
     * at <b>Object</b>, which is not included in the returned hierarchy list.
     *
     * @param topLevelClass The top-level class of the hierarchy.
     *
     * @return A list of classes in the hierarchy, starting with the top-level class and descending to <b>Object</b>.
     */
    public static List<Class<?>> getClassHierarchy(final Class<?> topLevelClass) {
        return getClassHierarchy(topLevelClass, Object.class, new ArrayList<Class<?>>());
    }

    /**
     * Returns the hierarchy for the submitted class parameter. This includes only primary superclasses and terminates
     * at the specified <b>base</b>. Unlike {@link #getClassHierarchy(Class)}, the terminating class <b>base</b> <i>is</i>
     * included in the returned hierarchy list.
     *
     * @param topLevelClass The top-level class of the hierarchy.
     * @param base          The bottom or limiting class of the hierarchy.
     *
     * @return A list of classes in the hierarchy, starting with the top-level class and descending to the <b>base</b> class.
     */
    public static List<Class<?>> getClassHierarchy(final Class<?> topLevelClass, final Class<?> base) {
        if (!base.isAssignableFrom(topLevelClass)) {
            log.warn("The class " + topLevelClass.getName() + " is not related to the specified base class " + base.getName() + " so the class hierarchy can't be calculated. Returning no results.");
            return Collections.emptyList();
        }
        return getClassHierarchy(topLevelClass, base, new ArrayList<Class<?>>());
    }

    public static <T> Class<T> getParameterizedTypeForClass(final Class<?> clazz) {
        Class<?>          working           = clazz;
        ParameterizedType parameterizedType = null;
        while (parameterizedType == null) {
            final Type superclass = working.getGenericSuperclass();
            if (superclass == null) {
                throw new RuntimeException("Can't find superclass as parameterized type!");
            }
            if (superclass instanceof ParameterizedType) {
                parameterizedType = (ParameterizedType) superclass;
                if (parameterizedType.getActualTypeArguments()[0] instanceof TypeVariable) {
                    throw new InvalidDirectParameterizedClassUsageException("When using a parameterized worker directly (i.e. with a generic subclass), you must call the AbstractParameterizedWorker constructor that takes the parameterized type directly.");
                }
            } else {
                working = clazz.getSuperclass();
            }
        }
        //noinspection unchecked
        return (Class<T>) parameterizedType.getActualTypeArguments()[0];
    }

    /**
     * Scans all classes accessible from the context class loader which belong
     * to the given package and subpackages.
     *
     * @param packageName The base package
     *
     * @return The classes
     *
     * @throws ClassNotFoundException the class not found exception
     * @throws IOException            Signals that an I/O exception has occurred.
     */
    public static List<Class<?>> getClassesForPackage(final String packageName) throws ClassNotFoundException, IOException {
        if (CACHED_CLASSES_BY_PACKAGE.containsKey(packageName)) {
            return CACHED_CLASSES_BY_PACKAGE.get(packageName);
        }
        log.info("Identifying classes for {}", packageName);
        final ClassLoader loader = getClassLoader();
        assert loader != null;

        Enumeration<URL>     resources   = loader.getResources(packageName.replace('.', '/'));
        List<File>           directories = new ArrayList<>();
        List<URL>            jarFiles    = new ArrayList<>();
        final List<Class<?>> classes     = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();

            if (resource.getProtocol().equalsIgnoreCase("jar")) {
                jarFiles.add(resource);
            } else {
                directories.add(new File(URLDecoder.decode(resource.getFile(), "UTF-8")));
            }
        }

        for (URL jarFile : jarFiles) {
            classes.addAll(findClassesInJarFile(jarFile, packageName));
        }

        for (File directory : directories) {
            classes.addAll(findClasses(directory, packageName));
        }

        CACHED_CLASSES_BY_PACKAGE.put(packageName, classes);

        return classes;
    }

    /**
     * Checks whether <b>clazz</b> or any of its superclasses are decorated with an annotation of the class
     * <b>annotationClass</b>. If so, the annotation instance is returned. If not, this method returns null. Note that
     * an instance of the annotation will not be found unless the annotation definition is retained for run-time
     * analysis, which requires the following annotation on the definition:
     *
     * <pre>@Retention(RetentionPolicy.RUNTIME)</pre>
     * <p>
     * See {@link XnatPlugin} for an example of an annotation that includes this configuration.
     *
     * @param <T>             The annotation class to check.
     * @param clazz           The top-level class to check.
     * @param annotationClass The annotation definition to check for.
     *
     * @return The annotation instance if it exists on the class or any of its subtypes, null otherwise.
     */
    @SuppressWarnings("unused")
    public static <T extends Annotation> T findAnnotationInClassHierarchy(final Class<?> clazz, final Class<T> annotationClass) {
        Class<?> current = clazz;
        while (current != null) {
            if (current.isAnnotationPresent(annotationClass)) {
                return current.getAnnotation(annotationClass);
            }
            current = clazz.getSuperclass();
        }
        return null;
    }

    public static void injectDynamicImplementations(final String _package, final boolean failOnException, final Map<String, Object> params) throws Exception {
        final List<Class<?>> classes = Reflection.getClassesForPackage(_package);
        if (classes != null && !classes.isEmpty()) {
            for (final Class<?> clazz : classes) {
                try {
                    if (InjectableI.class.isAssignableFrom(clazz)) {
                        ((InjectableI) clazz.newInstance()).execute(ObjectUtils.defaultIfNull(params, new HashMap<String, Object>()));
                    } else {
                        log.error("Reflection: {}.{} is NOT an implementation of InjectableI", _package, clazz.getName());
                    }
                } catch (Throwable e) {
                    if (failOnException) {
                        throw e;
                    } else {
                        log.error("", e);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    public static void injectDynamicImplementations(final String _package, final Map<String, Object> params) {
        try {
            injectDynamicImplementations(_package, false, params);
        } catch (Throwable ignored) {
            // Nothing to do here...
        }
    }

    @SuppressWarnings("unused")
    public static List<Class<?>> getClassesFromParameterizedType(final Type type) throws NotParameterizedTypeException, NotConcreteTypeException {
        if (!(type instanceof ParameterizedType)) {
            throw new NotParameterizedTypeException(type, "The type " + type.toString() + " is not a parameterized type");
        }
        final List<Class<?>>    classes           = new ArrayList<>();
        final ParameterizedType parameterizedType = (ParameterizedType) type;
        for (final Type subtype : parameterizedType.getActualTypeArguments()) {
            if (subtype instanceof ParameterizedType) {
                throw new NotConcreteTypeException(type, "The type " + type.toString() + " can not be a parameterized type");
            }
            classes.add((Class<?>) subtype);
        }
        return classes;
    }

    @SuppressWarnings("unused")
    public static boolean isGetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
               method.getParameterTypes().length == 0 &&
               ((PATTERN_OBJECT_GETTER.matcher(method.getName()).matches() && !method.getReturnType().equals(Void.TYPE)) || (PATTERN_BOOL_GETTER.matcher((method.getName())).matches() && method.getReturnType().equals(Boolean.TYPE)));
    }

    @SuppressWarnings("unused")
    public static boolean isSetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) && PATTERN_SETTER.matcher(method.getName()).matches() && method.getParameterTypes().length == 1;
    }

    @SuppressWarnings("unused")
    @SafeVarargs
    public static Method getGetter(final Class<?> clazz, final String property, final Predicate<? super Method>... predicates) {
        return getGetter(clazz, null, property, predicates);
    }

    @SafeVarargs
    public static Method getGetter(final Class<?> clazz, final Class<?> terminator, final String property, final Predicate<? super Method>... predicates) {
        final List<Method> methods = getMethodsUpToSuperclass(clazz, terminator, getGetterPredicate(property, Arrays.asList(predicates)));
        if (methods.size() == 0) {
            return null;
        }
        return methods.get(0);
    }

    @SuppressWarnings("unused")
    @SafeVarargs
    public static Method getSetter(final Class<?> clazz, final String property, final Predicate<? super Method>... predicates) {
        return getSetter(clazz, null, property, predicates);
    }

    @SafeVarargs
    public static Method getSetter(final Class<?> clazz, final Class<?> terminator, final String property, final Predicate<? super Method>... predicates) {
        final List<Method> methods = getMethodsUpToSuperclass(clazz, terminator, getSetterPredicate(property, Arrays.asList(predicates)));
        if (methods.size() == 0) {
            return null;
        }
        return methods.get(0);
    }

    @SafeVarargs
    public static List<Method> getGetters(final Class<?> clazz, final Predicate<? super Method>... predicates) {
        return getGetters(clazz, null, predicates);
    }

    @SuppressWarnings("unchecked")
    public static List<Method> getGetters(final Class<?> clazz, final Class<?> terminator, final Predicate<? super Method>... predicates) {
        return getMethodsUpToSuperclass(clazz, terminator, PREDICATES_ANY_GETTER, predicates);
    }

    @SafeVarargs
    public static List<Method> getSetters(final Class<?> clazz, final Predicate<? super Method>... predicates) {
        return getSetters(clazz, null, predicates);
    }

    @SuppressWarnings("unchecked")
    public static List<Method> getSetters(final Class<?> clazz, final Class<?> terminator, final Predicate<? super Method>... predicates) {
        return getMethodsUpToSuperclass(clazz, terminator, PREDICATES_ANY_SETTER, predicates);
    }

    public static Object callMethodForParameters(final Object object, final String methodName, final Object... parameters) {
        final Class<?> objectClass = object.getClass();
        final Method   method      = getMethodForParameters(objectClass, methodName, getClassTypes(parameters));
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("An error occurred trying to call the method '{}.{}'", objectClass.getName(), methodName, e);
        }
        return null;
    }

    public interface InjectableI {
        void execute(Map<String, Object> params);
    }

    /**
     * Find classes in jar file.
     *
     * @param jarFile     the jar file
     * @param packageName the package name
     *
     * @return the collection of classes in the jar file.
     *
     * @throws IOException            Signals that an I/O exception has occurred.
     * @throws ClassNotFoundException the class not found exception
     */
    public static Collection<? extends Class<?>> findClassesInJarFile(final URL jarFile, final String packageName) throws IOException, ClassNotFoundException {
        final List<Class<?>>   classes    = new ArrayList<>();
        final JarURLConnection connection = (JarURLConnection) jarFile.openConnection();
        final JarFile          jar        = connection.getJarFile();
        for (final JarEntry entry : Collections.list(jar.entries())) {
            if (entry.getName().startsWith(packageName.replace('.', '/')) && entry.getName().endsWith(".class") && !entry.getName().contains("$")) {
                final String className = entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirectories.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     *
     * @return The classes
     *
     * @throws ClassNotFoundException the class not found exception
     */
    public static List<Class<?>> findClasses(final File directory, final String packageName) throws ClassNotFoundException {
        final File[] files = directory.listFiles();
        if (!directory.exists() || files == null || files.length == 0) {
            return Collections.emptyList();
        }

        final ClassLoader classLoader = getClassLoader();
        assert classLoader != null;

        final List<Class<?>> classes = new ArrayList<>();
        for (final File file : files) {
            final String fileName = file.getName();
            if (file.isDirectory()) {
                assert !fileName.contains(".");
                classes.addAll(findClasses(file, packageName + "." + fileName));
            } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
                final String qualifiedClassFilename = FilenameUtils.removeExtension(packageName + "." + fileName);
                try {
                    classes.add(Class.forName(qualifiedClassFilename));
                } catch (ExceptionInInitializerError e) {
                    // This happens to classes which depend on Spring to inject
                    // some beans and fail if dependency is not fulfilled
                    classes.add(Class.forName(qualifiedClassFilename, false, classLoader));
                }
            }
        }
        return classes;
    }

    public static ClassLoader getClassLoader() {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }
        return Reflection.class.getClassLoader();
    }

    public static Properties getPropertiesForClass(final Class<?> parent) {
        final String bundle     = "/" + parent.getName().replace(".", "/") + ".properties";
        Properties   properties = new Properties();
        try {
            try (InputStream inputStream = parent.getResourceAsStream(bundle)) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            properties = null;
        }
        return properties;
    }

    /**
     * This method is to compensate for the fact that the default Java getConstructor() method doesn't match
     * constructors that have parameters that are superclasses of one of the submitted parameter classes. For example,
     * if an object submitted for the constructor is an <b>ArrayList</b>, <b>getConstructor()</b> will not match that to
     * a constructor that takes a <b>List</b>. This method checks for that downcast compatibility via the
     * <b>isAssignableFrom()</b> method.
     *
     * <b>Note:</b> This version of the method returns only publicly accessible constructors. If you need to find
     * protected or private constructors, call {@link #getConstructorForParameters(Class, int, Class[])}.
     *
     * @param target     The class that you want to inspect for compatible constructors.
     * @param parameters The parameter types you want to submit to the constructor.
     * @param <T>        The parameterized type for this method.
     *
     * @return A matching constructor, if any, <b>null</b> otherwise.
     */
    public static <T> Constructor<T> getConstructorForParameters(final Class<T> target, final Object... parameters) {
        return getConstructorForParameters(target, Modifier.PUBLIC, Lists.transform(Arrays.asList(parameters), new Function<Object, Class<?>>() {
            @Override
            public Class<?> apply(final Object parameter) {
                return parameter.getClass();
            }
        }).toArray(new Class[0]));
    }

    /**
     * This method is to compensate for the fact that the default Java getConstructor() method doesn't match
     * constructors that have parameters that are superclasses of one of the submitted parameter classes. For example,
     * if an object submitted for the constructor is an <b>ArrayList</b>, <b>getConstructor()</b> will not match that to
     * a constructor that takes a <b>List</b>. This method checks for that downcast compatibility via the
     * <b>isAssignableFrom()</b> method.
     *
     * <b>Note:</b> This version of the method returns only publicly accessible constructors. If you need to find
     * protected or private constructors, call {@link #getConstructorForParameters(Class, int, Class[])}.
     *
     * @param target         The class that you want to inspect for compatible constructors.
     * @param parameterTypes The parameter types you want to submit to the constructor.
     * @param <T>            The parameterized type for this method.
     *
     * @return A matching constructor, if any, <b>null</b> otherwise.
     */
    public static <T> Constructor<T> getConstructorForParameters(final Class<T> target, final Class<?>... parameterTypes) {
        return getConstructorForParameters(target, Modifier.PUBLIC, parameterTypes);
    }

    /**
     * This method is to compensate for the fact that the default Java getConstructor() method doesn't match
     * constructors that have parameters that are superclasses of one of the submitted parameter classes. For example,
     * if an object submitted for the constructor is an <b>ArrayList</b>, <b>getConstructor()</b> will not match that to
     * a constructor that takes a <b>List</b>. This method checks for that downcast compatibility via the
     * <b>isAssignableFrom()</b> method.
     *
     * <b>Note:</b> This version of the method can return public, protected, and private constructors. If you want to
     * find only publicly accessible constructors, you can call {@link #getConstructorForParameters(Class, Class[])}.
     * To specify what access level of constructor you want to retrieve, you should specify the appropriate value from
     * the {@link Modifier} class:
     *
     * <ul>
     * <li>{@link Modifier#PUBLIC} indicates only publicly accessible constructors</li>
     * <li>{@link Modifier#PROTECTED} indicates publicly accessible or protected constructors</li>
     * <li>{@link Modifier#PRIVATE} indicates public, protected, or private constructors</li>
     * </ul>
     *
     * @param target          The class that you want to inspect for compatible constructors.
     * @param requestedAccess Indicates the desired access level.
     * @param parameterTypes  The parameter types you want to submit to the constructor.
     * @param <T>             The parameterized type for this method.
     *
     * @return A matching constructor, if any, <b>null</b> otherwise.
     */
    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructorForParameters(final Class<T> target, final int requestedAccess, final Class<?>... parameterTypes) {
        // If there are no parameters specified, return the default constructor.
        if (parameterTypes == null || parameterTypes.length == 0) {
            try {
                final Constructor<T> constructor = target.getConstructor();
                // If the default constructor isn't accessible
                return isAccessible(requestedAccess, constructor.getModifiers()) ? constructor : null;
            } catch (NoSuchMethodException e) {
                // If there was no default constructor, then return null.
                return null;
            }
        }
        // Try to return constructor that's an exact match for the parameter types.
        // If that doesn't exist, ignore the exception. We have more sophisticated
        // things that we can try to match superclasses, interfaces, etc.
        try {
            return target.getConstructor(parameterTypes);
        } catch (NoSuchMethodException ignored) {
            //
        }

        final Constructor<T>[] constructors = (Constructor<T>[]) target.getConstructors();
        return constructors.length == 0 ? null : (Constructor<T>) getAccessibleForParameters(constructors, requestedAccess, parameterTypes);
    }

    public static <T> Method getMethodForParameters(final Class<T> target, final String name, final Class<?>... parameterTypes) {
        return getMethodForParameters(target, name, Modifier.PUBLIC, parameterTypes);
    }

    public static <T> Method getMethodForParameters(final Class<T> target, final String name, final int requestedAccess, final Class<?>... parameterTypes) {
        // If there are no parameters specified, return the default method.
        if (parameterTypes == null || parameterTypes.length == 0) {
            try {
                final Method method = target.getMethod(name);
                // If the default method isn't accessible
                return isAccessible(requestedAccess, method.getModifiers()) ? method : null;
            } catch (NoSuchMethodException e) {
                // If there was no default method, then return null.
                return null;
            }
        }
        // Try to return constructor that's an exact match for the parameter types.
        // If that doesn't exist, ignore the exception. We have more sophisticated
        // things that we can try to match superclasses, interfaces, etc.
        try {
            return target.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException ignored) {
            //
        }

        final Method[] methods = target.getMethods();
        return methods.length == 0 ? null : (Method) getAccessibleForParameters(methods, requestedAccess, parameterTypes);
    }

    public static <T> T constructObjectFromParameters(final Class<? extends T> type, final Object... parameters) {
        return constructObjectFromParameters(null, type, parameters);
    }

    public static <T> T constructObjectFromParameters(final String className, final Class<? extends T> type, final Object... parameters) {
        try {
            final Class<? extends T>       implClass      = StringUtils.isBlank(className) ? type : Class.forName(className).asSubclass(type);
            final Class<?>[]               parameterTypes = getClassTypes(parameters);
            final Constructor<? extends T> constructor    = getConstructorForParameters(implClass, parameterTypes);
            if (constructor == null) {
                log.error("No constructor was found for the class '{}' with the following parameter types: {}", className, StringUtils.join(parameterTypes, ", "));
                return null;
            }
            return constructor.newInstance(parameters);
        } catch (ClassNotFoundException e) {
            log.error("Couldn't find definition of the specified class '{}'", className);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            log.error("An error occurred trying to create an instance of the class '{}'", className, e);
        }
        return null;
    }

    public static Class<?>[] getClassTypes(final Object[] parameters) {
        return Lists.transform(Arrays.asList(parameters), new Function<Object, Class<?>>() {
            @Override
            public Class<?> apply(final Object object) {
                return object.getClass();
            }
        }).toArray(new Class<?>[0]);
    }

    @SuppressWarnings("unused")
    public static String findResource(final String resourcePackage, final String resourcePattern) {
        final Set<String> resources = findResources(resourcePackage, Pattern.compile(resourcePattern));
        if (resources.size() == 0) {
            return null;
        }
        if (resources.size() > 1) {
            throw new RuntimeException("You assumed there was only one resource with the package " + resourcePackage + " and the name " + resourcePattern + ", but that's not true: there are " + resources.size() + " of them (make sure your 'pattern' isn't actually a regex pattern but just a standard name): " + Joiner.on(", ").join(resources));
        }
        return (String) resources.toArray()[0];
    }

    @SuppressWarnings("unused")
    public static Set<String> findResources(final String resourcePackage, final String resourcePattern) {
        return findResources(resourcePackage, Pattern.compile(resourcePattern));
    }

    public static Set<String> findResources(final String resourcePackage, final Pattern resourcePattern) {
        final Reflections reflections = getReflectionsForPackage(resourcePackage);
        return reflections.getResources(resourcePattern);
    }

    @SuppressWarnings("unused")
    public static URL getResourceUrl(final String resource) {
        final ClassLoader classLoader = getClassLoader();
        assert classLoader != null;
        return classLoader.getResource(resource);
    }

    @SuppressWarnings("rawtypes")
    private static AccessibleObject getAccessibleForParameters(final AccessibleObject[] candidates, final int requestedAccess, final Class<?>... parameterTypes) {
        // If we got through all the bits where there are no constructors or parameters, we can try to match the
        // submitted parameter types.
        for (final AccessibleObject candidate : candidates) {
            final boolean isConstructor;
            if (candidate instanceof Constructor) {
                isConstructor = true;
            } else if (candidate instanceof Method) {
                isConstructor = false;
            } else {
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "This method only works for constructors and methods at this time.");
            }
            // If it's not accessible, don't even process.
            final int modifiers = isConstructor ? ((Constructor) candidate).getModifiers() : ((Method) candidate).getModifiers();
            if (!isAccessible(requestedAccess, modifiers)) {
                continue;
            }

            // Get all of the parameter types for the current candidate constructor.
            final Class<?>[] candidateParameterTypes = isConstructor ? ((Constructor) candidate).getParameterTypes() : ((Method) candidate).getParameterTypes();

            // If the number of parameter types don't match, then this isn't a match.
            // TODO: Check how this works with vararg constructors. It should be fine (should be Object[]), but still.
            if (candidateParameterTypes.length != parameterTypes.length) {
                continue;
            }
            // Let's assume the best.
            boolean match = true;
            // Now go through all of the parameters.
            for (int index = 0; index < candidateParameterTypes.length; index++) {
                // If we can't assign this submitted parameter to the candidate parameter...
                if (!matchParameterTypes(candidateParameterTypes[index], parameterTypes[index])) {
                    // We don't have a match.
                    match = false;
                    break;
                }
            }
            if (match) {
                return candidate;
            }
        }
        // If we made it through without returning, none of the constructor candidates match.
        return null;
    }

    private static boolean matchParameterTypes(final Class<?> first, final Class<?> second) {
        return first.isAssignableFrom(second) ||
               PRIMITIVES.contains(first) && PRIMITIVES.indexOf(first) == WRAPPERS.indexOf(second) ||
               PRIMITIVES.contains(second) && PRIMITIVES.indexOf(second) == WRAPPERS.indexOf(first);
    }

    private static boolean isAccessible(final int requestedAccess, final int modifiers) {
        // If they want private, they can have anything, so just say yes.
        if (requestedAccess == Modifier.PRIVATE) {
            return true;
        }

        // Find out if the target modifier is private or protected.
        boolean isPrivate   = Modifier.isPrivate(modifiers);
        boolean isProtected = Modifier.isPrivate(modifiers);

        // If requested access is protected, then return true if the modifier is NOT private, otherwise it's public so
        // return true if the modifier is NOT private or protected.
        return requestedAccess == Modifier.PROTECTED ? !isPrivate : !(isPrivate || isProtected);
    }

    private static Reflections getReflectionsForPackage(final String resourcePackage) {
        if (!_reflectionsCache.containsKey(resourcePackage)) {
            _reflectionsCache.put(resourcePackage, new Reflections(resourcePackage, new ResourcesScanner()));
        }
        return _reflectionsCache.get(resourcePackage);
    }

    @SuppressWarnings("rawtypes")
    @SafeVarargs
    private static List<Method> getMethodsUpToSuperclass(final Class<?> subclass, final Class<?> terminator, final List<Predicate<? super Method>> predicates, final Predicate<? super Method>... added) {
        final Predicate[] asArray   = predicates.toArray(new Predicate[0]);
        final Predicate[] submitted = added.length == 0 ? asArray : ArrayUtils.addAll(asArray, added);
        //noinspection unchecked
        final List<Method> methods    = new ArrayList<>(ReflectionUtils.getMethods(subclass, submitted));
        final Class<?>     superclass = subclass.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class) && (terminator == null || !terminator.equals(superclass))) {
            methods.addAll(getMethodsUpToSuperclass(superclass, terminator, predicates));
        }
        return methods;
    }

    private static List<Predicate<? super Method>> getGetterPredicate() {
        return getPredicate(null, true, null);
    }

    private static List<Predicate<? super Method>> getGetterPredicate(final String property, final List<Predicate<? super Method>> added) {
        return getPredicate(property, true, added);
    }

    private static List<Predicate<? super Method>> getSetterPredicate() {
        return getPredicate(null, false, null);
    }

    private static List<Predicate<? super Method>> getSetterPredicate(final String property, final List<Predicate<? super Method>> added) {
        return getPredicate(property, false, added);
    }

    private static List<Predicate<? super Method>> getPredicate(final String property, final boolean isGetter, final List<Predicate<? super Method>> added) {
        final List<Predicate<? super Method>> predicates = new ArrayList<>();
        predicates.add(withPattern(String.format(isGetter ? REGEX_REFL_GETTER : REGEX_REFL_SETTER, StringUtils.isBlank(property) ? CAPITALIZED_NAME : StringUtils.capitalize(property))));
        if (isGetter) {
            predicates.add(withParametersCount(0));
        } else {
            predicates.add(withReturnType(Void.TYPE));
        }
        if (added != null) {
            predicates.addAll(added);
        }
        return predicates;
    }

    private static <T> List<Class<?>> getClassHierarchy(final Class<T> current, final Class<?> base, final List<Class<?>> hierarchy) {
        hierarchy.add(current);
        return current.equals(base) ? hierarchy : getClassHierarchy(current.getSuperclass(), base, hierarchy);
    }

    private static final List<Class<?>>           WRAPPERS          = ImmutableList.of(Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class, Short.class, Void.class);
    private static final List<Class<?>>           PRIMITIVES        = ImmutableList.of(boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class, void.class);
    private static final Map<String, Reflections> _reflectionsCache = Collections.synchronizedMap(new HashMap<String, Reflections>());
}
