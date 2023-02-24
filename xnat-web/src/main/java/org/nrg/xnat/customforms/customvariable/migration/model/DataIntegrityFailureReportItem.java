package org.nrg.xnat.customforms.customvariable.migration.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class DataIntegrityFailureReportItem {

    public DataIntegrityFailureReportItem(final String entityId) {
        this.entityId = entityId;
    }

    private void addDataIntegrityItem(DataIntegrityItem di) {
        dataIntegrityItems.add(di);
    }

    public void addDataIntegrityItem(final String name, final String type, final String value) {
        DataIntegrityItem di = new DataIntegrityItem();
        di.setFieldName(name);
        di.setExpectedFormat(type);
        di.setDataFound(value);
        addDataIntegrityItem(di);
        return;
    }

    public String getEntityId() {
        return entityId;
    }

    public List<DataIntegrityItem> getDataIntegrityItems() {
        return dataIntegrityItems;
    }
    private String entityId;
    private List<DataIntegrityItem> dataIntegrityItems = new ArrayList<DataIntegrityItem>();
}
