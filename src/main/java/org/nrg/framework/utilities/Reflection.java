/*
 * framework: org.nrg.framework.utilities.Reflection
 * XNAT http://www.xnat.org
 * Copyright (c) 2018, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ArrayUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import static org.reflections.ReflectionUtils.*;

@SuppressWarnings("WeakerAccess")
public class Reflection {
    public static final String                          CAPITALIZED_NAME          = "[A-Z][A-z0-9_]*";
    public static final String                          REGEX_REFL_GETTER         = "^public.*\\.(get|is)%s\\(\\).*$";
    @SuppressWarnings("unchecked")
    public static final List<Predicate<? super Method>> PREDICATES_ANY_GETTER     = getGetterPredicate();
    public static final String                          REGEX_REFL_SETTER         = "^public.*\\.set(%s)\\(.+\\).*$";
    @SuppressWarnings("unchecked")
    public static final List<Predicate<? super Method>> PREDICATES_ANY_SETTER     = getSetterPredicate();
    public static final String                          REGEX_BOOL_GETTER         = "^is(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                          REGEX_OBJECT_GETTER       = "^get(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                          REGEX_GETTER              = "^(is|get)(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                      REGEX_SETTER              = "^set(?<property>[A-Z][A-z0-9_]*)$";
    public static final String                      REGEX_PROPERTY            = "^(?<prefix>is|get|set)(?<property>[A-Z][A-z0-9_]*)$";
    public static final Pattern                     PATTERN_OBJECT_GETTER     = Pattern.compile(REGEX_OBJECT_GETTER);
    public static final Pattern                     PATTERN_BOOL_GETTER       = Pattern.compile(REGEX_BOOL_GETTER);
    @SuppressWarnings("unused")
    public static final Pattern                     PATTERN_GETTER            = Pattern.compile(REGEX_GETTER);
    public static final Pattern                     PATTERN_SETTER            = Pattern.compile(REGEX_SETTER);
    public static final Pattern                     PATTERN_PROPERTY          = Pattern.compile(REGEX_PROPERTY);
    public static       Map<String, List<Class<?>>> CACHED_CLASSES_BY_PACKAGE = Maps.newHashMap();

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
        if (_log.isInfoEnabled()) {
            _log.info("Identifying classes for " + packageName);
        }
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

    public static void injectDynamicImplementations(final String _package, final boolean failOnException, Map<String, Object> params) throws Exception {
        List<Class<?>> classes = Reflection.getClassesForPackage(_package);
        if (params == null) {
            params = Maps.newHashMap();
        }
        if (classes != null && classes.size() > 0) {
            for (Class<?> clazz : classes) {
                try {
                    if (InjectableI.class.isAssignableFrom(clazz)) {
                        InjectableI action = (InjectableI) clazz.newInstance();
                        action.execute(params);
                    } else {
                        _log.error("Reflection: " + _package + "." + clazz.getName() + " is NOT an implementation of InjectableI");
                    }
                } catch (Throwable e) {
                    if (failOnException) {
                        throw e;
                    } else {
                        _log.error("", e);
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

    public static boolean isGetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
               method.getParameterTypes().length == 0 &&
               ((PATTERN_OBJECT_GETTER.matcher(method.getName()).matches() && !method.getReturnType().equals(Void.TYPE)) || (PATTERN_BOOL_GETTER.matcher((method.getName())).matches() && method.getReturnType().equals(Boolean.TYPE)));
    }

    public static boolean isSetter(final Method method) {
        return Modifier.isPublic(method.getModifiers()) && PATTERN_SETTER.matcher(method.getName()).matches() && method.getParameterTypes().length == 1;
    }

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
        final Method   method = getMethodForParameters(objectClass, methodName, getClassTypes(parameters));
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(object, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            _log.error("An error occurred trying to call the method '{}.{}'", objectClass.getName(), methodName, e);
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
        final List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) {
            return classes;
        }

        final File[] files = directory.listFiles();

        assert files != null;
        for (final File file : files) {
            final String fileName = file.getName();
            if (file.isDirectory()) {
                assert !fileName.contains(".");
                classes.addAll(findClasses(file, packageName + "." + fileName));
            } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
                try {
                    classes.add(Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6)));
                } catch (ExceptionInInitializerError e) {
                    // This happens to classes which depend on Spring to inject
                    // some beans
                    // and fail if dependency is not fulfilled
                    final ClassLoader classLoader = getClassLoader();
                    assert classLoader != null;
                    classes.add(Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6), false, classLoader));
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
        // Honestly I don't even think this can happen, but just in case...
        if (constructors == null || constructors.length == 0) {
            return null;
        }

        return (Constructor<T>) getAccessibleForParameters(constructors, requestedAccess, parameterTypes);
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
        // Honestly I don't even think this can happen, but just in case...
        if (methods == null || methods.length == 0) {
            return null;
        }

        return (Method) getAccessibleForParameters(methods, requestedAccess, parameterTypes);
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
                _log.error("No constructor was found for the class '{}' with the following parameter types: {}", className, StringUtils.join(parameterTypes, ", "));
                return null;
            }
            return constructor.newInstance(parameters);
        } catch (ClassNotFoundException e) {
            _log.error("Couldn't find definition of the specified class '{}'", className);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            _log.error("An error occurred trying to create an instance of the class '{}'", className, e);
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

    @SafeVarargs
    private static List<Method> getMethodsUpToSuperclass(final Class<?> subclass, final Class<?> terminator, final List<Predicate<? super Method>> predicates, final Predicate<? super Method>... added) {
        final List<Method> methods   = Lists.newArrayList();
        final Predicate[]  asArray   = predicates.toArray(new Predicate[0]);
        final Predicate[]  submitted = added.length == 0 ? asArray : ArrayUtils.addAll(asArray, added);
        //noinspection unchecked
        methods.addAll(ReflectionUtils.getMethods(subclass, submitted));
        final Class<?> superclass = subclass.getSuperclass();
        if (superclass != null && !superclass.equals(Object.class) && (terminator == null || !terminator.equals(superclass))) {
            methods.addAll(getMethodsUpToSuperclass(superclass, terminator, predicates));
        }
        return methods;
    }

    private static List<Predicate<? super Method>> getGetterPredicate() {
        return getGetterPredicate(null, null);
    }

    private static List<Predicate<? super Method>> getGetterPredicate(final String property, final List<Predicate<? super Method>> added) {
        final String                          pattern    = String.format(REGEX_REFL_GETTER, StringUtils.isBlank(property) ? CAPITALIZED_NAME : StringUtils.capitalize(property));
        final List<Predicate<? super Method>> predicates = Lists.newArrayList();
        predicates.add(withPattern(pattern));
        predicates.add(withParametersCount(0));
        if (added != null) {
            predicates.addAll(added);
        }
        return predicates;
    }

    private static List<Predicate<? super Method>> getSetterPredicate() {
        return getSetterPredicate(null, null);
    }

    private static List<Predicate<? super Method>> getSetterPredicate(final String property, final List<Predicate<? super Method>> added) {
        final String                          pattern    = String.format(REGEX_REFL_SETTER, StringUtils.isBlank(property) ? CAPITALIZED_NAME : StringUtils.capitalize(property));
        final List<Predicate<? super Method>> predicates = Lists.newArrayList();
        predicates.add(withPattern(pattern));
        predicates.add(withReturnType(Void.TYPE));
        if (added != null) {
            predicates.addAll(added);
        }
        return predicates;
    }

    private static final List<Class<?>> WRAPPERS   = ImmutableList.of(Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class, Short.class, Void.class);
    private static final List<Class<?>> PRIMITIVES = ImmutableList.of(boolean.class, byte.class, char.class, double.class, float.class, int.class, long.class, short.class, void.class);

    private static final Map<String, Reflections> _reflectionsCache = Collections.synchronizedMap(new HashMap<String, Reflections>());

    private static final Logger _log = LoggerFactory.getLogger(Reflection.class);
}
