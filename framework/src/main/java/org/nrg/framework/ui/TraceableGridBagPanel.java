/*
 * framework: org.nrg.framework.ui.TraceableGridBagPanel
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.ui;

import javax.swing.*;
import java.awt.*;

/**
 * The traceable grid-bag panel provides a way to view the grid lines defined by the various components injected into
 * the panel. This makes it easier to figure out which component may be causing layout issues in the panel display. To
 * view the grid lines, just create one of these objects with the constructor's trace parameter set to <b>true</b>.
 *
 * If you don't turn tracing on for the traceable grid-bag panel, then this code:
 *
 * <pre>final JPanel panel = new TraceableGridBagPanel(false);</pre>
 *
 * Is equivalent to this:
 *
 * <pre>final JPanel panel = new JPanel(new GridBagLayout());</pre>
 *
 * If you need to subsequently reference the <b>GridBagLayout</b> object directly, you can call the {@link #getLayout()}
 * method.
 */
@SuppressWarnings("unused")
public class TraceableGridBagPanel extends JPanel {
    /**
     * Creates a new traceable grid-bag panel with tracing turned off.
     */
    public TraceableGridBagPanel() {
        this(false);
    }

    /**
     * Creates a new traceable grid-bag panel with tracing turned on or off based on the <b>trace</b> parameter.
     * @param trace    Indicates whether grid-bag lines should be displayed or not.
     */
    public TraceableGridBagPanel(final boolean trace) {
        super(new GridBagLayout());
        _layout = (GridBagLayout) super.getLayout();
        _trace = trace;
    }

    /**
     * Overrides the base <b>paint()</b> method to implement tracing of the grid lines.
     * @param graphics    The graphics context for the panel.
     */
    @Override
    public void paint(Graphics graphics) {
        super.paint(graphics);

        if (_trace) {
            int[][] dimensions = _layout.getLayoutDimensions();
            graphics.setColor(Color.BLUE);
            int x = 0;
            for (final int width : dimensions[0]) {
                x += width;
                graphics.drawLine(x, 0, x, getHeight());
            }
            int y = 0;
            for (final int height : dimensions[1]) {
                y += height;
                graphics.drawLine(0, y, getWidth(), y);
            }
        }
    }

    /**
     * Retrieve the layout manager. For the {@link TraceableGridBagPanel} class, this will always be a {@link
     * GridBagLayout} object.
     * @return The panel's layout manager.
     */
    @Override
    public LayoutManager getLayout() {
        return _layout;
    }

    /**
     * Indicates whether tracing is enabled or not.
     * @return Returns true if tracing is enabled, false otherwise.
     */
    public boolean getTrace() {
        return _trace;
    }

    private final GridBagLayout _layout;
    private final boolean       _trace;
}
