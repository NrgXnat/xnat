/*
 * DicomUtils: org.dcm4che2.net.NetworkConnectionBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.dcm4che2.net;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class NetworkConnectionBuilder {
  private final NetworkConnection c;
  
  public NetworkConnectionBuilder() {
    c = new NetworkConnection();
  }
  
  public NetworkConnection build() { return c; }
  
  
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
  	case NO_ENCRYPTION: c.setTlsWithoutEncyrption(); break;
  	case TRIPLE_DES: c.setTls3DES_EDE_CBC(); break;
  	case AES: c.setTlsAES_128_CBC(); break;
  	}
  	return this;
  }
  
  public NetworkConnectionBuilder setTlsCipherSuite(final String...ciphers) {
  	c.setTlsCipherSuite(ciphers);
  	return this;
  }
  
  public NetworkConnectionBuilder bar(final String...protocol) {
  	c.setTlsProtocol(protocol);
  	return this;
  }
  
  public NetworkConnectionBuilder enableSSLv2Hello() {
  	c.enableSSLv2Hello();
  	return this;
  }
  
  public NetworkConnectionBuilder disableSSLv2Hello() {
  	c.disableSSLv2Hello();
  	return this;
  }
  
  public NetworkConnectionBuilder setTlsNeedClientAuth(final boolean needClientAuth) {
  	c.setTlsNeedClientAuth(needClientAuth);
  	return this;
  }
}
