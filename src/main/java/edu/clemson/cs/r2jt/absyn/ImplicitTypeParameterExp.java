/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.clemson.cs.r2jt.absyn;

import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.type.Type;
import java.util.Collections;
import java.util.Map;

/**
 * <p>An <code>ImplicitTypeParameterExp</p> is permitted only in two places:
 * 
 * <ul>
 *      <li>Inside the hierarchy of an {@link ArbitraryExpTy ArbitraryExpTy}
 *          when that ty appears in the context of the type of a formal 
 *          parameter to a mathematical definition.</li>
 *      <li>Inside a {@link TypeTheoremDec TypeTheoremDec}, either at the top
 *          level of the overall assertion, or at the top level of the 
 *          consequent of a top-level <em>implies</em> expression.</li>
 * </ul>
 * 
 * <p>In the first case, it defines a slot to be bound implicitly by some 
 * component of the type of the actual parameter.  Some examples:</p>
 * 
 * <pre>
 * Definition Foo(S : Str(T : Powerset(Z)) : T;
 * </pre>
 * 
 * <p>Here, the <code>T : Powerset(Z)</code> part is an implicit type parameter.
 * If one were to pass a <code>Str(N)</code> to <code>Foo</code>, then the 
 * return type would be <code>T</code>.  If one were to pass <code>Str(B)</code>
 * to <code>Foo</code> that would be a typechecking error, since <code>B</code>
 * is not in <code>Powerset(Z)</code>.</p>
 * 
 * <pre>
 * Definition Bar(L : Set(T : MType), E : Powerset(T)) : T;
 * </pre>
 * 
 * <p>Here, <code>T : MType</code> is an implicit type parameter, and the type
 * of both the second parameter and the return value are dependent on the 
 * binding of that parameter.  Implicit parameters are always bound left to 
 * right, so the first parameter's type may not depend on the second's.</p>
 * 
 * <p>In the second case, it asserts that any expression on the left side of the
 * colon is of the type on the right side, provided the antecedents of any
 * top-level implication are fulfilled.</p>
 */
public class ImplicitTypeParameterExp extends Exp {

    private final PosSymbol myName;
    private ArbitraryExpTy myTy;

    public ImplicitTypeParameterExp(PosSymbol name, ArbitraryExpTy ty) {
        myName = name;
        myTy = ty;
    }

    @Override
    public void accept(ResolveConceptualVisitor v) {
        v.visitImplicitTypeParameterExp(this);
    }

    @Override
    public Type accept(TypeResolutionVisitor v) throws TypeResolutionException {
        return v.getImplicitTypeParameterExp(this);
    }

    @Override
    public String asString(int indent, int increment) {
        return myName.getName() + " : "
                + myTy.asString(indent + increment, increment);
    }

    @Override
    public Location getLocation() {
        return myName.getLocation();
    }

    @Override
    public boolean containsVar(String varName, boolean IsOldExp) {
        return false;
    }

    @Override
    public List<Exp> getSubExpressions() {
        return new List<Exp>(Collections.singletonList(myTy.getArbitraryExp()));
    }

    @Override
    public void setSubExpression(int index, Exp e) {
        myTy = new ArbitraryExpTy(e);
    }

    @Override
    protected Exp substituteChildren(Map<Exp, Exp> substitutions) {
        return new ImplicitTypeParameterExp(myName, new ArbitraryExpTy(myTy
                .getArbitraryExp().substitute(substitutions)));
    }
}