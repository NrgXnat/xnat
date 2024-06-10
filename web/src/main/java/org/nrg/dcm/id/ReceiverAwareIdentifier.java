package org.nrg.dcm.id;

import org.nrg.dcm.scp.DicomSCPInstance;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xnat.DicomObjectIdentifier;

public interface ReceiverAwareIdentifier<T extends DicomObjectIdentifier<XnatProjectdata>> {
    T forInstance(DicomSCPInstance instance);
}
