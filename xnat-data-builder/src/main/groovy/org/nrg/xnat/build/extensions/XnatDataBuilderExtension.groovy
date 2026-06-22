package org.nrg.xnat.build.extensions

/**
 * Configuration for the plugin.
 */
class XnatDataBuilderExtension {
    static final String NAME = "xnatDataBuilderExtension"

    /** The version of XNAT dependencies to use. */
    String version

    XnatDataBuilderExtension() {
        try (final InputStream input = this.class.classLoader.getResourceAsStream("xnat-data-builder-metadata.properties")) {
            Properties metadata = new Properties()
            metadata.load input
            version = metadata.getProperty("version")
        }
    }
}
