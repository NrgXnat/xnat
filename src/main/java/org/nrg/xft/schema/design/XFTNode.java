/*
 * core: org.nrg.xft.schema.design.XFTNode
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xft.schema.design;

import org.nrg.xft.schema.XFTElement;

public abstract class XFTNode {
	private XFTNode parent= null;

	/**
	 * Checks if this item has a Parent
	 * @return Returns whether the item has a parent
	 */
	public boolean hasParent() {
		if (getParent() == null)
		{
			return false;
		}else
		{
			return true;
		}
	}
	/**
	 * Gets the XDATNode referenced as the parent.
	 * @return Returns the parent XDATNode
	 */
	public XFTNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent for this node.
	 * @param element
	 */
	public void setParent(XFTNode element) {
		parent = element;
	}

	/**
	 * Gets the highest parent in this items chain of parent items.
	 * @return Returns the highest parent in this items chain of parent items
	 */
	public XFTElement getAbsoluteParent()
	{
		if (parent == null)
		{
			return (XFTElement)this;
		}else
		{
			return parent.getAbsoluteParent();
		}
	}
	
	/**
	 * Returns the next parent Element in this items chain of parent items (rather than any field item)
	 * @return Returns the next parent Element in this items chain of parent items
	 */
	public XFTElement getParentElement() throws ClassCastException
	{
		if (parent == null)
		{
			return (XFTElement)this;
		}else if (parent.getClass().getName().equalsIgnoreCase("org.nrg.xft.schema.XFTElement"))
		{
			return (XFTElement)parent;
		}else
		{
			return parent.getParentElement();
		}
	}
}

