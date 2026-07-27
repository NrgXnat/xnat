package org.nrg.xnat.services.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.Test;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService.ConsoleFormat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultLoggingService#redirectFileAppendersToConsole} swaps file appenders (core or plugin) for a
 * console appender -- plain text or JSON -- with no archive surgery, exactly one console per logger, and the
 * source appender's filters preserved.
 */
@SuppressWarnings("unchecked")
public class DefaultLoggingServiceRedirectTest {

    private static final String PATTERN = "%d [%t] %-5p %c - %m%n";

    @Test
    public void replacesEveryFileAppenderWithAConsole() {
        final LoggerContext context = contextWithFileAppenders();

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

        for (final Logger logger : context.getLoggerList()) {
            for (final Appender<ILoggingEvent> appender : appendersOf(logger)) {
                assertThat(appender)
                        .as("logger '%s' still has a file appender after redirect", logger.getName())
                        .isNotInstanceOf(FileAppender.class);
            }
        }

        final List<Appender<ILoggingEvent>> security = appendersOf(context.getLogger("org.nrg.xnat.security"));
        assertThat(security).hasSize(1);
        assertThat(security.get(0)).isInstanceOf(ConsoleAppender.class);
        assertThat(security.get(0).getName()).isEqualTo("security");

        assertThat(appendersOf(context.getLogger(Logger.ROOT_LOGGER_NAME))).anyMatch(ConsoleAppender.class::isInstance);
    }

    @Test
    public void returnsCountOfDistinctRedirectedFileAppenders() {
        // security -> "security", root -> "app": two distinct file appenders.
        assertThat(DefaultLoggingService.redirectFileAppendersToConsole(contextWithFileAppenders(), ConsoleFormat.PLAIN, PATTERN))
                .isEqualTo(2);
    }

    @Test
    public void tagsTheConsolePatternWithTheAppenderNameForPlainFormat() {
        final LoggerContext context = contextWithFileAppenders();

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

        final ConsoleAppender<ILoggingEvent> console =
                (ConsoleAppender<ILoggingEvent>) appendersOf(context.getLogger("org.nrg.xnat.security")).get(0);
        assertThat(console.getEncoder()).isInstanceOf(PatternLayoutEncoder.class);
        assertThat(((PatternLayoutEncoder) console.getEncoder()).getPattern()).contains("[security]");
    }

    @Test
    public void jsonFormatUsesALogstashEncoderTaggedWithTheAppenderName() {
        final LoggerContext context = contextWithFileAppenders();

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.JSON, PATTERN);

