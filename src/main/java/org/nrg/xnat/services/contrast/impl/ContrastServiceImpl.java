package org.nrg.xnat.services.contrast.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.nrg.xnat.contrast.model.ContrastBolus;
import org.nrg.xnat.contrast.model.Ingredient;
import org.nrg.xnat.contrast.parser.ContrastParser;
import org.nrg.xdat.model.*;
import org.nrg.xdat.om.*;
import org.nrg.xft.event.EventMetaI;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.event.persist.PersistentWorkflowI;
import org.nrg.xft.event.persist.PersistentWorkflowUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnat.eventservice.exceptions.UnauthorizedException;
import org.nrg.xnat.helpers.ArchiveEntryFileWriterWrapper;
import org.nrg.xnat.helpers.resource.XnatResourceInfo;
import org.nrg.xnat.helpers.resource.direct.ResourceModifierA;
import org.nrg.xnat.helpers.uri.URIManager;
import org.nrg.xnat.helpers.uri.archive.ResourceURII;
import org.nrg.xnat.helpers.uri.archive.impl.ResourcesExptScanURI;
import org.nrg.xnat.services.contrast.ContrastService;
import org.nrg.xnat.services.triage.TriageUtils;
import org.nrg.xnat.utils.CatalogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.*;
import java.util.*;


@Slf4j
@Service
public class ContrastServiceImpl implements ContrastService {
    public static final String DICOM = "DICOM";

    public static final String CONTRAST_SUMMARY_JSON = "contrastSummary.json";
    public static final String JSON = "json";
    public static final String CONTRAST_J = "contrastJ";

