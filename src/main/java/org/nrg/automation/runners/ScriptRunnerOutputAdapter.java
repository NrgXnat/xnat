package org.nrg.automation.runners;

import org.nrg.automation.entities.Script;

import java.io.PrintWriter;

public interface ScriptRunnerOutputAdapter {
    /**
     * Gets a writer to handle the standard output stream from a script instance. Attributes of the script may be used
     * by the implementing class to create separate outputs based on script name or the like.
     * @return A print writer object that can be bound to the script at execution time to capture the script output.
     */
    PrintWriter getWriter(final Script script);
}
