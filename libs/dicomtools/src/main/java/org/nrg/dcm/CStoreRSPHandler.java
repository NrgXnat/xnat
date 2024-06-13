/*
 * dicomtools: org.nrg.dcm.CStoreRSPHandler
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dcm;

import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.net.Association;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public class CStoreRSPHandler extends DimseRSPStatusHandler {
  /* (non-Javadoc)
   * @see org.nrg.dcm.DimseRSPMaybeExceptionHandler#onDimseRSP(org.dcm4che2.net.Association, org.dcm4che2.data.DicomObject, org.dcm4che2.data.DicomObject)
   */
  public void onDimseRSP(final Association as, final DicomObject cmd, final DicomObject data) {
    final int status = cmd.getInt(Tag.Status);
    switch (status) {
    case 0:
      setSuccess();
      return;

    case 0xB000:
      setWarning(status, "Coercion of Data Elements", cmd.getString(Tag.ErrorComment));
      return;

    case 0xB007:
      setWarning(status, "Data Set does not match SOP Class", cmd.getString(Tag.ErrorComment));
      return;
      
    case 0xB006:
      setWarning(status, "Elements Discarded", cmd.getString(Tag.ErrorComment));
      return;

    default:
      if (0xA700 == (status & 0xff00)) {
        setFailure(status, "Out of Resources", cmd.getString(Tag.ErrorComment));
        return;
      } else if (0xA900 == (status & 0xff00)) {
        setFailure(status, "Data Set does not match SOP class", cmd.getString(Tag.ErrorComment));
        return;
      } else if (0xC000 == (status & 0xf000)) {
        setFailure(status, "Cannot understand", cmd.getString(Tag.ErrorComment));
        return;
      } else {
        final Logger logger = LoggerFactory.getLogger(CStoreRSPHandler.class);
        logger.error(String.format("Unexpected RSP status %1$04x: %2s", status, cmd.getString(Tag.ErrorComment)));
        setFailure(status, "Noncompliant DIMSE error", cmd.getString(Tag.ErrorComment));
        return;
      }
    }
  }
}
