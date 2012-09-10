/**
 * Copyright 2012 Washington University
 */
package org.nrg.xft.utils.ValidationUtils;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;

import com.google.common.collect.Lists;

/**
 * @author Kevin A. Archie <karchie@wustl.edu>
 *
 */
public class XFTValidatorTest {

    /**
     * Test method for {@link org.nrg.xft.utils.ValidationUtils.XFTValidator#ValidateValue(java.lang.Object, java.util.ArrayList, java.lang.String, org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField, org.nrg.xft.utils.ValidationUtils.ValidationResults, java.lang.String, org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement)}.
     */
    @Test
    public void testValidateValueLongValue() {
        final ArrayList<String[]> rules = Lists.<String[]>newArrayList(new String[]{"required", "false", "xs:long"});
        final String nsPrefix = "xs";
        final ValidationResults vr = new ValidationResults();
        final String xmlPath = "foo";
        final GenericWrapperElement element = new GenericWrapperElement();

        XFTValidator.ValidateValue("0", rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());

        XFTValidator.ValidateValue("-256", rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());

        XFTValidator.ValidateValue("42", rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());    

        XFTValidator.ValidateValue(Long.valueOf(Integer.MIN_VALUE - 1L).toString(),
                rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());

        XFTValidator.ValidateValue(Long.valueOf(Integer.MAX_VALUE + 1L).toString(),
                rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());

        XFTValidator.ValidateValue(String.valueOf(Long.MIN_VALUE),
                rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());

        XFTValidator.ValidateValue(String.valueOf(Long.MAX_VALUE),
                rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());
        
        XFTValidator.ValidateValue("", rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());
    }

    @Test
    public void testValidateValueLongValueBelowMin() {
        final ArrayList<String[]> rules = Lists.<String[]>newArrayList(new String[]{"required", "false", "xs:long"});
        final String nsPrefix = "xs";
        final ValidationResults vr = new ValidationResults();
        final String xmlPath = "foo";
        final GenericWrapperElement element = new GenericWrapperElement();

        XFTValidator.ValidateValue(String.valueOf(Long.MIN_VALUE) + "0",
                rules, nsPrefix, null, vr, xmlPath, element);
        assertFalse(vr.isValid());
    }

    @Test
    public void testValidateValueLongValueAboveMax() {
        final ArrayList<String[]> rules = Lists.<String[]>newArrayList(new String[]{"required", "false", "xs:long"});
        final String nsPrefix = "xs";
        final ValidationResults vr = new ValidationResults();
        final String xmlPath = "foo";
        final GenericWrapperElement element = new GenericWrapperElement();

        XFTValidator.ValidateValue(String.valueOf(Long.MAX_VALUE) + "0",
                rules, nsPrefix, null, vr, xmlPath, element);
        assertFalse(vr.isValid());
    }
    
    @Test
    public void testValidateValueLongValueRequired() {
        final ArrayList<String[]> rules = Lists.<String[]>newArrayList(new String[]{"required", "true", "xs:long"});
        final String nsPrefix = "xs";
        final ValidationResults vr = new ValidationResults();
        final String xmlPath = "foo";
        final GenericWrapperElement element = new GenericWrapperElement();

        XFTValidator.ValidateValue("0", rules, nsPrefix, null, vr, xmlPath, element);
        assertTrue(vr.isValid());

        XFTValidator.ValidateValue("", rules, nsPrefix, null, vr, xmlPath, element);
        assertFalse(vr.isValid());
    }
}
