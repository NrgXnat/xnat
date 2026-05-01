/*
 * framework: org.nrg.framework.pinto.TestPintoBeans
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.pinto;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

@PintoApplication(value = TestPintoBeans.TITLE, copyright = TestPintoBeans.COPYRIGHT, introduction = TestPintoBeans.INTRODUCTION)
public class TestPintoBeans {
    public static final String TITLE        = "TestPintoBeans Unit Tests";
    public static final String COPYRIGHT    = "(c) 2016, Washington University in St. Louis";
    public static final String INTRODUCTION = "Hello from NRG's Pinto Beans unit tests.";

    private static final String   TEST_URI                     = "https://www.xnat.org";
    private static final String   TEST_NAME                    = "foo";
    private static final int      TEST_COUNT                   = 10;
    public static final  String   TEST_TARGET_1                = "one";
    public static final  String   TEST_TARGET_2                = "two";
    public static final  String   TEST_TARGET_3                = "three";
    private static final String[] TEST_PARAMS                  = {"-n", TEST_NAME, "-u", TEST_URI, "-c", Integer.toString(TEST_COUNT), "--targets", TEST_TARGET_1, TEST_TARGET_2, TEST_TARGET_3};
    private static final String[] TEST_OVERRIDE_DEFAULT_PARAMS = {"-n", TEST_NAME, "-u", TEST_URI, "-c", Integer.toString(TEST_COUNT), "-threads", "2", "--targets", TEST_TARGET_1, TEST_TARGET_2, TEST_TARGET_3};

    @Test
    public void testBasicTestApplication() throws PintoException, URISyntaxException {
        BasicTestPintoBean bean = new BasicTestPintoBean(this, TEST_PARAMS);
        assertThat(bean).isNotNull()
                        .hasFieldOrPropertyWithValue("help", false)
                        .hasFieldOrPropertyWithValue("version", false)
                        .hasFieldOrPropertyWithValue("shouldContinue", true)
                        .hasFieldOrPropertyWithValue("name", "foo")
                        .hasFieldOrPropertyWithValue("uri", new URI(TEST_URI))
                        .hasFieldOrPropertyWithValue("count", TEST_COUNT)
                        .hasFieldOrPropertyWithValue("quitOnComplete", true)
                        .hasFieldOrPropertyWithValue("uploadThreads", 4)
                        .isNot(new Condition<>(object -> object.getTargets() == null, "Null targets"));
    }

    @Test
    public void testBasicTestApplicationWithDefaultOverride() throws PintoException, URISyntaxException {
        BasicTestPintoBean bean = new BasicTestPintoBean(this, TEST_OVERRIDE_DEFAULT_PARAMS);
        assertThat(bean).isNotNull()
                        .hasFieldOrPropertyWithValue("help", false)
                        .hasFieldOrPropertyWithValue("version", false)
                        .hasFieldOrPropertyWithValue("shouldContinue", true)
                        .hasFieldOrPropertyWithValue("name", "foo")
                        .hasFieldOrPropertyWithValue("uri", new URI(TEST_URI))
                        .hasFieldOrPropertyWithValue("count", TEST_COUNT)
                        .hasFieldOrPropertyWithValue("quitOnComplete", true)
                        .hasFieldOrPropertyWithValue("uploadThreads", 2)
                        .isNot(new Condition<>(object -> object.getTargets() == null, "Null targets"));
    }

    @Test
    public void testHelp() {
        try {
            BasicTestPintoBean bean = new BasicTestPintoBean(this, new String[]{"-h"});
            assertThat(bean).isNotNull()
                            .hasFieldOrPropertyWithValue("help", true)
                            .hasFieldOrPropertyWithValue("version", false)
                            .hasFieldOrPropertyWithValue("shouldContinue", false);
        } catch (PintoException exception) {
            fail("Found an exception in what should have been a valid parameter [" + exception.getParameter() + "]: " + exception.getType() + " " + exception.getMessage());
        }
    }

    @Test
    public void testVersion() {
        try {
            BasicTestPintoBean bean = new BasicTestPintoBean(this, new String[]{"-v"});
            assertThat(bean).isNotNull()
                            .hasFieldOrPropertyWithValue("help", false)
                            .hasFieldOrPropertyWithValue("version", true)
                            .hasFieldOrPropertyWithValue("shouldContinue", false);
        } catch (PintoException exception) {
            fail("Found an exception in what should have been a valid parameter [" + exception.getParameter() + "]: " + exception.getType() + " " + exception.getMessage());
        }
    }

    @Test
    @Disabled("Ignored because subclass methods hide base class methods. Need to walk base classes to find duplicates. Maybe the way it works now is OK?")
    public void testDuplicateParameter() {
        PintoException exception = assertThrows(PintoException.class, () -> new DuplicateParamDefinitionPintoBean(this, new String[]{"-h"}));
        assertThat(exception).isNotNull()
                             .hasFieldOrPropertyWithValue("type", PintoExceptionType.DuplicateParameter);

    }
}
