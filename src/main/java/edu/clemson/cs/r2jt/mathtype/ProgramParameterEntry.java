package edu.clemson.cs.r2jt.mathtype;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.clemson.cs.r2jt.absyn.ResolveConceptualElement;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.Mode;

public class ProgramParameterEntry extends SymbolTableEntry {

    public static enum ParameterMode {
        ALTERS, UPDATES, REPLACES, CLEARS, RESTORES, PRESERVES, EVALUATES, TYPE
    }

    public final static Map<Mode, ParameterMode> OLD_TO_NEW_MODE;

    static {
        Map<Mode, ParameterMode> mapping = new HashMap<Mode, ParameterMode>();

        mapping.put(Mode.ALTERS, ParameterMode.ALTERS);
        mapping.put(Mode.UPDATES, ParameterMode.UPDATES);
        mapping.put(Mode.REPLACES, ParameterMode.REPLACES);
        mapping.put(Mode.CLEARS, ParameterMode.CLEARS);
        mapping.put(Mode.RESTORES, ParameterMode.RESTORES);
        mapping.put(Mode.PRESERVES, ParameterMode.PRESERVES);
        mapping.put(Mode.EVALUATES, ParameterMode.EVALUATES);

        OLD_TO_NEW_MODE = Collections.unmodifiableMap(mapping);
    }

    private final PTType myDeclaredType;
    private final ParameterMode myPassingMode;

    private final MathSymbolEntry myMathSymbolAlterEgo;

    public ProgramParameterEntry(String name,
            ResolveConceptualElement definingElement,
            ModuleIdentifier sourceModule, PTType type, ParameterMode mode) {
        super(name, definingElement, sourceModule);

        myDeclaredType = type;
        myPassingMode = mode;

        MTType typeValue = null;
        if (mode == ParameterMode.TYPE) {
            typeValue = new PTGeneric(type.getTypeGraph(), name).toMath();
        }

        myMathSymbolAlterEgo =
                new MathSymbolEntry(type.getTypeGraph(), name,
                        Quantification.NONE, definingElement, type.toMath(),
                        typeValue, sourceModule);
    }

    @Override
    public ProgramParameterEntry toProgramParameterEntry(Location l) {
        return this;
    }

    @Override
    public MathSymbolEntry toMathSymbolEntry(Location l) {
        return myMathSymbolAlterEgo;
    }

    public ParameterMode getParameterMode() {
        return myPassingMode;
    }

    public PTType getDeclaredType() {
        return myDeclaredType;
    }

    @Override
    public String getEntryTypeDescription() {
        return "a program parameter";
    }

    @Override
    public SymbolTableEntry instantiateGenerics(
            Map<String, PTType> genericInstantiations,
            FacilityEntry instantiatingFacility) {

        return new ProgramParameterEntry(getName(), getDefiningElement(),
                getSourceModuleIdentifier(), myDeclaredType
                        .instantiateGenerics(genericInstantiations,
                                instantiatingFacility), myPassingMode);
    }
}
