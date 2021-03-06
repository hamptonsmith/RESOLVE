package edu.clemson.cs.r2jt.proving.absyn;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import edu.clemson.cs.r2jt.typeandpopulate.MTFunction;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;
import edu.clemson.cs.r2jt.proving.immutableadts.ImmutableList;
import edu.clemson.cs.r2jt.proving.immutableadts.SingletonImmutableList;

public class PLambda extends PExp {

    public final String variableName;
    private final PExp myBody;

    public PLambda(String variableName, MTType type, PExp body) {
        super(body.structureHash * 34, body.valueHash * 31
                + variableName.hashCode(), type, null);

        this.variableName = variableName;
        myBody = body;
    }

    public void accept(PExpVisitor v) {
        v.beginPExp(this);
        v.beginPLambda(this);

        v.beginChildren(this);
        v.endChildren(this);

        v.endPLambda(this);
        v.endPExp(this);
    }

    @Override
    public PLambda withTypeReplaced(MTType t) {
        return new PLambda(variableName, t, myBody);
    }

    @Override
    public PLambda withTypeValueReplaced(MTType t) {

        if (t != null) {
            throw new UnsupportedOperationException("Cannot set the type "
                    + "value on a " + this.getClass() + ".");
        }

        return this;
    }

    @Override
    public PLambda withSubExpressionReplaced(int i, PExp e) {
        if (i != 0) {
            throw new IndexOutOfBoundsException("" + i);
        }

        return new PLambda(variableName, myType, e);
    }

    @Override
    public ImmutableList<PExp> getSubExpressions() {
        return new SingletonImmutableList<PExp>(myBody);
    }

    @Override
    public PExpSubexpressionIterator getSubExpressionIterator() {
        return new PLambdaBodyIterator();
    }

    @Override
    public boolean isObviouslyTrue() {
        return myBody.isObviouslyTrue();
    }

    @Override
    protected void splitIntoConjuncts(List<PExp> accumulator) {
        accumulator.add(this);
    }

    @Override
    public PExp flipQuantifiers() {
        throw new UnsupportedOperationException("This method has not yet "
                + "been implemented.");
    }

    @Override
    protected void bindTo(PExp target, Map<PExp, PExp> accumulator)
            throws BindingException {

        //As a lambda expression, we can't be quantified and our body is
        //indivisible, so we only bind to identical things.
        if (!this.equals(target)) {
            throw BINDING_EXCEPTION;
        }
    }

    @Override
    public PExp substitute(Map<PExp, PExp> substitutions) {
        PExp retval;

        if (substitutions.containsKey(this)) {
            retval = substitutions.get(this);
        }
        else {
            retval = this;
        }

        return retval;
    }

    @Override
    public boolean containsName(String name) {
        return variableName.equals(name) || myBody.containsName(name);
    }

    @Override
    public Set<String> getSymbolNamesNoCache() {
        return myBody.getSymbolNames();
    }

    @Override
    public Set<PSymbol> getQuantifiedVariablesNoCache() {
        return myBody.getQuantifiedVariables();
    }

    @Override
    public List<PExp> getFunctionApplicationsNoCache() {
        return myBody.getFunctionApplications();
    }

    @Override
    public boolean containsExistential() {
        return myBody.containsExistential();
    }

    @Override
    public boolean isEquality() {
        return false;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    @Override
    public boolean isVariable() {
        return false;
    }

    private class PLambdaBodyIterator implements PExpSubexpressionIterator {

        private boolean myReturnedBodyFlag = false;

        @Override
        public boolean hasNext() {
            return !myReturnedBodyFlag;
        }

        @Override
        public PExp next() {
            if (myReturnedBodyFlag) {
                throw new NoSuchElementException();
            }

            return myBody;
        }

        @Override
        public PExp replaceLast(PExp newExpression) {
            return new PLambda(variableName, myType, myBody);
        }

    }
}
