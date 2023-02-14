/*
 * core: org.nrg.xdat.turbine.modules.screens.XDATScreen_uploadCSV
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */


package org.nrg.xdat.turbine.modules.screens;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.turbine.util.RunData;
import org.apache.velocity.context.Context;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.services.cache.UserDataCache;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.FieldMapping;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

public class XDATScreen_uploadCSV extends SecureScreen {
    @Override
    protected void doBuildTemplate(final RunData data, final Context context) throws Exception {
        context.put("elements", ElementSecurity.GetNonXDATElementNames());
        context.put("all_elements", GenericWrapperElement.GetAllElements(false));
        //If an upload was attempted earlier in the session, then those rows must be cleared off.
        data.getSession().removeAttribute("rows");
        final UserI user = XDAT.getUserDetails();
        assert user != null;

        final File   dir   = XDAT.getContextService().getBean(UserDataCache.class).getUserDataCacheFile(user, Paths.get("csv"), UserDataCache.Options.Folder);
        final File[] files = dir.listFiles();
        if (files != null) {
            context.put("fms", Lists.transform(Arrays.asList(files), new Function<File, FieldMapping>() {
                @Override
                public FieldMapping apply(final File file) {
                    return new FieldMapping(file);
                }
            }));
        }
    }
}
