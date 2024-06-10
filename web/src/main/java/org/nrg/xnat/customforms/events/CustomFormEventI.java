package org.nrg.xnat.customforms.events;

import com.google.common.collect.ImmutableMap;
import org.nrg.framework.event.EventI;
import org.nrg.xft.XFTItem;

import javax.annotation.Nonnull;
import java.util.Map;

public interface CustomFormEventI extends EventI {
    /**
     * The Constant CREATE.
     */
    String CREATE = "C";

    /**
     * The Constant UPDATE.
     */
    String UPDATE = "U";

    /**
     * The Constant DELETE.
     */
    String DELETE = "D";

    Map<String, String> ACTIONS = ImmutableMap.<String, String>builder().put(CREATE, "create").put(UPDATE, "update").put(DELETE, "delete").build();

    /**
     * Used to specify a specific action in the {@link #getProperties() event properties}.
     */
    String OPERATION = "action";

    /**
     * Gets the action associated with the event.
     *
     * @return The action.
     */
    String getAction();

    /**
     * Gets the XSI type for the object associated with the event.
     * @return Returns the XSI type of the associated {@link XFTItem}.
     */
    String getXsiType();

    /**
     * Gets the UUID .
     *
     * @return Returns the UUID.
     */
    String getUuid();


    /**
     * Provides a way to include extra information about the event in cases where a simple description such as {@link #UPDATE}
     * is insufficient. The properties included in the map are dependent on the event and its context.
     *
     * @return A map of properties and values.
     */
    @Nonnull
    Map<String, ?> getProperties();


}
