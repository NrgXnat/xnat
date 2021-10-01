/*
 * framework: org.nrg.framework.beans.XnatDataModelBean
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.annotations.XnatDataModel;

import java.util.Properties;

@Value
@Accessors(prefix = "_")
@Builder
@AllArgsConstructor
public class XnatDataModelBean {
    public static final String PLUGIN_DATA_MODEL_PREFIX = "dataModel.";

    public XnatDataModelBean(final String type, final Properties properties) {
        final String     prefix = getPrefix(type);
        final Properties init   = new Properties();
        for (final String property : properties.stringPropertyNames()) {
            if (property.startsWith(prefix)) {
                final String value = properties.getProperty(property);
                init.setProperty(property.substring(prefix.length()), value);
            }
        }
        _type     = type;
        _secured  = Boolean.parseBoolean(init.getProperty(XnatDataModel.DATA_MODEL_SECURED));
        _singular = init.getProperty(XnatDataModel.DATA_MODEL_SINGULAR);
        _plural   = init.getProperty(XnatDataModel.DATA_MODEL_PLURAL);
        _code     = init.getProperty(XnatDataModel.DATA_MODEL_CODE);
    }

    public XnatDataModelBean(final XnatDataModel dataModel) {
        this(dataModel.value(), dataModel.secured(), dataModel.singular(), dataModel.plural(), dataModel.code());
    }

    public Properties asProperties() {
        final String prefix = getPrefix(_type);

        final Properties properties = new Properties();
        properties.setProperty(prefix + "secured", Boolean.toString(_secured));
        if (StringUtils.isNotBlank(_singular)) {
            properties.setProperty(prefix + "singular", _singular);
        }
        if (StringUtils.isNotBlank(_plural)) {
            properties.setProperty(prefix + "plural", _plural);
        }
        if (StringUtils.isNotBlank(_code)) {
            properties.setProperty(prefix + "code", _code);
        }
        return properties;
    }

    private static String getPrefix(final String type) {
        return PLUGIN_DATA_MODEL_PREFIX + type.replace(":", ".") + ".";
    }

    String  _type;
    boolean _secured;
    String  _singular;
    String  _plural;
    String  _code;
}
