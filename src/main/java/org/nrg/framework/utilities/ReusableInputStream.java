/*
 * framework: org.nrg.framework.utilities.ReusableInputStream
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * Wraps an input stream in a buffered input stream and sets the
 * mark to the largest value possible. This marks the input stream
 * as buffered from the start all the way to the theoretical end.
 * On {@link #close()}, the input stream is not actually closed but
 * {@link #reset()} to the start of the buffer, allowing for
 * reuse each time the stream is "closed".
 *
 * This should be used with care, as it can consume a lot of
 * memory.
 */
public class ReusableInputStream extends BufferedInputStream {
    public ReusableInputStream(final InputStream input) {
        super(input);
        mark(Integer.MAX_VALUE);
    }

    /**
     * Note that this implementation of the <b>close()</b> method 
     * does * <em>not</em> actually close the stream! For that you
     * should * call the {@link #close(boolean)} version of this
     * method. This implementation actually calls that version,
     * passing <b>false</b> as the parameter.
     */
    @Override
    public void close() throws IOException {
        close(false);
    }

    /**
     * This method either closes or resets the wrapped input stream,
     * based on the value of the <b>forReal</b> parameter.
     *
     * @param forReal Indicates whether the stream should be closed
     *                (<b>true</b>) or just reset (<b>false</b>)
     */
    public void close(final boolean forReal) throws IOException {
        if (forReal) {
            super.close();
        } else {
            reset();
        }
    }
}
