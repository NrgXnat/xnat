package org.nrg.dicom.dicomedit.pixels;

import org.nrg.dicom.dicomedit.functions.FunctionManager;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Provide pixel editing service.
 *
 * Singleton class only created if a script requires it.
 * Discovers available PixelEditHandler providers on classpath
 */
public class PixelEditorManager {
    private static PixelEditorManager manager;
    private static List<PixelEditHandler> handlers;
    private static final Logger logger = LoggerFactory.getLogger(PixelEditorManager.class);

    /**
     * Construct the private static instance.
     */
    private PixelEditorManager() {
        handlers = new ArrayList<>();
        discoverHandlers();
    }

    public static void load( PixelEditHandler pixelEditHandler) {
        if( manager == null) getInstance();
        logger.info("Registering PixelEditHandler: {}", pixelEditHandler);
        handlers.add( pixelEditHandler);
    }

    public static void load( List<PixelEditHandler> handlers) {
        if( manager == null) getInstance();
        handlers.forEach( h -> logger.info("Registering PixelEditHandler: {}", h));
        handlers.addAll( handlers);
    }

    public void discoverHandlers() {
        ServiceLoader<PixelEditHandler> loader = ServiceLoader.load( PixelEditHandler.class);
        loader.forEach( h -> logger.info("Registering PixelEditHandler: {}", h));
        loader.iterator().forEachRemaining( handlers::add);
    }

    /**
     * Return the singleton instance.
     *
     * @return PixelEditorManager.
     */
    public static PixelEditorManager getInstance() {
        if( manager == null) {
            manager = new PixelEditorManager();
        }
        return manager;
    }

    /**
     * Apply the requested pixel editing to the DICOM object.
     *
     * @param shape String specifying shape of region of interest.
     * @param shapeProperties Properties needed to specify regions of given shape.
     * @param fillAlgorithm String specifying algorithm to apply to region of interest.
     * @param fillAlgorithmProperties Properties needed by requested algorithm.
     * @param dicomObject DicomObjectI to be edited.
     * @throws Exception if processing error occurs or UnsupportedOperationException if a PixelEditHandler was not
     * found for the requested operation.
     */
    public void apply(String shape, String shapeProperties, String fillAlgorithm, String fillAlgorithmProperties, DicomObjectI dicomObject) throws Exception {
        Optional<PixelEditHandler> any = handlers.stream()
                .filter(it -> it.handles(shape, shapeProperties, fillAlgorithm, fillAlgorithmProperties, dicomObject))
                .findAny();
        if( any.isPresent()) {
            any.get().apply( shape, shapeProperties, fillAlgorithm, fillAlgorithmProperties, dicomObject);
        }
        else {
            throw new UnsupportedOperationException("No PixelEditHandler for " + shape + ", " + fillAlgorithm);
        }
    }
}
