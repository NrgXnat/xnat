package org.nrg.xft.event;

import org.nrg.framework.event.EventI;

public interface XftItemEventI extends EventI {
    /**
     * The Constant CREATE.
     */
    String CREATE = "C";

    /**
     * The Constant READ.
     */
    String READ = "R";

    /**
     * The Constant UPDATE.
     */
    String UPDATE = "U";

    /**
     * The Constant DELETE.
     */
    String DELETE = "D";
}
