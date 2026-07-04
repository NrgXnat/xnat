package org.nrg.xnat.turbine;

import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.util.TurbineConfig;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

/**
 * Boots XNAT's actual Turbine 5.1 service container (WEB-INF/conf/TurbineResources.properties plus
 * the YAAFI roleConfiguration.xml / componentConfiguration.xml) via {@link TurbineConfig}, outside a
 * servlet container, and asserts the core render + service-container services initialize.
 *
 * <p>This is the Phase-0b "Rung 2" boot test: it validates the migrated config (service classnames,
 * YAAFI XML, Fulcrum roles, the custom Velocity loader wiring) before a full Tomcat deploy.
 */
public class TurbineBootTest {

    private static String webappRoot() {
        for (final String candidate : new String[]{"src/main/webapp", "xnat-web/src/main/webapp"}) {
            if (new File(candidate, "WEB-INF/conf/TurbineResources.properties").isFile()) {
                return new File(candidate).getAbsolutePath();
            }
        }
        throw new IllegalStateException("Could not locate xnat-web webapp root from " + new File(".").getAbsolutePath());
    }

    @Test
    public void turbineServiceContainerBoots() throws Exception {
        final String root = webappRoot();
        // The custom Velocity loader resolves templates (incl. the velocimacro library) against
        // XDATServlet.WEBAPP_ROOT, which is normally set during servlet init. Point it at the webapp.
        org.nrg.xdat.servlet.XDATServlet.WEBAPP_ROOT = root;
        final TurbineConfig config = new TurbineConfig(root, "/WEB-INF/conf/TurbineResources.properties");
        try {
            config.initialize();
            for (final String service : new String[]{
                    "AvalonComponentService", "VelocityService", "TemplateService",
                    "RunDataService", "AssemblerBrokerService", "ServletService",
                    "PullService", "SessionService"}) {
                assertNotNull("Turbine service did not initialize: " + service,
                              TurbineServices.getInstance().getService(service));
                System.out.println("[boot] OK: " + service);
            }

            // Exercise global pull-tool instantiation — service.init() alone does not surface a
            // missing/renamed tool class (Turbine logs it and continues). The $ui tool (UITool)
            // delegates to UIService, so this covers both.
            final org.apache.turbine.services.pull.PullService pull =
                    (org.apache.turbine.services.pull.PullService) TurbineServices.getInstance().getService("PullService");
            final org.apache.velocity.context.Context globalTools = pull.getGlobalContext();
            final Object ui = globalTools.get("ui");
            assertNotNull("global pull tool $ui (UITool) did not instantiate", ui);
            System.out.println("[boot] OK: pull tool $ui -> " + ui.getClass().getName());

            // Validate the request-pipeline descriptor. Turbine.configure() JAXB-unmarshals this at
            // servlet init; a missing file crashes there ("is parameter must not be null"). Confirm the
            // file exists, is well-formed, and every <valve> class loads.
            final File descriptor = new File(root, "WEB-INF/conf/turbine-classic-pipeline.xml");
            org.junit.Assert.assertTrue("pipeline descriptor missing: " + descriptor, descriptor.isFile());
            final org.w3c.dom.NodeList valves = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(descriptor).getElementsByTagName("valve");
            org.junit.Assert.assertTrue("pipeline has no <valve> entries", valves.getLength() > 0);
            for (int i = 0; i < valves.getLength(); i++) {
                Class.forName(valves.item(i).getTextContent().trim());   // throws if a valve class is missing/renamed
            }
            System.out.println("[boot] OK: pipeline (" + valves.getLength() + " valves, all classes load)");

            // Fulcrum security: Turbine 5.1's PullService.populateContext() looks up a TurbineUserManager
            // (role org.apache.fulcrum.security.UserManager) on every screen render. Confirm the
            // in-memory turbine security stack resolves through the service broker.
            final Object userManager = TurbineServices.getInstance().getService("org.apache.fulcrum.security.UserManager");
            assertNotNull("Fulcrum TurbineUserManager not registered — PullService needs it per render", userManager);
            org.junit.Assert.assertTrue("UserManager is not a TurbineUserManager: " + userManager.getClass(),
                    userManager instanceof org.apache.fulcrum.security.model.turbine.TurbineUserManager);
            System.out.println("[boot] OK: security UserManager -> " + userManager.getClass().getName());

            // Velocity 2.4.1 method introspection: MethodMap.<clinit> calls commons-lang3
            // MethodUtils.getMethodObject() (added in 3.15+). XNAT forced lang3 3.11, so every
            // $obj.method() render died with ExceptionInInitializerError. Force the static init.
            Class.forName("org.apache.velocity.util.introspection.MethodMap");
            System.out.println("[boot] OK: Velocity MethodMap init (commons-lang3 introspection)");

            // TurbineUtils.resourceExists() must resolve through the VelocityService's custom loader,
            // not the Velocity singleton — otherwise Index.vm's landing-page check always fails
            // ("Custom site login landing page cannot be found!").
            org.junit.Assert.assertTrue("resourceExists() cannot find a known template (/screens/Index.vm)",
                    org.nrg.xdat.turbine.utils.TurbineUtils.GetInstance().resourceExists("/screens/Index.vm"));
            System.out.println("[boot] OK: resourceExists(/screens/Index.vm)");

            // ExecutePageValve resolves the page module via TemplateService.getDefaultPageName();
            // for a request with no explicit target template it uses getDefaultPage(), which must be a
            // real module. With default.extension unset this was the bare "Default" -> ClassNotFoundException
            // ("Page not found: Default") on some post-login redirects.
            final org.apache.turbine.services.template.TemplateService ts =
                    (org.apache.turbine.services.template.TemplateService) TurbineServices.getInstance().getService("TemplateService");
            org.junit.Assert.assertNotEquals("TemplateService default page is the bare 'Default' (no real module)",
                    "Default", ts.getDefaultPage());
            System.out.println("[boot] OK: TemplateService default page -> " + ts.getDefaultPage());
        } finally {
            try {
                config.dispose();
            } catch (Exception ignored) {
                // best-effort teardown
            }
        }
    }
}
