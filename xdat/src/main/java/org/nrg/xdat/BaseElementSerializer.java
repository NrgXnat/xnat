package org.nrg.xdat;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.nrg.xdat.base.BaseElement;

import java.io.IOException;

public class BaseElementSerializer extends StdSerializer<BaseElement> {
    private static final long serialVersionUID = -8436897229364709726L;

    @SuppressWarnings("unused")
    public BaseElementSerializer() {
        this(BaseElement.class);
    }

    public BaseElementSerializer(final Class<BaseElement> type) {
        super(type);
    }

    @Override
    public void serialize(final BaseElement element, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
        final String flatString = element.getItem().writeToFlatString(0);
        generator.writeStringField("xftItem", flatString);
        if (element.getUser() != null) {
            generator.writeStringField("user", element.getUser().getLogin());
        }
    }

    @Override
    public void serializeWithType(final BaseElement element, final JsonGenerator generator, final SerializerProvider provider, TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefix(generator, typeSer.typeId(element, JsonToken.START_OBJECT));
        serialize(element, generator, provider);
        typeSer.writeTypeSuffix(generator, typeSer.typeId(element, JsonToken.END_OBJECT));
    }
}


