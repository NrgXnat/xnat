package org.nrg.xnat.services.cache;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheLock {
    /**
     * Indicates whether the lock should be for read (false) or write (true). Note that this
     * attribute is an alias for {@link #write()}.
     */
    @AliasFor(annotation = CacheLock.class, attribute = "write")
    boolean value() default false;

    /**
     * Indicates whether the lock should be for read (false) or write (true).
     */
    boolean write() default false;
}
