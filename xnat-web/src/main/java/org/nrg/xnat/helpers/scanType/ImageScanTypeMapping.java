/*
 * org.nrg.xnat.helpers.scanType.ImageScanTypeMapping
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
import com.google.common.collect.Lists;
import org.nrg.xdat.model.XnatImagescandataI;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class ImageScanTypeMapping extends AbstractScanTypeMapping<ImageScanTypeMapping.ScanTypeHistory> implements ScanTypeMappingI {
    public ImageScanTypeMapping(final String project, final String dbName) {
        super(project, dbName, buildSql(project));
    }

    private static final String buildSql(final String project) {
        if (Strings.isNullOrEmpty(project)) {
            return null;
        } else {
            return "SELECT DISTINCT REPLACE(REPLACE(REPLACE(REPLACE(UPPER(scan.series_description),' ',''),'_',''),'-',''),'*','') AS series_description,scan.type,frames FROM xnat_imagescandata scan LEFT JOIN xnat_experimentData isd ON scan.image_session_id=isd.id WHERE scan.series_description IS NOT NULL AND isd.project='" + project + "';";
        }
    }


    @Override
    protected String getMappedType(XnatImagescandataI scan,
            Map<String, ScanTypeHistory> histories) {
        final String seriesDescription = scan.getSeriesDescription();
        final String formatted = AbstractScanTypeMapping.standardizeFormat(seriesDescription);

        final ScanTypeHistory history = histories.get(formatted);
        return null == history ? null : history.match(seriesDescription, scan.getFrames());
    }

    @Override
    protected ScanTypeHistory newScanHistory() { return new ScanTypeHistory(); }

    @Override
    protected void update(final ScanTypeHistory h, final Hashtable<?,?> row) {
        h.add((String)row.get("type"),(Integer)row.get("frames"));
    }


    static final class ScanTypeHistory {
        private List<ScanType> types= Lists.newArrayList();

        public void add(ScanType st){
            types.add(st);
        }

        public void add(String t, Integer f){
            types.add(new ScanType(t,f));
        }

        public String match(final String desc, final Integer frames){
            if(types.size()==1){
                return types.get(0).getType();
            }

            //match by frames
            if(frames!=null){
                for(ScanType st: types){
                    if(frames.equals(st.getFrames())){
                        return st.getType();
                    }
                }
            } else {
                for(ScanType st: types){
                    if(st.getFrames()==null){
                        return st.getType();
                    }
                }
            }

            return types.get(0).getType();
        }
    }

    private static final class ScanType {
        private final String _type;
        private final Integer _frames;

        public ScanType(final String t, final Integer f){
            _type=t;
            _frames=f;
        }

        public Integer getFrames() {
            return _frames;
        }

        public String getType() {
            return _type;
        }

    }
}