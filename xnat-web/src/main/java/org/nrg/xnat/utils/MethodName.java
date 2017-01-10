/*
 * web: org.nrg.xnat.utils.MethodName
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.utils;

public final class MethodName {

    private static final int CLIENT_CODE_STACK_INDEX;
    
    static {
        // Finds out the index of "this code" in the returned stack trace - funny but it differs in JDK 1.5 and 1.6
        int i = 0;
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            i++;
            if (ste.getClassName().equals(MethodName.class.getName())) {
                break;
            }
        }
        CLIENT_CODE_STACK_INDEX = i;
    }

    public static void main(String[] args) {
        System.out.println("currentMethodName() = " + currentMethodName());
        System.out.println("CLIENT_CODE_STACK_INDEX = " + CLIENT_CODE_STACK_INDEX);
    }

    public static String currentMethodName() {
        return Thread.currentThread().getStackTrace()[CLIENT_CODE_STACK_INDEX].getMethodName();
    }
}
