package edu.clemson.cs.r2jt.type;

import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.typeandpopulate.MTType;
import edu.clemson.cs.r2jt.scope.Binding;
import edu.clemson.cs.r2jt.scope.ScopeID;

/**
 * <p>This class attempts to wrap one of the new types from the 
 * <code>MTType</code> hierarchy so that it can be used by the old type system.
 * This class simply exists to ease the transition from old type system to new.
 * Ultimately there should be no need for this class--nobody will use the old
 * type system.</p>
 */
public class NewType extends Type {

    private final MTType myMTType;

    public NewType(MTType mtType) {
        myMTType = mtType;
    }

    public MTType getWrappedType() {
        return myMTType;
    }

    @Override
    public NewType instantiate(ScopeID sid, Binding binding) {
        return this;
    }

    @Override
    public TypeName getProgramName() {
        throw new UnsupportedOperationException(
                "NewType doesn't support this operation.");
    }

    @Override
    public String getRelativeName(Location loc) {
        throw new UnsupportedOperationException(
                "NewType doesn't support this operation.");
    }

    @Override
    public NewType toMath() {
        return this;
    }

    @Override
    public String asString() {
        return "" + myMTType;
    }

    @Override
    public String toString() {
        return "" + myMTType;
    }
}
