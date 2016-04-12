/*
 * org.nrg.xnat.turbine.modules.screens.ManageProtocol
 * XNAT http://www.xnat.org
 * Copyright (c) 2013, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Author: Justin Cleveland <clevelandj@wustl.edu> (jcleve01)
 * Last modified 2/29/2016 11:20 AM
 */
package org.nrg.xnat.services;

import org.jetbrains.annotations.NotNull;
import org.nrg.xnat.configuration.ThemeConfig;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

@Service
public interface ThemeService {

//    abstract public void postServiceConstruction();

    /**
     * Returns the system theme file directory for reference by other XNAT modules.
     * @return the system theme file directory
     */
    String getThemesPath();

    /**
     * Gets the currently selected global system theme from a cache for a specific user role, or secondarily from the theme.json file in the themes folder.
     * @param role the name of the user role to fetch from the current global theme
     * @return The currently selected system theme configuration
     */
    ThemeConfig getTheme(String role);

    /**
     * Gets the currently selected global system theme from a cache, or secondarily from the theme.json file in the themes folder.
     * @return The currently selected global system theme configuration
     */
    ThemeConfig getTheme();

    /**
     * Searches the theme directory if a global theme is applied and returns a path string to the referenced page to redirect to.
     * If no global theme is selected or no overriding page is found the calling method should continue with it's default XNAT behavior.
     * @return a path string the referenced page if found. Otherwise returns null.
     */
    String getThemePage(String pageName);

    /**
     * Searches the theme directory if a global theme is applied and returns a path string to the referenced theme and matching type to redirect to.
     * If no global theme is selected or no overriding page with specified type is found the calling method should continue with it's default XNAT behavior.
     * @return a path string the referenced theme and type if found. Otherwise returns null.
     */
    String getThemePage(String pageName, String type);

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param themeConfig the theme configuration object to apply
     */
    ThemeConfig setTheme(ThemeConfig themeConfig) throws ThemeNotFoundException;

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param name the theme name. Creates a theme configuration object with it applying defaults
     */
    ThemeConfig setTheme(String name) throws ThemeNotFoundException;

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * Creates a theme configuration object with it applying a defaults path
     * @param name the theme name.
     * @param enabled flag specifying whether or not the theme should be active.
     */
    ThemeConfig setTheme(String name, boolean enabled) throws ThemeNotFoundException;

    /**
     * Sets the currently selected system theme in the theme.json file in the web application's themes folder and caches it.
     * @param name the theme name.
     * @param path base theme directory path.
     * @param enabled flag specifying whether or not the theme should be active.
     */
    ThemeConfig setTheme(String name, String path, boolean enabled) throws ThemeNotFoundException;

    /**
     * Loads the system theme options
     * @return The list of the available theme packages (folder names) available under the system themes directory
     */
    List<TypeOption> loadExistingThemes();

    /**
     * Checks if the specified theme exists.
     * @param name the name of the theme to look for
     * @return true if it could be found in the system theme directory
     */
    boolean themeExists(String name);

    /**
     * Extracts a zipped theme package from an given InputStream.
     * @param inputStream from which to read the zipped data
     * @return List of root level directories (theme names) that were extracted
     * @throws IOException
     */
    List<String> extractTheme(InputStream inputStream) throws IOException;

    /**
     * Helper exception to report more specific errors
     */
    class ThemeNotFoundException extends Exception{
        private String invalidTheme;
        public ThemeNotFoundException(String invalidTheme) {
            this.invalidTheme = invalidTheme;
        }
        public String getInvalidTheme() {
            return invalidTheme;
        }
    }

    /**
     * Helper class to organize the available themes for display in a select dropdown form
     */
    class TypeOption implements Comparable<TypeOption> {
        String value, label;
        public TypeOption(String value, String label) {
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
        public int compareTo(@NotNull TypeOption that) {
            return this.label.compareToIgnoreCase(that.label);
        }
    }
}