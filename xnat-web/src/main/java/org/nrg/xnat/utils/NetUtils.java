/*
 * org.nrg.xnat.utils.NetUtils
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.utils;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public class NetUtils {

    public static void main(String[] args) throws Exception {
	int port = Integer.valueOf(args[0]);
	System.out.println("Port " + port + " available? " + isPortAvailable(port));
	//occupyTcpPort(port);
    }

    public static boolean isPortAvailable(int port) {
	ServerSocket ss = null;
	DatagramSocket ds = null;
	try {
	    ss = new ServerSocket(port);
	    ss.setReuseAddress(true);
	    ds = new DatagramSocket(port);
	    ds.setReuseAddress(true);
	    return true;
	} catch (IOException e) {
	} finally {
	    if (ds != null) {
		ds.close();
	    }

	    if (ss != null) {
		try {
		    ss.close();
		} catch (IOException e) {
		    /* should not be thrown */
		}
	    }
	}

	return false;
    }

    public static void occupyTcpPort(int port) throws Exception {
	ServerSocket ss = new ServerSocket(port);
	ss.setReuseAddress(true);
	while (true) {
	    Thread.sleep(1000);
	}
    }
}
