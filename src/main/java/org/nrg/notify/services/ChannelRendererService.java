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

import org.nrg.framework.services.NrgService;
import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.exceptions.ChannelRendererNotFoundException;
import org.nrg.notify.renderers.ChannelRenderer;


/**
 * Provides the means for managing the various notification publication channels.
 *
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
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
