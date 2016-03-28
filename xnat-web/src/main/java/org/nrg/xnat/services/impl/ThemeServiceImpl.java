/*
 * org.nrg.xnat.turbine.modules.screens.ManageProtocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu> (jcleve01)
 * Last modified 2/30/2016 11:20 AM
 */
package org.nrg.xnat.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.nrg.xnat.configuration.ThemeConfig;
import org.nrg.xnat.services.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ThemeServiceImpl implements ThemeService {
    private static String webRelativeThemePath="themes";
    private static String themesPath;
    private static ThemeConfig themeConfig = null;
    private static File themeFile = null;
    protected final ObjectMapper mapper = new ObjectMapper();
    private static final int FILE_BUFFER_SIZE = 4096;

    @Autowired
    private ServletContext servletContext;

    @PostConstruct
    public void postServiceConstruction(){
        themesPath = servletContext.getRealPath(File.separator)+webRelativeThemePath;
        themeFile = new File(themesPath + File.separator + "theme.json");
        File checkThemesPath = new File(themesPath);
        if (!checkThemesPath.exists()) {
            checkThemesPath.mkdir();
        }
System.out.println("Theme Path: "+themeFile);
        servletContext.setAttribute("ThemeService", this);  // This is probably a terrible way to attempt to do this. We would ideally add an instance of ThemeService to the XDAT class
    }

    public String getThemesPath() {
        return themesPath;
    }

    /**
     * Gets the currently selected system theme from an application servlet context cache, or secondarily from the
     * theme.json file in the themes folder.
     * @return The currently selected system theme configuration
     */
    public ThemeConfig getTheme(String role) {
        if(themeConfig != null){
            return themeConfig;
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
            try {
                setTheme(themeConfig);
            } catch (ThemeNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(role != null){
            // TODO: implement search through the roles array in the ThemeConfig object for a matching ThemeConfig object for the specified role
        }
        return themeConfig;
    }
    public ThemeConfig getTheme() {
        return getTheme(null);
    }

    /**
     * Searches the theme directory if a global theme is applied and returns a path string to the referenced page to redirect to.
     * If no global theme is selected or no overriding page is found the calling method should continue with it's default XNAT behavior.
     * @return a path string the referenced page if found. Otherwise returns null.
     */
    public String getThemePage(String pageName) {
        return getThemePage(pageName, null);
    }

    /**
     * Searches the theme directory if a global theme is applied and returns a path string to the referenced theme and matching type to redirect to.
     * If no global theme is selected or no overriding page with specified type is found the calling method should continue with it's default XNAT behavior.
     * @return a path string the referenced theme and type if found. Otherwise returns null.
     */
    public String getThemePage(String pageName, String type) {
        String pagePath;
        ThemeConfig theme = getTheme();
        if(theme == null){
            return null;
        } else if (pageName == null){
            return null;
        } else { // Read the last saved theme selection from the theme.json file in the themes
            pagePath = checkThemeFileExists(theme, pageName, type);
        }
        return pagePath;
    }

    /**
     * Checks for the existence of a file name with a given set of accepted file extensions in the theme directory
     * and returns a relative web path string the referenced page if found.
     * @return a relative web path string prioritized by extension to the referenced page if found. Otherwise returns null.
     */
    private String checkThemeFileExists(ThemeConfig theme, String pageName) {
        return checkThemeFileExists(theme, pageName, null);
    }
    private String checkThemeFileExists(ThemeConfig theme, String pageName, String type) {
        String pagePath = null, typeSep = type + "s" + File.separator;
        String[] extensions = new String[]{};
        String[] pageExts = new String[]{"jsp", "vm", "htm", "html"};
        String[] scriptExts = new String[]{"js"};
        String[] styleExts = new String[]{"css"};
        if("page".equals(type)){
            extensions = (String[]) ArrayUtils.addAll(extensions, pageExts);
        }
        if("script".equals(type)){
            extensions = (String[]) ArrayUtils.addAll(extensions, scriptExts);
        }
        if("style".equals(type)){
            extensions = (String[]) ArrayUtils.addAll(extensions, styleExts);
        }
        if(type == null){
            typeSep = "";
            extensions = (String[]) ArrayUtils.addAll(extensions, pageExts);
            extensions = (String[]) ArrayUtils.addAll(extensions, scriptExts);
            extensions = (String[]) ArrayUtils.addAll(extensions, styleExts);
        }
        for (String ext : extensions) {
            File themePageFile = new File(theme.getPath() + File.separator + typeSep + pageName + "." + ext);
            if(themePageFile.exists()) {
                if(type != null){
                    typeSep = type + "s/";  // This is awful and should be set once up above
                }
                pagePath = "/" + webRelativeThemePath + "/" + theme.getName() + "/" + typeSep + pageName + "." + ext;
                break;
            }
        }
        return pagePath;
    }

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param themeConfig the theme configuration object to apply
     */
    public ThemeConfig setTheme(ThemeConfig themeConfig) throws ThemeNotFoundException {
        try {
            if (themeConfig == null){
                themeConfig = new ThemeConfig();
            }
            if(themeExists(themeConfig.getName())) {
                String themeJson = mapper.writeValueAsString(themeConfig);
                if (!themeFile.exists()) {
                    themeFile.createNewFile();
                }
                FileWriter writer = new FileWriter(themeFile);
                writer.write(themeJson);
                writer.flush();
                writer.close();
                ThemeServiceImpl.themeConfig = themeConfig;
            } else {
                throw new ThemeNotFoundException(themeConfig.getName());
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // TODO: rethrow this and respond as an internal server error
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: rethrow this and respond as an internal server error
        }
        return themeConfig;
    }

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param name the theme name. Creates a theme configuration object with it applying defaults
     */
    public ThemeConfig setTheme(String name) throws ThemeNotFoundException {
        return setTheme(name, true);
    }

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * Creates a theme configuration object with it applying a defaults path
     * @param name the theme name.
     * @param enabled flag specifying whether or not the theme should be active.
     */
    public ThemeConfig setTheme(String name, boolean enabled) throws ThemeNotFoundException {
        return setTheme(new ThemeConfig(name, themesPath + File.separator + name, enabled));
    }

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param name the theme name.
     * @param path base theme directory path.
     * @param enabled flag specifying whether or not the theme should be active.
     */
    public ThemeConfig setTheme(String name, String path, boolean enabled) throws ThemeNotFoundException {
        return setTheme(new ThemeConfig(name, path, enabled));
    }

    /**
     * Loads the system theme options
     * @return The list of the available theme packages (folder names) available under the system themes directory
     */
    public List<TypeOption> loadExistingThemes() {
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
     * Checks if the specified theme exists.
     * @param name the name of the theme to look for
     * @return true if it could be found in the system theme directory
     */
    public boolean themeExists(String name) {
        if(name == null) {
            return true;
        } else if(StringUtils.isEmpty(name)){
            return false;
        } else {
            List<TypeOption> themeList = loadExistingThemes();
            for (TypeOption to: themeList) {
                if(name.equals(to.getValue())){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Extracts a zipped theme package from an given InputStream.
     * @param inputStream from which to read the zipped data
     * @return List of root level directories (theme names) that were extracted
     * @throws IOException
     */
    public List<String> extractTheme(InputStream inputStream) throws IOException {
        ArrayList rootDirs = new ArrayList();
        ZipInputStream zipIn = new ZipInputStream(inputStream);
        ZipEntry entry = zipIn.getNextEntry();
        while (entry != null) {  // iterate over entries in the zip file
            String filePath = this.getThemesPath() + File.separator + entry.getName();
            if (!entry.isDirectory()) {  // if the entry is a file, extract it      // TODO: Make sure we get a directory the first iteration through (fail otherwise) so that no files get dumped in the root themes directory
                this.extractFile(zipIn, filePath);
            } else {  // if the entry is a directory, make the directory
                String rootDir = "";
                rootDir = entry.getName();
                int slashIndex = rootDir.indexOf('/');
                if(slashIndex>1){
                    int nextSlashIndex = rootDir.indexOf('/', slashIndex+1);
                    if(nextSlashIndex<0) {
                        rootDir = rootDir.substring(0, slashIndex);
                        rootDirs.add(rootDir);
                    }
                }
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
        inputStream.close();
        return rootDirs;
    }

    /**
     * Extracts a single zip entry (file entry)
     * @param zip zip input stream to extract it from
     * @param path to the file within the zip package
     * @throws IOException
     */
    private void extractFile(ZipInputStream zip, String path) throws IOException {
        BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(path));
        byte[] bytes = new byte[FILE_BUFFER_SIZE];
        int length;
        while ((length = zip.read(bytes)) != -1) {
            os.write(bytes, 0, length);
        }
        os.close();
    }
}