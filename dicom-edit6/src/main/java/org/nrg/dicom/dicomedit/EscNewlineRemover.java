package org.nrg.dicom.dicomedit;

import java.util.ArrayList;
import java.util.List;

/**
 * This processor strips all escaped newlines out of the script.
 */
public class EscNewlineRemover {

    public EscNewlineRemover() {}

    public List<String> process(List<String> lines) {
        this.lines = lines;
        joinedLines = new ArrayList<>(lines.size());
        if( ! lines.isEmpty()) {
            int index = 0;
            while( index < lines.size()) {
                index = stitch( lines.get(index), index+1);
            }
        }
        return joinedLines;
    }

    private List<String> lines;
    private List<String> joinedLines;

    private int stitch( String s, int nexti) {
        if( s.endsWith("\\")) {
            StringBuilder sb = new StringBuilder(s.substring(0,s.length()-1));
            if( nexti < lines.size()) {
                sb.append(lines.get(nexti));
                nexti++;
                return stitch(sb.toString(), nexti);
            } else {
                joinedLines.add(sb.toString());
                return nexti;
            }
        } else {
            joinedLines.add(s);
            return nexti;
        }
    }

}
