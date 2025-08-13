package org.nrg.xnat.contrast.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(prefix = "_")
public class Administration {

    private String _volume;
    private String _startTime;
    private String _endTime;
    private String _totalDose;
    private String _flowRate;
    private String _duration;
}
