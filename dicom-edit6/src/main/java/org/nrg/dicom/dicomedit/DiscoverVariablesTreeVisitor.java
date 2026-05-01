/*
 * DicomEdit: DiscoverVariablesTreeVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import com.google.common.collect.ImmutableMap;
import org.nrg.dicom.dicomedit.functions.FunctionManager;
import org.nrg.dicom.mizer.variables.BasicVariable;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Walk the parse tree and discover all referenced variables.
 *
 * Establishes a list of all defined variables.
 * Establishes a list of all variables that are referred to in the script but whose value is not set by the script.
 * These values must be passed in from an external source.
 */
public class DiscoverVariablesTreeVisitor extends DE6ParserBaseVisitor<Void> {
    public Map<String, Variable> getVariables() {
        return ImmutableMap.copyOf(_variables);
    }

    public Map<String, Variable> getExternalVariables() {
        return ImmutableMap.copyOf(_externalVariables);
    }

    @Override
    public Void visitInitialize(final DE6Parser.InitializeContext context) {
        logger.debug("Encountered initialization: " + context.getText());

        final String id = context.ID().getText();
        visit(context.getChild(2));

        logger.debug("Set variable: name= {}", id);
        _variables.put(id, new BasicVariable(id));
        return null;
    }

    @Override
    public Void visitIdValue(final DE6Parser.IdValueContext context) {
        logger.debug("Encountered idTerm.");
        final String id = context.variable().getText();
        if (!_variables.containsKey(id)) {
            final Variable variable = new BasicVariable(id);
            _externalVariables.put(id, variable);
            _variables.put(id, variable);
        }
        return null;
    }

    @Override public Void visitDescribeHiddenVariable(DE6Parser.DescribeHiddenVariableContext ctx) {
        logger.debug("Encountered hidden variable.");
        final String id = ctx.ID().toString();
        final Variable variable = initVariable( id);
        variable.setIsHidden(true);
        return null;
    }

    @Override public Void visitDescribeConstantVariable(DE6Parser.DescribeConstantVariableContext ctx) {
        logger.debug("Encountered constant variable.");
        final String id = ctx.ID().toString();
        final Variable variable = initVariable( id);
//        variable.setIsConstant(true);
        return null;
    }

    @Override public Void visitDescribeImmutableVariable(DE6Parser.DescribeImmutableVariableContext ctx) {
        logger.debug("Encountered immutable variable.");
        final String id = ctx.ID().toString();
        final Variable variable = initVariable( id);
//        variable.setIsImmutable(true);
        return null;
    }

    public Void visitFunction(DE6Parser.FunctionContext ctx) {
        logger.debug("Encountered function term: " + ctx.getText());
        String functionName = ctx.ID().getText();
        Set<String> elementNames = FunctionManager.getInstance().getInternalContext(functionName).keySet();
        elementNames.forEach(name -> _externalVariables.put(name, new BasicVariable(name)));
        return null;
    }

    protected Variable initVariable( String name) {
        final Variable variable;
        if (!_variables.containsKey( name)) {
            variable = new BasicVariable( name);
            _externalVariables.put( name, variable);
            _variables.put( name, variable);
        }
        else {
            variable = _variables.get(name);
        }
        return variable;
    }

    private static final Logger logger = LoggerFactory.getLogger(DiscoverVariablesTreeVisitor.class);

    private Map<String, Variable> _variables         = new HashMap<>();
    private Map<String, Variable> _externalVariables = new HashMap<>();
}
