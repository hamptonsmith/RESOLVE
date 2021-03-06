package edu.clemson.cs.r2jt.typeandpopulate.query;

import edu.clemson.cs.r2jt.typeandpopulate.entry.SymbolTableEntry;
import edu.clemson.cs.r2jt.typeandpopulate.entry.MathSymbolEntry;
import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.typeandpopulate.MathSymbolTable.FacilityStrategy;
import edu.clemson.cs.r2jt.typeandpopulate.MathSymbolTable.ImportStrategy;
import edu.clemson.cs.r2jt.typeandpopulate.searchers.NameSearcher;
import edu.clemson.cs.r2jt.typeandpopulate.PossiblyQualifiedPath;
import edu.clemson.cs.r2jt.utilities.Mapping;

public class MathSymbolQuery
        extends
            ResultProcessingQuery<SymbolTableEntry, MathSymbolEntry> {

    public MathSymbolQuery(PosSymbol qualifier, PosSymbol name) {
        this(qualifier, name.getName(), name.getLocation());
    }

    public MathSymbolQuery(PosSymbol qualifier, String name, Location nameLoc) {
        super(new BaseSymbolQuery<SymbolTableEntry>(new PossiblyQualifiedPath(
                qualifier, ImportStrategy.IMPORT_NAMED,
                FacilityStrategy.FACILITY_IGNORE, true), new NameSearcher(name,
                true)), new MapToMathSymbol(nameLoc));
    }

    private static class MapToMathSymbol
            implements
                Mapping<SymbolTableEntry, MathSymbolEntry> {

        private final Location myNameLocation;

        public MapToMathSymbol(Location l) {
            myNameLocation = l;
        }

        @Override
        public MathSymbolEntry map(SymbolTableEntry input) {
            return input.toMathSymbolEntry(myNameLocation);
        }
    }
}
