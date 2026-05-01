/*
 * DicomEdit: DicomEditParseTreeVisitor
 * XNAT http://www.xnat.org
 * Copyright (c) 2017, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 */

package org.nrg.dicom.dicomedit;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.nrg.dicom.dicomedit.functions.FunctionManager;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationException;
import org.nrg.dicom.mizer.exceptions.ScriptEvaluationRuntimeException;
import org.nrg.dicom.mizer.objects.DicomObjectI;
import org.nrg.dicom.mizer.tags.*;
import org.nrg.dicom.mizer.values.ConstantValue;
import org.nrg.dicom.mizer.values.IntegerValue;
import org.nrg.dicom.mizer.values.Value;
import org.nrg.dicom.mizer.variables.BasicVariable;
import org.nrg.dicom.mizer.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.nrg.dicom.mizer.values.AbstractMizerValue.VOID;

/**
 * Implementation of a DE6 grammar's parse-tree visitor, where in we add the semantics to the syntax.
 */
public class DicomEditParseTreeVisitor extends DE6ParserBaseVisitor<Value> {

    private Map<String, Variable> variables = new HashMap<String, Variable>();
    private DicomObjectI dicomObject;

    private static final Logger logger = LoggerFactory.getLogger(DicomEditParseTreeVisitor.class);

    public DicomEditParseTreeVisitor(DE6Script... scripts) {
        for (DE6Script script : scripts) {
            variables.putAll(script.getVariables());
        }
    }

    public void setDicomObject(DicomObjectI dicomObject) {
        this.dicomObject = dicomObject;
    }

    public Map<String, Variable> getVariables() {
        return variables;
    }

    public DicomObjectI getDicomObject() {
        return dicomObject;
    }

    public void setVariable(final String name, final Value value) {
        final Variable variable;
        if (getVariables().containsKey(name)) {
            variable = getVariable(name);
        } else {
            variable = new BasicVariable(name);
        }
        variable.setValue(value);
        variables.put(name, variable);
    }

    public Variable getVariable(String name) {
        return variables.get(name);
    }

    @Override
    public Value visitScript(DE6Parser.ScriptContext ctx) {
        logger.debug("Encountered script.");
        for (int i = 0; i < ctx.statement().size(); i++) {
            logger.debug("visit statement " + i + ": " + ctx.statement(i).getText());
            this.visit(ctx.statement(i));
        }
        return new ConstantValue(null);
    }

    @Override
    public Value visitStatement(DE6Parser.StatementContext ctx) {
        this.visit(ctx.getChild(0));
        return new ConstantValue(null);
    }

    @Override
    public Value visitVersion(DE6Parser.VersionContext ctx) {
        String versionString = ctx.STRING().toString();
        // remove the enclosing quotes on the string
        versionString = versionString.replaceAll("\"", "");
        VersionManager vm = VersionManager.getInstance();
        if (!vm.isKnownVersion(versionString)) {
            String msg = MessageFormat.format("Unsupported version: {0}. Supported versions are {1}", versionString, vm.getSupportedVersionStrings());
            logger.error(msg);
            throw new ScriptEvaluationRuntimeException(msg);
        }
        return new ConstantValue(versionString);
    }

    @Override
    public Value visitInitialize(DE6Parser.InitializeContext ctx) {
        logger.debug("Encountered initialization: " + ctx.getText());
        String id = ctx.ID().getText();
        Value value = visit(ctx.getChild(2));
        logger.debug("Set variable: name= {}, value= {}", id, value);
        variables.put(id, new BasicVariable(id, value));
        return value;
    }

    @Override
    public Value visitIf_elseif_else(DE6Parser.If_elseif_elseContext ctx) {
        int i = 0;
        for (DE6Parser.ConditionContext condition : ctx.condition()) {
            if (visit(condition).asBoolean()) {
                visitBlock(ctx.block(i));
                return VOID;
            }
            i++;
        }
        if (ctx.block().size() > ctx.condition().size()) {
            final DE6Parser.BlockContext elseBlock = ctx.block(i);
            visitBlock(elseBlock);
        }
        return VOID;
    }

    @Override
    public Value visitDescribeNamedVariable(DE6Parser.DescribeNamedVariableContext ctx) {
        String name = ctx.ID().getText();
        Variable variable = getVariable(name);
        if (variable == null) {
            variable = new BasicVariable(name);
            variables.put(name, variable);
        }
        String description = extractString(ctx.STRING().toString());
        variable.setDescription(description);
        return new ConstantValue(description);
    }

