package org.nrg.xnat.helpers.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamSource;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provides a wrapper with builder to simply managing multiple {@link XnatResourceInfo} objects. The names of the
 * resources are used as map keys, and also represent their relative path within the destination resource folder.
 */
@Slf4j
public class XnatResourceInfoMap extends HashMap<String, XnatResourceInfo> {
    /**
     * Builds for the {@link XnatResourceInfoMap} class. Provides fluent convenience methods for building and
     * aggregating multiple {@link XnatResourceInfo} instances.
     *
     * To get an instance of this builder, call {@link #builder()}.
     */
    public static class Builder {
        private Builder() {
        }

        /**
         * Specifies whether compressed archives should have their contents extracted by default. Note that this can be
         * overridden on a per-resource basis by setting {@link XnatResourceInfo#getExtract()} to <b>true</b>.
         *
         * @param extractCompressedResourcesByDefault Whether compressed resources should be extracted by default.
         *
         * @return The current builder instance.
         */
        @SuppressWarnings("unused")
        public Builder extractCompressedResourcesByDefault(final boolean extractCompressedResourcesByDefault) {
            _extractCompressedResourcesByDefault.set(extractCompressedResourcesByDefault);
            return this;
        }

        /**
         * Adds the submitted resource to the container, using the {@link XnatResourceInfo#getName() resource's
         * name} as the key.
         *
         * @param resource The resource to add to the container.
         *
         * @return The current builder instance.
         */
        public Builder resource(final XnatResourceInfo resource) {
            validate(resource.getName());
            _resources.put(resource.getName(), resource);
            return this;
        }

        /**
         * Adds a new resource to the container, using the <b>name</b> parameter as the {@link XnatResourceInfo#getName()
         * resource's name} and as the container key. The <b>source</b> parameter is set to the {@link
         * XnatResourceInfo#getSource()} property.
         *
         * @param name   The name of the resource in the container.
         * @param source An input stream source from which the resource file can be loaded.
         *
         * @return The current builder instance.
         */
        public Builder resource(final String name, final InputStreamSource source) {
            validate(name);
            _resources.put(name, XnatResourceInfo.builder().name(name).source(source).build());
            return this;
        }

        /**
         * Adds a new resource to the container, using the <b>name</b> parameter as the {@link XnatResourceInfo#getName()
         * resource's name} and as the container key. The <b>source</b> parameter is set to the {@link
         * XnatResourceInfo#getSource()} property, while the <b>format</b> and <b>content</b> parameters
         * are set for the {@link XnatResourceInfo#getFormat()} and {@link XnatResourceInfo#getContent()} properties
         * respectively.
         *
         * @param name    The name of the resource in the container.
         * @param source  An input stream source from which the resource file can be loaded.
         * @param format  The value to set for the resource file's format attribute.
         * @param content The value to set for the resource file's content attribute.
         *
         * @return The current builder instance.
         */
        public Builder resource(final String name, final InputStreamSource source, final String format, final String content) {
            validate(name);
            _resources.put(name, XnatResourceInfo.builder().name(name).source(source).format(format).content(content).build());
            return this;
        }

        /**
         * Adds a new resource to the container, using the <b>name</b> parameter as the {@link XnatResourceInfo#getName()
         * resource's name} and as the container key. The <b>file</b> parameter is used to instantiate the {@link
         * XnatResourceInfo#getSource()} property.
         *
         * @param name The name of the resource in the container.
         * @param file The resource file.
         *
         * @return The current builder instance.
         */
        public Builder resource(final String name, final File file) {
            validate(name);
            _resources.put(name, XnatResourceInfo.builder().name(name).file(file).build());
            return this;
        }

        /**
         * Adds a new resource to the container, using the <b>name</b> parameter as the {@link XnatResourceInfo#getName()
         * resource's name} and as the container key. The <b>file</b> parameter is used to instantiate the {@link
         * XnatResourceInfo#getSource()} property, while the <b>format</b> and <b>content</b> parameters
         * are set for the {@link XnatResourceInfo#getFormat()} and {@link XnatResourceInfo#getContent()} properties
         * respectively.
         *
         * @param name    The name of the resource in the container.
         * @param file    The resource file.
         * @param format  The value to set for the resource file's format attribute.
         * @param content The value to set for the resource file's content attribute.
         *
         * @return The current builder instance.
         */
        public Builder resource(final String name, final File file, final String format, final String content) {
            validate(name);
            _resources.put(name, XnatResourceInfo.builder().name(name).file(file).format(format).content(content).build());
            return this;
        }

        /**
         * Builds an instance of the {@link XnatResourceInfoMap} class populated with the configured {@link
         * XnatResourceInfo} instances.
         *
         * @return A populated resources map.
         */
        public XnatResourceInfoMap build() {
            return new XnatResourceInfoMap(_resources.values());
        }

        private void validate(final String name) {
            if (_resources.containsKey(name)) {
                throw new RuntimeException("There's already a resource with name " + name);
            }
        }

        private final Map<String, XnatResourceInfo> _resources                           = new HashMap<>();
        private final AtomicBoolean                 _extractCompressedResourcesByDefault = new AtomicBoolean();
    }

