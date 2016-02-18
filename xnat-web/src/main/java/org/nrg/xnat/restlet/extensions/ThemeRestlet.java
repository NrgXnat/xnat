/*
 * org.nrg.xnat.turbine.modules.screens.ManageProtocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu>
 * Last modified 1/25/2016 1:52 PM
 */

package org.nrg.xnat.restlet.extensions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.fileupload.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nrg.action.ClientException;
import org.nrg.xdat.security.helpers.Roles;
import org.nrg.xnat.configuration.ThemeConfig;
import org.nrg.xnat.restlet.XnatRestlet;
import org.nrg.xnat.restlet.resources.SecureResource;
import org.nrg.xnat.restlet.util.FileWriterWrapperI;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by jcleve01 on 1/26/2016.
 */
@XnatRestlet({"/theme"})
public class ThemeRestlet extends SecureResource {
    private static final Log _log = LogFactory.getLog(ThemeRestlet.class);
    private static String themesPath;
    protected final ObjectMapper mapper = new ObjectMapper();
    private static final int FILE_BUFFER_SIZE = 4096;
    private File themeFile = null;

    public ThemeRestlet(Context context, Request request, Response response) throws UnsupportedEncodingException {
        super(context, request, response);
        setModifiable(true);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        themesPath = this.getHttpSession().getServletContext().getRealPath(File.separator)+"themes";
        themeFile = new File(themesPath + File.separator + "theme.json");
        File checkThemesPath = new File(themesPath);
        if (!checkThemesPath.exists()) {
            checkThemesPath.mkdir();
        }
    }

    @Override
    public boolean allowDelete() {
        return true;
    }