    @Override
    public void save(final ContrastBolus contrast, final UserI user, final boolean isNewContrast) throws Exception {
        final XnatContrastbolusv2 bolus = toXFT(contrast);

        final XnatImagescandata scan = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(
                contrast.getScanId(), user, false);
        final XnatImagesessiondata session = XnatImagesessiondata.getXnatImagesessiondatasById(
                scan.getImageSessionId(), user, false);

        if(session==null) {
            throw new UnknownScanException("Unknown scan: "+ contrast.getScanId());
        }

        //event to track contrast modification
        final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, session.getItem(),
            EventUtils.newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.TYPE.REST, "Update contrast entry"));

        if (!isNewContrast) {
            bolus.setXnatContrastbolusv2Id(contrast.getPk());
        }

        //save chenges to XFT
        if(session.canEdit(user) && scan.canEdit(user)){
           bolus.save(user,false,true,wrk.buildEvent());
           contrast.setPk(bolus.getXnatContrastbolusv2Id());
        } else {
            throw new UnauthorizedException("User does not have the permission to edit scan:" +
                                                    contrast.getScanId() + " For session" + session.getId());
        }

        //save json to disk
        try {
            final List<ContrastBolus> currentContrasts = loadContrastWithInit(scan, user, session.getProject());
            int currentContrastIndex = indexOf(currentContrasts, contrast);
            if(currentContrastIndex < currentContrasts.size()){
                //updating an existing record
                currentContrasts.remove(currentContrastIndex);
            }
            currentContrasts.add(contrast);

            saveContrastToTextResource(currentContrasts, user, session, scan, wrk.buildEvent());
        } catch (Exception e) {
            log.error("Error saving contrast to disk", e);
            PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            return;
        }

        PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
    }

    private List<ContrastBolus> loadContrastWithInit(XnatImagescandataI scan, UserI user, String rootProject){
        final List<ContrastBolus> currentContrasts = getContrastFromTextResource(scan, rootProject);

        if(currentContrasts.isEmpty()){
            //this is the first time this scan has had a contrast modified.
            //Load the entries from DICOM and modify that.
            currentContrasts.addAll(initializeContrastBeans(scan, user, rootProject));
        }

        return currentContrasts;
    }


    @Override
    public void delete(ContrastBolus contrast, UserI user) throws Exception {
        if(contrast.getScanId() == null || contrast.getPk() == null) {
            //trying to delete an entry that wasn't saved yet
            return;
        }

        final XnatContrastbolusv2 toDelete = XnatContrastbolusv2.getXnatContrastbolusv2sByXnatContrastbolusv2Id(contrast.getPk(),user,true);
        if(toDelete == null){
            //Unable to identify bolus entry to delete
            return;
        }

        if(!Objects.equals(toDelete.getImageScanId(), contrast.getScanId())){
            //Somebody is doing something shady.
            return;
        }

        final XnatImagescandata scan = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(contrast.getScanId(), user, false);
        final XnatImagesessiondata session = XnatImagesessiondata.getXnatImagesessiondatasById(scan.getImageSessionId(), user, false);

        if(scan==null || session==null) {
            throw new UnknownScanException("Unknown scan: "+ contrast.getScanId());
        }

        //event to track contrast modification
        final PersistentWorkflowI wrk = PersistentWorkflowUtils.buildOpenWorkflow(user, session.getItem(), EventUtils.newEventInstance(EventUtils.CATEGORY.DATA,EventUtils.TYPE.REST, "Delete contrast data"));

        //save changes to XFT
        if(session.canEdit(user) && scan.canEdit(user)){
            try {
                SaveItemHelper.authorizedDelete(toDelete.getItem(), user, wrk.buildEvent());
            } catch (Exception e) {
                log.error("Failed to delete contrast entry from Database",e);
                throw e;
            }
        } else {
            throw new UnauthorizedException("User does not have the permission to delete scan:" +
                                                    contrast.getScanId() + " For session" + session.getId());
        }

        //save json to disk
        //retrieve the full set from db and/or DICOM
        //remove the one in question
        //save them to the resource
        try {
            final List<ContrastBolus> currentContrasts = loadContrastWithInit(scan, user, session.getProject());

            //remove entry from JSON
            int i=indexOf(currentContrasts, contrast);

            if(!currentContrasts.isEmpty() && i<currentContrasts.size()){
                //updating an existing record
                currentContrasts.remove(i);
            }

            saveContrastToTextResource(currentContrasts, user, session, scan, wrk.buildEvent());
        } catch (Exception e) {
            log.error("Error deleting contrast on disk", e);
            PersistentWorkflowUtils.fail(wrk, wrk.buildEvent());
            throw e;
        }

        PersistentWorkflowUtils.complete(wrk, wrk.buildEvent());
    }

    private XnatContrastbolusv2 toXFT(ContrastBolus contrast) throws Exception {
        final XnatContrastbolusv2 bolus = new XnatContrastbolusv2();
        bolus.setImageScanId(contrast.getScanId());
        bolus.setAgent(contrast.getAgent());

        return bolus;
    }

    private int indexOf(List<ContrastBolus> contrasts, ContrastBolus contrast) {
        int i=0;
        for(; i<contrasts.size(); i++) {
            if(Objects.equals(contrasts.get(i).getPk(), contrast.getPk())){
                break;
            }
        }
        return i;
    }

    private InputStream writeObjectToInputStream(Object object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue((OutputStream) baos, object);
            baos.flush();

            return new InputStreamResource(new ByteArrayInputStream(baos.toByteArray())).getInputStream();
        }
    }

    private List<ContrastBolus> readObjectFromFile(final File f) throws IOException {
        if(f==null || !f.exists()){
            return new ArrayList<>();
        }

        final ObjectMapper mapper = new ObjectMapper();

        try (FileInputStream fis = new FileInputStream(f)){
            return mapper.readValue(fis, new TypeReference<List<ContrastBolus>>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ContrastBolus> findContrast(final String sessionId, final UserI user) throws Exception {
        final XnatImagesessiondata session = XnatImagesessiondata.getXnatImagesessiondatasById(sessionId, user, false);

        if(! session.canRead(user)){
            throw new UnknownScanException("Unknown session: "+sessionId);
        }

        //first look for a text contrast file
        List<ContrastBolus> contrasts = new ArrayList<>();

        for(final XnatImagescandataI scan: session.getScans_scan()){
            final List<ContrastBolus> scanContrasts = getContrastFromTextResource(scan, session.getProject());

            if(!CollectionUtils.isEmpty(scanContrasts)){
                contrasts.addAll(scanContrasts);
                continue;
            }

            //if nothing on disk, then load it from XFT and DICOM
            contrasts.addAll(initializeContrastBeans(scan, user, session.getProject()));
        }

        return contrasts;
    }

    private List<ContrastBolus> initializeContrastBeans(XnatImagescandataI scan, final UserI user, String rootProject){
        List<ContrastBolus> contrasts = new ArrayList<>();

        //if nothing on disk, then load it from XFT
        contrasts.addAll(loadFromDB(scan, user));

        contrasts = parseFromDICOM(contrasts, scan, rootProject);

        return contrasts;
    }

    private List<ContrastBolus> loadFromDB(final XnatImagescandataI scan, final UserI user){
        final XnatImagescandataI freshScan = XnatImagescandata.getXnatImagescandatasByXnatImagescandataId(scan.getXnatImagescandataId(), user, true);

        final List<ContrastBolus> contrasts = new ArrayList<>();

        for(final XnatContrastbolusv2I contrast: freshScan.getContrasts_contrastbolusv2()){
            final ContrastBolus bolus = new ContrastBolus();
            bolus.setScanId(scan.getXnatImagescandataId());
            bolus.setAgent(contrast.getAgent());
            bolus.setPk(contrast.getXnatContrastbolusv2Id());

            contrasts.add(bolus);
        }

        return contrasts;
    }


    private List<ContrastBolus> parseFromDICOM(final List<ContrastBolus> contrastsFromXML, final XnatImagescandataI scan, final String rootProject) {
        final Optional<File> f;
        try {
            f = CatalogUtils.getFirstMatchingFile(scan.getFile(), DICOM, rootProject, new CatalogUtils.CatEntryFilterI(){
                @Override
                public boolean accept(CatEntryI entry) {
                    return (true);
                }
            });
            if(f.isPresent()){
                //reconcile the contrast found in the DICOM with the ones from the XML (ie database)
                final ContrastParser parser = new ContrastParser(f.get().toPath());
                final List<ContrastBolus> fromDCM = parser.call();
                for(final ContrastBolus contrast: fromDCM){
                     final ContrastBolus matchingContrastFromXML = contrastsFromXML.stream().filter(contrastBolus -> StringUtils.equals(contrastBolus.getAgent(), contrast.getAgent())).findFirst().orElse(null);
                     if(matchingContrastFromXML!=null){
                         contrastsFromXML.remove(matchingContrastFromXML);
                         contrast.setPk(matchingContrastFromXML.getPk());
                         contrast.setScanId(matchingContrastFromXML.getScanId());
                     }
                }

                if(contrastsFromXML.size()>0){
                    //this really shouldn't happen normally.  Only if the original session archive didn't store the contrast.
                    fromDCM.addAll(contrastsFromXML);
                }

                return fromDCM;
            }
        } catch (Exception e) {
            log.error("Falied to parse contrast from DICOM",e);
        }

        return contrastsFromXML;
    }

    private List<ContrastBolus> getContrastFromTextResource(final XnatImagescandataI scan, final String rootProject){
        final List<ContrastBolus> scanContrasts = new ArrayList<>();
        try {
            final Optional<File> f = CatalogUtils.getFirstMatchingFile(scan.getFile(), CONTRAST_J, rootProject, new CatalogUtils.CatEntryFilterI(){
                @Override
                public boolean accept(CatEntryI entry) {
                    return (StringUtils.equals(entry.getName(), CONTRAST_SUMMARY_JSON));
                }
            });
            if(f.isPresent()){
                scanContrasts.addAll(readObjectFromFile(f.get()));
            }
        } catch (Exception e) {
            log.error("Failed to parse contrast elements from archived file.", e);
        }
        return scanContrasts;
    }

    private void saveContrastToTextResource(final List<ContrastBolus> contrastL, final UserI user, final XnatImagesessiondata session, final XnatImagescandata scan, final EventMetaI ci) throws Exception {
        final InputStream in = writeObjectToInputStream(contrastL);
        final XnatResourceInfo info = TriageUtils.buildResourceInfo(user, ArrayListMultimap.create(), ci);
        final String destinationResourcePath = String.format("/archive/experiments/%1$s/scans/%2$s/files",session.getId(), scan.getId());
        final Map<String,Object> props = new HashMap<>();
        props.put(URIManager.ASSESSED_ID,session.getId());
        props.put(URIManager.SCAN_ID,scan.getId());

        final ResourceURII targetResource = new ResourcesExptScanURI(props,destinationResourcePath);
        final ResourceModifierA resourceModifier = TriageUtils.buildResourceModifier(user, targetResource, true,"out",ci);
        resourceModifier.addFile(
                Collections.singletonList(new ArchiveEntryFileWriterWrapper(CONTRAST_SUMMARY_JSON,in.available(),in)),
                CONTRAST_J,
                JSON,
                CONTRAST_SUMMARY_JSON,
                info,
                true);
    }

    public static class UnknownScanException extends Exception{
        public UnknownScanException(String message){
            super(message);
        }
    }
}
