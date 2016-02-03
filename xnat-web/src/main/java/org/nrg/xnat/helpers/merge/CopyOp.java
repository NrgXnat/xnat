/*
 * org.nrg.xnat.helpers.merge.CopyOp
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.merge;

import org.apache.commons.io.FileUtils;
import org.nrg.transaction.RollbackException;
import org.nrg.transaction.Transaction;
import org.nrg.transaction.TransactionException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;


public final class CopyOp<A> extends Transaction{
	final Callable<A> op;
	final File dir;
	final File rootBackup;
	final String backupDirName;
	File backup = null;
	final LoggerI logger;
	
	public interface LoggerI {
		void info(String l);
		void failed(String l);
	}

	public CopyOp(Callable<A>op, File dir, File rootBackup, String backupDirName) {
		this.op = op;
		this.dir = dir;
		this.rootBackup = rootBackup;
		this.backupDirName = backupDirName;
		this.logger = new LoggerI() {
			public void info (String l) {}
			public void failed (String l) {}
		};
	}
	public CopyOp(Callable<A>op, File dir, File rootBackup, String backupDirName, LoggerI logger) {
		this.op = op;
		this.dir = dir;
		this.rootBackup = rootBackup;
		this.backupDirName = backupDirName;
		this.logger = logger;
	}

	private File copy() throws TransactionException {
		File _backup = new File(rootBackup,this.backupDirName);
		_backup.mkdirs();
		
		logger.info("Backing up source directory");
		try {
			FileUtils.copyDirectoryToDirectory(dir, _backup);
		} catch (Exception e) {
			logger.failed("Failed to backup source directory");
			throw new TransactionException (e.getMessage(),e);
		}
		return _backup;
	}
	
	
	public void run() throws TransactionException {
		this.backup = this.copy();
		try {
			this.op.call();
		}
		catch (Throwable e){
			throw new TransactionException(e.getMessage(),e);
		}
	}
	
	private void rollbackHelper(File backupDIR, File destDIR, boolean overwrite)  throws RollbackException{
		File backup = new File(rootBackup,"modified_dest");
		backup.mkdirs();

		logger.info("Restoring previous version of destination directory.");
		try {
			FileUtils.moveDirectory(destDIR, backup);
		} catch (IOException e) {
			logger.info(e.getMessage());
		}
		
		try {
			FileUtils.moveDirectory(backupDIR, destDIR);
		} catch (Exception e) {
			logger.failed("Failed to restore previous version of destination directory.");
			throw new RollbackException(e.getMessage(),e);
		}
	}

	public void rollback() throws RollbackException {
		try {
			rollbackHelper(backup,dir,true);	
		}
		catch (RollbackException e) {
			logger.failed("Failed to restore previous version of destination directory.");
			throw e;
		}
	}
}
