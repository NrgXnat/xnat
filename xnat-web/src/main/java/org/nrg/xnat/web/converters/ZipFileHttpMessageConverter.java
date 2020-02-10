/*
 * xnat-web: org.nrg.xnat.web.converters.ZipFileHttpMessageConverter
 * XNAT http://www.xnat.org
 * Copyright (c) 2019, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.web.converters;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
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

        // With Java 8:
        // final Map<String, File> entries = new ZipUtils().extractMap(message.getBody(), directory.getAbsolutePath());
        // return entries.keySet().stream().filter(path -> entries.get(path).isFile()).collect(toMap(Function.identity(), entries::get));
        return Maps.filterValues(new ZipUtils().extractMap(message.getBody(), directory.getAbsolutePath()), new Predicate<File>() {
            @Override
            public boolean apply(final File file) {
                return file.isFile();
            }
        });
    }

    @Override
    protected void writeInternal(final Map<String, File> files, final HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        final ZipUtils zipper = new ZipUtils();
        zipper.setOutputStream(outputMessage.getBody());
        // With Java 8:
        // files.keySet().forEach(path -> writeFile(zipper, path, files.get(path)));
        for (final String path : files.keySet()) {
            final File file = files.get(path);
            try {
                zipper.write(path, file);
            } catch (IOException e) {
                log.warn("An error occurred writing the file {} to a Zip output stream", file.getPath());
            }
        }
    }
}
