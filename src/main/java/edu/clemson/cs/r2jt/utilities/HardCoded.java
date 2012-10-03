package edu.clemson.cs.r2jt.utilities;

import edu.clemson.cs.r2jt.absyn.VarExp;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.data.Symbol;
import edu.clemson.cs.r2jt.mathtype.DuplicateSymbolException;
import edu.clemson.cs.r2jt.mathtype.MTFunction;
import edu.clemson.cs.r2jt.mathtype.ScopeBuilder;
import edu.clemson.cs.r2jt.typereasoning.TypeGraph;

public class HardCoded {

    /**
     * <p>This method establishes all built-in symbols of the symbol table.</p>
     */
    public static void addBuiltInSymbols(TypeGraph g, ScopeBuilder b) {
        VarExp v = new VarExp();
        v.setName(new PosSymbol(null, Symbol.symbol("native")));

        try {
            b.addBinding("Entity", v, g.MTYPE, g.ENTITY);
            b.addBinding("MType", v, g.MTYPE, g.MTYPE);

            b.addBinding("SSet", v, g.MTYPE, g.SET);
            b.addBinding("B", v, g.MTYPE, g.BOOLEAN);

            b.addBinding("Empty_Set", v, g.MTYPE, g.EMPTY_SET);
            b.addBinding("Powerset", v, g.POWERTYPE);
            b.addBinding("true", v, g.BOOLEAN);
            b.addBinding("false", v, g.BOOLEAN);
            b.addBinding("union", v, g.UNION);
            b.addBinding("intersect", v, g.INTERSECT);
            b.addBinding("->", v, g.FUNCTION);
            b.addBinding("implies", v, g.BOOLEAN);
            b.addBinding("and", v, g.AND);
            b.addBinding("not", v, g.NOT);
            b.addBinding("*", v, g.CROSS);

            b.addBinding("=", v, new MTFunction(g, g.BOOLEAN, g.ENTITY,
                    g.ENTITY));
            b.addBinding("/=", v, new MTFunction(g, g.BOOLEAN, g.ENTITY,
                    g.ENTITY));
            b.addBinding("or", v, new MTFunction(g, g.BOOLEAN, g.BOOLEAN,
                    g.BOOLEAN));

            b.addBinding("Z", v, g.MTYPE, g.Z);
            b.addBinding("-", v, new MTFunction(g, g.Z, g.Z));
        }
        catch (DuplicateSymbolException dse) {
            //Not possible--we're the first ones to add anything
            throw new RuntimeException(dse);
        }
    }
}
