package org.nrg.xnat.eventservice.sort;

import org.nrg.xnat.eventservice.model.SimpleEvent;

import java.util.Comparator;

public class SimpleEventComparator implements Comparator<SimpleEvent> {

    @Override
    // This makes sure the scheduled events show up at the bottom of the list in the json object
    public int compare(SimpleEvent o1, SimpleEvent o2) {
        if(o1.displayName().contains("Scheduled Event") && o2.displayName().contains("Scheduled Event")){
            return o1.displayName().compareTo(o2.displayName());
        }else if(o1.displayName().contains("Scheduled Event")){
            return 1;
        }else if(o2.displayName().contains("Scheduled Event")){
            return -1;
        }else{
            return o1.displayName().compareTo(o2.displayName());
        }
    }
}
