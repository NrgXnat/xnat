package org.nrg.xnat.services.logging.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.FileAppender;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.Test;
import org.nrg.xnat.services.logging.impl.DefaultLoggingService.ConsoleFormat;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link DefaultLoggingService#redirectFileAppendersToConsole} swaps every file appender (core or
 * plugin) for a console appender -- plain text or JSON -- with no archive surgery and no plugin changes.
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

        // the named logger's file appender is now a console, keeping its name
        final List<Appender<ILoggingEvent>> security = appendersOf(context.getLogger("org.nrg.xnat.security"));
        assertThat(security).hasSize(1);
        assertThat(security.get(0)).isInstanceOf(ConsoleAppender.class);
        assertThat(security.get(0).getName()).isEqualTo("security");

        // root ends up with a console too
        assertThat(appendersOf(context.getLogger(Logger.ROOT_LOGGER_NAME)))
                .anyMatch(ConsoleAppender.class::isInstance);
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

    @Test
    public void parsesFormatTokensAndRejectsUnknownValues() {
        assertThat(ConsoleFormat.from("plain")).isEqualTo(ConsoleFormat.PLAIN);
        assertThat(ConsoleFormat.from("json")).isEqualTo(ConsoleFormat.JSON);
        assertThat(ConsoleFormat.from("  JSON ")).isEqualTo(ConsoleFormat.JSON);
        // unrecognized values map to null -> caller leaves file logging unchanged (fail-safe)
        assertThat(ConsoleFormat.from("file")).isNull();
        assertThat(ConsoleFormat.from("")).isNull();
    }

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
        // deliberately not started -> no file handle; the redirect only reads the name + encoder
        return appender;
    }

    private List<Appender<ILoggingEvent>> appendersOf(final Logger logger) {
        final List<Appender<ILoggingEvent>> list = new ArrayList<>();
        for (final Iterator<Appender<ILoggingEvent>> it = logger.iteratorForAppenders(); it.hasNext(); ) {
            list.add(it.next());
        }
        return list;
    }
}
