/*
 * core: org.nrg.xdat.preferences.PreferenceEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;

import com.google.common.collect.Sets;
import org.nrg.framework.event.StructuredEvent;
import org.nrg.framework.event.entities.EventSpecificFields;

public class PreferenceEvent extends StructuredEvent {
    public PreferenceEvent(final String preferenceName, final String value) {
        setEventSpecificFields(Sets.newHashSet(new EventSpecificFields(preferenceName, value)));
    }
}
