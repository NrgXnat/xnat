package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.model.XnatInvestigatordataI;
import org.nrg.xdat.model.XnatProjectdataAliasI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ProjectURII;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@JsonInclude(Include.NON_NULL)
public class Project extends XnatModelObject {
    @JsonIgnore private XnatProjectdata xnatProjectdata;
    private List<Resource> resources;
    private List<Subject> subjects;
    private String directory;
    private String title;
    @JsonProperty("running-title") private String runningTitle;
    private String description;
    private String keywords;
    private String accessibility;
    private List<String> aliases;
    private Investigator pi;
    private List<Investigator> investigators = new ArrayList<>();

    public Project() {}

    public Project(final String projectId, final UserI userI) {
        this(projectId, userI, true);
    }

    public Project(final String projectId, final UserI userI, final boolean preload) {
        this.id = projectId;
        loadXnatProjectdata(userI);
        this.uri = UriParserUtils.getArchiveUri(xnatProjectdata);
        populateProperties(preload);
    }

    public Project(final ProjectURII projectURII) {
        this(projectURII, true);
    }

    public Project(final ProjectURII projectURII, final boolean preload) {
        this.xnatProjectdata = projectURII.getProject();
        this.uri = ((URIManager.DataURIA) projectURII).getUri();
        populateProperties(preload);
    }

    public Project(final XnatProjectdata xnatProjectdata) {
        this(xnatProjectdata, true);
    }

    public Project(final XnatProjectdata xnatProjectdata, final boolean preload) {
        this.xnatProjectdata = xnatProjectdata;
        this.uri = UriParserUtils.getArchiveUri(xnatProjectdata);
        populateProperties(preload);
    }

    public static Project populateSample() {
        final Project project = new Project();
        project.setId("SampleProjectID");
        project.setLabel("SampleProjectLabel");
        project.setXsiType("xnat:projectData");
        project.setUri("/archive/projects/SampleProjectID");
        project.setDirectory("/data/xnat/archive/SampleProjectID/arc001");
        return project;
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
            reloadXnatProjectdata(userI);
        }
    }

    public void reloadXnatProjectdata(final UserI userI) {
        xnatProjectdata = XnatProjectdata.getXnatProjectdatasById(id, userI, false);
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

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getRunningTitle() { return runningTitle; }

    public void setRunningTitle(String runningTitle) { this.runningTitle = runningTitle; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    public String getKeywords() { return keywords; }

    public void setKeywords(String keywords) { this.keywords = keywords; }

    public List<String> getAliases() { return aliases; }

    public void setAliases(List<String> aliases) { this.aliases = aliases; }

    public Investigator getPi() { return pi; }

    public void setPi(Investigator pi) { this.pi = pi; }

    public List<Investigator> getInvestigators() { return investigators; }

    public void setInvestigators(List<Investigator> investigators) { this.investigators = investigators; }

    public String getAccessibility() { return accessibility; }

    public void setAccessibility(String accessibility) { this.accessibility = accessibility; }

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
                .add("pi", pi)
                .add("investigators", investigators)
                .toString();
    }

    private void populateProperties(final boolean preload) {
        this.id = xnatProjectdata.getId();
        this.xsiType = "xnat:projectData";
        try { this.xsiType = xnatProjectdata.getXSIType();} catch(NullPointerException e){log.error("Project failed to detect xsiType. " + e.getMessage());}
        if(StringUtils.contains(this.xsiType, "arc:project")){
            this.reloadXnatProjectdata(xnatProjectdata.getUser());
        }
        try { this.directory = xnatProjectdata.getRootArchivePath() + "arc001";} catch (NullPointerException e){log.error("Project could not get root archive path " + e.getMessage());}
        try { this.accessibility = xnatProjectdata.getPublicAccessibility();} catch (Throwable e){log.error("Could not get project accessibility.", e);}

        this.label = StringUtils.defaultIfBlank(xnatProjectdata.getName(), xnatProjectdata.getId());
        this.title = xnatProjectdata.getName();
        this.runningTitle = xnatProjectdata.getDisplayID();
        this.description = xnatProjectdata.getDescription();
        this.keywords = xnatProjectdata.getKeywords();
        this.aliases = xnatProjectdata.getAliases_alias().stream().map(XnatProjectdataAliasI::getAlias).collect(Collectors.toList());
        this.pi = xnatProjectdata.getPi() != null ? new Investigator(xnatProjectdata.getPi()) : null;

        List<XnatInvestigatordataI> xnatInvestigators = xnatProjectdata.getInvestigators_investigator();
        if(xnatInvestigators != null && !xnatInvestigators.isEmpty()){
            xnatInvestigators.forEach(i -> this.investigators.add(new Investigator(i)));
        }

        this.subjects = new ArrayList<>();
        try {
            if (preload) {
                subjects.addAll(xnatProjectdata.getParticipants_participant().stream().map(subject -> new Subject(subject, this.uri, xnatProjectdata.getRootArchivePath())).collect(Collectors.toList()));
            }
        } catch (Exception e) { log.error("Exception trying to load participants. " + e.getMessage()); }

        this.resources = new ArrayList<>();
        try {
            if (preload) {
                resources.addAll(xnatProjectdata.getResources_resource().stream().filter(XnatResourcecatalog.class::isInstance).map(resource -> new Resource((XnatResourcecatalog) resource, this.uri, xnatProjectdata.getRootArchivePath())).collect(Collectors.toList()));
            }
        } catch (Exception e) { log.error("Exception trying to load project resources. " + e.getMessage()) ;}
    }
}
