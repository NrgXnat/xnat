package org.nrg.xft.serializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.io.IOException;

public class XFTItemDeserializer extends StdDeserializer<XFTItem> {
    private static final long serialVersionUID = 6892630626174384077L;

    public XFTItemDeserializer() {
        this(XFTItem.class);
    }

    public XFTItemDeserializer(final Class<XFTItem> type) {
        super(type);
    }

    @Override
    public XFTItem deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        String value = null;
        String userName = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "value":
                    value = parser.getText();
                    break;
                case "user":
                    userName = parser.getText();
                    break;
            }
        }
        try {
            UserI user = null;
            if (userName != null) {
                user = XDAT.getUserDetails();
            }
            return XFTItem.PopulateItemFromFlatString(value, user == null || user.isGuest() ? null : user);
        } catch (Exception e) {
            throw new IOException("An error occurred trying to deserialize the XFTItem from the JSON object.", e);
        }
    }

    @Override
    public XFTItem deserializeWithType(JsonParser parser, DeserializationContext ctx, TypeDeserializer typeSer) throws IOException {
        return deserialize(parser, ctx);
    }
}
