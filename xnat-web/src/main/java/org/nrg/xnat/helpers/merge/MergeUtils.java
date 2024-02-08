/*
 * web: org.nrg.xnat.helpers.merge.MergeUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.merge;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.nrg.action.ServerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.xdat.bean.CatCatalogBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.model.XnatResourceI;
import org.nrg.xdat.model.XnatResourceseriesI;
import org.nrg.xdat.om.CatCatalog;
import org.nrg.xnat.archive.ArchivingException;
import org.nrg.xnat.utils.CatalogUtils;
import org.nrg.xnat.utils.CatalogUtils.CatalogData;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class MergeUtils {
    @SuppressWarnings("unused")
    public static boolean compareResources(final XnatAbstractresourceI src, final XnatAbstractresourceI dest) {
        if (src instanceof XnatResourceseriesI) {
            return (((XnatResourceseriesI) src).getPath() + ((XnatResourceseriesI) src).getPattern()).equals(((XnatResourceseriesI) src).getPath() + ((XnatResourceseriesI) src).getPattern());
        } else {
            return ((XnatResourceI) src).getUri().equals(((XnatResourceI) dest).getUri());
        }
    }

    public static XnatImagescandataI getMatchingScanById(final String id, final List<XnatImagescandataI> list) {
        return Iterables.tryFind(list, new Predicate<XnatImagescandataI>() {
            @Override
            public boolean apply(final XnatImagescandataI scan2) {
                return StringUtils.equals(id, scan2.getId());
            }
        }).orNull();
    }

    public static XnatImagescandataI getMatchingScan(final XnatImagescandataI scan, final List<XnatImagescandataI> list) {
        return Iterables.tryFind(list, new Predicate<XnatImagescandataI>() {
            @Override
            public boolean apply(final XnatImagescandataI scan2) {
                return StringUtils.equals(scan.getId(), scan2.getId());
            }
        }).orNull();
    }

    public static XnatImagescandataI getMatchingScanByUID(final XnatImagescandataI scan, final List<XnatImagescandataI> list) {
        return Iterables.tryFind(list, new Predicate<XnatImagescandataI>() {
            @Override
            public boolean apply(final XnatImagescandataI scan2) {
                return StringUtils.equals(scan.getUid(), scan2.getUid());
            }
        }).orNull();
    }

    public static XnatAbstractresourceI getMatchingResource(final XnatAbstractresourceI res, List<XnatAbstractresourceI> list) {
        return Iterables.tryFind(list, new Predicate<XnatAbstractresourceI>() {
            @Override
            public boolean apply(final XnatAbstractresourceI res2) {
                return StringUtils.equals(res.getLabel(), res2.getLabel());
            }
        }).orNull();
    }

    public static XnatAbstractresourceI getMatchingResourceByLabel(final String label, List<XnatAbstractresourceI> list) {
        return Iterables.tryFind(list, new Predicate<XnatAbstractresourceI>() {
            @Override
            public boolean apply(XnatAbstractresourceI res2) {
                return StringUtils.equals(label, res2.getLabel());
            }
        }).orNull();
    }


    public static void deleteRejectedFiles(Logger log, List<AnonymizationResult> anonResults, String project) throws ArchivingException {
        final List<AnonymizationResult> rejectedList = anonResults.stream().filter(AnonymizationResultReject.class::isInstance).collect(Collectors.toList());
        if(rejectedList.size()>0) {
            final Multimap<Path,CatalogData> resourceDirectories = ArrayListMultimap.create();
            final List<Path> reviewedDirectories = new ArrayList<>(); //just in case we have a directory of dicom that doesn't have any catalog.xml files

            for (final AnonymizationResult result : rejectedList) {
                try {
                    final Path path = Paths.get(result.getAbsolutePath());
                    final Path parent = path.getParent();
                    Files.delete(path);

                    //parse the available catalog.xml files to know what we have to work with
                    if(!reviewedDirectories.contains(parent)){
                        reviewedDirectories.add(parent);

                        for(final File f: parent.toFile().listFiles()){
                            if(f.getName().endsWith("catalog.xml")){
                                CatalogUtils.CatalogData catalogData = new CatalogUtils.CatalogData(f, project,false);
                                resourceDirectories.put(parent, catalogData);
                            }
                        }
                    }

                    //find the corresponding ccatalog and delete the entry for the deleted file
                    if(resourceDirectories.containsKey(parent)){
                        resourceDirectories.get(parent).forEach(catalogData -> {
                            final CatCatalogBean cat = catalogData.catBean;
                            final CatEntryI catEntry = CatalogUtils.getEntryByURI(cat, path.getFileName().toString());
                            if(catEntry!=null){
                                CatalogUtils.removeEntry(cat, catEntry);
                            }
                        });
                    }
                } catch (IOException e) {
                    log.error("Failed to delete rejected file", e);
                    throw new ArchivingException("Failed to delete rejected file: " + result.getAbsolutePath(), e);
                } catch (ServerException e) {
                    throw new ArchivingException(e);
                }
            }

            //update modified catalogs, delete if its empty and save the xml if it isn't.
            for(final Path parent : reviewedDirectories){
                for(final CatalogData catalogData : resourceDirectories.get(parent)){
                    if (catalogData.catBean.getEntries_entry().size() > 0) {
                        try {
                            CatalogUtils.writeCatalogToFile(catalogData.catBean, catalogData.catFile, project);
                        } catch (Exception e) {
                            log.error("Failed to write catalog.xml file", e);
                        }
                    } else {
                        try {
                            Files.delete(catalogData.catFile.toPath());
                        } catch (IOException e) {
                            log.error("Failed to delete catalog.xml file", e);
                        }
                    }
                }
            }
        }
    }
}
