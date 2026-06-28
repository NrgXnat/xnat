/*
 * xnat-data-builder: org.nrg.xnat.build.tasks.XnatPluginInfoTask
 * XNAT https://www.xnat.org
 * Copyright (c) 2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.build.tasks

import groovy.transform.Memoized
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.RemoteConfig
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.nrg.framework.exceptions.NrgServiceError
import org.nrg.framework.exceptions.NrgServiceRuntimeException
import org.nrg.xnat.build.utils.PropertyReader

import java.nio.file.Paths

import static groovy.io.FileType.FILES

/**
 * All of the git functionality here was liberally "borrowed" from the very nice gradle-git-version plugin. You can find
 * the source code for that at:
 *
 * https://github.com/palantir/gradle-git-version
 *
 * I tried to just leverage that plugin in here but couldn't get it to work, so I finally just submitted and stole...
 * er, I mean, borrowed. Many thanks to the authors of this plugin! - Rick
 */
@org.gradle.work.DisableCachingByDefault(because = "Reads git state at execution time")
class XnatPluginInfoTask extends DefaultTask {
    XnatPluginInfoTask() {
        group = "xnat"
        description = "Updates the XNAT plugin properties with build-specific information, including version, build " +
                "date and number (if available), and git repository commit information."
    }

    @TaskAction
    def process() {
        logger.lifecycle("Generating build properties for plugin ${project.name}")
        def PropertyReader properties = getPluginProperties()
        properties."xnat.plugin.name" = project.name
        properties."xnat.plugin.build.date" = new Date()
        if (hasProperty("BUILD_NUMBER")) {
            properties."xnat.plugin.build.number" = properties.getProperty("BUILD_NUMBER")
        }
        try {
            def revision = gitRevision(project)
            if (revision) {
                properties."xnat.plugin.build.commit" = revision
            }
            def remote = gitRemote(project)
            if (remote) {
                properties."xnat.plugin.build.repo" = remote
            }
        } catch (IllegalArgumentException e) {
            // TODO: Would be nice to add support for remote and revision from Mercurial, e.g.: https://bitbucket.org/aragost/javahg
            logger.info("No revision or remote information found: ${e.message}")
        }
        properties."xnat.plugin.build.version" = project.version
    }

    @Input
    def PropertyReader getPluginProperties() {
        def File xnatMetaInfFolder = Paths.get(project.buildDir.absolutePath, "classes", "main", "META-INF", "xnat").toFile()
        def List<String> pluginProperties = []
        if (xnatMetaInfFolder.exists()) {
            xnatMetaInfFolder.eachFileRecurse(FILES) { file ->
                if (file.name.endsWith('-plugin.properties')) {
                    pluginProperties << file.absolutePath
                }
            }
        }
        switch (pluginProperties.size()) {
            case 0:
                logger.warn("No plugin properties found in build output. The generated plugin info will be much more useful if you include an @XnatPlugin-annotated class.")
                new PropertyReader(Paths.get(xnatMetaInfFolder.absolutePath, "${project.name}-plugin.properties").toString())
                break
            case 1:
                new PropertyReader(pluginProperties.get(0))
                break;
            default:
                throw new NrgServiceRuntimeException(NrgServiceError.ConfigurationError, "There are multiple plugin properties files in here. There should be only one per plugin build.")
        }
    }

    @Memoized
    private static Git getGit(Project project) {
        Git.wrap(new FileRepository(getRootGitDir(project.rootDir)))
    }

    @Memoized
    private static String gitRevision(Project project) {
        Git git = getGit(project)
        try {
            String revision = null
            def call = git.log().call()
            if (call != null) {
                for (final RevCommit commit : call) {
                    revision = commit.name
                    break
                }
            }
            if (revision) {
                revision + (git.status().call().isClean() ? "" : ".dirty")
            } else {
                null
            }
        } catch (Throwable ignored) {
            null
        }
    }

    @Memoized
    private static String gitRemote(Project project) {
        Git git = getGit(project)
        for (final RemoteConfig remote : git.remoteList().call()) {
            if (remote.name == "origin") {
                return remote.getURIs().get(0)
            }
        }
        null
    }

    private static File getRootGitDir(File currentRoot) {
        File gitDir = scanForRootGitDir(currentRoot)
        if (!gitDir.exists()) {
            throw new IllegalArgumentException("Cannot find '.git' directory")
        }
        return gitDir
    }

    private static File scanForRootGitDir(File currentRoot) {
        File gitDir = new File(currentRoot, ".git")

        if (gitDir.exists()) {
            return gitDir
        }

        // stop at the root directory, return non-existing File object
        if (currentRoot.parentFile == null) {
            return gitDir
        }

        // look in parent directory
        return scanForRootGitDir(currentRoot.parentFile)
    }
}
