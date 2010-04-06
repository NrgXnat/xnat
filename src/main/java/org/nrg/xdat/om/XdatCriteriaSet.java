// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import org.nrg.xft.*;
import org.nrg.xdat.om.base.*;
import org.nrg.xdat.search.DisplayCriteria;
import org.nrg.xdat.search.ElementCriteria;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.search.SearchCriteria;
import org.nrg.xft.security.UserI;

import java.util.*;

/**
 * @author XDAT
 *
 */
@SuppressWarnings("serial")

public class XdatCriteriaSet extends BaseXdatCriteriaSet {

	public XdatCriteriaSet(ItemI item)
	{
		super(item);
	}

	public XdatCriteriaSet(UserI user)
	{
		super(user);
	}

	/*
	 * @deprecated Use BaseXdatCriteriaSet(UserI user)
	 **/
	public XdatCriteriaSet()
	{}

	public XdatCriteriaSet(Hashtable properties, UserI user)
	{
		super(properties,user);
	}

    public int size(){
        int i = this.getCriteria().size();

        if (this.getChildSet().size()>0){
            Iterator iter = getChildSet().iterator();
            while(iter.hasNext()){
                XdatCriteriaSet cs = (XdatCriteriaSet)iter.next();
                i += cs.size();
            }
        }

        return i;
    }

    public void populateCriteria(org.nrg.xft.search.CriteriaCollection cc) throws Exception{
        this.setMethod(cc.getJoinType());
        Iterator iter = cc.iterator();
        while (iter.hasNext())
        {
            SQLClause c = (SQLClause)iter.next();
            if (c instanceof org.nrg.xft.search.CriteriaCollection)
            {
                XdatCriteriaSet set = new XdatCriteriaSet();
                set.populateCriteria((org.nrg.xft.search.CriteriaCollection)c);

                if (set.size()> 0)
                {
                    this.setChildSet(set);
                }
            }else{
                XdatCriteria criteria = new XdatCriteria();
                criteria.populateCriteria(c);

                this.setCriteria(criteria);
            }
        }
    }
}

