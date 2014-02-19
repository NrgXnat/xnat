/*
 * org.nrg.xdat.turbine.modules.screens.XDATScreen_dataTypes
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/1/13 9:13 AM
 */


package org.nrg.xdat.turbine.modules.screens;

import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.XDATUser;
import org.nrg.xdat.turbine.utils.TurbineUtils;
import org.nrg.xft.collections.ItemCollection;
import org.nrg.xft.search.CriteriaCollection;
import org.nrg.xft.search.ItemSearch;

/**
 * @author Tim
 *
 */
public class XDATScreen_dataTypes extends AdminScreen {
	public void doBuildTemplate(RunData data, Context context)
	{
		//TurbineUtils.OutputPassedParameters(data,context,this.getClass().getName());
		XDATUser user = TurbineUtils.getUser(data);

        try {
            CriteriaCollection cc = new CriteriaCollection("AND");
            cc.addClause("xdat:element_security/element_name", " NOT LIKE ", "xdat:%");

            ItemSearch is = ItemSearch.GetItemSearch("xdat:element_security", user);
            is.add(cc);
            

            ItemCollection col=is.exec(false);

            context.put("data_types",col.getItems("xdat:element_security/sequence"));


		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
