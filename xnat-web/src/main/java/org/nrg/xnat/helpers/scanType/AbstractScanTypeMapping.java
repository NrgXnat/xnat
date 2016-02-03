/*
 * org.nrg.xnat.helpers.scanType.AbstractScanTypeMapping
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 7/10/13 9:04 PM
 */
package org.nrg.xnat.helpers.scanType;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.nrg.xdat.model.XnatImagescandataI;
import org.nrg.xdat.om.XnatProjectdata;
import org.nrg.xft.XFTTable;
import org.nrg.xft.exception.DBPoolException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

public abstract class AbstractScanTypeMapping<HistoryType> implements ScanTypeMappingI {
    private final Logger logger = LoggerFactory.getLogger(AbstractScanTypeMapping.class);

    private final String dbName;
    private final String scanSelectSql;
    private final Map<String,HistoryType> histories = Maps.newHashMap();

    private final static String defaultScanType = "unknown";

    /**
     * 
     * @param dbName name of the database in which scan type mapping is stored
     * @param scanSelectSql SQL statement to extract scan type history
     */
    public AbstractScanTypeMapping(final String projectId, final String dbName, final String scanSelectSql) {
        this.dbName = dbName;
        this.scanSelectSql = scanSelectSql;
        XnatProjectdata project = XnatProjectdata.getProjectByIDorAlias(projectId,null,false);
        if (project != null && project.getUseScanTypeMapping()) {
            loadScanTypes();
        }
    }

    private void loadScanTypes() {
        assert histories.isEmpty();
        if (!Strings.isNullOrEmpty(scanSelectSql)){
            try {
                final XFTTable t = XFTTable.Execute(scanSelectSql, dbName, "system");
                t.resetRowCursor();
                while (t.hasMoreRows()){
                    final Hashtable<?,?> rowHash = t.nextRowHash();
                    final String sd = (String)rowHash.get("series_description");
                    if (!histories.containsKey(sd)){
                        histories.put(sd, newScanHistory());
                    }
                    update(histories.get(sd), rowHash);
                }
            } catch (SQLException e) {
                logger.error("unable to load scan types",e);
            } catch (DBPoolException e) {
                logger.error("unable to contact database to load scan types",e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.nrg.xnat.helpers.scanType.ScanTypeMappingI#setType(org.nrg.xdat.model.XnatImagescandataI)
     */
    public void setType(final XnatImagescandataI scan) {
        if (Strings.isNullOrEmpty(scan.getType())) {
            String seriesDescription = scan.getSeriesDescription();
            if (Strings.isNullOrEmpty(seriesDescription)) {
                scan.setType(defaultScanType);
            } else {
                if (seriesDescription.startsWith("INVALID: ")) {
                    seriesDescription = seriesDescription.substring(9);
                }              
                if (Strings.isNullOrEmpty(seriesDescription)) {
                    scan.setType(defaultScanType);
                } else {
                    final String mappedType = getMappedType(scan, histories);
                    scan.setType(Strings.isNullOrEmpty(mappedType) ? seriesDescription : mappedType);
                }
            }
        }
    }
    

    /**
     * For the given scan, with the provided scan type histories, return the mapped scan type.
     * @param scan
     * @param histories
     * @return
     */
    protected abstract String getMappedType(final XnatImagescandataI scan,
            final Map<String,HistoryType> histories);

    /**
     * Create a new scan type history record.
     * @return new HistoryType
     */
    protected abstract HistoryType newScanHistory();
    
    /**
     * Update the given scan history with the contents of the scan types row.
     * @param h
     * @param row
     */
    protected abstract void update(HistoryType h, Hashtable<?,?> row);
    
    /**
     * Reformats the provided series description for type mapping.
     * @param originalString series description as given in image metadata
     * @return standardized form; if originalString is null, returns the empty string.
     */
    public static String standardizeFormat(final String originalString) {
        if (null == originalString) {
            return "";
        } else {
            return originalString.replaceAll("[ _\\-\\*]", "").toUpperCase();
        }
    }
}
