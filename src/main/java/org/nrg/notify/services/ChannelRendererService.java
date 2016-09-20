/*
 * org.nrg.notify.services.ChannelRendererService
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.notify.services;

import org.nrg.framework.services.NrgService;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.exceptions.ChannelRendererNotFoundException;
import org.nrg.notify.renderers.ChannelRenderer;


public interface ChannelRendererService extends NrgService {
    public static String SERVICE_NAME = "ChannelRendererService";
    
    /**
     * Gets the renderer identified by the <b>name</b> parameter.
     * @param name The name of the renderer to retrieve from the registry.
     * @return The specified {@link ChannelRenderer renderer}.
     * @throws ChannelRendererNotFoundException Thrown when the specified renderer is not found in the registry.
     */
    abstract public ChannelRenderer getRenderer(String name) throws ChannelRendererNotFoundException;
    
    /**
     * Sets the {@link ChannelRendererProvider provider} for the channel renderer registry.
     * @param provider The provider for the channel renderer registry.
     */
    abstract public void setRenderers(ChannelRendererProvider provider);
}
