/*
 * org.nrg.notify.services.impl.PropertyBasedChannelRendererService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services.impl;

import java.util.Map;

import javax.inject.Inject;

import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.exceptions.ChannelRendererNotFoundException;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.services.ChannelRendererService;
import org.springframework.stereotype.Service;

@Service
public class PropertyBasedChannelRendererService implements ChannelRendererService {

    /**
     * Gets the renderer identified by the <b>name</b> parameter.
     * @param name The name of the renderer to retrieve from the registry.
     * @return The specified {@link ChannelRenderer renderer}.
     * @throws ChannelRendererNotFoundException Thrown when the specified renderer is not found in the registry.
     * @see ChannelRendererService#getRenderer(String)
     */
    @Override
    public ChannelRenderer getRenderer(String name) throws ChannelRendererNotFoundException {
        assert _renderers != null;
        
        if (!_renderers.containsKey(name)) {
            throw new ChannelRendererNotFoundException("Could not find specified renderer: " + name);
        }
        
        return _renderers.get(name);
    }

    /**
     * Sets the {@link ChannelRendererProvider provider} for the channel renderer registry.
     * @param provider    The provider for the channel renderer registry.
     * @see ChannelRendererService#setRenderers(ChannelRendererProvider)
     */
    @Inject
    @Override
    public void setRenderers(ChannelRendererProvider provider) {
        _renderers = provider.get();
    }

    private Map<String, ChannelRenderer> _renderers;
}
