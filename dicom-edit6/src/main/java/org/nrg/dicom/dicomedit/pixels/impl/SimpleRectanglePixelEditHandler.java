package org.nrg.dicom.dicomedit.pixels.impl;

import org.nrg.dicom.dicomedit.pixels.PixelEditHandler;
import org.nrg.dicom.mizer.exceptions.MizerException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class SimpleRectanglePixelEditHandler implements PixelEditHandler {
    private static final Logger logger = LoggerFactory.getLogger(SimpleRectanglePixelEditHandler.class);

    @Override
    public boolean handles( String shape, String shapeProperties, String algorithm, String algorithmProperties, DicomObjectI dicomObject) {
        boolean knownShape = "RECTANGLE".equals( shape != null? shape.trim().toUpperCase(): "");
        boolean knownAlogorithm = "SOLID".equals( algorithm != null? algorithm.trim().toUpperCase(): "");
        return knownShape && knownAlogorithm;
    }

    @Override
    public void apply( String shape, String shapePropertiesString, String algorithm,
                     String algorithmPropertiesString, DicomObjectI dicomObject) throws Exception {

        RectangleShapeProperties shapeProperties = new RectangleShapeProperties( shapePropertiesString);
        FillAlgorithmProperties algorithmProperties = new FillAlgorithmProperties( algorithmPropertiesString);

        int l = shapeProperties.getLeft();
        int t = shapeProperties.getTop();
        int r = shapeProperties.getRight();
        int b = shapeProperties.getBottom();
        Color c = algorithmProperties.getColor();

        int w = Math.abs( r - l);
        int h = Math.abs( b - t);
        Rectangle2D rect = new Rectangle2D.Float(l, t, w, h);

        process( rect, c, dicomObject);
    }

    public abstract void process(Rectangle2D rect, Color color, DicomObjectI dicomObjectI) throws MizerException;

    private Properties parseProperties( String s) throws IOException {
        Properties properties = new Properties();
        if( s != null) {
            String propertyString = s.replaceAll("\\s+", "").replaceAll(",", "\n");
            try( InputStream is = new ByteArrayInputStream( propertyString.getBytes())) {
                properties.load( is);
            }
        }
        return properties;
    }

    private class RectangleShapeProperties {
        private Map<String, Object> propertyMap;

        public RectangleShapeProperties(String s) throws Exception {
            Properties properties = parseProperties(s);
            propertyMap = new HashMap<>();

            for (String name : properties.stringPropertyNames()) {
                switch (name) {
                    case "left":
                    case "l":
                        String v = properties.getProperty(name);
                        if (v == null) throw new IllegalArgumentException("Missing rectangle shape parameter 'left'");
                        propertyMap.put("l", Integer.parseInt(v));
                        break;
                    case "right":
                    case "r":
                        v = properties.getProperty(name);
                        if (v == null) throw new IllegalArgumentException("Missing rectangle shape parameter 'right'");
                        propertyMap.put("r", Integer.parseInt(v));
                        break;
                    case "top":
                    case "t":
                        v = properties.getProperty( name);
                        if (v == null) throw new IllegalArgumentException("Missing rectangle shape parameter 'top'");
                        propertyMap.put("t", Integer.parseInt(v));
                        break;
                    case "bottom":
                    case "b":
                        v = properties.getProperty( name);
                        if (v == null) throw new IllegalArgumentException("Missing rectangle shape parameter 'bottom'");
                        propertyMap.put("b", Integer.parseInt(v));
                        break;
                }
            }
        }

        public int getLeft() { return (int) propertyMap.get("l");}
        public int getRight() { return (int) propertyMap.get("r");}
        public int getTop() { return (int) propertyMap.get("t");}
        public int getBottom() { return (int) propertyMap.get("b");}
    }

    private class FillAlgorithmProperties {
        private Map<String, Object> propertyMap;

        private FillAlgorithmProperties(String s) throws Exception {
            Properties properties = parseProperties(s);
            propertyMap = new HashMap<>();

            for (String name : properties.stringPropertyNames()) {
                switch (name) {
                    case "value":
                    case "v":
                        String v = properties.getProperty(name);
                        if( v == null) throw new IllegalArgumentException("Missing fill algorithm parameter 'value'");
                        propertyMap.put( "v", Integer.parseInt(v));
                        break;
                    case "red":
                    case "r":
                        v = properties.getProperty(name);
                        if( v == null) throw new IllegalArgumentException("Missing fill algorithm parameter 'red'");
                        propertyMap.put( "r", Integer.parseInt(v));
                        break;
                    case "green":
                    case "g":
                        v = properties.getProperty(name);
                        if( v == null) throw new IllegalArgumentException("Missing fill algorithm parameter 'green'");
                        propertyMap.put( "g", Integer.parseInt(v));
                        break;
                    case "blue":
                    case "b":
                        v = properties.getProperty(name);
                        if( v == null) throw new IllegalArgumentException("Missing fill algorithm parameter 'blue'");
                        propertyMap.put( "b", Integer.parseInt(v));
                        break;
                }
            }
        }

        public Color getColor() {
            if( propertyMap.containsKey( "v")) {
                int v = (int) propertyMap.get("v");
                return new Color(v,v,v);
            }
            else if( propertyMap.containsKey("r") && propertyMap.containsKey("g") && propertyMap.containsKey("b") ) {
                return new Color( (int)propertyMap.get("r"), (int)propertyMap.get("g"), (int)propertyMap.get("b"));
            }
            return null;
        }
    }
}
