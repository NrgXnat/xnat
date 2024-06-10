package org.nrg.xnat.customforms.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.nrg.xnat.features.CustomFormsFeatureFlags;

@Data
@AllArgsConstructor
public class XnatFormsIOEnv {
    private boolean siteHasProtocolsPluginDeployed;
    private CustomFormsFeatureFlags features;
}
