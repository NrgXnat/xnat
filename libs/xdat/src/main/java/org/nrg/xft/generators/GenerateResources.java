/*
 * core: org.nrg.xft.generators.GenerateResources
 * XNAT http://www.xnat.org
 * Copyright (c) 2005-2017, Washington University School of Medicine and Howard Hughes Medical Institute
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.xft.generators;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xdat.XDAT;
import org.nrg.xft.XFT;
import org.nrg.xft.schema.Wrappers.GenericWrapper.GenericWrapperElement;
import org.nrg.xft.schema.XFTDataModel;
import org.nrg.xft.schema.XFTManager;
import org.springframework.core.io.FileSystemResource;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author Tim
 */
public class GenerateResources {
    private static final String PACKAGE = "org.nrg.xdat";
    private String schemaDir;
    private String javaDir;
    private String templatesDir;
    private String name;
    private String srcControlDir;
    private String beanDir;
    private String beanPropsDir;
    private String javascriptDir;

    @SuppressWarnings("unused")
    public GenerateResources(String name, String schemaDir, String javaDir, String templatesDir, String srcControlDir, String beanDir, String beanPropsDir) {
        this(name, schemaDir, javaDir, templatesDir, srcControlDir, beanDir, beanPropsDir, null);
    }

    public GenerateResources(String name, String schemaDir, String javaDir, String templatesDir, String srcControlDir, String beanDir, String beanPropsDir, String javascriptDir) {
        this.schemaDir = schemaDir;
        this.javaDir = javaDir;
        this.templatesDir = templatesDir;
        this.name = name;
        this.srcControlDir = srcControlDir;
        this.beanDir = beanDir;
        this.beanPropsDir = beanPropsDir;
        this.javascriptDir = javascriptDir;
    }

    public static void main(String[] args) {
        String name = null;
        String schema = null;
        String java = null;
        String templates = null;
        String srcControlDir = null;
        String beanDir = null;
        String beanPropsDir = null;
        String javascriptDir = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-name")) {
                if (i + 1 < args.length) {
                    name = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-schemaDir")) {
                if (i + 1 < args.length) {
                    schema = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-javaDir")) {
                if (i + 1 < args.length) {
                    java = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-templatesDir")) {
                if (i + 1 < args.length) {
                    templates = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-srcControlDir")) {
                if (i + 1 < args.length) {
                    srcControlDir = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-beanDir")) {
                if (i + 1 < args.length) {
                    beanDir = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-beanPropsDir")) {
                if (i + 1 < args.length) {
                    beanPropsDir = args[i + 1];
                }
            }

            if (args[i].equalsIgnoreCase("-javascriptDir")) {
                if (i + 1 < args.length) {
                    javascriptDir = args[i + 1];
                }
            }
        }

        if (name == null) {
            System.out.println("Missing required -name parameter");
            return;
        }

        if (schema == null) {
            System.out.println("Missing required -schemaDir parameter");
            return;
        }

        if (java == null) {
            System.out.println("Missing required -javaDir parameter");
            return;
        }

        if (templates == null) {
            System.out.println("Missing required -templatesDir parameter");
            return;
        }

        if (srcControlDir == null) {
            System.out.println("Missing required -srcControlDir parameter");
            return;
        }

        if (beanDir == null) {
            System.out.println("Missing required -beanDir parameter");
            return;
        }

        GenerateResources generator = new GenerateResources(name, schema, java, templates, srcControlDir, beanDir, beanPropsDir, javascriptDir);
        generator.process();
    }

    public void process() {
        try {
            if (!schemaDir.endsWith(File.separator)) {
                schemaDir += File.separator;
            }

            if (!javaDir.endsWith(File.separator)) {
                javaDir += File.separator;
            }

            if (!templatesDir.endsWith(File.separator)) {
                templatesDir += File.separator;
            }

            if (!srcControlDir.endsWith(File.separator)) {
                srcControlDir += File.separator;
            }

            if (!beanDir.endsWith(File.separator)) {
                beanDir += File.separator;
            }

            if (!beanPropsDir.endsWith(File.separator)) {
                beanPropsDir += File.separator;
            }

            final boolean hasJavascriptDir = StringUtils.isNotBlank(javascriptDir);
            if (hasJavascriptDir && !javascriptDir.endsWith(File.separator)) {
                javascriptDir += File.separator;
            }

            XFT.VERBOSE = true;

            XDAT.init(schemaDir, false, true);

            final JavaFileGenerator generator = new JavaFileGenerator();
            final JavaScriptGenerator jsGenerator = new JavaScriptGenerator();
            final JavaBeanGenerator beanGenerator = new JavaBeanGenerator();
            beanGenerator.setProject(PACKAGE);

            for (final XFTDataModel model : XFTManager.GetDataModels().values()) {
                //only build resources for schema that are on the file system
                if (model.getResource() instanceof FileSystemResource) {
                    final String modelDisplayPath = Paths.get(beanPropsDir, "schemas", model.getResource().getFile().getParentFile().getName()).toFile().getAbsolutePath();
                    final List<String> qualifiedElementNames = Lists.transform(model.getSchema().getSortedElementNames(), new PrefixTransform(model.getSchemaAbbr()));
                    for (final String qualifiedElementName : qualifiedElementNames) {
                        final GenericWrapperElement element = GenericWrapperElement.GetElement(qualifiedElementName);
                        if (element.getAddin().equalsIgnoreCase("")) {
                            generator.generateJavaFile(element, javaDir, srcControlDir);
                            if (!element.getProperName().equals(element.getFullXMLName())) {
                                generator.generateDisplayFile(element, modelDisplayPath);
                                generator.generateJavaReportFile(element, javaDir);
                                generator.generateVMReportFile(element, templatesDir);
                                generator.generateJavaEditFile(element, javaDir);
                                generator.generateVMEditFile(element, templatesDir);
                                generator.generateVMSearchFile(element, templatesDir);
                                if (hasJavascriptDir) {
                                    jsGenerator.generateJSFile(element, javascriptDir);
                                }
                            }
                        }
                    }
                    beanGenerator.generateJavaFiles(qualifiedElementNames, name + "-" + model.getSchemaAbbr(), beanDir, PACKAGE, beanPropsDir);
                }
            }

            System.out.println("File generation complete!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class PrefixTransform implements Function<String, String> {
        PrefixTransform(final String prefix) {
            _prefix = prefix;
        }

        @Nullable
        @Override
        public String apply(@Nullable final String elementName) {
            if (StringUtils.isBlank(elementName)) {
                return null;
            }
            return _prefix + ":" + elementName;
        }

        private final String _prefix;
    }
}