    /** // TODO: convert to swagger doc tags
     * Administrator use only
     * Deletes the entire theme package folder specified by the "theme" query parameter. If it happens to be the
     * currently applied theme, the current theme is set to null and the default XNAT theme will be restored.
     */
    @Override
    public void handleDelete() {
        String theme = this.getQueryVariable("theme");
        if(theme != null && !theme.isEmpty()) {
            File f = new File(themesPath + File.separator + theme);
            if (Roles.isSiteAdmin(user)) {
                try {
                    FileUtils.deleteDirectory(f);
                    ThemeConfig tc = getTheme();
                    String themeName = (tc!=null)?tc.getName():null;
                    if(theme != null && theme.equals(themeName)){
                        setTheme(null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Theme directory deletion failed.");
                }
            } else {
                getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Only site administrators can delete themes.");
            }
        }
    }

    @Override
    public boolean allowPut() {
        return true;
    }

    /** // TODO: convert to swagger doc tags
     * Administrator use only
     * Sets the currently selected global theme configuration package by updating the theme.json file under the
     * application's theme folder and caches a copy of it in the app server's application context for reference by any
     * other pages or servlets that may need to alter their styles and behaviors to fit the overriding theme.
     */
    @Override
    public void handlePut() {
        if (Roles.isSiteAdmin(user)) {
            boolean enabled = true;
            String name = this.getQueryVariable("theme"), path = themesPath + File.separator + name;
            if("true".equals(this.getQueryVariable("disabled"))){
                enabled = false;
            }
            setTheme(new ThemeConfig(name, themesPath, enabled));
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Only site administrators can set the site theme.");
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    /** // TODO: convert to swagger doc tags
     * Administrator use only
     * Accepts a multipart form with a zip file upload and extracts its contents in the theme system folder.
     * The structure of the zipped package must have only directories at it's root
     * If successful, the first (root) directory name (or theme name) unzipped is returned in the response.
     * This will overwrite any other directories already existing with the same name without warning.
     */
    @Override
    public void handlePost() { //Representation entity) { // Upload Theme package
        Representation result = null;
        if (Roles.isSiteAdmin(user)) {
            try {
                String firstDirName = "";
                FileWriterWrapperI fw;
                List<FileWriterWrapperI> fws = this.getFileWriters();
                if (fws.size() == 0) {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    getResponse().setEntity("No valid files were uploaded.", MediaType.TEXT_PLAIN);
                    return;
                }
                if (fws.size() > 1) {
                    this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    getResponse().setEntity("Theme importer is limited to one uploaded theme package per request.", MediaType.TEXT_PLAIN);
                    return;
                }
                fw = fws.get(0);                  //TODO: inspect this fw object a little more for metadata
                // TODO: validate zipped media type (fail otherwise)
                final InputStream is = fw.getInputStream();
                ZipInputStream zipIn = new ZipInputStream(is);
                ZipEntry entry = zipIn.getNextEntry();
                int dirCount = 0;
                while (entry != null) {  // iterate over entries in the zip file
                    String filePath = themesPath + File.separator + entry.getName();
                    if (!entry.isDirectory()) {  // if the entry is a file, extract it      // TODO: Make sure we get a directory the first iteration through (fail otherwise) so that no files get dumped in the root themes directory
                        extractFile(zipIn, filePath);
                    } else {  // if the entry is a directory, make the directory
                        if(dirCount == 0) {
                            firstDirName = entry.getName();
                            int slashIndex = firstDirName.indexOf('/');
                            if(slashIndex>1){
                                firstDirName = firstDirName.substring(0, slashIndex);
                            }
                        }
                        dirCount++;
                        File dir = new File(filePath);
                        dir.mkdir();
                    }
                    zipIn.closeEntry();
                    entry = zipIn.getNextEntry();
                }
                zipIn.close();
                is.close();
                getResponse().setEntity(firstDirName, MediaType.TEXT_PLAIN);
            } catch (FileUploadBase.InvalidContentTypeException icte){
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                getResponse().setEntity("Invalid Content Type: "+icte.getMessage(), MediaType.TEXT_PLAIN);
            } catch (FileUploadException fue) {
                this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                getResponse().setEntity("File Upload Exception: " + fue.getMessage(), MediaType.TEXT_PLAIN);
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED, "Only site administrators can upload new themes.");
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zip
     * @param path
     * @throws IOException
     */
    private void extractFile(ZipInputStream zip, String path) throws IOException {
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(path));
        byte[] bytes = new byte[FILE_BUFFER_SIZE];
        int length = 0;
        while ((length = zip.read(bytes)) != -1) {
            os.write(bytes, 0, length);
        }
        os.close();
    }

    /** // TODO: convert to swagger doc tags
     * Reports the currently selected global theme (if there is one), whether or not it's enabled, and a list of
     * available themes on the system in a JSON string.
     */
    @Override
    public Representation represent(Variant variant){
        String themeOptions = null, themeName = null;
        boolean themeEnabled = false;
        ThemeConfig theme = getTheme();
        try {
            themeOptions = mapper.writeValueAsString(loadExistingThemes().toArray());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        if(theme != null) {
            themeName="\""+theme.getName()+"\"";
            themeEnabled=theme.isEnabled();
        }
        return new StringRepresentation(
                "{\"theme\":"+themeName+", \"enabled\":"+themeEnabled+", \"themeOptions\":"+themeOptions+"}",
                MediaType.APPLICATION_JSON);
    }

    /**
     * Loads the system theme options
     * @return The list of the available theme packages (folder names) available under the system themes directory
     */
    public ArrayList<TypeOption> loadExistingThemes() {
        ArrayList<TypeOption> themeOptions = new ArrayList<>();
        themeOptions.add(new TypeOption(null, "None"));
        File f = new File(themesPath); // current directory
        FileFilter directoryFilter = new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        File[] files = f.listFiles(directoryFilter);
        if(files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    themeOptions.add(new TypeOption(file.getName(), file.getName()));
                }
            }
        }
        return themeOptions;
    }

    /**
     * Gets the currently selected system theme from an application servlet context cache, or secondarily from the
     * theme.json file in the themes folder.
     * @return The currently selected system theme configuration
     */
    public ThemeConfig getTheme() {
        ThemeConfig themeConfig = null, cachedTheme = (ThemeConfig) this.getHttpSession().getServletContext().getAttribute("theme");
        if(cachedTheme != null){
            return cachedTheme;
        } else {                        // Read the last saved theme selection from the theme.json file in the themes
            if (themeFile.exists()) {   // directory in the event it can't be found in the application context.
                try {                   // (ie. the server was just started/restarted)
                    BufferedReader reader = new BufferedReader(new FileReader(themeFile));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    String contents = sb.toString();
                    themeConfig = mapper.readValue(contents, ThemeConfig.class);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            setTheme(themeConfig);
        }
        return themeConfig;
    }

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and
     * stores it in the application servlet context cache
     * @param themeConfig the theme configuration object to apply
     */
    public void setTheme(ThemeConfig themeConfig) {
        try {
            if (themeConfig != null) {
                String themeJson = mapper.writeValueAsString(themeConfig);
                if (!themeFile.exists()) {
                    themeFile.createNewFile();
                }
                FileWriter writer = new FileWriter(themeFile);
                writer.write(themeJson);
                writer.flush();
                writer.close();
            } else {
                themeFile.delete();
                if (themeFile.exists()) {                           // Backup hack in case the file deletion does not
                    FileWriter writer = new FileWriter(themeFile);  // succeed due to a file lock which sometimes
                    writer.write("{\"name\":null}");                // strangely happens in Windows
                    writer.flush();                                 // ...in that case wipe it out.
                    writer.close();
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // TODO: rethrow this and respond as an internal server error
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.getHttpSession().getServletContext().setAttribute("theme", themeConfig);
    }

    /**
     * Helper class to organize the available themes for display in a select dropdown form
     */
    public static class TypeOption implements Comparable<TypeOption> {
        String value, label;
        private TypeOption(String value, String label) {
            this.value = value;
            this.label = label;
        }
        public String getValue() {
            return value;
        }
        public String getLabel() {
            return label;
        }
        @Override
        public int compareTo(TypeOption that) {
            return this.label.compareToIgnoreCase(that.label);
        }
    }
}
