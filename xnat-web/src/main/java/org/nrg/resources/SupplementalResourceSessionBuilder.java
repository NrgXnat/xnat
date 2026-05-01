package org.nrg.resources;

import org.nrg.session.SessionBuilder;
import org.nrg.xdat.bean.XnatImagesessiondataBean;

import java.io.File;

/**
 * We use this so that we can use {@link SessionBuilder#run()} to write the xml after we add resources to it
 */
public class SupplementalResourceSessionBuilder extends SessionBuilder {
    private XnatImagesessiondataBean sessionBean;

    protected SupplementalResourceSessionBuilder(XnatImagesessiondataBean sessionBean, File sessionDir, String desc, File file) {
        super(sessionDir, desc, file);
        this.sessionBean = sessionBean;
    }

    @Override
    public String getSessionInfo() {
        return null;
    }

    @Override
    public XnatImagesessiondataBean call() {
        return sessionBean;
    }
}
