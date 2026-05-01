/*
 * core: org.nrg.xdat.om.XdatCriteriaSet
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.om;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.om.base.BaseXdatCriteriaSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.search.SQLClause;
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
            for (final Object cs : getChildSet()) {
                i += ((XdatCriteriaSet) cs).size();
            }
        }

        return i;
    }
    
    public static boolean compareCriteriaSets(List<XdatCriteriaSet> set1, List<XdatCriteriaSet> set2){
        return (CollectionUtils.subtract(set1, set2).size() == 0 && CollectionUtils.subtract(set2, set1).size() == 0);
    }
    
    public static boolean compareCriteria(final List<XdatCriteria> set1, final List<XdatCriteria> set2){
    	return (CollectionUtils.subtract(set1, set2).size() == 0 && CollectionUtils.subtract(set2, set1).size() == 0);
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

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof XdatCriteriaSet) {
            XdatCriteriaSet other = (XdatCriteriaSet) anObject;
            if(StringUtils.equals(this.getMethod(),other.getMethod())){
                if(compareCriteriaSets(this.getChildSet(),other.getChildSet()) && compareCriteria(this.getCriteria(),other.getCriteria())){
                    return true;
                }
            }
        }
        return false;
    }
}
