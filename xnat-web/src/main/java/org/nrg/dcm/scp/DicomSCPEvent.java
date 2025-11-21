package org.nrg.dcm.scp;

import lombok.Value;
import lombok.experimental.SuperBuilder;
import org.nrg.framework.event.EventI;

@Value
@SuperBuilder
public class DicomSCPEvent implements EventI {
    private static final long serialVersionUID = 8569245569264053222L;

    long   id;
    String action;
    String aeTitle;
    int    port;
}
