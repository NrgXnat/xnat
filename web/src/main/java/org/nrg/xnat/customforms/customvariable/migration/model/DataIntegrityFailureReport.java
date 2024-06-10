package org.nrg.xnat.customforms.customvariable.migration.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DataIntegrityFailureReport {

    public void addDataIntegrityReportItem(DataIntegrityFailureReportItem di) {
        dataIntegrityReportItems.add(di);
    }

    private List<DataIntegrityFailureReportItem> dataIntegrityReportItems = new ArrayList<DataIntegrityFailureReportItem>();
    private String projectId;
}
