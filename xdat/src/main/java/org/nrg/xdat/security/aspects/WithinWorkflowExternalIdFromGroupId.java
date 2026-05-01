package org.nrg.xdat.security.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WithinWorkflowExternalIdFromGroupId {
    WithinWorkflow withinWorkflow();
    String groupIdArg();
}
