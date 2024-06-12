/*
 * framework: org.nrg.framework.utilities.ConfigPathsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.nrg.framework.configuration.ConfigPaths;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ConfigPathsTest {
    @Before
    public void setUpPropertiesFiles() throws IOException {
        _root = Paths.get(System.getProperty("java.io.tmpdir"), getClass().getSimpleName() + "-" + (new Date().getTime())).toFile();
        _root.mkdirs();
        final Path firstParent = Paths.get(_root.getAbsolutePath(), "first");
        firstParent.toFile().mkdirs();
        _first = firstParent.resolve("first.properties").toFile();
        try (final PrintWriter output = new PrintWriter(new FileWriter(_first))) {
            output.println("first.one=1");
            output.println("first.two=2");
            output.println("first.three=3");
            output.println("first.four=4");
        }
        final Path secondParent = Paths.get(_root.getAbsolutePath(), "second");
        secondParent.toFile().mkdirs();
        _second = secondParent.resolve("second.properties").toFile();
        try (final PrintWriter output = new PrintWriter(new FileWriter(_second))) {
            output.println("second.one=1");
            output.println("second.two=2");
            output.println("second.three=3");
            output.println("second.four=4");
        }
    }

    @After
    public void cleanUpPropertiesFiles() {
        _first.delete();
        _first.getParentFile().delete();
        _second.delete();
        _second.getParentFile().delete();
        _root.delete();
    }

    @Test
    public void testConfigPaths() {
        final ConfigPaths configPaths = new ConfigPaths();
        configPaths.add(Paths.get(_root.getAbsolutePath(), "first"));
        configPaths.add(Paths.get(_root.getAbsolutePath(), "second"));
        final List<File> paths = configPaths.findFiles("first.properties", "second.properties");
        assertTrue(paths.contains(_first));
        assertTrue(paths.contains(_second));
    }

    private File _root;
    private File _first;
    private File _second;
}
