package org.nrg.xnat.compute.models;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class ConstraintTest {

    @Test
    public void testListConversion_IN() {
        Constraint constraint = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
                .build();

        assertEquals(Arrays.asList("node.instance.type==spot", "node.instance.type==demand"), constraint.toList());
    }

    @Test
    public void testListConversion_NOT_IN() {
        Constraint constraint = Constraint.builder()
                .key("node.instance.type")
                .operator(Constraint.Operator.NOT_IN)
                .values(new HashSet<>(Arrays.asList("spot", "demand")))
                .build();

        assertEquals(Arrays.asList("node.instance.type!=spot", "node.instance.type!=demand"), constraint.toList());
    }

}