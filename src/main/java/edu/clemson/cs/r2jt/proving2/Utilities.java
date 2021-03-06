package edu.clemson.cs.r2jt.proving2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.clemson.cs.r2jt.absyn.BetweenExp;
import edu.clemson.cs.r2jt.absyn.DotExp;
import edu.clemson.cs.r2jt.absyn.EqualsExp;
import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.FunctionExp;
import edu.clemson.cs.r2jt.absyn.InfixExp;
import edu.clemson.cs.r2jt.absyn.IntegerExp;
import edu.clemson.cs.r2jt.absyn.MathVarDec;
import edu.clemson.cs.r2jt.absyn.OutfixExp;
import edu.clemson.cs.r2jt.absyn.PrefixExp;
import edu.clemson.cs.r2jt.absyn.QuantExp;
import edu.clemson.cs.r2jt.absyn.VarExp;
import edu.clemson.cs.r2jt.analysis.TypeResolutionException;
import edu.clemson.cs.r2jt.collections.List;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.proving.Conjuncts;
import edu.clemson.cs.r2jt.proving.VerificationCondition;
import edu.clemson.cs.r2jt.proving.absyn.PExp;
import edu.clemson.cs.r2jt.type.Type;

/**
 * <p>A variety of useful general-purpose methods.</p>
 * 
 * @author H. Smith
 */
public class Utilities {

    private static boolean genericBindWarningPrinted = false;
    private static boolean myBindDebugFlag = false;

    public static void setBindDebugFlag(boolean f) {
        myBindDebugFlag = f;
    }

    public static String conjunctListToString(java.util.List<PExp> l) {
        StringBuilder b = new StringBuilder();

        boolean first = true;
        for (PExp p : l) {
            if (first) {
                first = false;
            }
            else {
                b.append(" and ");
            }

            b.append(p);
        }

        return "" + b;
    }

    public static VC convertToImmutableVC(VerificationCondition vc) {

        java.util.List<PExp> newAntecedents = new LinkedList<PExp>();

        Conjuncts oldAntecedents = vc.getAntecedents();
        for (Exp a : oldAntecedents) {
            newAntecedents.add(PExp.buildPExp(a));
        }

        java.util.List<PExp> newConsequents = new LinkedList<PExp>();

        Conjuncts oldConsequents = vc.getConsequents();
        for (Exp c : oldConsequents) {
            newConsequents.add(PExp.buildPExp(c));
        }

        VC retval =
                new VC(vc.getName(), new Antecedent(newAntecedents),
                        new Consequent(newConsequents));

        return retval;
    }

    public static void buildExpMapFromPosSymbolMap(
            Map<PosSymbol, Exp> original, Map<Exp, Exp> newMap) {
        Set<Map.Entry<PosSymbol, Exp>> entrySet = original.entrySet();
        for (Map.Entry<PosSymbol, Exp> binding : entrySet) {
            newMap.put(new VarExp(null, null, binding.getKey()), binding
                    .getValue());
        }
    }

    public static Map<Exp, Exp> shallowCopyMap(Map<Exp, Exp> original) {
        Map<Exp, Exp> newMap = new HashMap<Exp, Exp>();

        Set<Map.Entry<Exp, Exp>> entrySet = original.entrySet();
        for (Map.Entry<Exp, Exp> binding : entrySet) {
            newMap.put(binding.getKey(), binding.getValue());
        }

        return newMap;
    }

