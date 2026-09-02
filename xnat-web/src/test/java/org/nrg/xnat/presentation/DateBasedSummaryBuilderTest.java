/*
 * web: org.nrg.xnat.presentation.DateBasedSummaryBuilderTest
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.presentation;

import org.junit.Test;
import org.nrg.xft.presentation.FlattenedItem.FlattenedFile;
import org.nrg.xft.presentation.FlattenedItemA;
import org.nrg.xft.presentation.FlattenedItemA.ItemObject;
import org.nrg.xft.presentation.FlattenedItemI;
import org.nrg.xnat.presentation.ChangeSummaryBuilderA.ChangeSummary;
import org.nrg.xnat.presentation.ChangeSummaryBuilderA.ItemEventI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Every file added by one event carries that event's timestamp, so a session archive lands all of its
 * files at the same instant. These tests cover which of those the change summary treats as the same
 * file.
 */
public class DateBasedSummaryBuilderTest {

	private static final Date ARCHIVED = new Date(1700000000000L);

	@Test
	public void keepsSameNamedFilesInDifferentResourcesApart() throws Exception {
		final Map<Date, ChangeSummary> byDate = DateBasedSummaryBuilder.build(
				Arrays.asList(file("1-1.dcm", resource("101")), file("1-1.dcm", resource("102"))), null);

		final List<ItemEventI> events = byDate.get(ARCHIVED).getEvents();
		assertEquals(2, events.size());

		final Set<Object> resources = new HashSet<>();
		for (final ItemEventI event : events) {
			assertEquals(ChangeSummaryBuilderA.ADDED, event.getAction());
			resources.add(event.getParent().getObjectId());
		}
		assertEquals(new HashSet<>(Arrays.asList("101", "102")), resources);
	}

	@Test
	public void collapsesTheSameFileRegisteredTwice() throws Exception {
		final ItemObject resource = resource("101");
		final Map<Date, ChangeSummary> byDate = DateBasedSummaryBuilder.build(
				Arrays.asList(file("1-1.dcm", resource), file("1-1.dcm", resource)), null);

		assertEquals(1, byDate.get(ARCHIVED).getEvents().size());
	}

	/** A file added to the given resource by the archive event. */
	private static FlattenedItemI file(final String name, final ItemObject resource) {
		final List<ItemObject> parents = new ArrayList<>();
		parents.add(new ItemObject("scan", "1", "1", Collections.singletonList("xnat:mrScanData")));
		parents.add(resource);
		return new FlattenedFile(new FlattenedItemA.FieldTracker(), false, ARCHIVED, ARCHIVED, 1, "system:file",
				"admin", null, 900, name, parents, "admin");
	}

	private static ItemObject resource(final String id) {
		return new ItemObject("resource", "DICOM", id, Collections.singletonList("xnat:resourceCatalog"));
	}
}
