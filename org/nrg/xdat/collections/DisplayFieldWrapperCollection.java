//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
/*
 * Created on May 5, 2005
 *
 */
package org.nrg.xdat.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.DisplayFieldReferenceI;
import org.nrg.xdat.search.DisplayFieldWrapper;
import org.nrg.xft.collections.XFTCollection;
import org.nrg.xft.exception.DuplicateKeyException;
import org.nrg.xft.exception.ItemNotFoundException;
import org.nrg.xft.sequence.SequenceComparator;

/**
 * @author Tim
 *
 */
public class DisplayFieldWrapperCollection extends XFTCollection {

	public DisplayFieldWrapperCollection()
	{
		super();
		setAllowReplacement(true);
	}

	public DisplayFieldWrapper getDisplayField(String id)
	{
			return (DisplayFieldWrapper)this.getStoredItem(id);
	}

	public void addDisplayField(DisplayFieldWrapper df)
	{
	    	df.setSequence(this.getItemHash().size());
			this.addStoredItem(df);
	}

    public void addDisplayField(DisplayField df, String header, Object value)
    {
            DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
            if(header!=null)
            	dfw.setHeader(header);
            dfw.setValue(value);
            addDisplayField(dfw);
    }

    public void addDisplayField(DisplayField df, String header, Object value,Boolean visible)
    {
            DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
            if(header!=null)
            	dfw.setHeader(header);
            dfw.setValue(value);
            dfw.setVisible(visible);
            addDisplayField(dfw);
    }

	public void addDisplayField(DisplayField df)
	{
	    	DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
            dfw.setHeader(df.getHeader());
	    	addDisplayField(dfw);
	}

    public void addDisplayField(DisplayField df,Object value)
    {
            DisplayFieldWrapper dfw = new DisplayFieldWrapper(df);
            dfw.setHeader(df.getHeader());
            dfw.setValue(value);
            addDisplayField(dfw);
    }

	public void addDisplayFields(Collection coll)
	{
	    Iterator iter = coll.iterator();
	    while (iter.hasNext())
	    {
	    	Object o = iter.next();
	    	if (o instanceof DisplayFieldWrapper)
	    	{
	    	    addDisplayField((DisplayFieldWrapper)o);
	    	}else if (o instanceof DisplayField)
	    	{
	    	    addDisplayField((DisplayField)o);
	    	}
	    }
	}
	/**
	 * @param id
	 * @return
	 */
	public DisplayFieldWrapper getDisplayFieldWException(String id)
	{
	    try{
	        return (DisplayFieldWrapper)this.getStoredItemWException(id);
	    } catch (ItemNotFoundException e1) {
            return null;
        }
	}

	public void addDisplayFieldWException(DisplayFieldWrapper df)
	{
		try {
            df.setSequence(this.getItemHash().size());
            this.addStoredItemWException(df);
        } catch (DuplicateKeyException e) {
        }
	}

	public ArrayList getSortedFields()
	{
	    ArrayList al = new ArrayList();
	    al.addAll(this.getItemHash().values());
	    Collections.sort(al,SequenceComparator.SequenceComparator);
	    return al;
	}

	public ArrayList<DisplayFieldReferenceI> getSortedVisibleFields()
	{
	    ArrayList<DisplayFieldReferenceI> al = new ArrayList<DisplayFieldReferenceI>();
	    Iterator iter = this.getDisplayFieldIterator();
	    while (iter.hasNext())
	    {
	        DisplayFieldWrapper dfw = (DisplayFieldWrapper)iter.next();
	        if(dfw.getDf().isVisible()){
	            al.add(dfw);
	        }
	    }
	    Collections.sort(al,SequenceComparator.SequenceComparator);
	    return al;
	}

	public Iterator getDisplayFieldIterator()
	{
		return this.getItemIterator();
	}

	public java.util.Hashtable getDisplayFieldHash()
	{
		return this.getItemHash();
	}

}
