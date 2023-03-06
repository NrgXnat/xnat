package org.nrg.xnat.customforms.utils;

public class TypeConversionUtils {

    public static String mapFormioTypeToXnatType(final String formioType) {
        return CustomFormsConstants.FORMIO_TYPE_TO_XNAT_TYPE.getOrDefault(formioType, CustomFormsConstants.DEFAULT_XNAT_TYPE);
    }
}
