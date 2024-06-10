/*
 * web: ReplaceUrlPathsWithBase64.groovy
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *  
 * Released under the Simplified BSD.
 */

@Grab('commons-io:commons-io:2.5')

import org.apache.commons.io.FileUtils

import java.nio.file.Paths

if (args.size() == 0) {
    println("You must specify the path to at least one CSS file to be processed. For example:");
    println("");
    println(" groovy utilities/ReplaceUrlPathsWithBase64.groovy src/main/webapp/style/app.css src/main/webapp/style/icons.css");
    println("");
    println("This will replace all file references in url() directives with the base64-encoded");
    println("contents of the file. The original file will be preserved with the '.bak' extension.");
    return
}

def pattern = ~ /^(?<open>.*url\()'(?<file>.*)'(?<close>\).*)$/
args.each { def String filename ->
    def source = Paths.get(filename)
    def input = source.toFile()
    def output = Paths.get("${filename}.out").toFile()
    def backup = Paths.get("${filename}.bak").toFile()

    [output, backup].each { def file ->
        if (file.exists()) {
            FileUtils.forceDelete(file)
        }
    }

    output.withWriter { def writer ->
        input.eachLine { def String line ->
            if (line.contains("url('")) {
                def matcher = line =~ pattern
                def open = matcher[0][1]
                def close = matcher[0][3]
                def file = source.parent.resolve(Paths.get(matcher[0][2] as String)).toFile()
                if (file.exists()) {
                    writer.print "${open}data:image/${file.name.endsWith(".png") ? "png" : "gif"};base64,"
                    file.readBytes().encodeBase64().writeTo(writer)
                    writer.println("${close}")
                } else {
                    println("ERROR: The file ${file.getPath()} can't be found")
                    writer.println("${line} /* The file ${file.getPath()} can't be found */")
                }
            } else {
                writer.println(line)
            }
        }
    }

    FileUtils.moveFile(input, backup)
    FileUtils.moveFile(output, input)
}
