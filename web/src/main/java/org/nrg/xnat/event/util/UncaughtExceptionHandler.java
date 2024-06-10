package org.nrg.xnat.event.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.fn.Consumer;

@Component
@Slf4j
public class UncaughtExceptionHandler implements Consumer<Throwable> {
    @Override
    public void accept(final Throwable throwable) {
        log.error("An error occurred and I found it.", throwable);
    }
}
