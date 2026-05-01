/*
 * framework: org.nrg.framework.utilities.Messages
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.framework.utilities;

import java.text.ParseException;

@SuppressWarnings("unused")
public class Messages {
    private Messages() {
        // No instantiation.
    }

    /**
     * This formats HTML to a particular width. This ignores the contents inside tag elements (i.e. anything between the
     * &lt; and &gt; characters), but considers anything else to be text requiring justification. New lines, in the form
     * of opening &lt;p&gt; and closing &lt;/p&gt; tags, are added to the message to result in text that is formatted in
     * lines of <b>width</b> characters. Note that this may result in some odd displays because of proportional font
     * sizes, but should be fairly close.
     *
     * TODO:  The line-length count should reset upon finding the &lt;p&gt;, &lt;/p&gt;, &lt;br&gt;, and &lt;/br&gt;
     *        tags, allowing callers to add their own line breaks as well.
     * @param message    The message to be formatted.
     * @param width      The width desired for each line.
     * @return The formatted message.
     * @throws ParseException When tags are nested inside of other tags.
     */
    public static String formatHtml(String message, int width) throws ParseException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<html>");

        boolean insideTag = false;
        final char[] chars = message.toCharArray();
        int lineCount = 0;
        for (int index = 0; index < chars.length; index++) {
            char c = chars[index];
            // If c starts a tag and we're inside a tag OR c ends a tag and we're not inside a tag, that's not good.
            if (c == '<' && insideTag || c == '>' && !insideTag) {
                // Bail out in this case.
                throw new ParseException("Invalid format: nested tags", index);
            }
            // If c starts a tag (we know we're not currently inside a tag)... 
            if (c == '<') {
                // Set the inside tag flag and break. We ignore stuff inside the tag for the line count.
                insideTag = true;
            } else if (insideTag) {
                if (c == '>') {
                    insideTag = false;
                }
            }
            buffer.append(c);
            if (++lineCount == width) {
                buffer.reverse();
            }
        }
        buffer.append("</html>");
        return buffer.toString();
    }

    public enum Format {
        Html
    }
}
