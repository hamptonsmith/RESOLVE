package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.data.PosSymbol;

public abstract class LineNumberedExp extends Exp {

    protected PosSymbol myLineNumber;

    public LineNumberedExp(PosSymbol lineNumber) {
        myLineNumber = lineNumber;
    }

    /** Returns the line number for this expression. */
    public PosSymbol getLineNum() {
        return myLineNumber;
    }

    /** Sets the line number for this expression. */
    public void setLineNum(PosSymbol lineNumber) {
        myLineNumber = lineNumber;
    }
}
