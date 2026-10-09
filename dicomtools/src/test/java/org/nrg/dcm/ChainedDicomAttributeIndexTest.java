/*
 * dicomtools: org.nrg.dcm.ChainedDicomAttributeIndexTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2026, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Sequence;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ChainedDicomAttributeIndexTest {

    private static final Integer[] SLICE_THICKNESS_PATH = {
            Tag.PerFrameFunctionalGroupsSequence, null,
            Tag.PixelMeasuresSequence, 0,
            Tag.SliceThickness
    };

    private static final Integer[] PIXEL_SPACING_PATH = {
            Tag.PerFrameFunctionalGroupsSequence, null,
            Tag.PixelMeasuresSequence, 0,
            Tag.PixelSpacing
    };

    /**
     * Build a Per-frame Functional Groups SQ with the given number of frames,
     * each containing a Pixel Measures SQ item with the specified Slice Thickness
     * and Pixel Spacing values.
     */
    private static Attributes buildPerFrameData(final int nFrames,
                                                final String sliceThickness,
                                                final String pixelSpacing) {
        final Attributes root = new Attributes();
        final Sequence perFrameSeq = root.newSequence(Tag.PerFrameFunctionalGroupsSequence, nFrames);
        for (int i = 0; i < nFrames; i++) {
            final Attributes frameItem = new Attributes();
            final Sequence pixelMeasuresSeq = frameItem.newSequence(Tag.PixelMeasuresSequence, 1);
            final Attributes pixelMeasures = new Attributes();
            pixelMeasures.setString(Tag.SliceThickness, VR.DS, sliceThickness);
            pixelMeasures.setString(Tag.PixelSpacing, VR.DS, pixelSpacing);
            pixelMeasuresSeq.add(pixelMeasures);
            perFrameSeq.add(frameItem);
        }
        return root;
    }

    // --- getString tests ---

    /**
     * When a wildcard path matches multiple sequence items that all have the same value,
     * getString should return that single value, not a backslash-joined repetition.
     */
    @Test
    public void testGetStringCoalescesIdenticalValues() {
        final Attributes root = buildPerFrameData(4, "1.5", "0.5\\0.5");

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertEquals("1.5", index.getString(root, null));
    }

    /**
     * When a wildcard path matches a single sequence item, getString returns that value.
     */
    @Test
    public void testGetStringSingleMatch() {
        final Attributes root = buildPerFrameData(1, "2.0", "0.5\\0.5");

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertEquals("2.0", index.getString(root, null));
    }

    /**
     * When a wildcard path matches no sequence items, getString returns the default value.
     */
    @Test
    public void testGetStringNoMatch() {
        final Attributes root = new Attributes();

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertNull(index.getString(root, null));
        assertEquals("default", index.getString(root, "default"));
    }

    /**
     * For a mergeable attribute (SliceThickness is VR DS, not in the unmergeable sets),
     * different values across wildcard matches are combined with backslash.
     */
    @Test
    public void testGetStringMergeableDifferentValues() {
        final Attributes root = new Attributes();
        final Sequence perFrameSeq = root.newSequence(Tag.PerFrameFunctionalGroupsSequence, 2);

        final Attributes frame1 = new Attributes();
        final Sequence pm1 = frame1.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures1 = new Attributes();
        measures1.setString(Tag.SliceThickness, VR.DS, "1.5");
        pm1.add(measures1);
        perFrameSeq.add(frame1);

        final Attributes frame2 = new Attributes();
        final Sequence pm2 = frame2.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures2 = new Attributes();
        measures2.setString(Tag.SliceThickness, VR.DS, "3.0");
        pm2.add(measures2);
        perFrameSeq.add(frame2);

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertEquals("1.5\\3.0", index.getString(root, null));
    }

    /**
     * Coalescing identical values also works for unmergeable multi-valued attributes
     * like Pixel Spacing.
     */
    @Test
    public void testGetStringCoalescesIdenticalUnmergeableAttribute() {
        final Attributes root = buildPerFrameData(3, "1.5", "0.5\\0.5");

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "PixelSpacing", PIXEL_SPACING_PATH);

        assertEquals("0.5\\0.5", index.getString(root, null));
    }

    /**
     * For an unmergeable attribute (Pixel Spacing is in the unmergeable set),
     * conflicting values across wildcard matches yield null — the values can't
     * be sensibly combined.
     */
    @Test
    public void testGetStringUnmergeableConflictingValues() {
        final Attributes root = new Attributes();
        final Sequence perFrameSeq = root.newSequence(Tag.PerFrameFunctionalGroupsSequence, 2);

        final Attributes frame1 = new Attributes();
        final Sequence pm1 = frame1.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures1 = new Attributes();
        measures1.setString(Tag.PixelSpacing, VR.DS, "0.5\\0.5");
        pm1.add(measures1);
        perFrameSeq.add(frame1);

        final Attributes frame2 = new Attributes();
        final Sequence pm2 = frame2.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures2 = new Attributes();
        measures2.setString(Tag.PixelSpacing, VR.DS, "1.0\\1.0");
        pm2.add(measures2);
        perFrameSeq.add(frame2);

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "PixelSpacing", PIXEL_SPACING_PATH);

        assertNull(index.getString(root, null));
        assertEquals("default", index.getString(root, "default"));
    }

    // --- getStrings tests ---

    /**
     * getStrings should deduplicate identical values from wildcard-matched sequence items.
     */
    @Test
    public void testGetStringsCoalescesIdenticalValues() {
        final Attributes root = buildPerFrameData(4, "1.5", "0.5\\0.5");

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertArrayEquals(new String[]{"1.5"}, index.getStrings(root));
    }

    /**
     * For a mergeable attribute, getStrings returns all distinct values.
     */
    @Test
    public void testGetStringsMergeableDifferentValues() {
        final Attributes root = new Attributes();
        final Sequence perFrameSeq = root.newSequence(Tag.PerFrameFunctionalGroupsSequence, 2);

        final Attributes frame1 = new Attributes();
        final Sequence pm1 = frame1.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures1 = new Attributes();
        measures1.setString(Tag.SliceThickness, VR.DS, "1.5");
        pm1.add(measures1);
        perFrameSeq.add(frame1);

        final Attributes frame2 = new Attributes();
        final Sequence pm2 = frame2.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures2 = new Attributes();
        measures2.setString(Tag.SliceThickness, VR.DS, "3.0");
        pm2.add(measures2);
        perFrameSeq.add(frame2);

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertArrayEquals(new String[]{"1.5", "3.0"}, index.getStrings(root));
    }

    /**
     * For an unmergeable attribute with conflicting values, getStrings returns
     * an empty array.
     */
    @Test
    public void testGetStringsUnmergeableConflictingValues() {
        final Attributes root = new Attributes();
        final Sequence perFrameSeq = root.newSequence(Tag.PerFrameFunctionalGroupsSequence, 2);

        final Attributes frame1 = new Attributes();
        final Sequence pm1 = frame1.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures1 = new Attributes();
        measures1.setString(Tag.PixelSpacing, VR.DS, "0.5\\0.5");
        pm1.add(measures1);
        perFrameSeq.add(frame1);

        final Attributes frame2 = new Attributes();
        final Sequence pm2 = frame2.newSequence(Tag.PixelMeasuresSequence, 1);
        final Attributes measures2 = new Attributes();
        measures2.setString(Tag.PixelSpacing, VR.DS, "1.0\\1.0");
        pm2.add(measures2);
        perFrameSeq.add(frame2);

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "PixelSpacing", PIXEL_SPACING_PATH);

        assertArrayEquals(new String[]{}, index.getStrings(root));
    }

    /**
     * getStrings should return an empty array when no sequence items match.
     */
    @Test
    public void testGetStringsNoMatch() {
        final Attributes root = new Attributes();

        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "SliceThickness", SLICE_THICKNESS_PATH);

        assertArrayEquals(new String[]{}, index.getStrings(root));
    }

    /**
     * A stop tag only ever gates top-level elements: reading a sequence brings its items with it whatever the
     * stop tag says. So the bound is where each candidate path enters the object, not the largest tag anywhere
     * along it -- a nested tag above its own sequence would otherwise widen every read for nothing.
     */
    @Test
    public void boundsAtTheTopLevelTagOfEachPath() {
        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "nested",
                new Integer[]{0x00081140, null, 0xF2151050},   // nested tag far above its sequence
                new Integer[]{0x00189999});                    // a higher top-level tag

        assertEquals(0x00189999, index.getMaxTag());
    }

    /** A path that names nothing asks for nothing, rather than throwing. */
    @Test
    public void ignoresAPathWithNoTags() {
        final ChainedDicomAttributeIndex index = new ChainedDicomAttributeIndex(
                "degenerate", new Integer[]{}, new Integer[]{0x00080060});

        assertEquals(0x00080060, index.getMaxTag());
    }
}
