package org.nrg.xnat.snapshot;

import org.junit.Test;
import org.nrg.xnat.snapshot.generator.impl.SliceCoordinate;
import org.nrg.xnat.snapshot.generator.impl.SliceCoordinateCalculator;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SliceCoordinateCalculatorTests {

    @Test
    public void testSelectSliceIndices() {
        SliceCoordinateCalculator calculator = new SliceCoordinateCalculator();

        List<Integer> slices = calculator.selecctSliceIndices( 0, 0);
        assertTrue( slices.isEmpty());

        slices = calculator.selecctSliceIndices( 0, 1);
        assertTrue( slices.isEmpty());

        slices = calculator.selecctSliceIndices( 1, 0);
        assertTrue( slices.isEmpty());

        slices = calculator.selecctSliceIndices( 1, 1);
        assertEquals( 1, slices.size());
        assertEquals( 0, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 2,1);
        assertEquals( 1, slices.size());
        assertEquals( 0, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 1,2);
        assertEquals( 1, slices.size());
        assertEquals( 1, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 1,3);
        assertEquals( 1, slices.size());
        assertEquals( 1, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 1, 4);
        assertEquals( 1, slices.size());
        assertEquals( 2, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 1, 5);
        assertEquals( 1, slices.size());
        assertEquals( 2, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 1, 6);
        assertEquals( 1, slices.size());
        assertEquals( 3, (int) slices.get(0));

        slices = calculator.selecctSliceIndices( 2, 5);
        assertEquals( 2, slices.size());
        assertEquals( 1, (int) slices.get(0));
        assertEquals( 3, (int) slices.get(1));

        slices = calculator.selecctSliceIndices( 2, 6);
        assertEquals( 2, slices.size());
        assertEquals( 1, (int) slices.get(0));
        assertEquals( 4, (int) slices.get(1));

        slices = calculator.selecctSliceIndices( 6, 2);
        assertEquals( 2, slices.size());
        assertEquals( 0, (int) slices.get(0));
        assertEquals( 1, (int) slices.get(1));
    }

    @Test
    public void testSliceCoordinates() {
        SliceCoordinateCalculator calculator = new SliceCoordinateCalculator();
        List<String> files = Arrays.asList( "0", "1", "2");

        try {
            List<SliceCoordinate> coordinates = calculator.getSliceCoordinates(0, 0, Arrays.asList());
            assertTrue( coordinates.isEmpty());

            coordinates = calculator.getSliceCoordinates(0, 1, Arrays.asList());
            assertTrue( coordinates.isEmpty());

            coordinates = calculator.getSliceCoordinates(1, 0, Arrays.asList());
            assertTrue( coordinates.isEmpty());

            coordinates = calculator.getSliceCoordinates(1, 1, Arrays.asList( "foo"));
            assertEquals( 1, coordinates.size());
            assertEquals( new SliceCoordinate(0,0), coordinates.get(0));

            coordinates = calculator.getSliceCoordinates(2, 1, Arrays.asList( "foo"));
            assertEquals( 1, coordinates.size());
            assertEquals( new SliceCoordinate(0,0), coordinates.get(0));

            coordinates = calculator.getSliceCoordinates(1, 2, Arrays.asList( "foo", "bar"));
            assertEquals( 1, coordinates.size());
            assertEquals( new SliceCoordinate(1,0), coordinates.get(0));

            coordinates = calculator.getSliceCoordinates(1, 2, Arrays.asList( "foo"));
            assertEquals( 1, coordinates.size());
            assertEquals( new SliceCoordinate(0,1), coordinates.get(0));

        }
        catch( Exception e) {
            fail("Unexpected exception: " + e);
        }
    }
}
