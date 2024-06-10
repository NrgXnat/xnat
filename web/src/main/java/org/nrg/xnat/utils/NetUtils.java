/*
 * web: org.nrg.xnat.utils.NetUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NetUtils {
    @SuppressWarnings("unused")
    public static boolean isPortAvailable(final int port) {
        return isPortAvailable(port, 0);
    }

    public static boolean isPortAvailable(final int port, final int attempts) {
        return isPortAvailable(port, attempts, 0);
    }

    public static boolean isPortAvailable(final int port, final int attempts, final int timeout) {
        final int resolvedAttempts = attempts == 0 ? 3 : attempts;
        final int resolvedTimeout  = (timeout > 0 ? timeout * 2 : 1) * 500;
        log.debug("Preparing to check port {} up to {} times if necessary, with a timeout of {} ms between each attempt", port, resolvedAttempts, resolvedTimeout);

        final AtomicInteger count = new AtomicInteger();
        while (count.getAndIncrement() < resolvedAttempts) {
            try (final ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setReuseAddress(true);
                try (final DatagramSocket socket = new DatagramSocket(port)) {
                    socket.setReuseAddress(true);
                }
                return true;
            } catch (IOException e) {
                log.debug("Got an exception of type \"{}\" when creating new ServerSocket instance on port {}, throwing it upstairs for handling.", e.getClass().getName(), port);
                try {
                    Thread.sleep(resolvedTimeout);
                } catch (InterruptedException ex) {
                    log.warn("Got interrupted while trying to wait for the requested port {} to become available", port, ex);
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unused")
    public static void occupyTcpPort(final int port) throws Exception {
        new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final ServerSocket serverSocket = new ServerSocket(port);
                serverSocket.setReuseAddress(true);
                while (serverSocket.isBound()) {
                    Thread.sleep(1000);
                }
                return null;
            }
        }.call();
    }
}
