/*
 * dicomtools: org.nrg.dcm.DimseRSPStatusHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che3.net.DimseRSPHandler;

/**
 * Records the service status the remote AE returned for a DIMSE request. Subclasses map the status codes for their
 * particular service by overriding {@link DimseRSPHandler#onDimseRSP} and calling {@link #setSuccess()},
 * {@link #setWarning(int, String, String)} or {@link #setFailure(int, String, String)}.
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public abstract class DimseRSPStatusHandler extends DimseRSPHandler {
  public DimseRSPStatusHandler(int msgId) {
    super(msgId);
  }

  public enum ServiceStatus { SUCCESS, WARNING, FAILURE }

  private ServiceStatus status = null;
  private int statusCode;
  private String meaning = null;
  private String comment = null;
  
  private void setFields(final ServiceStatus status,
      final int statusCode, final String meaning, final String comment) {
    if (null != this.status) throw new IllegalStateException("already set: " + this);
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
  
  /**
   * Indicates whether a status has been recorded for this request yet. This is false when the remote AE never returned
   * a response, in which case the accessors below all throw {@link IllegalStateException}.
   *
   * @return true if the remote AE returned a status.
   */
  public final boolean hasStatus() {
    return null != status;
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
