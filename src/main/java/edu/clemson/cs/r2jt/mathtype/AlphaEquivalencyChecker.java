package edu.clemson.cs.r2jt.mathtype;

public class AlphaEquivalencyChecker extends SymmetricBoundVariableVisitor {

    public boolean beginMTNamed(MTNamed t1, MTNamed t2) {
        //TODO: This doesn't deal correctly with multiple appearances of a
        //variable

        MTType t1Value = getInnermostBinding1(t1.name);
        MTType t2Value = getInnermostBinding1(t2.name);

        SymmetricVisitor alphaEq = new AlphaEquivalencyChecker();

        alphaEq.visit(t1Value, t2Value);

        return true;
    }

    public boolean beginMTProper(MTProper t1, MTProper t2) {
        if (t1 != t2) {
            throw new IllegalArgumentException(
                    new TypeMismatchException(t1, t2));
        }

        return true;
    }

    public boolean beginMTSetRestriction(MTSetRestriction t1,
            MTSetRestriction t2) {

        //TODO:
        //We really need a way to check the expression embedded in each set
        //restriction for alpha-equivalency.  We don't have one, so for the
        //moment, we throw an exception
        throw new RuntimeException("Can't check set restrictions for "
                + "alpha equivalency.");
    }

    public boolean mismatch(MTType t1, MTType t2) {
        throw new IllegalArgumentException(new TypeMismatchException(t1, t2));
    }
}
