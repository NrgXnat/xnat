/*
 * core: org.nrg.xft.collections.XftFieldExclusionList
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.collections;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.nrg.xft.entities.XftFieldExclusion;

@XmlRootElement(name = "exclusions")
public class XftFieldExclusionList extends ArrayList<XftFieldExclusion> {
    public XftFieldExclusionList() {
    }

    public XftFieldExclusionList(List<XftFieldExclusion> exclusions) {
        addAll(exclusions);
    }

    @XmlElement(name = "exclusion")
    public List<XftFieldExclusion> getExclusions() {
        return this;
    }

    public void setExclusions(List<XftFieldExclusion> exclusions) {
        clear();
        addAll(exclusions);
    }

    private static final long serialVersionUID = -6654493660928944984L;
}