    /**
     * Gets an instance of the {@link Builder} class.
     *
     * @return An instance of the {@link Builder} class.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static XnatResourceInfoMap getFilesAsXnatResourceInfoMap(final Collection<File> files) throws FileNotFoundException {
        return getFilesAsXnatResourceInfoMap(files, null);
    }

    public static XnatResourceInfoMap getFilesAsXnatResourceInfoMap(final Collection<File> files, final @Nullable Boolean defaultExtractCompressedResource) throws FileNotFoundException {
        if (!files.stream().allMatch(File::exists)) {
            throw new FileNotFoundException("Unable to find the following source files/folders: " + files.stream().filter(file -> !file.exists()).map(File::toString).collect(Collectors.joining(", ")));
        }
        final XnatResourceInfoMap map = new XnatResourceInfoMap(files.stream().map(file -> XnatResourceInfo.builder().name(file.getName()).file(file).build()).collect(Collectors.toList()));
        if (defaultExtractCompressedResource != null) {
            map.setDefaultExtractCompressedResource(defaultExtractCompressedResource);
        }
        return map;
    }


    /**
     * Creates a new resource resources object populated with the submitted resources, using the {@link
     * XnatResourceInfo#getName() resources' name property} as the key.
     *
     * @param resources The resources used to populate the instance.
     */
    public XnatResourceInfoMap(final Collection<XnatResourceInfo> resources) {
        super(resources.stream().collect(Collectors.toMap(XnatResourceInfo::getName, Function.identity())));
    }

    /**
     * Creates a new resource resources object populated with the submitted resources, using the {@link
     * XnatResourceInfo#getName() resources' name property} as the key.
     *
     * @param resources The resources used to populate the instance.
     */
    public XnatResourceInfoMap(final XnatResourceInfo... resources) {
        this(Arrays.asList(resources));
    }

    /**
     * Creates a new resource resources object populated with a single {@link XnatResourceInfo resource}, with the
     * {@link XnatResourceInfo#getName() resource name} set to the value of the <b>name</b> parameter and the
     * <b>source</b> parameter set to the {@link XnatResourceInfo#getSource()} property.
     *
     * @param name   The name of the resource in the container.
     * @param source An input stream source from which the resource file can be loaded.
     */
    public XnatResourceInfoMap(final String name, final InputStreamSource source) {
        super(Collections.singletonMap(name, XnatResourceInfo.builder().name(name).source(source).build()));
    }

    /**
     * Creates a new resource resources object populated with {@link XnatResourceInfo resources}, with the
     * {@link XnatResourceInfo#getName() resource name} set to the keys in the <b>resources</b> map and the
     * <b>source</b> parameter set to the {@link XnatResourceInfo#getSource()} values.
     *
     * @param resources A map of keys and input stream sources.
     */
    public XnatResourceInfoMap(final Map<String, ? extends InputStreamSource> resources) {
        super(resources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> XnatResourceInfo.builder().name(entry.getKey()).source(entry.getValue()).build())));
    }

    public boolean isDefaultExtractCompressedResource() {
        return _defaultExtractCompressedResource.get();
    }

    public void setDefaultExtractCompressedResource(final boolean defaultExtractCompressedResource) {
        _defaultExtractCompressedResource.set(defaultExtractCompressedResource);
    }

    /**
     * Finds the {@link XnatResourceInfo resource} with the specified name. If the name doesn't exist in this
     * container, this method returns <b>null</b>.
     *
     * @param name The name of the {@link XnatResourceInfo resource} to retrieve.
     *
     * @return The specified {@link XnatResourceInfo resource} if present, <b>null</b> otherwise.
     */
    @SuppressWarnings("unused")
    @Nullable
    public XnatResourceInfo findByName(final String name) {
        return get(name);
    }

    /**
     * Finds all {@link XnatResourceInfo resources} in the container with the specified format.
     *
     * @param format The format of the {@link XnatResourceInfo resources} to retrieve.
     *
     * @return All {@link XnatResourceInfo resources} with the specified format value.
     */
    @SuppressWarnings("unused")
    public Set<XnatResourceInfo> findByFormat(final String format) {
        return values().stream().filter(resource -> StringUtils.equals(resource.getFormat(), format)).collect(Collectors.toSet());
    }

    /**
     * Finds all {@link XnatResourceInfo resources} in the container with the specified content.
     *
     * @param content The content of the {@link XnatResourceInfo resources} to retrieve.
     *
     * @return All {@link XnatResourceInfo resources} with the specified content value.
     */
    @SuppressWarnings("unused")
    public Set<XnatResourceInfo> findByContent(final String content) {
        return values().stream().filter(resource -> StringUtils.equals(resource.getContent(), content)).collect(Collectors.toSet());
    }

    /**
     * Finds all {@link XnatResourceInfo resources} in the container with the specified format and content.
     *
     * @param format  The format of the {@link XnatResourceInfo resources} to retrieve.
     * @param content The content of the {@link XnatResourceInfo resources} to retrieve.
     *
     * @return All {@link XnatResourceInfo resources} with the specified format and content values.
     */
    @SuppressWarnings("unused")
    public Set<XnatResourceInfo> findByContent(final String format, final String content) {
        return values().stream().filter(resource -> StringUtils.equals(resource.getFormat(), format) && StringUtils.equals(resource.getContent(), content)).collect(Collectors.toSet());
    }

    private final AtomicBoolean _defaultExtractCompressedResource = new AtomicBoolean();
}
