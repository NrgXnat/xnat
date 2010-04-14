// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.event;

import org.nrg.xft.ItemI;

public class Event extends Object {
	public final static String CREATE="C";
	public final static String READ="R";
	public final static String UPDATE="U";
	public final static String DELETE="D";
	
	private String xsiType=null;
	private ItemI i=null;
	private String action=null;
	private Object id=null;
	
	public Event(String xsiType,String action){
		this.xsiType=xsiType;
		this.action=action;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public ItemI getItem() {
		return i;
	}
	
	public void setItem(ItemI i) {
		this.i = i;
	}
	
	public String getXsiType() {
		return xsiType;
	}
	
	public void setXsiType(String xsiType) {
		this.xsiType = xsiType;
	}

	public Object getId() {
		return id;
	}

	public void setId(Object id) {
		this.id = id;
	}
	
}
