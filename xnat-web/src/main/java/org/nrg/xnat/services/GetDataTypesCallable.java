/*
 * web: org.nrg.xnat.services.GetDataTypesCallable
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2025, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.services;

import org.nrg.xdat.XDAT;
import org.nrg.xdat.security.ElementSecurity;
import org.nrg.xdat.security.helpers.UserHelper;
import org.nrg.xft.security.UserI;

import java.util.*;
import java.util.concurrent.Callable;

/**
 * Callable for retrieving and filtering XNAT data types.
 * This encapsulates the logic for fetching ElementSecurity entries with optional filtering.
 */
public class GetDataTypesCallable implements Callable<List<DataTypeInfo>> {

    private final UserI user;
    private final boolean secured;
    private final boolean used;
    private final boolean readable;

    /**
     * Constructor for GetDataTypesCallable
     *
     * @param user     The user making the request
     * @param secured  Filter to only secured data types (optional)
     * @param used     Filter to only data types with existing data (optional)
     * @param readable Use readable counts instead of total counts (optional)
     */
    public GetDataTypesCallable(UserI user, boolean secured, boolean used, boolean readable) {
        this.user = user;
        this.secured = secured;
        this.used = used;
        this.readable = readable;
    }

    @Override
    public List<DataTypeInfo> call() throws Exception {
        final Map<String, ElementSecurity> allES = new HashMap<>(ElementSecurity.GetElementSecurities());
        final Set<String> removals = new HashSet<>();

        // Remove security elements (those starting with "xdat:")
        for (ElementSecurity es : allES.values()) {
            final String elementName = es.getElementName();
            if (elementName.startsWith("xdat:")) {
                removals.add(es.getElementName());
            }
        }

        for (final String elementName : removals) {
            allES.remove(elementName);
        }
        removals.clear();

        // Filter by secured if requested
        if (secured) {
            for (ElementSecurity es : allES.values()) {
                if (!es.isSecure()) {
                    removals.add(es.getElementName());
                }
            }
        }

        for (final String elementName : removals) {
            allES.remove(elementName);
        }
        removals.clear();

        // Get counts (either readable or total)
        final Map<String, Long> counts = readable
                ? UserHelper.getUserHelperService(user).getReadableCounts()
                : XDAT.getTotalCounts();

        // Filter by used if requested
        if (used) {
            for (ElementSecurity es : allES.values()) {
                if (!counts.containsKey(es.getElementName())) {
                    removals.add(es.getElementName());
                }
            }
        }

        for (final String elementName : removals) {
            allES.remove(elementName);
        }
        removals.clear();

        // Build result list
        List<DataTypeInfo> dataTypes = new ArrayList<>();
        for (ElementSecurity es : allES.values()) {
            String elementName = es.getElementName();
            String singular = es.getSingularDescription();
            if (singular == null) {
                singular = elementName;
            }

            String plural = es.getPluralDescription();
            if (plural == null) {
                plural = elementName;
            }

            boolean isSecured = es.isSecure();
            Long count = counts.containsKey(elementName) ? counts.get(elementName) : 0L;

            dataTypes.add(new DataTypeInfo(elementName, singular, plural, isSecured, count));
        }

        return dataTypes;
    }
}
