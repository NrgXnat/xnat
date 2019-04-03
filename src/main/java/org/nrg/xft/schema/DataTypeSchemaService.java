package org.nrg.xft.schema;

import org.w3c.dom.Document;

public interface DataTypeSchemaService {
    Document getSchema(String namespace, String schema);

    Document getSchema(String schemaPath);

    String getSchemaContents(String namespace, String schema);

    String getSchemaContents(String schemaPath);
}
