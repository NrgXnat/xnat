package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xdat.om.XnatSubjectdata;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ProjectURII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class Project extends XnatModelObject {
    @JsonIgnore private XnatProjectdata xnatProjectdata;
    private List<Resource> resources;
    private List<Subject> subjects;
    private String directory;

    private static final Logger log = LoggerFactory.getLogger(Project.class);

    public Project() {}

    public Project(final String projectId, final UserI userI) {
        new Project(projectId, userI, true);
    }

    public Project(final String projectId, final UserI userI, final boolean preload) {
        this.id = projectId;
        loadXnatProjectdata(userI);
        this.uri = UriParserUtils.getArchiveUri(xnatProjectdata);
        populateProperties(preload);
    }

    public Project(final ProjectURII projectURII) {
        new Project(projectURII, true);
    }

    public Project(final ProjectURII projectURII, final boolean preload) {
        this.xnatProjectdata = projectURII.getProject();
        this.uri = ((URIManager.DataURIA) projectURII).getUri();
        populateProperties(preload);
    }

    public Project(final XnatProjectdata xnatProjectdata) {
        new Project(xnatProjectdata, true);
    }

    public Project(final XnatProjectdata xnatProjectdata, final boolean preload) {
        this.xnatProjectdata = xnatProjectdata;
        this.uri = UriParserUtils.getArchiveUri(xnatProjectdata);
        populateProperties(preload);
    }

    private void populateProperties(final boolean preload) {
        this.id = xnatProjectdata.getId();
        this.label = xnatProjectdata.getName();
        this.xsiType = "xnat:projectData";
        try { this.xsiType = xnatProjectdata.getXSIType();} catch(NullPointerException e){log.error("Project failed to detect xsiType");}
        this.directory = xnatProjectdata.getRootArchivePath() + "arc001";

        this.subjects = Lists.newArrayList();
        if (preload) {
            for (final XnatSubjectdata subject : xnatProjectdata.getParticipants_participant()) {
                subjects.add(new Subject(subject, this.uri, xnatProjectdata.getRootArchivePath()));
            }
        }

        this.resources = Lists.newArrayList();
        if (preload) {
            for (final XnatAbstractresourceI xnatAbstractresourceI : xnatProjectdata.getResources_resource()) {
                if (xnatAbstractresourceI instanceof XnatResourcecatalog) {
                    resources.add(new Resource((XnatResourcecatalog) xnatAbstractresourceI, this.uri, xnatProjectdata.getRootArchivePath()));
                }
            }
        }
    }

    public static Function<URIManager.ArchiveItemURI, Project> uriToModelObject() {
        return uriToModelObject(true);
    }

    public static Function<URIManager.ArchiveItemURI, Project> uriToModelObject(final boolean preload) {
        return new Function<URIManager.ArchiveItemURI, Project>() {
            @Nullable
            @Override
            public Project apply(@Nullable URIManager.ArchiveItemURI uri) {
                if (uri != null &&
                        ProjectURII.class.isAssignableFrom(uri.getClass())) {
                    return new Project((ProjectURII) uri, preload);
                }

                return null;
            }
        };
    }

    public static Function<String, Project> idToModelObject(final UserI userI) {
        return idToModelObject(userI, true);
    }

    public static Function<String, Project> idToModelObject(final UserI userI, final boolean preload) {
        return new Function<String, Project>() {
            @Nullable
            @Override
            public Project apply(@Nullable String s) {
                if (StringUtils.isBlank(s)) {
                    return null;
                }
                final XnatProjectdata xnatProjectdata = XnatProjectdata.getXnatProjectdatasById(s, userI, false);
                if (xnatProjectdata != null) {
                    return new Project(xnatProjectdata, preload);
                }
                return null;
            }
        };
    }

    public Project getProject(final UserI userI) {
        loadXnatProjectdata(userI);
        return this;
    }

    public void loadXnatProjectdata(final UserI userI) {
        if (xnatProjectdata == null) {
            xnatProjectdata = XnatProjectdata.getXnatProjectdatasById(id, userI, false);
        }
    }

    public XnatProjectdata getXnatProjectdata() {
        return xnatProjectdata;
    }

    public void setXnatProjectdata(final XnatProjectdata xnatProjectdata) {
        this.xnatProjectdata = xnatProjectdata;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }

    public List<Subject> getSubjects() {
        return subjects;
    }

    public void setSubjects(final List<Subject> subjects) {
        this.subjects = subjects;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    @Override
    public XFTItem getXftItem(final UserI userI) {
        loadXnatProjectdata(userI);
        return xnatProjectdata == null ? null : xnatProjectdata.getItem();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Project that = (Project) o;
        return Objects.equals(this.xnatProjectdata, that.xnatProjectdata) &&
                Objects.equals(this.directory, that.directory) &&
                Objects.equals(this.resources, that.resources) &&
                Objects.equals(this.subjects, that.subjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), xnatProjectdata, directory, resources, subjects);
    }


    @Override
    public String toString() {
        return addParentPropertiesToString(MoreObjects.toStringHelper(this))
                .add("directory", directory)
                .add("resources", resources)
                .add("subjects", subjects)
                .toString();
    }
}
