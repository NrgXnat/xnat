/*
 * core: org.nrg.xdat.preferences.PreferenceEvent
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.preferences;
import org.nrg.framework.event.StructuredEvent;
import org.nrg.framework.event.entities.EventSpecificFields;
import java.util.Set;
import java.util.HashSet;

public class PreferenceEvent extends StructuredEvent {

    public PreferenceEvent(String preferenceName, String value){
        Set<EventSpecificFields> eventSpecificFields = new HashSet<EventSpecificFields>();
        EventSpecificFields field = new EventSpecificFields(preferenceName, value);
        eventSpecificFields.add(field);
        this.setEventSpecificFields(eventSpecificFields);
    }
}