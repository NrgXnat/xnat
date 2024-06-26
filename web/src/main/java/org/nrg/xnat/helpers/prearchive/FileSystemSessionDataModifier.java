/*
 * web: org.nrg.xnat.helpers.prearchive.FileSystemSessionDataModifier
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.helpers.prearchive;

import org.apache.commons.lang3.StringUtils;
import org.nrg.config.entities.Configuration;
import org.nrg.dcm.xnat.DICOMSessionBuilder;
import org.nrg.dcm.xnat.XnatAttrDef;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultError;
import org.nrg.dicom.mizer.objects.AnonymizationResultReject;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.service.MizerService;
import org.nrg.dicom.mizer.service.impl.MizerContextWithScript;
import org.nrg.framework.exceptions.NrgServiceError;
import org.nrg.framework.exceptions.NrgServiceRuntimeException;
import org.nrg.session.SessionBuilder;
import org.nrg.xdat.XDAT;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xft.utils.FileUtils;
import org.nrg.xnat.helpers.merge.anonymize.DefaultAnonUtils;
import org.nrg.xnat.helpers.prearchive.PrearcDatabase.SyncFailedException;
import org.nrg.xnat.helpers.prearchive.PrearcUtils.PrearcStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

/**
 * Modify the session on the filesystem
 *
 * @author aditya
 */
