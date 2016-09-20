/*
 * core: org.nrg.xft.sequence.SequentialObject
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.sequence;


/**
 * @author Tim
 *
 */
public interface SequentialObject {
    public int getSequence();
    public void setSequence(int sequence);
}
