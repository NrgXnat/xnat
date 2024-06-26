/*
 * ExtAttr: org.nrg.attr.Utils
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.attr;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collection of utility functions more or less related to attribute processing
 *
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 */
@SuppressWarnings("unused")
public final class Utils {
    private Utils() {
    }        // prevent instantiation

    public static class NotRootDir extends Exception {
        private static final long serialVersionUID = 1L;

        NotRootDir(final File notparent, final File file) {
            super("URI for " + file.getPath() + " cannot be constructed relative to " + notparent.getPath());
        }
    }

    /**
     * Generate a new filename, by appending an integer if necessary.
     * NOTE: Nothing is changed on the file system, so this is not an
     * atomic operation and, in principle, someone could create a
     * file with our reserved name before we get a chance to.
     *
     * @param parent The directory that will contain the new file
     * @param name   Base name for the new file
     * @param suffix File suffix; number is placed between the base name and this suffix.
     *               If null, the number is just placed after the base name.
     *
     * @return File record for the new file
     */
    public static File getUnique(final File parent, final String name, final String suffix) {
        final String nameFormat = "%1$s_%2$d";
        File f = new File(parent, name);
        for (int i = 1; f.exists(); i++) {
            final String trialname;
            if (suffix == null) {
                trialname = String.format(nameFormat, name, i);
            } else {
                final int loc = name.lastIndexOf(suffix);
                if (loc >= 0) {
                    trialname = String.format(nameFormat, name.substring(0, loc), i) + suffix;
                } else {
                    trialname = String.format(nameFormat, name, i);
                }
            }
            f = new File(parent, trialname);
        }
        return f;
    }

    /**
     * Generate a new filename, by appending an integer if necessary.
     * NOTE: Nothing is changed on the file system, so this is not an
     * atomic operation and, in principle, someone could create a
     * file with our reserved name before we get a chance to.
     *
     * @param parent The directory that will contain the new file
     * @param name   Base name for the new file
     *
     * @return File record for the new file
     */
    public static File getUnique(final File parent, final String name) {
        return getUnique(parent, name, null);
    }

    /**
     * Returns the canonical path for the given file if available,
     * or the absolute path otherwise.
     *
     * @param f The file object.
     *
     * @return full path for the given file
     */
    public static String getFullPath(final File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            return f.getAbsolutePath();
        }
    }

    public static String getRelativeURI(final File root, final File file) throws IOException, NotRootDir {
        final char URI_SEPARATOR_CHAR = '/';
        final String filePath = file.getCanonicalPath();

        int relativeStart;
        if (root == null) {
            return Paths.get(filePath).toString();
        } else {
            final String rootPath = root.getCanonicalPath();

            if (!filePath.startsWith(rootPath)) {
                throw new NotRootDir(root, file);
            }

            return Paths.get(root.getCanonicalPath()).relativize(Paths.get(filePath)).toString();
        }
    }

    public final static class MaybeNumericStringComparator
            implements Comparator<String>, Serializable {
        private static final long serialVersionUID = 7748262008072773193L;

        /*
         * (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(final String o1, final String o2) {
            try {
                final int i1 = Integer.parseInt(o1);
                final int i2 = Integer.parseInt(o2);
                return i1 - i2;
            } catch (NumberFormatException e) {
                return o1.compareTo(o2);
            }
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(final Object o) {
            return o instanceof MaybeNumericStringComparator;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return 0;
        }
    }

    public static <K, V> Map<K, V> zipmap(final Map<K, V> m, final K[] ks, final V[] vs) {
        for (int i = 0; i < ks.length; i++) {
            m.put(ks[i], vs[i]);
        }
        return m;
    }

    public static <K, V> Map<K, V> zipmap(final K[] ks, final V[] vs) {
        return zipmap(new LinkedHashMap<K, V>(), ks, vs);
    }

    public static <V> Collection<V> addTo(final Collection<V> vs, final V v) {
        vs.add(v);
        return vs;
    }

    public static <K, V> Map<K, V> merge(final Iterable<Map<K, V>> ms) {
        final Iterator<Map<K, V>> i = ms.iterator();
        if (i.hasNext()) {
            final Map<K, V> merged = new LinkedHashMap<>(i.next());
            while (i.hasNext()) {
                merged.putAll(i.next());
            }
            return merged;
        } else {
            return Collections.emptyMap();
        }
    }

    @SafeVarargs
    public static <K, V> Map<K, V> merge(final Map<K, V>... ms) {
        return merge(Arrays.asList(ms));
    }
}
