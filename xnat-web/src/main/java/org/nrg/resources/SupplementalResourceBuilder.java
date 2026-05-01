package org.nrg.resources;

import lombok.extern.slf4j.Slf4j;
import org.nrg.io.RelativePathWriter;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatResourcecatalogBean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public abstract class SupplementalResourceBuilder {
    private Class<? extends XnatImagesessiondataBean> imageSessionDataBeanClass;

    protected SupplementalResourceBuilder(Class<? extends XnatImagesessiondataBean> imageSessionDataBeanClass) {
        this.imageSessionDataBeanClass = imageSessionDataBeanClass;
    }

    @Nullable
    public <T extends XnatImagesessiondataBean> List<String> addToSession(T sessionBean,
                                                                          Collection<File> dirs,
                                                                          List<String> excluded)
            throws SupplementalResourceBuilderException {
        if (!imageSessionDataBeanClass.isInstance(sessionBean)) {
            throw new SupplementalResourceBuilderException(this.getClass().getName() + " does not support beans of type " +
                    sessionBean.getClass().getName());
        }

        File prearchivePath = new File(sessionBean.getPrearchivepath());

        Map<File, Collection<File>> matchMap = new HashMap<>();
        for (File dir : dirs) {
            if (!dir.isDirectory()) {
                continue;
            }
            matchMap.put(dir, getRelevantFiles(dir));
        }

        if (matchMap.isEmpty()) {
            return null;
        }

        List<String> added = new ArrayList<>();
        Map<String, CatCatalogBean> catalogMap = new HashMap<>();
        for (File dir : matchMap.keySet()) {

            boolean alreadyInPrearchive = alreadyInPrearchive(dir, prearchivePath);

            for (File file : matchMap.get(dir)) {
                Path relPath = dir.toPath().relativize(file.toPath());
                int index = alreadyInPrearchive ? 1 : 0; // set to 1 to skip "RESOURCES"
                String label = relPath.subpath(index, ++index).toString();
                if (label.equals(relPath.toString())) {
                    label = "supplemental";
                    relPath = Paths.get(label).resolve(relPath);
                }

                Path catalogUriRelative = getCatalogUri(label);
                Path catalogUri = prearchivePath.toPath().resolve(catalogUriRelative);
                Path catalogPath = catalogUri.getParent();

                String catalogUriStr = catalogUriRelative.toString();
                if (excluded.contains(catalogUriStr) || file.equals(catalogUri.toFile())) {
                    continue;
                }

                try {
                    Files.createDirectories(catalogPath);
                } catch (IOException e) {
                    log.error("Unable to create directory {}", catalogPath, e);
                    continue;
                }

                // Make catalog
                CatCatalogBean catalog;
                if (catalogMap.containsKey(catalogUriStr)) {
                    catalog = catalogMap.get(catalogUriStr);
                } else {
                    catalog = new CatCatalogBean();
                    catalog.setId(label);
                    catalogMap.put(catalogUriStr, catalog);
                }

                String catalogRelativePath = relPath.subpath(index, relPath.getNameCount()).toString();
                if (!alreadyInPrearchive) {
                    // copy into prearchive
                    Path dest = catalogPath.resolve(catalogRelativePath);
                    try {
                        Files.createDirectories(dest.getParent());
                        Files.copy(file.toPath(), dest);
                    } catch (IOException e) {
                        log.error("Unable to copy {} to {}", file, dest, e);
                        continue;
                    }
                }
                CatEntryBean entry = new CatEntryBean();
                entry.setId(catalogRelativePath);
                entry.setUri(catalogRelativePath);
                catalog.addEntries_entry(entry);
            }

            for (String catalogUriStr : catalogMap.keySet()) {
                CatCatalogBean catalog = catalogMap.get(catalogUriStr);

                try (RelativePathWriter writer = new RelativePathWriter(prearchivePath,
                        new File(prearchivePath, catalogUriStr))) {
                    catalog.toXML(writer, true);
                } catch (IOException e) {
                    log.error("Unable to write catalog to {}", catalogUriStr, e);
                    continue;
                }

                // Make resource
                XnatResourcecatalogBean resource = new XnatResourcecatalogBean();
                resource.setLabel(catalog.getId());
                resource.setUri(catalogUriStr);
                sessionBean.addResources_resource(resource);

                added.add(catalogUriStr);
            }
        }

        return added;
    }

    @Nonnull
    protected abstract Collection<File> getRelevantFiles(File dir);

    private Path getCatalogUri(String label) {
        String catalogName = label + "_catalog.xml";
        return Paths.get("RESOURCES", label, catalogName);
    }

    private boolean alreadyInPrearchive(File dir, File prearchivePath) {
        return dir.equals(prearchivePath);
    }
}