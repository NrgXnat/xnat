/*
 * org.nrg.xft.utils.VelocityUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
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
