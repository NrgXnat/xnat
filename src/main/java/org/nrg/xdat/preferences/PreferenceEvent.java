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