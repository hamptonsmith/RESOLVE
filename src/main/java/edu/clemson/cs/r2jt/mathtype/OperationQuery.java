package edu.clemson.cs.r2jt.mathtype;

import java.util.List;

import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTable.FacilityStrategy;
import edu.clemson.cs.r2jt.mathtype.MathSymbolTable.ImportStrategy;

public class OperationQuery extends BaseSymbolQuery<OperationEntry> {

    public OperationQuery(PosSymbol qualifier, PosSymbol name,
            List<PTType> argumentTypes) {
        super(new PossiblyQualifiedPath(qualifier, ImportStrategy.IMPORT_NAMED,
                FacilityStrategy.FACILITY_INSTANTIATE, false),
                new OperationSearcher(name, argumentTypes));
    }
}
