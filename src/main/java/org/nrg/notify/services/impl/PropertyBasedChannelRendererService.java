/**
 * PropertyBasedChannelRendererService
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 29, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.services.impl;

import java.util.Map;

import org.nrg.notify.entities.ChannelRenderer;
import org.nrg.notify.services.ChannelRendererService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class PropertyBasedChannelRendererService implements ChannelRendererService {

    /**
     * @see org.nrg.notify.services.ChannelRendererService#getRenderer(java.lang.String)
     */
    @Override
    public ChannelRenderer getRenderer(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.nrg.notify.services.ChannelRendererService#setRenderers(java.util.Map)
     */
    @Autowired
    @Qualifier("renderers")
    @Override
    public void setRenderers(Map<String, String> renderers) {
        // TODO Auto-generated method stub
        
    }

    /**
     * @see org.nrg.notify.services.ChannelRendererService#getRenderers()
     */
    @Override
    public Map<String, String> getRenderers() {
        // TODO Auto-generated method stub
        return null;
    }

}
