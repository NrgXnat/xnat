/*
 * web: org.nrg.xnat.itemBuilders.FullFileHistoryBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.itemBuilders;

import org.junit.Test;
import org.nrg.xdat.bean.CatEntryBean;
import org.nrg.xdat.model.CatEntryI;
import org.nrg.xft.presentation.FlattenedItem.FlattenedFile;
import org.nrg.xft.presentation.FlattenedItemA;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Which catalog entries the audit trail reads as the same file. Grouping runs through
 * {@link FlattenedItemA#isLike}: two entries that are alike are merged, and the later one is then
 * reported as a revision of the earlier rather than as a file of its own.
 */
public class FullFileHistoryBuilderTest {

	private static final Date CREATED = new Date(1700000000000L);

	@Test
	public void keepsSameNamedFilesInDifferentSubdirectoriesApart() throws Exception {
		assertFalse(FlattenedItemA.isLike(file(entry("a/x.dcm", "a/x.dcm"), false),
				file(entry("b/x.dcm", "b/x.dcm"), false)));
	}

	@Test
	public void matchesAnEntryToItsHistoricalVersion() throws Exception {
		//a history entry is the entry it superseded, with only the URI rewritten to its path under history/
		assertTrue(FlattenedItemA.isLike(file(entry("a/x.dcm", "a/x.dcm"), false),
				file(entry("a/x.dcm", "/data/archive/history/DICOM/a/20200101_000000/x.dcm"), true)));
	}

	@Test
	public void stillReadsAsItsNameOnScreen() throws Exception {
		assertEquals("x.dcm", file(entry("a/x.dcm", "a/x.dcm"), false).getItemObject().getObjectLabel());
	}

	/** An entry as the catalog records one: the path in ID, the bare name in name. */
	private static CatEntryBean entry(final String id, final String uri) {
		final CatEntryBean entry = new CatEntryBean();
		entry.setId(id);
		entry.setName("x.dcm");
		entry.setUri(uri);
		entry.setCreatedby("admin");
		entry.setCreatedtime(CREATED);
		entry.setCreatedeventid(900);
		return entry;
	}

	private static FlattenedFile file(final CatEntryI entry, final boolean isHistory) throws Exception {
		return FullFileHistoryBuilder.BuildFlattenedFile(entry, isHistory, () -> 1, Collections.emptyList());
	}
}
