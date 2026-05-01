/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.XnatClassMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm.xnat;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.bean.ClassMappingFactory;
import org.nrg.xdat.bean.base.BaseElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
public final class XnatClassMapping<T extends BaseElement> implements Function<String, Class<? extends T>> {
    private static final Logger              _log           = LoggerFactory.getLogger(XnatClassMapping.class);
    private static final String              XNAT_NS_PREFIX = "http://nrg.wustl.edu/xnat:";
    private static final XnatClassMapping<?> instance       = new XnatClassMapping<>();

    @SuppressWarnings("unchecked")
    public static <T extends BaseElement> XnatClassMapping<T> forBaseClass(final Class<T> clazz) {
        return (XnatClassMapping<T>) instance;
    }

    private final Map<String, Class<? extends T>> beanClassCache = Maps.newHashMap();

    private XnatClassMapping() {
    }

    /*
     * (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    public Class<? extends T> apply(final String typeName) {
        try {
            return getBeanClass(typeName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new bean object for the provided XNAT data type
     *
     * @param type XNAT data type name (without namespace prefix: e.g., mrScanData)
     * @return newly allocated bean object
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public T create(final String type)
            throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        final Class<? extends T> beanClass = getBeanClass(type);
        assert beanClass != null;
        return beanClass.newInstance();
    }

    /**
     * Gets the bean class corresponding to the provided XNAT data type name
     *
     * @param type XNAT data type name (without namespace prefix: e.g., mrScanData)
     * @return bean class
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    public Class<? extends T> getBeanClass(final String type)
            throws ClassNotFoundException {
        final String fqtn = XNAT_NS_PREFIX + type;
        synchronized (beanClassCache) {
            if (beanClassCache.containsKey(type)) {
                return beanClassCache.get(type);
            } else {
                try {
                    final String             className = ClassMappingFactory.getInstance().getElements().get(fqtn);
                    if (StringUtils.isBlank(className)) {
                        throw new ClassNotFoundException("No corresponding class found for schema element: " + fqtn);
                    }
                    final Class<? extends T> clazz     = (Class<? extends T>) XnatClassMapping.class.getClassLoader().loadClass(className);
                    beanClassCache.put(type, clazz);
                    return clazz;
                } catch (InstantiationException | IllegalAccessException e) {
                    _log.error("An error occurred trying to find the class associated with the FQTN " + fqtn, e);
                    return null;
                }
            }
        }
    }
}
