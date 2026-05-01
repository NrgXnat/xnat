/*
 * DicomEdit: DiscoverVariablesTreeVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.nrg.dicom.mizer.tags.Tag;
import org.nrg.dicom.mizer.tags.TagPath;
import org.nrg.dicom.mizer.tags.TagPublic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Walk the parse tree and discover all referenced variables.
 *
 * Establishes a list of all defined variables.
 * Establishes a list of all variables that are referred to in the script but whose value is not set by the script. These
 * values must be passed in from an external source.
 *
 */
public class DiscoverReferencedTagsTreeVisitor extends DE6ParserBaseVisitor<Void> {

    private Set<TagPath> tags = new HashSet<>();

    public Set<TagPath> getReferenedTags() {
        return tags;
    }


    @Override
    public Void visitPublic_tag(DE6Parser.Public_tagContext ctx) {
        String group = ctx.PUBLIC_GROUP().getText();
        group = group.substring(1);  // strip leading '('
        String element = ctx.PUBLIC_ELEMENT().getText();
        element = element.substring(0, element.length()-1);  // strip trailing ')'

        TagPath tp = new TagPath();
        tp.addTag( new TagPublic(group, element));
        tags.add( tp);
        return null;
    }

}
