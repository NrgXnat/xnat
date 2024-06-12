/*
 * framework: org.nrg.framework.test.models.containers.MarshalableList
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.test.models.containers;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.nrg.framework.test.models.entities.MarshalableThingy;

@XmlRootElement(name = "thingies")
public class MarshalableList extends ArrayList<MarshalableThingy> {
    public MarshalableList() {
    }

    public MarshalableList(List<MarshalableThingy> thingies) {
        addAll(thingies);
    }
    
    @XmlElement(name = "thingy")
    public List<MarshalableThingy> getThingies() {
        return this;
    }
    
    public void setThingies(List<MarshalableThingy> thingies) {
        clear();
        addAll(thingies);
    }

    private static final long serialVersionUID = 3301820223510865398L;
}
