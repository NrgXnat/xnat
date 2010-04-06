// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * Created on Apr 6, 2006
 *
 */
package org.nrg.xft.layeredSequence;
import java.util.ArrayList;
/**
 * @author Tim
 *
 */
public interface LayeredSequenceObjectI  {
    public String getLayeredsequence();
    public void setLayeredsequence(String sequence) throws Exception;
    
    public void addLayeredChild(LayeredSequenceObjectI o);
    
    public ArrayList getLayeredChildren();
}
