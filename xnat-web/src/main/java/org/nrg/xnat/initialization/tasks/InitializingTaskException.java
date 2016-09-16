package org.nrg.xnat.initialization.tasks;

public class InitializingTaskException extends Exception {
    public InitializingTaskException(final Level level, final String message) {
        super(message);
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
        SingleNotice,
        Info,
        Warn,
        Error
    }

    private final Level _level;
}
