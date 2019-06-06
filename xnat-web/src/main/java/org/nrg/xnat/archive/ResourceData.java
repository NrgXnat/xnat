package org.nrg.xnat.archive;

import org.nrg.xdat.model.XnatResourcecatalogI;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.turbine.utils.ArchivableItem;

import javax.annotation.Nullable;

public class ResourceData {
    private URIManager.DataURIA uri;
    private URIManager.ArchiveItemURI xnatUri;
    private XnatResourcecatalogI catalogResource;
    private ArchivableItem item;

    public ResourceData(URIManager.DataURIA uri,
                        URIManager.ArchiveItemURI xnatUri,
                        ArchivableItem item,
                        @Nullable XnatResourcecatalogI catalogResource) {
        this.uri = uri;
        this.xnatUri = xnatUri;
        this.catalogResource = catalogResource;
        this.item = item;
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

    public XnatResourcecatalogI getCatalogResource() {
        return catalogResource;
    }

    public void setCatalogResource(XnatResourcecatalogI catalogResource) {
        this.catalogResource = catalogResource;
    }

    public ArchivableItem getItem() {
        return item;
    }

    public void setItem(ArchivableItem item) {
        this.item = item;
    }
}
