/*
 * dicomtools: org.nrg.dicomtools.builders.NetworkApplicationEntityBuilder
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicomtools.builders;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.TransferCapability;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class NetworkApplicationEntityBuilder {
  private final ApplicationEntity entity;

  public NetworkApplicationEntityBuilder() {
    entity = new ApplicationEntity();
  }

  public ApplicationEntity build() { return entity; }


  public NetworkApplicationEntityBuilder setAETitle(final String aeTitle) {
    entity.setAETitle(aeTitle);
    return this;
  }

  public NetworkApplicationEntityBuilder setNetworkConnection(final Connection c) {
    entity.addConnection(c);
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
    for (TransferCapability tc : tcs) {
      entity.addTransferCapability(tc);
    }
    return this;
  }
}
