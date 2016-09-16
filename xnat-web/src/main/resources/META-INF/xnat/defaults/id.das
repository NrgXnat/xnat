//
// Default XNAT anonymization script
// XNAT http://www.xnat.org
// Copyright (c) 2016, Washington University School of Medicine
// All Rights Reserved
//
// Released under the Simplified BSD.
//
project != "Unassigned" ? (0008,1030) := project
(0010,0010) := subject
(0010,0020) := session
