/*
 * web: xstypes.js
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

/**
 * Script is likely not used. Code commented to prepare for deletion.
 */

//Date.from_xsDateTime = function(dt) {
//  if (null == dt) {
//    return null;
//  }
//
//  // 1: year
//  // 2: month
//  // 3: day
//  // 4: hours
//  // 5: minutes
//  // 6: seconds
//  // 7: fractional seconds (optional, part of 6)
//  // 8: TZ specification (optional)
//  // 9: TZ offset hours (optional, part of 8)
//  // 10: TZ offset minutes (optional, part of 8)
//  var result = dt.match(/(\-?\d{4,})\-(\d\d)\-(\d\d)T(\d\d)\:(\d\d)\:(\d\d(\.\d*)?)(Z|([\-\+]\d\d):(\d\d))?/);
//
//  if (null == result) {
//    return null;		// couldn't parse string
//  }
//
//  if (result[8]) {
//    // DateTime includes time zone specification
//    var isGMT = 'Z' == result[8];
//    var date = new Date(Date.UTC(result[1], result[2]-1, result[3],
//			     result[4], result[5], result[6]));
//    if (!isGMT) {
//      var offset = 1000*60 * (parseInt(result[10].replace(/0?(\d+)/, '$1'))
//			      + 60
//			      * parseInt(result[9].replace(/\+?0?(\d+)/, '$1')));
//      date.setTime(date.getTime() - offset);
//    }
//    return date;
//  } else {
//    // DateTime includes no timezone spec; assume local time
//    return new Date(result[1], result[2]-1, result[3],
//		    result[4], result[5], result[6]);
//  }
//}
