/*
 * core: org.nrg.xft.utils.VelocityUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.utils;

import org.apache.velocity.app.Velocity;

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
    
    public static void init() throws Exception
    {
        if (!INIT_COMPLETE)
        {
            Velocity.init();
            
            INIT_COMPLETE= true;
        }
    }

}
