/*
 * xnat-web: org.nrg.xnat.web.converters.ZipFileHttpMessageConverter
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.web.converters;

import lombok.extern.slf4j.Slf4j;
import org.nrg.xft.utils.zip.ZipUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class ZipFileHttpMessageConverter extends AbstractHttpMessageConverter<Map<String, File>> {
    public ZipFileHttpMessageConverter() {
        super(MediaType.parseMediaType("application/zip"));
    }

    @Override
    protected boolean supports(final Class clazz) {
        return true;
    }

    @Override
    protected Map<String, File> readInternal(final Class clazz, final HttpInputMessage message) throws IOException, HttpMessageNotReadableException {
        final File directory = Files.createTempDirectory(Long.toString(Calendar.getInstance().getTimeInMillis())).toFile();
        directory.deleteOnExit();

        final Map<String, File> entries = new ZipUtils().extractMap(message.getBody(), directory.getAbsolutePath());
        return entries.keySet().stream().filter(path -> entries.get(path).isFile()).collect(toMap(Function.identity(), entries::get));
    }

    @Override
    protected void writeInternal(final Map<String, File> files, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        final ZipUtils zipper = new ZipUtils();
        zipper.setOutputStream(outputMessage.getBody());
        files.keySet().forEach(path -> writeFile(zipper, path, files.get(path)));
    }

    private void writeFile(final ZipUtils zipper, final String path, final File file) {
        try {
            zipper.write(path, file);
        } catch (IOException e) {
            log.warn("An error occurred writing the file {} to a Zip output stream", file.getPath());
        }
    }
}
