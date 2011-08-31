/**
 * ChannelRendererProvider
 * (C) 2011 Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD License
 *
 * Created on Aug 30, 2011 by Rick Herrick <rick.herrick@wustl.edu>
 */
package org.nrg.notify.entities;

import java.util.Map;

import javax.inject.Provider;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
public class ChannelRendererProvider implements Provider<Map<String, String>> {

    /**
     * @see javax.inject.Provider#get()
     */
    @Override
    public Map<String, String> get() {
        return _renderers;
    }

    public void setRenderers(Map<String, String> renderers) {
        _renderers = renderers;
    }

    private Map<String, String> _renderers;
}
