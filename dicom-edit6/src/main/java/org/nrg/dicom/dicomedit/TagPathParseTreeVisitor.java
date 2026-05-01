package org.nrg.dicom.dicomedit;

import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

public class TagPathParseTreeVisitor extends TagPathBaseVisitor<Integer> {
    private List<Integer> _tags = new ArrayList<>();

    public List<Integer> getTagPath(TagPathParser.TagpathContext ctx) {
        visitTagpath( ctx);
        return _tags;
    }

    @Override
    public Integer visitTagpath(TagPathParser.TagpathContext ctx) {
        _tags.clear();
        for( ParseTree pt: ctx.children) {
            if( pt instanceof TagPathParser.TagContext || pt instanceof TagPathParser.ItemnumberContext) {
                _tags.add(visit(pt));
            }
        }

        return 0;
    }

    @Override
    public Integer visitTag(TagPathParser.TagContext ctx) {
//        super.visitTag(ctx);
        String hexString = ctx.group().HEXWORD().toString() + ctx.element().HEXWORD().toString();
        return Integer.parseInt( hexString, 16);
    }

    @Override
    public Integer visitItemnumber(TagPathParser.ItemnumberContext ctx) {
//        super.visitItemnumber(ctx);
        String itemNumber = ctx.num().getText();
        return Integer.parseInt( itemNumber);
    }
}
