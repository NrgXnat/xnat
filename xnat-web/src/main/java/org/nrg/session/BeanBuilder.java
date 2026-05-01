/*
 * SessionBuilders: org.nrg.session.BeanBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.session;

import java.util.Collection;

import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.xdat.bean.base.BaseElement;

public interface BeanBuilder {
  Collection<? extends BaseElement> buildBeans(ExtAttrValue value) throws ConversionFailureException;
}
