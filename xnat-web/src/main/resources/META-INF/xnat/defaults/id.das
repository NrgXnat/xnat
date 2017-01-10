//
// Default XNAT anonymization script
// XNAT http://www.xnat.org
// Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
// All Rights Reserved
//
// Released under the Simplified BSD.
//
project != "Unassigned" ? (0008,1030) := project
(0010,0010) := subject
(0010,0020) := session
