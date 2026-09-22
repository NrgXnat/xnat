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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Runs an operation that produces a new version of a file, then puts that version in the file's
 * place.
 * <p>
 * The operation writes to a staging file in <b>tempDir</b>; the source is not touched until the
 * operation has finished. When tempDir is on the same filesystem as the source the staged file is
 * renamed over the source, which is atomic: a reader sees the old bytes or the new ones, never a
 * missing or partial file. On a different filesystem a rename is impossible and the staged file is
 * copied over the source instead, which is what this always did.
 * <p>
 * Staging names carry a random component. Files worked on concurrently often share a name -- every
 * scan has a 1.dcm -- and a tempDir shared between them has to keep them apart.
 * <p>
 * When the replacement itself fails -- possible only on the non-atomic cross-filesystem fallback --
 * the source may already be damaged, so the staged file may be the only intact copy: rollback
 * leaves it in place, and the exception names where it is.
 */
public final class WorkOnCopyOp<T> extends Transaction<T> {
    public WorkOnCopyOp(File source, File tempDir, CallOnFile<T> callOnFile) {
        _source = source;
        _tempDir = tempDir;
        _callOnFile = callOnFile;
    }

    @Override
    public T run() throws TransactionException {
        try {
            _callOnFile.setFile(new File(_tempDir, "staged-" + UUID.randomUUID() + "-" + _source.getName()));
            final T result = _callOnFile.call();
            try {
                replace(_callOnFile.getFile().toPath(), _source.toPath());
            } catch (IOException e) {
                // The non-atomic fallback deletes the source before copying over it, so a failure
                // here can leave the staged file as the only intact copy: rollback must keep it.
                _keepStagedOnRollback = true;
                throw new TransactionException("Unable to replace " + _source + " with the staged version,"
                                               + " which is preserved at " + _callOnFile.getFile(), e);
            }
            return result;
        } catch (TransactionException e) {
            throw e;
        } catch (Throwable e) {
            throw new TransactionException(e);
        }
    }

    private static void replace(final Path staged, final Path source) throws IOException {
        try {
            Files.move(staged, source, ATOMIC_MOVE, REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            // Another filesystem. Without ATOMIC_MOVE, Files.move copies the staged file over the
            // source and then deletes it.
            Files.move(staged, source, REPLACE_EXISTING);
        }
    }

    @Override
    public void rollback() throws RollbackException {
        final File staged = _callOnFile.getFile();
        if (staged == null || !staged.exists() || _keepStagedOnRollback) {
            return;
        }
        try {
            if (staged.isDirectory()) {
                FileUtils.deleteDirectory(staged);
            } else {
                FileUtils.forceDelete(staged);
            }
        } catch (IOException e) {
            throw new RollbackException(e);
        }
    }

    private final File             _source;
    private final File             _tempDir;
    private final CallOnFile<T> _callOnFile;

    /** Set when {@link #replace} failed: the source may be damaged, so the staged file stays. */
    private boolean _keepStagedOnRollback;
}
