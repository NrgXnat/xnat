/*
 * DicomEdit: org.nrg.dicom.dicomedit.ScriptRunner
 * XNAT http://www.xnat.org
 * Copyright (c) 2016, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.AnonymizationResult;
import org.nrg.dicom.mizer.objects.AnonymizationResultSuccess;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

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
public class SerialScriptAnonymizer {
    private List<DE6Script> scripts;
    private Path inRootPath;
    private Path outRootPath;
    private PathMapper mapper;

    private static final Logger logger = LoggerFactory.getLogger(SerialScriptAnonymizer.class);

    public SerialScriptAnonymizer(List<DE6Script> scripts, Path inRootPath, Path outRootPath) throws MizerException {
        this.scripts = scripts;
        this.inRootPath = inRootPath;
        this.outRootPath = outRootPath;
        this.mapper = new PathMapper( inRootPath.toFile(), outRootPath.toFile());
    }

    public static void main(String[] args) {
        List<DE6Script> scripts = new ArrayList<>();
        Path inRootPath = null;
        Path outRootPath = null;

        int iarg = 0;
        while( iarg < args.length) {
            switch( args[ iarg]) {
                case "-s":
                    String scriptPathArg = nextArg( args, ++iarg);
                    if( scriptPathArg == null) {
                        String msg = "Missing scriptPath argument for flag -s";
                        System.out.println(msg);
                        System.exit(1);
                    }
                    String[] tokens = scriptPathArg.split(",");
                    File scriptFile = new File(tokens[0].trim());
                    File lookupTableFile = (tokens.length > 1)? new File(tokens[1].trim()): null;

                    try {
                        FileReader scriptReader = new FileReader( scriptFile);
                        FileReader lookupFileReader = (lookupTableFile != null)? new FileReader( lookupTableFile): null;
                        scripts.add( new DE6Script.Builder().statements( scriptReader).lookupTable(lookupFileReader).build());
                    }
                    catch( Exception e) {
                        String msg = "Error loading script: " + e.getMessage();
                        System.out.println(msg);
                        System.exit(1);
                    }
                    iarg++;
                    break;
                case "-i":
                    String inRootString = nextArg( args, ++iarg);
                    if( inRootString == null) {
                        String msg = "Missing inpuut root path argument for flag -i";
                        System.out.println(msg);
                        System.exit(1);
                    }
                    inRootPath = Paths.get(inRootString);
                    iarg++;
                    break;
                case "-o":
                    String outRootString = nextArg( args, ++iarg);
                    if( outRootString == null) {
                        String msg = "Missing output root path argument for flag -i";
                        System.out.println(msg);
                        System.exit(1);
                    }
                    outRootPath = Paths.get( outRootString);
                    iarg++;
                    break;
                default:
                    System.out.println("Unknown argument: " + args[iarg]);
                    printUsage();
                    System.exit(1);
            }
        }

        if( scripts.isEmpty()) {
            System.out.println("No scripts were specified.");
            printUsage();
            System.exit(1);
        }

        if( inRootPath == null) {
            System.out.println("No input path was specified.");
            printUsage();
            System.exit(1);
        }

        if( outRootPath == null) {
            System.out.println("No output path was specified.");
            printUsage();
            System.exit(1);
        }

        try {
            SerialScriptAnonymizer anonymizer = new SerialScriptAnonymizer( scripts, inRootPath, outRootPath);
            anonymizer.anon();

        } catch (MizerException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String nextArg( String[] args, int iarg) {
        return (iarg < args.length)? args[iarg]: null;
    }

    private static void printUsage() {
        System.out.println("anon -s <script-file>[,<lookup-file>] [-s <script-file>[,<lookup-file> ...] -i <path to input dicom file or dir> -o <path to output dir>");
        System.out.println("\tScripts will be processed in the order specified.");
        System.out.println("\tEach script may optionally reference a lookup table.");
        System.out.println();
    }

    public void anon() throws IOException {
        final List<SerialScriptApplicator> applicators = new ArrayList<>();
        final SerialScriptApplicator applicator;

        applicator = new SerialScriptApplicator( scripts);

        File inFile = inRootPath.toFile();
        File outFile = outRootPath.toFile();
        mapper = new PathMapper( inFile, outFile);
        List<File> errors = anonymize( applicator, inFile, mapper.map( inFile));
        logger.warn("The following {} files generated errors:", errors.size());
        for( File f: errors) {
            logger.warn("{}", f);
        }
    }

    /**
     * Anonymize the input file and write result to outFile. Return a list of files that had processing errors.
     *
     * Log all exceptions but do not throw the exception if it contains "Not a DICOM Stream".  This allows recursion
     * through directories that contain file types in addition to DICOM.
     * Log all exceptions and record files with errors.  This allows the recursion through directories to continue
     * despite some errors.
     *
     * @param applicator
     * @param inFile
     * @param outFile
     * @return list of files that had processing errors.
     */
    protected List<File> anonymize( SerialScriptApplicator applicator, File inFile, File outFile) {
        List<File> errors = new ArrayList<>();

        try {
            if (inFile.isDirectory()) {
                for (File f : inFile.listFiles()) {
                    errors.addAll( anonymize(applicator, f, mapper.map(f)));
                }
            } else {
                if( isDICOMPart10( inFile)) {
                    AnonymizationResult result = applicator.apply(inFile);
                    if (result instanceof AnonymizationResultSuccess) {
                        File parent = outFile.getAbsoluteFile().getParentFile();
                        if (!parent.exists()) {
                            Files.createDirectories(parent.toPath());
                        }
                        try (FileOutputStream out = new FileOutputStream(outFile)) {
                            result.getDicomObject().write(out);
                        }
                    }
                }
                else {
                    logger.warn("Skipped processing non-Part10 DICOM input: {}", inFile);
                }
            }
        }
        catch( Exception e) {
            if( isNotDICOM(e)) {
                String format = "Skipped processing non-DICOM input = {0}\n";
                String msg = MessageFormat.format(format, inFile);
                logger.warn( msg);
            }
            else {
                String format = "Error processing input = {0}, \noutput = {1}\n";
                String msg = MessageFormat.format(format, inFile, outFile);
                logger.error( msg, e);
                errors.add( inFile);
            }
        }
        return errors;
    }

    /**
     *
     * @param e
     * @return
     */
    public boolean isNotDICOM( Exception e) {
        return e.getMessage().contains("Not a DICOM Stream");
    }

    /**
     * All DICOM Part 10 files should have 'DICM' in bytes 128-131.
     *
     * @param f File to test for DICOM Part10 compliance.
     * @return true if compliant, false otherwise.
     */
    public boolean isDICOMPart10( File f) {
        boolean b = false;
        try (InputStream is = new FileInputStream(f)) {
            byte[] ba = new byte[132];
            int rlen = is.read(ba, 0, ba.length);
            if( rlen == ba.length && hasPrefix( ba)) {
                b = true;
            }
        } catch (IOException e) {
            b =  false;
        }
        return b;
    }

    /**
     * is 'DICM' in bytes 128-131?
     *
     * @param ba
     * @return
     */
    public boolean hasPrefix( byte[] ba) {
        return (ba.length >= 132) && (ba[128] == 'D' && ba[129] == 'I' && ba[130] == 'C' && ba[131] == 'M');
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
