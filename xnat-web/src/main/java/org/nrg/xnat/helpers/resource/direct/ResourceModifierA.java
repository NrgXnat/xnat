/*
 * web: org.nrg.xnat.helpers.resource.direct.ResourceModifierA
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.resource.direct;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatAbstractresource;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResource;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.base.BaseXnatExperimentdata.UnknownPrimaryProjectException;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.nrg.xnat.utils.CatalogUtils;

import java.io.Serializable;
import java.util.ArrayList;
/**
 * @author timo
 */
public abstract class ResourceModifierA implements Serializable {
    private static final long serialVersionUID = 42L;
    final boolean overwrite;
    final UserI user;
    final EventMetaI ci;

    public ResourceModifierA(final boolean overwrite, final UserI user, final EventMetaI ci) {
        this.overwrite = overwrite;
        this.user = user;
        this.ci = ci;
    }

    public static class UpdateMeta implements EventMetaI, Serializable {
        private static final long serialVersionUID = 42L;
        final EventMetaI i;
        final boolean update;
        public UpdateMeta(EventMetaI i, boolean update) {
            this.i = i;
            this.update = update;
        }

        @Override
        public String getMessage() {
            return i.getMessage();
        }

        @Override
        public Date getEventDate() {
            return i.getEventDate();
        }

        @Override
        public String getTimestamp() {
            return i.getTimestamp();
        }

        @Override
        public UserI getUser() {
            return i.getUser();
        }

        @Override
        public Number getEventId() {
            return i.getEventId();
        }

        public boolean getUpdate() {
            return update;
        }

    }

    public abstract XnatProjectdata getProject();

    public abstract boolean addResource(final XnatResource resource, final String type, final UserI user) throws Exception;

    public String getRootPath() {
        return getProject().getRootArchivePath();
    }

    public List<String> addFile(final List<? extends FileWriterWrapperI> fws, final Object resourceIdentifier, final String type, final String filepath, final XnatResourceInfo info, final boolean extract) throws Exception {
        List<String> duplicates = new ArrayList<String>();

        if (fws == null || fws.size() == 0) {
            return duplicates;
        }

        XnatAbstractresource abst = (XnatAbstractresource) getResourceByIdentifier(resourceIdentifier, type);

        boolean isNew = false;
        if (abst == null) {
            isNew = true;
            //new resource
            abst = new XnatResourcecatalog((UserI) user);

            if (resourceIdentifier != null) {
                abst.setLabel(resourceIdentifier.toString());
            }
            abst.setFileCount(0);
            abst.setFileSize(0);

            createCatalog((XnatResourcecatalog) abst, info);

        } else {
            if (!(abst instanceof XnatResourcecatalog)) {
                throw new Exception("Conflict:Non-catalog resource already exits.");
            }
        }

        duplicates.addAll(CatalogUtils.storeCatalogEntry(fws, filepath, (XnatResourcecatalog) abst, getProject(), extract, info, overwrite, ci));

        CatalogUtils.populateStats(abst, null);

        if (isNew) {
            addResource((XnatResourcecatalog) abst, type, user);
        } else {
            if ((!(ci instanceof UpdateMeta)) || ((UpdateMeta) ci).getUpdate()) {
                SaveItemHelper.authorizedSave(abst, user, false, false, ci);
            }
        }

        return duplicates;
    }

    public XnatAbstractresourceI getResourceByIdentifier(final Object resourceIdentifier, final String type) {
        if (resourceIdentifier == null) {
            return null;
        }

        XnatAbstractresourceI resource = null;

        if (resourceIdentifier instanceof Integer) {
            resource = getResourceById((Integer) resourceIdentifier, type);
        }

        if (resource != null) {
            return resource;
        }

        resource = getResourceByLabel(resourceIdentifier.toString(), type);

        if (resource != null) {
            return resource;
        }

        if (StringUtils.isNumeric(resourceIdentifier.toString())) {
            resource = getResourceById(Integer.valueOf(resourceIdentifier.toString()), type);
        }

        return resource;
    }

    protected static String getDefaultUID() {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss");
        return formatter.format(Calendar.getInstance().getTime());
    }

    protected abstract String buildDestinationPath() throws InvalidArchiveStructure, UnknownPrimaryProjectException;

    protected abstract XnatAbstractresourceI getResourceById(final Integer i, final String type);

    protected abstract XnatAbstractresourceI getResourceByLabel(final String lbl, final String type);

    private boolean createCatalog(XnatResourcecatalog resource, XnatResourceInfo info) throws Exception {
        CatalogUtils.configureEntry(resource, info, user);

        final String dest_path = this.buildDestinationPath();

        CatCatalogBean cat = new CatCatalogBean();
        if (resource.getLabel() != null) {
            cat.setId(resource.getLabel());
        } else {
            cat.setId(getDefaultUID());
        }

        File saveTo = new File(new File(dest_path, cat.getId()), cat.getId() + "_catalog.xml");
        saveTo.getParentFile().mkdirs();

        CatalogUtils.writeCatalogToFile(cat, saveTo);

        resource.setUri(saveTo.getAbsolutePath());

        return true;
    }
}

