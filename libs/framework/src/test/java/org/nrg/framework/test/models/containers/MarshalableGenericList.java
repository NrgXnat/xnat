/*
 * framework: org.nrg.framework.test.models.containers.MarshalableGenericList
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

@XmlRootElement(name = "list")
public class MarshalableGenericList<T> extends ArrayList<T> {

    public MarshalableGenericList() {
    }

    public MarshalableGenericList(List<T> items) {
        addAll(items);
    }
    
    @XmlElement(name = "item")
    public List<T> getItems() {
        return this;
    }
    
    public void setItems(List<T> items) {
        clear();
        addAll(items);
    }

    private static final long serialVersionUID = 3301820223510865398L;
}
