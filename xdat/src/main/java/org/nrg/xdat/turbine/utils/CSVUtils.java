package org.nrg.xdat.turbine.utils;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;

public class CSVUtils {

    public static final CSVFormat DEFAULT_FORMAT = CSVFormat.DEFAULT.builder()
                                                                    .setIgnoreSurroundingSpaces(true)
                                                                    .setIgnoreEmptyLines(true).build();
    public static final CSVFormat ENCODING_FORMAT = CSVFormat.DEFAULT.builder()
                                                                    .setQuoteMode(QuoteMode.ALL)
                                                                    .setAutoFlush(true).build();

}
