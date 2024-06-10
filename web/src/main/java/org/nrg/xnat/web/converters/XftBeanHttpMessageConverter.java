package org.nrg.xnat.web.converters;

import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Handles converting XFT classes
 */
@Component
public class XftBeanHttpMessageConverter extends AbstractHttpMessageConverter<BaseElement> {
    public XftBeanHttpMessageConverter() {
        super(MediaType.APPLICATION_XML);
    }

    @Override
    protected boolean supports(final Class<?> clazz) {
        return BaseElement.class.isAssignableFrom(clazz);
    }

    @Override
    protected BaseElement readInternal(final Class<? extends BaseElement> clazz, final HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        try (final InputStream input = inputMessage.getBody()) {
            final XDATXMLReader reader = new XDATXMLReader();
            return reader.parse(input);
        } catch (SAXException e) {
            throw new HttpMessageNotReadableException("Couldn't read the requested input", e);
        }
    }

    @Override
    protected void writeInternal(final BaseElement baseElement, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        try (final Writer output = new OutputStreamWriter(outputMessage.getBody())) {
            baseElement.toXML(output);
        } catch (Exception e) {
            throw new HttpMessageNotReadableException("An error occurred writing the provided object", e);
        }
    }
}
