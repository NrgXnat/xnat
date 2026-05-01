package org.nrg.xdat.preferences;

import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;

public enum HandlePetMr {
    Default,
    Separate,
    Pet,
    PetMr;

    public static final String       SEPARATE_PET_MR_ALT     = "separatePetMr";
    public static final String       SEPARATE_PET_MR         = "separatePETMR";
    public static final String       SEPARATE                = "separate";
    public static final String       PETMR                   = "petmr";
    public static final String       PET                     = "pet";
    public static final String       CONFIG                  = "config";
    public static final String       PREARCHIVE_PATH         = "prearchivePath";
    public static final String       PARAM_SOURCE            = "SOURCE";
    public static final List<String> DEFAULT_EXCLUDED_FIELDS = Arrays.asList(PARAM_SOURCE, SEPARATE_PET_MR, SEPARATE_PET_MR_ALT, PREARCHIVE_PATH);

    public static HandlePetMr get(final String value) {
        if (StringUtils.isBlank(value)) {
            return Default;
        }
        final String key = StringUtils.deleteWhitespace(StringUtils.lowerCase(value));
        switch (key) {
            case SEPARATE:
                return Separate;
            case PET:
                return Pet;
            case PETMR:
                return PetMr;
            default:
                throw new RuntimeException("Unknown PET/MR setting " + key);
        }
    }

    public static HandlePetMr getSeparatePetMr() {
        return HandlePetMr.get(XDAT.getSiteConfigPreferences().getSitewidePetMr());
    }

    public static HandlePetMr getSeparatePetMr(final String project) {
        if (StringUtils.isBlank(project) || StringUtils.equals(UNASSIGNED, project)) {
            return getSeparatePetMr();
        }
        return HandlePetMr.get(Optional.ofNullable(XDAT.getConfigValue(project, SEPARATE_PET_MR, CONFIG, false)).orElseGet(() -> XDAT.getSiteConfigPreferences().getSitewidePetMr()));
    }

    public static boolean shouldSeparatePetMr() {
        return getSeparatePetMr() == HandlePetMr.Separate;
    }

    public static boolean shouldSeparatePetMr(final String project) {
        return getSeparatePetMr(project) == HandlePetMr.Separate;
    }

    public static HandlePetMr getParameter(final Map<String, ?> parameters) {
        return get(parameters.containsKey(HandlePetMr.SEPARATE_PET_MR)
                   ? (String) parameters.get(HandlePetMr.SEPARATE_PET_MR)
                   : (parameters.containsKey(HandlePetMr.SEPARATE_PET_MR_ALT) ? (String) parameters.get(HandlePetMr.SEPARATE_PET_MR_ALT) : ""));
    }

    public String value() {
        return this == Default ? "" : StringUtils.lowerCase(name());
    }
}
