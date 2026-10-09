/*
 * DicomEdit: org.nrg.dicom.dicomedit.ScriptRunner
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.dicomedit.functions.LookupManager;
import org.nrg.dicom.mizer.objects.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 * An app to quickly apply specified scripts to specified data.
 *
 * First argument is path to the anon script.
 * Second argument is path to directory of DICOM objects to which the script will be applied.
 * Third argument is root path to output directory.
 *
 * Modified DICOM objects will be written in the output directory with the same naming structure.
 *
 */
public class Anonymizer {
    private String anonScriptFileString;
    private String lookupTablePathString;
    private String inRootFileString;
    private String outRootFileString;
    private PathMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(Anonymizer.class);

    public Anonymizer( String anonScriptFileString, String inRootFileString, String outRootFileString, String lookupTablePathString) {
        this.anonScriptFileString = anonScriptFileString;
        this.lookupTablePathString = lookupTablePathString;
        this.inRootFileString = inRootFileString;
        this.outRootFileString = outRootFileString;
        this.mapper = new PathMapper( Paths.get(inRootFileString).toFile(), Paths.get(outRootFileString).toFile());
    }

    public static void main(String[] args) {
        if( args.length == 3 ) {
            Anonymizer anonymizer= new Anonymizer(args[0], args[1], args[2], null);
            anonymizer.anon();
        }
        else if( args.length == 4) {
            Anonymizer anonymizer= new Anonymizer(args[0], args[1], args[2], args[3]);
            anonymizer.anon();
        }
        else {
            System.out.println("Usage: Anonymizer <path-to-script> <path-to-input-root> <path-to-output-root> [<path-to-lookupTable>]");
        }
    }

    public void anon() {
        final BaseScriptApplicator applicator;

        try {
            try (InputStream is = new FileInputStream( anonScriptFileString)) {
                applicator = BaseScriptApplicator.getInstance( is);
            }

            File inFile = new File( inRootFileString);
            File outFile = new File( outRootFileString);
            if( lookupTablePathString != null) {
                LookupManager.getInstance().load( Paths.get( lookupTablePathString).toFile());
            }
            mapper = new PathMapper( inFile, outFile);
            anonymize( applicator, inFile, mapper.map( inFile));

        } catch (Exception e) {
            String format = "Error processing \nscript = {0}, \ninputRoot = {1}, \noutputRoot = {2}\n";
            logger.error( MessageFormat.format(format, anonScriptFileString, inRootFileString, outRootFileString), e);
        }
    }

    protected void anonymize(BaseScriptApplicator applicator, File inFile, File outFile)  {
        try {
            if (inFile.isDirectory()) {
                for (File f : inFile.listFiles()) {
                    anonymize(applicator, f, mapper.map(f));
                }
            } else {
                AnonymizationResult result = applicator.apply(inFile);
                try {
                    if (result instanceof AnonymizationResultReject) {
                        logger.info("File rejected: %s", inFile.getAbsolutePath());
                    }
                    else if (result instanceof AnonymizationResultError) {
                        logger.error("Error processing %s", inFile.getAbsolutePath(), String.join("\n", result.getMessages()));
                    } else {
                        File parent = outFile.getAbsoluteFile().getParentFile();
                        if (!parent.exists()) {
                            Files.createDirectories(parent.toPath());
                        }
                        try (FileOutputStream out = new FileOutputStream(outFile)) {
                            result.getDicomObject().write(out);
                        }
                    }
                } finally {
                    // A pixel edit stages the redacted frames in a scratch file that the object
                    // reads from on write, so it cannot be released until after the write. Nothing
                    // else deletes it, and over a directory of objects that is one file apiece.
                    result.getDicomObject().releaseScratchFiles();
                }
            }
        }
        catch( Exception e) {
            String format = "Error processing \nscript = {0}, \ninput = {1}, \noutput = {2}\n";
            logger.error( MessageFormat.format(format, anonScriptFileString, inFile.getAbsolutePath(), outFile.getAbsolutePath()), e);
        }
    }

    protected class PathMapper {
        Path inRootPath;
        Path outRootPath;

        public PathMapper( File inFile, File outFile) {
            if( inFile.isDirectory() && outFile.isFile()) {
                throw new IllegalArgumentException( MessageFormat.format("Can not map input directory '{0}' to file '{1}'.", inFile, outFile));
            }
            if( inFile.isDirectory()) {
                inRootPath = inFile.getAbsoluteFile().toPath().toAbsolutePath();
            }
            else {
                inRootPath = inFile.getAbsoluteFile().getParentFile().toPath().toAbsolutePath();
            }
            if( (! outFile.exists()) || outFile.isDirectory()) {
                outRootPath = outFile.getAbsoluteFile().toPath().toAbsolutePath();
            }
            else {
                outRootPath = outFile.getAbsoluteFile().getParentFile().toPath().toAbsolutePath();
            }
        }

        public File map( File inFile) throws IOException {
            Path inFilePath = inFile.toPath().toAbsolutePath();
            logger.debug( "inRootPath: " + inRootPath.toString());
            logger.debug( "inFilePath: " + inFilePath.toString());
            File outFile = outRootPath.resolve( inRootPath.relativize( inFilePath)).toFile();
//            File parent = outFile.getAbsoluteFile().getParentFile();
//            if( ! parent.exists()) {
//                Files.createDirectories( parent.toPath());
//            }
            return outFile;
        }
    }

}
