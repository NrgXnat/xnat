/*
 * web: org.nrg.xnat.initialization.tasks.InitializingTaskException
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.initialization.tasks;

import org.slf4j.helpers.MessageFormatter;

public class InitializingTaskException extends Exception {
    public InitializingTaskException(final Level level) {
        _level = level;
    }

    public InitializingTaskException(final Level level, final String message) {
        super(message);
        _level = level;
    }

    public InitializingTaskException(final Level level, final String message, final Object... parameters) {
        super(formatMessage(message, parameters));
        _level = level;
    }

    public InitializingTaskException(final Level level, final String message, final Exception exception) {
        super(message, exception);
        _level = level;
    }

    public Level getLevel() {
        return _level;
    }

    enum Level {
        RequiresInitialization,
        SingleNotice,
        Info,
        Warn,
        Error
    }

    private static String formatMessage(final String message, final Object... parameters) {
        return MessageFormatter.arrayFormat(message, parameters).getMessage();
    }

    private final Level _level;
}
