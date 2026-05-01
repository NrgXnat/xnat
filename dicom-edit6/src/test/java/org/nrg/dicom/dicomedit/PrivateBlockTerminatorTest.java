package org.nrg.dicom.dicomedit;

import org.junit.Test;
import org.nrg.dicom.mizer.objects.DicomObjectFactory;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPrivateCreator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PrivateBlockTerminatorTest {

    @Test
    public void testEmptyList() {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( 0x00100010, "snafu");
        dobj.putString( 0x00190010, "Mine");
        dobj.putString( 0x00191000, "oh boy");

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));

        PrivateBlockTerminator visitor = new PrivateBlockTerminator();
        visitor.deletePrivateGroups( new ArrayList<TagPath>(), dobj);

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));
    }

    @Test
    public void testList() {
        final DicomObjectI dobj = DicomObjectFactory.newInstance();

        dobj.putString( 0x00100010, "snafu");
        dobj.putString( 0x00190010, "Mine");
        dobj.putString( 0x00191000, "oh boy");
        dobj.putString( 0x001900E1, "Mine");
        dobj.putString( 0x0019E100, "oh boy");

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));
        assertEquals( "Mine", dobj.getString( 0x001900E1));
        assertEquals( "oh boy", dobj.getString( 0x0019E100));

        PrivateBlockTerminator visitor = new PrivateBlockTerminator();

        List<TagPath> tagsToDelete = new ArrayList();
        TagPath tagPath = new TagPath();
        tagPath.addTag(new TagPrivateCreator(0x001900E1, "Mine" ));
        tagsToDelete.add( tagPath);

        visitor.deletePrivateGroups(  tagsToDelete, dobj);

        assertEquals( "snafu", dobj.getString( 0x00100010));
        assertEquals( "Mine", dobj.getString( 0x00190010));
        assertEquals( "oh boy", dobj.getString( 0x00191000));
        assertFalse( dobj.contains( 0x001900E1));
        assertFalse( dobj.contains( 0x0019E100));
    }

}