// Copyright 2010 Washington University School of Medicine All Rights Reserved
/*
 * GENERATED FILE
 * Created on Mon Feb 26 14:11:51 CST 2007
 *
 */
package org.nrg.xdat.om;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.om.base.BaseXdatCriteriaSet;
import org.nrg.xft.ItemI;
import org.nrg.xft.search.SQLClause;
import org.nrg.xft.security.UserI;

import com.google.common.collect.Lists;

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
    
    public static boolean compareCriteriaSets(List<XdatCriteriaSet> set1, List<XdatCriteriaSet> set2){
    	final List<XdatCriteriaSet> copy1=new ArrayList<XdatCriteriaSet>(set1);
    	final List<XdatCriteriaSet> copy2=new ArrayList<XdatCriteriaSet>(set2);
    	
    	for(int i=0;i<set1.size();i++){
    		XdatCriteriaSet set=copy1.get(i);
    		for(XdatCriteriaSet second: copy2){
    			if(equalByFields(set,second)){
    				copy2.remove(second);
    	    		copy1.remove(i);
    				break;
    			}
    		}
    	}
    	
    	//all matches should have been removed from the copies
    	return !(copy1.size()>0 || copy2.size()>0);
    }
    
    public static boolean compareCriteria(final List<XdatCriteria> set1, final List<XdatCriteria> set2){
    	final List<XdatCriteria> copy1=new ArrayList<XdatCriteria>(set1);
    	final List<XdatCriteria> copy2=new ArrayList<XdatCriteria>(set2);
    	
    	Collections.reverse(copy1);
    	
    	for(int i=0;i<set1.size();i++){
    		XdatCriteria crit1=copy1.get(i);
    		for(XdatCriteria second: copy2){
    			if(equalByFields(crit1,second)){
    				copy2.remove(second);
    	    		copy1.remove(i);
    				break;
    			}
    		}
    	}
    	
    	//all matches should have been removed from the copies
    	return (copy1.size()>0 || copy2.size()>0);
    }
    
    public static boolean equalByFields(XdatCriteriaSet set1,XdatCriteriaSet set2){
    	if(StringUtils.equals(set1.getMethod(),set2.getMethod())){
    		if(compareCriteriaSets(set1.getChildSet(),set2.getChildSet()) && compareCriteria(set1.getCriteria(),set2.getCriteria())){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static boolean equalByFields(XdatCriteria crit1,XdatCriteria crit2){
    	if(!StringUtils.equals(crit1.getComparisonType(), crit1.getComparisonType()))return false;
    	if(!StringUtils.equals(crit1.getCustomSearch(), crit1.getCustomSearch()))return false;
    	if(!StringUtils.equals(crit1.getSchemaElementName(), crit1.getSchemaElementName()))return false;
    	if(!StringUtils.equals(crit1.getSchemaField(), crit1.getSchemaField()))return false;
    	if(!StringUtils.equals(crit1.getValue(), crit1.getValue()))return false;
    	
    	return true;
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

