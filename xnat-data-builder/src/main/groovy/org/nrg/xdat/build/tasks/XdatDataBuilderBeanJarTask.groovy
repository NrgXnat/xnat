/*
 * xdat-data-builder: org.nrg.xdat.build.tasks.XdatDataBuilderBeanJarTask
 * XNAT https://www.xnat.org
 * Copyright (c) 2020, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.build.tasks

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Bean JAR task depends on generated source output")
abstract class XdatDataBuilderBeanJarTask extends Jar {
    @Input
    boolean buildBeanJar = false

    @Internal
    boolean hasSourceSets = false

    XdatDataBuilderBeanJarTask() {
        // Set group and description
        group = "xnat"
        description = "Creates the beans jar from classes in the org.nrg.xdat.bean and org.nrg.xdat.model packages."

        def sourceSets = ((SourceSetContainer) project.properties.get("sourceSets"))
        if (buildBeanJar && sourceSets != null) {
            hasSourceSets = true

            // Set basename for this to include "-beans"
            archiveBaseName.set "${project.name}-beans"

            // Set a specific source set to include just bean and model classes.
            def mainSourceSet = sourceSets.getByName("main")
            from mainSourceSet.output
            includes = ["org/nrg/xdat/bean/**/*", "org/nrg/xdat/model/**/*"]
        }
    }

    @TaskAction
    def process() {
        if (hasSourceSets) {
            // Nothing to do in here, everything is done at configuration.
            logger.lifecycle("Creating beans jar ${archiveBaseName}")
        }
    }
}
