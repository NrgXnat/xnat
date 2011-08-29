/**
 * CategoryScope
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.api;

/**
 * 
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public enum CategoryScope {
    Site,
    Project,
    Subject,
    Session;
    
    public static CategoryScope Default = Site;
}
