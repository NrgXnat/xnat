package org.nrg.resources;

import lombok.extern.slf4j.Slf4j;
import org.nrg.framework.services.ContextService;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xdat.model.XnatAbstractresourceI;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatResourcecatalog;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
public class SupplementalResourceBuilderUtils {
    public static void addSupplementalResourcesToXml(Collection<File> xmls, Collection<File> dirs) {
        ContextService cs;
        try {
            cs = XDAT.getContextService();
        } catch (Exception e) {
            log.error("Unable get context service, spring injection will be skipped", e);
            return;
        }

        List<SupplementalResourceBuilder> builders = null;
        try {
            Map<String, SupplementalResourceBuilder> factoryMap = cs.getBeansOfType(SupplementalResourceBuilder.class);
            if (factoryMap != null) {
                builders = new ArrayList<>(factoryMap.values());
            }
        } catch (Exception e) {
            log.error("Unable to retrieve injected SupplementalResourceBuilder beans", e);
            return;
        }

        if (builders == null || builders.isEmpty()) {
            log.trace("No SupplementalResourceBuilder beans");
            return;
        }

        // We expect that sessionBeanFactories are going to be exclusive - we don't control the priority/order
        for (File xml : xmls) {
            XnatImagesessiondataBean sessionBean;
            try (FileInputStream fis = new FileInputStream(xml)) {
                XDATXMLReader reader = new XDATXMLReader();
                BaseElement base = reader.parse(fis);
                if (!(base instanceof XnatImagesessiondataBean)) {
                    log.warn("Not a session xml {}", xml);
                    continue;
                }
                sessionBean = (XnatImagesessiondataBean) base;
            } catch (IOException | SAXException e) {
                log.error("Unable to parse xml {}", xml, e);
                continue;
            }

            List<String> excluded = getReferencedFiles(sessionBean);
            boolean modified = false;
            for (SupplementalResourceBuilder b : builders) {
                try {
                    List<String> added = b.addToSession(sessionBean, dirs, excluded);
                    if (added != null && !added.isEmpty()) {
                        modified = true;
                        excluded.addAll(added);
                    }
                } catch (SupplementalResourceBuilderException e) {
                    // Ignore
                }
            }

            if (modified) {
                SupplementalResourceSessionBuilder sb = new SupplementalResourceSessionBuilder(sessionBean,
                        xml.getParentFile(), xml.getName(), xml);
                sb.run();
            }
        }
    }

    private static List<String> getReferencedFiles(XnatImagesessiondataBean sessionBean) {
        List<String> files = new ArrayList<>();
        List<XnatAbstractresourceI> resources = new ArrayList<>(sessionBean.getResources_resource());
        for (XnatImagescandataI scan : sessionBean.getScans_scan()) {
            resources.addAll(scan.getFile());
        }
        for (XnatAbstractresourceI r : resources) {
            if (!(r instanceof XnatResourcecatalog)) {
                continue;
            }
            files.add(((XnatResourcecatalog) r).getUri());
        }
        return files;
    }
}
