package org.nrg.xft.event;

import org.apache.commons.lang3.tuple.Pair;
import org.nrg.framework.event.EventI;
import org.nrg.xft.XFTItem;

import java.util.List;

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
     * Indicates whether this event affected only a single item or more than one.
     *
     * @return Returns true if the event represents more than one item, false otherwise.
     */
    boolean isMultiItemEvent();

    /**
     * Gets the XSI type for a {@link #isMultiItemEvent() single-item event}.
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
     * Gets a list of XSI type and ID pairs for the event. Note that this works for {@link #isMultiItemEvent() multi-item events}
     * as well as single-item events. For single-item events, a list containing a single element is returned.
     *
     * @return Returns a list of XSI type ID of the associated {@link XFTItem}.
     */
    List<Pair<String, String>> getTypeAndIds();

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
}
