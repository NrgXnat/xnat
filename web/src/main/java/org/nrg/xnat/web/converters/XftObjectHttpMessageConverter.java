package org.nrg.xnat.web.converters;

import org.apache.commons.io.IOUtils;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.exception.ElementNotFoundException;
import org.nrg.xft.exception.XFTInitException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Handles converting XFT classes
 */
@Component
public class XftObjectHttpMessageConverter extends AbstractHttpMessageConverter<ItemI> {
    public XftObjectHttpMessageConverter() {
        super(MediaType.APPLICATION_XML);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return ItemI.class.isAssignableFrom(clazz);
    }

    @Override
    protected ItemI readInternal(final Class<? extends ItemI> clazz, final HttpInputMessage message) throws IOException, HttpMessageNotReadableException {
        try (final InputStreamReader input = new InputStreamReader(message.getBody())) {
            final String value = IOUtils.toString(input);
            return XFTItem.NewItem(value, null);
        } catch (ElementNotFoundException e) {
            throw new HttpMessageNotReadableException("Couldn't create the requested type of object: " + e.ELEMENT, e);
        } catch (XFTInitException e) {
            throw new HttpMessageNotReadableException("An error occurred trying to access XFT", e);
        }
    }

    @Override
    protected void writeInternal(final ItemI item, final HttpOutputMessage message) throws IOException, HttpMessageNotWritableException {
        try (final Writer output = new OutputStreamWriter(message.getBody())) {
            item.toXML(output, true);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException("An error occurred writing the provided object", e);
        }
    }
}
