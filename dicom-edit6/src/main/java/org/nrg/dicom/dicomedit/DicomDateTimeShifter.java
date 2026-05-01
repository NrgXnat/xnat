package org.nrg.dicom.dicomedit;

public interface DicomDateTimeShifter {
   default long toSeconds( long increment, String units) {
       switch (units.toLowerCase()) {
           case "years":
               return increment * 365 * 24 * 60 * 60;
           case "days":
               return increment * 24 * 60 * 60;
           case "hours":
               return increment * 60 * 60;
           case "minutes":
               return increment * 60;
           case "seconds":
               return increment;
           default:
               throw new UnsupportedOperationException("Unsupported time-shift units: " + units);
       }
   }

    default String shiftTime( String dicomTimeString, long increment, String units) {
        return shiftTimeBySeconds(dicomTimeString, toSeconds(increment, units));
    }
    default String shiftDateTime( String dicomDateTimeString, long increment, String units) {
       return shiftDateTimeBySeconds(dicomDateTimeString, toSeconds(increment, units));
    }

    String shiftTimeBySeconds( String dicomTimeString, long seconds);
    String shiftDateTimeBySeconds( String dicomDateTimeString, long seconds);

}
