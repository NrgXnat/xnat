package org.nrg.xnat.contrast.parser;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;
import org.nrg.xnat.contrast.model.Administration;
import org.nrg.xnat.contrast.model.ContrastBolus;
import org.nrg.xnat.contrast.model.Ingredient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class ContrastParser implements Callable<List<ContrastBolus>> {
    public static final String STRING = "string";
    public static final List<String> enhancedUIDs = Arrays.asList("1.2.840.10008.5.1.4.1.1.4.1", "1.2.840.10008.5.1.4.1.1.12.1.1", "1.2.840.10008.5.1.4.1.1.2.1");

    private final Path _dcmF;

    public ContrastParser(final Path dcmF) {
        _dcmF = dcmF;
    }

    @Override
    public List<ContrastBolus> call() throws Exception {
        return parseContrast(_dcmF);
    }

    private List<ContrastBolus> parseContrast(final Path dcmF) throws IOException {
        Attributes attributes = loadAttributes(dcmF);
        final String classUID = attributes.getString(Tag.SOPClassUID);//0x0008,0016

        final List<ContrastBolus> contrasts;
        if (enhancedUIDs.contains(classUID)) {
            contrasts = parseEnhanced(attributes);
        } else {
            contrasts = parsePlain(attributes);
        }
        return contrasts;
    }

    private Attributes loadAttributes(final Path dcmF) throws IOException {
        try (final DicomInputStream dis = new DicomInputStream(Files.newInputStream(dcmF.toFile().toPath()))) {
            dis.setIncludeBulkData(DicomInputStream.IncludeBulkData.NO);
            return dis.readDataset();
        }
    }

    private List<ContrastBolus> parsePlain(Attributes dcmObj) {
        final List<String> parsedAgents = new ArrayList<>();
        String rootAgent = dcmObj.getString(Tag.ContrastBolusAgent);
        if (StringUtils.isNotBlank(rootAgent)) {
            if (StringUtils.endsWith(rootAgent, "-text")) {
                rootAgent = StringUtils.stripEnd(rootAgent, "-text");
            }
            parsedAgents.add(rootAgent);
        }

        List<String> codeMeanings = parseMultipleCodedMeaning(dcmObj, Tag.ContrastBolusAgentSequence); //0x0018,0012
        if (!codeMeanings.isEmpty()) {
            codeMeanings.forEach(codedMeaning -> {
                if (codedMeaning != null && !parsedAgents.contains(codedMeaning)) {
                    parsedAgents.add(codedMeaning);
                }
            });
        }

        return parsedAgents.stream().map(ContrastBolus::new).collect(Collectors.toList());
    }

    private List<ContrastBolus> parseEnhanced(final Attributes dcmObj) {
        final Sequence seq = dcmObj.getSequence(Tag.ContrastBolusAgentSequence); //0018,0012
        if (CollectionUtils.isEmpty(seq)) {
            return Collections.emptyList();
        }

        final List<String> parsedAgents = new ArrayList<>();
        final List<ContrastBolus> contrasts = new ArrayList<>();
        for (Attributes attrs : seq) {
            final ContrastBolus contrast = new ContrastBolus();
            final String agent = attrs.getString(Tag.CodeMeaning);//0008,0104
            if (agent == null || parsedAgents.contains(agent)) {
                continue;
            }
            parsedAgents.add(agent);
            contrast.setAgent(agent);

            final Attributes adminRoute = seq.getFirst().getNestedDataset(Tag.ContrastBolusAdministrationRouteSequence);
            contrast.setRoute(adminRoute.getString(Tag.CodeMeaning));

            parseMultipleCodedMeaning(attrs, Tag.ContrastBolusIngredientCodeSequence).forEach(s -> processIngredient(contrast, s));//0018,9338

            final Attributes adminSequence = attrs.getNestedDataset(Tag.ContrastAdministrationProfileSequence);//0018,9340
            if (adminSequence != null) {
                processAdministrationProfile(contrast, adminSequence);
            }

            contrast.setConcentration(attrs.getString(Tag.ContrastBolusIngredientConcentration));//0018,1049
            contrast.setPercentageByVolume(attrs.getString(Tag.ContrastBolusIngredientPercentByVolume));//0052,0001
            contrast.setIngredientOpaque(attrs.getString(Tag.ContrastBolusIngredientOpaque));//0018,9425
            contrast.setAgentNumber(attrs.getString(Tag.ContrastBolusAgentNumber));//0018,9337

            contrasts.add(contrast);
        }

        return contrasts;
    }

    private List<String> parseMultipleCodedMeaning(final Attributes parent, final int tag) {
        final Sequence seq = parent.getSequence(tag);
        if (CollectionUtils.isEmpty(seq)) {
            return Collections.emptyList();
        }

        final List<String> codedMeanings = new ArrayList<>();
        for (Attributes obj : seq) {
            final String meaning = obj.getString(Tag.CodeMeaning);
            if (meaning != null) {
                codedMeanings.add(meaning);
            }
        }
        return codedMeanings;
    }

    private void processIngredient(final ContrastBolus contrastBean, final String ingredient) {
        if (StringUtils.isNotBlank(ingredient)) {
            contrastBean.addIngredient(new Ingredient(ingredient));
        }
    }

    private void processAdministrationProfile(final ContrastBolus contrastBean, final Attributes dcm) {
        final Administration adminBean = new Administration();
        adminBean.setVolume(dcm.getString(Tag.ContrastBolusVolume));
        adminBean.setStartTime(dcm.getString(Tag.ContrastBolusStartTime));
        adminBean.setEndTime(dcm.getString(Tag.ContrastBolusStopTime));
        adminBean.setFlowRate(dcm.getString(Tag.ContrastFlowRate));
        adminBean.setDuration(dcm.getString(Tag.ContrastFlowDuration));
        contrastBean.addAdministration(adminBean);
    }
}
