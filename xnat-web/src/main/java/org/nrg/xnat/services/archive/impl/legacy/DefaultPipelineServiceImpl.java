package org.nrg.xnat.services.archive.impl.legacy;

import lombok.extern.slf4j.Slf4j;
import org.nrg.pipeline.XnatPipelineLauncher;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.preferences.NotificationsPreferences;
import org.nrg.xdat.preferences.SiteConfigPreferences;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.preferences.PipelinePreferences;
import org.nrg.xnat.restlet.util.XNATRestConstants;
import org.nrg.xnat.services.archive.PipelineService;
import org.nrg.xnat.turbine.utils.ArcSpecManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultPipelineServiceImpl implements PipelineService {
    @Autowired
    public DefaultPipelineServiceImpl(final PipelinePreferences preferences, final SiteConfigPreferences siteConfigPreferences, final NotificationsPreferences notificationsPreferences) {
        _preferences = preferences;
        _siteConfigPreferences = siteConfigPreferences;
        _notificationsPreferences = notificationsPreferences;
    }

    @Override
    public boolean launchAutoRun(final XnatExperimentdata experiment, final boolean suppressEmail, final UserI user) {
        return launchAutoRun(experiment, suppressEmail, user, false);
    }

    @Override
    public boolean launchAutoRun(final XnatExperimentdata experiment, final boolean suppressEmail, final UserI user, final boolean waitFor) {
        try {
            if (!_preferences.isAutoRunEnabled(experiment.getProject())) {
                log.info("AutoRun pipeline was not enabled for this site or for project {}, returning true because this is fine but not launching AutoRun pipeline", experiment.getProject());
                return true;
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Couldn't find the project " + experiment.getProject() + " as specified on experiment with ID " + experiment.getId());
        }

        final XnatPipelineLauncher launcher = new XnatPipelineLauncher(user);
        launcher.setAdmin_email(_siteConfigPreferences.getAdminEmail());
        launcher.setAlwaysEmailAdmin(ArcSpecManager.GetInstance().getEmailspecifications_pipeline());
        launcher.setPipelineName(XNAT_TOOLS_AUTO_RUN_XML);
        launcher.setNeedsBuildDir(false);
        launcher.setSupressNotification(true);
        launcher.setId(experiment.getId());
        launcher.setLabel(experiment.getLabel());
        launcher.setDataType(experiment.getXSIType());
        launcher.setExternalId(experiment.getProject());
        launcher.setWaitFor(waitFor);
        launcher.setParameter(XNATRestConstants.SUPRESS_EMAIL, (Boolean.valueOf(suppressEmail)).toString());
        launcher.setParameter("session", experiment.getId());
        launcher.setParameter("sessionLabel", experiment.getLabel());
        launcher.setParameter("useremail", user.getEmail());
        launcher.setParameter("userfullname", XnatPipelineLauncher.getUserName(user));
        launcher.setParameter("adminemail", XDAT.getSiteConfigPreferences().getAdminEmail());
        launcher.setParameter("xnatserver", TurbineUtils.GetSystemName());
        launcher.setParameter("mailhost", XDAT.getNotificationsPreferences().getSmtpServer().getHostname());
        launcher.setParameter("sessionType", experiment.getXSIType());
        launcher.setParameter("xnat_project", experiment.getProject());
        return launcher.launch(null);
    }

    private static final String XNAT_TOOLS_AUTO_RUN_XML = "xnat_tools/AutoRun.xml";

    private final PipelinePreferences      _preferences;
    private final SiteConfigPreferences    _siteConfigPreferences;
    private final NotificationsPreferences _notificationsPreferences;
}
