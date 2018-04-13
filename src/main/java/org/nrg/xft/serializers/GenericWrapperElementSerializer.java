package org.nrg.xft.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;

import java.io.IOException;

public class GenericWrapperElementSerializer extends StdSerializer<GenericWrapperElement> {
    @SuppressWarnings("unused")
    public GenericWrapperElementSerializer() {
        this(GenericWrapperElement.class);
    }

    public GenericWrapperElementSerializer(final Class<GenericWrapperElement> type) {
        super(type);
    }

    @Override
    public void serialize(final GenericWrapperElement element, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        generator.writeStringField("label", element.getName());
        generator.writeStringField("sqlName", element.getSQLName());
        generator.writeStringField("name", element.getName());
        generator.writeStringField("properName", element.getProperName());
        generator.writeStringField("formattedName", element.getFormattedName());
        generator.writeStringField("xmlName", element.getFullXMLName());
        generator.writeStringField("type", element.getType().toString());
        generator.writeStringField("namespacePrefix", element.getSchemaTargetNamespacePrefix());
        generator.writeStringField("namespaceURI", element.getSchemaTargetNamespaceURI());
        generator.writeObjectFieldStart("defaultKey");
        generator.writeStringField(element.getDefaultKey().getName(), element.getDefaultKey().getWrapped().getXMLType().toString());
        generator.writeEndObject();
        generator.writeObjectFieldStart("fields");
        for (final Object object : element.getAllFields()) {
            final GenericWrapperField field = (GenericWrapperField) object;
            generator.writeObjectFieldStart(field.getName());
            generator.writeStringField("prefix", field.getPrefix());
            generator.writeStringField("xmlPath", field.getXMLPathString());
            generator.writeStringField("xmlType", field.getXMLType().toString());
            generator.writeEndObject();
        }
        generator.writeEndObject();
        generator.writeEndObject();
    }
}
