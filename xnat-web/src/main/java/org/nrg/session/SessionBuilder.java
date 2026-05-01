/*
 * SessionBuilders: org.nrg.session.SessionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.session;

import org.apache.commons.lang.StringEscapeUtils;
import org.nrg.CausedIOException;
import org.nrg.attr.*;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.base.BaseElement.UnknownFieldException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public abstract class SessionBuilder implements Runnable, Callable<XnatImagesessiondataBean> {

    private final Logger logger = LoggerFactory.getLogger(SessionBuilder.class);

    private final File sessionDir;
    private final File file;
    private final Writer writer;
    private final String desc;
    private boolean isInPrearc = true;
    private final Map<String, String> _parameters = new ConcurrentHashMap<>();

    protected SessionBuilder(final File sessionDir, final String desc, final Writer writer, final File file) {
        this.sessionDir = sessionDir;
        this.desc = desc;
        if (null == writer && null == file) {
            throw new IllegalArgumentException("Either writer or file must be non-null");
        }
        this.writer = writer;
        this.file = file;
    }

    protected SessionBuilder(final File sessionDir, final String desc, final Writer writer) {
        this(sessionDir, desc, writer, null);
    }

    protected SessionBuilder(final File sessionDir, final String desc, final File file) {
        this(sessionDir, desc, null, file);
    }

    public final static class NoUniqueSessionException extends Exception {
        private final static long serialVersionUID = 1L;
        private final String[] ids;
        public NoUniqueSessionException(final String...ids) {
            super(buildMessage(ids));
            this.ids = ids;
        }
        public String[] getIDs() { return ids; }

        private static String buildMessage(final String...ids) {
            if (ids.length > 0) {
                return "Multiple sessions found: " + Arrays.toString(ids);
            } else {
                return "No session found";
            }
        }
    }

    public abstract String getSessionInfo();

    public final SessionBuilder setIsInPrearchive(boolean isInPrearchive) {
        this.isInPrearc = isInPrearchive;
        return this;
    }

    public final XnatImagesessiondataBean setPrearchivePath(final XnatImagesessiondataBean session) throws IOException {
        if (isInPrearc) {
            session.setPrearchivepath(StringEscapeUtils.escapeXml(sessionDir.getAbsolutePath()));
        }
        return session;
    }

    public final boolean useRelativePaths() { return isInPrearc; }

    public final Map<String, String> getParameters() {
        return new HashMap<>(_parameters);
    }

    public final void setParameters(final Map<String, String> parameters) {
        _parameters.clear();
        _parameters.putAll(parameters);
    }

    public static <S,V> List<ExtAttrValue> getValues(final AttrAdapter<S,V> adapter,
            final Map<ExtAttrDef<S>,Throwable> failures)
            throws IOException {
        try {
            return adapter.getValues(failures);
        } catch (ExtAttrException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            } else {
                throw new CausedIOException(e);
            }
        }
    }

    public static <S,V> List<ExtAttrValue> getValues(final AttrAdapter<S,V> adapter,
            final Map<S,V> given, final Map<ExtAttrDef<S>,Throwable> failures)
            throws IOException {
        try {
            return adapter.getValuesGiven(given, failures);
        } catch (ExtAttrException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException)e.getCause();
            } else {
                throw new CausedIOException(e);
            }
        }
    }


    public static Collection<ExtAttrValue> setValues(final BaseElement data, final Collection<ExtAttrValue> values, final String... skips) {
        return setValues(data, values, null, skips);
    }

    /**
     * Sets values in the given bean corresponding to the given external attribute values
     * @param data Bean in which values should be set
     * @param values External attribute values
     * @param builders Map of BeanBuilders to handle sub-beans (null for no builders)
     * @param skips Attributes to be skipped; these are usually handled explicitly by the caller
     * @return A collection of attribute value objects.
     */
    public static Collection<ExtAttrValue> setValues(final BaseElement data, final Collection<ExtAttrValue> values, final Map<String,BeanBuilder> builders, final String... skips) {
        final Logger logger = LoggerFactory.getLogger(SessionBuilder.class);
        final Collection<ExtAttrValue> skipped = new LinkedList<>();
        final Set<String> skip = new HashSet<>(Arrays.asList(skips == null ? new String[0] : skips));

        for (final ExtAttrValue val : values) {
            final String name = val.getName();
            if (skip.contains(name)) {
                skipped.add(val);
            } else if (null != builders && builders.containsKey(name)) {
                try {
                    for (final BaseElement bean : builders.get(name).buildBeans(val)) {
                        try {
                            data.setReferenceField(name, bean);
                        } catch (UnknownFieldException e) {
                            String beanText;
                            if (bean != null) {
                                StringWriter writer = new StringWriter();
                                try {
                                    bean.toXML(writer);
                                    writer.flush();
                                    beanText = writer.toString();
                                } catch (Exception e1) {
                                    beanText = "(error serializing bean value)";
                                }
                            } else {
                                beanText = "Null value submitted";
                            }
                            logger.info(getUnknownFieldMessage(name, beanText));
                        }
                    }
                } catch (ConversionFailureException e) {
                    logger.info("Bad content for " + name + ": " + val, e);
                }
            } else {
                if (null != val.getText()) {
                    try {
                        data.setDataField(name, val.getText());
                    } catch (UnknownFieldException e) {
                        logger.info(getUnknownFieldMessage(name, val.getText()));
                    } catch (IllegalArgumentException e) {
                        logger.info("bad value for " + name, e);
                    }
                }
                for (final Map.Entry<String,String> me : val.getAttrs().entrySet()) {
                    try {
                        data.setDataField(name + "/" + me.getKey(), me.getValue());
                    } catch (UnknownFieldException e) {
                        logger.info(getUnknownFieldMessage(name + "/" + me.getKey(), me.getValue()));
                    } catch (IllegalArgumentException e) {
                        logger.info("bad value for " + name + "/" + me.getKey(), e);
                    }
                }
            }
        }

        return skipped;
    }

    private static String getUnknownFieldMessage(final String name, final String value) {
        return "Attempt to set unknown field \"" + name + "\" with the value \"" + value + "\". Note that this doesn't necessarily indicate a problem: some fields are specified for processing or control purposes that aren't recognized at the session object level.";
    }

    /**
     * Reads all the files in the study directory, extract the necessary
     * attributes, translate as needed, and write the session description.
     * @param writer the Writer to which output should be sent
     */
    private XnatImagesessiondataBean writeSession(final Writer writer) throws IOException,NoUniqueSessionException {
        try {
            XnatImagesessiondataBean session = call();
            if (null == session) {
                throw new NoUniqueSessionException();
            }
            session.toXML(writer, true);
            return session;
        } catch (IOException | NoUniqueSessionException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            writer.close();
        }
    }


    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public final void run() {
        try {
            final Writer writer;
            if (null == this.writer) {
                boolean made = file.getParentFile().mkdirs();
                if (logger.isDebugEnabled()) {
                    final String path = file.getParentFile().getPath();
                    logger.debug(made ? "Created " + path : "Found " + path);
                }
                final FileOutputStream fos = new FileOutputStream(file);
                try {
                    logger.trace("locking {}", file);
                    fos.getChannel().lock();
                    logger.debug("locked {}", file);
                } catch (IOException e) {
                    logger.warn("unable to obtain lock on " + file, e);
                }
                writer = new BufferedWriter(new OutputStreamWriter(fos));
            } else {
                writer = this.writer;
            }
            try {
                writeSession(writer);
                logger.info("Wrote session XML for {}: {}", desc, getSessionInfo());
            } catch (IOException e) {
                throw e;
            } catch (NoUniqueSessionException e) {
                logger.error(desc + " must contain exactly one session", e);
            } catch (Exception e) {
                logger.error("Unable to write session XML for " + desc, e);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            logger.error(desc + " write failed", e);
        }
    }
}
