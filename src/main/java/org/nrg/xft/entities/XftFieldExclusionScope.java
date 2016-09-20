/*
 * core: org.nrg.xft.entities.XftFieldExclusionScope
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.entities;

public enum XftFieldExclusionScope {
    System,
    Project,
    DataType;

    public static XftFieldExclusionScope Default = System;
}
