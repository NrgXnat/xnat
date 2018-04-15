/*
 * core: org.nrg.xdat.services.impl.ThemeServiceImpl
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xdat.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.nrg.framework.services.SerializerService;
import org.nrg.xdat.entities.ThemeConfig;
import org.nrg.xdat.services.ThemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletContext;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.nrg.xdat.velocity.loaders.CustomClasspathResourceLoader.safeJoin;

@Service
@Slf4j
public class ThemeServiceImpl implements ThemeService {
    @Autowired
    public ThemeServiceImpl(final SerializerService serializer, final ServletContext context) {
        _serializer = serializer;
        _themesPath = Paths.get(context.getRealPath("/"), WEB_RELATIVE_THEME_PATH);
        _themeFile = _themesPath.resolve("theme.json").toFile();

        final File checkThemesPath = _themesPath.toFile();
        if (!checkThemesPath.exists()) {
            checkThemesPath.mkdir();
        }
    }

    public String getThemesPath() {
        return _themesPath.toString();
    }

    /**
     * Gets the currently selected system theme from an application servlet context cache, or secondarily from the
     * theme.json file in the themes folder.
     * @return The currently selected system theme configuration
     */
    public ThemeConfig getTheme(String role) {
        if(_themeConfig != null){
            return _themeConfig;
        } else {                        // Read the last saved theme selection from the theme.json file in the themes
            if (_themeFile.exists()) {   // directory in the event it can't be found in the application context.
                try {                   // (ie. the server was just started/restarted)
                    BufferedReader reader = new BufferedReader(new FileReader(_themeFile));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    reader.close();
                    String contents = sb.toString();
                    _themeConfig = _serializer.deserializeJson(contents, ThemeConfig.class);
                } catch (IOException e) {
                    log.error("An error occurred trying to retrieve the theme file " + _themeFile.getAbsolutePath(), e);
                }
            }
            try {
                setTheme(_themeConfig);
            } catch (ThemeNotFoundException e) {
                log.error("The specified theme {} wasn't found.", _themeConfig.getName());
            }
        }
        //noinspection StatementWithEmptyBody
        if (role != null) {
            // TODO: implement search through the roles array in the ThemeConfig object for a matching ThemeConfig object for the specified role
        }
        return _themeConfig;
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
    public String getThemePage(final String pageName, final String type) {
        if (StringUtils.isBlank(pageName)) {
            return null;
        }
        final ThemeConfig theme = getTheme();
        if (theme == null) {
            return null;
        }
        return checkThemeFileExists(theme, pageName, type);
    }

    private String checkThemeFileExists(ThemeConfig theme, String pageName, final String type) {
        String pagePath = null;
        String[] extensions = new String[]{};
        String[] pageExts = new String[]{"jsp", "vm", "htm", "html"};
        String[] scriptExts = new String[]{"js"};
        String[] styleExts = new String[]{"css"};
        if("page".equals(type)){
            extensions = ArrayUtils.addAll(extensions, pageExts);
        }
        if("script".equals(type)){
            extensions = ArrayUtils.addAll(extensions, scriptExts);
        }
        if("style".equals(type)){
            extensions = ArrayUtils.addAll(extensions, styleExts);
        }
        final boolean useTypeSeparator;
        if(type == null){
            useTypeSeparator = false;
            extensions = ArrayUtils.addAll(extensions, pageExts);
            extensions = ArrayUtils.addAll(extensions, scriptExts);
            extensions = ArrayUtils.addAll(extensions, styleExts);
        } else {
            useTypeSeparator = true;
        }
        for (String ext : extensions) {
            File themePageFile = (useTypeSeparator ? Paths.get(theme.getPath(), type + "s", pageName + "." + ext) : Paths.get(theme.getPath(), pageName + "." + ext)).toFile();
            if(themePageFile.exists()) {
                pagePath = safeJoin("/" + WEB_RELATIVE_THEME_PATH, theme.getName(), type + "s", pageName + "." + ext);
                break;
            }
        }
        return pagePath;
    }

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param themeConfig the theme configuration object to apply
     */
    public ThemeConfig setTheme(final ThemeConfig themeConfig) throws ThemeNotFoundException {
        try {
            final ThemeConfig working = ObjectUtils.defaultIfNull(themeConfig, new ThemeConfig());
            if(themeExists(working.getName())) {
                final String themeJson = _serializer.toJson(working);
                if (!_themeFile.exists()) {
                    _themeFile.createNewFile();
                }
                FileWriter writer = new FileWriter(_themeFile);
                writer.write(themeJson);
                writer.flush();
                writer.close();
                _themeConfig = working;
            } else {
                throw new ThemeNotFoundException(working.getName());
            }
        } catch (IOException e) {
            // TODO: rethrow this and respond as an internal server error
            log.error("An error occurred retrieving a theme", e);
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
        return setTheme(new ThemeConfig(name, _themesPath + File.separator + name, enabled));
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
        File f = _themesPath.toFile(); // current directory
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
     * @throws IOException When an error occurs accessing a file or directory.
     */
    public List<String> extractTheme(InputStream inputStream) throws IOException {
        final List<String> rootDirs = new ArrayList<>();
        try (final ZipInputStream zipIn = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {  // iterate over entries in the zip file
                final String filePath = this.getThemesPath() + File.separator + entry.getName();
                if (!entry.isDirectory()) {  // if the entry is a file, extract it      // TODO: Make sure we get a directory the first iteration through (fail otherwise) so that no files get dumped in the root themes directory
                    extractFile(zipIn, filePath);
                } else {  // if the entry is a directory, make the directory
                    String rootDir    = entry.getName();
                    int    slashIndex = rootDir.indexOf('/');
                    if (slashIndex > 1) {
                        int nextSlashIndex = rootDir.indexOf('/', slashIndex + 1);
                        if (nextSlashIndex < 0) {
                            rootDir = rootDir.substring(0, slashIndex);
                            rootDirs.add(rootDir);
                        }
                    }
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zipIn.closeEntry();
            }
        }
        return rootDirs;
    }

    /**
     * Extracts a single zip entry (file entry)
     * @param zip zip input stream to extract it from
     * @param path to the file within the zip package
     * @throws IOException When an error occurs accessing a file or directory.
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

    private static final String WEB_RELATIVE_THEME_PATH ="themes";
    private static final int    FILE_BUFFER_SIZE        = 4096;

    private final SerializerService _serializer;
    private final Path                    _themesPath;
    private final File                    _themeFile;

    private ThemeConfig _themeConfig = null;
}
