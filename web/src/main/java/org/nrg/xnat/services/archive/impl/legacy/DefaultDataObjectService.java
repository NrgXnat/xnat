/*
 * web: org.nrg.xnat.services.archive.impl.legacy.DefaultDataObjectService
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services.archive.impl.legacy;

import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.services.archive.DataObjectService;
import org.springframework.stereotype.Service;

/**
 * Implements the default data object service for XFT legacy data objects.
 */
@Service
public class DefaultDataObjectService implements DataObjectService {
    /**
     * {@inheritDoc}
     */
    @Override
    public ItemI createDataObject(final String xml, final UserI user) throws Exception {
        final XFTItem item = XFTItem.NewItem(xml, user);
        SaveItemHelper.authorizedSave(item, user, false, false, false, false, EventUtils.ADMIN_EVENT(user));
        return item;
    }
}
