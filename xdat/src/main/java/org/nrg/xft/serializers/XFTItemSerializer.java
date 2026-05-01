package org.nrg.xft.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.io.IOException;

public class XFTItemSerializer extends StdSerializer<XFTItem> {
    private static final long serialVersionUID = -2048938268024164294L;

    @SuppressWarnings("unused")
    public XFTItemSerializer() {
        this(XFTItem.class);
    }

    public XFTItemSerializer(final Class<XFTItem> type) {
        super(type);
    }

    @Override
    public void serialize(final XFTItem element, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        final String flatString = element.writeToFlatString(0);
        generator.writeStringField("value", flatString);
        UserI user = element.getUser();
        if (user != null) {
            generator.writeStringField("user", user.getLogin());
        }
    }

    @Override
    public void serializeWithType(final XFTItem element, final JsonGenerator generator, final SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefix(generator, typeSer.typeId(element, JsonToken.START_OBJECT));
        serialize(element, generator, provider);
        typeSer.writeTypeSuffix(generator, typeSer.typeId(element, JsonToken.END_OBJECT));
    }
}
