/*
 * framework: org.nrg.framework.utilities.ConfigPathsTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class NameUtilsTest {
    @Test
    public void testNameUtils() {
        final List<String> resourceNames = NameUtils.convertBeanIdsToResourceNames(BEAN_IDS);
        assertThat(resourceNames, IsIterableContainingInOrder.contains(RESOURCE_NAMES.toArray()));
        assertThat(NameUtils.convertResourceNamesToBeanIds(resourceNames), IsIterableContainingInOrder.contains(BEAN_IDS.toArray()));
        assertThat(NameUtils.convertClassNamesToBeanIds(CLASS_NAMES), IsIterableContainingInOrder.contains(BEAN_IDS.toArray()));
    }

    private static final List<String> BEAN_IDS       = Arrays.asList("thisIsThingOne", "thisIsThingTwo", "thisIsThingThree");
    private static final List<String> RESOURCE_NAMES = Arrays.asList("this-is-thing-one", "this-is-thing-two", "this-is-thing-three");
    private static final List<String> CLASS_NAMES    = Arrays.asList("one.two.three.ThisIsThingOne", "com.example.who.ThisIsThingTwo", "org.nrg.framework.test.ThisIsThingThree");
}
