package org.nrg.pipeline;

import java.util.*;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.springframework.stereotype.Component;


@Data
@Component
public class PipelineLaunchParameters {

    @NonNull
    private UserI user;


    private String id;
    private String label;
    private boolean runPipelineInProcess;
    @Builder.Default
    private boolean recordWorkflowEntries = true;

    @Builder.Default
    private boolean alwaysEmailAdmin = true;

    private boolean useAlias;
    private boolean supressNotification;
    private boolean waitFor;

    @Builder.Default
    private boolean needsBuildDir = true;

    @Builder.Default
    private Set<String> notificationEmailIds  = new HashSet<>();;

    @Builder.Default
    private Map<String, List<String>> parameters = new Hashtable<>();

    private String pipelineName;
    private String externalId; // Workflows External Id
    private String dataType;
    private String startAt;
    private String parameterFile;
    private String admin_email;

    private final String host;

    private String buildDir;

    @Builder
    public PipelineLaunchParameters(final UserI user) {
        this.user = user;
        final String pipelineUrl = XDAT.safeSiteConfigProperty("processingUrl", "");
        host = StringUtils.isNotBlank(pipelineUrl) ? pipelineUrl : TurbineUtils.GetFullServerPath();
    }


    public void setParameter(String name, String value) {
        if (parameters.containsKey(name)) {
            parameters.get(name).add(value);
        } else {
            parameters.put(name, Collections.singletonList(value));
        }
    }

    public void setParameter(String name, ArrayList<String> values) {
        if (values != null && values.size() > 0) {
            if (parameters.containsKey(name)) {
                parameters.get(name).addAll(values);
            } else {
                parameters.put(name, values);
            }
        }
    }


    public void notificationEmailId(final String... emailId) {
        notificationEmailIds.addAll(Arrays.asList(emailId));
    }

    public static String getUserName(UserI user) {
        String rtn = "";
        try {
            if (user.getFirstname() != null && user.getLastname() != null) rtn = user.getFirstname().substring(0, 1) + "." + user.getLastname();
        } catch (Exception ignored) {
        }
        return rtn;
    }

}