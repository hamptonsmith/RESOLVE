package edu.clemson.cs.r2jt.mathtype;

import java.util.List;

/**
 * <p>A simple refinement on {@link TableSearch TableSearch} that guarantees
 * its method will not throw a {@link DuplicateSymbolException 
 * DuplicateSymbolException}.</p>
 */
public interface MultimatchTableSearcher<E extends SymbolTableEntry>
        extends
            TableSearcher<E> {

    /**
     * <p>Refines {@link TableSearcher#addMatches(SymbolTable, List) 
     * TableSearcher.addMatches()} to guarantee that it will not throw a
     * {@link DuplicateSymbolException DuplicateSymbolException}.  Otherwise,
     * behaves identically.</p>
     */
    public boolean addMatches(SymbolTable entries, List<E> matches);
}