/*
 * org.nrg.xdat.om.base.BaseXnatFielddefinitiongroup
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xdat.om.base;

import org.nrg.xdat.om.base.auto.AutoXnatFielddefinitiongroup;
import org.nrg.xft.ItemI;
import org.nrg.xft.security.UserI;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author XDAT
 *
 */
@SuppressWarnings({"unchecked","rawtypes"})
public abstract class BaseXnatFielddefinitiongroup extends AutoXnatFielddefinitiongroup {

	public BaseXnatFielddefinitiongroup(ItemI item)
	{
		super(item);
	}

	public BaseXnatFielddefinitiongroup(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXnatFielddefinitiongroup(UserI user)
	 **/
	public BaseXnatFielddefinitiongroup()
	{}

	public BaseXnatFielddefinitiongroup(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

	 private ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> _sorted_Fields_field =null;

	/**
	 * fields/field
	 * @return Returns an ArrayList of org.nrg.xdat.om.XnatFielddefinitiongroupField
	 */
	public ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField> getFields_field() {
		try{
			if (_sorted_Fields_field==null){
				_sorted_Fields_field=org.nrg.xdat.base.BaseElement.WrapItems(getChildItemCollection("fields/field").getItems("xnat:fieldDefinitionGroup_field/sequence"));
				return _sorted_Fields_field;
			}else {
				return _sorted_Fields_field;
			}
		} catch (Exception e1) {return new ArrayList<org.nrg.xdat.om.XnatFielddefinitiongroupField>();}
	}
}

