package edu.clemson.cs.r2jt.proving2;

import java.util.HashMap;
import java.util.Map;

import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.VarExp;
import edu.clemson.cs.r2jt.proving.absyn.BindingException;
import edu.clemson.cs.r2jt.proving.absyn.PExp;

public class Antecedent extends ImmutableConjuncts {

    public static final Antecedent EMPTY = new Antecedent();

    public Antecedent(Exp e) {
        super(e);
    }

    public Antecedent(PExp e) {
        super(e);
    }

    public Antecedent(Iterable<PExp> i) {
        super(i);
    }

    private Antecedent() {
        super();
    }

    @Override
    public Antecedent substitute(Map<PExp, PExp> mapping) {
        ImmutableConjuncts genericRetval = super.substitute(mapping);
        return new Antecedent(genericRetval);
    }

    @Override
    public Antecedent removed(int indexToRemove) {
        ImmutableConjuncts genericRetval = super.removed(indexToRemove);
        return new Antecedent(genericRetval);
    }

    public Antecedent subConjuncts(int start, int length) {
        ImmutableConjuncts genericRetval = super.subConjuncts(start, length);
        return new Antecedent(genericRetval);
    }

    @Override
    public Antecedent appended(Iterable<PExp> i) {
        ImmutableConjuncts genericRetval = super.appended(i);
        return new Antecedent(genericRetval);
    }

    @Override
    public Antecedent eliminateObviousConjuncts() {
        ImmutableConjuncts genericRetval = super.eliminateObviousConjuncts();
        return new Antecedent(genericRetval);
    }

    @Override
    public Antecedent eliminateRedundantConjuncts() {
        ImmutableConjuncts genericRetval = super.eliminateRedundantConjuncts();
        return new Antecedent(genericRetval);
    }

    /**
     * <p>Transforms an <code>Antecedent</code> into a <code>Consequent</code>
     * by flipping its universally and existentially quantified expressions.</p>
     * 
     * @return The <code>Antecedent</code> with universally and existentially
     *         quantified expressions reversed.
     */
    public Consequent instantiate() {
        return new Consequent(this.flipQuantifiers());
    }

    public static void flipQuantifiers(Exp e) {
        if (e instanceof VarExp) {
            VarExp eAsVarExp = (VarExp) e;
            switch (eAsVarExp.getQuantification()) {
            case VarExp.EXISTS:
                eAsVarExp.setQuantification(VarExp.FORALL);
                break;
            case VarExp.FORALL:
                eAsVarExp.setQuantification(VarExp.EXISTS);
                break;
            }
        }
        else {
            for (Exp subexp : e.getSubExpressions()) {
                flipQuantifiers(subexp);
            }
        }
    }

    public Antecedent apply(Antecedent a, Consequent c) {

        return satisfy(this, a, new HashMap<PExp, PExp>(), c);
    }

    /**
     * <p>Attempts to find bindings for the universally quantified variables in
     * the antecedent of an implication against a concrete set of assumptions,
     * given a set of assumed bindings.  The set of antecedents to bind start
     * at index <code>curAntecedentIndex</code> in <code>antecedents</code> and
     * continue until the end.  <code>assumptions</code> is the set of 
     * assumptions to bind against.  <code>bindings</code> is a set of assumed
     * bindings.</p>
     * 
     * <p>For each binding found, the <code>Exp</code>s in 
     * <code>consequent</code> are added to <code>accumulator</code> with the
     * bindings applied.</p>
     * 
     * <p>As an example, given the implication:</p>
     * 
     * <p><code>For all i, j, k : Z, i &gt; 0 and i + j &lt;= k --&gt; 
     * 		j &lt; k</code></p>
     * 
     * <p>And the concrete set of assumptions:</p>
     * 
     * <ul>
     * <li>a &gt; 0</li>
     * <li>b &gt; 0</li>
     * <li>a + b &lt;= c</li>
     * </ul>
     * 
     * <p>The only possible binding is:</p>
     * 
     * <ul>
     * <li>i --&gt; a</li>
     * <li>j --&gt; b</li>
     * <li>k --&gt; c</li>
     * </ul>
     * 
     * <p>And the final value of <code>accumulator</code> will be:</p>
     * 
     * <ul>
     * <li>b < c</li>
     * </ul>
     * 
     * <p>If there is no possible binding, nothing is added to the 
     * accumulator.</p>
     * 
     * @param assumptions A concrete set of assumptions against which to match.
     * @param antecedents A list of universally quantified antecedents to match
     *                    against the assumptions.
     * @param curAntecedentIndex The index of the first antecedent not already
     *                           matched and reflected in the set of assumed
     *                           bindings.
     * @param bindings A set of assumed bindings, reflected match choices for
     *                 antecedents before <code>curAntecedentIndex</code>.
     * @param typer A <code>MathExpTypeResolve</code> to aid in matching types.
     * @param consequent The set of consequents in the universally quantified
     *                   implication, in which we would like to make 
     *                   replacements based on our binding.
     * @param accumulator A list to hold the result of our matching.
     */
    public static Antecedent satisfy(ImmutableConjuncts assumptions,
            ImmutableConjuncts antecedents, Map<PExp, PExp> bindings,
            Consequent consequent) {

        Antecedent retval;

        if (antecedents.size() == 0) {
            retval = consequent.substitute(bindings).assumed();
        }
        else {
            Map<PExp, PExp> subBinding = null;

            PExp curAntecedent = antecedents.get(0).substitute(bindings);

            retval = EMPTY;

            ImmutableConjuncts subAntecedentList = null;

            for (PExp assumption : assumptions) {

                try {
                    subBinding = curAntecedent.bindTo(assumption);

                    if (subAntecedentList == null) {
                        subAntecedentList =
                                antecedents.subConjuncts(1, antecedents.size());
                    }

                    Antecedent subAntecedent =
                            satisfy(assumptions, subAntecedentList, subBinding,
                                    consequent);

                    retval = retval.appended(subAntecedent);
                }
                catch (BindingException e) {

                }
            }
        }

        return retval;
    }
}
