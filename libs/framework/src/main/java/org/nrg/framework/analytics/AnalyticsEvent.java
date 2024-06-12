/*
 * framework: org.nrg.framework.analytics.AnalyticsEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.analytics;

import org.nrg.framework.logging.RemoteEvent;

import org.slf4j.event.Level;

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@XmlRootElement
public class AnalyticsEvent extends RemoteEvent implements Serializable {
    public AnalyticsEvent() {
        setLevel(Level.WARN);
        setTimestamp(new Date());
    }

    /**
     * The key for the analytics event. This can use the '.' separator to define event namespaces.
     * @return The analytics key event.
     */
    @XmlID
    public String getKey() {
        return _key;
    }

    /**
     * Sets the key for the analytics event. This can use the '.' separator to define event namespaces.
     * @param key The analytics key event to set.
     */
    public void setKey(final String key) {
        _key = key;
    }

    /**
     * The timestamp for the analytics event.
     * @return The timestamp for the analytics event.
     */
    @SuppressWarnings("unused")
    public Date getTimestamp() {
        return _timestamp;
    }

    /**
     * Sets the timestamp for the analytics event.
     * @param timestamp The timestamp to set for the analytics event.
     */
    public void setTimestamp(final Date timestamp) {
        _timestamp = timestamp;
    }

    /**
     * Gets the level for the analytics logging. This defaults to {@link Level#WARN}.
     * @return The current analytics logging level.
     */
    public Level getLevel() {
        return _level;
    }

    /**
     * Sets the level for the analytics logging. This defaults to {@link Level#WARN}.
     * @param level The analytics logging level to be set.
     */
    public void setLevel(Level level) {
        _level = level;
    }

    /**
     * Gets the ad hoc property map for the analytics event. Each specific type of analytic event can define its own
     * type that extends the <b>Map&lt;String, String&gt;</b> type to plug into the generic event properties structure.
     * @return The properties map for the analytics event.
     */
    public Map<String, String> getProperties() {
        return this;
    }

    /**
     * Clears the values of all cached string properties and sets the values contained in the submitted properties map.
     * If the submitted map is null or empty, the currently cached properties are cleared, but no new properties are
     * set.
     * @param properties    The properties to set for the event object.
     */
    public void setProperties(final Map<String, String> properties) {
        clear();
        if (properties != null) {
            putAll(properties);
        }
    }

    private String _key;
    private Date _timestamp;
    private Level _level;
}
