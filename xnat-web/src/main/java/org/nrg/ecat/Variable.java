/*
 * ecat4xnat: org.nrg.ecat.Variable
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.ecat;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import org.nrg.ecat.Header.Type; 

public abstract class Variable implements Comparable<Variable> {
  private static final String CHARSET = "US-ASCII";
  private static final Map<Type,Collection<Variable>> vars = new TreeMap<Type,Collection<Variable>>();
  
  public static Collection<Variable> getVars() {
    final Collection<Variable> l = new LinkedList<Variable>();
    for (final Collection<Variable> var : vars.values())
      l.addAll(var);
    return l;
  }
  
  protected final String name;
  protected final int offset;
  protected final int size;
  protected final Type header;

  private Variable(final Type header, final String name, final int offset, final int size) {
    this.header = header; this.name = name; this.offset = offset; this.size = size;
    if (!vars.containsKey(header))
      vars.put(header, new TreeSet<Variable>());
    if (offset >= 0) {
      assert ! vars.get(header).contains(this);
      vars.get(header).add(this);
    }
  }
  
  @Override
  public String toString() {
    return name + " (offset " + offset + ")";
  }
  
  public int compareTo(Variable o) {
    final int hc = this.header.compareTo(o.header);
    return hc == 0 ? offset-o.offset : hc;
  }
  
  public abstract Object getValue(ByteBuffer bb);
  
  private final static class Constant extends Variable {
    private static int offset = -1;     // each constant gets its own (negative) offset
    private final Object o;
    public Constant(final Type header, final String name, final Object o) {
      super(header, name,offset--,-1); 
      this.o = o;
    }
    @Override
    public Object getValue(final ByteBuffer bb) {
      return o;
    }
  }
  
  
  private static class CharVar extends Variable {
    public CharVar(final Type header, final String name, final int offset, final int size) {
      super(header,name,offset,size);
    }
    @Override
    public String getValue(final ByteBuffer bb) {
      final byte[] bytes = new byte[size];
      bb.position(offset);
      bb.get(bytes, 0, size);
      int len;
      for (len = 0; len < size; len++)
        if (bytes[len] == 0) break;
      try {
        return new String(bytes, 0, len, CHARSET);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);  // US-ASCII is unsupported?!
      }
    }
  }

  private static class ShortVar extends Variable {
    public ShortVar(final Type header, final String name, final int offset) {
      super(header,name,offset,Short.SIZE/Byte.SIZE);
    }
    @Override
    public Short getValue(final ByteBuffer bb) {
      return bb.getShort(offset);
    }
  }
  
  private static class ShortArray extends Variable {
    final int n;
    public ShortArray(final Type header, final String name, final int offset, final int n) {
      super(header,name,offset,Short.SIZE/Byte.SIZE*n);
      this.n = n;
    }
    @Override
    public short[] getValue(final ByteBuffer bb) {
      bb.position(offset);
      final short[] s = new short[n];
      bb.asShortBuffer().get(s);
      return s;
    }
  }
  
  private static class IntVar extends Variable {
    public IntVar(final Type header, final String name, final int offset) {
      super(header,name,offset,Integer.SIZE/Byte.SIZE);
    }
    @Override
    public Integer getValue(final ByteBuffer bb) {
      return bb.getInt(offset);
    }
  }
  
  private static class RealVar extends Variable {
    public RealVar(final Type header, final String name, final int offset) {
      super(header, name,offset,Float.SIZE/Byte.SIZE);
    }
    @Override
    public Float getValue(final ByteBuffer bb) {
      return bb.getFloat(offset);
    }
  }
  
  private static class RealArray extends Variable {
    final int n;
    public RealArray(final Type header, final String name, final int offset, final int n) {
      super(header,name,offset,Float.SIZE/Byte.SIZE*n);
      this.n = n;
    }
    @Override
    public float[] getValue(final ByteBuffer bb) {
      bb.position(offset);
      final float[] f = new float[n];
      bb.asFloatBuffer().get(f);
      return f;
    }
  }
  
  private static class DateTimeVar extends Variable {
    public DateTimeVar(final Type header, final String name, final int offset) {
      super(header,name,offset,4);
    }
    @Override
    public Date getValue(final ByteBuffer bb) {
      final long t = bb.getInt(offset) * 1000L;
      return new Date(t);
    }
  }
  
  // main header
  public final static Variable MAGIC_NUMBER = new CharVar(Type.MAIN,"MAGIC_NUMBER",0,14);
  public final static Variable ORIGINAL_FILE_NAME = new CharVar(Type.MAIN,"ORIGINAL_FILE_NAME",14,32);
  public final static Variable SW_VERSION = new ShortVar(Type.MAIN,"SW_VERSION",46);
  public final static Variable SYSTEM_TYPE = new ShortVar(Type.MAIN,"SYSTEM_TYPE",48);
  public final static Variable FILE_TYPE = new ShortVar(Type.MAIN,"FILE_TYPE",50);
  public final static Variable SERIAL_NUMBER = new CharVar(Type.MAIN,"SERIAL_NUMBER",52,10);
  public final static Variable SCAN_START_TIME = new DateTimeVar(Type.MAIN,"SCAN_START_TIME",62); 
  public final static Variable ISOTOPE_NAME = new CharVar(Type.MAIN,"ISOTOPE_NAME",66,8);
  public final static Variable ISOTOPE_HALFLIFE = new RealVar(Type.MAIN,"ISOTOPE_HALFLIFE",74);
  public final static Variable RADIOPHARMACEUTICAL = new CharVar(Type.MAIN,"RADIOPHARMACEUTICAL",78,32);
  public final static Variable GANTRY_TILT = new RealVar(Type.MAIN,"GANTRY_TILT",110);
  public final static Variable GANTRY_ROTATION = new RealVar(Type.MAIN,"GANTRY_ROTATION",114);
  public final static Variable BED_ELEVATION = new RealVar(Type.MAIN,"BED_ELEVATION",118);
  public final static Variable INTRINSIC_TILT = new RealVar(Type.MAIN,"INTRINSIC_TILT",122);
  public final static Variable WOBBLE_SPEED = new ShortVar(Type.MAIN,"WOBBLE_SPEED",126);
  public final static Variable TRANSM_SOURCE_TYPE = new ShortVar(Type.MAIN,"TRANSM_SOURCE_TYPE",128);
  public final static Variable DISTANCE_SCANNED = new RealVar(Type.MAIN,"DISTANCE_SCANNED",130);
  public final static Variable TRANSAXIAL_FOV = new RealVar(Type.MAIN,"TRANSAXIAL_FOV",134);
  public final static Variable ANGULAR_COMPRESSION = new ShortVar(Type.MAIN,"ANGULAR_COMPRESSION",138);
  public final static Variable COIN_SAMP_MODE = new ShortVar(Type.MAIN,"COIN_SAMP_MODE",140);
  public final static Variable AXIAL_SAMP_MODE = new ShortVar(Type.MAIN,"AXIAL_SAMP_MODE",142);
  public final static Variable ECAT_CALIBRATION_FACTOR = new RealVar(Type.MAIN,"ECAT_CALIBRATION_FACTOR",144);
  public final static Variable CALIBRATION_UNITS = new ShortVar(Type.MAIN,"CALIBRATION_UNITS",148);
  public final static Variable CALIBRATION_UNITS_LABEL = new ShortVar(Type.MAIN,"CALIBRATION_UNITS_LABEL",150);
  public final static Variable COMPRESSION_CODE = new ShortVar(Type.MAIN,"COMPRESSION_CODE",152);
  public final static Variable STUDY_TYPE = new CharVar(Type.MAIN,"STUDY_TYPE",154,12);
  public final static Variable PATIENT_ID = new CharVar(Type.MAIN,"PATIENT_ID",166,16);
  public final static Variable PATIENT_NAME = new CharVar(Type.MAIN,"PATIENT_NAME",182,32);
  public final static Variable PATIENT_SEX = new CharVar(Type.MAIN,"PATIENT_SEX",214,1);
  public final static Variable PATIENT_DEXTERITY = new CharVar(Type.MAIN,"PATIENT_DEXTERITY",215,1);
  public final static Variable PATIENT_AGE = new RealVar(Type.MAIN,"PATIENT_AGE",216);
  public final static Variable PATIENT_HEIGHT = new RealVar(Type.MAIN,"PATIENT_HEIGHT",220);
  public final static Variable PATIENT_WEIGHT = new RealVar(Type.MAIN,"PATIENT_WEIGHT",224);
  public final static Variable PATIENT_BIRTH_DATE = new IntVar(Type.MAIN,"PATIENT_BIRTH_DATE",228);
  public final static Variable PHYSICIAN_NAME = new CharVar(Type.MAIN,"PHYSICIAN_NAME",232,32);
  public final static Variable OPERATOR_NAME = new CharVar(Type.MAIN,"OPERATOR_NAME",264,32);
  public final static Variable STUDY_DESCRIPTION = new CharVar(Type.MAIN,"STUDY_DESCRIPTION",296,32);
  public final static Variable ACQUISITION_TYPE = new ShortVar(Type.MAIN,"ACQUISITION_TYPE",328);
  public final static Variable PATIENT_ORIENTATION = new ShortVar(Type.MAIN,"PATIENT_ORIENTATION",330);
  public final static Variable FACILITY_NAME = new CharVar(Type.MAIN,"FACILITY_NAME",332,20);
  public final static Variable NUM_PLANES = new ShortVar(Type.MAIN,"NUM_PLANES",352);
  public final static Variable NUM_FRAMES = new ShortVar(Type.MAIN,"NUM_FRAMES",354);
  public final static Variable NUM_GATES = new ShortVar(Type.MAIN,"NUM_GATES",356);
  public final static Variable NUM_BED_POS = new ShortVar(Type.MAIN,"NUM_BED_POS",358);
  public final static Variable INIT_BED_POSITION = new RealVar(Type.MAIN,"INIT_BED_POSITION",360);
  public final static Variable BED_POSITION = new RealArray(Type.MAIN,"BED_POSITION",364,15);
  public final static Variable PLANE_SEPARATION = new RealVar(Type.MAIN,"PLANE_SEPARATION",424);
  public final static Variable LWR_SCTR_THRES = new ShortVar(Type.MAIN,"LWR_SCTR_THRES",428);
  public final static Variable LWR_TRUE_THRES = new ShortVar(Type.MAIN,"LWR_TRUE_THRES",430);
  public final static Variable UPR_TRUE_THRES = new ShortVar(Type.MAIN,"UPR_TRUE_THRES",432);
  public final static Variable USER_PROCESS_CODE = new CharVar(Type.MAIN,"USER_PROCESS_CODE",434,10);
  public final static Variable ACQUISITION_MODE = new ShortVar(Type.MAIN,"ACQUISITION_MODE",444);
  public final static Variable BIN_SIZE = new RealVar(Type.MAIN,"BIN_SIZE",446);
  public final static Variable BRANCHING_FRACTION = new RealVar(Type.MAIN,"BRANCHING_FRACTION",450);
  public final static Variable DOSE_START_TIME = new DateTimeVar(Type.MAIN,"DOSE_START_TIME",454);
  public final static Variable DOSAGE = new RealVar(Type.MAIN,"DOSAGE",458);
  public final static Variable WELL_COUNTER_CORR_FACTOR = new RealVar(Type.MAIN,"WELL_COUNTER_CORR_FACTOR",462);
  public final static Variable DATA_UNITS = new CharVar(Type.MAIN,"DATA_UNITS",466,32);
  public final static Variable SEPTA_STATE = new ShortVar(Type.MAIN,"SEPTA_STATE",498);
  public final static Variable MAIN_CTI_FILL = new ShortArray(Type.MAIN,"FILL",500,6);
  
  // matrix image file subheader
  public final static Variable DATA_TYPE = new ShortVar(Type.IMAGE,"DATA_TYPE",0);
  public final static Variable NUM_DIMENSIONS = new ShortVar(Type.IMAGE,"NUM_DIMENSIONS",2);
  public final static Variable X_DIMENSION = new ShortVar(Type.IMAGE,"X_DIMENSION",4);
  public final static Variable Y_DIMENSION = new ShortVar(Type.IMAGE,"Y_DIMENSION",6);
  public final static Variable Z_DIMENSION = new ShortVar(Type.IMAGE,"Z_DIMENSION",8);
  public final static Variable X_OFFSET = new RealVar(Type.IMAGE,"X_OFFSET",10);
  public final static Variable Y_OFFSET = new RealVar(Type.IMAGE,"Y_OFFSET",14);
  public final static Variable Z_OFFSET = new RealVar(Type.IMAGE,"Z_OFFSET",18);
  public final static Variable RECON_ZOOM = new RealVar(Type.IMAGE,"RECON_ZOOM",22);
  public final static Variable SCALE_FACTOR = new RealVar(Type.IMAGE,"SCALE_FACTOR",26);
  public final static Variable IMAGE_MIN = new ShortVar(Type.IMAGE,"IMAGE_MIN",30);
  public final static Variable IMAGE_MAX = new ShortVar(Type.IMAGE,"IMAGE_MAX",32);
  public final static Variable X_PIXEL_SIZE = new RealVar(Type.IMAGE,"X_PIXEL_SIZE",34);
  public final static Variable Y_PIXEL_SIZE = new RealVar(Type.IMAGE,"Y_PIXEL_SIZE",38);
  public final static Variable Z_PIXEL_SIZE = new RealVar(Type.IMAGE,"Z_PIXEL_SIZE",42);
  public final static Variable FRAME_DURATION = new IntVar(Type.IMAGE,"FRAME_DURATION",46);
  public final static Variable FRAME_START_TIME = new IntVar(Type.IMAGE,"FRAME_START_TIME",50);
  public final static Variable FILTER_CODE = new ShortVar(Type.IMAGE,"FILTER_CODE",54);
  public final static Variable X_RESOLUTION = new RealVar(Type.IMAGE,"X_RESOLUTION",56);
  public final static Variable Y_RESOLUTION = new RealVar(Type.IMAGE,"Y_RESOLUTION",60);
  public final static Variable Z_RESOLUTION = new RealVar(Type.IMAGE,"Z_RESOLUTION",64);
  public final static Variable NUM_R_ELEMENTS = new RealVar(Type.IMAGE,"NUM_R_ELEMENTS",68);
  public final static Variable NUM_ANGLES = new RealVar(Type.IMAGE,"NUM_ANGLES",72);
  public final static Variable Z_ROTATION_ANGLE = new RealVar(Type.IMAGE,"Z_ROTATION_ANGLE",76);
  public final static Variable DECAY_CORR_FCTR = new RealVar(Type.IMAGE,"DECAY_CORR_FCTR",80);
  public final static Variable PROCESSING_CODE = new IntVar(Type.IMAGE,"PROCESSING_CODE",84);
  public final static Variable GATE_DURATION = new IntVar(Type.IMAGE,"GATE_DURATION",88);
  public final static Variable R_WAVE_OFFSET = new IntVar(Type.IMAGE,"R_WAVE_OFFSET",92);
  public final static Variable NUM_ACCEPTED_BEATS = new IntVar(Type.IMAGE,"NUM_ACCEPTED_BEATS",96);
  public final static Variable FILTER_CUTOFF_FREQUENCY = new RealVar(Type.IMAGE,"FILTER_CUTOFF_FREQUENCY",100);
  @Deprecated
  public final static Variable FILTER_RESOLUTION = new RealVar(Type.IMAGE,"FILTER_RESOLUTION",104);
  @Deprecated
  public final static Variable FILTER_RAMP_SLOPE = new RealVar(Type.IMAGE,"FILTER_RAMP_SLOPE",108);
  @Deprecated
  public final static Variable FILTER_ORDER = new ShortVar(Type.IMAGE,"FILTER_ORDER",112);
  @Deprecated
  public final static Variable FILTER_SCATTER_FRACTION = new RealVar(Type.IMAGE,"FILTER_SCATTER_FRACTION",114);
  @Deprecated
  public final static Variable FILTER_SCATTER_SLOPE = new RealVar(Type.IMAGE,"FILTER_SCATTER_SLOPE",118);
  public final static Variable ANNOTATION = new CharVar(Type.IMAGE,"ANNOTATION",122,40);
  public final static Variable MT_1_1 = new RealVar(Type.IMAGE,"MT_1_1",162);
  public final static Variable MT_1_2 = new RealVar(Type.IMAGE,"MT_1_2",166);
  public final static Variable MT_1_3 = new RealVar(Type.IMAGE,"MT_1_3",170);
  public final static Variable MT_2_1 = new RealVar(Type.IMAGE,"MT_2_1",174);
  public final static Variable MT_2_2 = new RealVar(Type.IMAGE,"MT_2_2",178);
  public final static Variable MT_2_3 = new RealVar(Type.IMAGE,"MT_2_3",182);
  public final static Variable MT_3_1 = new RealVar(Type.IMAGE,"MT_3_1",186);
  public final static Variable MT_3_2 = new RealVar(Type.IMAGE,"MT_3_2",190);
  public final static Variable MT_3_3 = new RealVar(Type.IMAGE,"MT_3_3",194);
  public final static Variable RFILTER_CUTOFF = new RealVar(Type.IMAGE,"RFILTER_CUTOFF",198);
  public final static Variable RFILTER_RESOLUTION = new RealVar(Type.IMAGE,"RFILTER_RESOLUTION",202);
  public final static Variable RFILTER_CODE = new ShortVar(Type.IMAGE,"RFILTER_CODE",206);
  public final static Variable RFILTER_ORDER = new ShortVar(Type.IMAGE,"RFILTER_ORDER",208);
  public final static Variable ZFILTER_CUTOFF = new RealVar(Type.IMAGE,"ZFILTER_CUTOFF",210);
  public final static Variable ZFILTER_RESOLUTION = new RealVar(Type.IMAGE,"ZFILTER_RESOLUTION",214);
  public final static Variable ZFILTER_CODE = new ShortVar(Type.IMAGE,"ZFILTER_CODE",218);
  public final static Variable ZFILTER_ORDER = new ShortVar(Type.IMAGE,"ZFILTER_ORDER",220);
  public final static Variable MT_1_4 = new RealVar(Type.IMAGE,"MT_1_4",222);
  public final static Variable MT_2_4 = new RealVar(Type.IMAGE,"MT_2_4",226);
  public final static Variable MT_3_4 = new RealVar(Type.IMAGE,"MT_3_4",230);
  public final static Variable SCATTER_TYPE = new ShortVar(Type.IMAGE,"SCATTER_TYPE",234);
  public final static Variable RECON_TYPE = new ShortVar(Type.IMAGE,"RECON_TYPE",236);
  public final static Variable RECON_VIEWS = new ShortVar(Type.IMAGE,"RECON_VIEWS",238);
  public final static Variable IMAGE_CTI_FILL = new ShortArray(Type.IMAGE,"FILL",240,87);
  public final static Variable IMAGE_USER_FILL = new ShortArray(Type.IMAGE,"FILL",414,48);
  
  // Not actually an ECAT variable, but rather a property of the standard
  public final static Variable PIXEL_SIZE_UNITS = new Constant(Type.IMAGE, "pixel size units", "cm");
  
  public static final class AcquisitionType {
    private final short type;
    private final String name;
    
    private AcquisitionType(final int type, final String name) {
      this.type = (short)type;
      this.name = name;
    }
    
    public String toString() {
      return name + " (" + type + ")";
    }
    
    public static final AcquisitionType UNDEFINED = new AcquisitionType(0, "Undefined");
    public static final AcquisitionType BLANK = new AcquisitionType(1, "Blank");
    public static final AcquisitionType TRANSMISSION = new AcquisitionType(2, "Transmission");
    public static final AcquisitionType STATIC_EMISSION = new AcquisitionType(3, "Static emission");
    public static final AcquisitionType DYNAMIC_EMISSION = new AcquisitionType(4, "Dynamic emission");
    public static final AcquisitionType GATED_EMISSION = new AcquisitionType(5, "Gated emission");
    public static final AcquisitionType TRANSMISSION_RECTILINEAR = new AcquisitionType(6, "Transmission rectilinear");
    public static final AcquisitionType EMISSION_RECTILINEAR = new AcquisitionType(7, "Emission rectilinear");
    
    private final static AcquisitionType[] TYPES = {
	UNDEFINED, BLANK, TRANSMISSION, STATIC_EMISSION, DYNAMIC_EMISSION, GATED_EMISSION,
	TRANSMISSION_RECTILINEAR, EMISSION_RECTILINEAR
    };
    
    public final static AcquisitionType getInstance(final short type) {
      return TYPES[type];
    }
  }
}
