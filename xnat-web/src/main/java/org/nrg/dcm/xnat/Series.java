/*
 * dicom-xnat-mx: org.nrg.dcm.xnat.Series
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */
package org.nrg.dcm.xnat;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.nrg.attr.Utils;

/**
 * @author Kevin A. Archie &lt;karchie@wustl.edu&gt;
 *
 */
public final class Series implements Comparable<Series> {
  private final static Comparator<String> nsc = new Utils.MaybeNumericStringComparator();
  private final String number;
  private final String uid;
  private final SortedSet<String> modalities = new TreeSet<String>();
  private final Set<String> sopClasses = new LinkedHashSet<String>();
  
  public Series(final String number, final String uid) {
    this.number = number;
    this.uid = uid;
    if (null == this.number) {
      throw new IllegalArgumentException("Series " + uid + " has no number");
    }
    if (null == this.uid){
      throw new IllegalArgumentException("Series Number " + number + " has no UID");
    }
  }
  
  public Series addModality(final String modality) {
    modalities.add(modality);
    return this;
  }
  
  public Series addSOPClass(final String uid) {
    sopClasses.add(uid);
    return this;
  }
  
  public SortedSet<String> getModalities() {
    return Collections.unmodifiableSortedSet(modalities);
  }
  
  public Set<String> getSOPClasses() {
    return Collections.unmodifiableSet(sopClasses);
  }
  
  public String getNumber() { return number; }
  
  public String getUID() { return uid; }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
   public int compareTo(final Series o) {
     final int c1 = nsc.compare(number, o.number);
     return 0 == c1 ? uid.compareTo(o.uid) : c1; 
   }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(final Object o) {
    return o instanceof Series && uid.equals(((Series)o).uid) && number.equals(((Series)o).uid);
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return 37 * uid.hashCode() + number.hashCode();
  }
}
