/*
 * xnat-data-builder: org.nrg.xnat.build.utils.PropertyReader
 * XNAT https://www.xnat.org
 * Copyright (c) 2021, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xnat.build.utils

/**
 * This code was taken directly from Pawel Oczadly as posted at:
 *
 * https://paweloczadly.github.io/dev/2014/09/23/groovy-reading-and-writing-to-properties-file
 *
 * It's a very slick implementation and should be pulled in core Groovy, I think! Many thanks
 * to Pawel. - Rick
 */
class PropertyReader {
    String filePath

    PropertyReader(String filePath) {
        this.filePath = filePath
    }

    def propertyMissing(String name) {
        Properties props = new Properties()
        File propsFile = new File(filePath)
        propsFile.withInputStream {
            props.load it
        }
        props."$name"
    }

    def propertyMissing(String name, args) {
        Properties props = new Properties()
        File propsFile = new File(filePath)

        props.load propsFile.newDataInputStream()
        props.setProperty name, args.toString() - '[' - ']'
        props.store propsFile.newWriter(), null
    }
}
