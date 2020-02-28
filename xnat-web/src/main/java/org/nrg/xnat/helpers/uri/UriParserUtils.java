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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.om.*;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.FieldNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.nrg.xnat.helpers.uri.URIManager.DataURIA;
import org.restlet.util.Template;
import org.restlet.util.Variable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.*;

@Slf4j
public final class UriParserUtils {
    public static final String _REMAINDER = "_REMAINDER";

    public static URIManager.DataURIA parseURI(final String passedDataUri) throws MalformedURLException {
        if (StringUtils.isBlank(passedDataUri)) {
            return null;
        }
        final String dataUri;
        if (passedDataUri.startsWith("/data")) {
            dataUri = passedDataUri.substring(5);
        } else if (passedDataUri.startsWith("/REST")) {
            dataUri = passedDataUri.substring(5);
        } else {
            dataUri = passedDataUri;
        }

        log.info("Parsing data URI: {}", dataUri);

        if (dataUri.startsWith("/prearchive")) {
            if (dataUri.equals("/prearchive")) {
                log.debug("Got an empty data URI '/prearchive' from the passed data URI {}", passedDataUri);
                return new URIManager.PrearchiveURI(Collections.<String, Object>emptyMap(), dataUri);
            }

            for (final URIManager.TemplateInfo template : URIManager.getTemplates(URIManager.TEMPLATE_TYPE.PREARC)) {
                final URIManager.DataURIA uri = mapTemplateInfo(template, dataUri);
                if (uri != null) {
                    return uri;
                }
            }
        } else if (dataUri.startsWith("/archive")) {
            if (dataUri.equals("/archive")) {
                log.debug("Got an empty data URI '/archive' from the passed data URI {}", passedDataUri);
                return new URIManager.ArchiveURI(Collections.<String, Object>emptyMap(), dataUri);
            }

            for (final URIManager.TemplateInfo template : URIManager.getTemplates(URIManager.TEMPLATE_TYPE.ARC)) {
                final URIManager.DataURIA uri = mapTemplateInfo(template, dataUri);
                if (uri != null) {
                    return uri;
                }
            }
        } else if (dataUri.startsWith("/user")) {
            if (dataUri.equals("/user")) {
                log.debug("Got an empty data URI '/user' from the passed data URI {}", passedDataUri);
                return new URIManager.UserCacheURI(Collections.<String, Object>emptyMap(), dataUri);
            }

            for (final URIManager.TemplateInfo template : URIManager.getTemplates(URIManager.TEMPLATE_TYPE.CACHE)) {
                final URIManager.DataURIA uri = mapTemplateInfo(template, dataUri);
                if (uri != null) {
                    return uri;
                }
            }
        } else if(dataUri.startsWith("/services/triage")){
			if(dataUri.equals("/services/triage")){
				final Map<String,Object> t=Collections.emptyMap();
				return new URIManager.TriageURI(t,dataUri);
			}
			
			for(final URIManager.TemplateInfo template: URIManager.getTemplates(URIManager.TEMPLATE_TYPE.TRIAGE)){
				Map<String,Object> map=new UriParser(template.key,template.MODE).readUri(dataUri);
				if(map.size()>0){
					return template.wrap(map,dataUri);
				}
			}
			
		} else {
        	// Parse Custom Plugin URIs
        	Collection<ManageableXnatURIContainer> containers = XDAT.getContextService().getBeansOfType(ManageableXnatURIContainer.class).values();
        	for (ManageableXnatURIContainer uriContainer : containers) {
        		if(dataUri.startsWith(uriContainer.getBaseTemplate())) {
        			if(dataUri.equals(uriContainer.getBaseTemplate())) {
        				try {
        					final Map<String,Object> t=Collections.emptyMap();
        					Constructor<? extends DataURIA> uriConstructor = uriContainer.getUri().getConstructor(Map.class,String.class);
        					return uriConstructor.newInstance(t,dataUri);
        				} catch (Exception e) {
        					log.error("Unable to create URI Class " + uriContainer.getClass().getName(),e);
        					throw new MalformedURLException();
        				}
        			}
        			
        			for(final URIManager.TemplateInfo template: URIManager.getTemplates(uriContainer.getTemplateType())){
        				Map<String,Object> map=new UriParser(template.key,template.MODE).readUri(dataUri);
        				if(map.size()>0){
        					return template.wrap(map,dataUri);
        				}
        			}
        		}
        	}
        }

        log.warn("No valid data URI format was found for the data URI '{}'", dataUri);
        throw new MalformedURLException();
    }

