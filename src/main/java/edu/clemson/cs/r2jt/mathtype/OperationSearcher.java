package edu.clemson.cs.r2jt.mathtype;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.clemson.cs.r2jt.data.Location;
import edu.clemson.cs.r2jt.data.PosSymbol;
import edu.clemson.cs.r2jt.proving.immutableadts.ImmutableList;

public class OperationSearcher implements TableSearcher<OperationEntry> {

    private final String myQueryName;
    private final Location myQueryLocation;
    private final List<PTType> myActualArgumentTypes;

    public OperationSearcher(PosSymbol name, List<PTType> argumentTypes) {
        myQueryName = name.getName();
        myQueryLocation = name.getLocation();
        myActualArgumentTypes = new LinkedList<PTType>(argumentTypes);
    }

    @Override
    public boolean addMatches(SymbolTable entries, List<OperationEntry> matches)
            throws DuplicateSymbolException {

        if (entries.containsKey(myQueryName)) {
            OperationEntry operation =
                    entries.get(myQueryName).toOperationEntry(myQueryLocation);

            if (argumentsMatch(operation.getParameters())) {
                //We have a match at this point
                if (!matches.isEmpty()) {
                    throw new DuplicateSymbolException();
                }

                matches.add(operation);
            }
        }

        return false;
    }

    private boolean argumentsMatch(
            ImmutableList<ProgramParameterEntry> formalParameters) {

        boolean result =
                (formalParameters.size() == myActualArgumentTypes.size());

        if (result) {
            Iterator<ProgramParameterEntry> formalParametersIter =
                    formalParameters.iterator();
            Iterator<PTType> actualArgumentTypeIter =
                    myActualArgumentTypes.iterator();

            PTType actualArgumentType, formalParameterType;
            while (result && formalParametersIter.hasNext()) {
                actualArgumentType = actualArgumentTypeIter.next();
                formalParameterType =
                        formalParametersIter.next().getDeclaredType();

                result = actualArgumentType.equals(formalParameterType);
            }
        }

        return result;
    }
}
