package org.nrg.xnat.archive;

import org.nrg.xdat.om.XnatResourcecatalog;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import javax.annotation.Nullable;

public class ResourceData {
    private URIManager.DataURIA uri;
    private URIManager.ArchiveItemURI xnatUri;
    private ArchivableItem item;
    @Nullable private XnatResourcecatalog catalogResource;
    @Nullable private String resourceFilePath;

    public ResourceData(URIManager.DataURIA uri,
                        URIManager.ArchiveItemURI xnatUri,
                        ArchivableItem item,
                        @Nullable XnatResourcecatalog catalogResource,
                        @Nullable String resourceFilePath) {
        this.uri = uri;
        this.xnatUri = xnatUri;
        this.catalogResource = catalogResource;
        this.item = item;
        this.resourceFilePath = resourceFilePath;
    }

    public URIManager.DataURIA getUri() {
        return uri;
    }

    public void setUri(URIManager.DataURIA uri) {
        this.uri = uri;
    }

    public URIManager.ArchiveItemURI getXnatUri() {
        return xnatUri;
    }

    public void setXnatUri(URIManager.ArchiveItemURI xnatUri) {
        this.xnatUri = xnatUri;
    }

    @Nullable
    public XnatResourcecatalog getCatalogResource() {
        return catalogResource;
    }

    public void setCatalogResource(@Nullable XnatResourcecatalog catalogResource) {
        this.catalogResource = catalogResource;
    }

    public ArchivableItem getItem() {
        return item;
    }

    public void setItem(ArchivableItem item) {
        this.item = item;
    }

    @Nullable
    public String getResourceFilePath() {
        return resourceFilePath;
    }

    public void setResourceFilePath(@Nullable String resourceFilePath) {
        this.resourceFilePath = resourceFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ResourceData)) return false;
        ResourceData other = (ResourceData) o;
        return item.equals(other.getItem()) && uri.equals(other.getUri()) && xnatUri.equals(other.getXnatUri()) &&
                (catalogResource == null && other.getCatalogResource() == null
                        || catalogResource.equals(other.getCatalogResource())) &&
                (resourceFilePath == null && other.getResourceFilePath() == null
                        || resourceFilePath.equals(other.getResourceFilePath()));
    }
}