        final ConsoleAppender<ILoggingEvent> console =
                (ConsoleAppender<ILoggingEvent>) appendersOf(context.getLogger("org.nrg.xnat.security")).get(0);
        assertThat(console.getEncoder()).isInstanceOf(LogstashEncoder.class);
        assertThat(((LogstashEncoder) console.getEncoder()).getCustomFields()).contains("\"appender\":\"security\"");
    }

    /** The JSON path was never exercised on the source branch; prove logstash+jackson actually encode. */
    @Test
    public void jsonFormatEmitsParseableJson() {
        final LoggerContext context = contextWithFileAppenders();

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.JSON, PATTERN);

        final Logger                         security = context.getLogger("org.nrg.xnat.security");
        final ConsoleAppender<ILoggingEvent> console  = (ConsoleAppender<ILoggingEvent>) appendersOf(security).get(0);
        final LoggingEvent                   event    = new LoggingEvent("fqcn", security, Level.INFO, "hello-json", null, null);

        final String json = new String(console.getEncoder().encode(event), StandardCharsets.UTF_8);

        assertThat(json).contains("\"message\":\"hello-json\"")
                        .contains("\"level\":\"INFO\"")
                        .contains("\"appender\":\"security\"");
    }

    /** A JSON appender name with a quote must not corrupt customFields (Jackson-built, not concatenated). */
    @Test
    public void jsonFormatEscapesAwkwardAppenderNames() {
        final LoggerContext context = new LoggerContext();
        final Logger        logger  = context.getLogger("weird");
        logger.setAdditive(false);
        logger.addAppender(fileAppender(context, "we\"ird"));

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.JSON, PATTERN);

        final ConsoleAppender<ILoggingEvent> console = (ConsoleAppender<ILoggingEvent>) appendersOf(logger).get(0);
        final LoggingEvent                   event   = new LoggingEvent("fqcn", logger, Level.INFO, "m", null, null);
        // The custom field is valid JSON with the quote escaped; encoding does not throw or lose the line.
        final String json = new String(console.getEncoder().encode(event), StandardCharsets.UTF_8);
        assertThat(json).contains("we\\\"ird").contains("\"message\":\"m\"");
    }

    // ---- reviewer-flagged gaps ----

    /** Reproducible dup case: a logger already carrying a ConsoleAppender must not gain a second one. */
    @Test
    public void dropsFileAppenderInsteadOfDuplicatingWhenConsoleAlreadyPresent() {
        final LoggerContext context = new LoggerContext();
        final Logger        root    = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(fileAppender(context, "logfile"));
        final ConsoleAppender<ILoggingEvent> existing = startedConsole(context, "console");
        root.addAppender(existing);

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

        final List<Appender<ILoggingEvent>> after = appendersOf(root);
        assertThat(after).hasSize(1);
        assertThat(after.get(0)).as("the pre-existing console is kept, the file appender dropped").isSameAs(existing);
        assertThat(after).noneMatch(FileAppender.class::isInstance);
    }

    /** The source==null fallback: root with no appender of its own gets an untagged "CONSOLE". */
    @Test
    public void addsUntaggedFallbackConsoleToRootWhenItHasNone() {
        final LoggerContext context = new LoggerContext();
        final Logger        x       = context.getLogger("x");
        x.setAdditive(false);
        x.addAppender(fileAppender(context, "security"));
        // root deliberately has no appenders of its own

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

        final List<Appender<ILoggingEvent>> rootAppenders = appendersOf(context.getLogger(Logger.ROOT_LOGGER_NAME));
        assertThat(rootAppenders).hasSize(1);
        final ConsoleAppender<ILoggingEvent> fallback = (ConsoleAppender<ILoggingEvent>) rootAppenders.get(0);
        assertThat(fallback.getName()).isEqualTo("CONSOLE");
        // no source appender -> pattern is the untouched default, not [name]-tagged
        assertThat(((PatternLayoutEncoder) fallback.getEncoder()).getPattern()).isEqualTo(PATTERN);
    }

    @Test
    public void tagPlacesNameAfterTimestampAcrossDatePatterns() {
        assertThat(DefaultLoggingService.tagWithName("%d [%t] %m%n", "a")).contains("%d [a]");
        assertThat(DefaultLoggingService.tagWithName("%date{ISO8601} %m%n", "a"))
                .startsWith("%date{ISO8601} [a]").doesNotStartWith("[a]");
        assertThat(DefaultLoggingService.tagWithName("%d{HH:mm:ss} %m%n", "a")).contains("%d{HH:mm:ss} [a]");
        assertThat(DefaultLoggingService.tagWithName("%m%n", "a")).startsWith("[a] ");
    }

    @Test
    public void coalesceConfigValuePrefersPropertyThenEnvIgnoringBlanks() {
        assertThat(DefaultLoggingService.coalesceConfigValue("json", null)).isEqualTo("json");
        assertThat(DefaultLoggingService.coalesceConfigValue("plain", "json")).isEqualTo("plain");
        assertThat(DefaultLoggingService.coalesceConfigValue("", "json")).isEqualTo("json");   // blank -D falls to env
        assertThat(DefaultLoggingService.coalesceConfigValue("   ", "json")).isEqualTo("json");
        assertThat(DefaultLoggingService.coalesceConfigValue("  json  ", null)).isEqualTo("json");
        assertThat(DefaultLoggingService.coalesceConfigValue(null, null)).isNull();
    }

    @Test
    public void stopsDetachedFileAppenders() throws Exception {
        final LoggerContext context = new LoggerContext();
        final Path          tmp     = Files.createTempFile("redirect-test", ".log");
        try {
            final FileAppender<ILoggingEvent> file = new FileAppender<>();
            file.setContext(context);
            file.setName("tmp");
            file.setFile(tmp.toString());
            final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
            encoder.setContext(context);
            encoder.setPattern(PATTERN);
            encoder.start();
            file.setEncoder(encoder);
            file.start();
            assertThat(file.isStarted()).isTrue();

            final Logger logger = context.getLogger("org.nrg.xnat.security");
            logger.setAdditive(false);
            logger.addAppender(file);

            DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

            assertThat(file.isStarted()).as("detached file appender should be stopped so its handle closes").isFalse();
        } finally {
            Files.deleteIfExists(tmp);
        }
    }

    @Test
    public void sharesOneConsoleAcrossLoggersReferencingTheSameFileAppender() {
        final LoggerContext context = new LoggerContext();
        final FileAppender<ILoggingEvent> shared = fileAppender(context, "shared");
        final Logger a = context.getLogger("a");
        final Logger b = context.getLogger("b");
        a.setAdditive(false);
        b.setAdditive(false);
        a.addAppender(shared);
        b.addAppender(shared);

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

        final Appender<ILoggingEvent> consoleA = appendersOf(a).get(0);
        final Appender<ILoggingEvent> consoleB = appendersOf(b).get(0);
        assertThat(consoleA).isInstanceOf(ConsoleAppender.class).isSameAs(consoleB);
    }

    @Test
    public void copiesSourceFiltersOntoTheConsole() {
        final LoggerContext context = new LoggerContext();
        final Logger        logger  = context.getLogger("org.nrg.xnat.security");
        logger.setAdditive(false);
        final FileAppender<ILoggingEvent> file   = fileAppender(context, "security");
        final ThresholdFilter             filter = new ThresholdFilter();
        filter.setLevel("WARN");
        filter.setContext(context);
        filter.start();
        file.addFilter(filter);
        logger.addAppender(file);

        DefaultLoggingService.redirectFileAppendersToConsole(context, ConsoleFormat.PLAIN, PATTERN);

        final ConsoleAppender<ILoggingEvent> console = (ConsoleAppender<ILoggingEvent>) appendersOf(logger).get(0);
        assertThat(console.getCopyOfAttachedFiltersList()).anyMatch(ThresholdFilter.class::isInstance);
    }

    @Test
    public void parsesFormatTokensAndRejectsUnknownValues() {
        assertThat(ConsoleFormat.from("plain")).isEqualTo(ConsoleFormat.PLAIN);
        assertThat(ConsoleFormat.from("json")).isEqualTo(ConsoleFormat.JSON);
        assertThat(ConsoleFormat.from("  JSON ")).isEqualTo(ConsoleFormat.JSON);
        assertThat(ConsoleFormat.from("file")).isNull();
        assertThat(ConsoleFormat.from("")).isNull();
    }

    // ---- helpers ----

    private LoggerContext contextWithFileAppenders() {
        final LoggerContext context = new LoggerContext();
        final Logger security = context.getLogger("org.nrg.xnat.security");
        security.setAdditive(false);
        security.addAppender(fileAppender(context, "security"));
        context.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(fileAppender(context, "app"));
        return context;
    }

    private FileAppender<ILoggingEvent> fileAppender(final LoggerContext context, final String name) {
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(PATTERN);
        final FileAppender<ILoggingEvent> appender = new FileAppender<>();
        appender.setContext(context);
        appender.setName(name);
        appender.setEncoder(encoder);
        // deliberately not started -> no file handle; the redirect only reads the name + encoder + filters
        return appender;
    }

    private ConsoleAppender<ILoggingEvent> startedConsole(final LoggerContext context, final String name) {
        final PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(PATTERN);
        encoder.start();
        final ConsoleAppender<ILoggingEvent> console = new ConsoleAppender<>();
        console.setContext(context);
        console.setName(name);
        console.setEncoder(encoder);
        console.start();
        return console;
    }

    private List<Appender<ILoggingEvent>> appendersOf(final Logger logger) {
        final List<Appender<ILoggingEvent>> list = new ArrayList<>();
        for (final Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
            list.add(it.next());
        }
        return list;
    }
}
