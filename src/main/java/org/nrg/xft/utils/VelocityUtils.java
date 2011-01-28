// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * Created on Apr 14, 2006
 *
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
