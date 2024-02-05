/*
 * anonymize: org.nrg.dcm.CallOnFile
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction.operations;

import java.io.File;
import java.util.concurrent.Callable;

public abstract class CallOnFile<A> implements Callable<A> {
    private File _file = null;

    public void setFile(final File file) {
        final File parent = file.getParentFile();
        if (!file.exists()) {
            parent.mkdirs();
        }
        file.setWritable(true);
        parent.setReadable(true);
        parent.setWritable(true);
        _file = file;
    }

    public File getFile() {
        return _file;
    }
}
