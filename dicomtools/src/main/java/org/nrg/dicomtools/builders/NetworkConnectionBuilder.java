/*
 * dicomtools: org.nrg.dicomtools.builders.NetworkConnectionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.builders;

import org.dcm4che3.net.Connection;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class NetworkConnectionBuilder {
  private final Connection c;

  public NetworkConnectionBuilder() {
    c = new Connection();
  }

  public Connection build() { return c; }


  public NetworkConnectionBuilder setHostname(final String hostname) {
    c.setHostname(hostname);
    return this;
  }

  public NetworkConnectionBuilder setPort(final int port) {
    c.setPort(port);
    return this;
  }

  public enum TlsType {
  	NO_ENCRYPTION,
  	TRIPLE_DES,
  	AES
  }

  public NetworkConnectionBuilder setTls(final TlsType tlsType) {
  	switch (tlsType) {
  	case NO_ENCRYPTION:
  		// dcm4che3 uses setTlsCipherSuites with empty array for no encryption
  		c.setTlsCipherSuites();
  		break;
  	case TRIPLE_DES:
  		c.setTlsCipherSuites("TLS_RSA_WITH_3DES_EDE_CBC_SHA");
  		break;
  	case AES:
  		c.setTlsCipherSuites("TLS_RSA_WITH_AES_128_CBC_SHA");
  		break;
  	}
  	return this;
  }

  public NetworkConnectionBuilder setTlsCipherSuite(final String...ciphers) {
  	c.setTlsCipherSuites(ciphers);
  	return this;
  }

  public NetworkConnectionBuilder setTlsProtocol(final String...protocol) {
  	c.setTlsProtocols(protocol);
  	return this;
  }

  public NetworkConnectionBuilder setTlsNeedClientAuth(final boolean needClientAuth) {
  	c.setTlsNeedClientAuth(needClientAuth);
  	return this;
  }
}
