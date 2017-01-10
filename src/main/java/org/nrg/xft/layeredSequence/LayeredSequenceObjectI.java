/*
 * core: org.nrg.xft.layeredSequence.LayeredSequenceObjectI
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.layeredSequence;
import java.util.ArrayList;

import org.nrg.xft.ItemI;
/**
 * @author Tim
 *
 */
public interface LayeredSequenceObjectI extends ItemI  {
    public String getLayeredsequence();
    public void setLayeredsequence(String sequence) throws Exception;
    
    public void addLayeredChild(LayeredSequenceObjectI o);
    
    public ArrayList getLayeredChildren();
}