    @Override
    public Value visitDescribeHiddenVariable(DE6Parser.DescribeHiddenVariableContext ctx) {
        String name = ctx.ID().getText();
        Variable variable = getVariable(name);
        if (variable == null) {
            variable = new BasicVariable(name);
            variables.put(name, variable);
        }
        final String hiddenToken = extractString(ctx.HIDDEN_TOKEN().toString());
        variable.setIsHidden(true);
        return new ConstantValue(hiddenToken);
    }

    @Override
    public Value visitExport_stmt(DE6Parser.Export_stmtContext ctx) {
        String name = ctx.ID().getText();
        Variable variable = getVariable(name);
        if (variable == null) {
            variable = new BasicVariable(name);
            variables.put(name, variable);
        }
        String exportString = extractString(ctx.STRING().toString());
        variable.setExportField(exportString);
        return new ConstantValue(exportString);
    }

    @Override
    public Value visitAssign(DE6Parser.AssignContext ctx) {
        logger.debug("Encountered assignment: " + ctx.getText());
        Value assignedValue = visit(ctx.value());
        if (VOID.equals(assignedValue)) {
            throw new IllegalArgumentException("Assigned value is VOID.");
        }
        String assignedString = assignedValue.asString();

        Value lvalue = visit(ctx.lvalue());
        if (VOID.equals(lvalue)) {
            throw new IllegalArgumentException("Value assigned to can not be VOID.");
        }
        TagPath tp = (TagPath) lvalue.asObject();

        logger.debug("Assigned: " + tp + " = " + assignedString);
        dicomObject.assign(tp, assignedString);

        return assignedValue;
    }

    @Override
    public Value visitAssign_if_exists(DE6Parser.Assign_if_existsContext ctx) {
        logger.debug("Encountered assign_if_exists: " + ctx.getText());
        Value assignedValue = visit(ctx.value());
        if (VOID.equals(assignedValue)) {
            throw new IllegalArgumentException("Assigned value is VOID.");
        }

        Value lvalue = visit(ctx.lvalue());
        if (VOID.equals(lvalue)) {
            throw new IllegalArgumentException("Value assigned to can not be VOID.");
        }
        TagPath tp = (TagPath) lvalue.asObject();

        dicomObject.assignIfExists(tp, assignedValue);

        return assignedValue;
    }

    @Override
    public Value visitTag(DE6Parser.TagContext ctx) {
        return visit(ctx.getChild(0));
    }

    @Override
    public Value visitTagpath(DE6Parser.TagpathContext ctx) {
        TagPath tagPath = new TagPath();
        List<DE6Parser.Seq_elementContext> seq_elementContexts = ctx.seq_element();
        for (DE6Parser.Seq_elementContext sctx : seq_elementContexts) {
            Value v = visit(sctx);
            tagPath.addTag((Tag) v.asObject());
        }
        if (ctx.element() != null) {
            Value v = visit(ctx.element());
            tagPath.addTag((Tag) v.asObject());
        } else {
            logger.debug("TagPath must include an element");
        }
        return new ConstantValue(tagPath);
    }

    @Override
    public Value visitSeq_element(DE6Parser.Seq_elementContext ctx) {
        Tag tag;
        if (ctx.element() != null) {
            Tag t = (Tag) visit(ctx.element()).asObject();
            String itemNumberString = null;
            if (ctx.itemnumber() != null) {
                itemNumberString = visit(ctx.itemnumber()).toString();
            }
            tag = new TagSequence(t, itemNumberString);
        } else {
            tag = (Tag) visit(ctx.seq_wildcard()).asObject();
        }
        return new ConstantValue(tag);
    }

    @Override
    public Value visitSeq_wildcard(DE6Parser.Seq_wildcardContext ctx) {
        String s = ctx.getText();
        return new ConstantValue(new TagSequenceWildcard(s));
    }

    @Override
    public Value visitItemnumber(DE6Parser.ItemnumberContext ctx) {
        Value value;
        if (ctx.INTEGER() != null) {
            value = new IntegerValue(ctx.INTEGER().getText());
        } else if (ctx.ITEM_WILDCARD() != null) {
            value = new ConstantValue(ctx.ITEM_WILDCARD().getText());
        } else {
            value = new ConstantValue();
        }
        return value;
    }

    private boolean isLValueContext(ParserRuleContext ctx) {
        boolean b = false;
        do {
            if (ctx instanceof DE6Parser.LvalueContext) {
                b = true;
                break;
            }
        } while ((ctx = ctx.getParent()) != null);
        return b;
    }

