/*
 * DicomUtils: org.nrg.dcm.DimseRSPStatusHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.DimseRSPHandler;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public abstract class DimseRSPStatusHandler extends DimseRSPHandler {
  public static enum ServiceStatus { SUCCESS, WARNING, FAILURE }

  private ServiceStatus status = null;
  private int statusCode;
  private String meaning = null;
  private String comment = null;
  
  public abstract void onDimseRSP(Association as, DicomObject cmd, DicomObject data);

  private void setFields(final ServiceStatus status,
      final int statusCode, final String meaning, final String comment) {
    if (null == status) throw new IllegalStateException("already set: " + this);
    this.status = status;
    this.statusCode = statusCode;
    this.meaning = meaning;
    this.comment = comment;
  }
  
  public final void setSuccess() {
    setFields(ServiceStatus.SUCCESS, 0, "Success", null);
  }
  
  public final void setWarning(final int code, final String meaning, final String comment) {
    setFields(ServiceStatus.WARNING, code, meaning, comment);
  }
  
  public final void setFailure(final int code, final String meaning, final String comment) {
    setFields(ServiceStatus.FAILURE, code, meaning, comment);
  }
  
  private final void assertStatus() {
    if (null == status) throw new IllegalStateException("status not set");
  }
  
  public final ServiceStatus getStatus() {
    assertStatus();
    return status;
  }
  
  public final int getStatusCode() {
    assertStatus();
    return statusCode;
  }
  
  public final String getStatusMeaning() {
    assertStatus();
    return meaning;
  }
  
  public final String getErrorComment() {
    assertStatus();
    return comment;
  }
  
  public String toString() {
    if (null == status) return super.toString() + " (unset)";

    final StringBuilder sb = new StringBuilder(status.toString());
    sb.append(" ").append(Integer.toHexString(statusCode));
    sb.append(" ").append(meaning);
    if (null != comment) sb.append(": ").append(comment);
    return sb.toString();
  }
}
