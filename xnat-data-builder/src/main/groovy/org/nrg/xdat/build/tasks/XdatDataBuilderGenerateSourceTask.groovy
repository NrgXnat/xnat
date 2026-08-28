/*
 * xdat-data-builder: org.nrg.xdat.build.tasks.XdatDataBuilderGenerateSourceTask
 * XNAT https://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.build.tasks

import org.apache.commons.io.FileUtils
import org.gradle.api.DefaultTask
import org.gradle.api.UnknownTaskException
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.javadoc.Javadoc
import org.nrg.xft.generators.GenerateResources

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import static java.nio.file.FileVisitOption.FOLLOW_LINKS
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Generates source from XSD schemas, output depends on external state")
class XdatDataBuilderGenerateSourceTask extends DefaultTask {
    @Internal
    Path generated
    @Internal
    File java
    @Internal
    File resources

    XdatDataBuilderGenerateSourceTask() {
        // Set group and description
        group = "xnat"
        description = "Generates Java, Velocity, and other resources from XNAT datatype-definition schemas."

        // Everything will be generated under this folder.
        generated = Paths.get project.buildDir.absolutePath, "xnat-generated"
        java = generated.resolve("src/main/java").toFile()
        resources = generated.resolve("src/main/resources").toFile()
    }

    @TaskAction
    def process() {
        project.tasks.findAll { task -> Javadoc.class.isAssignableFrom(task.class) }.forEach { task ->
            (task as Javadoc).failOnError = false
        }

        try {
            (project.tasks.getByName("compileJava") as JavaCompile).source java
            (project.tasks.getByName("processResources") as Copy).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
            (project.tasks.getByName("sourceJar") as Jar).duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        } catch (UnknownTaskException ignored) {
            // Ignore this, just means there's no compilation task.
        }

        // Get the root project path.
        def root = Paths.get(project.projectDir.absolutePath)

        // Where schema are located
        def schemaSources = root.resolve("src/main/resources/schemas").toFile()
        def schemaCount = !schemaSources.exists() ? 0 : Files.walk(schemaSources.toPath(), FOLLOW_LINKS).findAll { it =~ /^.*\.xsd$/ }.size()
        if (schemaCount == 0) {
            logger.lifecycle "No schemas found to generate resources for"
        } else {
            logger.lifecycle "Found schema files to be processed", schemaCount

            // Delete generated resources if they exist
            if (generated.toFile().exists()) {
                logger.debug "Deleting existing generated file directory {}", generated
                generated.toFile().deleteDir()
            }

            // Each type of content is generated into a particular location.
            def resourcePath = Paths.get(resources.toURI())
            def schemas = resourcePath.resolve("schemas").toFile()
            def templates = resourcePath.resolve("META-INF/resources/base-templates/screens").toFile()
            def javascript = resourcePath.resolve("META-INF/resources/scripts").toFile()

            //copy schemas to destination (including display docs if they are there)
            FileUtils.copyDirectory(schemaSources, schemas, new FileFilter() {
                @Override
                boolean accept(final File pathname) {
                    pathname.directory || pathname.name =~ /^.*\.(xml|xsd)/
                }
            })

            // Where customized classes are defined (like customized Base*.java)
            def javaSources = Paths.get(project.projectDir.absolutePath, "src/main/java").toFile()

            // Generate the resources in the destination
            logger.info "Running generate resources for project {}:\n * Schemas: {}\n * Java: {}\n * Templates: {}\n * Java sources: {}\n * Resources: {}\n * JavaScript: {}", project.name, schemas.getAbsolutePath(), java.absolutePath, templates.absolutePath, javaSources.absolutePath, resources.absolutePath, javascript.absolutePath
            new GenerateResources(project.name, schemas.getAbsolutePath(), java.absolutePath, templates.absolutePath, javaSources.absolutePath, java.absolutePath, resources.absolutePath, javascript.absolutePath).process()
        }
    }
}