    @Override
    public Value visitPublic_tag(DE6Parser.Public_tagContext ctx) {
        String group = ctx.PUBLIC_GROUP().getText();
        group = group.substring(1);  // strip leading '('
        String element = ctx.PUBLIC_ELEMENT().getText();
        element = element.substring(0, element.length() - 1);  // strip trailing ')'

        TagPublic tag = new TagPublic(group, element);
        return new ConstantValue(tag);
    }

    /**
     * This hard codes some of the syntax which ain't a good thing.
     * TODO: figure out how to not need the grouping characters in the parser or how to grab those characters by
     * reference.
     *
     * @param ctx The context containing text.
     * @return The value for the string terminator.
     */
    @Override
    public Value visitPvt_tag(DE6Parser.Pvt_tagContext ctx) {
        String group = ctx.PVT_GROUP().getText();
        group = group.substring(1);  // strip leading '('
        String element = ctx.PVT_ELEMENT().getText();
        element = element.substring(0, element.length() - 1);  // strip trailing ')'
        String pvtCreatorID = element.substring(1, element.indexOf("}"));
        String subindex = element.substring(element.indexOf("}") + 1);

        TagPrivate tag = new TagPrivate(group, pvtCreatorID, subindex);
        return new ConstantValue(tag);
    }

    @Override
    public Value visitFunction(DE6Parser.FunctionContext ctx) {
        Value value;
        String functionName = null;
        try {
            logger.debug("Encountered function term: " + ctx.getText());
            functionName = ctx.ID().getText();
            DE6Parser.ArgumentlistContext c = ctx.argumentlist();
            Value termListValue = (c == null) ? new ConstantValue(new ArrayList<Value>()) : visit(ctx.argumentlist());
            List<Value> args = termListValue.asValueList();
            value = evaluateFunction(functionName, args, variables, dicomObject);
            logger.debug("Function " + functionName + " = " + value.asString());
            return value;
        } catch (ScriptEvaluationException e) {
            String msg = "Error evaluating " + ("null".equals(functionName) ? "unknown" : functionName);
            logger.debug(msg);
            logger.debug(e.getMessage());
            throw new ScriptEvaluationRuntimeException(msg, e);
        }
    }

    @Override
    public Value visitValuelist(DE6Parser.ValuelistContext ctx) {
        logger.debug("Encountered term list.");
        List<Value> values = new ArrayList<Value>();
        ctx.value().forEach(valueContext -> values.add(visit(valueContext)));
        return new ConstantValue(values);
    }

    @Override
    public Value visitArgumentlist(DE6Parser.ArgumentlistContext ctx) {
        logger.debug("Encountered term list.");
        List<Value> values = new ArrayList<Value>();
        ctx.argument().forEach(argumentContext -> values.add(visit(argumentContext)));
        return new ConstantValue(values);
    }

    @Override
    public Value visitVariable(DE6Parser.VariableContext ctx) {
        return getVariable(ctx.ID().getText()).getValue();
    }

    /**
     * Return the value of the string.
     * <p>
     * Having problems when the string is a regex?  Look here first!
     *
     * @param ctx The context containing text.
     * @return The value for the string terminator.
     */
    @Override
    public Value visitStringValue(DE6Parser.StringValueContext ctx) {
        String str = ctx.getText();
        return new ConstantValue(extractStringValue(str));
    }

    /**
     * A String is just its value in a function argument.
     * (see visitStringValue)
     *
     * @param ctx the parse tree
     * @return Value of string.
     */
    @Override
    public Value visitStringArgument(DE6Parser.StringArgumentContext ctx) {
        String str = ctx.getText();
        return new ConstantValue(extractStringValue(str));
    }

    protected String extractStringValue(String str) {
        // strip quotes
        str = str.substring(1, str.length() - 1).replace("\"\"", "\"");
        // Ugh, java strings with backslashes are confusing.
        // User has to write the string with backslashes (likely in regexs) escaped but antlr seems to provide the
        // string with both the escaping and the escaped \.  Remove the escaping \
        str = str.replace("\\\\", "\\");
        return str;
    }

    @Override
    public Value visitIdValue(DE6Parser.IdValueContext ctx) {
        logger.trace("Encountered idTerm.");
        String id = ctx.variable().getText();
        return (extractVariableValue(id));
    }

    protected Value extractVariableValue(String id) {
        if (variables.containsKey(id)) {
            return variables.get(id).getValue();
        } else {
            throw new ParseCancellationException("Unknown variable: " + id);
        }
    }

    @Override
    public Value visitNumberValue(DE6Parser.NumberValueContext ctx) {
        return new ConstantValue(ctx.getText());
    }

    @Override
    public Value visitNumberArgument(DE6Parser.NumberArgumentContext ctx) {
        return new ConstantValue(ctx.getText());
    }

