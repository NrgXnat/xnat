/*
 * web: org.nrg.xnat.helpers.uri.archive.ResourceURIA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri.archive;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatImageassessordata;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatReconstructedimagedata;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.URIManager.ArchiveURI;
import org.nrg.xnat.helpers.uri.UriParserUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Provides the basic functions for a resource URI.
 */
@Slf4j
public abstract class ResourceURIA extends ArchiveURI implements ResourceURII {
    public ResourceURIA(final Map<String, Object> properties, final String uri) {
        super(properties, uri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    abstract public XnatAbstractresourceI getXnatResource();

    /**
     * {@inheritDoc}
     */
    @Override
    abstract public XnatProjectdata getProject();

    /**
     * {@inheritDoc}
     */
    @Override
    public int getXnatAbstractresourceId() {
        final XnatAbstractresourceI resource = getXnatResource();
        return resource != null ? resource.getXnatAbstractresourceId() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceLabel() {
        return (String) props.get(URIManager.XNAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getResourceFilePath() {
        return (String) props.get(UriParserUtils._REMAINDER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<XnatAbstractresourceI> getResources(final boolean includeAll) {
        return Lists.newArrayList(getXnatResource());
    }

    /**
     * Tests whether the any of the submitted resources match the resource represented by this URI and returns the first
     * one that does. This uses the {@link #matches(XnatAbstractresourceI)} method to test each resource and filter
     * non-matching resources. It then returns the first matching resource found.
     *
     * @param resources The resources to be compared with the resource represented by this URI object.
     *
     * @return Returns the matching resource if found, or null if no matching resource was found.
     */
    @Nullable
    protected XnatAbstractresourceI getMatchingResource(final Collection<XnatAbstractresourceI> resources) {
        final Iterator<XnatAbstractresourceI> filtered = Iterables.filter(resources, new Predicate<XnatAbstractresourceI>() {
            @Override
            public boolean apply(@Nullable final XnatAbstractresourceI resource) {
                return matches(resource);
            }
        }).iterator();
        if (filtered.hasNext()) {
            return filtered.next();
        }
        return null;
    }

    /**
     * Tests whether the submitted resource matches the resource represented by this URI. If both resources have
     * resource labels, this compares those and returns true if they are the same. If either has a blank label,
     * this method gets the ID of each resource and compares those.
     *
     * @param resource The resource to be compared with the resource represented by this URI object.
     *
     * @return Returns true if the labels or IDs match, false otherwise.
     */
    protected boolean matches(final XnatAbstractresourceI resource) {
        if (resource == null) {
            return false;
        }

        final String resourceLabel = getResourceLabel();
        final String otherLabel    = resource.getLabel();

        if (StringUtils.isAnyBlank(otherLabel, resourceLabel)) {
            if (NumberUtils.isCreatable(resourceLabel)) {
                return NumberUtils.compare(NumberUtils.createInteger(resourceLabel), resource.getXnatAbstractresourceId()) == 0;
            }
            return false;
        }

        return StringUtils.equals(otherLabel, resourceLabel);
    }

    protected List<XnatAbstractresourceI> getAssessorResources(final XnatImageassessordata assessor, final String type) {
        switch (type) {
            case "out":
                return assessor.getOut_file();

            case "in":
                return assessor.getIn_file();

            default:
                log.error("An unknown assessor type was specified: \"{}\". I don't know how to handle this so returning empty resource set.", type);
                return Collections.emptyList();
        }
    }

    protected List<XnatAbstractresourceI> getReconstructionResources(final XnatReconstructedimagedata reconstruction, final String type) {
        switch (type) {
            case "out":
                return reconstruction.getOut_file();

            case "in":
                return reconstruction.getIn_file();

            default:
                log.error("An unknown reconstruction type was specified: \"{}\". I don't know how to handle this so returning empty resource set.", type);
                return Collections.emptyList();
        }
    }
}
