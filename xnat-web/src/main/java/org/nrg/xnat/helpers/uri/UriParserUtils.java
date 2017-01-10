/*
 * web: org.nrg.xnat.helpers.uri.UriParserUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.uri;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.nrg.xdat.om.*;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.restlet.util.Template;
import org.restlet.util.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.*;

public final class UriParserUtils {
    public static final String _REMAINDER = "_REMAINDER";

    public static URIManager.DataURIA parseURI(String s) throws MalformedURLException {
        if (s.startsWith("/data")) {
            s = s.substring(5);
        } else if (s.startsWith("/REST")) {
            s = s.substring(5);
        }

        if (s.startsWith("/prearchive")) {
            if (s.equals("/prearchive")) {
                final Map<String, Object> t = Collections.emptyMap();
                return new URIManager.PrearchiveURI(t, s);
            }

            for (final URIManager.TemplateInfo template : URIManager.getTemplates(URIManager.TEMPLATE_TYPE.PREARC)) {
                Map<String, Object> map = new UriParser(template.key, template.MODE).readUri(s);
                if (map.size() > 0) {
                    return template.wrap(map, s);
                }
            }
        } else if (s.startsWith("/archive")) {
            if (s.equals("/archive")) {
                final Map<String, Object> t = Collections.emptyMap();
                return new URIManager.ArchiveURI(t, s);
            }

            for (final URIManager.TemplateInfo template : URIManager.getTemplates(URIManager.TEMPLATE_TYPE.ARC)) {
                Map<String, Object> map = new UriParser(template.key, template.MODE).readUri(s);
                if (map.size() > 0) {
                    return template.wrap(map, s);
                }
            }

        } else if (s.startsWith("/user")) {
            if (s.equals("/user")) {
                final Map<String, Object> t = Collections.emptyMap();
                return new URIManager.UserCacheURI(t, s);
            }

            for (final URIManager.TemplateInfo template : URIManager.getTemplates(URIManager.TEMPLATE_TYPE.CACHE)) {
                Map<String, Object> map = new UriParser(template.key, template.MODE).readUri(s);
                if (map.size() > 0) {
                    return template.wrap(map, s);
                }
            }

        }

        throw new MalformedURLException();
    }

    public static String getArchiveUri(final Object... objects) {
        final Map<String, String> parameters = Maps.newHashMap();
        for (final Object object : objects) {
            final Class<?> objectClass = object.getClass();
            if (XFTItem.class.isAssignableFrom(objectClass)) {
                final XFTItem item = (XFTItem) object;
                try {
                    if (item.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                        parameters.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, item.getIDValue());
                    } else if (item.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
                        parameters.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, item.getIDValue());
                    } else if (item.instanceOf(XnatDeriveddata.SCHEMA_ELEMENT_NAME)) {
                        parameters.put(XnatDeriveddata.SCHEMA_ELEMENT_NAME, item.getIDValue());
                    } else if (item.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
                        parameters.put(XnatExperimentdata.SCHEMA_ELEMENT_NAME, item.getIDValue());
                    } else if (item.instanceOf(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME)) {
                        parameters.put(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME, item.getIDValue());
                    } else if (item.instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME)) {
                        parameters.put(XnatImagescandata.SCHEMA_ELEMENT_NAME, item.getIDValue());
                    } else {
                        logger.error("Found XFTItem of type " + item.getXSIType() + ", I don't know how to deal with this in the current context, so ignoring.");
                    }
                } catch (XFTInitException | ElementNotFoundException e) {
                    logger.error("An error occurred trying to retrieve the ID value for the XFT item of type " + ((XFTItem) object).getXSIType() + " primary key: " + ((XFTItem) object).getPK().toString());
                }
            } else if (XnatProjectdata.class.isAssignableFrom(objectClass)) {
                parameters.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, ((XnatProjectdata) object).getId());
            } else if (XnatSubjectdata.class.isAssignableFrom(objectClass)) {
                parameters.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, ((XnatSubjectdata) object).getId());
            } else if (XnatDeriveddata.class.isAssignableFrom(objectClass)) {
                parameters.put(XnatDeriveddata.SCHEMA_ELEMENT_NAME, ((XnatDeriveddata) object).getId());
            } else if (XnatExperimentdata.class.isAssignableFrom(objectClass)) {
                parameters.put(XnatExperimentdata.SCHEMA_ELEMENT_NAME, ((XnatExperimentdata) object).getId());
            } else if (XnatReconstructedimagedata.class.isAssignableFrom(objectClass)) {
                parameters.put(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME, ((XnatReconstructedimagedata) object).getId());
            } else if (XnatImagescandata.class.isAssignableFrom(objectClass)) {
                parameters.put(XnatImagescandata.SCHEMA_ELEMENT_NAME, ((XnatImagescandata) object).getId());
            } else if (object instanceof String) {
                final String string = (String) object;
                parameters.put(StringUtils.equals("in", string) || StringUtils.equals("out", string) ? "type" : "xname", string);
            }
        }
        return getArchiveUriFromParameterTypes(parameters);
    }

    public static String getArchiveUriFromParameterTypes(final Map<String, String> parameters) {
        final String[] types = parameters.keySet().toArray(new String[parameters.size()]);
        final String format = XSI_ARCHIVE_FORMATS.get(getTypeList(types));
        return StrSubstitutor.replace(format, parameters);
    }

    /**
     * A base parser that reads a uri using the given template.
     *
     * @author aditya
     */
    public static class UriParser implements UriParserI<Map<String, Object>> {
        String template;
        int mode = Template.MODE_STARTS_WITH;

        @SuppressWarnings("unused")
        UriParser(String template) {
            this.template = template;
        }

        public UriParser(String template, int mode) {
            this.template = template;
            this.mode = mode;
        }

        /**
         * Parse the uri with the given template. No errors are thrown
         * for fields, instead they are set to null. Users of this object beware.
         */
        public Map<String, Object> readUri(String uri) {
            final Template t = new Template(template, mode, Variable.TYPE_URI_SEGMENT, "", true, false);
            final Map<String, Object> so = new HashMap<>();
            final int matched = t.parse(uri, so);
            if (matched > -1 && matched < uri.length()) {
                so.put(_REMAINDER, uri.substring(matched));
            }
            return so;
        }
    }

    private static List<String> getTypeList(final String[] types) {
        final List<String> list = Arrays.asList(types);
        Collections.sort(list, TYPE_COMPARATOR);
        return list;
    }

    private static final Logger logger = LoggerFactory.getLogger(UriParserUtils.class);

    private static final Comparator<String> TYPE_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(final String first, final String second) {
            return getRank(first).compareTo(getRank(second));
        }

        private Integer getRank(final String type) {
            if (StringUtils.equals(XnatProjectdata.SCHEMA_ELEMENT_NAME, type)) {
                return 0;
            } else if (StringUtils.equals(XnatSubjectdata.SCHEMA_ELEMENT_NAME, type)) {
                return 1;
            } else if (StringUtils.equals(XnatExperimentdata.SCHEMA_ELEMENT_NAME, type)) {
                return 2;
            } else if (StringUtils.equals(XnatDeriveddata.SCHEMA_ELEMENT_NAME, type)) {
                return 3;
            } else if (StringUtils.equals(XnatImagescandata.SCHEMA_ELEMENT_NAME, type)) {
                return 4;
            } else if (StringUtils.equals(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME, type)) {
                return 5;
            } else if (StringUtils.equals("type", type)) {
                return 6;
            } else if (StringUtils.equals("xname", type)) {
                return 7;
            } else {
                return 8;
            }
        }
    };

    private static final Map<List<String>, String> XSI_ARCHIVE_FORMATS = ImmutableMap.copyOf(new HashMap<List<String>, String>() {{
        put(ImmutableList.of("xnat:derivedData"), "/archive/experiments/${xnat:derivedData}");
        put(ImmutableList.of("xnat:experimentData"), "/archive/experiments/${xnat:experimentData}");
        put(ImmutableList.of("xnat:experimentData", "xname"), "/archive/experiments/${xnat:experimentData}/resources/${xname}");
        put(ImmutableList.of("xnat:experimentData", "xnat:derivedData"), "/archive/experiments/${xnat:experimentData}/assessors/${xnat:derivedData}");
        put(ImmutableList.of("xnat:experimentData", "xnat:derivedData", "type", "xname"), "/archive/experiments/${xnat:experimentData}/assessors/${xnat:derivedData}/${type}/resources/${xname}");
        put(ImmutableList.of("xnat:experimentData", "xnat:imageScanData"), "/archive/experiments/${xnat:experimentData}/scans/${xnat:imageScanData}");
        put(ImmutableList.of("xnat:experimentData", "xnat:imageScanData", "xname"), "/archive/experiments/${xnat:experimentData}/scans/${xnat:imageScanData}/resources/${xname}");
        put(ImmutableList.of("xnat:experimentData", "xnat:reconstructedImageData"), "/archive/experiments/${xnat:experimentData}/reconstructions/${xnat:reconstructedImageData}");
        put(ImmutableList.of("xnat:experimentData", "xnat:reconstructedImageData", "type", "xname"), "/archive/experiments/${xnat:experimentData}/reconstructions/${xnat:reconstructedImageData}/${type}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData"), "/archive/projects/${xnat:projectData}");
        put(ImmutableList.of("xnat:projectData", "xname"), "/archive/projects/${xnat:projectData}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData", "xnat:experimentData"), "/archive/projects/${xnat:projectData}/experiments/${xnat:experimentData}");
        put(ImmutableList.of("xnat:projectData", "xnat:experimentData", "xname"), "/archive/projects/${xnat:projectData}/experiments/${xnat:experimentData}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xname"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xname"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:derivedData"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/assessors/${xnat:derivedData}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:derivedData", "type", "xname"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/assessors/${xnat:derivedData}/${type}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:imageScanData"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/scans/${xnat:imageScanData}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:imageScanData", "xname"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/scans/${xnat:imageScanData}/resources/${xname}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:reconstructedImageData"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/reconstructions/${xnat:reconstructedImageData}");
        put(ImmutableList.of("xnat:projectData", "xnat:subjectData", "xnat:experimentData", "xnat:reconstructedImageData", "type", "xname"), "/archive/projects/${xnat:projectData}/subjects/${xnat:subjectData}/experiments/${xnat:experimentData}/reconstructions/${xnat:reconstructedImageData}/${type}/resources/${xname}");
        put(ImmutableList.of("xnat:subjectData"), "/archive/subjects/${xnat:subjectData}");
        put(ImmutableList.of("xnat:subjectData", "xname"), "/archive/subjects/${xnat:subjectData}/resources/${xname}");
    }});
}