    /**
     * <p>Attempts to find a binding for the universally quantified variables in
     * the antecedent of an implication against a concrete set of assumptions,
     * given a set of assumed bindings.  The set of antecedents to bind start
     * at index <code>curAntecedentIndex</code> in <code>antecedents</code> and
     * continue until the end.  <code>assumptions</code> is the set of 
     * assumptions to bind against.  <code>bindings</code> is a set of assumed
     * bindings.</p>
     * 
     * <p>If a binding is found, the <code>Exp</code>s in 
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
     * <p>If there is no such binding, nothing is added to the accumulator.</p>
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
    public static void satisfy(Iterable<Exp> assumptions,
            List<Exp> antecedents, int curAntecedentIndex,
            Map<Exp, Exp> bindings, List<Exp> consequent, List<Exp> accumulator) {

        if (curAntecedentIndex >= antecedents.size()) {
            for (Exp c : consequent) {
                accumulator.add(c.substitute(bindings));
            }
        }
        else {
            Map<PosSymbol, Exp> subBinding = null;
            Iterator<Exp> assumptionsIter = assumptions.iterator();
            Exp assumption;

            Exp curAntecedent = antecedents.get(curAntecedentIndex);

            Type beforeType = curAntecedent.getType();
            Exp antecedent = curAntecedent.substitute(bindings);
            Type afterType = antecedent.getType();

            if (beforeType != null && afterType == null) {
                throw new UnsupportedOperationException("Substitution failed "
                        + "to set Type for "
                        + antecedents.get(curAntecedentIndex).getClass() + ". "
                        + "Be certain it's copy() and substituteChildren() "
                        + "methods set a type on the returned value. \n\n"
                        + "Before: " + antecedents.get(curAntecedentIndex)
                        + "  (" + beforeType + ")\nAfter: " + antecedent
                        + "  (" + afterType + ")");
            }

            while (assumptionsIter.hasNext()) {
                assumption = assumptionsIter.next();

                subBinding = bind(antecedent, assumption);

                if (subBinding != null) {
                    Map<Exp, Exp> workingBindings = shallowCopyMap(bindings);
                    buildExpMapFromPosSymbolMap(subBinding, workingBindings);
                    satisfy(assumptions, antecedents, curAntecedentIndex + 1,
                            workingBindings, consequent, accumulator);
                }
            }
        }
    }

    public static List<Exp> splitIntoConjuncts(List<Exp> c) {
        List<Exp> retval = new List<Exp>();
        for (Exp e : c) {
            splitIntoConjuncts(e, retval);
        }

        return retval;
    }

    public static List<Exp> splitIntoConjuncts(Exp e) {
        List<Exp> accumulator = new List<Exp>();
        splitIntoConjuncts(e, accumulator);
        return accumulator;
    }

    public static void splitIntoConjuncts(Exp e, List<Exp> accumulator) {
        if (e instanceof InfixExp && Utilities.isAndExp((InfixExp) e)) {
            InfixExp eAsInfix = (InfixExp) e;
            splitIntoConjuncts(eAsInfix.getLeft(), accumulator);
            splitIntoConjuncts(eAsInfix.getRight(), accumulator);
        }
        else if (e instanceof BetweenExp) {
            BetweenExp eAsBetween = (BetweenExp) e;
            List<Exp> subexpressions = eAsBetween.getLessExps();

            for (Exp sub : subexpressions) {
                splitIntoConjuncts(sub, accumulator);
            }
        }
        else {
            accumulator.add(e);
        }
    }

    public static List<Exp> splitIntoDisjuncts(Exp e) {
        List<Exp> accumulator = new List<Exp>();
        splitIntoDisjuncts(e, accumulator);
        return accumulator;
    }

    private static void splitIntoDisjuncts(Exp e, List<Exp> disjuncts) {
        if (e instanceof InfixExp && Utilities.isOrExp((InfixExp) e)) {
            InfixExp eAsInfix = (InfixExp) e;
            splitIntoDisjuncts(eAsInfix.getLeft(), disjuncts);
            splitIntoDisjuncts(eAsInfix.getRight(), disjuncts);
        }
        else {
            disjuncts.add(e);
        }
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> the given expression is
     * an equality with both sides precisely equal.</p>
     * 
     * @param expression The expression to test.
     * @return <code>true</code> <strong>iff</strong> <code>e</code> is an
     *         equality with both sides precisely equal.
     */
    public static boolean isSymmetricEquality(Exp expression) {
        boolean retval = false;

        if (expression instanceof EqualsExp) {
            EqualsExp expressionAsEquals = (EqualsExp) expression;

            if (expressionAsEquals.getOperator() == EqualsExp.EQUAL) {
                retval =
                        expressionAsEquals.getLeft().equivalent(
                                expressionAsEquals.getRight());
            }
        }

        return retval;
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> the provided
     * <code>Exp</code> repesents the Resolve boolean value "true".</p>
     * 
     * @param e The <code>Exp</code> to check.
     * @return <code>true</code> <strong>iff</strong> <code>e</code> represents
     *         the boolean value "true".
     */
    public static boolean isLiteralTrue(Exp e) {
        return (e instanceof VarExp && ((VarExp) e).getName().getName()
                .equalsIgnoreCase("true"));
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> the provided
     * <code>InfixExp</code> represents Resolve boolean function "and".</p>
     * 
     * @param e The <code>InfixExp</code> to check.
     * @return <code>true</code> <strong>iff</strong> <code>e</code> represents
     *         boolean "and".
     */
    public static boolean isAndExp(InfixExp e) {
        return ((InfixExp) e).getOpName().getName().equals("and");
    }

    /**
     * <p>Returns <code>true</code> <strong>iff</strong> the provided
     * <code>InfixExp</code> represents Resolve boolean function "and".</p>
     * 
     * @param e The <code>InfixExp</code> to check.
     * @return <code>true</code> <strong>iff</strong> <code>e</code> represents
     *         boolean "and".
     */
    public static boolean isOrExp(InfixExp e) {
        return ((InfixExp) e).getOpName().getName().equals("or");
    }

    /**
     * <p>Attempts to bind <code>pattern</code> to a concrete expression,
     * <code>e</code>, returning a mapping from the quantified symbols in 
     * <code>pattern</code> to the concrete subexpressions they were bound to.  
     * If a binding is not possible, returns <code>null</code>.</p>
     * 
     * <p>Neither <code>pattern</code> nor <code>e</code> is changed.</p>
     * 
     * <p>This method is extremely long and messy, reflecting the messiness of
     * the type system and the AST.  As a result, it tries to fail well in
     * unexpected circumstances by throwing an UnsupportedOperationException in
     * cases where it encounters something it is not equipped to handle.</p>
     * 
     * <p>If myBindDebugFlag is on, this will print some informative messages in
     * certain cases where it is unable to bind.</p>
     * 
     * @param pattern The pattern to attempt to bind.
     * @param e The concrete expression to attempt to bind to.
     * @param typer Provides typing information.
     * 
     * @return A <code>Map</code> from <code>PosSymbol</code>s in the pattern
     *         to concrete <code>Exp</code>s from the expression.
     *         
     * @throws NullPointerException If <code>pattern</code> or <code>e</code> is
     *                              null.
     */
    public static Map<PosSymbol, Exp> bind(Exp pattern, Exp e) {

        //We need typing information to do our job, however the Analyzer doesn't
        //work consistently.  Fail well if we can't do our job.
        if (pattern.getType() == null || e.getType() == null) {
            if (!(e instanceof InfixExp || e instanceof BetweenExp
                    || e instanceof PrefixExp || e instanceof EqualsExp || e instanceof DotExp)) {
                //if (!(e instanceof InfixExp || e instanceof BetweenExp)) {
                throw new UnsupportedOperationException(
                        "Pattern or expression has null type.\n\n"
                                + "Pattern: " + pattern + "  ("
                                + pattern.getType() + ") " + pattern.getClass()
                                + "\nExpression: " + e + "  (" + e.getType()
                                + ") " + e.getClass());
            }
        }

        Map<PosSymbol, Exp> retval = null;

        if (pattern instanceof VarExp) {
            VarExp patternVar = (VarExp) pattern;

            if (patternVar.getQuantification() == QuantExp.FORALL) {
                if (pattern.getType() != null && e.getType() != null
                        && e.getMathType().isSubtypeOf(pattern.getMathType())) {

                    retval = new HashMap<PosSymbol, Exp>();
                    retval.put(patternVar.getName(), e);
                }

                if (retval == null && myBindDebugFlag) {
                    System.out.println("Prover.Utilities.bind() reports: "
                            + "Cannot bind " + patternVar + " with " + e
                            + " because:");

                    if (pattern.getType() == null || e.getType() == null) {
                        if (pattern.getType() == null) {
                            System.out.println("\t" + patternVar
                                    + "'s type is null.");
                        }
                        if (e.getType() == null) {
                            System.out.println("\t" + e + "'s type is null.");
                        }
                    }
                    else if (!e.getMathType()
                            .isSubtypeOf(pattern.getMathType())) {
                        System.out.println("\tType " + pattern.getType()
                                + " does not match type " + e.getType() + ".");
                    }
                }
            }
            else if (patternVar.getQuantification() == QuantExp.EXISTS) {
                if (e instanceof VarExp) {
                    System.out.println("Utilities.bind");
                }

                if (pattern.getType() != null && e.getType() != null
                        && e.getMathType().isSubtypeOf(pattern.getMathType())) {

                    if (e instanceof VarExp) {
                        VarExp eAsVarExp = (VarExp) e;
                        if (eAsVarExp.getQuantification() == QuantExp.NONE) {
                            retval = new HashMap<PosSymbol, Exp>();
                            retval.put(patternVar.getName(), e);
                        }
                    }
                    else {
                        retval = new HashMap<PosSymbol, Exp>();
                        retval.put(patternVar.getName(), e);
                    }
                }
            }
            else {
                if (pattern.equals(e)) {
                    retval = new HashMap<PosSymbol, Exp>();
                }
            }
        }
        else if (pattern instanceof FunctionExp) {
            FunctionExp patternFunction = (FunctionExp) pattern;

            if (e instanceof FunctionExp) {
                FunctionExp eFunction = (FunctionExp) e;

                if (patternFunction.getQuantification() == QuantExp.FORALL) {
                    if (pattern.getType() != null
                            && e.getType() != null
                            && e.getMathType().isSubtypeOf(
                                    pattern.getMathType())) {

                        retval = bindSubExpressions(pattern, e);

                        if (retval != null) {

                            VarExp functionName =
                                    new VarExp(eFunction.getLocation(),
                                            eFunction.getQualifier(), eFunction
                                                    .getName());
                            functionName.setType(eFunction.getType());

                            retval.put(patternFunction.getName(), functionName);
                        }
                    }
                }
                else {
                    if (Exp.posSymbolEquivalent(patternFunction.getName(),
                            eFunction.getName())) {

                        retval = bindSubExpressions(pattern, e);
                    }
                }
            }
        }
        else if (pattern instanceof InfixExp) {
            InfixExp patternInfix = (InfixExp) pattern;

            if (e instanceof InfixExp) {

                InfixExp eInfix = (InfixExp) e;

                if (Exp.posSymbolEquivalent(patternInfix.getOpName(), eInfix
                        .getOpName())) {

                    retval = bindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof OutfixExp) {
            OutfixExp patternOutfix = (OutfixExp) pattern;

            if (e instanceof OutfixExp) {
                OutfixExp eOutfix = (OutfixExp) e;

                if (patternOutfix.getOperator() == eOutfix.getOperator()) {
                    retval = bindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof PrefixExp) {
            PrefixExp patternPrefix = (PrefixExp) pattern;

            if (e instanceof PrefixExp) {
                PrefixExp ePrefix = (PrefixExp) e;

                if (Exp.posSymbolEquivalent(patternPrefix.getSymbol(), ePrefix
                        .getSymbol())) {

                    retval = bindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof EqualsExp) {
            EqualsExp patternAsEqualsExp = (EqualsExp) pattern;

            if (e instanceof EqualsExp) {
                EqualsExp eAsEqualsExp = (EqualsExp) e;

                if (eAsEqualsExp.getOperator() == patternAsEqualsExp
                        .getOperator()) {

                    retval = bindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof IntegerExp) {
            IntegerExp patternAsInteger = (IntegerExp) pattern;

            if (e instanceof IntegerExp) {
                IntegerExp eAsInteger = (IntegerExp) e;

                if (patternAsInteger.getValue() == eAsInteger.getValue()) {
                    retval = bindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof DotExp) {
            retval = bindSubExpressions(pattern, e);
        }
        else {

            if (!genericBindWarningPrinted) {
                System.out.println("WARNING: Binding generic class "
                        + pattern.getClass() + " by subexpressions only in "
                        + "proving.Utilities.bind.");
                genericBindWarningPrinted = true;
            }

            if (pattern.getClass().equals(e.getClass())) {
                retval = bindSubExpressions(pattern, e);
            }
        }

        return retval;
    }

    /**
     * <p>The original bind mapped from PosSymbols to Exp, which required the
     * PosSymbol keys to be wrapped BACK into Exps before they could be sent to
     * substitute.  This one goes from Exp to Exp.  The original bind should
     * eventually be deleted (and this one renamed) once the new prover is ready
     * for production. (TODO) Apr 26 2010</p>
     * 
     * <p>Attempts to bind <code>pattern</code> to a concrete expression,
     * <code>e</code>, returning a mapping from the quantified symbols in 
     * <code>pattern</code> to the concrete subexpressions they were bound to.  
     * If a binding is not possible, returns <code>null</code>.</p>
     * 
     * <p>Neither <code>pattern</code> nor <code>e</code> is changed.</p>
     * 
     * <p>This method is extremely long and messy, reflecting the messiness of
     * the type system and the AST.  As a result, it tries to fail well in
     * unexpected circumstances by throwing an UnsupportedOperationException in
     * cases where it encounters something it is not equipped to handle.</p>
     * 
     * <p>If myBindDebugFlag is on, this will print some informative messages in
     * certain cases where it is unable to bind.</p>
     * 
     * @param pattern The pattern to attempt to bind.
     * @param e The concrete expression to attempt to bind to.
     * @param typer Provides typing information.
     * 
     * @return A <code>Map</code> from <code>PosSymbol</code>s in the pattern
     *         to concrete <code>Exp</code>s from the expression.
     *         
     * @throws NullPointerException If <code>pattern</code> or <code>e</code> is
     *                              null.
     */
    public static Map<Exp, Exp> newBind(Exp pattern, Exp e) {

        //We need typing information to do our job, however the Analyzer doesn't
        //work consistently.  Fail well if we can't do our job.
        if (pattern.getType() == null || e.getType() == null) {
            if (!(e instanceof InfixExp || e instanceof BetweenExp
                    || e instanceof PrefixExp || e instanceof EqualsExp || e instanceof EqualsExp)) {
                //if (!(e instanceof InfixExp || e instanceof BetweenExp)) {
                throw new UnsupportedOperationException(
                        "Pattern or expression has null type.\n\n"
                                + "Pattern: " + pattern + "  ("
                                + pattern.getType() + ") " + pattern.getClass()
                                + "\nExpression: " + e + "  (" + e.getType()
                                + ") " + e.getClass());
            }
        }

        Map<Exp, Exp> retval = null;

        if (pattern instanceof VarExp) {
            VarExp patternVar = (VarExp) pattern;

            if (patternVar.getQuantification() == QuantExp.FORALL) {
                if (pattern.getType() != null && e.getType() != null
                        && e.getMathType().isSubtypeOf(pattern.getMathType())) {

                    retval = new HashMap<Exp, Exp>();
                    retval.put(patternVar, e);
                }

                if (retval == null && myBindDebugFlag) {
                    System.out.println("Prover.Utilities.bind() reports: "
                            + "Cannot bind " + patternVar + " with " + e
                            + " because:");

                    if (pattern.getType() == null || e.getType() == null) {
                        if (pattern.getType() == null) {
                            System.out.println("\t" + patternVar
                                    + "'s type is null.");
                        }
                        if (e.getType() == null) {
                            System.out.println("\t" + e + "'s type is null.");
                        }
                    }
                    else if (!e.getMathType()
                            .isSubtypeOf(pattern.getMathType())) {
                        System.out.println("\tType " + pattern.getType()
                                + " does not match type " + e.getType() + ".");
                    }
                }
            }
            else if (patternVar.getQuantification() == QuantExp.EXISTS) {
                if (e instanceof VarExp) {
                    System.out.println("Utilities.bind");
                }

                if (pattern.getType() != null && e.getType() != null
                        && e.getMathType().isSubtypeOf(pattern.getMathType())) {

                    if (e instanceof VarExp) {
                        VarExp eAsVarExp = (VarExp) e;
                        if (eAsVarExp.getQuantification() == QuantExp.NONE) {
                            retval = new HashMap<Exp, Exp>();
                            retval.put(patternVar, e);
                        }
                    }
                    else {
                        retval = new HashMap<Exp, Exp>();
                        retval.put(patternVar, e);
                    }
                }
            }
            else {
                if (pattern.equals(e)) {
                    retval = new HashMap<Exp, Exp>();
                }
            }
        }
        else if (pattern instanceof FunctionExp) {
            FunctionExp patternFunction = (FunctionExp) pattern;

            if (e instanceof FunctionExp) {
                FunctionExp eFunction = (FunctionExp) e;

                if (patternFunction.getQuantification() == QuantExp.FORALL) {
                    if (pattern.getType() != null
                            && e.getType() != null
                            && e.getMathType().isSubtypeOf(
                                    pattern.getMathType())) {

                        retval = newBindSubExpressions(pattern, e);

                        if (retval != null) {

                            VarExp functionName =
                                    new VarExp(eFunction.getLocation(),
                                            eFunction.getQualifier(), eFunction
                                                    .getName());
                            functionName.setType(eFunction.getType());

                            retval.put(patternFunction, functionName);
                        }
                    }
                }
                else {
                    if (Exp.posSymbolEquivalent(patternFunction.getName(),
                            eFunction.getName())) {

                        retval = newBindSubExpressions(pattern, e);
                    }
                }
            }
        }
        else if (pattern instanceof InfixExp) {
            InfixExp patternInfix = (InfixExp) pattern;

            if (e instanceof InfixExp) {

                InfixExp eInfix = (InfixExp) e;

                if (Exp.posSymbolEquivalent(patternInfix.getOpName(), eInfix
                        .getOpName())) {

                    retval = newBindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof OutfixExp) {
            OutfixExp patternOutfix = (OutfixExp) pattern;

            if (e instanceof OutfixExp) {
                OutfixExp eOutfix = (OutfixExp) e;

                if (patternOutfix.getOperator() == eOutfix.getOperator()) {

                    retval = newBindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof PrefixExp) {
            PrefixExp patternPrefix = (PrefixExp) pattern;

            if (e instanceof PrefixExp) {
                PrefixExp ePrefix = (PrefixExp) e;

                if (Exp.posSymbolEquivalent(patternPrefix.getSymbol(), ePrefix
                        .getSymbol())) {

                    retval = newBindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof EqualsExp) {
            EqualsExp patternAsEqualsExp = (EqualsExp) pattern;

            if (e instanceof EqualsExp) {
                EqualsExp eAsEqualsExp = (EqualsExp) e;

                if (eAsEqualsExp.getOperator() == patternAsEqualsExp
                        .getOperator()) {

                    retval = newBindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof IntegerExp) {
            IntegerExp patternAsInteger = (IntegerExp) pattern;

            if (e instanceof IntegerExp) {
                IntegerExp eAsInteger = (IntegerExp) e;

                if (patternAsInteger.getValue() == eAsInteger.getValue()) {

                    retval = newBindSubExpressions(pattern, e);
                }
            }
        }
        else if (pattern instanceof DotExp) {

            retval = newBindSubExpressions(pattern, e);
        }
        else {

            if (!genericBindWarningPrinted) {
                System.out.println("WARNING: Binding generic class "
                        + pattern.getClass() + " by subexpressions only in "
                        + "proving.Utilities.bind.");
                genericBindWarningPrinted = true;
            }

            if (pattern.getClass().equals(e.getClass())) {

                retval = newBindSubExpressions(pattern, e);
            }
        }

        return retval;
    }

    private static Map<PosSymbol, Exp> bindSubExpressions(Exp pattern, Exp e) {
        Map<PosSymbol, Exp> retval = new HashMap<PosSymbol, Exp>();

        Iterator<Exp> patternSubExpressions =
                pattern.getSubExpressions().iterator();
        Iterator<Exp> eSubExpressions = e.getSubExpressions().iterator();
        Map<PosSymbol, Exp> subBinding;
        while (retval != null && eSubExpressions.hasNext()
                && patternSubExpressions.hasNext()) {
            subBinding =
                    bind(patternSubExpressions.next(), eSubExpressions.next());

            if (subBinding == null) {
                retval = null;
            }
            else {
                retval = unifyMaps(retval, subBinding);
            }
        }

        if (patternSubExpressions.hasNext() || eSubExpressions.hasNext()) {
            retval = null;
        }

        return retval;
    }

    /**
     * <p>(TODO) See note at newBind.</p>
     * @param pattern
     * @param e
     * @param typer
     * @return
     */
    private static Map<Exp, Exp> newBindSubExpressions(Exp pattern, Exp e) {
        Map<Exp, Exp> retval = new HashMap<Exp, Exp>();

        Iterator<Exp> patternSubExpressions =
                pattern.getSubExpressions().iterator();
        Iterator<Exp> eSubExpressions = e.getSubExpressions().iterator();
        Map<Exp, Exp> subBinding;
        while (retval != null && eSubExpressions.hasNext()
                && patternSubExpressions.hasNext()) {
            subBinding =
                    newBind(patternSubExpressions.next(), eSubExpressions
                            .next());

            if (subBinding == null) {
                retval = null;
            }
            else {
                retval = newUnifyMaps(retval, subBinding);
            }
        }

        if (patternSubExpressions.hasNext() || eSubExpressions.hasNext()) {
            retval = null;
        }

        return retval;
    }

    private static Map<PosSymbol, Exp> unifyMaps(Map<PosSymbol, Exp> m1,
            Map<PosSymbol, Exp> m2) {
        Set<Map.Entry<PosSymbol, Exp>> entrySet = m2.entrySet();
        Iterator<Map.Entry<PosSymbol, Exp>> entryIter = entrySet.iterator();

        Exp otherExp;
        Map<PosSymbol, Exp> retval = m1;
        Map.Entry<PosSymbol, Exp> entry;
        while (retval != null && entryIter.hasNext()) {
            entry = entryIter.next();

            otherExp = mapContainsPosSymbol(retval, entry.getKey());

            if (otherExp != null) {
                if (!otherExp.equivalent(entry.getValue())) {
                    retval = null;
                }
            }
            else {
                retval.put(entry.getKey(), entry.getValue());
            }
        }

        return retval;
    }

    /**
     * <p>(TODO) See newBind</p>
     * @param m1
     * @param m2
     * @return
     */
    private static Map<Exp, Exp> newUnifyMaps(Map<Exp, Exp> m1, Map<Exp, Exp> m2) {
        Set<Map.Entry<Exp, Exp>> entrySet = m2.entrySet();
        Iterator<Map.Entry<Exp, Exp>> entryIter = entrySet.iterator();

        Exp otherExp;
        Map<Exp, Exp> retval = m1;
        Map.Entry<Exp, Exp> entry;
        while (retval != null && entryIter.hasNext()) {
            entry = entryIter.next();

            otherExp = mapContainsExp(retval, entry.getKey());

            if (otherExp != null) {
                if (!otherExp.equivalent(entry.getValue())) {
                    retval = null;
                }
            }
            else {
                retval.put(entry.getKey(), entry.getValue());
            }
        }

        return retval;
    }

    /**
     * <p>(TODO) See newBind.</p>
     * @param m
     * @param k
     * @return
     */
    private static Exp mapContainsExp(Map<Exp, Exp> m, Exp k) {
        Exp retval = null;

        Iterator<Exp> i = m.keySet().iterator();
        Exp curKey;
        while (i.hasNext() && retval == null) {
            curKey = i.next();
            if (curKey.equivalent(k)) {
                retval = m.get(curKey);
            }
        }

        return retval;
    }

    private static Exp mapContainsPosSymbol(Map<PosSymbol, Exp> m, PosSymbol k) {
        Exp retval = null;

        Iterator<PosSymbol> i = m.keySet().iterator();
        PosSymbol curKey;
        while (i.hasNext() && retval == null) {
            curKey = i.next();
            if (curKey.getName().equals(k.getName())) {
                retval = m.get(curKey);
            }
        }

        return retval;
    }

    /**
     * <p>Takes an expression and distributes its quantifiers down to the
     * variables, removing the quantifiers in the process.  (That is, in the
     * expression <code>For all x, x = y</code>, the variable <code>x</code>
     * in the equals expression would be marked internaly as a "for all"
     * variable and the expression <code>x = y</code> would be returned.)
     * <code>For all</code> quantifiers are not permitted to have a "where"
     * clause (that is, they must be total).  If a <code>for all</code> with a
     * "where" clause is detected, an <code>IllegalArgumentException</code>
     * will be thrown.</p>
     * 
     * @param e The expression for whom quantifiers should be distributed
     *          downward.  This will largely be modified in place.
     *          
     * @return The root of the new expression.
     * 
     * @throw IllegalArgumentException If <code>e</code> contains a
     *    <code>for all</code> expression with a "where" clause.
     */
    public static Exp applyQuantification(Exp e) {
        Map<String, Integer> quantifiedVariables =
                new HashMap<String, Integer>();

        return applyQuantification(e, quantifiedVariables);
    }

    /**
     * <p>Takes an expression and distributes its quantifiers down to the
     * variables, removing the quantifiers in the process.  (That is, in the
     * expression <code>For all x, x = y</code>, the variable <code>x</code>
     * in the equals expression would be marked internaly as a "for all"
     * variable and the expression <code>x = y</code> would be returned.)</p>
     * 
     * <p>Quantified expressiong with "where" clauses will be normalized to
     * remove the where clause.  In the case of "for all" statements, the
     * where clause will be added as the antecedent to an implication, so that
     * <code>For all x, where Q(x), P(x)</code> will become <code>For all x,
     * Q(x) implies P(x)</code> and <code>There exists x, where Q(x), such that
     * P(x)</code> will become <code>There exists x such that Q(x) and P(x)
     * </code>.</p>
     * 
     * @param e The expression for whom quantifiers should be distributed
     *          downward.  This will largely be modified in place.
     * @param quantifiedVariables A map of variable names that should be 
     *          considered quantified mapped to the quantifiers that apply to
     *          them. 
     *          
     * @return The root of the new expression. 
     */
    private static Exp applyQuantification(Exp e,
            Map<String, Integer> quantifiedVariables) {

        Exp retval;

        if (e instanceof QuantExp) {
            QuantExp eAsQuantifier = (QuantExp) e;

            //Normalize our eAsQuantifier so that it doesn't have a "where"
            //clause by appropriately transferring the "where" clause into
            //the body using logical connectives
            if (eAsQuantifier.getWhere() != null) {
                switch (eAsQuantifier.getOperator()) {
                case QuantExp.FORALL:
                    eAsQuantifier =
                            new QuantExp(eAsQuantifier.getLocation(),
                                    eAsQuantifier.getOperator(), eAsQuantifier
                                            .getVars(), null, Exp
                                            .buildImplication(eAsQuantifier
                                                    .getWhere(), eAsQuantifier
                                                    .getBody()));
                    break;
                case QuantExp.EXISTS:
                    eAsQuantifier =
                            new QuantExp(eAsQuantifier.getLocation(),
                                    eAsQuantifier.getOperator(), eAsQuantifier
                                            .getVars(), null, Exp
                                            .buildConjunction(eAsQuantifier
                                                    .getWhere(), eAsQuantifier
                                                    .getBody()));
                    break;
                default:
                    throw new RuntimeException("Don't know how to normalize "
                            + "this kind of quantified expression.");
                }
            }

            List<MathVarDec> variableNames = eAsQuantifier.getVars();

            for (MathVarDec v : variableNames) {
                quantifiedVariables.put(v.getName().getName(), eAsQuantifier
                        .getOperator());
            }

            retval =
                    applyQuantification(eAsQuantifier.getBody(),
                            quantifiedVariables);

            for (MathVarDec v : variableNames) {
                quantifiedVariables.remove((v.getName().getName()));
            }
        }
        else {
            if (e instanceof VarExp) {
                VarExp eAsVar = (VarExp) e;
                String varName = eAsVar.getName().getName();

                if (quantifiedVariables.containsKey(varName)) {
                    eAsVar.setQuantification(quantifiedVariables.get(varName));
                }
            }
            else {
                if (e instanceof FunctionExp) {
                    FunctionExp eAsFunctionExp = (FunctionExp) e;
                    String functionName = eAsFunctionExp.getName().getName();
                    if (quantifiedVariables.containsKey(functionName)) {
                        eAsFunctionExp.setQuantification(quantifiedVariables
                                .get(functionName));
                    }
                }

                List<Exp> subExpressions = e.getSubExpressions();
                int numSubExpressions = subExpressions.size();
                Exp curSubExpression;
                for (int curIndex = 0; curIndex < numSubExpressions; curIndex++) {

                    curSubExpression = subExpressions.get(curIndex);
                    e.setSubExpression(curIndex, applyQuantification(
                            curSubExpression, quantifiedVariables));
                }
            }

            retval = e;
        }

        return retval;
    }

    public static java.util.List<String> getQuantifiedVariables(Exp e) {
        java.util.List<String> variables = new LinkedList<String>();
        getQuantifiedVariables(e, variables);
        return variables;
    }

    public static java.util.List<String> getQuantifiedVariables(Iterable<Exp> es) {

        java.util.List<String> variables = new LinkedList<String>();
        getQuantifiedVariables(es, variables);
        return variables;
    }

    public static java.util.List<String> getQuantifiedVariables(
            Iterable<Exp> es, java.util.List<String> variables) {

        for (Exp e : es) {
            getQuantifiedVariables(e, variables);
        }

        return variables;
    }

    public static void getQuantifiedVariables(Exp e,
            java.util.List<String> accumulator) {

        if (e instanceof VarExp) {
            VarExp eAsVarExp = (VarExp) e;
            if (eAsVarExp.getQuantification() != VarExp.NONE) {
                addUnique(accumulator, eAsVarExp.toString());
            }
        }
        else {
            List<Exp> subexpressions = e.getSubExpressions();
            for (Exp subexpression : subexpressions) {
                getQuantifiedVariables(subexpression, accumulator);
            }
        }
    }

    public static void addUnique(java.util.List<String> l, String e) {
        if (!l.contains(e)) {
            l.add(e);
        }
    }
}
