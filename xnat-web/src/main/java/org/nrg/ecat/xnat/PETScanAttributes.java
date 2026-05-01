/*
 * ecat4xnat: org.nrg.ecat.xnat.PETScanAttributes
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat.xnat;

import java.util.Map;

import org.nrg.attr.AttrDefs;
import org.nrg.attr.ExtAttrException;
import org.nrg.attr.ExtAttrValue;
import org.nrg.attr.NoUniqueValueException;
import org.nrg.ecat.AttrDefSet;
import org.nrg.ecat.Variable;
import static org.nrg.ecat.Variable.*;

final class PETScanAttributes {
	private PETScanAttributes() {}        // no instantiation

	static public AttrDefs<Variable> get() { return s; }

	static final private AttrDefSet s = new AttrDefSet();

	static {
		s.add("ID");        // incrementing integer, filled in by session builder
		s.add(new XnatAttrDef.Abstract<Short,String>("type", ACQUISITION_TYPE) {
			final String[] types = {
					"Undefined", "Blank", "Transmission", "Static emission",
					"Dynamic emission", "Gated emission", "Transmission rectilinear",
					"Emission rectilinear"
			};

			public String foldl(final String a, final Map<? extends Variable,? extends Short> m) throws ExtAttrException {
				final String v = types[m.containsKey(ACQUISITION_TYPE) ? m.get(ACQUISITION_TYPE) : 0];
				if (a == null) {
				    return v;
				} else if (!a.equals(v)) {
				    throw new NoUniqueValueException(getName(), new String[]{a,v});
				} else {
				    return a;
				}
			}

            public Iterable<ExtAttrValue> apply(String a) throws ExtAttrException {
                return applyString(a);
            }
 
            public String start() { return null; }
		});
		s.add("startTime", SCAN_START_TIME);  // needs explicit processing for time zone

		s.add("parameters/orientation", PATIENT_ORIENTATION);
		s.add("parameters/originalFileName", ORIGINAL_FILE_NAME);
		s.add("parameters/systemType", SYSTEM_TYPE);
		s.add("parameters/fileType", FILE_TYPE);
		s.add("parameters/transaxialFOV", TRANSAXIAL_FOV);
		s.add("parameters/acqType", ACQUISITION_TYPE);
		s.add("parameters/facility", FACILITY_NAME);
		s.add("parameters/numPlanes", NUM_PLANES);
		s.add("frames", NUM_FRAMES);	// complicated, requires explicit processing
		s.add("parameters/numGates", NUM_GATES);
		s.add("parameters/planeSeparation", PLANE_SEPARATION);
		s.add("parameters/binSize", BIN_SIZE);
		s.add("parameters/dataType", DATA_TYPE);
		s.add(new XnatAttrDef.AttributesOnly("parameters/dimensions",
				new String[]{"x","y","z","num"},
				new Variable[]{X_DIMENSION,Y_DIMENSION,Z_DIMENSION,NUM_DIMENSIONS}));
		s.add(new XnatAttrDef.AttributesOnly("parameters/offset",
				new String[]{"x","y","z"},
				new Variable[]{X_OFFSET,Y_OFFSET,Z_OFFSET}));
		s.add("parameters/reconZoom", RECON_ZOOM);
		s.add(new XnatAttrDef.AttributesOnly("parameters/pixelSize",
				new String[]{"x","y","z"},
				new Variable[]{X_PIXEL_SIZE,Y_PIXEL_SIZE,Z_PIXEL_SIZE}));
		s.add("parameters/filterCode", FILTER_CODE);
		s.add(new XnatAttrDef.AttributesOnly("parameters/resolution",
				new String[]{"x","y","z"},
				new Variable[]{X_RESOLUTION,Y_RESOLUTION,Z_RESOLUTION}));
		s.add("parameters/numRElements", NUM_R_ELEMENTS);
		s.add("parameters/numAngles", NUM_ANGLES);
		s.add("parameters/ZRotationAngle", Z_ROTATION_ANGLE);
		s.add("parameters/processingCode", PROCESSING_CODE);
		s.add("parameters/gateDuration", GATE_DURATION);
		s.add("parameters/rWaveOffset", R_WAVE_OFFSET);
		s.add("parameters/numAcceptedBeats", NUM_ACCEPTED_BEATS);
		s.add(new XnatAttrDef.AttributesOnly("parameters/filter",
				new String[]{"cutoff"},
				new Variable[]{FILTER_CUTOFF_FREQUENCY}));
		s.add("parameters/annotation", ANNOTATION);
		s.add("parameters/MT_1_1", MT_1_1);
		s.add("parameters/MT_1_2", MT_1_2);
		s.add("parameters/MT_1_3", MT_1_3);
		s.add("parameters/MT_1_4", MT_1_4);
		s.add("parameters/MT_2_1", MT_2_1);
		s.add("parameters/MT_2_2", MT_2_2);
		s.add("parameters/MT_2_3", MT_2_3);
		s.add("parameters/MT_2_4", MT_2_4);
		s.add("parameters/MT_3_1", MT_3_1);
		s.add("parameters/MT_3_2", MT_3_2);
		s.add("parameters/MT_3_3", MT_3_3);
		s.add("parameters/MT_3_4", MT_3_4);
		s.add(new XnatAttrDef.AttributesOnly("parameters/RFilter",
				new String[]{"cutoff","resolution","code","order"},
				new Variable[]{RFILTER_CUTOFF,RFILTER_RESOLUTION,RFILTER_CODE,RFILTER_ORDER}));
		s.add(new XnatAttrDef.AttributesOnly("parameters/ZFilter",
				new String[]{"cutoff","resolution","code","order"},
				new Variable[]{ZFILTER_CUTOFF,ZFILTER_RESOLUTION,ZFILTER_CODE,ZFILTER_ORDER}));
		s.add("parameters/scatterType",SCATTER_TYPE);
		s.add("parameters/reconType",RECON_TYPE);
		s.add("parameters/reconViews",RECON_VIEWS);
		s.add(new XnatAttrDef.ArrayElement("parameters/bedPosition", BED_POSITION, 0));
		s.add("parameters/ecatCalibrationFactor", ECAT_CALIBRATION_FACTOR);
	}
}