    public static String getArchiveUri(final Object... objects) {
        final Map<String, String> parameters = new HashMap<>();
        for (final Object object : objects) {
            final Class<?> objectClass = object.getClass();
            if (XFTItem.class.isAssignableFrom(objectClass)) {
                log.debug("Found an XFTItem object");
                final XFTItem item = (XFTItem) object;
                try {
                    final String itemId = getItemId(item);
                    if (item.instanceOf(XnatProjectdata.SCHEMA_ELEMENT_NAME)) {
                        log.debug("Found an XFTItem object of type xnat:projectData with ID '{}'", itemId);
                        parameters.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, itemId);
                    } else if (item.instanceOf(XnatSubjectdata.SCHEMA_ELEMENT_NAME)) {
                        log.debug("Found an XFTItem object of type xnat:subjectData with ID '{}'", itemId);
                        parameters.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, itemId);
                    } else if (item.instanceOf(XnatDeriveddata.SCHEMA_ELEMENT_NAME)) {
                        log.debug("Found an XFTItem object of type xnat:derivedData (actually {}) with ID '{}'", item.getXSIType(), itemId);
                        parameters.put(XnatDeriveddata.SCHEMA_ELEMENT_NAME, itemId);
                    } else if (item.instanceOf(XnatExperimentdata.SCHEMA_ELEMENT_NAME)) {
                        log.debug("Found an XFTItem object of type xnat:experimentData (actually {}) with ID '{}'", item.getXSIType(), itemId);
                        parameters.put(XnatExperimentdata.SCHEMA_ELEMENT_NAME, itemId);
                    } else if (item.instanceOf(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME)) {
                        log.debug("Found an XFTItem object of type xnat:xnat:reconstructedImageData (actually {}) with ID '{}'", item.getXSIType(), itemId);
                        parameters.put(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME, itemId);
                    } else if (item.instanceOf(XnatImagescandata.SCHEMA_ELEMENT_NAME)) {
                        log.debug("Found an XFTItem object of type xnat:xnat:imageScanData (actually {}) with ID '{}'", item.getXSIType(), itemId);
                        parameters.put(XnatImagescandata.SCHEMA_ELEMENT_NAME, itemId);
                        parameters.put(XnatExperimentdata.SCHEMA_ELEMENT_NAME, item.getStringProperty(XnatImagescandata.SCHEMA_ELEMENT_NAME + "/image_session_ID"));
                    } else {
                        log.error("Found XFTItem of type {} with ID '{}', I don't know how to deal with this in the current context, so I'm ignoring it.", item.getXSIType(), itemId);
                    }
                } catch (XFTInitException | ElementNotFoundException | FieldNotFoundException e) {
                    log.error("An error occurred trying to retrieve the ID value for the XFT item of type {} with primary key: {}", ((XFTItem) object).getXSIType(), ((XFTItem) object).getPK().toString());
                }
            } else if (XnatProjectdata.class.isAssignableFrom(objectClass)) {
                final String id = ((XnatProjectdata) object).getId();
                log.debug("Found an XnatProjectdata object with ID '{}'", id);
                parameters.put(XnatProjectdata.SCHEMA_ELEMENT_NAME, id);
            } else if (XnatSubjectdata.class.isAssignableFrom(objectClass)) {
                final String id = ((XnatSubjectdata) object).getId();
                log.debug("Found an XnatSubjectdata object with ID '{}'", id);
                parameters.put(XnatSubjectdata.SCHEMA_ELEMENT_NAME, id);
            } else if (XnatDeriveddata.class.isAssignableFrom(objectClass)) {
                final String id = ((XnatDeriveddata) object).getId();
                log.debug("Found an XnatDeriveddata object (actually {}) with ID '{}'", objectClass.getName(), id);
                parameters.put(XnatDeriveddata.SCHEMA_ELEMENT_NAME, id);
            } else if (XnatExperimentdata.class.isAssignableFrom(objectClass)) {
                final String id = ((XnatExperimentdata) object).getId();
                log.debug("Found an XnatExperimentdata object (actually {}) with ID '{}'", objectClass.getName(), id);
                parameters.put(XnatExperimentdata.SCHEMA_ELEMENT_NAME, id);
            } else if (XnatReconstructedimagedata.class.isAssignableFrom(objectClass)) {
                final String id = ((XnatReconstructedimagedata) object).getId();
                log.debug("Found an XnatReconstructedimagedata object with ID '{}'", id);
                parameters.put(XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME, id);
            } else if (XnatImagescandata.class.isAssignableFrom(objectClass)) {
                XnatImagescandata scan = (XnatImagescandata) object;
                final String id = scan.getId();
                log.debug("Found an XnatImagescandata object (actually {}) with ID '{}'", objectClass.getName(), id);
                parameters.put(XnatImagescandata.SCHEMA_ELEMENT_NAME, id);
                parameters.put(XnatExperimentdata.SCHEMA_ELEMENT_NAME, scan.getImageSessionId());
            } else if (object instanceof String) {
                final String value = (String) object;
                final String name  = StringUtils.equalsAny(value, "in", "out") ? "type" : "xname";
                log.debug("Found a string '{}' with value '{}'", name, value);
                parameters.put(name, value);
            }
        }
        return getArchiveUriFromParameterTypes(parameters);
    }

    public static String getArchiveUriFromParameterTypes(final Map<String, String> parameters) {
        final String[] types  = parameters.keySet().toArray(new String[0]);
        final String   format = XSI_ARCHIVE_FORMATS.get(getTypeList(types));
        return StringSubstitutor.replace(format, parameters);
    }

    /**
     * A base parser that reads a uri using the given template.
     *
     * @author aditya
     */
    public static class UriParser implements UriParserI<Map<String, Object>> {
        String template;
        int    mode = Template.MODE_STARTS_WITH;

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
            final Template            t       = new Template(template, mode, Variable.TYPE_URI_SEGMENT, "", true, false);
            final Map<String, Object> so      = new HashMap<>();
            final int                 matched = t.parse(uri, so);
            if (matched > -1 && matched < uri.length()) {
                so.put(_REMAINDER, uri.substring(matched));
            }
            return so;
        }
    }

    private static String getItemId(final XFTItem item) throws XFTInitException, ElementNotFoundException {
        if (item == null) {
            return null;
        }

        try {
            return StringUtils.defaultIfBlank(item.getIDValue(), item.getStringProperty("ID"));
        } catch (FieldNotFoundException ignored) {
            // We have a fallback...
        }
        return null;
    }

    private static List<String> getTypeList(final String[] types) {
        final List<String> list = Arrays.asList(types);
        Collections.sort(list, TYPE_COMPARATOR);
        return list;
    }

    private static URIManager.DataURIA mapTemplateInfo(final URIManager.TemplateInfo template, final String dataUri) {
        final Map<String, Object> map = new UriParser(template.key, template.MODE).readUri(dataUri);
        if (map.size() > 0) {
            log.debug("Found {} parameters from the data URI {}: {}", map.size(), dataUri, map);
            return template.wrap(map, dataUri);
        }
        log.debug("Found no parameters from the data URI {}", dataUri);
        return null;
    }

    private static final Comparator<String> TYPE_COMPARATOR = new Comparator<String>() {
        @Override
        public int compare(final String first, final String second) {
            return getRank(first).compareTo(getRank(second));
        }

        private Integer getRank(final String type) {
            final int index = TYPE_RANKS.indexOf(type);
            return index >= 0 ? index : TYPE_RANKS.size();
        }
    };

    private static final List<String>              TYPE_RANKS          = Arrays.asList(XnatProjectdata.SCHEMA_ELEMENT_NAME, XnatSubjectdata.SCHEMA_ELEMENT_NAME, XnatExperimentdata.SCHEMA_ELEMENT_NAME, XnatDeriveddata.SCHEMA_ELEMENT_NAME, XnatImagescandata.SCHEMA_ELEMENT_NAME, XnatReconstructedimagedata.SCHEMA_ELEMENT_NAME, "type", "xname");
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
