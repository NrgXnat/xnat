/*
 * xnat-data-builder: org.nrg.xnat.build.XnatDataBuilderPlugin
 * XNAT https://www.xnat.org
 * Copyright (c) 2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.build

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.UnknownTaskException
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.GradleVersion
import org.nrg.xdat.build.tasks.XdatDataBuilderBeanJarTask
import org.nrg.xdat.build.tasks.XdatDataBuilderGenerateSourceTask
import org.nrg.xnat.build.extensions.XnatDataBuilderExtension
import org.nrg.xnat.build.tasks.XnatPluginInfoTask

class XnatDataBuilderPlugin implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create(XnatDataBuilderExtension.NAME, XnatDataBuilderExtension)

        final pluginInfo = project.task('xnatPluginInfo', type: XnatPluginInfoTask)
        final dataBuilder = project.task('xnatDataBuilder', type: XdatDataBuilderGenerateSourceTask)
        final beanJar = project.task('xnatBeanJar', type: XdatDataBuilderBeanJarTask)

        // This will eventually kick off the source generation task to pre-populate any code dependencies required.
        project.plugins.withType(JavaPlugin) {
            project.afterEvaluate {
                project.logger.lifecycle "Completed evaluation of project ${project.name}."
                addFrameworkDependency(project)
            }
        }

        try {
            // Set the primary compileJava task to depend on generate source.
            (project.tasks.getByName("compileJava") as JavaCompile).with {
                dependsOn dataBuilder
                doLast {
                    pluginInfo
                }
            }

            // Set the primary jar task to trigger bean jar task.
            (project.tasks.getByName("jar") as Jar).with {
                dependsOn beanJar
                // Add "-plugin" to the output jar name if it's not already there.
                archiveBaseName.set "${project.name}${project.name.endsWith("-plugin") ? "" : "-plugin"}"
            }
        } catch (UnknownTaskException ignored) {
            // Ignore this, just means there's no compilation task.
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void addFrameworkDependency(Project project) {
        boolean atLeastGradle4_6 = GradleVersion.version(project.gradle.gradleVersion) >= GradleVersion.version('4.6')
        if (atLeastGradle4_6) {
            [JavaPlugin.ANNOTATION_PROCESSOR_CONFIGURATION_NAME, JavaPlugin.TEST_ANNOTATION_PROCESSOR_CONFIGURATION_NAME].each { configurationName ->
                project.dependencies.add(configurationName, "org.nrg:framework:${project.xnatDataBuilderExtension.version}")
            }
        }
    }
}
