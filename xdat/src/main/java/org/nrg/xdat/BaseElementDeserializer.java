package org.nrg.xdat;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.nrg.xdat.base.BaseElement;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.security.UserI;

import java.io.IOException;

public class BaseElementDeserializer extends StdDeserializer<ItemI> {
    public BaseElementDeserializer() {
        this(ItemI.class);
    }

    public BaseElementDeserializer(final Class<ItemI> type) {
        super(type);
    }

    @Override
    public ItemI deserialize(final JsonParser parser, final DeserializationContext context) throws IOException {
        String userName = null;
        String value = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "xftItem":
                    value = parser.getText();
                    break;
                case "user":
                    userName = parser.getText();
                    break;
                default:
            }
        }
        try {
            UserI user = (userName == null) ? null : XDAT.getUserDetails();
            XFTItem xftItem = XFTItem.PopulateItemFromFlatString(value, user == null || user.isGuest() ? null : user);
            return BaseElement.GetGeneratedItem(xftItem);
        } catch (Exception e) {
            throw new IOException("An error occurred trying to deserialize the BaseElement from the JSON object.", e);
        }
    }
}