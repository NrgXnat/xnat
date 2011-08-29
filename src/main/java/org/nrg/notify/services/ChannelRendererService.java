/**
 * ChannelRendererService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services;

import java.util.Map;

import org.nrg.framework.services.NrgService;
import org.nrg.notify.entities.ChannelRenderer;


/**
 * Provides the means for managing the various notification publication channels.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public interface ChannelRendererService extends NrgService {
    public static String SERVICE_NAME = "ChannelRendererService";
    
    abstract public ChannelRenderer getRenderer(String name);
    
    abstract public void setRenderers(Map<String, String> renderers);

    abstract public Map<String, String> getRenderers();
}
