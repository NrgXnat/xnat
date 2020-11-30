package org.nrg.xnat.eventservice.model.xnat;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.model.XnatAbstractprojectassetI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatExperimentdataI;
import org.nrg.xdat.model.XnatImagesessiondataI;
import org.nrg.xdat.model.XnatSubjectdataI;
import org.nrg.xdat.om.XnatAbstractprojectasset;
import org.nrg.xdat.om.XnatExperimentdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xdat.om.base.BaseXnatExperimentdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.exceptions.InvalidArchiveStructure;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@JsonInclude(Include.NON_NULL)
public class ProjectAsset extends XnatModelObject {

    @JsonIgnore private XnatAbstractprojectassetI xnatProjectAssetI;
    private List<Session> sessions;
    private List<Subject> subjects;
    private List<Resource> resources;
    @JsonProperty("project-id") private String projectId;
    private String directory;
    @JsonProperty("datatype-string") private String datatypeString;


    public ProjectAsset() {}

    public ProjectAsset(final String projectAssetId, final UserI userI) {
        this.id = projectAssetId;
        loadXnatProjectAssetDataI(userI);
        this.uri = UriParserUtils.getArchiveUri(xnatProjectAssetI);
        populateProperties(null);
    }

    public ProjectAsset(final XnatAbstractprojectassetI xnatProjectAssetI, final String parentUri, final String rootArchivePath) {
        this.xnatProjectAssetI = xnatProjectAssetI;
        if (parentUri == null) {
            this.uri = UriParserUtils.getArchiveUri(xnatProjectAssetI);
        } else {
            this.uri = parentUri + "/subjects/" + xnatProjectAssetI.getId();
        }
        populateProperties(rootArchivePath);
    }

    private void populateProperties(final String rootArchivePath) {
        this.id = xnatProjectAssetI.getId();
        this.label = xnatProjectAssetI.getLabel();
        this.xsiType = xnatProjectAssetI.getXSIType();
        this.projectId = xnatProjectAssetI.getProject();

        try {
            this.directory = ((XnatAbstractprojectasset) xnatProjectAssetI).getCurrentSessionFolder(true);
        } catch (BaseXnatExperimentdata.UnknownPrimaryProjectException | InvalidArchiveStructure e) {
            // ignored, I guess?
        }

        this.subjects = Lists.newArrayList();
        for (final XnatSubjectdataI xnatSubjectdataI : xnatProjectAssetI.getSubjects_subject()) {
            if (xnatSubjectdataI instanceof XnatSubjectdata) {
                subjects.add(new Subject(xnatSubjectdataI, this.uri, rootArchivePath));
            }
        }

        this.sessions = Lists.newArrayList();
        for (final XnatExperimentdataI xnatExperimentdataI : xnatProjectAssetI.getExperiments_experiment()) {
            if (xnatExperimentdataI instanceof XnatImagesessiondataI) {
                sessions.add(new Session((XnatImagesessiondataI) xnatExperimentdataI, this.uri, rootArchivePath));
            }
        }

        this.resources = Lists.newArrayList();
        for (final XnatAbstractresourceI xnatAbstractresourceI : xnatProjectAssetI.getResources_resource()) {
            if (xnatAbstractresourceI instanceof XnatResourcecatalog) {
                resources.add(new Resource((XnatResourcecatalog) xnatAbstractresourceI, this.uri, rootArchivePath));
            }
        }
        datatypeString = null;
        if(xnatProjectAssetI != null){
            try {
                datatypeString = xnatProjectAssetI.toString();
            } catch (Throwable e){ }
        }
    }

    public static Function<URIManager.ArchiveItemURI, ProjectAsset> uriToModelObject() {
        return new Function<URIManager.ArchiveItemURI, ProjectAsset>() {
            @Nullable
            @Override
            public ProjectAsset apply(@Nullable URIManager.ArchiveItemURI uri) {
                return null;
            }
        };
    }
    public static Function<String, ProjectAsset> idToModelObject(final UserI userI, final boolean loadFiles,
                                                                 @Nonnull final Set<String> loadTypes) {
        return new Function<String, ProjectAsset>() {
            @Nullable
            @Override
            public ProjectAsset apply(@Nullable String s) {
                if (StringUtils.isBlank(s)) {
                    return null;
                }
                final XnatAbstractprojectasset xnatAbstractprojectasset = XnatAbstractprojectasset.getXnatAbstractprojectassetsById(s, userI, true);
                if (xnatAbstractprojectasset != null) {
                    return new ProjectAsset(xnatAbstractprojectasset.getId(), userI);
                }
                return null;
            }
        };
    }

    public Project getProject(final UserI userI) {
        loadXnatProjectAssetDataI(userI);
        return new Project(xnatProjectAssetI.getProject(), userI);
    }

    public void loadXnatProjectAssetDataI(final UserI userI) {
        if (xnatProjectAssetI == null) {
            xnatProjectAssetI = XnatAbstractprojectasset.getXnatAbstractprojectassetsById(id, userI, false);
        }
    }

    public String getProjectId() {
        return projectId;
    }


    public List<Session> getSessions() { return sessions; }

    public List<Subject> getSubjects() { return subjects; }

    public List<Resource> getResources() { return resources; }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public String getDatatypeString() {
        return datatypeString;
    }

    @Override
    public XFTItem getXftItem(final UserI userI) {
        loadXnatProjectAssetDataI(userI);
        return xnatProjectAssetI == null ? null : ((XnatExperimentdata)xnatProjectAssetI).getItem();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final ProjectAsset that = (ProjectAsset) o;
        return Objects.equals(this.xnatProjectAssetI, that.xnatProjectAssetI) &&
                Objects.equals(this.subjects, that.subjects) &&
                Objects.equals(this.sessions, that.sessions) &&
                Objects.equals(this.resources, that.resources) &&
                Objects.equals(this.projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), xnatProjectAssetI, subjects, sessions, resources, projectId);
    }

    @Override
    public String toString() {
        return addParentPropertiesToString(MoreObjects.toStringHelper(this))
                .add("sessions", sessions)
                .add("subjects", subjects)
                .add("resources", resources)
                .add("projectId", projectId)
                .toString();
    }

}
