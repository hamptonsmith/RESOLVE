package edu.clemson.cs.r2jt.mathtype;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.clemson.cs.r2jt.absyn.DefinitionDec;
import edu.clemson.cs.r2jt.absyn.Exp;
import edu.clemson.cs.r2jt.absyn.MathVarDec;
import edu.clemson.cs.r2jt.typereasoning.TypeComparison;
import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

public class MTFunction extends MTAbstract<MTFunction> {

    private static final int BASE_HASH = "MTFunction".hashCode();
    private static final FunctionApplicationFactory DEFAULT_FACTORY =
            new VanillaFunctionApplicationFactory();

    /**
     * <p>In cases where myDomain is an instance of MTCartesian, the names of
     * the original parameters are stored in the tags of that cartesian product.
     * However, when myDomain is another type, we represent a function with
     * a SINGLE PARAMETER and we have no way to embed the name of our parameter.
     * In the latter case, this field will reflect the parameter name (or be 
     * null if we represent a function with un-named parameters).  In the former
     * case, the value of this field is undefined.</p>
     */
    private final String mySingleParameterName;

    private final MTType myDomain;
    private final MTType myRange;
    private final boolean myRestrictionFlag;
    private final FunctionApplicationFactory myFunctionApplicationFactory;

    private List<MTType> myComponents;

    public MTFunction(TypeGraph g, MTType range, List<MTType> paramTypes,
            String singleParameterName) {
        this(g, false, DEFAULT_FACTORY, range, Collections
                .singletonList(singleParameterName), paramTypes);
    }

    public MTFunction(TypeGraph g, MTType range, MTType... paramTypes) {
        this(g, false, range, paramTypes);
    }

    public MTFunction(TypeGraph g, MTType range, List<MTType> paramTypes) {
        this(g, false, range, paramTypes);
    }

    /**
     * This assumes that d has some parameters in its .getParams().
     */
    public MTFunction(TypeGraph g, DefinitionDec d) {
        this(g, false, DEFAULT_FACTORY, d.getReturnTy().getMathTypeValue(),
                getParamNames(d.getParameters()), getParamTypes(d
                        .getParameters()));
    }

    public MTFunction(TypeGraph g, boolean elementsRestrict, MTType range,
            MTType... paramTypes) {

        this(g, false, DEFAULT_FACTORY, range, paramTypes);
    }

    public MTFunction(TypeGraph g, boolean elementsRestrict, MTType range,
            List<MTType> paramTypes) {

        this(g, false, DEFAULT_FACTORY, range, paramTypes);
    }

    public MTFunction(TypeGraph g, FunctionApplicationFactory apply,
            MTType range, MTType... paramTypes) {

        this(g, false, apply, range, paramTypes);
    }

    public MTFunction(TypeGraph g, FunctionApplicationFactory apply,
            MTType range, List<MTType> paramTypes) {

        this(g, false, apply, range, paramTypes);
    }

    public MTFunction(TypeGraph g, boolean elementsRestrict,
            FunctionApplicationFactory apply, MTType range,
            MTType... paramTypes) {

        this(g, elementsRestrict, apply, range, Arrays.asList(paramTypes));
    }

    public MTFunction(TypeGraph g, boolean elementsRestrict,
            FunctionApplicationFactory apply, MTType range,
            List<MTType> paramTypes) {

        this(g, elementsRestrict, apply, range,
                buildNullNameListOfEqualLength(paramTypes), paramTypes);
    }

    private MTFunction(TypeGraph g, boolean elementsRestrict,
            FunctionApplicationFactory apply, MTType range,
            List<String> paramNames, List<MTType> paramTypes) {

        super(g);

        if (paramNames.size() == 1) {
            mySingleParameterName = paramNames.get(0);
        }
        else {
            mySingleParameterName = null;
        }

        myDomain = buildParameterType(g, paramNames, paramTypes);
        myRange = range;
        myRestrictionFlag = elementsRestrict;

        List<MTType> components = new LinkedList<MTType>();
        components.add(myDomain);
        components.add(myRange);
        myComponents = Collections.unmodifiableList(components);

        myFunctionApplicationFactory = apply;
    }

    public boolean parametersMatch(List<Exp> parameters,
            TypeComparison<Exp, MTType> comparison) {

        boolean result = false;

        if (myDomain == myTypeGraph.VOID) {
            result = (parameters.isEmpty());
        }
        else {
            if (myDomain instanceof MTCartesian) {
                MTCartesian domainAsMTCartesian = (MTCartesian) myDomain;

                int domainSize = domainAsMTCartesian.size();
                int parametersSize = parameters.size();

                result = (domainSize == parametersSize);

                if (result) {
                    int i = 0;
                    Exp parameter;
                    while (result && i < domainSize) {

                        parameter = parameters.get(i);
                        result =
                                comparison.compare(parameter, parameter
                                        .getMathType(), domainAsMTCartesian
                                        .getFactor(i));

                        i++;
                    }
                }
            }

            if (!result && (parameters.size() == 1)) {
                Exp parameter = parameters.get(0);
                result =
                        comparison.compare(parameter, parameter.getMathType(),
                                myDomain);
            }
        }

        return result;
    }

