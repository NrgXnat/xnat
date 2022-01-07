package org.nrg.xdat.security.aspects;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.security.helpers.Groups;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class WithinWorkflowAspect {
    @Pointcut("@annotation(withinWorkflow)")
    public void withinWorkflowPointcut(final WithinWorkflow withinWorkflow) {
    }

    @Pointcut("@annotation(withinWorkflowExternalIdFromGroupId)")
    public void withinWorkflowExternalIdFromGroupIdPointCut(final WithinWorkflowExternalIdFromGroupId withinWorkflowExternalIdFromGroupId) {
    }

    @Around(value = "withinWorkflowPointcut(withinWorkflow)", argNames = "joinPoint, withinWorkflow")
    public Object wrapWithinWorkflow(final ProceedingJoinPoint joinPoint, final WithinWorkflow withinWorkflow) throws Throwable {
        return wrapWithWorkflow(joinPoint, withinWorkflow);
    }

    @Around(value = "withinWorkflowExternalIdFromGroupIdPointCut(withinWorkflowExternalIdFromGroupId)", argNames = "joinPoint, withinWorkflowExternalIdFromGroupId")
    public Object wrapWithinWorkflowExternalIdFromGroupId(final ProceedingJoinPoint joinPoint, final WithinWorkflowExternalIdFromGroupId withinWorkflowExternalIdFromGroupId) throws Throwable {
        return wrapWithWorkflow(joinPoint, withinWorkflowExternalIdFromGroupId.withinWorkflow(),
                withinWorkflowExternalIdFromGroupId.groupIdArg());
    }

    private Object wrapWithWorkflow(ProceedingJoinPoint joinPoint, WithinWorkflow withinWorkflow) throws Throwable {
        return wrapWithWorkflow(joinPoint, withinWorkflow, null);
    }

    private Object wrapWithWorkflow(ProceedingJoinPoint joinPoint, WithinWorkflow withinWorkflow,
                                    @Nullable String groupIdArg)
            throws Throwable {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method  = signature.getMethod();

        UserI authorizingUser = null;
        String id = null;
        String itemId = null;
        String xsiType = null;
        String externalId = PersistentWorkflowUtils.ADMIN_EXTERNAL_ID;
        EventMetaI eventMeta = null;
        AtomicReference<String> action = new AtomicReference<>(withinWorkflow.action());
        String justification = null;
        int eventIndex = -1;

        String[] parameterNames = signature.getParameterNames();;
        Object[] parameterValues = joinPoint.getArgs();
        for (int i = 0; i < method.getParameterCount(); i++) {
            String parameterName = parameterNames[i];
            if (parameterName.equals(withinWorkflow.idArg())) {
                id = (String) parameterValues[i];
            } else if (parameterName.equals(withinWorkflow.baseElementArg())) {
                XFTItem item = ((BaseElement) parameterValues[i]).getItem();
                itemId = item.getPK().toString();
                xsiType = item.getXSIType();
            } else if (parameterName.equals(withinWorkflow.executingUserArg())) {
                authorizingUser = (UserI) parameterValues[i];
            } else if (parameterName.equals(withinWorkflow.externalIdArg())) {
                externalId = (String) parameterValues[i];
            } else if (parameterName.equals(withinWorkflow.eventArg())) {
                eventMeta = (EventMetaI) parameterValues[i];
                eventIndex = i;
            }
            if (parameterName.equals(withinWorkflow.appendToActionArg())) {
                final String appentToAction = parameterValues[i].toString();
                action.getAndUpdate(current -> current + " " + appentToAction);
            }
            if (parameterName.equals(withinWorkflow.userListArgForJustification())) {
                justification = JUSTIFICATION_FROM_USERS_LIST.apply(parameterValues[i]);
            }
            if (parameterName.equals(groupIdArg)) {
                externalId = EXTERNAL_ID_FROM_GROUP_ID.apply(parameterValues[i], action);
            }
        }

        // Prefer these from xftItem
        id = StringUtils.defaultIfBlank(itemId, id);
        xsiType = StringUtils.defaultIfBlank(xsiType, withinWorkflow.xsiType());

        PersistentWorkflowI workflow = null;
        if (eventMeta == null) {
            workflow = PersistentWorkflowUtils.buildOpenWorkflow(authorizingUser, xsiType, id,
                    externalId, EventUtils.newEventInstance(withinWorkflow.eventCategory(),
                            withinWorkflow.eventType(), action.get()));
            workflow.setJustification(justification);
            eventMeta = workflow.buildEvent();
            if (eventIndex != -1) {
                parameterValues[eventIndex] = eventMeta;
            }
        }

        Object rtn;
        try {
            rtn = joinPoint.proceed(parameterValues);
            if (workflow != null) {
                PersistentWorkflowUtils.complete(workflow, eventMeta);
            }
        } catch (Exception e) {
            if (workflow != null) {
                workflow.setDetails(e.getMessage());
                PersistentWorkflowUtils.fail(workflow, eventMeta);
            }
            throw e;
        }

        return rtn;
    }

    private static final BiFunction<Object, AtomicReference<String>, String> EXTERNAL_ID_FROM_GROUP_ID = (object, action) -> {
        String groupId = (String) object;
        String project = Groups.getGroup(groupId).getTag();
        if (StringUtils.isBlank(project)) {
            project = PersistentWorkflowUtils.ADMIN_EXTERNAL_ID;
            action.getAndUpdate(current -> current.replace("project", "group " + groupId));
        }
        return project;
    };

    private static final Function<Object, String> JUSTIFICATION_FROM_USERS_LIST = (object) -> {
        @SuppressWarnings("unchecked")
        List<UserI> modifiedUsers = (List<UserI>) object;
        return "Modified users " + modifiedUsers.stream().map(UserI::getUsername).collect(Collectors.joining(","));
    };
}
