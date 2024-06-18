/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_edit_xdat_element_security
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import com.google.common.collect.Lists;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.security.helpers.FeatureDefinitionI;
import org.nrg.xdat.security.helpers.Features;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tim
 */
@SuppressWarnings("unused")
public class XDATScreen_edit_xdat_element_security extends AdminEditScreenA {
    /**
     * {@inheritDoc}
	 */
    @Override
	public String getElementName()
	{
		return "xdat:element_security";
	}

    /**
     * {@inheritDoc}
     */
    @Override
    public void finalProcessing(final RunData data, final Context context) {
        context.put("fields", getAllDefinedFieldsForSecurityElement());

        final List<List<String>> features = new ArrayList<>();
        for(final FeatureDefinitionI feature : Features.getAllFeatures()) {
        	features.add(Lists.newArrayList(feature.getKey(),feature.getName()));
        }
        context.put("features", features);
    }
}

