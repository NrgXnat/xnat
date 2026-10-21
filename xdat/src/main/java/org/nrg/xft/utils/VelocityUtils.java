/*
 * core: org.nrg.xft.utils.VelocityUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;

import org.apache.turbine.services.TurbineServices;
import org.apache.turbine.services.velocity.VelocityService;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

/**
 * @author Tim
 *
 */
public class VelocityUtils {
    public static boolean INIT_COMPLETE = false;
    /**
     *
     */
    public VelocityUtils() {
        super();
    }

    /**
     * Render a NAMED template through Turbine's VelocityService — the VelocityEngine configured with
     * XNAT's CustomClasspathResourceLoader. Use this instead of the org.apache.velocity.app.Velocity
     * static singleton, which in Velocity 2 / Turbine 5.1 is a separate, unconfigured engine that
     * cannot find XNAT's templates.
     */
    public static String render(final Context context, final String templateName) {
        try {
            return ((VelocityService) TurbineServices.getInstance().getService(VelocityService.SERVICE_NAME))
                    .handleRequest(context, templateName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render Velocity template: " + templateName, e);
        }
    }
    
    public static void init() throws Exception
    {
        if (!INIT_COMPLETE)
        {
            Velocity.init();
            
            INIT_COMPLETE= true;
        }
    }

}
