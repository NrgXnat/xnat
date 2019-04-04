package org.nrg.xft.schema;

import org.w3c.dom.Document;

import javax.annotation.Nullable;

public interface DataTypeSchemaService {
    /**
     * Retrieves the schema document at the specified namespace and path.
     *
     * @param namespace The schema namespace.
     * @param schema    The schema name.
     *
     * @return The requested document if found, <b>null</b> if not found.
     */
    @Nullable
    Document getSchema(String namespace, String schema);

    /**
     * Retrieves the schema document at the specified path.
     *
     * @param schema The schema name.
     *
     * @return The requested document if found, <b>null</b> if not found.
     */
    @Nullable
    Document getSchema(String schema);

    /**
     * Retrieves the contents of the schema document at the specified namespace and path.
     *
     * @param namespace The schema namespace.
     * @param schema    The schema name.
     *
     * @return The contents of the requested document if found, <b>null</b> if not found.
     */
    @Nullable
    String getSchemaContents(String namespace, String schema);

    /**
     * Retrieves the contents of the schema document at the specified path.
     *
     * @param schema    The schema name.
     *
     * @return The contents of the requested document if found, <b>null</b> if not found.
     */
    @Nullable
    String getSchemaContents(String schema);
}
