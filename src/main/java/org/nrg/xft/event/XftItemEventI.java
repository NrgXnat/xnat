package org.nrg.xft.event;

import org.nrg.framework.event.EventI;
import org.nrg.xft.XFTItem;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Indicates that an object was shared to a new project. Events with this
     * action should include the "target" property with the new project ID.
     */
    String SHARE = "S";

    /**
     * Indicates that an object was moved. Events with this action should
     * include the "origin" and "target" properties with the two project IDs.
     */
    String MOVE = "M";

    Map<String, String> ACTIONS = new HashMap<String, String>() {{
        put(CREATE, "create");
        put(READ, "read");
        put(UPDATE, "update");
        put(DELETE, "delete");
        put(SHARE, "share");
        put(MOVE, "move");
    }};

    /**
     * Used to specify a specific action in the {@link #getProperties() event properties}.
     */
    String OPERATION = "action";

    /**
     * Indicates whether this event affected only a single item or more than one.
     *
     * @return Returns true if the event represents more than one item, false otherwise.
     */
    boolean isMultiItemEvent();

    /**
     * Gets the XSI type for the object associated with the event. This works for both {@link #isMultiItemEvent() multi-item events}
     * and single-item events, since multi-item events <i>must</i> have the same XSI type for all associated objects.
     *
     * @return Returns the XSI type of the associated {@link XFTItem}.
     */
    String getXsiType();

    /**
     * Gets the ID for a {@link #isMultiItemEvent() single-item event}.
     *
     * @return Returns the ID of the associated {@link XFTItem}.
     */
    String getId();

    /**
     * Gets a list of IDs for the event. Note that this works for both {@link #isMultiItemEvent() multi-item}
     * and single-item events. For single-item events, a list containing a single ID is returned.
     *
     * @return Returns a list of IDs of the associated {@link XFTItem}.
     */
    List<String> getIds();

    /**
     * Gets the action associated with the event.
     *
     * @return The action.
     */
    String getAction();

    /**
     * Gets the {@link XFTItem} for a {@link #isMultiItemEvent() single-item event}. If the item was not passed in when the
     * event was created, an instance is created on demand.
     *
     * @return Returns the associated {@link XFTItem}.
     */
    XFTItem getItem();

    /**
     * Gets a list of the associated {@link XFTItem}s for a {@link #isMultiItemEvent() multi-item event}. If the items were not
     * passed in when the event was created, the instances are created on demand.
     *
     * @return Returns the associated {@link XFTItem} objects.
     */
    List<XFTItem> getItems();

    /**
     * Provides a way to include extra information about the event in cases where a simple description such as {@link #UPDATE}
     * is insufficient. The properties included in the map are dependent on the event and its context.
     *
     * @return A map of properties and values.
     */
    @Nonnull
    Map<String, ?> getProperties();
}
