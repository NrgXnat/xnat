/*
 * SessionBuilders: org.nrg.io.RelativePathWriter
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RelativePathWriter extends Writer implements RelativePathItem {
    private static final Logger logger = LoggerFactory.getLogger(RelativePathWriter.class);
    private final Writer writer;
    private final String relativePath, fullPath;

    public RelativePathWriter(final File root, final File out) throws IOException {
        String relpath = getRelativePath(out, root);
        if (null == relpath) {
            logger.debug("Unable to construct relative path for {} against {}; trying canonical paths", out, root);
            relpath = getRelativePath(out.getCanonicalFile(), root.getCanonicalFile());
            if (null == relpath) {
                throw new IOException("cannot construct path for " + out + " relative to " + root);
            } else {
                relativePath = relpath;
            }
            fullPath = out.getCanonicalPath();
        } else {
            relativePath = relpath;
            fullPath = out.getAbsolutePath();
        }

        writer = new FileWriter(out);
    }

    /* (non-Javadoc)
     * @see java.io.Writer#close()
     */
    @Override
    public void close() throws IOException {
        writer.close();
    }

    /* (non-Javadoc)
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush() throws IOException {
        writer.flush();
    }

    /* (non-Javadoc)
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        writer.write(cbuf, off, len);
    }

    public String getFullPath() {
        return fullPath;
    }

    /* (non-Javadoc)
     * @see org.nrg.session.RelativePathItem#getRelativePath()
     */
    public String getRelativePath() {
        return relativePath;
    }


    private static String getRelativePath(final File f, final File dir) {
        final Stack<String> elems = new Stack<String>();
        elems.push(f.getName());
        for (File fp = f.getParentFile(); null != fp; fp = fp.getParentFile()) {
            if (dir.equals(fp)) {
                return joinStack(File.separator, elems);
            } else {
                elems.push(fp.getName());
            }
        }
        return null;    // could not construct relative path
    }

    private static String joinStack(final String separator, final Stack<?> stack) {
        return joinStack(new StringBuilder(), separator, stack).toString();
    }

    private static StringBuilder joinStack(final StringBuilder sb, final String separator,
                                           final Stack<?> stack) {
        if (stack.empty()) return sb;
        sb.append(stack.pop());
        while (!stack.empty()) {
            sb.append(separator);
            sb.append(stack.pop());
        }
        return sb;
    }
}