public class FileSystemSessionDataModifier implements SessionDataModifierI {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemSessionDataModifier.class);
    private final String basePath;

    public FileSystemSessionDataModifier(String basePath) {
        this.basePath = basePath;
    }

    /**
     * Represents each step of the operation can either be run or rolled back in case of exception.
     * <p/>
     * There is no recovery from a rollback.
     *
     * @param <T> the type of value returned from a running the op.
     *
     * @author aditya
     */
    interface Transaction<T> {
        T run() throws SyncFailedException;

        void rollback() throws IllegalStateException;
    }

    static class Move {
        final String basePath, session, uri, subject, newProject;
        final File fileSetDir, timestampDir, newTimestampDir, xml;
        XnatImagesessiondataBean doc = null;

        /**
         * Each step of the move is a Transaction that can be rolled back. It is expected that the steps are executed in the order in which they appear below because each step depends on the success of the previous one. So an error in 'setXml' step for example invokes a rollback on the 'copy' step.
         */
        Transaction<Void> copy;
        Transaction<XnatImagesessiondataBean> setXml;
        Transaction<Void> writeXml;

        class Copy implements Transaction<Void> {
            File timestampDir;
            File newTimestampDir;
            String session;

            public Copy(File timestampDir, File newTimestampDir, String session) {
                super();
                this.timestampDir = timestampDir;
                this.newTimestampDir = newTimestampDir;
                this.session = session;
            }

            public Void run() throws SyncFailedException {
                try {
                    org.apache.commons.io.FileUtils.copyDirectoryToDirectory(new File(timestampDir, session), newTimestampDir);
                } catch (IOException e) {
                    throwSync(e.getMessage());
                }
                return null;
            }

            public void rollback() {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(new File(newTimestampDir, session));
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }

        class SetXml implements Transaction<XnatImagesessiondataBean> {
            final File xml;
            final String newProject, newDirPath;

            public SetXml(File xml, String newProject, String newDirPath) {
                this.xml = xml;
                this.newProject = newProject;
                this.newDirPath = newDirPath;
            }

            public XnatImagesessiondataBean run() throws SyncFailedException {
                XnatImagesessiondataBean doc = null;
                try {
                    final Configuration configuration = DefaultAnonUtils.getService().getProjectScriptConfiguration(newProject);
                    if (configuration != null && StringUtils.equals(Configuration.ENABLED_STRING, configuration.getStatus())) {
                        final String anonScript = configuration.getContents();
                        final XnatAttrDef[] params = {new XnatAttrDef.Constant("project", newProject)};
                        final MizerService mizerService = XDAT.getContextService().getBeanSafely(MizerService.class);
                        if (mizerService == null) {
                            throw new NrgServiceRuntimeException(NrgServiceError.Unknown, "Can't find the mizer service");
                        }
                        final MizerContextWithScript context = new MizerContextWithScript();
                        try {
                            context.setScriptId(configuration.getId());
                            context.setScript(anonScript);
                            context.setElement("project", newProject);
                            context.setElement("subject", subject);
                            context.setElement("session", session);

                            if( ! mizerService.setContext( context)) {
                                throw new RuntimeException("Failed to set mizer service context: " + context);
                            }
                        }
                        catch( MizerException e) {
                            throw new RuntimeException(e);
                        }
                        final DICOMSessionBuilder db = new DICOMSessionBuilder(fileSetDir, params,
                                o -> {
                                    DicomObjectI di = DicomObjectFactory.newInstance(o);
                                    try {
                                        AnonymizationResult anonResult = mizerService.anonymize( di, context);
                                        if (anonResult instanceof AnonymizationResultError) {
                                            throw new RuntimeException(String.join(" ","Error on DICOM object when anonymizing", fileSetDir.getAbsolutePath(),":",anonResult.getMessage()));
                                        }
                                        if (anonResult instanceof AnonymizationResultReject) {
                                            final File f = new File(anonResult.getAbsolutePath());
                                            FileUtils.deleteQuietly(f);
                                            throw new RuntimeException(String.join(" ","Rejected DICOM object when anonymizing",fileSetDir.getAbsolutePath(),":",anonResult.getMessage()));
                                        }
                                    }
                                    catch ( MizerException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return di.getDcm4che2Object();
                                });
                        doc = db.call();
                        mizerService.removeContext( context);
                    } else {
                        doc = PrearcTableBuilder.parseSession(xml);
                        doc.setProject(newProject);
                    }
                    //modified to also set the new prearchive path.
                    doc.setPrearchivepath(newDirPath);
                } catch ( SAXException | SQLException | SessionBuilder.NoUniqueSessionException | IOException e) {
                    throwSync(e);
                }

                return doc;
            }

            public void rollback() throws IllegalStateException {
                // this operation is in memory there is no need to rollback
            }
        }

        class WriteXml implements Transaction<Void> {
            File timestampDir;
            String session;
            File xml;

            public WriteXml(File timestampDir, String session) {
                super();
                this.timestampDir = timestampDir;
                this.session = session;
                xml = new File(timestampDir, session + ".xml");
            }

            public Void run() throws SyncFailedException {
                FileWriter fw = null;
                try {
                    fw = new FileWriter(xml);
                    doc.toXML(fw);
                } catch (Exception e) {
                    throwSync(e.getMessage());
                } finally {
                    if (fw != null)
                        try {
                            fw.close();
                        } catch (IOException ignored) {
                        }
                }
                return null;
            }

            public void rollback() throws IllegalStateException {
                xml.delete();
            }
        }

        public Move(String basePath, final String session, String uri, String subject, final String newProject) {
            super();
            this.basePath = basePath;
            this.session = session;
            this.uri = uri;
            this.subject = subject;
            this.newProject = newProject;
            this.fileSetDir = new File(this.uri);
            this.timestampDir = fileSetDir.getParentFile();
            Path newTimestampDirPath = Paths.get( this.basePath, this.newProject, this.timestampDir.getName());
            this.newTimestampDir = newTimestampDirPath.normalize().toFile();
            this.xml = new File(timestampDir, session + ".xml");
            copy = new Copy(timestampDir, newTimestampDir, session);
            setXml = new SetXml(xml, newProject, (new File(newTimestampDir, session)).getAbsolutePath());
            writeXml = new WriteXml(newTimestampDir, session);
        }

        void throwSync(String msg) throws SyncFailedException {
            throw new SyncFailedException(msg);
        }

        void throwSync(Throwable cause) throws SyncFailedException {
            throw new SyncFailedException(cause);
        }

        public Transaction<Void> getCopy() {
            return copy;
        }

        public void setCopy(Transaction<Void> copy) {
            this.copy = copy;
        }

        void run() throws SyncFailedException {
            try {
                this.copy.run();
            } catch (SyncFailedException e) {
                this.copy.rollback();
                throw e;
            }

            try {
                this.doc = this.setXml.run();
            } catch (SyncFailedException | RuntimeException e) {
                this.setXml.rollback();
                this.copy.rollback();
                throw e;
            }

            try {
                this.writeXml.run();
            } catch (SyncFailedException | RuntimeException e) {
                this.writeXml.rollback();
                this.setXml.rollback();
                this.copy.rollback();
                throw e;
            }
            // If everything was moved, we can remove the session and timestamp directories.
            FileUtils.deleteDirQuietly(fileSetDir);
            if (fileSetDir.exists()) {
                logger.warn("moved session " + session + " to " + newProject + ", but unable to delete original directory.");
            }
            // timestamp directory might contain another session, so no warning if deletion fails.
            final String[] list = timestampDir.list();
            if (list != null && list.length == 0) {
                timestampDir.delete();
            }
        }
    }

    public void move(final SessionData sd, final String newProject) throws SyncFailedException {
        this._move(new Move(this.basePath, sd.getFolderName(), sd.getUrl(), sd.getSubject(), newProject) {
        });
    }

    protected void _move(Move move) throws SyncFailedException {
        move.run();
    }

    static class Slice {
        final SessionData _sessionData;
        final String _newSessionLabel;
        final String _newSessionFolder;
        final List<String> _scans;
        final File _source, _destination;

        public Slice(final SessionData sessionData, final String newSessionLabel, final String newSessionFolder, final List<String> scans) {
            super();
            _sessionData = sessionData;
            _newSessionLabel = newSessionLabel;
            _newSessionFolder = newSessionFolder;
            _scans = scans;
            _source = new File(_sessionData.getUrl());
            _destination = new File(_newSessionFolder);
            _copy = new Copy(_source, _destination, _scans);
        }

        final Transaction<Void> _copy;

        class Copy implements Transaction<Void> {
            final File _source;
            final File _destination;
            final List<String> _scans;

            public Copy(final File source, final File destination, final List<String> scans) {
                super();
                _source = source;
                _destination = destination;
                _scans = scans;
            }

            public Void run() throws SyncFailedException {
                try {
                    final File scanDestination = Paths.get(_destination.toURI()).resolve("SCANS").toFile();
                    if (!scanDestination.exists()) {
                        if (scanDestination.mkdirs() && logger.isDebugEnabled()) {
                            logger.debug("Created new destination scan folder " + scanDestination.getAbsolutePath());
                        }
                    }
                    for (final String scan : _scans) {
                        final File scanSource = Paths.get(_source.toURI()).resolve("SCANS").resolve(scan).toFile();
                        if (!scanSource.exists()) {
                            throw new SyncFailedException("Couldn't find the source path for scan " + scan + " under the " + _source.getAbsolutePath() + " session folder.");
                        }
                        org.apache.commons.io.FileUtils.copyDirectoryToDirectory(scanSource, scanDestination);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Moved scan from {} to {}", scanSource.getAbsolutePath(), scanDestination.getAbsolutePath());
                        }
                    }
                } catch (IOException e) {
                    throw new SyncFailedException(e.getMessage());
                }
                return null;
            }

            public void rollback() {
                try {
                    org.apache.commons.io.FileUtils.deleteDirectory(_destination);
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage());
                }
            }
        }

        void run() throws SyncFailedException {
            try {
                _copy.run();
            } catch (SyncFailedException e) {
                _copy.rollback();
                throw e;
            }
        }
    }

    @Override
    public void moveScans(final SessionData source, final String newSessionLabel, final String newSessionFolder, final List<String> scans) throws SyncFailedException {
        new Slice(source, newSessionLabel, newSessionFolder, scans).run();
    }

    public void delete(SessionData sd) {
        final File sessionFolder = new File(sd.getUrl());
        final File timestampDir = sessionFolder.getParentFile();
        try {
            final File sessionXml = new File(sessionFolder.getPath() + ".xml");
            if (sessionFolder.exists()) {
                if (sessionXml.exists()) {
                    FileUtils.MoveToCache(sessionXml);
                }
                FileUtils.MoveToCache(sessionFolder);
            } else {
                FileUtils.DeleteFile(sessionXml);
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        if (!FileUtils.HasFiles(timestampDir)) {
            FileUtils.deleteDirQuietly(timestampDir);    // delete timestamp parent only if empty.
        }
    }

    public void setStatus(SessionData sd, PrearcStatus status) {
        // TODO Auto-generated method stub
    }
}
