/*
 * dicomtools: org.dcm4che2.net.NetworkApplicationEntityBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.dcm4che2.net;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class NetworkApplicationEntityBuilder {
  private final NetworkApplicationEntity entity;
  
  public NetworkApplicationEntityBuilder() {
    entity = new NetworkApplicationEntity();
  }
  
  public NetworkApplicationEntity build() { return entity; }
  
  
  public NetworkApplicationEntityBuilder setAETitle(final String aeTitle) {
    entity.setAETitle(aeTitle);
    return this;
  }
  
  public NetworkApplicationEntityBuilder setNetworkConnection(final NetworkConnection c) {
    entity.setNetworkConnection(c);
    return this;
  }
  
  public NetworkApplicationEntityBuilder setAssociationInitiator() {
    entity.setAssociationInitiator(true);
    return this;
  }
  
  public NetworkApplicationEntityBuilder setAssociationAcceptor() {
    entity.setAssociationAcceptor(true);
    return this;
  }
  
  public NetworkApplicationEntityBuilder setTransferCapability(final TransferCapability...tcs) {
    entity.setTransferCapability(tcs);
    return this;
  }
}
