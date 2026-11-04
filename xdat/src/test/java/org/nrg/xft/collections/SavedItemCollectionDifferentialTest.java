/*
 * core: org.nrg.xft.collections.SavedItemCollectionDifferentialTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2026, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.collections;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.nrg.framework.services.ContextService;
import org.nrg.xft.XFT;
import org.nrg.xft.XFTItem;
import org.nrg.xft.db.ViewManager;
import org.nrg.xft.meta.XFTMetaManager;
import org.nrg.xft.references.XFTPseudonymManager;
import org.nrg.xft.references.XFTReferenceManager;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperField;
import org.nrg.xft.schema.XFTManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static org.junit.Assert.assertTrue;

/**
 * The saved-item index must find what the scan it replaces finds. On real items of every element in the security
 * schema, a set of stored items, some of other types, is put in both collections; then probes with the same and
 * other values are looked up by primary key, by unique fields, and with {@code contains}, which is what the trigger
 * list uses. Values mix null, letters of either case, numbers and a
 * number written as text, so equality and type coercion are compared too.
 */
public class SavedItemCollectionDifferentialTest {
    private static final Object[] VALUES = {null, "a", "b", "A", 1, 2, "1"};
    private static final int      ROUNDS = 30;
    private static final int      PROBES = 20;

    @BeforeClass
    public static void loadSchema() throws Exception {
        ContextService.getInstance().setApplicationContext(null);
        final File schema = new File(SavedItemCollectionDifferentialTest.class.getClassLoader().getResource("schemas/security/security.xsd").toURI());
        XFT.init(schema.getParentFile().getParentFile().getParent());
    }

    @AfterClass
    public static void unloadSchema() {
        XFTManager.clean();
        XFTMetaManager.clean();
        XFTReferenceManager.clean();
        XFTPseudonymManager.clean();
        GenericWrapperElement.ClearElementCache();
        ViewManager.FIELD_MAPS.clear();
        ViewManager.FIELD_NAMES.clear();
    }

    @Test
    public void theIndexFindsWhatTheScanFinds() throws Exception {
        final Random                      random     = new Random(42);
        final Map<String, Integer>        mismatches = new TreeMap<>();
        final List<String>                examples   = new ArrayList<>();
        final Map<String, Integer>        matches    = new TreeMap<>();
        final List<GenericWrapperElement> elements   = new ArrayList<>();
        for (final Object name : XFTMetaManager.GetElementNames()) {
            elements.add(GenericWrapperElement.GetElement((String) name));
        }

        for (final GenericWrapperElement element : elements) {
            final List<String> paths = keyPaths(element);
            if (paths.isEmpty()) {
                continue;
            }
            for (int round = 0; round < ROUNDS; round++) {
                final ItemCollection      scan   = new ItemCollection();
                final SavedItemCollection index  = new SavedItemCollection();
                final List<XFTItem>       probes = new ArrayList<>();
                final int                 stored = 1 + random.nextInt(12);
                for (int i = 0; i < stored; i++) {
                    // Mostly this element, sometimes another: an item of one type must never match a probe of another.
                    final GenericWrapperElement type = random.nextInt(8) == 0 ? elements.get(random.nextInt(elements.size())) : element;
                    final XFTItem               item = item(type, type == element ? paths : keyPaths(type), random);
                    scan.add(item);
                    index.add(item);
                    probes.add(item);
                }
                for (int i = 0; i < PROBES; i++) {
                    probes.add(item(element, paths, random));
                }
                for (final XFTItem probe : probes) {
                    final Object byPk = scan.findByPK(probe, false);
                    compare("findByPK", byPk, index.findByPK(probe, false), element, probe, mismatches, examples);
                    final Object byUnique = scan.findByUnique(probe, false);
                    compare("findByUnique", byUnique, index.findByUnique(probe, false), element, probe, mismatches, examples);
                    if (scan.contains(probe, false) != index.contains(probe, false)) {
                        mismatches.merge("contains " + element.getFullXMLName(), 1, Integer::sum);
                    }
                    if (byPk != null) {
                        matches.merge("primary key", 1, Integer::sum);
                    }
                    if (byUnique != null && probe.hasUniques()) {
                        matches.merge(element.getUniqueCompositeFields().isEmpty() ? "unique field" : "unique composite", 1, Integer::sum);
                    }
                }
            }
        }

        assertTrue("The index and the scan disagree: " + mismatches + "\n" + String.join("\n", examples), mismatches.isEmpty());
        // Without matches of each kind the comparison would prove nothing.
        assertTrue("Too few matches to compare: " + matches, matches.getOrDefault("primary key", 0) > 100
                                                             && matches.getOrDefault("unique field", 0) + matches.getOrDefault("unique composite", 0) > 100);
    }

    /** The paths of an element's primary key, unique fields and the fields of its unique composites. */
    @SuppressWarnings("unchecked")
    private static List<String> keyPaths(final GenericWrapperElement element) throws Exception {
        final LinkedHashSet<String> paths = new LinkedHashSet<>();
        for (final GenericWrapperField field : (List<GenericWrapperField>) element.getUniqueFields()) {
            paths.add(field.getXMLPathString(element.getFullXMLName()));
        }
        for (final List<GenericWrapperField> composite : ((Map<String, List<GenericWrapperField>>) element.getUniqueCompositeFields()).values()) {
            for (final GenericWrapperField field : composite) {
                if (field.isReference()) {
                    for (final List<Object> localReference : (List<List<Object>>) field.getLocalRefNames()) {
                        paths.add(element.getFullXMLName() + "/" + localReference.get(0));
                    }
                } else {
                    paths.add(field.getXMLPathString(element.getFullXMLName()));
                }
            }
        }
        for (final String pk : element.getPkNames()) {
            paths.add(element.getFullXMLName() + "/" + pk);
        }
        return new ArrayList<>(paths);
    }

    /** An item of the element with a random value, or none, at each key path. Values a field rejects are left unset. */
    private static XFTItem item(final GenericWrapperElement element, final List<String> paths, final Random random) throws Exception {
        final XFTItem item = XFTItem.NewItem(element, null);
        for (final String path : paths) {
            final Object value = VALUES[random.nextInt(VALUES.length)];
            if (value != null) {
                try {
                    item.setProperty(path, value);
                } catch (Exception ignored) {
                    // A value the field's type can't take: the probe goes without it, as an item would.
                }
            }
        }
        return item;
    }

    private static void compare(final String lookup, final Object scan, final Object index, final GenericWrapperElement element, final XFTItem probe,
                                final Map<String, Integer> mismatches, final List<String> examples) {
        if (scan != index) {
            mismatches.merge(lookup + " " + element.getFullXMLName(), 1, Integer::sum);
            if (examples.size() < 10) {
                examples.add(lookup + " " + element.getFullXMLName() + " probe " + probe.getProps() + ": scan found " + describe(scan) + ", index found " + describe(index));
            }
        }
    }

    private static String describe(final Object item) {
        return item == null ? "nothing" : ((XFTItem) item).getProps().toString();
    }
}
