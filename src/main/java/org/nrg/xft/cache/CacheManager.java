/*
 * core: org.nrg.xft.cache.CacheManager
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.cache;

import org.nrg.xft.event.XftItemEvent;
import org.nrg.xft.event.XftItemEventI;

import java.util.Hashtable;
import java.util.Map;

public class CacheManager {

    private Map<String, Map<Object, Object>> cache = new Hashtable<String, Map<Object, Object>>();

    private static CacheManager cm = null;

    public synchronized static CacheManager GetInstance() {
        if (cm == null) {
            CacheManager temp = new CacheManager();
            cm = temp;
        }

        return cm;
    }

    public void clearAll() {
        for (Map<Object, Object> m : cache.values()) {
            m.clear();
        }
        cache.clear();
    }

    public Object retrieve(String xsiType, Object id) {
        // Fixes issue where null ID causes NPE inside of Hashtable code.
        if (id == null) {
            return null;
        }
        Map<Object, Object> items = cache.get(xsiType);
        if (items == null) {
            return null;
        }
        return items.get(id);
    }

    public synchronized void put(String xsiType, Object id, Object i) {
        if (i == null) {
            throw new NullPointerException();
        }
        if (id == null) {
            throw new NullPointerException();
        }
        if (xsiType == null) {
            throw new NullPointerException();
        }

        Map<Object, Object> items = cache.get(xsiType);
        if (items == null) {
            items = new Hashtable<Object, Object>();
            cache.put(xsiType, items);
        }

        items.put(id, i);
    }

    public synchronized Object remove(String xsiType, Object id) {
        Map<Object, Object> items = cache.get(xsiType);
        if (items == null) {
            items = cache.put(xsiType, new Hashtable<Object, Object>());
        }

        return items.remove(id);
    }

    public void handleXftItemEvent(final XftItemEventI e) {

        if (e.getXsiType() == null) {
            return;
        }

        Map<Object, Object> items = cache.get(e.getXsiType());

        if (items == null) {
            return;//if null, then we aren't listening to this type yet.
        }

        if ((e.getAction().equals(XftItemEvent.CREATE))) {
            if (e.getId() != null && e.getItem() != null) {
                items.put(e.getId(), e.getItem());
            }
        } else if ((e.getAction().equals(XftItemEvent.UPDATE))) {
            if (e.getId() != null && e.getItem() != null) {
                items.put(e.getId(), e.getItem());
            } else if (e.getId() != null) {
                items.remove(e.getId());
            } else {
                items.clear();
            }
        } else if ((e.getAction().equals(XftItemEvent.DELETE))) {
            if (e.getId() != null) {
                items.remove(e.getId());
            } else {
                items.clear();
            }
        }
    }

}
