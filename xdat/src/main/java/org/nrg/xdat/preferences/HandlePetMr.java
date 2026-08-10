package org.nrg.xdat.preferences;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.nrg.xft.utils.predicates.ProjectAccessPredicate.UNASSIGNED;

@Slf4j
public enum HandlePetMr {
    Default,
    Pet,
    PetMr;

    /**
     * The names of the project configuration and request parameter keys that carry the PET/MR handling setting. These
     * are historical names from when this setting could also separate a PET/MR session into distinct PET and MR
     * sessions. That behavior is no longer supported, but the keys are retained as-is because they identify stored
     * project configurations and request parameters that predate its removal.
     */
    public static final String       SEPARATE_PET_MR_ALT     = "separatePetMr";
    public static final String       SEPARATE_PET_MR         = "separatePETMR";

    /**
     * The retired setting value that requested separation into distinct PET and MR sessions. Retained only so that
     * {@link #get(String)} can recognize and remap stored values instead of failing on them.
     */
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
                // Separating PET/MR sessions into separate PET and MR sessions is no longer supported. Sites and
                // projects that still have this value stored are treated as if they were set to create a PET/MR
                // session, which is the closest surviving behavior.
                log.warn("Found the unsupported PET/MR setting \"{}\". Separating PET/MR sessions into separate PET and MR sessions is no longer supported, so this is being handled as \"{}\" instead.", SEPARATE, PETMR);
                return PetMr;
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

    public static HandlePetMr getParameter(final Map<String, ?> parameters) {
        return get(parameters.containsKey(HandlePetMr.SEPARATE_PET_MR)
                   ? (String) parameters.get(HandlePetMr.SEPARATE_PET_MR)
                   : (parameters.containsKey(HandlePetMr.SEPARATE_PET_MR_ALT) ? (String) parameters.get(HandlePetMr.SEPARATE_PET_MR_ALT) : ""));
    }

    public String value() {
        return this == Default ? "" : StringUtils.lowerCase(name());
    }
}
