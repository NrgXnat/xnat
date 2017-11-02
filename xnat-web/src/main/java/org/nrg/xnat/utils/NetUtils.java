/*
 * web: org.nrg.xnat.utils.NetUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.util.concurrent.Callable;

public class NetUtils {
    public static boolean isPortAvailable(int port) {
        try (final ServerSocket serverSocket = new ServerSocket(port)) {
            serverSocket.setReuseAddress(true);
            try (final DatagramSocket datagramSocket = new DatagramSocket(port)) {
                datagramSocket.setReuseAddress(true);
            }
            return true;
        } catch (IOException ignored) {
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
