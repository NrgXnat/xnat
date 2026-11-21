package org.nrg.xdat.collections;

import org.junit.Test;
import org.nrg.xdat.display.DisplayField;
import org.nrg.xdat.display.ElementDisplay;
import org.nrg.xdat.search.DisplayFieldWrapper;

import static org.assertj.core.api.Assertions.assertThat;

public class TestDisplayFieldWrapperCollection {
    /**
     * XNAT-6374: values that clean or lower-case to the same row ID would otherwise share one result column, so one field
     * shows the other's data.
     */
    @Test
    public void testCollidingRowIDsAreDistinguished() {
        final ElementDisplay                display    = createDisplay("xnat:mrSessionData");
        final DisplayField                  fieldMap   = createField(display, "XNAT_MRSESSIONDATA_FIELD_MAP");
        final DisplayFieldWrapperCollection collection = new DisplayFieldWrapperCollection();
        collection.addDisplayField(fieldMap, "dose_alpha");
        collection.addDisplayField(fieldMap, "dose-alpha");
        collection.addDisplayField(fieldMap, "Segment");
        collection.addDisplayField(fieldMap, "segment");
        collection.addDisplayField(fieldMap, "weight-kg");

        final String doseAlphaUnderscore = rowID(collection, "xnat:mrSessionData.XNAT_MRSESSIONDATA_FIELD_MAP.dose_alpha");
        final String doseAlphaHyphen     = rowID(collection, "xnat:mrSessionData.XNAT_MRSESSIONDATA_FIELD_MAP.dose-alpha");
        final String segmentUpper        = rowID(collection, "xnat:mrSessionData.XNAT_MRSESSIONDATA_FIELD_MAP.Segment");
        final String segmentLower        = rowID(collection, "xnat:mrSessionData.XNAT_MRSESSIONDATA_FIELD_MAP.segment");

        assertThat(doseAlphaHyphen).isNotEqualToIgnoringCase(doseAlphaUnderscore);
        assertThat(segmentUpper).isNotEqualToIgnoringCase(segmentLower);

        // A value that cleaning and lower-casing leave alone keeps its row ID, and so does a field that collides with nothing.
        assertThat(doseAlphaUnderscore).isEqualTo("XNAT_MRSESSIONDATA_FIELD_MAP_dose_alpha");
        assertThat(segmentLower).isEqualTo("XNAT_MRSESSIONDATA_FIELD_MAP_segment");
        assertThat(rowID(collection, "xnat:mrSessionData.XNAT_MRSESSIONDATA_FIELD_MAP.weight-kg")).isEqualTo("XNAT_MRSESSIONDATA_FIELD_MAP_weight_kg");
    }

    /**
     * The batch-launch plugin finds a pipeline's container ID column by replacing wrk_status with wrk_status_cid in its
     * status column's row ID, so both columns must get the same suffix.
     */
    @Test
    public void testPipelineStatusColumnsStayPaired() {
        final ElementDisplay                display     = createDisplay("xnat:petSessionData");
        final DisplayField                  status      = createField(display, "WRK_STATUS");
        final DisplayField                  containerId = createField(display, "WRK_STATUS_CID");
        final DisplayFieldWrapperCollection collection  = new DisplayFieldWrapperCollection();
        for (final String pipeline : new String[]{"segmentation_model_v2.4-scan", "segmentation_model_v2_4-scan"}) {
            collection.addDisplayField(status, pipeline);
            collection.addDisplayField(containerId, pipeline);
        }

        final String dotStatus        = rowID(collection, "xnat:petSessionData.WRK_STATUS.segmentation_model_v2.4-scan").toLowerCase();
        final String underscoreStatus = rowID(collection, "xnat:petSessionData.WRK_STATUS.segmentation_model_v2_4-scan").toLowerCase();
        assertThat(dotStatus).isNotEqualTo(underscoreStatus);
        assertThat(dotStatus.replace("wrk_status", "wrk_status_cid")).isEqualTo(rowID(collection, "xnat:petSessionData.WRK_STATUS_CID.segmentation_model_v2.4-scan").toLowerCase());
        assertThat(underscoreStatus.replace("wrk_status", "wrk_status_cid")).isEqualTo(rowID(collection, "xnat:petSessionData.WRK_STATUS_CID.segmentation_model_v2_4-scan").toLowerCase());
    }

    @Test
    public void testReplacingAFieldDoesNotDistinguishIt() {
        final DisplayField                  fieldMap   = createField(createDisplay("xnat:mrSessionData"), "XNAT_MRSESSIONDATA_FIELD_MAP");
        final DisplayFieldWrapperCollection collection = new DisplayFieldWrapperCollection();
        collection.addDisplayField(fieldMap, "dose-alpha");
        collection.addDisplayField(fieldMap, "dose-alpha");
        assertThat(rowID(collection, "xnat:mrSessionData.XNAT_MRSESSIONDATA_FIELD_MAP.dose-alpha")).isEqualTo("XNAT_MRSESSIONDATA_FIELD_MAP_dose_alpha");
    }

    private static ElementDisplay createDisplay(final String elementName) {
        final ElementDisplay display = new ElementDisplay();
        display.setElementName(elementName);
        return display;
    }

    private static DisplayField createField(final ElementDisplay display, final String id) {
        final DisplayField field = new DisplayField(display);
        field.setId(id);
        return field;
    }

    private static String rowID(final DisplayFieldWrapperCollection collection, final String id) {
        final DisplayFieldWrapper wrapper = collection.getDisplayField(id);
        assertThat(wrapper).as("display field " + id).isNotNull();
        return wrapper.getRowID();
    }
}
