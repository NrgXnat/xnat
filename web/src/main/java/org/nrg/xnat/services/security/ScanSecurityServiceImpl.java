package org.nrg.xnat.services.security;

import org.nrg.xdat.base.BaseElement;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatImagesessiondata;
import org.nrg.xdat.security.services.ScanSecurityService;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.XftItemException;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

@Service
public class ScanSecurityServiceImpl implements ScanSecurityService {
    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public ItemI determineSession(final ItemI item) throws XftItemException {
        final XnatImagescandata scanItem;
        if (item instanceof XFTItem && item.getItem().instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME)) {
            scanItem = (XnatImagescandata) BaseElement.GetGeneratedItem(item);
        } else if (item instanceof XnatImagescandata) {
            scanItem = (XnatImagescandata) item;
        } else {
            // Not a scan
            return null;
        }

        // If we have an image session for the scan, return it
        ItemI session = scanItem.getImageSessionId() != null ? scanItem.getImageSessionData() : null;
        if (session != null) {
            return session;
        }

        // If we're creating a new session, we cannot look it up by id. We should have the session in the XFT parent field,
        // but it will be a generic typed instance
        session = scanItem.getParent();
        if (session == null) {
            throw new XftItemException("Cannot determine parent session to check scan permissions on " + scanItem);
        }
        // Attempt to retrieve the "extender" of the generic type
        session = session.getItem().getExtenderItem();
        if (isGenericType(session)) {
            throw new XftItemException("Cannot determine parent session data type to check scan permissions on " +
                    scanItem);
        }
        return session;
    }

    /**
     * Check if session is generic type (vs an extender of generic type, e.g., xnat:mrSessionData)
     *
     * Why do we care? Bc generic types don't have element security and thus permissions checks on them always return true
     *
     * @param session the session item
     * @return T/F
     */
    private boolean isGenericType(ItemI session) {
        return XnatImagesessiondata.SCHEMA_ELEMENT_NAME.equals(session.getXSIType());
    }
}
