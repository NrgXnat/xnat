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

import javax.inject.Inject;

import org.nrg.notify.entities.ChannelRendererProvider;
import org.nrg.notify.exceptions.ChannelRendererNotFoundException;
import org.nrg.notify.exceptions.InvalidChannelRendererException;
import org.nrg.notify.exceptions.UnknownChannelRendererException;
import org.nrg.notify.renderers.ChannelRenderer;
import org.nrg.notify.services.ChannelRendererService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * 
 * @author Rick Herrick <rick.herrick@wustl.edu>
 */
@Service
public class PropertyBasedChannelRendererService implements ChannelRendererService, ApplicationContextAware {

    /**
     * @throws ChannelRendererNotFoundException 
     * @throws InvalidChannelRendererException 
     * @throws UnknownChannelRendererException 
     * @see org.nrg.notify.services.ChannelRendererService#getRenderer(java.lang.String)
     */
    @Override
    public ChannelRenderer getRenderer(String name) throws ChannelRendererNotFoundException, InvalidChannelRendererException, UnknownChannelRendererException {
        assert _renderers != null;
        
        if (!_renderers.containsKey(name)) {
            return null;
        }

        if (_context.containsBean(name)) {
            return _context.getBean(name, ChannelRenderer.class);
        }

        // TODO: It may make sense to cache these class instances.
        String clazzName = _renderers.get(name);
        Class<?> clazz;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException exception) {
            throw new ChannelRendererNotFoundException("Could not find specified class for renderer " + name + ": " + clazzName);
        }
        
        if (!clazz.isAssignableFrom(ChannelRenderer.class)) {
            throw new InvalidChannelRendererException("The specified class for renderer " + name + " is not a valid channel renderer implementation: " + clazzName);
        }
        
        try {
            return (ChannelRenderer) clazz.newInstance();
        } catch (InstantiationException exception) {
            throw new UnknownChannelRendererException("An exception occurred for renderer " + name + " and class: " + clazzName, exception);
        } catch (IllegalAccessException exception) {
            throw new UnknownChannelRendererException("An exception occurred for renderer " + name + " and class: " + clazzName, exception);
        }
    }

    /**
     * @see ChannelRendererService#setRenderers(Map)
     */
    @Inject
    @Override
    public void setRenderers(ChannelRendererProvider renderers) {
        _renderers = renderers.get();
    }

    /**
     * @see ChannelRendererService#getRenderers()
     */
    @Override
    public Map<String, String> getRenderers() {
        return _renderers;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        _context = context;
    }

    private ApplicationContext _context;
    private Map<String, String> _renderers;
}
