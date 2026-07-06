package org.nrg.xnat.turbine.pipeline;

import org.apache.commons.lang3.StringUtils;
import org.apache.turbine.Turbine;
import org.apache.turbine.TurbineConstants;
import org.apache.turbine.pipeline.PipelineData;
import org.apache.turbine.pipeline.Valve;
import org.apache.turbine.pipeline.ValveContext;
import org.apache.turbine.util.RunData;
import org.apache.turbine.util.TurbineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Restores the Turbine 2.3.3 {@code template.homepage} default that Turbine's 4.0 service-container
 * rewrite dropped.
 *
 * <p>Turbine 5.1 no longer reads {@code template.homepage} / {@code screen.homepage} — the
 * {@link TurbineConstants} entries still exist but no class references them. As a result a request with
 * no screen target reaches {@link org.apache.turbine.modules.pages.TemplatePage} with a {@code null}
 * screen template and fails with "Couldn't map Template null to any Screen class!". The most common way
 * to hit this is the bare context root: the {@code app} welcome-file (web.xml) forwards {@code /} to
 * {@code /app} with no path-info.
 *
 * <p>When both the screen target and the action are empty — i.e. a bare entry request — this valve sets
 * the screen template to the configured homepage ({@code template.homepage}, default {@code Index.vm}),
 * exactly restoring the pre-5.1 behavior. Action requests are deliberately left untouched: their screen
 * is set while the action runs in {@code ExecutePageValve}, which is invoked after this valve.
 *
 * <p>Wired into {@code WEB-INF/conf/turbine-classic-pipeline.xml} immediately after
 * {@code DetermineTargetValve} and before {@code ExecutePageValve}.
 */
public class DefaultHomepageTargetValve implements Valve {

    private static final Logger log             = LoggerFactory.getLogger(DefaultHomepageTargetValve.class);
    private static final String DEFAULT_HOMEPAGE = "Index.vm";

    @Override
    public void invoke(final PipelineData pipelineData, final ValveContext context)
            throws IOException, TurbineException {
        final RunData data = pipelineData.getRunData();
        if (StringUtils.isEmpty(data.getScreenTemplate()) && StringUtils.isEmpty(data.getAction())) {
            final String homepage = Turbine.getConfiguration().getString(TurbineConstants.TEMPLATE_HOMEPAGE, DEFAULT_HOMEPAGE);
            log.debug("Empty-target entry request; defaulting screen template to homepage [{}]", homepage);
            data.setScreenTemplate(homepage);
        }
        context.invokeNext(pipelineData);
    }
}
