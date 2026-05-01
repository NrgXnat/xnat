/*
 * anonymize: org.nrg.dcm.WorkOnCopyOp
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction.operations;

import org.apache.commons.io.FileUtils;
import org.nrg.transaction.RollbackException;
import org.nrg.transaction.Transaction;
import org.nrg.transaction.TransactionException;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public final class WorkOnCopyOp<T> extends Transaction<T> {
    public WorkOnCopyOp(File source, File tempDir, CallOnFile<T> callOnFile) {
        _source = source;
        _tempDir = tempDir;
        _callOnFile = callOnFile;
    }

    @Override
    public T run() throws TransactionException {
        try {
            final long ms = Calendar.getInstance().getTimeInMillis();
            _callOnFile.setFile(new File(_tempDir.getAbsolutePath(), ms + _source.getName()));
            final T result = _callOnFile.call();

            String originalPath = _source.getAbsolutePath();
            if (!_source.delete()) {
                throw new RollbackException("Unable to delete " + originalPath + ". A backup exists at " + _callOnFile.getFile().getAbsolutePath());
            }
            // Simple renameTo fails on Windows if the destination directory exists
            // So we have to copy the original directory to the destination and then
            // delete the original. There has to be a better way to do this.
            try {
                FileUtils.copyFile(_callOnFile.getFile(), new File(originalPath));
            } catch (IOException e) {
                throw new RollbackException(e);
            }
            if (!_callOnFile.getFile().delete()) {
                throw new RollbackException("Unable to delete " + _callOnFile.getFile().getAbsolutePath());
            }
            return result;
        } catch (Throwable e) {
            throw new TransactionException(e);
        }
    }

    @Override
    public void rollback() throws RollbackException {
        File tmp = new File(_tempDir.getAbsolutePath(), _source.getName());
        if (tmp.exists()) {
            if (tmp.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(tmp);
                } catch (IOException e) {
                    throw new RollbackException(e);
                }
            } else {
                try {
                    FileUtils.forceDelete(tmp);
                } catch (IOException e) {
                    throw new RollbackException(e);
                }
            }
        }
    }

    private final File             _source;
    private final File             _tempDir;
    private final CallOnFile<T> _callOnFile;
}
