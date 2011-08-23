/**
 * CategoryScope
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 19, 2011
 */
package org.nrg.notify.api;

/**
 * 
 *
 * @author rherrick
 */
public enum CategoryScope {
    Site,
    Project,
    Subject,
    Session;
    
    public static CategoryScope Default = Site;
}
