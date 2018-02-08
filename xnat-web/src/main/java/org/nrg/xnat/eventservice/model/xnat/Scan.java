package org.nrg.xnat.eventservice.model.xnat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatImagescandata;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.UriParserUtils;
import org.nrg.xnat.helpers.uri.archive.ScanURII;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
public class Scan extends XnatModelObject {
    @JsonIgnore private XnatImagescandataI xnatImagescandataI;
    @JsonProperty("integer-id") private Integer integerId;
    @JsonProperty("scan-type") private String scanType;
    @JsonProperty("project-id") private String projectId;
    @JsonProperty("session-id") private String sessionId;
    private List<Resource> resources;
    private String directory;

    private Integer frames;
    private String note;
    private String modality;
    private String quality;
    private String scanner;
    @JsonProperty("scanner-manufacturer") private String scannerManufacturer;
    @JsonProperty("scanner-model") private String scannerModel;
    @JsonProperty("scanner-software-version") private String scannerSoftwareVersion;
    @JsonProperty("series-description") private String seriesDescription;
    @JsonProperty("start-time") private Object startTime;
    private String uid;

    private static final Logger log = LoggerFactory.getLogger(Scan.class);

    public Scan() {}

    public Scan(final ScanURII scanURII) {
        this.xnatImagescandataI = scanURII.getScan();
        this.uri = ((URIManager.ArchiveItemURI)scanURII).getUri();
        populateProperties(null);
    }

    public Scan(final XnatImagescandataI xnatImagescandataI, final String parentUri, final String rootArchivePath) {
        this.xnatImagescandataI = xnatImagescandataI;
        if (parentUri == null) {
            this.uri = UriParserUtils.getArchiveUri(xnatImagescandataI);
        } else {
            this.uri = parentUri + "/scans/" + xnatImagescandataI.getId();
        }
        populateProperties(rootArchivePath);
    }

    private void populateProperties(final String rootArchivePath) {
        this.integerId = xnatImagescandataI.getXnatImagescandataId();
        this.id = xnatImagescandataI.getId();
        this.xsiType = "xnat:imageScanData";
        try { this.xsiType = xnatImagescandataI.getXSIType();} catch(NullPointerException e){log.error("Scan failed to detect xsiType");}
        this.scanType = xnatImagescandataI.getType();
        this.label = String.format("%s - %s", this.id, this.scanType);

        this.frames = xnatImagescandataI.getFrames();
        this.note = xnatImagescandataI.getNote();
        this.modality = xnatImagescandataI.getModality();
        this.quality = xnatImagescandataI.getQuality();
        this.scanner = xnatImagescandataI.getScanner();
        this.scannerManufacturer = xnatImagescandataI.getScanner_manufacturer();
        this.scannerModel = xnatImagescandataI.getScanner_model();
        this.scannerSoftwareVersion = xnatImagescandataI.getScanner_softwareversion();
        this.seriesDescription = xnatImagescandataI.getSeriesDescription();
        this.startTime = xnatImagescandataI.getStarttime();
        this.uid = xnatImagescandataI.getUid();

        if (this.xnatImagescandataI instanceof XnatImagescandata) {
            this.directory = ((XnatImagescandata) xnatImagescandataI).deriveScanDir();
        }

        this.resources = Lists.newArrayList();
        for (final XnatAbstractresourceI xnatAbstractresourceI : this.xnatImagescandataI.getFile()) {
            if (xnatAbstractresourceI instanceof XnatResourcecatalog) {
                resources.add(new Resource((XnatResourcecatalog) xnatAbstractresourceI, this.uri, rootArchivePath));
            }
        }

    }

    public static Function<URIManager.ArchiveItemURI, Scan> uriToModelObject() {
        return new Function<URIManager.ArchiveItemURI, Scan>() {
            @Nullable
            @Override
            public Scan apply(@Nullable URIManager.ArchiveItemURI uri) {
                if (uri != null &&
                        ScanURII.class.isAssignableFrom(uri.getClass())) {
                    return new Scan((ScanURII) uri);
                }

                return null;
            }
        };
    }

    public static Function<String, Scan> idToModelObject(final UserI userI) {
        return null;
    }

