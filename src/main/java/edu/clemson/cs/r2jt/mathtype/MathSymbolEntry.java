package edu.clemson.cs.r2jt.mathtype;

import java.util.Map;

import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

public class MathSymbolEntry extends SymbolTableEntry {

    private final MTType myType;
    private final MTType myTypeValue;
    private final Quantification myQuantification;

    public MathSymbolEntry(TypeGraph g, String name, Quantification q,
            ResolveConceptualElement definingElement, MTType type,
            MTType typeValue, ModuleIdentifier sourceModule) {
        super(name, definingElement, sourceModule);

        myType = type;
        myQuantification = q;
        if (typeValue != null) {
            myTypeValue = typeValue;
        }
        else if (type.isKnownToContainOnlyMTypes()) {
            myTypeValue =
                    new MTProper(g, type, type
                            .membersKnownToContainOnlyMTypes(), name);
        }
        else {
            myTypeValue = null;
        }
    }

    public MTType getType() {
        return myType;
    }

    public Quantification getQuantification() {
        return myQuantification;
    }

    public MTType getTypeValue() throws SymbolNotOfKindTypeException {
        if (myTypeValue == null) {
            throw new SymbolNotOfKindTypeException();
        }

        return myTypeValue;
    }

    @Override
    public String toString() {
        return getSourceModuleIdentifier() + "." + getName() + "\t\t"
                + myQuantification + "\t\tOf type: " + myType
                + "\t\t Defines type: " + myTypeValue;
    }

    public MathSymbolEntry toMathSymbolEntry(Location l) {
        return this;
    }

    public String getEntryTypeDescription() {
        return "a math symbol";
    }

    @Override
    public MathSymbolEntry instantiateGenerics(
            Map<String, PTType> genericInstantiations,
            FacilityEntry instantiatingFacility) {

        Map<String, MTType> genericMathematicalInstantiations =
                SymbolTableEntry.buildMathTypeGenerics(genericInstantiations);

        VariableReplacingVisitor typeSubstitutor =
                new VariableReplacingVisitor(genericMathematicalInstantiations);
        myType.accept(typeSubstitutor);

        MTType instantiatedTypeValue = null;
        if (myTypeValue != null) {
            VariableReplacingVisitor typeValueSubstitutor =
                    new VariableReplacingVisitor(
                            genericMathematicalInstantiations);
            myTypeValue.accept(typeValueSubstitutor);
            instantiatedTypeValue = typeValueSubstitutor.getFinalExpression();
        }

        return new MathSymbolEntry(myType.getTypeGraph(), getName(),
                getQuantification(), getDefiningElement(), typeSubstitutor
                        .getFinalExpression(), instantiatedTypeValue,
                getSourceModuleIdentifier());
    }
}