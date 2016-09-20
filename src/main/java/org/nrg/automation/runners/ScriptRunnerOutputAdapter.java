/*
 * org.nrg.automation.runners.ScriptRunnerOutputAdapter
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.automation.runners;

import org.nrg.automation.entities.Script;

import java.io.PrintWriter;

public interface ScriptRunnerOutputAdapter {
    /**
     * Gets a writer to handle the standard output stream from a script instance. Attributes of the script may be used
     * by the implementing class to create separate outputs based on script name or the like.
     *
     * @param script The script to be run.
     *
     * @return A print writer object that can be bound to the script at execution time to capture the script output.
     */
    PrintWriter getWriter(final Script script);
}
