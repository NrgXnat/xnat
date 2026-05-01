/*
 * ecat4xnat: org.nrg.ecat.xnat.PETSessionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat.xnat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.nrg.attr.ConversionFailureException;
import org.nrg.attr.ExtAttrDef;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.attr.Utils;
import org.nrg.attr.Utils.NotRootDir;
import org.nrg.ecat.AttrAdapter;
import org.nrg.ecat.FileSet;
import org.nrg.ecat.Header;
import org.nrg.ecat.MatrixDataFile;
import org.nrg.ecat.Variable;
import org.nrg.session.SessionBuilder;
import org.nrg.ulog.FileMicroLogFactory;
import org.nrg.ulog.MicroLog;
import org.nrg.ulog.MicroLogFactory;
import org.nrg.ulog.SubMicroLog;
import org.nrg.xdat.bean.XnatImageresourceBean;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.XnatPetscandataBean;
import org.nrg.xdat.bean.XnatPetscandataFrameBean;
import org.nrg.xdat.bean.XnatPetsessiondataBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public final class PETSessionBuilder extends SessionBuilder {
    private static final Set<Variable> requiredVars = new HashSet<>();

    public static Set<Variable> getRequiredECATVars() {
        if (requiredVars.isEmpty()) {
            requiredVars.addAll(PETSessionAttributes.get().getNativeAttrs());
            requiredVars.addAll(PETScanAttributes.get().getNativeAttrs());
            requiredVars.addAll(ImageResourceAttributes.get().getNativeAttrs());
            requiredVars.addAll(FrameAttributes.get().getNativeAttrs());
            requiredVars.add(Variable.ACQUISITION_TYPE);
            requiredVars.add(Variable.DOSE_START_TIME);    // we use this explicitly
        }
        return requiredVars;
    }

    /**
     * @param args roots (directory pathnames) for which XML should be written
     * @throws IOException When an error occurs while reading or writing data.
     */
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i += 2) {
            final File           f   = new File(args[i]);
            final File           out = new File(args[i] + ".xml");
            final SessionBuilder psb = new PETSessionBuilder(f, new FileWriter(out), args[i + 1]);
            psb.run();
        }
    }

    private static void report(final Object context, final Object subject,
                               final ConversionFailureException e, final MicroLog log) throws IOException {
        final StringBuilder sb = startReport(context, subject);
        sb.append(": ECAT attribute ").append(e.getAttr());
        sb.append(" has bad value (").append(e.getValue()).append("); unable to derive attribute(s) ");
        sb.append(Arrays.toString(e.getExtAttrs()));
        log.log(sb.toString());
    }

    private static void report(final Object context, final Object subject,
                               final NoUniqueValueException e, final MicroLog log) throws IOException {
        final StringBuilder sb = startReport(context, subject);
        if (0 == e.getValues().length) {
            sb.append(" has no value");
        } else {
            sb.append(" has multiple values: ").append(Arrays.toString(e.getValues()));
        }
        log.log(sb.toString());
    }

    private static void report(final Object context, final Object subject, final Throwable e, final MicroLog log) {
        try {
            if (e instanceof ConversionFailureException) {
                report(context, subject, (ConversionFailureException) e, log);
            } else if (e instanceof NoUniqueValueException) {
                report(context, subject, (NoUniqueValueException) e, log);
            } else {
                final StringBuilder sb = startReport(context, subject);
                sb.append(" could not be resolved :").append(e);
                log.log(sb.toString());
            }
        } catch (IOException e1) {
            LoggerFactory.getLogger(SessionBuilder.class).error("Unable to write to session log " + log, e1);
        }
    }


    private static void reportAll(final Object context,
                                  final Map<ExtAttrDef<Variable>, Throwable> failures,
                                  final MicroLog log) {
        for (final Map.Entry<ExtAttrDef<Variable>, Throwable> me : failures.entrySet()) {
            report(context, me.getKey(), me.getValue(), log);
        }
    }


    private static StringBuilder startReport(final Object context, final Object subject) {
        return new StringBuilder(context.toString()).append(" attribute ").append(subject);
    }


    private final Logger logger = LoggerFactory.getLogger(PETSessionBuilder.class);

    private final MicroLogFactory logWriterFactory;

    private final FileSet es;

    private String label = null, subject = null;
    private TimeZone timezone = null;

    /**
     * @param ecatdir Directory containing ECAT files
     * @param writer  Where the session XML will be written
     * @param project Name of the project
     */
    public PETSessionBuilder(final File ecatdir, final Writer writer, final String project) {
        this(new FileSet(ecatdir, getRequiredECATVars()), writer, project);
    }

    public PETSessionBuilder(final FileSet es, final Writer writer, final String project) {
        super(es.getRoot(), es.getRoot().getPath(), writer);
        this.es = es;
        this.logWriterFactory = new FileMicroLogFactory(es.getRoot());
    }

    public PETSessionBuilder setSessionLabel(final String label) {
        this.label = label;
        return this;
    }

    public PETSessionBuilder setSubject(final String subject) {
        this.subject = subject;
        return this;
    }

    public PETSessionBuilder setTimezone(final String timezone) {
        this.timezone = TimeZone.getTimeZone(timezone);
        return this;
    }

    @SuppressWarnings("unchecked")
    public XnatImagesessiondataBean call() throws IOException, NoUniqueSessionException {
        final List<MatrixDataFile> files = es.getFiles();
        if (files.isEmpty()) {
            logger.warn("no ECAT files found in {}", es.getRoot());
            return null;
        }

        final Date       studyDate = files.get(0).getScanStartTime();
        final List<Date> scanDates = Lists.newArrayListWithExpectedSize(files.size());

        final Map<Variable.AcquisitionType, Date> doseStartTimes = new HashMap<>();

        // Extract dose start times for each acquisition type.
        for (final MatrixDataFile ef : files) {
            final Variable.AcquisitionType type = ef.getAcquisitionType();
            if (doseStartTimes.containsKey(type)) {
                logger.warn("Multiple dose start times for acquisition type " + type);
            } else {
                doseStartTimes.put(type, ef.getDoseStartTime());
            }
            scanDates.add(ef.getScanStartTime());
        }

        try (MicroLog sessionLog = logWriterFactory.getLog("ecattoxnat.log")) {
            final AttrAdapter sessionAttrs = new AttrAdapter(es);
            sessionAttrs.add(PETSessionAttributes.get());
            final Map<ExtAttrDef<Variable>, Throwable> failures      = new HashMap<>();
            final List<ExtAttrValue>                   sessionValues = getValues(sessionAttrs, failures);
            for (final Map.Entry<ExtAttrDef<Variable>, Throwable> me : failures.entrySet()) {
                final Throwable cause = me.getValue();
                if (cause instanceof NoUniqueValueException) {
                    final NoUniqueValueException nuve = (NoUniqueValueException) cause;
                    if ("ID".equals(nuve.getAttribute())) {
                        throw new NoUniqueSessionException(nuve.getValues());
                    }
                } else {
                    report("session", me.getKey(), me.getValue(), sessionLog);
                }
            }

            /*
             * Some attributes need extra processing:
             * Some are not directly derived from ECAT variables,
             * while others are attributes of the PETSessionData rather than elements.
             * I use XnatAttrDef.Empty attributes, rather than just adding the elements separately,
             * so we can specify the element order in the same place we defined them.
             */
            final XnatPetsessiondataBean session = new XnatPetsessiondataBean();
            for (final ExtAttrValue val : setValues(session, sessionValues, "prearchivePath", "date", "time", "tracer")) {
                final String name = val.getName();
                if ("prearchivePath".equals(name)) {
                    setPrearchivePath(session);
                } else if ("date".equals(name)) {
                    session.setDate(studyDate);
                } else if ("time".equals(name)) {
                    final SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");
                    if (timezone != null) {
                        timef.setTimeZone(timezone);
                    }
                    session.setTime(timef.format(studyDate));
                } else if ("tracer".equals(name)) {
                    final SimpleDateFormat timef = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    if (timezone != null) {
                        timef.setTimeZone(timezone);
                    }
                    if (doseStartTimes.containsKey(Variable.AcquisitionType.DYNAMIC_EMISSION)) {
                        session.setTracer_starttime(timef.format(doseStartTimes.get(Variable.AcquisitionType.DYNAMIC_EMISSION)));
                    }
                    if (doseStartTimes.containsKey(Variable.AcquisitionType.TRANSMISSION)) {
                        session.setTracer_transmissionsStarttime(timef.format(doseStartTimes.get(Variable.AcquisitionType.TRANSMISSION)));
                    }
                }
            }

            int scani = 0;
            for (final MatrixDataFile ef : files) {
                scani++;
                final AttrAdapter scanAttrs = new AttrAdapter(ef);

                scanAttrs.add(PETScanAttributes.get());

                final XnatPetscandataBean scan = new XnatPetscandataBean();

                final Map<ExtAttrDef<Variable>, Throwable> scanFailures = new HashMap<>();
                final MicroLog                             scanLog      = new SubMicroLog(sessionLog);
                try {
                    final List<ExtAttrValue> scanValues = getValues(scanAttrs, scanFailures);
                    reportAll("scan " + scani, scanFailures, sessionLog);

                    // As with the session, some attributes need a little extra handling.
                    for (final ExtAttrValue val : setValues(scan, scanValues, "ID", "frames", "startTime")) {
                        final String name = val.getName();
                        if ("ID".equals(name)) {
                            scan.setId(String.valueOf(scani));
                        } else if ("startTime".equals(name)) {
                            final SimpleDateFormat timef = new SimpleDateFormat("HH:mm:ss");
                            if (null != timezone) {
                                timef.setTimeZone(timezone);
                            }
                            scan.setStarttime(timef.format(scanDates.get(scani - 1)));
                        } else if ("frames".equals(name)) {
                            // frames is hierarchical and requires all sorts of special processing.
                            //             final ECATFile ecat = new ECATFile(files[filei], FrameAttributes.get().getNativeAttrs());
                            int framei = 0;
                            for (final Header framev : ef.getSubheaders()) {
                                final XnatPetscandataFrameBean frame = new XnatPetscandataFrameBean();
                                frame.setNumber(val.getText());

                                final AttrAdapter frameAttrs = new AttrAdapter(framev, FrameAttributes.get());

                                final Map<ExtAttrDef<Variable>, Throwable> frameFailures = new HashMap<>();
                                final List<ExtAttrValue>                   frameValues   = getValues(frameAttrs, frameFailures);
                                reportAll("scan " + scani + " frame " + framei, frameFailures, sessionLog);

                                framei++;

                                for (final ExtAttrValue frameVal : setValues(frame, frameValues, "number", "starttime", "length")) {
                                    final String aname = frameVal.getName();
                                    if ("number".equals(aname)) {
                                        frame.setNumber(framei);
                                    } else {
                                        // Time variables need conversion from ms to s
                                        try {
                                            final double t = Double.valueOf(frameVal.getText()) / 1000;
                                            if ("starttime".equals(aname)) {
                                                frame.setStarttime(t);
                                            } else if ("length".equals(aname)) {
                                                frame.setLength(t);
                                            } else {
                                                throw new RuntimeException("unknown frame field: " + aname);
                                            }
                                        } catch (NumberFormatException e) {
                                            throw new RuntimeException("invalid value for " + aname, e);
                                        }
                                    }
                                }

                                scan.addParameters_frames_frame(frame);
                            }
                        }
                    }

                    final AttrAdapter imageAttrs = new AttrAdapter(ef);
                    imageAttrs.add(ImageResourceAttributes.get());

                    final Map<ExtAttrDef<Variable>, Throwable> imageFailures = new HashMap<>();
                    List<ExtAttrValue>                         imageValues   = getValues(imageAttrs, imageFailures);
                    reportAll("scan " + scani + " image", imageFailures, sessionLog);

                    final XnatImageresourceBean imageResource = new XnatImageresourceBean();
                    for (final ExtAttrValue val : setValues(imageResource, imageValues, "URI")) {
                        assert "URI".equals(val.getName());
                        try {
                            if (useRelativePaths()) {
                                imageResource.setUri(Utils.getRelativeURI(es.getRoot(), ef.getFile()));
                            } else {
                                imageResource.setUri(ef.getFile().getCanonicalPath());
                            }
                        } catch (NotRootDir e) {
                            logger.error("Data file " + ef.getFile() + " not contained in " + es.getRoot().getPath());
                        }
                    }
                    scan.addFile(imageResource);

                    session.addScans_scan(scan);
                } finally {
                    if (scanLog.hasMessages()) {
                        scan.setEcatvalidation("errors occurred in processing; see " + sessionLog);
                        logger.info("Errors occurred in processing " + es + "; see " + sessionLog);
                        scanLog.close();
                    }
                }
            }
            if (null != label) {
                session.setLabel(label);
            }
            if (null != subject) {
                session.setSubjectId(subject);
            }
            return session;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.session.SessionBuilder#getSessionInfo()
     */
    public final String getSessionInfo() {
        final int           n  = es.getFiles().size();
        return String.valueOf(n) + " scans in " + n + " files";
    }
}