    @Override
    public Value visitString(DE6Parser.StringContext ctx) {
        // remove beginning and ending quotes.
        return new ConstantValue(ctx.getText().replaceAll("^\"|\"$", ""));
    }

    /**
     * Return the value at the tagpath, not the tagpath itself, when a tagpath is a value.
     * (see visitTagPathArgument).
     *
     * @param ctx the parse tree
     * @return
     */
    @Override
    public Value visitTagpathValue(DE6Parser.TagpathValueContext ctx) {
        Value vtp = visit(ctx.tagpath());
        TagPath tp = (TagPath) vtp.asObject();
        if (tp.isSingular()) {
            return new ConstantValue(dicomObject.getString(tp));
        } else {
            String msg = "Path matching multiple tags not allowed as term: " + tp;
            logger.warn(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Return the tagpath, not the value at the tagpath, for tagpaths in function arguments.
     * (see visitTagpathArgument)
     *
     * @param ctx the parse tree
     * @return
     */
    @Override
    public Value visitTagpathArgument(DE6Parser.TagpathArgumentContext ctx) {
        return visit(ctx.tagpath());
    }

    @Override
    public Value visitList(DE6Parser.ListContext ctx) {
        logger.debug("Encountered list.");
        final List<Value> values = ctx.list_entry().stream()
                .map(le -> visit(le.list_element()))
                .collect(Collectors.toList());
        return new ConstantValue(values);
    }

    private Value evaluateFunction(String functionName, List<Value> args, Map<String, Variable> variables, DicomObjectI dicomObject) throws ScriptEvaluationException {
        FunctionManager functionManager = FunctionManager.getInstance();
        return functionManager.execute(functionName, args, variables, dicomObject);
    }

    @Override
    public Value visitDelete(DE6Parser.DeleteContext ctx) {
        logger.debug("Encountered deletion: " + ctx.getText());
        Value lvalue = visit(ctx.lvalue());
        TagPath tp = (TagPath) lvalue.asObject();

        dicomObject.delete(tp);

        return VOID;
    }

    @Override
    public Value visitRemoveAllPrivateTags(DE6Parser.RemoveAllPrivateTagsContext ctx) {
        dicomObject.deleteAllPrivateTags();
        return VOID;
    }

    @Override
    public Value visitConditional_statement(DE6Parser.Conditional_statementContext ctx) {
        Value condition = visit(ctx.condition());
        Value v = VOID;
        if (condition.asBoolean()) {
            v = visit(ctx.action(0));
        } else {
            if (ctx.action().size() == 2) {
                v = visit(ctx.action(1));
            }
        }
        return v;
    }

    @Override
    public Value visitEcho(DE6Parser.EchoContext ctx) {
        logger.debug("Encountered echo: " + ctx.getText());
        Value value = visit(ctx.value());
        if (VOID.equals(value)) {
            throw new IllegalArgumentException("Assigned value is VOID.");
        }
        String valueString = (VOID.equals(value) ? "" : value.asString());
        logger.trace("{}", valueString);
        System.out.println(valueString);

        return value;
    }

    /**
     * Return result of condition evaluation.
     * A value of 'null' will not match anything.
     *
     * @param ctx
     * @return boolean result of condition evaluation.
     */
    @Override
    public Value visitBooleanExpression(DE6Parser.BooleanExpressionContext ctx) {
        String op = ctx.conditionOperator().getText();
        String s1 = visit(ctx.value(0)).asString();
        String s2 = visit(ctx.value(1)).asString();
        final boolean equals;
        switch (op) {
            case "=":
                equals = s1 != null && s1.equals(s2);
                break;
            case "!=":
                equals = s1 == null || !s1.equals(s2);
                break;
            case "~":
                equals = s1 != null && s1.matches(s2);
                break;
            case "!~":
                equals = s1 == null || !s1.matches(s2);
                break;
            default:
                equals = false;
                logger.warn("Unknown conditional operator: " + op);
        }
        return new ConstantValue(equals);
    }

    @Override
    public Value visitBooleanFunction(DE6Parser.BooleanFunctionContext ctx) {
        Value value = visit(ctx.function());
        return value.isBoolean() ? value : new ConstantValue(value.asBoolean());
    }

    /**
     * Remove surrounding quotes.
     * <p>
     * TODO: remove internal escaped quotes.
     *
     * @param s
     * @return
     */
    private static String extractString(String s) {
        final StringBuilder sb = new StringBuilder(s);

        sb.deleteCharAt(0); // remove leading and trailing quotes
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

}
