package org.nrg.xdat.security.aspects;

import org.nrg.xft.event.EventUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithinWorkflow {
    // Names of arguments from which to get workflow attributes
    String executingUserArg();
    // If defined, baseElementArg (from which we get primary key and xsiType) will take precedence over values in
    // idArg/xaiType
    String baseElementArg() default "";
    String idArg() default "";
    String eventArg() default "";
    String appendToActionArg() default "";
    String externalIdArg() default "";
    String userListArgForJustification() default "";

    boolean createWorkflowAndReplaceEvent() default false;
    // Static values passed straight to workflow constructor
    String xsiType() default "";
    EventUtils.CATEGORY eventCategory();
    EventUtils.TYPE eventType();
    String action();
}