    public Project getProject(final UserI userI) {
        loadXnatImagescandataI(userI);
        return new Project(xnatImagescandataI.getProject(), userI);
    }

    public Session getSession(final UserI userI) {
        loadXnatImagescandataI(userI);
        return new Session(xnatImagescandataI.getImageSessionId(), userI);
    }

    public void loadXnatImagescandataI(final UserI userI) {
        if (xnatImagescandataI == null) {
            xnatImagescandataI = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(integerId, userI, false);
        }
    }

    public XnatImagescandataI getXnatImagescandataI() {
        return xnatImagescandataI;
    }

    public void setXnatImagescandataI(final XnatImagescandataI xnatImagescandataI) {
        this.xnatImagescandataI = xnatImagescandataI;
    }

    public String getScanType() {
        return scanType;
    }

    public void setScanType(final String scanType) {
        this.scanType = scanType;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(final List<Resource> resources) {
        this.resources = resources;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(final String projectId) {
        this.projectId = projectId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(final String sessionId) {
        this.sessionId = sessionId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    public Integer getIntegerId() {
        return integerId;
    }

    public void setIntegerId(final Integer integerId) {
        this.integerId = integerId;
    }

    public Integer getFrames() {
        return frames;
    }

    public void setFrames(final Integer frames) {
        this.frames = frames;
    }

    public String getNote() {
        return note;
    }

    public void setNote(final String note) {
        this.note = note;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(final String modality) {
        this.modality = modality;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(final String quality) {
        this.quality = quality;
    }

    public String getScanner() {
        return scanner;
    }

    public void setScanner(final String scanner) {
        this.scanner = scanner;
    }

    public String getScannerManufacturer() {
        return scannerManufacturer;
    }

    public void setScannerManufacturer(final String scannerManufacturer) {
        this.scannerManufacturer = scannerManufacturer;
    }

    public String getScannerModel() {
        return scannerModel;
    }

    public void setScannerModel(final String scannerModel) {
        this.scannerModel = scannerModel;
    }

    public String getScannerSoftwareVersion() {
        return scannerSoftwareVersion;
    }

    public void setScannerSoftwareVersion(final String scannerSoftwareVersion) {
        this.scannerSoftwareVersion = scannerSoftwareVersion;
    }

    public String getSeriesDescription() {
        return seriesDescription;
    }

    public void setSeriesDescription(final String seriesDescription) {
        this.seriesDescription = seriesDescription;
    }

    public Object getStartTime() {
        return startTime;
    }

    public void setStartTime(final Object startTime) {
        this.startTime = startTime;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(final String uid) {
        this.uid = uid;
    }

    @Override
    public XFTItem getXftItem(final UserI userI) {
        loadXnatImagescandataI(userI);
        return xnatImagescandataI == null ? null : ((XnatImagescandata)xnatImagescandataI).getItem();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final Scan that = (Scan) o;
        return Objects.equals(this.xnatImagescandataI, that.xnatImagescandataI) &&
                Objects.equals(this.integerId, that.integerId) &&
                Objects.equals(this.scanType, that.scanType) &&
                Objects.equals(this.resources, that.resources) &&
                Objects.equals(this.projectId, that.projectId) &&
                Objects.equals(this.sessionId, that.sessionId) &&
                Objects.equals(this.directory, that.directory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), xnatImagescandataI, integerId, scanType, resources, projectId, sessionId, directory);
    }

    @Override
    public String toString() {
        return addParentPropertiesToString(MoreObjects.toStringHelper(this))
                .add("integerId", integerId)
                .add("scanType", scanType)
                .add("resources", resources)
                .add("projectId", projectId)
                .add("sessionId", sessionId)
                .add("directory", directory)
                .add("frames", frames)
                .add("note", note)
                .add("modality", modality)
                .add("quality", quality)
                .add("scanner", scanner)
                .add("scannerManufacturer", scannerManufacturer)
                .add("scannerModel", scannerModel)
                .add("scannerSoftwareVersion", scannerSoftwareVersion)
                .add("seriesDescription", seriesDescription)
                .add("startTime", startTime)
                .add("uid", uid)
                .toString();
    }
}
