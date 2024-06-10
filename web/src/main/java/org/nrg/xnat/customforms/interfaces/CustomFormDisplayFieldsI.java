package org.nrg.xnat.customforms.interfaces;

import org.nrg.xdat.schema.SchemaElement;
import org.nrg.xft.XFTTable;

import java.util.List;

public interface CustomFormDisplayFieldsI {
    void addDisplayFields(final SchemaElement se, final Object o, List<String> addedJsonFields, XFTTable fields);
}
