/*
 * framework: org.nrg.framework.orm.hibernate.annotations.Auditable
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.orm.hibernate.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.envers.Audited;
import org.nrg.framework.orm.hibernate.BaseHibernateEntity;

/**
 * @deprecated Use the Hibernate Envers {@link Audited} annotation instead.
 */
@Documented
@Target(TYPE)
@Retention(RUNTIME)
@Deprecated
public @interface Auditable {

}
