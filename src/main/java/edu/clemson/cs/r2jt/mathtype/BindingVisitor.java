package edu.clemson.cs.r2jt.mathtype;

import java.util.HashMap;
import java.util.Map;

import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

/**
 * <p>Attempts to bind the concrete expression <code>t1</code> against the
 * template expression <code>t2</code>.</p>
 */
public class BindingVisitor extends SymmetricBoundVariableVisitor {

    private final TypeGraph myTypeGraph;
    private Map<String, MTType> myBindings = new HashMap<String, MTType>();

    private boolean myMatchSoFarFlag = true;

    public BindingVisitor(TypeGraph g) {
        myTypeGraph = g;
    }

    public BindingVisitor(TypeGraph g, FinalizedScope concreteContext) {
        super(concreteContext);
        myTypeGraph = g;
    }

    public BindingVisitor(TypeGraph g, Map<String, MTType> concreteContext) {
        super(concreteContext);
        myTypeGraph = g;
    }

    public boolean binds() {
        return myMatchSoFarFlag;
    }

    public Map<String, MTType> getBindings() {
        return myBindings;
    }

    /*@Override
    public boolean beginMTType(MTType t1, MTType t2) {
    	//Syntactic subtypes definitely bind.  No need to descend
    	//return !t1.isSyntacticSubtypeOf(t2);
    }*/

    @Override
    public boolean beginMTNamed(MTNamed t1, MTNamed t2) {
        MTType t1DeclaredType = getInnermostBinding1(t1.name);
        MTType t2DeclaredType = getInnermostBinding2(t2.name);

        //Fine if the declared type of t1 restricts the declared type of t2
        myMatchSoFarFlag &=
                myTypeGraph.isSubtype(t1DeclaredType, t2DeclaredType);

        //No need to keep searching if we've already found we don't bind
        return myMatchSoFarFlag;
    }

    @Override
    public boolean beginMTProper(MTProper t1, MTProper t2) {
        myMatchSoFarFlag &= t1.equals(t2);

        //No need to keep searching if we've already found we don't bind
        return myMatchSoFarFlag;
    }

    @Override
    public boolean mismatch(MTType t1, MTType t2) {

        //This is fine if t1 names a type of which t2 is a supertype
        if (t1 instanceof MTNamed) {
            String t1Name = ((MTNamed) t1).name;
            MTType t1DeclaredType = getInnermostBinding1(t1Name);
            myMatchSoFarFlag &= myTypeGraph.isSubtype(t1DeclaredType, t2);

            if (myMatchSoFarFlag) {
                myBindings.put(t1Name, t2);
            }
        }
        else if (t1 instanceof MTBigUnion) {
            //So long as the inner expression binds, this is ok
            myMatchSoFarFlag = visit(((MTBigUnion) t1).getExpression(), t2);
        }
        else if (t2 instanceof MTBigUnion) {
            //So long as the inner expression binds, this is ok
            myMatchSoFarFlag = visit(t1, ((MTBigUnion) t2).getExpression());
        }
        else {
            myMatchSoFarFlag = false;
        }

        //No need to keep searching if we've already found we don't bind
        return myMatchSoFarFlag;
    }
}