    public boolean parameterTypesMatch(MTFunction other,
            Comparator<MTType> comparison) {

        MTType otherDomain = other.getDomain();

        boolean result;

        if (myDomain instanceof MTCartesian) {
            result = otherDomain instanceof MTCartesian;

            if (result) {
                MTCartesian domainAsMTCartesian = (MTCartesian) myDomain;
                MTCartesian otherDomainAsMTCartesian =
                        (MTCartesian) otherDomain;

                int domainSize = domainAsMTCartesian.size();
                int otherDomainSize = otherDomainAsMTCartesian.size();

                result = (domainSize == otherDomainSize);

                if (result) {
                    int i = 0;
                    while (result && i < domainSize) {
                        result =
                                (comparison.compare(domainAsMTCartesian
                                        .getFactor(i), otherDomainAsMTCartesian
                                        .getFactor(i)) == 0);

                        i++;
                    }
                }
            }
        }
        else {
            result = (comparison.compare(myDomain, otherDomain) == 0);
        }

        return result;
    }

    private static List<String> getParamNames(
            edu.clemson.cs.r2jt.collections.List<MathVarDec> params) {

        List<String> names = new LinkedList<String>();

        for (MathVarDec d : params) {
            names.add(d.getName().getName());
        }

        return names;
    }

    private static List<MTType> getParamTypes(
            edu.clemson.cs.r2jt.collections.List<MathVarDec> params) {

        List<MTType> names = new LinkedList<MTType>();

        for (MathVarDec d : params) {
            names.add(d.getMathType());
        }

        return names;
    }

    private static List<String> buildNullNameListOfEqualLength(
            List<MTType> original) {

        List<String> names = new LinkedList<String>();
        for (@SuppressWarnings("unused")
        MTType t : original) {
            names.add(null);
        }

        return names;
    }

    public MTType getDomain() {
        return myDomain;
    }

    public MTType getRange() {
        return myRange;
    }

    public String getSingleParameterName() {
        return mySingleParameterName;
    }

    public MTType getApplicationType(String calledAsName, List<MTType> arguments) {

        return myFunctionApplicationFactory.buildFunctionApplication(
                myTypeGraph, this, calledAsName, arguments);
    }

    @Override
    public boolean isKnownToContainOnlyMTypes() {
        return false;
    }

    public boolean applicationResultsKnownToContainOnlyRestrictions() {
        return myRestrictionFlag;
    }

    @Override
    public int getHashCode() {
        return BASE_HASH + (myDomain.hashCode() * 31) + myRange.hashCode();
    }

    @Override
    public String toString() {
        return "(" + myDomain.toString() + " -> " + myRange.toString() + ")";
    }

    @Override
    public void accept(TypeVisitor v) {
        v.beginMTType(this);
        v.beginMTAbstract(this);
        v.beginMTFunction(this);

        v.beginChildren(this);

        myDomain.accept(v);
        myRange.accept(v);

        v.endChildren(this);

        v.endMTFunction(this);
        v.endMTAbstract(this);
        v.endMTType(this);
    }

    @Override
    public List<MTType> getComponentTypes() {
        return myComponents;
    }

    @Override
    public MTType withComponentReplaced(int index, MTType newType) {
        MTType newDomain = myDomain;
        MTType newRange = myRange;

        switch (index) {
        case 0:
            newDomain = newType;
            break;
        case 1:
            newRange = newType;
            break;
        default:
            throw new IndexOutOfBoundsException();
        }

        return new MTFunction(getTypeGraph(), myRestrictionFlag, newRange,
                newDomain);
    }

    public static final MTType buildParameterType(TypeGraph g,
            List<MTType> paramTypes) {

        return buildParameterType(g,
                buildNullNameListOfEqualLength(paramTypes), paramTypes);
    }

    public static final MTType buildParameterType(TypeGraph g,
            List<String> paramNames, List<MTType> paramTypes) {

        MTType result;

        switch (paramTypes.size()) {
        case 0:
            result = g.VOID;
            break;
        case 1:
            result = paramTypes.get(0);
            break;
        default:
            List<MTCartesian.Element> elements =
                    new LinkedList<MTCartesian.Element>();

            Iterator<String> namesIter = paramNames.iterator();
            Iterator<MTType> typesIter = paramTypes.iterator();
            while (namesIter.hasNext()) {
                elements.add(new MTCartesian.Element(namesIter.next(),
                        typesIter.next()));
            }

            result = new MTCartesian(g, elements);
        }

        return result;
    }

    private static class VanillaFunctionApplicationFactory
            implements
                FunctionApplicationFactory {

        @Override
        public MTType buildFunctionApplication(TypeGraph g, MTFunction f,
                String calledAsName, List<MTType> arguments) {
            return new MTFunctionApplication(g, f, calledAsName, arguments);
        }
    }
}
