package org.nrg.xnat.contrast.parser;

import org.apache.commons.lang.StringUtils;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomInputStream;
import org.nrg.xnat.contrast.model.Administration;
import org.nrg.xnat.contrast.model.ContrastBolus;
import org.nrg.xnat.contrast.model.Ingredient;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class ContrastParser implements Callable<List<ContrastBolus>> {
    public static final String STRING = "string";
    public static final List<String> enhancedUIDs = Arrays.asList("1.2.840.10008.5.1.4.1.1.4.1","1.2.840.10008.5.1.4.1.1.12.1.1", "1.2.840.10008.5.1.4.1.1.2.1");

    Path _dcmF = null;

    public ContrastParser(final Path dcmF) {
        _dcmF = dcmF;
    }

    @Override
    public List<ContrastBolus> call() throws Exception {
        return parseContrast(_dcmF);
    }

    private List<ContrastBolus> parseContrast(final Path dcmF) throws IOException {
        DicomInputStream din = null;
        try {
            din = new DicomInputStream(dcmF.toFile());

            final DicomObject dcmObj = din.readDicomObject();

            final String classUID=dcmObj.getString(org.dcm4che2.data.Tag.SOPClassUID);

            final List<ContrastBolus> contrasts;
            if(enhancedUIDs.contains(classUID)){
                contrasts = parseEnhanced(dcmObj);
            }else{
                contrasts = parsePlain(dcmObj);
            }
            return contrasts;
        }
        finally {
            try { din.close(); } catch (Throwable ignore) {}
        }
    }

    private List<ContrastBolus> parsePlain(DicomObject dcmObj) {
        final List<String> parsedAgents = new ArrayList<>();
        String rootAgent = dcmObj.getString(Tag.ContrastBolusAgent);
        if(rootAgent!=null){
            if(StringUtils.endsWith(rootAgent,"-text")){
                rootAgent = StringUtils.stripEnd(rootAgent,"-text");
            }
            parsedAgents.add(rootAgent);
        }

        List<String> codeMeanings = parseMultipleCodedMeaning(dcmObj,Tag.ContrastBolusAgentSequence);
        if(codeMeanings!=null && !codeMeanings.isEmpty()){
            codeMeanings.forEach(codedMeaning -> {
                if(codedMeaning !=null && !parsedAgents.contains(codedMeaning)){
                    parsedAgents.add(codedMeaning);
                }
            });
        }

        List<ContrastBolus> contrasts = new ArrayList<>();
        parsedAgents.stream().forEach(agent -> {
            ContrastBolus contrast = new ContrastBolus();
            contrast.setAgent(agent);
            contrasts.add(contrast);
        });

        return contrasts;
    }

    private List<ContrastBolus> parseEnhanced(final DicomObject dcmObj) {
        final DicomElement sequenceElement = dcmObj.get(Tag.ContrastBolusAgentSequence);
        final List<String> parsedAgents = new ArrayList<>();
        final List<ContrastBolus> contrasts = new ArrayList<>();

        if (sequenceElement!=null && sequenceElement.hasItems()) {
            for (int i = 0, n = sequenceElement.countItems(); i < n; ++i) {
                final DicomObject seq = sequenceElement.getDicomObject(i);

                final ContrastBolus contrast = new ContrastBolus();
                final String agent = seq.getString(Tag.CodeMeaning);
                if(agent!=null && !parsedAgents.contains(agent)){
                    parsedAgents.add(agent);
                    contrast.setAgent(agent);

                    final String embeddedRoute = parseSingularCodedMeaning(dcmObj, Tag.ContrastBolusAdministrationRouteSequence);
                    contrast.setRoute((embeddedRoute != null)?embeddedRoute:dcmObj.getString(Tag.ContrastBolusRoute));

                    parseMultipleCodedMeaning(seq, Tag.ContrastBolusIngredientCodeSequence).forEach(s -> processIngredient(contrast,s));

                    final DicomObject adminRoute = seq.getNestedDicomObject(Tag.ContrastAdministrationProfileSequence);
                    if(adminRoute != null){
                        processAdministrationProfile(contrast, adminRoute);
                    }

                    contrast.setConcentration(seq.getString(Tag.ContrastBolusIngredientConcentration));
                    contrast.setPercentageByVolume(seq.getString(Tag.ContrastBolusIngredientPercentByVolume));
                    contrast.setIngredientOpaque(seq.getString(Tag.ContrastBolusIngredientOpaque));
                    contrast.setAgentNumber(seq.getString(Tag.ContrastBolusAgentNumber));

                    contrasts.add(contrast);
                }
            }
        }

        return contrasts;
    }

    private String parseSingularCodedMeaning(final DicomObject parent, final int tag) {
        final DicomObject codedSequence = parent.getNestedDicomObject(tag);
        if (codedSequence == null) {
            return null;
        } else {
            return parent.getString(Tag.CodeMeaning);
        }
    }

    private List<String> parseMultipleCodedMeaning(final DicomObject parent, final int tag) {
        final DicomElement sequenceElement = parent.get(tag);
        if (sequenceElement == null) {
            return null;
        } else {
            final List<String> codedMeanings = new ArrayList<String>();
            if (sequenceElement.hasItems()) {
                for (int i = 0, n = sequenceElement.countItems(); i < n; ++i) {
                    final DicomObject obj = sequenceElement.getDicomObject(i);
                    final String meaning = obj.getString(Tag.CodeMeaning);
                    if(meaning != null){
                        codedMeanings.add(meaning);
                    }
                }
            }
            return codedMeanings;
        }
    }

    private void processIngredient(final ContrastBolus contrastBean, final String ingredient){
        if(ingredient !=null){
            Ingredient ingred = new Ingredient();
            ingred.setName(ingredient);
            contrastBean.addIngredient(ingred);
        }
    }

    private void processAdministrationProfile(final ContrastBolus contrastBean, final DicomObject dcm){
        final Administration adminBean = new Administration();

        adminBean.setVolume(dcm.getString(Tag.ContrastBolusVolume));
        adminBean.setStartTime(dcm.getString(Tag.ContrastBolusStartTime));
        adminBean.setEndTime(dcm.getString(Tag.ContrastBolusStopTime));
        adminBean.setFlowRate(dcm.getString(Tag.ContrastFlowRate));
        adminBean.setDuration(dcm.getString(Tag.ContrastFlowDuration));

        contrastBean.addAdministration(adminBean);
    }
}
