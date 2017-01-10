/*
 * notify: org.nrg.notify.entities.ChannelRendererProvider
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.notify.entities;

import java.util.Map;

import javax.inject.Provider;

import org.nrg.notify.renderers.ChannelRenderer;

public class ChannelRendererProvider implements Provider<Map<String, ChannelRenderer>> {

    /**
     * Gets the map of registered channel renderers.
     * @see Provider#get()
     */
    @Override
    public Map<String, ChannelRenderer> get() {
        return _renderers;
    }

    /**
     * Sets the map of available channel renderers.
     * @param renderers    The renderers to set for the channel.
     */
    public void setRenderers(Map<String, ChannelRenderer> renderers) {
        _renderers = renderers;
    }

    private Map<String, ChannelRenderer> _renderers;
}
