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
 * screen template and fails with "Couldn't map Template null to any Screen class!". Two ways to hit it:
 * the bare context root (the {@code app} welcome-file forwards {@code /} to {@code /app} with no
 * path-info), and any ACTION request whose action ends without setting a screen template or redirect —
 * e.g. {@code QuickSearchAction}'s no-match / error paths, which only call {@code setMessage()}.
 *
 * <p>In 2.3.3 the fallback lived in {@code TemplateSessionValidator.doPerform} ("make sure we have some
 * way to return a response"): whenever no screen and no screen template were set it applied
 * {@code template.homepage} — regardless of whether an action was present, because it ran BEFORE the
 * action. The action could then override the template ({@code TemplatePage.doBuildAfterAction} re-reads
 * it after the action runs) or set a redirect ({@code DefaultPage.doBuild} early-returns). This valve
 * restores exactly that condition; an earlier version also required the action to be empty, which
 * regressed the message-only action paths to the null-template exception.
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
        // 2.3.3 TemplateSessionValidator condition: no explicit screen AND no screen template. No action
        // check — the fallback must apply to action requests too, so an action that ends without setting
        // a template (message-only paths) still renders the homepage instead of "Couldn't map Template
        // null". Actions that do set a template or redirect override this (they run later, in
        // ExecutePageValve).
        if (!data.hasScreen() && StringUtils.isEmpty(data.getScreenTemplate())) {
            final String homepage = Turbine.getConfiguration().getString(TurbineConstants.TEMPLATE_HOMEPAGE, DEFAULT_HOMEPAGE);
            log.debug("No screen target on [{}]; defaulting screen template to homepage [{}]", data.getRequest().getRequestURI(), homepage);
            data.setScreenTemplate(homepage);
        }
        context.invokeNext(pipelineData);
    }
}
