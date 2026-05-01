/*
 * framework: org.nrg.framework.annotations.XnatMixIn
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.annotations;

import java.lang.annotation.*;

/**
 * Associates a mix-in class with one or more classes that require custom serialization configuration. Mix-in classes
 * provide a way to customize how instances of associated classes are serialized by the Jackson object mapper framework.
 * <a href="http://www.baeldung.com/jackson-annotations">This article</a> describes using annotations to control JSON
 * serialization and how to use mix-in classes to apply this to other data types. This annotation provides different
 * methods to configure how mix-ins are associated with the target classes:
 *
 * <ul>
 *     <li>Specifying the {@link #value()} attribute associates the <i>current class</i> with the mix-in class specified
 *     as the value. That is, <b>@XnatMixIn(Foo.class)</b> on a class <b>Bar</b> would use <b>Foo</b> as the mix-in for
 *     <b>Bar</b>.</li>
 *     <li>Specifying the {@link #targets()} attribute associates the current class <i>as a mix-in class</i> with one or
 *     more target classes specified in the value. For example, <b>@XnatMixIn({Foo.class, Bar.class})</b> on the
 *     <b>Zed</b> class would configure <b>Zed</b> as the mix-in class for both <b>Foo</b> and <b>Bar</b>.</li>
 *     <li>Specifying both {@link #value()} and {@link #targets()} lets you associate a mix-in class with one or more
 *     target classes without having to modify either the mix-in or the target.</li>
 * </ul>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@Documented
public @interface XnatMixIn {
    /**
     * Indicates the mix-in class to be used when serializing the target class. If no {@link #targets()} are specified,
     * the annotated class is the target.
     *
     * @return The mix-in class.
     */
    Class<?>[] value() default {};

    /**
     * Indicates the class or classes that the serializer should associate with a mix-in class. If no value is set for
     * the {@link #value()} attribute, the mix-in class is set to the annotated class.
     *
     * @return The target class or classes.
     */
    Class<?>[] targets() default {};
}
