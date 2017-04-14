/*
 * anonymize: org.nrg.dcm.CopyOp
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.transaction.operations;

import org.apache.commons.io.FileUtils;
import org.nrg.transaction.OperationI;
import org.nrg.transaction.RollbackException;
import org.nrg.transaction.Transaction;
import org.nrg.transaction.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class CopyOp extends Transaction {
    /**
	 * The default constructor
	 * @param operation         The operation to run on a set of files or directories
	 * @param rootBackup The directory into which to backup the files or directories
	 * @param dirs       A map of names to Files so they can be retrieved by the user's code.
	 */
	public CopyOp(OperationI<Map<String,File>> operation, File rootBackup, Map<String,File> dirs) {
		_dirs = dirs;
		_operation = operation;
		_rootBackup = rootBackup;
		Map<String,File> backups = new HashMap<>();
		for (String f : dirs.keySet()) {
			backups.put(f, null);
		}
		_backups = backups;
	}
	
	public CopyOp(OperationI<Map<String,File>> operation, File rootBackup, File dir) {
		this(operation, rootBackup, CopyOp.addToMap(dir));
	}
	
	public CopyOp(OperationI<Map<String,File>> operation, File rootBackup, Set<File> dirs) {
		this(operation, rootBackup, CopyOp.addToMap(dirs));
	}
	
	private static Map<String,File> addToMap(File dir) {
		Map<String,File> fs = new HashMap<>();
		fs.put("0", dir);
		return fs;
	}
	
	private static Map<String,File> addToMap(Set<File> dirs) {
		Integer counter = 0;
		Map<String,File> fs = new HashMap<>();
		for (File f : dirs) {
			fs.put(counter.toString(), f);
			counter++;
		}
		return fs;
	}
	
    private File copy(File file) throws TransactionException {
		_rootBackup.mkdirs();
		_rootBackup.setWritable(true);
		
		logger.info("Backing up source directory");
		try {
			if (file.isDirectory()) {
				FileUtils.copyDirectoryToDirectory(file, _rootBackup);
			}
			else {
				FileUtils.copyFile(file, _rootBackup);
			}
		} catch (Exception e) {
			logger.error("Failed to backup source directory");
			throw new TransactionException (e.getMessage(),e);
		}
		return new File(_rootBackup, file.getName());
	}
	
	
	public void run() throws TransactionException {
		for (String f : _backups.keySet()) {
			if (_dirs.get(f) != null && _dirs.get(f).exists()) {
				_backups.put(f, copy(_dirs.get(f)));
			}
		}
		try {
			_operation.run(_dirs);
			for (File f : _backups.values()) {
				if (f.isFile()) {
					f.delete();
				}
				else if (f.isDirectory()) {
					FileUtils.deleteDirectory(f);
				}
			}
			if (_rootBackup.isDirectory()) {
                final String[] list = _rootBackup.list();
                if (list == null || list.length == 0) {
					FileUtils.deleteDirectory(_rootBackup);
				}
			}
		}
		catch (Throwable e){
			throw new TransactionException(e.getMessage(),e);
		}
	}

    public void rollback() throws RollbackException {
		try {
			File backupTmp = new File(_rootBackup, "modified_dest");
			backupTmp.mkdirs();
			for (String f : _backups.keySet()){
				if (_dirs.get(f) != null) {
					String originalPath = _dirs.get(f).getAbsolutePath();
					File b = _backups.get(f);
					if (! b.renameTo(new File(backupTmp, b.getName()))) {
						throw new RollbackException("unable to rename " + b.getAbsolutePath() + " to " + new File(backupTmp, b.getName()).getAbsolutePath());
					}
					else {
						try {
							FileUtils.deleteDirectory(_dirs.get(f));
						}
						catch (IOException e) {
							throw new RollbackException(e);
						}
					}
					File newB = new File(backupTmp, b.getName());
					if (! newB.renameTo(new File(originalPath))) {
						throw new RollbackException("unable to rename " + newB.getAbsolutePath() + " to " + originalPath);
					}	
				}
			}
		}
		catch (RollbackException e) {
			logger.error("Failed to restore previous version of destination directory.");
			throw e;
		}
	}

    private static final Logger logger = LoggerFactory.getLogger(CopyOp.class);

    private final File _rootBackup;            //the main directory into which files or directories are backed up

    private final Map<String,File>             _dirs;      // an associative list of files
    private final Map<String,File>             _backups;   // an associative list of backups
    private final OperationI<Map<String,File>> _operation;
}
