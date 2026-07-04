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
    public void turbineServiceContainerBoots() {
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
        } finally {
            try {
                config.dispose();
            } catch (Exception ignored) {
                // best-effort teardown
            }
        }
    }
}
