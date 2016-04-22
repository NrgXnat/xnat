// Copyright 2010 Washington University School of Medicine All Rights Reserved
package org.nrg.xft.event;

/**
 * The Class XftItemEvent.
 */
public class XftItemEvent implements EventI {
	
	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 199850020591951620L;
	
	/** The Constant CREATE. */
	public final static String CREATE="C";
	
	/** The Constant READ. */
	public final static String READ="R";
	
	/** The Constant UPDATE. */
	public final static String UPDATE="U";
	
	/** The Constant DELETE. */
	public final static String DELETE="D";
	
	/** The xsi type. */
	private String xsiType=null;
	
	/** The i. */
	private Object i=null;
	
	/** The action. */
	private String action=null;
	
	/** The id. */
	private Object id=null;
	
	/**
	 * Instantiates a new xft item event.
	 *
	 * @param xsiType the xsi type
	 * @param action the action
	 */
	public XftItemEvent(String xsiType,String action){
		this.xsiType=xsiType;
		this.action=action;
	}
	
	/**
	 * Instantiates a new xft item event.
	 *
	 * @param xsiType the xsi type
	 * @param id the id
	 * @param action the action
	 */
	public XftItemEvent(String xsiType,Object id,String action){
		this.xsiType=xsiType;
		this.id=id;
		this.action=action;
	}
	
	/**
	 * Gets the action.
	 *
	 * @return the action
	 */
	public String getAction() {
		return action;
	}
	
	/**
	 * Sets the action.
	 *
	 * @param action the new action
	 */
	public void setAction(String action) {
		this.action = action;
	}
	
	/**
	 * Gets the item.
	 *
	 * @return the item
	 */
	public Object getItem() {
		return i;
	}
	
	/**
	 * Sets the item.
	 *
	 * @param i the new item
	 */
	public void setItem(Object i) {
		this.i = i;
	}
	
	/**
	 * Gets the xsi type.
	 *
	 * @return the xsi type
	 */
	public String getXsiType() {
		return xsiType;
	}
	
	/**
	 * Sets the xsi type.
	 *
	 * @param xsiType the new xsi type
	 */
	public void setXsiType(String xsiType) {
		this.xsiType = xsiType;
	}

	/**
	 * Gets the id.
	 *
	 * @return the id
	 */
	public Object getId() {
		return id;
	}

	/**
	 * Sets the id.
	 *
	 * @param id the new id
	 */
	public void setId(Object id) {
		this.id = id;
	}
	
}
